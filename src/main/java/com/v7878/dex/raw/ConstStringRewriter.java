package com.v7878.dex.raw;

import static com.v7878.dex.Opcode.ARRAY_PAYLOAD;
import static com.v7878.dex.Opcode.CONST_STRING;
import static com.v7878.dex.Opcode.CONST_STRING_JUMBO;
import static com.v7878.dex.Opcode.FILL_ARRAY_DATA;
import static com.v7878.dex.Opcode.RAW;
import static com.v7878.dex.WriteOptions.StringFix.FIX_ALL;
import static com.v7878.dex.WriteOptions.StringFix.FIX_JUMBO;
import static com.v7878.dex.WriteOptions.StringFix.NONE;
import static com.v7878.dex.raw.DexCollector.StringIndexer;
import static com.v7878.dex.util.MathUtils.uwidth;

import com.v7878.collections.IntMap;
import com.v7878.collections.IntSet;
import com.v7878.dex.WriteOptions.StringFix;
import com.v7878.dex.builder.CodeBuilder;
import com.v7878.dex.builder.CodeBuilder.Test;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction21t;
import com.v7878.dex.immutable.bytecode.Instruction22t;
import com.v7878.dex.immutable.bytecode.Instruction31c;
import com.v7878.dex.immutable.bytecode.Instruction31t;
import com.v7878.dex.immutable.bytecode.InstructionRaw;
import com.v7878.dex.immutable.bytecode.PackedSwitchPayload;
import com.v7878.dex.immutable.bytecode.SparseSwitchPayload;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.immutable.debug.AdvancePC;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.immutable.debug.EndLocal;
import com.v7878.dex.immutable.debug.LineNumber;
import com.v7878.dex.immutable.debug.RestartLocal;
import com.v7878.dex.immutable.debug.SetEpilogueBegin;
import com.v7878.dex.immutable.debug.SetFile;
import com.v7878.dex.immutable.debug.SetPrologueEnd;
import com.v7878.dex.immutable.debug.StartLocal;

import java.util.TreeMap;
import java.util.function.IntFunction;

public class ConstStringRewriter {
    private static Integer label(int offset) {
        return offset;
    }

    private static Integer debugLabel(int offset, IntSet positions, int after) {
        int index = positions.indexOf(offset);
        if (index < 0) {
            index = ~index - 1;
        }
        if (index >= positions.size()) {
            return after;
        }
        return label(positions.at(index));
    }

    private static void copyTryBlocks(CodeBuilder ib, Iterable<TryBlock> blocks) {
        for (var block : blocks) {
            int start = block.getStartAddress();
            int end = block.getEndAddress();
            for (var handler : block.getHandlers()) {
                ib.try_catch(
                        label(start),
                        label(end),
                        handler.getExceptionType(),
                        label(handler.getAddress())
                );
            }
            var catch_all = block.getCatchAllAddress();
            if (catch_all != null) {
                ib.try_catch_all(
                        label(start),
                        label(end),
                        label(catch_all)
                );
            }
        }
    }

    private static void copyDebugItems(
            CodeBuilder ib, Iterable<DebugItem> items, IntSet positions, int after) {
        int offset = 0;
        for (var debug : items) {
            if (debug instanceof AdvancePC item) {
                offset += item.getAddrDiff();
            } else if (debug instanceof LineNumber item) {
                ib.line(debugLabel(offset, positions, after), item.getLine());
            } else if (debug instanceof SetFile item) {
                ib.source(debugLabel(offset, positions, after), item.getName());
            } else if (debug instanceof StartLocal item) {
                ib.local(debugLabel(offset, positions, after), item.getRegister(), item.getName(),
                        item.getType(), item.getSignature());
            } else if (debug instanceof RestartLocal item) {
                ib.restart_local(debugLabel(offset, positions, after), item.getRegister());
            } else if (debug instanceof EndLocal item) {
                ib.end_local(debugLabel(offset, positions, after), item.getRegister());
            } else if (debug instanceof SetPrologueEnd) {
                ib.prologue(debugLabel(offset, positions, after));
            } else if (debug instanceof SetEpilogueBegin) {
                ib.epilogue(debugLabel(offset, positions, after));
            }
        }
    }

    private static void copyInstruction(CodeBuilder ib, StringIndexer strings, StringFix rewrite,
                                        int offset, Instruction insn, IntFunction<Instruction> code_map) {
        var opcode = insn.getOpcode();
        switch (opcode) {
            case CONST_STRING -> {
                var tmp = (Instruction21c) insn;
                var str = (String) tmp.getReference1();
                var reg = tmp.getRegister1();
                var index = strings.getStringIndex(str);
                if (uwidth(index, 16)) {
                    ib.raw_const_string(reg, str);
                } else {
                    ib.raw_const_string_jumbo(reg, str);
                }
            }
            case CONST_STRING_JUMBO -> {
                if (rewrite != FIX_ALL) {
                    ib.raw(insn);
                    break;
                }
                var tmp = (Instruction31c) insn;
                var str = (String) tmp.getReference1();
                var reg = tmp.getRegister1();
                var index = strings.getStringIndex(str);
                if (uwidth(index, 16)) {
                    ib.raw_const_string(reg, str);
                } else {
                    ib.raw_const_string_jumbo(reg, str);
                }
            }
            case GOTO, GOTO_16, GOTO_32 -> {
                var tmp = ((BranchOffsetInstruction) insn);
                ib.goto_(label(offset + tmp.getBranchOffset()));
            }
            case PACKED_SWITCH -> {
                var tmp = ((Instruction31t) insn);
                var payload = (PackedSwitchPayload) code_map.apply(offset + tmp.getBranchOffset());
                var branches = new TreeMap<Integer, Integer>();
                for (var entry : payload.getSwitchElements()) {
                    branches.put(entry.getKey(), label(offset + entry.getOffset()));
                }
                ib.switch_(tmp.getRegister1(), branches);
            }
            case SPARSE_SWITCH -> {
                var tmp = ((Instruction31t) insn);
                var payload = (SparseSwitchPayload) code_map.apply(offset + tmp.getBranchOffset());
                var branches = new TreeMap<Integer, Integer>();
                for (var entry : payload.getSwitchElements()) {
                    branches.put(entry.getKey(), label(offset + entry.getOffset()));
                }
                ib.switch_(tmp.getRegister1(), branches);
            }
            case FILL_ARRAY_DATA -> {
                var tmp = ((Instruction31t) insn);
                ib.f31t(FILL_ARRAY_DATA, tmp.getRegister1(), label(offset + tmp.getBranchOffset()));
            }
            case IF_EQ, IF_NE, IF_LT, IF_GE, IF_GT, IF_LE -> {
                var tmp = ((Instruction22t) insn);
                var test = Test.of(opcode);
                ib.if_test(test, tmp.getRegister1(), tmp.getRegister2(), label(offset + tmp.getBranchOffset()));
            }
            case IF_EQZ, IF_NEZ, IF_LTZ, IF_GEZ, IF_GTZ, IF_LEZ -> {
                var tmp = ((Instruction21t) insn);
                var test = Test.of(opcode);
                ib.if_testz(test, tmp.getRegister1(), label(offset + tmp.getBranchOffset()));
            }
            case ARRAY_PAYLOAD -> {
                ib.odd_spacer();
                ib.label(label(offset));
                ib.raw(insn);
            }
            case RAW -> {
                var tmp = ((InstructionRaw) insn);
                if (tmp.isAligned()) ib.odd_spacer();
                ib.label(label(offset));
                ib.raw(insn);
            }
            case NOP, PACKED_SWITCH_PAYLOAD, SPARSE_SWITCH_PAYLOAD -> {
                // nop
            }
            default -> ib.raw(insn);
        }
    }

    public static MethodImplementation process(MethodImplementation impl, StringIndexer strings,
                                               StringFix rewrite, boolean debug) {
        if (rewrite == NONE || (rewrite == FIX_JUMBO && strings.getStringCount() < 65535)) {
            return impl;
        }

        boolean needs_fix = false;
        for (var i : impl.getInstructions()) {
            var opcode = i.getOpcode();
            if (opcode == CONST_STRING) {
                var tmp = (Instruction21c) i;
                var str = (String) tmp.getReference1();
                if (!uwidth(strings.getStringIndex(str), 16)) {
                    needs_fix = true;
                    break;
                }
            } else if (rewrite == FIX_ALL && opcode == CONST_STRING_JUMBO) {
                var tmp = (Instruction31c) i;
                var str = (String) tmp.getReference1();
                if (uwidth(strings.getStringIndex(str), 16)) {
                    needs_fix = true;
                    break;
                }
            }
        }
        if (!needs_fix) return impl;

        return CodeBuilder.build(impl.getRegisterCount(), ib -> {
            int size = impl.getInstructions().size();
            var code_map = new IntMap<Instruction>(size);
            int offset = 0;
            for (var i : impl.getInstructions()) {
                code_map.append(offset, i);
                offset += i.getUnitCount();
            }

            for (int i = 0; i < size; i++) {
                int position = code_map.keyAt(i);
                var insn = code_map.valueAt(i);
                if (!(insn.getOpcode() == ARRAY_PAYLOAD || insn.getOpcode() == RAW)) {
                    ib.label(label(position));
                }
                copyInstruction(ib, strings, rewrite, position, insn, code_map::get);
            }
            ib.label(label(offset));

            copyTryBlocks(ib, impl.getTryBlocks());
            if (debug) {
                copyDebugItems(ib, impl.getDebugItems(), code_map.keysSet(), offset);
            }
        });
    }
}

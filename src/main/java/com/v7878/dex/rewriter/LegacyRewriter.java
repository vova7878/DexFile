package com.v7878.dex.rewriter;

import static com.v7878.dex.Opcode.EXECUTE_INLINE;
import static com.v7878.dex.Opcode.FILLED_NEW_ARRAY;
import static com.v7878.dex.Opcode.INVOKE_DIRECT;
import static com.v7878.dex.Opcode.INVOKE_DIRECT_EMPTY;
import static com.v7878.dex.Opcode.INVOKE_INTERFACE;
import static com.v7878.dex.Opcode.INVOKE_STATIC;
import static com.v7878.dex.Opcode.INVOKE_SUPER;
import static com.v7878.dex.Opcode.INVOKE_SUPER_QUICK;
import static com.v7878.dex.Opcode.INVOKE_VIRTUAL;
import static com.v7878.dex.Opcode.INVOKE_VIRTUAL_QUICK;
import static com.v7878.dex.builder.CodeBuilder.Test;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.collections.IntMap;
import com.v7878.collections.IntSet;
import com.v7878.dex.builder.CodeBuilder;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.Instruction11p;
import com.v7878.dex.immutable.bytecode.Instruction12x;
import com.v7878.dex.immutable.bytecode.Instruction21t;
import com.v7878.dex.immutable.bytecode.Instruction22c;
import com.v7878.dex.immutable.bytecode.Instruction22t;
import com.v7878.dex.immutable.bytecode.Instruction34c;
import com.v7878.dex.immutable.bytecode.Instruction35c;
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

/// Converts DEX009 and DEX013 instructions to a modern format
public class LegacyRewriter extends DexRewriter {
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

    private static void copyInstruction(CodeBuilder ib, int offset, Instruction insn,
                                        IntFunction<Instruction> code_map) {
        var opcode = insn.getOpcode();
        switch (opcode) {
            case M_FILLED_NEW_ARRAY, M_INVOKE_VIRTUAL, M_INVOKE_SUPER,
                 M_INVOKE_DIRECT, M_INVOKE_STATIC, M_INVOKE_INTERFACE,
                 M_EXECUTE_INLINE, M_INVOKE_DIRECT_EMPTY,
                 M_INVOKE_VIRTUAL_QUICK, M_INVOKE_SUPER_QUICK -> {
                var tmp = ((Instruction34c) insn);
                ib.raw(Instruction35c.of(
                        switch (opcode) {
                            case M_FILLED_NEW_ARRAY -> FILLED_NEW_ARRAY;
                            case M_INVOKE_VIRTUAL -> INVOKE_VIRTUAL;
                            case M_INVOKE_SUPER -> INVOKE_SUPER;
                            case M_INVOKE_DIRECT -> INVOKE_DIRECT;
                            case M_INVOKE_STATIC -> INVOKE_STATIC;
                            case M_INVOKE_INTERFACE -> INVOKE_INTERFACE;
                            case M_EXECUTE_INLINE -> EXECUTE_INLINE;
                            case M_INVOKE_DIRECT_EMPTY -> INVOKE_DIRECT_EMPTY;
                            case M_INVOKE_VIRTUAL_QUICK -> INVOKE_VIRTUAL_QUICK;
                            case M_INVOKE_SUPER_QUICK -> INVOKE_SUPER_QUICK;
                            default -> throw shouldNotReachHere();
                        },
                        tmp.getRegisterCount(),
                        tmp.getRegister1(),
                        tmp.getRegister2(),
                        tmp.getRegister3(),
                        tmp.getRegister4(),
                        0,
                        tmp.getReference1()
                ));
            }
            case M_CONST_SPECIAL -> {
                var tmp = ((Instruction11p) insn);
                var reg = tmp.getRegister1();
                var lit = tmp.getLiteral();
                ib.const_(reg, lit);
            }
            case M_CONST_WIDE_SPECIAL -> {
                var tmp = ((Instruction11p) insn);
                var reg = tmp.getRegister1();
                var lit = tmp.getWideLiteral();
                ib.const_wide(reg, lit);
            }
            case M_NEW_ARRAY -> {
                var tmp = ((Instruction22c) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                var ref = (TypeId) tmp.getReference1();
                ib.new_array(reg1, reg2, ref.array());
            }
            case M_NEW_ARRAY_BOOLEAN -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.Z.array());
            }
            case M_NEW_ARRAY_BYTE -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.B.array());
            }
            case M_NEW_ARRAY_CHAR -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.C.array());
            }
            case M_NEW_ARRAY_SHORT -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.S.array());
            }
            case M_NEW_ARRAY_INT -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.I.array());
            }
            case M_NEW_ARRAY_LONG -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.J.array());
            }
            case M_NEW_ARRAY_FLOAT -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.F.array());
            }
            case M_NEW_ARRAY_DOUBLE -> {
                var tmp = ((Instruction12x) insn);
                var reg1 = tmp.getRegister1();
                var reg2 = tmp.getRegister2();
                ib.new_array(reg1, reg2, TypeId.D.array());
            }
            case GOTO, M_GOTO_24 -> {
                var tmp = ((BranchOffsetInstruction) insn);
                ib.goto_(label(offset + tmp.getBranchOffset()));
            }
            case M_PACKED_SWITCH -> {
                var tmp = ((Instruction21t) insn);
                var payload = (PackedSwitchPayload) code_map
                        .apply(offset + tmp.getBranchOffset());
                var branches = new TreeMap<Integer, Integer>();
                for (var entry : payload.getSwitchElements()) {
                    branches.put(entry.getKey(),
                            label(offset + entry.getOffset()));
                }
                ib.switch_(tmp.getRegister1(), branches);
            }
            case M_SPARSE_SWITCH -> {
                var tmp = ((Instruction21t) insn);
                var payload = (SparseSwitchPayload) code_map
                        .apply(offset + tmp.getBranchOffset());
                var branches = new TreeMap<Integer, Integer>();
                for (var entry : payload.getSwitchElements()) {
                    branches.put(entry.getKey(),
                            label(offset + entry.getOffset()));
                }
                ib.switch_(tmp.getRegister1(), branches);
            }
            case IF_EQ, IF_NE, IF_LT, IF_GE, IF_GT, IF_LE -> {
                var tmp = ((Instruction22t) insn);
                var test = Test.of(opcode);
                ib.if_test(test, tmp.getRegister1(), tmp.getRegister2(),
                        label(offset + tmp.getBranchOffset()));
            }
            case IF_EQZ, IF_NEZ, IF_LTZ, IF_GEZ, IF_GTZ, IF_LEZ -> {
                var tmp = ((Instruction21t) insn);
                var test = Test.of(opcode);
                ib.if_testz(test, tmp.getRegister1(),
                        label(offset + tmp.getBranchOffset()));
            }
            case NOP, M_PACKED_SWITCH_PAYLOAD, M_SPARSE_SWITCH_PAYLOAD -> {
                // nop
            }
            default -> ib.raw(insn);
        }
    }

    @Override
    public MethodImplementation rewriteMethodImplementation(MethodImplementation impl) {
        return CodeBuilder.build(impl.getRegisterCount(), ib -> {
            int size = impl.getInstructions().size();
            var code_map = new IntMap<Instruction>(size);
            int offset = 0;
            for (var i : impl.getInstructions()) {
                code_map.append(offset, i);
                offset += i.getUnitCount();
            }

            for (int i = 0; i < size; i++) {
                int offset2 = code_map.keyAt(i);
                ib.label(label(offset2));
                copyInstruction(ib, offset2, code_map.valueAt(i), code_map::get);
            }
            ib.label(label(offset));

            copyTryBlocks(ib, impl.getTryBlocks());
            copyDebugItems(ib, impl.getDebugItems(), code_map.keysSet(), offset);
        });
    }
}

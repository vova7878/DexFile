package com.v7878.dex;

import static com.v7878.dex.DexConstants.DBG_ADVANCE_LINE;
import static com.v7878.dex.DexConstants.DBG_ADVANCE_PC;
import static com.v7878.dex.DexConstants.DBG_END_LOCAL;
import static com.v7878.dex.DexConstants.DBG_END_SEQUENCE;
import static com.v7878.dex.DexConstants.DBG_FIRST_SPECIAL;
import static com.v7878.dex.DexConstants.DBG_LAST_SPECIAL;
import static com.v7878.dex.DexConstants.DBG_LINE_BASE;
import static com.v7878.dex.DexConstants.DBG_LINE_RANGE;
import static com.v7878.dex.DexConstants.DBG_LINE_TOP;
import static com.v7878.dex.DexConstants.DBG_RESTART_LOCAL;
import static com.v7878.dex.DexConstants.DBG_SET_EPILOGUE_BEGIN;
import static com.v7878.dex.DexConstants.DBG_SET_FILE;
import static com.v7878.dex.DexConstants.DBG_SET_PROLOGUE_END;
import static com.v7878.dex.DexConstants.DBG_START_LOCAL;
import static com.v7878.dex.DexConstants.DBG_START_LOCAL_EXTENDED;
import static com.v7878.dex.DexConstants.NO_INDEX;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;

import java.util.Objects;

public abstract sealed class DebugItem implements Mutable {

    public static MutableList<DebugItem> readArray(
            RandomInput in, ReadContext context, int line_start) {
        MutableList<DebugItem> out = MutableList.empty();
        int[] address = {0, 0};
        Runnable emit_address = () -> {
            if (address[0] != address[1]) {
                out.add(new AdvancePC(address[0] - address[1]));
                address[1] = address[0];
            }
        };
        int[] line = {line_start, 0};
        Runnable emit_line = () -> {
            if (line[0] != line[1]) {
                out.add(new LineNumber(line[0]));
                line[1] = line[0];
            }
        };

        int opcode;
        do {
            opcode = in.readUnsignedByte();
        } while (switch (opcode) {
            case DBG_END_SEQUENCE -> false;
            case DBG_ADVANCE_PC -> {
                address[0] += in.readULeb128();
                yield true;
            }
            case DBG_ADVANCE_LINE -> {
                line[0] += in.readSLeb128();
                yield true;
            }
            case DBG_START_LOCAL, DBG_START_LOCAL_EXTENDED -> {
                int reg = in.readULeb128();
                int name_idx = in.readULeb128() - 1;
                String name = name_idx == NO_INDEX ?
                        null : context.string(name_idx);
                int type_idx = in.readULeb128() - 1;
                TypeId type = type_idx == NO_INDEX ?
                        null : context.type(type_idx);
                int signature_idx = opcode == DBG_START_LOCAL ?
                        NO_INDEX : in.readULeb128() - 1;
                String signature = signature_idx == NO_INDEX ?
                        null : context.string(signature_idx);
                emit_address.run();
                out.add(new StartLocal(reg, name, type, signature));
                yield true;
            }
            case DBG_END_LOCAL -> {
                int reg = in.readULeb128();
                emit_address.run();
                out.add(new EndLocal(reg));
                yield true;
            }
            case DBG_RESTART_LOCAL -> {
                int reg = in.readULeb128();
                emit_address.run();
                out.add(new RestartLocal(reg));
                yield true;
            }
            case DBG_SET_PROLOGUE_END -> {
                emit_address.run();
                out.add(SetPrologueEnd.INSTANCE);
                yield true;
            }
            case DBG_SET_EPILOGUE_BEGIN -> {
                emit_address.run();
                out.add(SetEpilogueBegin.INSTANCE);
                yield true;
            }
            case DBG_SET_FILE -> {
                int name_idx = in.readULeb128() - 1;
                String name = name_idx == NO_INDEX ?
                        null : context.string(name_idx);
                emit_address.run();
                out.add(new SetFile(name));
                yield true;
            }
            default -> {
                int adjopcode = opcode - DBG_FIRST_SPECIAL;
                address[0] += adjopcode / DBG_LINE_RANGE;
                line[0] += DBG_LINE_BASE + (adjopcode % DBG_LINE_RANGE);
                emit_address.run();
                emit_line.run();
                yield true;
            }
        });
        return out;
    }

    public static void writeArray(RandomOutput out, WriteContext context,
                                  MutableList<DebugItem> items, int[] first_line) {
        int[] address = {0, 0};
        Runnable emit_address = () -> {
            if (address[0] != address[1]) {
                out.writeByte(DBG_ADVANCE_PC);
                out.writeULeb128(address[0] - address[1]);
                address[1] = address[0];
            }
        };
        int[] line = {0, 0};
        Runnable emit_position = () -> {
            if (line[0] != line[1]) {
                if (line[1] == 0) first_line[0] = line[1] = line[0];

                int addr_diff = address[0] - address[1];
                int line_diff = line[0] - line[1];

                int adjusted_opcode = Math.max(Math.min(
                        line_diff, DBG_LINE_TOP), DBG_LINE_BASE);
                line_diff -= adjusted_opcode;
                adjusted_opcode -= DBG_LINE_BASE;

                int max_addr_diff = DBG_LAST_SPECIAL - DBG_FIRST_SPECIAL;
                max_addr_diff -= adjusted_opcode;
                max_addr_diff /= DBG_LINE_RANGE;
                max_addr_diff = Math.min(max_addr_diff, addr_diff);
                addr_diff -= max_addr_diff;

                adjusted_opcode += max_addr_diff * DBG_LINE_RANGE + DBG_FIRST_SPECIAL;

                if (addr_diff != 0) {
                    out.writeByte(DBG_ADVANCE_PC);
                    out.writeULeb128(addr_diff);
                }
                if (line_diff != 0) {
                    out.writeByte(DBG_ADVANCE_LINE);
                    out.writeSLeb128(line_diff);
                }
                out.writeByte(adjusted_opcode);

                address[1] = address[0];
                line[1] = line[0];
            }
        };
        for (var item : items) {
            if (item instanceof AdvancePC op) {
                address[0] += op.getAddrDiff();
            } else if (item instanceof StartLocal op) {
                var register = op.getRegister();
                var name = op.getName();
                var type = op.getType();
                var signature = op.getSignature();
                emit_address.run();
                out.writeByte(signature == null ? DBG_START_LOCAL : DBG_START_LOCAL_EXTENDED);
                out.writeULeb128(register);
                out.writeULeb128((name == null ? NO_INDEX : context.getStringIndex(name)) + 1);
                out.writeULeb128((type == null ? NO_INDEX : context.getTypeIndex(type)) + 1);
                if (signature != null) out.writeULeb128(context.getStringIndex(signature) + 1);
            } else if (item instanceof EndLocal op) {
                var register = op.getRegister();
                emit_address.run();
                out.writeByte(DBG_END_LOCAL);
                out.writeULeb128(register);
            } else if (item instanceof RestartLocal op) {
                var register = op.getRegister();
                emit_address.run();
                out.writeByte(DBG_RESTART_LOCAL);
                out.writeULeb128(register);
            } else if (item instanceof SetPrologueEnd) {
                emit_address.run();
                out.writeByte(DBG_SET_PROLOGUE_END);
            } else if (item instanceof SetEpilogueBegin) {
                emit_address.run();
                out.writeByte(DBG_SET_EPILOGUE_BEGIN);
            } else if (item instanceof SetFile op) {
                var name = op.getName();
                emit_address.run();
                out.writeULeb128((name == null ? NO_INDEX : context.getStringIndex(name)) + 1);
            } else if (item instanceof LineNumber op) {
                line[0] = op.getLine();
                emit_position.run();
            }
        }
        out.writeByte(DBG_END_SEQUENCE);
    }

    public void collectData(DataCollector data) {
    }

    @Override
    public abstract DebugItem mutate();

    private static void checkRegister(int register) {
        if (register < 0) {
            throw new IllegalArgumentException(
                    "register can`t be negative: " + register);
        }
    }

    public static final class AdvancePC extends DebugItem {

        private int addr_diff;

        public AdvancePC(int addr_diff) {
            setAddrDiff(addr_diff);
        }

        public int getAddrDiff() {
            return addr_diff;
        }

        public void setAddrDiff(int addr_diff) {
            if (addr_diff <= 0) {
                throw new IllegalArgumentException(
                        "addr_diff should be greater than zero: " + addr_diff);
            }
            this.addr_diff = addr_diff;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof AdvancePC other
                    && addr_diff == other.addr_diff;
        }

        @Override
        public int hashCode() {
            return addr_diff;
        }

        @Override
        public AdvancePC mutate() {
            return new AdvancePC(addr_diff);
        }
    }

    public static final class StartLocal extends DebugItem {

        private int register;
        private String name;
        private TypeId type;
        private String signature;

        public StartLocal(int register, String name, TypeId type, String signature) {
            setRegister(register);
            setName(name);
            setType(type);
            setSignature(signature);
        }

        public void setRegister(int register) {
            checkRegister(register);
            this.register = register;
        }

        public int getRegister() {
            return register;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setType(TypeId type) {
            this.type = type;
        }

        public TypeId getType() {
            return type;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getSignature() {
            return signature;
        }

        @Override
        public void collectData(DataCollector data) {
            if (name != null) data.add(name);
            if (type != null) data.add(type);
            if (signature != null) data.add(signature);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof StartLocal other
                    && register == other.register
                    && Objects.equals(name, other.name)
                    && Objects.equals(type, other.type)
                    && Objects.equals(signature, other.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(register, name, type, signature);
        }

        @Override
        public StartLocal mutate() {
            return new StartLocal(register, name, type, signature);
        }
    }

    public static final class EndLocal extends DebugItem {

        private int register;

        public EndLocal(int register) {
            setRegister(register);
        }

        public void setRegister(int register) {
            checkRegister(register);
            this.register = register;
        }

        public int getRegister() {
            return register;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof EndLocal other
                    && register == other.register;
        }

        @Override
        public int hashCode() {
            return register;
        }

        @Override
        public EndLocal mutate() {
            return new EndLocal(register);
        }
    }

    public static final class RestartLocal extends DebugItem {

        private int register;

        public RestartLocal(int register) {
            setRegister(register);
        }

        public void setRegister(int register) {
            DebugItem.checkRegister(register);
            this.register = register;
        }

        public int getRegister() {
            return register;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof RestartLocal other
                    && register == other.register;
        }

        @Override
        public int hashCode() {
            return register;
        }

        @Override
        public RestartLocal mutate() {
            return new RestartLocal(register);
        }
    }

    public static final class SetPrologueEnd extends DebugItem {

        public static final SetPrologueEnd INSTANCE = new SetPrologueEnd();

        private SetPrologueEnd() {
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof SetPrologueEnd;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public SetPrologueEnd mutate() {
            return INSTANCE;
        }
    }

    public static final class SetEpilogueBegin extends DebugItem {

        public static final SetEpilogueBegin INSTANCE = new SetEpilogueBegin();

        private SetEpilogueBegin() {
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof SetEpilogueBegin;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public SetEpilogueBegin mutate() {
            return INSTANCE;
        }
    }


    public static final class SetFile extends DebugItem {

        private String name;

        public SetFile(String name) {
            setName(name);
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public void collectData(DataCollector data) {
            if (name != null) data.add(name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof SetFile other
                    && Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }

        @Override
        public SetFile mutate() {
            return new SetFile(name);
        }
    }

    public static final class LineNumber extends DebugItem {

        private int line;

        public LineNumber(int line) {
            setLine(line);
        }

        public void setLine(int line) {
            if (line <= 0) {
                throw new IllegalArgumentException(
                        "line should be greater than zero: " + line);
            }
            this.line = line;
        }

        public int getLine() {
            return line;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof LineNumber other
                    && line == other.line;
        }

        @Override
        public int hashCode() {
            return line;
        }

        @Override
        public LineNumber mutate() {
            return new LineNumber(line);
        }
    }
}

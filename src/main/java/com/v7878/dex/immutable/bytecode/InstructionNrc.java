package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format3rc;
import static com.v7878.dex.Format.Format5rc;
import static com.v7878.dex.Opcode.EXECUTE_INLINE_RANGE;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.iface.RegisterRangeInstruction;
import com.v7878.dex.immutable.bytecode.iface.SingleReferenceInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionNrc extends Instruction
        implements RegisterRangeInstruction, SingleReferenceInstruction {
    private final int register_count;
    private final int start_register;
    private final Object reference1;

    private InstructionNrc(Opcode opcode, int register_count,
                           int start_register, Object reference1) {
        super(Preconditions.checkFormat(opcode, "Nrc", Format3rc, Format5rc));
        this.register_count = switch (opcode.format()) {
            case Format3rc -> opcode == EXECUTE_INLINE_RANGE ?
                    Preconditions.check34cOrExecuteInlineRegisterCount(register_count) :
                    Preconditions.checkByteRegisterRangeCount(register_count);
            case Format5rc -> Preconditions.checkShortRegisterRangeCount(register_count);
            default -> throw shouldNotReachHere();
        };
        // TODO: single range check
        this.start_register = Preconditions.checkShortRegister(start_register);
        Preconditions.checkShortRegister(start_register + register_count);
        this.reference1 = ReferenceType.validate(getReferenceType1(), reference1);
    }

    public static InstructionNrc of(Opcode opcode, int register_count,
                                    int start_register, Object reference1) {
        return new InstructionNrc(opcode,
                register_count, start_register, reference1);
    }

    @Override
    public int getRegisterCount() {
        return register_count;
    }

    @Override
    public int getStartRegister() {
        return start_register;
    }

    @Override
    public Object getReference1() {
        return reference1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOpcode(), getRegisterCount(), getStartRegister(), getReference1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionNrc other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getRegisterCount() == other.getRegisterCount()
                && getStartRegister() == other.getStartRegister()
                && Objects.equals(getReference1(), other.getReference1());
    }

    @Override
    public String toString() {
        var start = start_register;
        var end = start + register_count;
        return getName() + " {" + Formatter.register(start) + " .. " + Formatter.register(end) + "}"
                + ", " + ReferenceType.describe(getReferenceType1(), reference1);
    }
}

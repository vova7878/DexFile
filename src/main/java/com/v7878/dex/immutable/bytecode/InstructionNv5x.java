package com.v7878.dex.immutable.bytecode;

import static com.v7878.dex.Format.Format25x;

import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.iface.LambdaInstruction;
import com.v7878.dex.immutable.bytecode.iface.VariableFourRegisterInstruction;
import com.v7878.dex.util.Formatter;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class InstructionNv5x extends Instruction implements
        LambdaInstruction, VariableFourRegisterInstruction {
    private final int closure_register;
    private final int argument_count;
    private final int argument1;
    private final int argument2;
    private final int argument3;
    private final int argument4;

    private InstructionNv5x(Opcode opcode, int closure_register, int argument_count,
                            int argument1, int argument2, int argument3, int argument4) {
        super(Preconditions.checkFormat(opcode, "Nv5x", Format25x));
        this.closure_register = Preconditions.checkNibbleRegister(closure_register);
        this.argument_count = Preconditions.check25xRegisterCount(argument_count);
        this.argument1 = (argument_count > 0) ? Preconditions.checkNibbleRegister(argument1) : 0;
        this.argument2 = (argument_count > 1) ? Preconditions.checkNibbleRegister(argument2) : 0;
        this.argument3 = (argument_count > 2) ? Preconditions.checkNibbleRegister(argument3) : 0;
        this.argument4 = (argument_count > 3) ? Preconditions.checkNibbleRegister(argument4) : 0;
    }

    public static InstructionNv5x of(Opcode opcode, int closure_register, int argument_count,
                                     int argument1, int argument2, int argument3, int argument4) {
        return new InstructionNv5x(opcode, closure_register, argument_count,
                argument1, argument2, argument3, argument4);
    }

    @Override
    public int getClosureRegister() {
        return closure_register;
    }

    @Override
    public int getRegisterCount() {
        return argument_count;
    }

    @Override
    public int getRegister1() {
        return argument1;
    }

    @Override
    public int getRegister2() {
        return argument2;
    }

    @Override
    public int getRegister3() {
        return argument3;
    }

    @Override
    public int getRegister4() {
        return argument4;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = 31 * h + Objects.hashCode(getOpcode());
        h = 31 * h + Integer.hashCode(getClosureRegister());
        h = 31 * h + Integer.hashCode(getRegisterCount());
        h = 31 * h + Integer.hashCode(getRegister1());
        h = 31 * h + Integer.hashCode(getRegister2());
        h = 31 * h + Integer.hashCode(getRegister3());
        h = 31 * h + Integer.hashCode(getRegister4());
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof InstructionNv5x other
                && Objects.equals(getOpcode(), other.getOpcode())
                && getClosureRegister() == other.getClosureRegister()
                && getRegisterCount() == other.getRegisterCount()
                && getRegister1() == other.getRegister1()
                && getRegister2() == other.getRegister2()
                && getRegister3() == other.getRegister3()
                && getRegister4() == other.getRegister4();
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(getName());
        builder.append(' ');
        builder.append(Formatter.register(closure_register));
        builder.append(", {");
        if (argument_count > 0) builder.append(Formatter.register(argument1));
        if (argument_count > 1) builder.append(", ").append(Formatter.register(argument2));
        if (argument_count > 2) builder.append(", ").append(Formatter.register(argument3));
        if (argument_count > 3) builder.append(", ").append(Formatter.register(argument4));
        builder.append("}");
        return builder.toString();
    }
}

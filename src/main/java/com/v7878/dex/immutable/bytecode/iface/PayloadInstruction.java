package com.v7878.dex.immutable.bytecode.iface;

public sealed interface PayloadInstruction extends InstructionI
        permits ArrayPayloadInstruction, SwitchPayloadInstruction {
    @Override
    int getUnitCount();
}

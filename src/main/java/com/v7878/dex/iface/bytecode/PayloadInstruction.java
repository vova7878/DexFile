package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.ArrayPayload;

public sealed interface PayloadInstruction extends Instruction
        permits SwitchPayloadInstruction, ArrayPayload {
}

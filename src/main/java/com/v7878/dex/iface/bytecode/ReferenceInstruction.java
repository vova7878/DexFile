package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction21c;
import com.v7878.dex.iface.bytecode.formats.Instruction22c22cs;
import com.v7878.dex.iface.bytecode.formats.Instruction31c;
import com.v7878.dex.iface.bytecode.formats.Instruction35c35mi35ms;
import com.v7878.dex.iface.bytecode.formats.Instruction3rc3rmi3rms;

public sealed interface ReferenceInstruction extends Instruction permits
        DualReferenceInstruction, Instruction21c, Instruction22c22cs,
        Instruction31c, Instruction35c35mi35ms, Instruction3rc3rmi3rms {
    Object getReference1();
}

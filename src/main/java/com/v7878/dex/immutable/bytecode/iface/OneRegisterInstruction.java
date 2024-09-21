package com.v7878.dex.immutable.bytecode.iface;

import com.v7878.dex.immutable.bytecode.Instruction11n;
import com.v7878.dex.immutable.bytecode.Instruction11x;
import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction21ih;
import com.v7878.dex.immutable.bytecode.Instruction21lh;
import com.v7878.dex.immutable.bytecode.Instruction21s;
import com.v7878.dex.immutable.bytecode.Instruction21t;
import com.v7878.dex.immutable.bytecode.Instruction31c;
import com.v7878.dex.immutable.bytecode.Instruction31i;
import com.v7878.dex.immutable.bytecode.Instruction31t;
import com.v7878.dex.immutable.bytecode.Instruction51l;

public sealed interface OneRegisterInstruction extends InstructionI
        permits Instruction11n, Instruction11x, Instruction21c, Instruction21ih,
        Instruction21lh, Instruction21s, Instruction21t, Instruction31c, Instruction31i,
        Instruction31t, Instruction51l, TwoRegisterInstruction {
    int getRegister1();
}

package com.v7878.dex.iface.bytecode;

import com.v7878.dex.iface.bytecode.formats.Instruction11n;
import com.v7878.dex.iface.bytecode.formats.Instruction11x;
import com.v7878.dex.iface.bytecode.formats.Instruction21c;
import com.v7878.dex.iface.bytecode.formats.Instruction21ih;
import com.v7878.dex.iface.bytecode.formats.Instruction21lh;
import com.v7878.dex.iface.bytecode.formats.Instruction21s;
import com.v7878.dex.iface.bytecode.formats.Instruction21t;
import com.v7878.dex.iface.bytecode.formats.Instruction31c;
import com.v7878.dex.iface.bytecode.formats.Instruction31i;
import com.v7878.dex.iface.bytecode.formats.Instruction31t;
import com.v7878.dex.iface.bytecode.formats.Instruction51l;

public sealed interface OneRegisterInstruction extends Instruction permits
        TwoRegisterInstruction, Instruction11n, Instruction11x, Instruction21c,
        Instruction21ih, Instruction21lh, Instruction21s, Instruction21t,
        Instruction31c, Instruction31i, Instruction31t, Instruction51l {
    int getRegister1();
}

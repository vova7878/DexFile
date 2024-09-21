package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.Format;
import com.v7878.dex.Opcode;
import com.v7878.dex.iface.bytecode.Instruction;
import com.v7878.dex.iface.bytecode.formats.ArrayPayload;
import com.v7878.dex.iface.bytecode.formats.Instruction10t;
import com.v7878.dex.iface.bytecode.formats.Instruction10x;
import com.v7878.dex.iface.bytecode.formats.Instruction11n;
import com.v7878.dex.iface.bytecode.formats.Instruction11x;
import com.v7878.dex.iface.bytecode.formats.Instruction12x;
import com.v7878.dex.iface.bytecode.formats.Instruction20t;
import com.v7878.dex.iface.bytecode.formats.Instruction21c;
import com.v7878.dex.iface.bytecode.formats.Instruction21ih;
import com.v7878.dex.iface.bytecode.formats.Instruction21lh;
import com.v7878.dex.iface.bytecode.formats.Instruction21s;
import com.v7878.dex.iface.bytecode.formats.Instruction21t;
import com.v7878.dex.iface.bytecode.formats.Instruction22b;
import com.v7878.dex.iface.bytecode.formats.Instruction22c22cs;
import com.v7878.dex.iface.bytecode.formats.Instruction22s;
import com.v7878.dex.iface.bytecode.formats.Instruction22t;
import com.v7878.dex.iface.bytecode.formats.Instruction22x;
import com.v7878.dex.iface.bytecode.formats.Instruction23x;
import com.v7878.dex.iface.bytecode.formats.Instruction30t;
import com.v7878.dex.iface.bytecode.formats.Instruction31c;
import com.v7878.dex.iface.bytecode.formats.Instruction31i;
import com.v7878.dex.iface.bytecode.formats.Instruction31t;
import com.v7878.dex.iface.bytecode.formats.Instruction32x;
import com.v7878.dex.iface.bytecode.formats.Instruction35c35mi35ms;
import com.v7878.dex.iface.bytecode.formats.Instruction3rc3rmi3rms;
import com.v7878.dex.iface.bytecode.formats.Instruction45cc;
import com.v7878.dex.iface.bytecode.formats.Instruction4rcc;
import com.v7878.dex.iface.bytecode.formats.Instruction51l;
import com.v7878.dex.iface.bytecode.formats.PackedSwitchPayload;
import com.v7878.dex.iface.bytecode.formats.SparseSwitchPayload;

public class ImmutableInstruction implements Instruction {
    private final Opcode opcode;

    public ImmutableInstruction(Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public final Opcode getOpcode() {
        return opcode;
    }

    static ImmutableInstruction of(Instruction other) {
        return of(other.getOpcode().format(), other);
    }

    static ImmutableInstruction of(Format format, Instruction other) {
        if (other instanceof ImmutableInstruction immutable) return immutable;
        return switch (other.getOpcode().format()) {
            case Format10t -> ImmutableInstruction10t.of((Instruction10t) other);
            case Format10x -> ImmutableInstruction10x.of((Instruction10x) other);
            case Format11n -> ImmutableInstruction11n.of((Instruction11n) other);
            case Format11x -> ImmutableInstruction11x.of((Instruction11x) other);
            case Format12x -> ImmutableInstruction12x.of((Instruction12x) other);
            case Format20bc ->
                //TODO: ImmutableInstruction20bc.of((Instruction20bc) other);
                    throw new UnsupportedOperationException("TODO");
            case Format20t -> ImmutableInstruction20t.of((Instruction20t) other);
            case Format21c -> ImmutableInstruction21c.of((Instruction21c) other);
            case Format21ih -> ImmutableInstruction21ih.of((Instruction21ih) other);
            case Format21lh -> ImmutableInstruction21lh.of((Instruction21lh) other);
            case Format21s -> ImmutableInstruction21s.of((Instruction21s) other);
            case Format21t -> ImmutableInstruction21t.of((Instruction21t) other);
            case Format22b -> ImmutableInstruction22b.of((Instruction22b) other);
            case Format22c22cs -> ImmutableInstruction22c22cs.of((Instruction22c22cs) other);
            case Format22s -> ImmutableInstruction22s.of((Instruction22s) other);
            case Format22t -> ImmutableInstruction22t.of((Instruction22t) other);
            case Format22x -> ImmutableInstruction22x.of((Instruction22x) other);
            case Format23x -> ImmutableInstruction23x.of((Instruction23x) other);
            case Format30t -> ImmutableInstruction30t.of((Instruction30t) other);
            case Format31c -> ImmutableInstruction31c.of((Instruction31c) other);
            case Format31i -> ImmutableInstruction31i.of((Instruction31i) other);
            case Format31t -> ImmutableInstruction31t.of((Instruction31t) other);
            case Format32x -> ImmutableInstruction32x.of((Instruction32x) other);
            case Format35c35mi35ms ->
                    ImmutableInstruction35c35mi35ms.of((Instruction35c35mi35ms) other);
            case Format3rc3rmi3rms ->
                    ImmutableInstruction3rc3rmi3rms.of((Instruction3rc3rmi3rms) other);
            case Format45cc -> ImmutableInstruction45cc.of((Instruction45cc) other);
            case Format4rcc -> ImmutableInstruction4rcc.of((Instruction4rcc) other);
            case Format51l -> ImmutableInstruction51l.of((Instruction51l) other);
            case PackedSwitchPayload ->
                    ImmutablePackedSwitchPayload.of((PackedSwitchPayload) other);
            case SparseSwitchPayload ->
                    ImmutableSparseSwitchPayload.of((SparseSwitchPayload) other);
            case ArrayPayload -> ImmutableArrayPayload.of((ArrayPayload) other);
        };
    }
}

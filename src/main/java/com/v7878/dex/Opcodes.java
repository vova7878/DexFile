package com.v7878.dex;

import com.v7878.dex.util.SparseArray;

import java.util.EnumMap;

public final class Opcodes {
    private final SparseArray<Opcode> opcodesByValue;
    private final EnumMap<Opcode, Integer> opcodeValues;

    private Opcodes(DexVersion dex, int api, boolean art, boolean odex) {
        var opcodes = Opcode.values();
        opcodeValues = new EnumMap<>(Opcode.class);
        opcodesByValue = new SparseArray<>(opcodes.length);

        for (var opcode : opcodes) {
            Integer value = opcode.getValue(dex, api, art, odex);
            if (value != null) {
                opcodeValues.put(opcode, value);
                opcodesByValue.put(value, opcode);
            }
        }
    }

    public static Opcodes of(DexVersion dexVersion, int targetApi,
                             boolean targetForArt, boolean allowOdexInstructions) {
        return new Opcodes(dexVersion, targetApi, targetForArt, allowOdexInstructions);
    }

    public Opcode getOpcodeByValue(int opcodeValue) {
        return opcodesByValue.get(opcodeValue);
    }

    public Integer getOpcodeValue(Opcode opcode) {
        return opcodeValues.get(opcode);
    }
}

package com.v7878.dex;

import com.v7878.collections.IntMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Objects;

public final class Opcodes {
    private final IntMap<Opcode> opcodesByValue;
    private final EnumMap<Opcode, Integer> opcodeValues;
    private final DexVersion dex;
    private final DexOptions<?> options;

    private Opcodes(DexVersion dex, DexOptions<?> options) {
        this.dex = Objects.requireNonNull(dex);
        this.options = Objects.requireNonNull(options);

        var opcodes = Opcode.values();
        opcodeValues = new EnumMap<>(Opcode.class);
        opcodesByValue = new IntMap<>(opcodes.length);

        for (var opcode : opcodes) {
            Integer value = opcode.getValue(dex, options);
            if (value != null) {
                opcodeValues.put(opcode, value);
                opcodesByValue.put(value, opcode);
            }
        }
    }

    public static Opcodes of(DexVersion dex, DexOptions<?> options) {
        return new Opcodes(dex, options);
    }

    private static String print(DexOptions<?> options) {
        return "{" +
                "api=" + options.api +
                ", art=" + options.art +
                ", odex=" + options.odex +
                ", expanded=" + options.expanded +
                '}';
    }

    public Opcode getOpcodeByValue(int value) {
        var out = opcodesByValue.get(value);
        if (out == null) {
            var candidates = new ArrayList<Opcode>();
            for (var op : Opcode.values()) {
                if (op.getConstraint().contains(value)) {
                    candidates.add(op);
                }
            }
            throw new IllegalArgumentException(String.format(
                    "No opcode found with value %d for dex=%s, options=%b. %s",
                    value, dex, print(options),
                    candidates.stream()
                            .map(op -> op + " (" + op.getConstraint() + ")")
                            .reduce((a, b) -> a + ", " + b)
                            .map(msg -> String.format("Candidates: {%s}", msg))
                            .orElse("No candidates found")
            ));
        }
        return out;
    }

    public int getOpcodeValue(Opcode opcode) {
        Objects.requireNonNull(opcode);
        var out = opcodeValues.get(opcode);
        if (out == null) {
            var constraint = opcode.getConstraint();
            throw new IllegalArgumentException(String.format(
                    "Constraints for opcode %s (%s) do not allow " +
                            "obtaining int value with dex=%s, options=%b",
                    opcode, constraint, dex, print(options)));
        }
        return out;
    }
}

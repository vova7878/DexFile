package com.v7878.dex.util;

import com.v7878.dex.Format;
import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.bytecode.SwitchElement;

import java.util.List;

public class Preconditions {
    public static int checkInnerClassAccessFlags(int access_flags) {
        // TODO: check
        return access_flags;
    }

    public static int checkClassAccessFlags(int access_flags) {
        // TODO: check
        return access_flags;
    }

    public static int checkMethodAccessFlags(int access_flags) {
        // TODO: check
        return access_flags;
    }

    public static int checkParamaterAccessFlags(int access_flags) {
        // TODO: check
        return access_flags;
    }

    public static int checkFieldAccessFlags(int access_flags) {
        // TODO: check
        return access_flags;
    }

    public static int checkHiddenApiFlags(int hiddenapi_flags) {
        // TODO: check
        return hiddenapi_flags;
    }

    public static int checkMethodRegisterCount(int registerCount) {
        if ((registerCount & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid register count: %d. Must be between 0 and 65535, inclusive", registerCount));
        }
        return registerCount;
    }

    public static Opcode checkFormat(Opcode opcode, Format expectedFormat) {
        if (opcode.format() != expectedFormat) {
            throw new IllegalArgumentException(
                    String.format("Invalid opcode %s for %s", opcode, expectedFormat));
        }
        return opcode;
    }

    public static int checkDebugAddrDiff(int addr_diff) {
        if (addr_diff < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid addr_diff: %d. Must be greater than zero", addr_diff));
        }
        return addr_diff;
    }

    public static int checkDebugLine(int line) {
        if (line < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid line: %d. Must be greater than zero", line));
        }
        return line;
    }

    public static int checkCodeAddress(int address) {
        if (address < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid code address: %d. Must be greater than or equal to zero", address));
        }
        return address;
    }

    public static int checkUnitCount(int units) {
        if ((units & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid unit count: %d. Must be between 0 and 65535, inclusive", units));
        }
        return units;
    }

    public static int checkNibbleRegister(int register) {
        if ((register & 0xFFFFFFF0) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid register: v%d. Must be between v0 and v15, inclusive", register));
        }
        return register;
    }

    public static int checkByteRegister(int register) {
        if ((register & 0xFFFFFF00) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid register: v%d. Must be between v0 and v255, inclusive", register));
        }
        return register;
    }

    public static int checkShortRegister(int register) {
        if ((register & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid register: v%d. Must be between v0 and v65535, inclusive", register));
        }
        return register;
    }

    public static int checkNibbleLiteral(int literal) {
        if (literal < -8 || literal > 7) {
            throw new IllegalArgumentException(
                    String.format("Invalid literal value: %d. Must be between -8 and 7, inclusive", literal));
        }
        return literal;
    }

    public static int checkByteLiteral(int literal) {
        if (literal < -128 || literal > 127) {
            throw new IllegalArgumentException(
                    String.format("Invalid literal value: %d. Must be between -128 and 127, inclusive", literal));
        }
        return literal;
    }

    public static int checkShortLiteral(int literal) {
        if (literal < -32768 || literal > 32767) {
            throw new IllegalArgumentException(
                    String.format("Invalid literal value: %d. Must be between -32768 and 32767, inclusive", literal));
        }
        return literal;
    }

    public static int checkIntegerHatLiteral(int literal) {
        if ((literal & 0xFFFF) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid literal value: %d. Low 16 bits must be zeroed out", literal));
        }
        return literal;
    }

    public static long checkLongHatLiteral(long literal) {
        if ((literal & 0xFFFFFFFFFFFFL) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid literal value: %d. Low 48 bits must be zeroed out", literal));
        }
        return literal;
    }

    public static int checkByteCodeOffset(int offset) {
        if (offset < -128 || offset > 127) {
            throw new IllegalArgumentException(
                    String.format("Invalid code offset: %d. Must be between -128 and 127, inclusive", offset));
        }
        return offset;
    }

    public static int checkShortCodeOffset(int offset) {
        if (offset < -32768 || offset > 32767) {
            throw new IllegalArgumentException(
                    String.format("Invalid code offset: %d. Must be between -32768 and 32767, inclusive", offset));
        }
        return offset;
    }

    public static int check35cAnd45ccRegisterCount(int registerCount) {
        if (registerCount < 0 || registerCount > 5) {
            throw new IllegalArgumentException(
                    String.format("Invalid register count: %d. Must be between 0 and 5, inclusive", registerCount));
        }
        return registerCount;
    }

    public static int checkRegisterRangeCount(int registerCount) {
        if ((registerCount & 0xFFFFFF00) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid register count: %d. Must be between 0 and 255, inclusive", registerCount));
        }
        return registerCount;
    }

    public static int checkRawIndex(int index) {
        if ((index & 0xFFFF0000) != 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid raw index: %d. Must be between 0 and 65535, inclusive", index));
        }
        return index;
    }

    public static List<SwitchElement> checkSequentialOrderedKeys(List<SwitchElement> elements) {
        Integer previousKey = null;
        for (SwitchElement element : elements) {
            int key = element.getKey();
            if (previousKey != null && previousKey + 1 != key) {
                throw new IllegalArgumentException("SwitchElement set is not sequential and ordered");
            }
            previousKey = key;
        }
        return elements;
    }

    public static int checkArrayPayloadElementWidth(int elementWidth) {
        return switch (elementWidth) {
            case 1, 2, 4, 8 -> elementWidth;
            default -> throw new IllegalArgumentException(
                    String.format("Not a valid element width: %d", elementWidth));
        };
    }

    public static <L extends List<? extends Number>> L checkArrayPayloadElements(int elementWidth, L elements) {
        // TODO: check by type
        return elements;
    }
}

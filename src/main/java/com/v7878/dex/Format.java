package com.v7878.dex;

import com.v7878.dex.ReferenceType.ReferenceStorage;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.raw.InstructionReader;

public enum Format {
    Format10t(1) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_10t(opcode, arg);
        }
    }, Format10x(1) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_10x(opcode, arg);
        }
    }, Format11n(1) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_11n(opcode, arg);
        }
    }, Format11x(1) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_11x(opcode, arg);
        }
    }, Format12x(1) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_12x(opcode, arg);
        }
    }, Format20bc(2, true, false) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            //TODO
            throw new UnsupportedOperationException("Unimplemented yet!");
        }
    }, Format20t(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_20t(opcode, in, arg);
        }
    }, Format21c(2, true, false) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_21c(opcode, in, indexer, arg);
        }
    }, Format21ih(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_21ih(opcode, in, arg);
        }
    }, Format21lh(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_21lh(opcode, in, arg);
        }
    }, Format21s(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_21s(opcode, in, arg);
        }
    }, Format21t(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_21t(opcode, in, arg);
        }
    }, Format22b(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_22b(opcode, in, arg);
        }
    }, Format22c22cs(2, true, false) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_22c2cs(opcode, in, indexer, arg);
        }
    }, Format22s(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_22s(opcode, in, arg);
        }
    }, Format22t(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_22t(opcode, in, arg);
        }
    }, Format22x(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_22x(opcode, in, arg);
        }
    }, Format23x(2) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_23x(opcode, in, arg);
        }
    }, Format30t(3) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_30t(opcode, in, arg);
        }
    }, Format31c(3, true, false) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_31c(opcode, in, indexer, arg);
        }
    }, Format31i(3) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_31i(opcode, in, arg);
        }
    }, Format31t(3) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_31t(opcode, in, arg);
        }
    }, Format32x(3) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_32x(opcode, in, arg);
        }
    }, Format35c35mi35ms(3, true, false) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_35c_35ms_35mi(opcode, in, indexer, arg);
        }
    }, Format3rc3rmi3rms(3, true, false) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_3rc_3rms_3rmi(opcode, in, indexer, arg);
        }
    }, Format45cc(4, true, true) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_45cc(opcode, in, indexer, arg);
        }
    }, Format4rcc(4, true, true) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_4rcc(opcode, in, indexer, arg);
        }
    }, Format51l(5) {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_51l(opcode, in, arg);
        }
    }, ArrayPayload() {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_array_payload(opcode, in, arg);
        }
    }, PackedSwitchPayload() {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_packed_switch_payload(opcode, in, arg);
        }
    }, SparseSwitchPayload() {
        @Override
        public Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer) {
            return InstructionReader.read_sparse_switch_payload(opcode, in, arg);
        }
    };

    private final int units;
    private final boolean payload;
    private final boolean reference1;
    private final boolean reference2;

    Format(int units, boolean payload, boolean reference1, boolean reference2) {
        assert (units == -1) == payload;
        assert !reference2 || reference1;
        assert !payload || !reference1;
        this.units = units;
        this.payload = payload;
        this.reference1 = reference1;
        this.reference2 = reference2;
    }

    Format(int units, boolean reference1, boolean reference2) {
        this(units, false, reference1, reference2);
    }

    Format(int units) {
        this(units, false, false, false);
    }

    Format() {
        this(-1, true, false, false);
    }

    public int getUnitCount() {
        return units;
    }

    public boolean isPayload() {
        return payload;
    }

    public boolean hasReferenceType1() {
        return reference1;
    }

    public boolean hasReferenceType2() {
        return reference2;
    }

    public abstract Instruction read(Opcode opcode, RandomInput in, int arg, ReferenceStorage indexer);
}

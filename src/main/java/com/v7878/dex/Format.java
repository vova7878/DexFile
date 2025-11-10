package com.v7878.dex;

public enum Format {
    Format10t(1),
    Format10x(1),
    Format11n(1),
    Format11x(1),
    Format12x(1),
    Format20bc(2, true, false),
    Format20t(2),
    Format21c(2, true, false),
    Format21ih(2),
    Format21lh(2),
    Format21s(2),
    Format21t(2),
    Format22b(2),
    Format22c22cs(2, true, false),
    Format22s(2),
    Format22t(2),
    Format22x(2),
    Format23x(2),
    Format30t(3),
    Format31c(3, true, false),
    Format31i(3),
    Format31t(3),
    Format32x(3),
    Format35c35mi35ms(3, true, false),
    Format3rc3rmi3rms(3, true, false),
    Format45cc(4, true, true),
    Format4rcc(4, true, true),
    Format51l(5),
    ArrayPayload(),
    PackedSwitchPayload(),
    SparseSwitchPayload(),
    // special format
    FormatRaw(1);

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
}

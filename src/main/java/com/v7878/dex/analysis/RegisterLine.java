package com.v7878.dex.analysis;

import com.v7878.dex.analysis.Register.Identifier;
import com.v7878.dex.analysis.Register.Undefined;
import com.v7878.dex.analysis.Register.WidePiece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RegisterLine {
    private final Register[] registers;

    /* package */ RegisterLine(int count) {
        var array = new Register[count];
        Arrays.fill(array, Undefined.INSTANCE);
        this.registers = array;
    }

    public List<Register> registers() {
        return Collections.unmodifiableList(Arrays.asList(registers));
    }

    public int registerCount() {
        return registers.length;
    }

    public Register at(int index) {
        return registers[index];
    }

    public record RegisterPair(Register lo, Register hi) {
        public RegisterPair {
            Objects.requireNonNull(lo);
            Objects.requireNonNull(hi);
        }

        public boolean isWidePair() {
            return lo().isWideLo() && hi().isWideHi();
        }

        public boolean isLongPair() {
            return lo().isLongLo() && hi().isLongHi();
        }

        public boolean isDoublePair() {
            return lo().isDoubleLo() && hi().isDoubleHi();
        }
    }

    public RegisterPair pairAt(int index) {
        return new RegisterPair(at(index), at(index + 1));
    }

    /* package */ void replace(int index, Register value) {
        registers[index] = value;
    }

    /* package */ void copy(int address, int index, Register value) {
        var tmp = registers[index];
        registers[index] = value;
        if (tmp.isWideLo()) {
            var next = index + 1;
            assert registers[next].isWideHi();
            registers[next] = WidePiece.of(new Identifier(address, next));
        } else if (tmp.isWideHi()) {
            var prev = index - 1;
            assert registers[prev].isWideLo();
            registers[prev] = WidePiece.of(new Identifier(address, prev));
        }
    }

    /* package */ void copyWide(int address, int index, RegisterPair pair) {
        copyWide(address, index, pair.lo(), pair.hi());
    }

    /* package */ void copyWide(int address, int index_lo, Register lo, Register hi) {
        assert lo.isWideLo();
        assert hi.isWideHi();
        int index_hi = index_lo + 1;
        var tmp_lo = registers[index_lo];
        var tmp_hi = registers[index_hi];
        if (tmp_lo.isWideHi()) {
            var prev = index_lo - 1;
            assert registers[prev].isWideLo();
            registers[prev] = WidePiece.of(new Identifier(address, prev));
        }
        if (tmp_hi.isWideLo()) {
            var next = index_hi + 1;
            assert registers[next].isWideHi();
            registers[next] = WidePiece.of(new Identifier(address, next));
        }
        assert tmp_lo.isWideLo() == tmp_hi.isWideHi();
        registers[index_lo] = lo;
        registers[index_hi] = hi;
    }

    private static int assertSame(int a, int b) {
        assert a == b;
        return a;
    }

    /* package */ void copy(RegisterLine line) {
        int length = assertSame(registers.length, line.registers.length);
        System.arraycopy(line.registers, 0, registers, 0, length);
    }

    /* package */ boolean merge(int address, RegisterLine line) {
        int length = assertSame(registers.length, line.registers.length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            var current = registers[i];
            var merged = Register.merge(address, i, current, line.registers[i]);
            changed = changed || !Objects.equals(current, merged);
        }
        return changed;
    }

    @Override
    public String toString() {
        return Arrays.toString(registers);
    }
}

package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;

public class StringId {

    public static final int SIZE = 0x04;

    public static final Comparator<String> COMPARATOR = String::compareTo;

    public static String read(RandomInput in) {
        int data_off = in.readInt();
        return in.duplicate(data_off).readMUTF8();
    }

    public static void write(String value, WriteContext context,
                             RandomOutput ids_out, RandomOutput data_out) {
        ids_out.writeInt((int) data_out.position());
        data_out.writeMUtf8(value);
    }
}

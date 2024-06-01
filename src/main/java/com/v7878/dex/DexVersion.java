package com.v7878.dex;

public enum DexVersion {
    V035(1, '0', '3', '5'),
    V037(24, '0', '3', '7'),
    V038(26, '0', '3', '8'),
    V039(28, '0', '3', '9'),
    V040(29, '0', '4', '0');
    //TODO: V041(35, '0', '4', '1'); // dex containers

    private final int minApi;
    private final int value;

    private static int getValue(int first, int second, int third) {
        return first << 16 | second << 8 | third;
    }

    DexVersion(int minApi, int first, int second, int third) {
        this.minApi = minApi;
        this.value = getValue(first, second, third);
    }

    public int getMinApi() {
        return minApi;
    }

    public byte firstByte() {
        return (byte) ((value >> 16) & 0xff);
    }

    public byte secondByte() {
        return (byte) ((value >> 8) & 0xff);
    }

    public byte thirdByte() {
        return (byte) (value & 0xff);
    }

    public static DexVersion fromApi(int api) {
        if (api < V037.minApi) {
            return V035;
        }
        if (api < V038.minApi) {
            return V037;
        }
        if (api < V039.minApi) {
            return V038;
        }
        if (api < V040.minApi) {
            return V039;
        }
        return V040;
    }

    public static DexVersion fromBytes(byte first, byte second, byte third) {
        int value = getValue(first, second, third);

        for (DexVersion version : values()) {
            if (value == version.value) {
                return version;
            }
        }

        throw new IllegalArgumentException("unknown dex version: \""
                + (char) (first & 0xff) + (char) (second & 0xff) + (char) (third & 0xff) + "\"");
    }
}

package com.v7878.dex.util;

import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProtoDescriptor {
    private ProtoDescriptor() {
    }

    private static void parseError(String str, String msg) {
        throw new IllegalArgumentException("Bad signature: " + str + ": " + msg);
    }

    private static List<TypeId> parseDescriptor(String signature) {
        int length = signature.length();
        int[] i = {0};
        var ptypes = new ArrayList<TypeId>();
        if (i[0] < length && signature.charAt(i[0]) == '(') {
            ++i[0];  // skip '('
            while (i[0] < length && signature.charAt(i[0]) != ')') {
                TypeId pt = parseSig(signature, i, length);
                if (pt == null || TypeId.V.equals(pt)) {
                    parseError(signature, "Bad argument type");
                }
                ptypes.add(pt);
            }
            ++i[0];  // skip ')'
        } else {
            parseError(signature, "Not a method type");
        }
        TypeId rtype = parseSig(signature, i, length);
        if (rtype == null || i[0] != length) {
            parseError(signature, "Bad return type");
        }
        ptypes.add(rtype);
        return ptypes;
    }

    private static TypeId parseSig(String str, int[] i, int length) {
        if (i[0] == length) {
            return null;
        }
        char c = str.charAt(i[0]++);
        switch (c) {
            case 'L' -> {
                int begin = i[0] - 1;
                int end = str.indexOf(';', begin);
                if (end < 0) {
                    return null;
                }
                end += 1;
                i[0] = end;
                return TypeId.of(str.substring(begin, end));
            }
            case '[' -> {
                TypeId t = parseSig(str, i, length);
                if (t != null) {
                    t = t.array();
                }
                return t;
            }
            default -> {
                return TypeId.of(Character.toString(c));
            }
        }
    }

    public static ProtoId parseProto(String descriptor) {
        if (descriptor.charAt(0) != '('
                || descriptor.indexOf(')') < 0
                || descriptor.indexOf('.') >= 0) {
            throw new IllegalArgumentException(
                    "Not a proto descriptor: " + descriptor);
        }
        List<TypeId> types = parseDescriptor(descriptor);
        TypeId rtype = types.remove(types.size() - 1);
        types = Collections.unmodifiableList(types);
        return ProtoId.raw(rtype, types);
    }
}

package com.v7878.dex;

import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_BLOCKED;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_O;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_P;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_Q;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_R;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_S;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_SDK;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_UNSUPPORTED;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

// Needed to read or write
final class HiddenApi {

    public static final int ALIGNMENT = 4;

    public static String printFlag(int flag) {
        return switch (flag) {
            case HIDDENAPI_FLAG_SDK -> "sdk";
            case HIDDENAPI_FLAG_UNSUPPORTED -> "unsupported";
            case HIDDENAPI_FLAG_BLOCKED -> "blocked";
            case HIDDENAPI_FLAG_MAX_TARGET_O -> "max-target-o";
            case HIDDENAPI_FLAG_MAX_TARGET_P -> "max-target-p";
            case HIDDENAPI_FLAG_MAX_TARGET_Q -> "max-target-q";
            case HIDDENAPI_FLAG_MAX_TARGET_R -> "max-target-r";
            case HIDDENAPI_FLAG_MAX_TARGET_S -> "max-target-s";
            default -> "[hiddenapi_flag " + flag + "]";
        };
    }

    private static int membersCount(ClassData data) {
        return data.getStaticFields().size() + data.getInstanceFields().size()
                + data.getDirectMethods().size() + data.getVirtualMethods().size();
    }

    private static boolean collectFlags(ClassDef[] classDefs, int[][] hiddenapi_flags) {
        boolean empty = true;
        for (int i = 0; i < classDefs.length; i++) {
            ClassDef def = classDefs[i];
            ClassData data = def.getClassData();
            boolean def_empty = true;
            int index = 0;
            int[] array = new int[membersCount(data)];
            for (var tmp : data.getStaticFields()) {
                int flag = tmp.getHiddenapiFlag();
                array[index++] = flag;
                def_empty &= flag == 0;
            }
            for (var tmp : data.getInstanceFields()) {
                int flag = tmp.getHiddenapiFlag();
                array[index++] = flag;
                def_empty &= flag == 0;
            }
            for (var tmp : data.getDirectMethods()) {
                int flag = tmp.getHiddenapiFlag();
                array[index++] = flag;
                def_empty &= flag == 0;
            }
            for (var tmp : data.getVirtualMethods()) {
                int flag = tmp.getHiddenapiFlag();
                array[index++] = flag;
                def_empty &= flag == 0;
            }
            hiddenapi_flags[i] = def_empty ? null : array;
            empty &= def_empty;
        }
        return !empty;
    }

    private static void checkOptions(DexOptions<?> options) {
        // TODO: should we require dex >= 039?
        options.requireMinApi(29);
    }

    public static void writeSection(WriteContext context, FileMap map,
                                    RandomOutput out, ClassDef[] classDefs) {
        int[][] hiddenapi_flags = new int[classDefs.length][];
        if (!collectFlags(classDefs, hiddenapi_flags)) {
            return;
        }
        checkOptions(context.getOptions());
        out.alignPosition(ALIGNMENT);
        int start = (int) out.position();
        out.addPosition(4); // size
        RandomOutput offsets = out.duplicate(out.position());
        int offsets_size = classDefs.length * 4;
        out.addPosition(offsets_size);
        for (int[] tmp : hiddenapi_flags) {
            if (tmp == null || tmp.length == 0) {
                offsets.writeInt(0);
                continue;
            }

            offsets.writeInt((int) out.position() - start);
            for (int flag : tmp) {
                out.writeULeb128(flag);
            }
        }
        offsets.position(start);
        offsets.writeInt((int) out.position() - start);
        map.hiddenapi_class_data_items_off = start;
    }

    public static void readSection(ReadContext context, FileMap map, ClassDef[] classDefs) {
        int start = map.hiddenapi_class_data_items_off;
        if (start == 0) {
            return;
        }
        checkOptions(context.getOptions());

        RandomInput in = context.data(start);
        in.addPosition(4); // size

        for (ClassDef def : classDefs) {
            int offset = in.readInt();
            if (offset == 0) continue;

            var flags = in.duplicate(start + offset);
            ClassData data = def.getClassData();

            for (var tmp : data.getStaticFields()) {
                tmp.setHiddenapiFlag(flags.readULeb128());
            }
            for (var tmp : data.getInstanceFields()) {
                tmp.setHiddenapiFlag(flags.readULeb128());
            }
            for (var tmp : data.getDirectMethods()) {
                tmp.setHiddenapiFlag(flags.readULeb128());
            }
            for (var tmp : data.getVirtualMethods()) {
                tmp.setHiddenapiFlag(flags.readULeb128());
            }
        }
    }
}

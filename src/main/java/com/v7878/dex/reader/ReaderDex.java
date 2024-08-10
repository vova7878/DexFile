package com.v7878.dex.reader;

import static com.v7878.dex.DexConstants.BASE_HEADER_SIZE;
import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.DexConstants;
import com.v7878.dex.DexVersion;
import com.v7878.dex.ReadOptions;
import com.v7878.dex.base.BaseDex;
import com.v7878.dex.io.ByteArrayInput;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.reader.raw.HiddenApiData;
import com.v7878.dex.reader.raw.MapItem;
import com.v7878.dex.reader.raw.StringId;
import com.v7878.dex.reader.raw.TypeList;
import com.v7878.dex.reader.util.CachedFixedSizeList;
import com.v7878.dex.reader.util.InvalidFile;
import com.v7878.dex.reader.util.NotADexFile;
import com.v7878.dex.reader.value.ReaderEncodedArray;
import com.v7878.dex.util.SparseArray;

import java.util.List;
import java.util.function.IntSupplier;

public class ReaderDex extends BaseDex {
    private final RandomInput main_buffer;
    private final RandomInput data_buffer;

    private final ReadOptions options;
    private final DexVersion version;

    private final SparseArray<TypeList> typelist_section;
    private final SparseArray<ReaderEncodedArray> encoded_array_section;

    private final List<String> string_section;
    private final List<ReaderTypeId> type_section;
    private final List<ReaderProtoId> proto_section;
    private final List<ReaderFieldId> field_section;
    private final List<ReaderMethodId> method_section;
    private final List<ReaderMethodHandleId> method_handle_section;
    private final List<ReaderCallSiteId> callsite_section;
    private final List<ReaderClassDef> class_section;

    private final List<MapItem> map_items;

    private final HiddenApiData hiddenapi_section;

    public ReaderDex(ReadOptions options, byte[] buf, int offset, int header_offset) {
        this.options = options;

        main_buffer = new ByteArrayInput(buf).slice(offset);

        if (main_buffer.size() < BASE_HEADER_SIZE) {
            throw new NotADexFile("File is too short");
        }
        version = DexVersion.forMagic(mainAt(0).readLong());
        //TODO: check version min api

        //TODO: check endian tag

        int container_off = 0;
        if (version.isDexContainer()) {
            container_off = mainAt(header_offset + DexConstants.CONTAINER_OFF_OFFSET).readSmallUInt();
        }
        if (container_off != header_offset) {
            throw new InvalidFile("Unexpected container offset in header");
        }

        int data_off = 0;
        if (version.isCompact()) {
            data_off = mainAt(header_offset + DexConstants.DATA_START_OFFSET).readSmallUInt();
        }
        data_buffer = main_buffer.slice(data_off);

        typelist_section = new SparseArray<>();
        encoded_array_section = new SparseArray<>();

        string_section = makeStringSection(
                mainAt(header_offset + DexConstants.STRING_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexConstants.STRING_START_OFFSET).readSmallUInt()
        );
        type_section = makeTypeSection(
                mainAt(header_offset + DexConstants.TYPE_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexConstants.TYPE_START_OFFSET).readSmallUInt()
        );
        proto_section = makeProtoSection(
                mainAt(header_offset + DexConstants.PROTO_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexConstants.PROTO_START_OFFSET).readSmallUInt()
        );
        field_section = makeFieldSection(
                mainAt(header_offset + DexConstants.FIELD_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexConstants.FIELD_START_OFFSET).readSmallUInt()
        );
        method_section = makeMethodSection(
                mainAt(header_offset + DexConstants.METHOD_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexConstants.METHOD_START_OFFSET).readSmallUInt()
        );

        map_items = makeMapItemsList(mainAt(header_offset + DexConstants.MAP_OFFSET).readSmallUInt());

        MapItem method_handles = getMapItemForSection(DexConstants.TYPE_METHOD_HANDLE_ITEM);
        method_handle_section = makeMethodHandleSection(
                method_handles != null ? method_handles.getItemCount() : 0,
                method_handles != null ? method_handles.getOffset() : NO_OFFSET
        );

        MapItem callsites = getMapItemForSection(DexConstants.TYPE_CALL_SITE_ID_ITEM);
        callsite_section = makeCallSiteSection(
                callsites != null ? callsites.getItemCount() : 0,
                callsites != null ? callsites.getOffset() : NO_OFFSET
        );

        MapItem hiddenapi = getMapItemForSection(DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM);
        hiddenapi_section = new HiddenApiData(this,
                hiddenapi != null ? hiddenapi.getOffset() : NO_OFFSET);

        class_section = makeClassSection(
                mainAt(header_offset + DexConstants.CLASS_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexConstants.CLASS_START_OFFSET).readSmallUInt()
        );
    }

    public ReaderDex(ReadOptions options, byte[] buf) {
        this(options, buf, 0, 0);
    }

    public RandomInput mainAt(int offset) {
        return main_buffer.duplicateAt(offset);
    }

    public RandomInput dataAt(int offset) {
        return data_buffer.duplicateAt(offset);
    }

    public ReadOptions options() {
        return options;
    }

    public DexVersion version() {
        return version;
    }

    private List<MapItem> makeMapItemsList(int map_off) {
        if (map_off == NO_OFFSET) return List.of();
        int map_size = dataAt(map_off).readSmallUInt();
        return new CachedFixedSizeList<>(map_size) {
            @Override
            public MapItem compute(int index) {
                int offset = map_off + 4 + index * MapItem.ITEM_SIZE;
                return new MapItem(ReaderDex.this, offset);
            }
        };
    }

    public List<MapItem> getMapItems() {
        return map_items;
    }

    public MapItem getMapItemForSection(int type) {
        for (MapItem mapItem : getMapItems()) {
            if (mapItem.getType() == type) {
                return mapItem;
            }
        }
        return null;
    }

    public TypeList getTypeList(int offset) {
        TypeList out = typelist_section.get(offset);
        if (out != null) return out;
        out = TypeList.readItem(this, offset);
        typelist_section.put(offset, out);
        return out;
    }

    public ReaderEncodedArray getEncodedArray(int offset) {
        ReaderEncodedArray out = encoded_array_section.get(offset);
        if (out != null) return out;
        out = ReaderEncodedArray.readValue(this, offset);
        encoded_array_section.put(offset, out);
        return out;
    }

    private void checkIndex(int index, int size, String name) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid %s index %d, not in [0, %d)", name, index, size));
        }
    }

    private List<String> makeStringSection(int string_ids_size, int string_ids_off) {
        if (string_ids_size == 0) return List.of();
        return new CachedFixedSizeList<>(string_ids_size) {
            @Override
            protected String compute(int index) {
                return StringId.readItem(ReaderDex.this, index, string_ids_off);
            }
        };
    }

    public String getString(int index) {
        var section = string_section;
        checkIndex(index, section.size(), "string");
        return section.get(index);
    }

    private List<ReaderTypeId> makeTypeSection(int type_ids_size, int type_ids_off) {
        if (type_ids_size == 0) return List.of();
        return new CachedFixedSizeList<>(type_ids_size) {
            @Override
            protected ReaderTypeId compute(int index) {
                return new ReaderTypeId(ReaderDex.this, index, type_ids_off);
            }
        };
    }

    public ReaderTypeId getTypeId(int index) {
        var section = type_section;
        checkIndex(index, section.size(), "type");
        return section.get(index);
    }

    private List<ReaderFieldId> makeFieldSection(int field_ids_size, int field_ids_off) {
        if (field_ids_size == 0) return List.of();
        return new CachedFixedSizeList<>(field_ids_size) {
            @Override
            protected ReaderFieldId compute(int index) {
                return new ReaderFieldId(ReaderDex.this, index, field_ids_off);
            }
        };
    }

    public ReaderFieldId getFieldId(int index) {
        var section = field_section;
        checkIndex(index, section.size(), "field");
        return section.get(index);
    }

    private List<ReaderProtoId> makeProtoSection(int proto_ids_size, int proto_ids_off) {
        if (proto_ids_size == 0) return List.of();
        return new CachedFixedSizeList<>(proto_ids_size) {
            @Override
            protected ReaderProtoId compute(int index) {
                return new ReaderProtoId(ReaderDex.this, index, proto_ids_off);
            }
        };
    }

    public ReaderProtoId getProtoId(int index) {
        var section = proto_section;
        checkIndex(index, section.size(), "proto");
        return section.get(index);
    }

    private List<ReaderMethodId> makeMethodSection(int method_ids_size, int method_ids_off) {
        if (method_ids_size == 0) return List.of();
        return new CachedFixedSizeList<>(method_ids_size) {
            @Override
            protected ReaderMethodId compute(int index) {
                return new ReaderMethodId(ReaderDex.this, index, method_ids_off);
            }
        };
    }

    public ReaderMethodId getMethodId(int index) {
        var section = method_section;
        checkIndex(index, section.size(), "method");
        return section.get(index);
    }

    private List<ReaderMethodHandleId> makeMethodHandleSection(
            int method_handles_size, int method_handles_off) {
        if (method_handles_size == 0) return List.of();
        return new CachedFixedSizeList<>(method_handles_size) {
            @Override
            protected ReaderMethodHandleId compute(int index) {
                return new ReaderMethodHandleId(
                        ReaderDex.this, index, method_handles_off);
            }
        };
    }

    public ReaderMethodHandleId getMethodHandleId(int index) {
        var section = method_handle_section;
        checkIndex(index, section.size(), "method handle");
        return section.get(index);
    }

    private List<ReaderCallSiteId> makeCallSiteSection(
            int callsite_ids_size, int callsite_ids_off) {
        if (callsite_ids_size == 0) return List.of();
        return new CachedFixedSizeList<>(callsite_ids_size) {
            @Override
            protected ReaderCallSiteId compute(int index) {
                return new ReaderCallSiteId(
                        ReaderDex.this, index, callsite_ids_off);
            }
        };
    }

    public ReaderCallSiteId getCallSiteId(int index) {
        var section = callsite_section;
        checkIndex(index, section.size(), "callsite");
        return section.get(index);
    }

    private List<ReaderClassDef> makeClassSection(int class_defs_size, int class_defs_off) {
        if (class_defs_size == 0) return List.of();
        return new CachedFixedSizeList<>(class_defs_size) {
            @Override
            protected ReaderClassDef compute(int index) {
                return new ReaderClassDef(ReaderDex.this, index, class_defs_off);
            }
        };
    }

    @Override
    public List<? extends ReaderClassDef> getClasses() {
        return class_section;
    }

    public IntSupplier getHiddenApiIterator(int class_idx) {
        checkIndex(class_idx, class_section.size(), "class def");
        return hiddenapi_section.iterator(class_idx);
    }
}

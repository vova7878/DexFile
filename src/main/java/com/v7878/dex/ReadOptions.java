package com.v7878.dex;

public final class ReadOptions extends DexOptions<ReadOptions> {
    private final boolean annotations;
    private final boolean field_values;
    private final boolean method_bodies;

    ReadOptions(int api, boolean art, boolean odex, boolean expanded_opcodes,
                boolean hiddenapi, boolean debug_info,
                boolean annotations, boolean field_values, boolean method_bodies) {
        super(api, art, odex, expanded_opcodes, hiddenapi, debug_info);
        this.annotations = annotations;
        this.field_values = field_values;
        this.method_bodies = method_bodies;
    }

    private ReadOptions() {
        super();
        this.annotations = true;
        this.field_values = true;
        this.method_bodies = true;
    }

    @Override
    protected ReadOptions dup(int api, boolean art, boolean odex,
                              boolean expanded_opcodes,
                              boolean hiddenapi, boolean debug_info) {
        return new ReadOptions(api, art, odex, expanded_opcodes, hiddenapi,
                debug_info, annotations, field_values, method_bodies);
    }

    public static ReadOptions defaultOptions() {
        return new ReadOptions();
    }

    public boolean hasAnnotations() {
        return annotations;
    }

    public ReadOptions withAnnotations(boolean annotations) {
        return new ReadOptions(api, art, odex, expanded, hiddenapi,
                debug_info, annotations, field_values, method_bodies);
    }

    public boolean hasFieldValues() {
        return field_values;
    }

    public ReadOptions withFieldValues(boolean field_values) {
        return new ReadOptions(api, art, odex, expanded, hiddenapi,
                debug_info, annotations, field_values, method_bodies);
    }

    public boolean hasMethodBodies() {
        return method_bodies;
    }

    public ReadOptions withMethodBodies(boolean method_bodies) {
        return new ReadOptions(api, art, odex, expanded, hiddenapi,
                debug_info, annotations, field_values, method_bodies);
    }

    //TODO: verify checksum/signature option
}
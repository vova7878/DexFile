@SuppressWarnings("JavaModuleNaming")
module com.v7878.dex {
    exports com.v7878.dex;
    exports com.v7878.dex.builder;
    exports com.v7878.dex.rewriter;
    exports com.v7878.dex.immutable;
    exports com.v7878.dex.immutable.debug;
    exports com.v7878.dex.immutable.value;
    exports com.v7878.dex.immutable.bytecode;
    exports com.v7878.dex.immutable.bytecode.iface;

    // TODO: move to distinct library
    exports com.v7878.collections;

    requires com.v7878.misc;
}

@SuppressWarnings("JavaModuleNaming")
module com.v7878.dex {
    exports com.v7878.dex;
    exports com.v7878.dex.iface;
    exports com.v7878.dex.iface.debug;
    exports com.v7878.dex.iface.value;
    exports com.v7878.dex.iface.bytecode;
    exports com.v7878.dex.iface.bytecode.formats;
    exports com.v7878.dex.base;
    exports com.v7878.dex.base.debug;
    exports com.v7878.dex.base.value;
    exports com.v7878.dex.base.bytecode;
    exports com.v7878.dex.immutable;
    exports com.v7878.dex.immutable.debug;
    exports com.v7878.dex.immutable.value;
    exports com.v7878.dex.immutable.bytecode;

    requires com.v7878.misc;
    requires java.rmi;
}

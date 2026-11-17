package com.v7878.dex.smali;

/**
 * Immutable printing options of the smali writer; the defaults mirror the
 * upstream baksmali disassembler. Instances are built from
 * {@link #defaultOptions()} by {@code withXxx} copies, never mutated.
 */
public final class SmaliOptions {
    private final boolean parameterRegisters;
    private final boolean useLocals;
    private final boolean sequentialLabels;
    private final boolean debugInfo;
    private final boolean codeOffsets;
    private final boolean relativeOffsets;
    private final boolean implicitReferences;

    private SmaliOptions() {
        parameterRegisters = true;
        useLocals = false;
        sequentialLabels = false;
        debugInfo = true;
        codeOffsets = false;
        relativeOffsets = false;
        implicitReferences = false;
    }

    private SmaliOptions(boolean parameterRegisters, boolean useLocals,
                         boolean sequentialLabels, boolean debugInfo,
                         boolean codeOffsets, boolean relativeOffsets,
                         boolean implicitReferences) {
        this.parameterRegisters = parameterRegisters;
        this.useLocals = useLocals;
        this.sequentialLabels = sequentialLabels;
        this.debugInfo = debugInfo;
        this.codeOffsets = codeOffsets;
        this.relativeOffsets = relativeOffsets;
        this.implicitReferences = implicitReferences;
    }

    /**
     * Returns the default options: {@code pN} parameter registers,
     * {@code .registers}, address-based label names, debug directives,
     * no offset comments, label targets and long member references.
     */
    public static SmaliOptions defaultOptions() {
        return new SmaliOptions();
    }

    /**
     * Returns whether registers in the incoming parameter zone are printed
     * as {@code pN} instead of {@code vN}. The {@code p} numbering always
     * counts {@code this} of an instance method as {@code p0}.
     */
    public boolean hasParameterRegisters() {
        return parameterRegisters;
    }

    /**
     * Sets whether registers in the incoming parameter zone are printed
     * as {@code pN} instead of {@code vN}.
     */
    public SmaliOptions withParameterRegisters(boolean parameterRegisters) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }

    /**
     * Returns whether method bodies open with {@code .locals} (the register
     * count without the incoming parameters) instead of {@code .registers}.
     */
    public boolean hasUseLocals() {
        return useLocals;
    }

    /**
     * Sets whether method bodies open with {@code .locals} instead of
     * {@code .registers}.
     */
    public SmaliOptions withUseLocals(boolean useLocals) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }

    /**
     * Returns whether labels are numbered sequentially per prefix instead of
     * being named after their code-unit address.
     */
    public boolean hasSequentialLabels() {
        return sequentialLabels;
    }

    /**
     * Sets whether labels are numbered sequentially per prefix instead of
     * being named after their code-unit address.
     */
    public SmaliOptions withSequentialLabels(boolean sequentialLabels) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }

    /**
     * Returns whether debug directives ({@code .line}, {@code .local},
     * {@code .param} names, {@code .prologue}, {@code .epilogue} and
     * in-method {@code .source}) are printed. When disabled, the printed
     * text re-parses without the dropped information.
     */
    public boolean hasDebugInfo() {
        return debugInfo;
    }

    /**
     * Sets whether debug directives are printed.
     */
    public SmaliOptions withDebugInfo(boolean debugInfo) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }

    /**
     * Returns whether a {@code #@0xN} comment with the code-unit address
     * precedes every instruction.
     */
    public boolean hasCodeOffsets() {
        return codeOffsets;
    }

    /**
     * Sets whether a {@code #@0xN} comment with the code-unit address
     * precedes every instruction.
     */
    public SmaliOptions withCodeOffsets(boolean codeOffsets) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }

    /**
     * Returns whether branch, payload and switch-element targets are printed
     * as raw {@code +0xN}/{@code -0xN} offsets relative to the referring
     * instruction instead of labels, and try ranges as offsets relative to
     * the start of the code. This prints any code, including targets the
     * label form cannot express, but the result is not re-parseable smali.
     */
    public boolean hasRelativeOffsets() {
        return relativeOffsets;
    }

    /**
     * Sets whether targets are printed as raw relative offsets instead of
     * labels; see {@link #hasRelativeOffsets()}.
     */
    public SmaliOptions withRelativeOffsets(boolean relativeOffsets) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }

    /**
     * Returns whether field and method references whose declaring class is
     * the class being printed are shortened to the upstream
     * {@code name:Type} / {@code name(...)...} form.
     * The option never affects {@link SmaliIO#printMethod}, which has no
     * enclosing class to resolve the short form against.
     */
    public boolean hasImplicitReferences() {
        return implicitReferences;
    }

    /**
     * Sets whether member references inside the printed class are shortened;
     * see {@link #hasImplicitReferences()}.
     */
    public SmaliOptions withImplicitReferences(boolean implicitReferences) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences);
    }
}

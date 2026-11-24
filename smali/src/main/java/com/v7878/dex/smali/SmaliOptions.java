package com.v7878.dex.smali;

import com.v7878.dex.analysis.ClassPath;
import com.v7878.dex.analysis.TypeResolver;

/**
 * Immutable printing options of the smali writer; the defaults mirror the
 * upstream baksmali disassembler. Instances are built from
 * {@link #defaultOptions()} by {@code withXxx} copies, never mutated.
 * <p>
 * The analysis and resolution options print read-only {@code #@}
 * comments; they change no code and no executable semantics. The class
 * path source is one slot in two shapes: a {@link ClassPath} or a bare
 * {@link TypeResolver}, and the factories keep exactly one of them set.
 */
public final class SmaliOptions {
    private final boolean parameterRegisters;
    private final boolean useLocals;
    private final boolean sequentialLabels;
    private final boolean debugInfo;
    private final boolean codeOffsets;
    private final boolean relativeOffsets;
    private final boolean implicitReferences;
    private final boolean analysisInfo;
    private final boolean analysisVerification;
    private final boolean resolutionInfo;
    private final ClassPath classPath;
    private final TypeResolver typeResolver;

    private SmaliOptions() {
        parameterRegisters = true;
        useLocals = false;
        sequentialLabels = false;
        debugInfo = true;
        codeOffsets = false;
        relativeOffsets = false;
        implicitReferences = false;
        analysisInfo = false;
        analysisVerification = false;
        resolutionInfo = false;
        classPath = null;
        typeResolver = null;
    }

    private SmaliOptions(boolean parameterRegisters, boolean useLocals,
                         boolean sequentialLabels, boolean debugInfo,
                         boolean codeOffsets, boolean relativeOffsets,
                         boolean implicitReferences, boolean analysisInfo,
                         boolean analysisVerification, boolean resolutionInfo,
                         ClassPath classPath, TypeResolver typeResolver) {
        this.parameterRegisters = parameterRegisters;
        this.useLocals = useLocals;
        this.sequentialLabels = sequentialLabels;
        this.debugInfo = debugInfo;
        this.codeOffsets = codeOffsets;
        this.relativeOffsets = relativeOffsets;
        this.implicitReferences = implicitReferences;
        this.analysisInfo = analysisInfo;
        this.analysisVerification = analysisVerification;
        this.resolutionInfo = resolutionInfo;
        this.classPath = classPath;
        this.typeResolver = typeResolver;
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
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
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
    }

    /**
     * Returns whether the analysis of each method body is printed as
     * {@code #@} comments: the entry register line, the per-instruction
     * state block and its flags. The option also switches the code offset
     * header on, so the header is printed once, never twice.
     */
    public boolean hasAnalysisInfo() {
        return analysisInfo;
    }

    /**
     * Sets whether the analysis of each method body is printed as
     * {@code #@} comments.
     */
    public SmaliOptions withAnalysisInfo(boolean analysisInfo) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
    }

    /**
     * Returns whether the analysis runs with verification: the value-type
     * checks (return types, invoke arguments, field kinds, the super-call
     * rule, cast operand kinds) are on. When off, only the structural checks run.
     */
    public boolean hasAnalysisVerification() {
        return analysisVerification;
    }

    /**
     * Sets whether the analysis runs with verification.
     */
    public SmaliOptions withAnalysisVerification(boolean analysisVerification) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
    }

    /**
     * Returns whether every field and method reference is printed with the
     * {@code #@} comment of its resolution or dispatch answer. The option
     * also runs the analysis, because the receiver type of a virtual call
     * comes from the register state.
     */
    public boolean hasResolutionInfo() {
        return resolutionInfo;
    }

    /**
     * Sets whether reference resolution comments are printed.
     */
    public SmaliOptions withResolutionInfo(boolean resolutionInfo) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, typeResolver);
    }

    /**
     * Returns the class path used to resolve and dispatch references, or
     * {@code null} when none is set. With neither a class path nor a type
     * resolver, the printer builds one from the model it prints.
     */
    public ClassPath getClassPath() {
        return classPath;
    }

    /**
     * Sets the class path used to resolve and dispatch references; clears
     * the type resolver. A {@code null} class path clears the source.
     */
    public SmaliOptions withClassPath(ClassPath classPath) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, classPath, null);
    }

    /**
     * Returns the type resolver used by the analysis for assignability and
     * joins, or {@code null} when none is set.
     */
    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    /**
     * Sets a bare type resolver for the analysis; clears the class path, so
     * reference resolution answers undeterminable. A {@code null} resolver
     * clears the source.
     */
    public SmaliOptions withTypeResolver(TypeResolver typeResolver) {
        return new SmaliOptions(parameterRegisters, useLocals,
                sequentialLabels, debugInfo, codeOffsets,
                relativeOffsets, implicitReferences, analysisInfo,
                analysisVerification, resolutionInfo, null, typeResolver);
    }

    /**
     * Checks the option combination; the printer calls it once per print
     * call, so the {@code withXxx} factories never reject their argument.
     *
     * @throws IllegalArgumentException if verification is enabled while both
     *                                  information options are off, or if both
     *                                  a class path and a type resolver are set
     */
    public void verify() {
        if (analysisVerification && !analysisInfo && !resolutionInfo) {
            throw new IllegalArgumentException(
                    "analysisVerification requires analysisInfo or resolutionInfo");
        }
        if (classPath != null && typeResolver != null) {
            throw new IllegalArgumentException(
                    "classPath and typeResolver are mutually exclusive");
        }
    }
}


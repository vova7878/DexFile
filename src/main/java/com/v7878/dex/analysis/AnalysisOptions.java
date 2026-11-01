package com.v7878.dex.analysis;

public final class AnalysisOptions {
    private final boolean verify;

    private AnalysisOptions(boolean verify) {
        this.verify = verify;
    }

    public static AnalysisOptions defaultOptions() {
        return new AnalysisOptions(true);
    }

    private AnalysisOptions dup(boolean verify) {
        return new AnalysisOptions(verify);
    }

    public boolean hasVerification() {
        return verify;
    }

    public AnalysisOptions withVerification(boolean verify) {
        return dup(verify);
    }
}

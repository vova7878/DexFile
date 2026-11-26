package com.v7878.dex.util;

/**
 * Support for a lazily cached {@code hashCode}.
 * <p>
 * The cache is a plain {@code int}: {@link #NO_HASH} means that the hash is not computed yet.
 * A real zero cannot be stored as is, so {@link #ZERO_HASH} is stored instead.
 */
public final class HashUtils {
    /**
     * The cache is not filled yet.
     */
    public static final int NO_HASH = 0;

    /**
     * Stored instead of a real zero hash.
     */
    public static final int ZERO_HASH = 0x9E3779B9;

    private HashUtils() {
    }

    /**
     * Replaces {@link #NO_HASH} with {@link #ZERO_HASH}, so that a computed hash is never
     * mistaken for an unfilled cache.
     */
    public static int replaceZero(int hash) {
        return hash == NO_HASH ? ZERO_HASH : hash;
    }
}

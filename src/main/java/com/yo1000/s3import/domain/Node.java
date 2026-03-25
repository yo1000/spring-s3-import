package com.yo1000.s3import.domain;

public record Node(
        String id,
        int rank,
        long lastModifiedEpochMillis
) {
    public static final Node INIT_CHECK = new Node("~", Integer.MAX_VALUE, Long.MAX_VALUE);

    public Node update(int newRank) {
        return update(newRank, lastModifiedEpochMillis());
    }

    public Node update(int newRank, long newLastModifiedEpochMillis) {
        return new Node(id(), newRank, newLastModifiedEpochMillis);
    }
}

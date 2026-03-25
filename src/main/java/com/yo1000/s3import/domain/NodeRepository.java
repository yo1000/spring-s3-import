package com.yo1000.s3import.domain;

import java.util.Optional;

public interface NodeRepository {
    Optional<Node> findById(String id);
    Iterable<Node> findAll();
    Node save(Node node);
    void deleteByLastModifiedEpochMillisBefore(long lastModifiedEpochMillis);
}

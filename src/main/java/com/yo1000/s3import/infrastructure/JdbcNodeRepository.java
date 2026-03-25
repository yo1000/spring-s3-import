package com.yo1000.s3import.infrastructure;

import com.yo1000.s3import.domain.Node;
import com.yo1000.s3import.domain.NodeRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcNodeRepository implements NodeRepository {
    private final JdbcClient jdbcClient;

    public JdbcNodeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Node> findById(String id) {
        return jdbcClient
                .sql("""
                    SELECT
                        id,
                        rank,
                        lastmod_epoch_millis AS last_modified_epoch_millis
                    FROM
                        user_node
                    WHERE
                        id = :id
                    """)
                .param("id", id)
                .query(Node.class)
                .optional();
    }

    @Override
    public Iterable<Node> findAll() {
        return jdbcClient
                .sql("""
                    SELECT
                        id,
                        rank,
                        lastmod_epoch_millis AS last_modified_epoch_millis
                    FROM
                        user_node
                    """)
                .query(Node.class)
                .list();
    }

    @Override
    public Node save(Node node) {
        findById(node.id()).ifPresentOrElse(
                n -> jdbcClient
                        .sql("""
                            UPDATE user_node
                            SET
                                rank = :rank,
                                lastmod_epoch_millis = :lastmodEpochMillis
                            WHERE
                                id = :id
                            """)
                        .param("id", n.id())
                        .param("rank", node.rank())
                        .param("lastmodEpochMillis", node.lastModifiedEpochMillis())
                        .update(),
                () -> jdbcClient
                        .sql("""
                            INSERT INTO user_node(
                                id,
                                rank,
                                lastmod_epoch_millis
                            ) VALUES(
                                :id,
                                :rank,
                                :lastmodEpochMillis
                            )
                            """)
                        .param("id", node.id())
                        .param("rank", node.rank())
                        .param("lastmodEpochMillis", node.lastModifiedEpochMillis())
                        .update()
        );

        return node;
    }

    @Override
    public void deleteByLastModifiedEpochMillisBefore(long lastModifiedEpochMillis) {
        jdbcClient
                .sql("""
                    DELETE FROM
                        user_node
                    WHERE
                        lastmod_epoch_millis < :lastModifiedEpochMillis
                    """)
                .param("lastModifiedEpochMillis", lastModifiedEpochMillis)
                .update();
    }
}

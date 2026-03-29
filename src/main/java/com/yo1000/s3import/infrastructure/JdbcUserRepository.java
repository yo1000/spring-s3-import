package com.yo1000.s3import.infrastructure;

import com.yo1000.s3import.domain.User;
import com.yo1000.s3import.domain.UserRepository;
import com.yo1000.s3import.domain.vo.Genders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final NamedParameterJdbcOperations namedJdbcOperations;

    public JdbcUserRepository(NamedParameterJdbcOperations namedJdbcOperations) {
        this.namedJdbcOperations = namedJdbcOperations;
    }

    @Override
    public Optional<User> findById(String id) {
        return namedJdbcOperations.query(
                """
                    SELECT
                        id,
                        username,
                        email,
                        given_name,
                        family_name,
                        gender,
                        birth_date,
                        address,
                        creation_epoch_millis
                    FROM
                        "user"
                    WHERE
                        id = :id
                """,
                new MapSqlParameterSource()
                        .addValue("id", id),
                (rs, rowNum) -> new User(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("given_name"),
                            rs.getString("family_name"),
                            Genders.ofName(rs.getString("gender")),
                            Optional.ofNullable(rs.getDate("birth_date")).map(Date::toLocalDate).orElse(null),
                            rs.getString("address"),
                            rs.getLong("creation_epoch_millis")

                ))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<User> findByUsernameAndCreationEpochMillis(String username, long creationEpochMillis) {
        return namedJdbcOperations.query(
                        """
                            SELECT
                                id,
                                username,
                                email,
                                given_name,
                                family_name,
                                gender,
                                birth_date,
                                address,
                                creation_epoch_millis
                            FROM
                                "user"
                            WHERE
                                username = :username
                            AND creation_epoch_millis = :creationEpochMillis
                        """,
                        new MapSqlParameterSource()
                                .addValue("username", username)
                                .addValue("creationEpochMillis", creationEpochMillis),
                        (rs, rowNum) -> new User(
                                rs.getString("id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("given_name"),
                                rs.getString("family_name"),
                                Genders.ofName(rs.getString("gender")),
                                Optional.ofNullable(rs.getDate("birth_date")).map(Date::toLocalDate).orElse(null),
                                rs.getString("address"),
                                rs.getLong("creation_epoch_millis")))
                .stream()
                .findFirst();
    }

    @Override
    public Page<User> findAllByUsernameLikeAndEmailLikeAndGivenNameLikeAndFamilyNameLikeAndAddressLikeAndCreationEpochMillis(
            String username, String email, String givenName, String familyName, String address,
            long creationEpochMillis, Pageable pageable) {
        String sql = """
                    SELECT
                        id,
                        username,
                        email,
                        given_name,
                        family_name,
                        gender,
                        birth_date,
                        address,
                        creation_epoch_millis
                    FROM
                        "user"
                    WHERE
                        creation_epoch_millis = :creationEpochMillis
                    
                """;

        String totalSql = """
                    SELECT
                        COUNT(*)
                    FROM
                        "user"
                    WHERE
                        creation_epoch_millis = :creationEpochMillis
                """;

        String paramsSql = "";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("creationEpochMillis", creationEpochMillis);

        if (StringUtils.hasText(username)) {
            paramsSql += " AND username LIKE '%' || :username || '%' ";
            params.addValue("username", username);
        }

        if (StringUtils.hasText(email)) {
            paramsSql += " AND email LIKE '%' || :email || '%' ";
            params.addValue("email", email);
        }

        if (StringUtils.hasText(givenName)) {
            paramsSql += " AND given_name LIKE '%' || :givenName || '%' ";
            params.addValue("givenName", givenName);
        }

        if (StringUtils.hasText(familyName)) {
            paramsSql += " AND family_name LIKE '%' || :familyName || '%' ";
            params.addValue("familyName", familyName);
        }

        if (StringUtils.hasText(address)) {
            paramsSql += " AND address LIKE '%' || :address || '%' ";
            params.addValue("address", address);
        }

        sql += paramsSql + """
                    ORDER BY
                        creation_epoch_millis DESC,
                        username
                    OFFSET :offset
                    LIMIT :limit
                """;
        params.addValue("offset", pageable.getOffset());
        params.addValue("limit", pageable.getPageSize());

        totalSql += paramsSql;

        long total = Optional.ofNullable(namedJdbcOperations.queryForObject(totalSql, params, Long.class)).orElse(0L);

        return new PageImpl<>(namedJdbcOperations.query(sql, params, (rs, rowNum) -> new User(
                rs.getString("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("given_name"),
                rs.getString("family_name"),
                Genders.ofName(rs.getString("gender")),
                Optional.ofNullable(rs.getDate("birth_date")).map(Date::toLocalDate).orElse(null),
                rs.getString("address"),
                rs.getLong("creation_epoch_millis"))),
                pageable, total);
    }

    @Override
    public Iterable<Long> findAllCreationEpochMillis() {
        return namedJdbcOperations.queryForList(
                """
                    SELECT
                        creation_epoch_millis
                    FROM
                        "user"
                    GROUP BY
                        creation_epoch_millis
                """,
                Map.of(),
                Long.class
        );
    }

    @Override
    public Iterable<User> saveAll(Iterable<User> entities) {
        final int batchSize = 2000;
        List<SqlParameterSource> batchParams = new ArrayList<>(batchSize);

        for (User user : entities) {
            batchParams.add(new MapSqlParameterSource()
                    .addValue("id", user.id())
                    .addValue("username", user.username())
                    .addValue("email", user.email())
                    .addValue("givenName", user.givenName())
                    .addValue("familyName", user.familyName())
                    .addValue("gender", user.gender().getName())
                    .addValue("birthDate", user.birthDate())
                    .addValue("address", user.address())
                    .addValue("creationEpochMillis", user.creationEpochMillis()));

            if (batchParams.size() >= batchSize) {
                batchUpdate(batchParams);
                batchParams.clear();
            }
        }

        if (!batchParams.isEmpty()) {
            batchUpdate(batchParams);
        }

        // To prioritise performance, the method returns the argument as is.
        return entities;
    }

    private void batchUpdate(List<SqlParameterSource> params) {
        namedJdbcOperations.batchUpdate(
                """
                    INSERT INTO "user" (
                        id,
                        username,
                        email,
                        given_name,
                        family_name,
                        gender,
                        birth_date,
                        address,
                        creation_epoch_millis
                    ) VALUES (
                        :id,
                        :username,
                        :email,
                        :givenName,
                        :familyName,
                        :gender,
                        :birthDate,
                        :address,
                        :creationEpochMillis
                    )
                """,
                params.toArray(MapSqlParameterSource[]::new));
    }

    @Override
    public void deleteAllByCreationEpochMillis(Iterable<Long> creationEpochMillis) {
        namedJdbcOperations.batchUpdate(
                """
                    DELETE FROM
                        "user"
                    WHERE
                        creation_epoch_millis = :creationEpochMillis
                """,
                StreamSupport.stream(creationEpochMillis.spliterator(), false)
                        .map(millis -> new MapSqlParameterSource()
                                .addValue("creationEpochMillis", millis))
                        .toArray(SqlParameterSource[]::new));
    }
}

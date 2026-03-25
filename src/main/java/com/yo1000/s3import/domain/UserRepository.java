package com.yo1000.s3import.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByUsernameAndCreationEpochMillis(String username, long creationEpochMillis);
    Page<User> findAllByUsernameLikeAndEmailLikeAndGivenNameLikeAndFamilyNameLikeAndAddressLikeAndCreationEpochMillis(
            String username, String email, String givenName, String familyName, String address,
            long creationEpochMillis, Pageable pageable);
    Iterable<Long> findAllCreationEpochMillis();
    Iterable<User> saveAll(Iterable<User> entities);
    void deleteAllByCreationEpochMillis(Iterable<Long> creationEpochMillis);
}

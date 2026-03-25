package com.yo1000.s3import.domain;

import com.yo1000.s3import.domain.vo.Genders;

import java.time.LocalDate;

public record User(
        String id,
        String username,
        String email,
        String givenName,
        String familyName,
        Genders gender,
        LocalDate birthDate,
        String address,
        long creationEpochMillis
) {}

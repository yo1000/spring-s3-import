package com.yo1000.s3import.domain.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;

public enum Genders {
    MALE("1", "Male"),
    FEMALE("2", "Female"),
    NONE(null, null),
    ;

    private final String code;
    private final String name;

    Genders(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static Genders ofName(String name) {
        return Arrays.stream(values())
                .filter(gender -> Objects.equals(gender.name, name))
                .findFirst()
                .orElse(NONE);
    }
}

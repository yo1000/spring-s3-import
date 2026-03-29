package com.yo1000.s3import.domain.vo;

public record WaitTime(long millis) {
    public void sleep() {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

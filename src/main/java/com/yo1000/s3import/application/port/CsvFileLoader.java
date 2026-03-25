package com.yo1000.s3import.application.port;

import org.springframework.data.util.CloseableIterator;

import java.io.IOException;

public interface CsvFileLoader<T, R> {
    interface RowHandler<T, R> {
        R handle(T row);
    }

    CloseableIterator<R> loadCsv(RowHandler<T, R> handler) throws IOException;
}

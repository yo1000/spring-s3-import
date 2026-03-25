package com.yo1000.s3import.infrastructure;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.yo1000.s3import.application.port.CsvFileLoader;
import com.yo1000.s3import.application.port.UserCsv;
import com.yo1000.s3import.config.CsvProperties;
import com.yo1000.s3import.config.S3Properties;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Repository
public class S3CsvFileLoader<R> implements CsvFileLoader<UserCsv, R> {
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final CsvSchema csvSchema;
    private final S3Properties s3Props;
    private final CsvProperties csvProps;

    public S3CsvFileLoader(
            S3Client s3Client,
            ObjectMapper objectMapper,
            CsvSchema csvSchema,
            S3Properties s3Props,
            CsvProperties csvProps) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.csvSchema = csvSchema;
        this.s3Props = s3Props;
        this.csvProps = csvProps;
    }

    @Override
    public CloseableIterator<R> loadCsv(RowHandler<UserCsv, R> handler) throws IOException {
        ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(builder -> builder
                .bucket(s3Props.getBucketName())
                .key(csvProps.getFilePath()));
        GZIPInputStream gzipStream = new GZIPInputStream(s3Stream);
        InputStreamReader inputStreamReader = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        MappingIterator<UserCsv> iter = objectMapper
                .readerFor(UserCsv.class)
                .with(csvSchema)
                .readValues(bufferedReader);

        return new RowHandlingIterator<>(iter, handler);
    }

    static class RowHandlingIterator<T, R> implements CloseableIterator<R> {
        private final MappingIterator<T> sourceIterator;
        private final RowHandler<T, R> rowHandler;

        public RowHandlingIterator(MappingIterator<T> sourceIterator, RowHandler<T, R> rowHandler) {
            this.sourceIterator = sourceIterator;
            this.rowHandler = rowHandler;
        }

        @Override
        public boolean hasNext() {
            try {
                return sourceIterator.hasNextValue();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public R next() {
            try {
                return rowHandler.handle(sourceIterator.nextValue());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() {
            try {
                sourceIterator.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}

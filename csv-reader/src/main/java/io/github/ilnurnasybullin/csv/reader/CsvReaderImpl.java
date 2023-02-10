package io.github.ilnurnasybullin.csv.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

class CsvReaderImpl implements CsvReader {

    @Override
    public Stream<CsvReader.Rows> readCsv(InputStream stream, Config config) {
        InputStreamReader streamReader = new InputStreamReader(stream, config.charset());
        BufferedReader reader = new BufferedReader(streamReader);
        return reader.lines()
                .onClose(() -> closeStreams(reader, streamReader))
                .map(new RowReader(!config.hasHeader(), config.regexSplitter()))
                .flatMap(Optional::stream);
    }

    private void closeStreams(AutoCloseable ... autoCloseables) {
        for (AutoCloseable autoCloseable: autoCloseables) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Rows implements CsvReader.Rows {

        private final String[] values;
        private final Map<String, String> headers;

        private Rows(String[] values, Map<String, String> headers) {
            this.values = values;
            this.headers = headers;
        }

        @Override
        public Optional<String> onColumn(String column) {
            return Optional.ofNullable(headers.get(column));
        }

        @Override
        public Optional<String> onIndex(int index) {
            if (index >= values.length) {
                return Optional.empty();
            }

            return Optional.of(values[index]);
        }

        @Override
        public Iterator<String> iterator() {
            return Arrays.asList(values).iterator();
        }
    }

    private static class RowReader implements Function<String, Optional<CsvReader.Rows>> {

        private boolean headerIsRead;
        private final String delimiter;
        private Map<Integer, String> headers;

        public RowReader(boolean headerIsRead, String delimiter) {
            this.headerIsRead = headerIsRead;
            this.delimiter = delimiter;
            headers = Map.of();
        }

        @Override
        public Optional<CsvReader.Rows> apply(String line) {
            if (headerIsRead) {
                return Optional.of(readRow(line));
            }

            headers = readHeaders(line);
            headerIsRead = true;
            return Optional.empty();
        }

        private Map<Integer, String> readHeaders(String line) {
            String[] headers = line.split(delimiter);
            Map<Integer, String> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(i, headers[i]);
            }

            return headerMap;
        }

        private CsvReader.Rows readRow(String line) {
            String[] values = line.split(delimiter);
            Map<String, String> headers = new HashMap<>();
            this.headers.forEach((index, header) -> {
                headers.put(header, values[index]);
            });

            return new Rows(values, headers);
        }

    }
}

package io.github.ilnurnasybullin.csv.writer;

import java.io.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CsvWriterImpl implements CsvWriter {
    @Override
    public <T> void writeToCsv(OutputStream stream, Stream<T> objects, Config<T> config) throws IOException {
        try(OutputStreamWriter writer = new OutputStreamWriter(stream, config.charset());
            BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            writeHeaders(bufferedWriter, config);
            objects.forEach(object -> writeObject(bufferedWriter, object, config));
        }
    }

    private <T> void writeObject(BufferedWriter writer, T object, Config<T> config) {
        BiFunction<T, String, String> function = config.toFieldFunction();

        try {
            String line = config.headers().isEmpty() ? object.toString() :
                    config.headers()
                            .stream()
                            .map(header -> function.apply(object, header))
                            .collect(Collectors.joining(config.splitter()));
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> void writeHeaders(BufferedWriter writer, Config<T> config) throws IOException {
        if (config.headers().isEmpty()) {
            return;
        }

        String line = config.headers()
                .stream()
                .collect(Collectors.joining(config.splitter()));
        writer.write(line);
        writer.newLine();
    }
}

package io.github.ilnurnasybullin.csv.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public interface CsvWriter {
    interface Config<T> {
        Charset charset();
        String splitter();
        List<String> headers();
        BiFunction<T, String, String> toFieldFunction();
    }

    <T> void writeToCsv(OutputStream stream, Stream<T> objects, Config<T> config) throws IOException;

    static CsvWriter getInstance() {
        return ServiceLoader.load(CsvWriter.class)
                .findFirst()
                .orElseGet(CsvWriterImpl::new);
    }
}

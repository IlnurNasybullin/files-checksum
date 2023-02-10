package io.github.ilnurnasybullin.csv.reader;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface CsvReader {
    interface Config {
        Charset charset();
        String regexSplitter();
        boolean hasHeader();
    }

    Stream<Rows> readCsv(InputStream stream, Config config);

    interface Rows extends Iterable<String> {
        Optional<String> onColumn(String column);
        Optional<String> onIndex(int index);
    }

    static CsvReader getInstance() {
        return ServiceLoader.load(CsvReader.class)
                .findFirst()
                .orElseGet(CsvReaderImpl::new);
    }
}

package io.github.ilnurnasybullin.files.checksum.cli.subcommands;

import io.github.ilnurnasybullin.csv.reader.CsvReader;
import io.github.ilnurnasybullin.csv.writer.CsvWriter;
import io.github.ilnurnasybullin.files.checksum.core.ChecksumAlgorithm;
import io.github.ilnurnasybullin.files.checksum.core.ChecksumAlgorithmProvider;
import io.github.ilnurnasybullin.files.checksum.core.ChecksumCalculator;
import io.github.ilnurnasybullin.files.checksum.core.CoreChecksumAlgorithmProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "checksum")
public class ChecksumCommand implements Runnable {

    private final static String CHECKSUM_EXTENSION = ".checksum";

    private final static String FILE_HEADER = "file";
    private final static String CHECKSUM_HEADER = "checksum";

    private final Predicate<Path> filesPredicate = file -> !Files.isDirectory(file) &&
            !file.toString().endsWith(CHECKSUM_EXTENSION);

    private final ChecksumAlgorithmProvider algorithmProvider;
    private final CsvReader csvReader;

    @Option(names = {"-a", "--append"})
    private boolean append = false;

    @Option(names = {"-r", "--recursive"})
    private boolean recursive = false;

    @Parameters(index = "0")
    private Path file;

    @Parameters(index = "1")
    private ChecksumAlgorithm algorithm;

    public ChecksumCommand() {
        this(new CoreChecksumAlgorithmProvider(), CsvReader.getInstance());
    }

    public ChecksumCommand(ChecksumAlgorithmProvider algorithmProvider, CsvReader csvReader) {
        this.algorithmProvider = algorithmProvider;
        this.csvReader = csvReader;
    }

    @Override
    public void run() {
        validateParameters();
        List<Path> filesToChecksum = filesToChecksum(file, recursive);
        List<Checksum> checksums = calculateChecksums(filesToChecksum, algorithm, append);
        writeChecksumsToFiles(checksums, append);
    }

    private void writeChecksumsToFiles(List<Checksum> checksums, boolean append) {
        Map<ChecksumAlgorithm, List<Checksum>> groupedByAlgorithms = checksums.stream()
                .collect(Collectors.groupingBy(Checksum::algorithm));

        groupedByAlgorithms.forEach((algorithm, mapChecksums) -> {
            writeChecksumsToFiles(algorithm, mapChecksums, append);
        });
    }

    private void writeChecksumsToFiles(ChecksumAlgorithm algorithm, List<Checksum> checksums, boolean append) {
        Map<Path, List<Checksum>> folderChecksums = checksums.stream()
                .collect(Collectors.groupingBy(checksum -> toFolder(checksum.file())));

        folderChecksums.forEach((folder, mapChecksums) -> {
            writeChecksumsToFiles(algorithm, folder, mapChecksums, append);
        });
    }

    private void writeChecksumsToFiles(ChecksumAlgorithm algorithm, Path folder, List<Checksum> checksums, boolean append) {
        Path path = checksumFileName(folder, algorithm);

        CsvWriter writer = CsvWriter.getInstance();
        try(OutputStream stream = Files.newOutputStream(path, writeOptions(append))) {
            writer.writeToCsv(stream, checksums.stream(), new CsvWriterConfig());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OpenOption[] writeOptions(boolean append) {
        if (append) {
            return new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
        }

        return new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
    }

    private List<Checksum> calculateChecksums(List<Path> files, ChecksumAlgorithm algorithm, boolean append) {
        Map<Path, Set<Path>> checksumCalculatedFiles = new HashMap<>();
        ChecksumCalculator calculator = algorithmProvider.getByChecksumAlgorithm(algorithm)
                .get()
                .get();

        List<Checksum> checksums = new ArrayList<>();
        for (Path file: files) {
            Path folder = toFolder(file);
            checksumCalculatedFiles.computeIfAbsent(folder, fd -> getCalculatedChecksumsForFolder(fd, algorithm, append));

            if (checksumCalculatedFiles.get(folder).contains(file)) {
                continue;
            }

            byte[] checksumBytes;
            try(InputStream stream = Files.newInputStream(file, StandardOpenOption.READ)) {
                checksumBytes = calculator.calculate(stream, algorithm);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Checksum checksum = new Checksum(file, checksumBytes, algorithm);
            checksums.add(checksum);
        }

        return checksums;
    }

    private Set<Path> getCalculatedChecksumsForFolder(Path folder, ChecksumAlgorithm algorithm, boolean append) {
        if (!append) {
            return Set.of();
        }

        Path checksumFile = checksumFileName(folder, algorithm);
        if (!Files.exists(checksumFile)) {
            return Set.of();
        }

        return filesInChecksumFile(checksumFile);
    }

    private Set<Path> filesInChecksumFile(Path checksumFile) {
        try(InputStream stream = Files.newInputStream(checksumFile, StandardOpenOption.READ)) {
            return csvReader.readCsv(stream, new CsvReaderConfig())
                    .map(rows -> rows.onColumn(FILE_HEADER).orElseThrow())
                    .map(Path::of)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path checksumFileName(Path folder, ChecksumAlgorithm algorithm) {
        String filename = String.format(".%s%s", algorithm.type(), CHECKSUM_EXTENSION);
        return folder.resolve(filename);
    }

    private Path toFolder(Path file) {
        return file.getParent();
    }

    private List<Path> filesToChecksum(Path file, boolean recursive) {
        try {
            if (Files.isDirectory(file)) {
                if (!recursive) {
                    try(Stream<Path> files = Files.list(file)) {
                        return files.filter(path -> !Files.isDirectory(path))
                                .filter(path -> !path.toString().endsWith(".checksum"))
                                .toList();
                    }
                }

                try(Stream<Path> files = Files.walk(file)) {
                    return files.filter(path -> !Files.isDirectory(path))
                            .filter(path -> !path.toString().endsWith(".checksum"))
                            .toList();
                }
            }

            return List.of(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateParameters() {
        if (file == null) {
            throw new IllegalArgumentException("File is not defined!");
        }

        if (!Files.exists(file)) {
            throw new IllegalArgumentException(
                    new FileNotFoundException(String.format("File %s is not found!", file))
            );
        }

        if (!Files.isDirectory(file) && !filesPredicate.test(file)) {
            throw new IllegalArgumentException("File %s is not supported for checksum calculation");
        }

        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm is not defined!");
        }

        if (algorithmProvider.getByChecksumAlgorithm(algorithm).isEmpty()) {
            throw new IllegalArgumentException(String.format("For algorithm %s checksum provider is not found!", algorithm));
        }
    }

    private final static String SPLITTER = "|";

    private record CsvReaderConfig() implements CsvReader.Config {
        @Override
        public Charset charset() {
            return StandardCharsets.UTF_8;
        }

        @Override
        public String regexSplitter() {
            return "\\|";
        }

        @Override
        public boolean hasHeader() {
            return true;
        }
    }

    private record CsvWriterConfig() implements CsvWriter.Config<Checksum> {

        @Override
        public Charset charset() {
            return StandardCharsets.UTF_8;
        }

        @Override
        public String splitter() {
            return SPLITTER;
        }

        @Override
        public List<String> headers() {
            return List.of(FILE_HEADER, CHECKSUM_HEADER);
        }

        @Override
        public BiFunction<Checksum, String, String> toFieldFunction() {
            return (checksum, field) -> switch (field) {
                case FILE_HEADER -> checksum.file().toString();
                case CHECKSUM_HEADER -> HexFormat.of().formatHex(checksum.checksum());
                default -> throw new IllegalArgumentException(String.format("Illegal field: %s", field));
            };
        }
    }

}

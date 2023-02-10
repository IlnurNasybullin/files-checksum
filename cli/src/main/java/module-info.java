open module io.github.ilnurnasybullin.files.checksum.cli {
    uses io.github.ilnurnasybullin.files.checksum.core.ChecksumAlgorithmProvider;

    requires io.github.ilnurnasybullin.files.checksum.core;
    requires info.picocli;
    requires io.github.ilnurnasybullin.csv.reader;
    requires io.github.ilnurnasybullin.csv.writer;

    exports io.github.ilnurnasybullin.files.checksum.cli.subcommands;
}
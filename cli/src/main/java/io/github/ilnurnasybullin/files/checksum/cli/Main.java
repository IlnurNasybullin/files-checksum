package io.github.ilnurnasybullin.files.checksum.cli;

import io.github.ilnurnasybullin.files.checksum.cli.subcommands.ChecksumCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(subcommands = {
    ChecksumCommand.class
})
public class Main {
    public static void main(String[] args) {
        new CommandLine(new Main())
                .execute(args);
    }
}

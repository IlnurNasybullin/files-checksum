package io.github.ilnurnasybullin.files.checksum.cli.subcommands;

import io.github.ilnurnasybullin.files.checksum.core.ChecksumAlgorithm;

import java.nio.file.Path;

record Checksum(Path file, byte[] checksum, ChecksumAlgorithm algorithm) {}

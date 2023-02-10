package io.github.ilnurnasybullin.files.checksum.core;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public interface ChecksumAlgorithmProvider extends ServiceLoader.Provider<ChecksumAlgorithmProvider> {
    Optional<Supplier<ChecksumCalculator>> getByChecksumAlgorithm(ChecksumAlgorithm algorithm);
}

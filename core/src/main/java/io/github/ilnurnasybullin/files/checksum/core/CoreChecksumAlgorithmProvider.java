package io.github.ilnurnasybullin.files.checksum.core;

import java.util.Optional;
import java.util.function.Supplier;

public class CoreChecksumAlgorithmProvider implements ChecksumAlgorithmProvider {
    @Override
    public Optional<Supplier<ChecksumCalculator>> getByChecksumAlgorithm(ChecksumAlgorithm algorithm) {
        if (MessageDigestCalculator.algorithms().contains(algorithm)) {
            return Optional.of(MessageDigestCalculator::new);
        }

        return Optional.empty();
    }

    @Override
    public Class<? extends ChecksumAlgorithmProvider> type() {
        return CoreChecksumAlgorithmProvider.class;
    }

    @Override
    public ChecksumAlgorithmProvider get() {
        return this;
    }
}

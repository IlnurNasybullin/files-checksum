package io.github.ilnurnasybullin.files.checksum.core;

import java.io.InputStream;
import java.util.Set;

public interface ChecksumCalculator {
    byte[] calculate(InputStream stream, ChecksumAlgorithm algorithm);
    Set<ChecksumAlgorithm> supportedAlgorithms();
}

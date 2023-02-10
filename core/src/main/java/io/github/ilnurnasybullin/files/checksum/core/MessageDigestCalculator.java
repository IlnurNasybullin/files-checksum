package io.github.ilnurnasybullin.files.checksum.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

class MessageDigestCalculator implements ChecksumCalculator {

    @Override
    public byte[] calculate(InputStream stream, ChecksumAlgorithm algorithm) {
        byte[] buffer = new byte[1024];

        byte[] bytes;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.type());
            try(DigestInputStream inputStream = new DigestInputStream(stream, messageDigest)) {
                while (inputStream.read(buffer) != -1) {}
            }
            bytes = messageDigest.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }

        return bytes;
    }

    @Override
    public Set<ChecksumAlgorithm> supportedAlgorithms() {
        return algorithms();
    }

    static Set<ChecksumAlgorithm> algorithms() {
        return Set.of(ChecksumAlgorithm.values());
    }
}

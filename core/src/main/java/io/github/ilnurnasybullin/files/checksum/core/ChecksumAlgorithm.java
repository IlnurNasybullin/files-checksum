package io.github.ilnurnasybullin.files.checksum.core;

public enum ChecksumAlgorithm {
    MD5("md5"),
    SHA256("sha256");

    private final String type;

    ChecksumAlgorithm(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}

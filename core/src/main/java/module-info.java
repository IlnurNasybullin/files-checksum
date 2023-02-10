import io.github.ilnurnasybullin.files.checksum.core.ChecksumAlgorithmProvider;
import io.github.ilnurnasybullin.files.checksum.core.CoreChecksumAlgorithmProvider;

module io.github.ilnurnasybullin.files.checksum.core {
    exports io.github.ilnurnasybullin.files.checksum.core;
    provides ChecksumAlgorithmProvider with CoreChecksumAlgorithmProvider;
}
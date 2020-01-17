package com.harsh.bullrun;

/**
 * A basic implementation of the {@link Checksum} interface.
 */
public class SimpleChecksum implements Checksum {
    private String fileName;
    private String algorithm;
    private String hashValue;
    private Boolean verified;

    /**
     * Constructor for a simple checksum.
     *
     * @param fileName the file whose hash is calculated
     * @param algorithm the cryptographic algorithm used to calculate the file's digest
     * @param hashValue the digest/hash of the file
     * @param verified true or false depending on whether the checksum is verified; null if the
     *                 checksum hasn't even been checked for verification of file's integrity.
     */
    SimpleChecksum(String fileName, String algorithm, String hashValue, Boolean verified) {
        this.fileName = fileName;
        this.algorithm = algorithm;
        this.hashValue = hashValue;
        this.verified = verified;
    }

    /**
     * Constructor for a simple checksum. This assumes the verification status to be
     * <code>null</code>. See {@link Checksum#isVerified()} for more information.
     *
     * @param fileName the file whose hash is calculated
     * @param algorithm the cryptographic algorithm used to calculate the file's digest
     * @param hashValue the digest/hash of the file
     */
    SimpleChecksum(String fileName, String algorithm, String hashValue) {
        this(fileName, algorithm, hashValue, null);
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getAlgorithm() {
        return this.algorithm;
    }

    @Override
    public String getHashValue() {
        return this.hashValue;
    }

    @Override
    public Boolean isVerified() {
        return this.verified;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj != null && this.getClass().getName().equalsIgnoreCase(obj.getClass().getName())) {
            Checksum checksum = (Checksum) obj;
            isEqual = this.getAlgorithm().equalsIgnoreCase(checksum.getAlgorithm());
            isEqual = isEqual && this.getHashValue().equalsIgnoreCase(checksum.getHashValue());
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return (this.getHashValue() + this.getAlgorithm()).hashCode();
    }
}
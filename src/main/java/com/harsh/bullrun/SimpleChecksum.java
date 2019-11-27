package com.harsh.bullrun;

public class SimpleChecksum implements Checksum {
    private String fileName;
    private String algorithm;
    private String hashValue;
    private Boolean verified;

    SimpleChecksum(String fileName, String algorithm, String hashValue, Boolean verified) {
        // TODO: check for null fileName, algorithm and hashValue. verified can have null values
        this.fileName = fileName;
        this.algorithm = algorithm;
        this.hashValue = hashValue;
        this.verified = verified;
    }

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
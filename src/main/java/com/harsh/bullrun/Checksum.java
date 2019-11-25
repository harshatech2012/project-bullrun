package com.harsh.bullrun;

public interface Checksum {

    public String getFileName();

    public String getAlgorithm();

    public String getHashValue();

    public Boolean isVerified();

}

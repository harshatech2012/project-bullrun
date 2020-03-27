package com.harsh.bullrun;

/**
 * Marker interface for checksums. A checksum is a datum used to verify
 * integrity of some data.
 */
public interface Checksum {

    /**
     * Returns the type of checksum that this instance represents. Checksum can be generated
     * using different types of algorithms - Cryptographic Hash Functions, Parity-Bit methods etc.
     *
     * <p>The value returned by this method should be such that it could be used for
     * identifying the checksum type.</p>
     *
     * @return string identifier of the checksum type. Should be user-readable and understandable.
     */
    public String getChecksumType();

}

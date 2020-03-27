package com.harsh.bullrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interface defines the fundamental quantities/properties for fully defining a
 * cryptographic message digest of a file.
 */
public class HashChecksum implements Checksum {
    private static final Logger logger = LoggerFactory.getLogger(HashChecksum.class);

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
    HashChecksum(String fileName, String algorithm, String hashValue, Boolean verified) {
        this.fileName = fileName;
        this.algorithm = algorithm;
        this.hashValue = hashValue;
        this.verified = verified;
    }

    /**
     * Constructor for a simple checksum. This assumes the verification status to be
     * <code>null</code>. See {@link HashChecksum#isVerified()} for more information.
     *
     * @param fileName the file whose hash is calculated
     * @param algorithm the cryptographic algorithm used to calculate the file's digest
     * @param hashValue the digest/hash of the file
     */
    HashChecksum(String fileName, String algorithm, String hashValue) {
        this(fileName, algorithm, hashValue, null);
    }


    /**
     * Returns the name of the file corresponding to the hash.
     *
     * @return name of the file
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Returns the cryptographic algorithm's name used for calculating the file's digest.
     *
     * @return name of the cryptographic algorithm used
     */
    public String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns the hash/digest/checksum of the file.
     *
     * @return hash/digest/checksum of the file
     */
    public String getHashValue() {
        return this.hashValue;
    }

    /**
     * Returns whether the hash has been verified against a 3rd party source. Normally, the
     * file's publisher provides checksum's, using various algorithms, for verifying file's
     * integrity.
     *
     * <p>The method should return three different values, namely null, false, and true in the
     * following cases:
     * <ul>
     *     <li><code>null</code> - if the calculated checksum has not been checked against any
     *     3rd party source, like checksum from the file's official publisher</li>
     *     <li>
     *         But if checked against a 3rd party checksum value, then:
     *         <ul>
     *             <li><code>false</code> - if the checksum's don't match</li>
     *             <li><code>true</code> - if the checksum's match</li>
     *         </ul>
     *     </li>
     * </ul></p>
     *
     * @return null if checksum has not been checked against any 3rd party value; But if
     * checked against a 3rd party value then true if the file's integrity has been verified, and
     * false otherwise.
     */
    public Boolean isVerified() {
        return this.verified;
    }

    @Override
    public String getChecksumType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        // fully qualified name
        return this.getClass().getCanonicalName();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj != null && this.getClass().getName().equalsIgnoreCase(obj.getClass().getName())) {
            HashChecksum checksum = (HashChecksum) obj;
            isEqual = this.getAlgorithm().equalsIgnoreCase(checksum.getAlgorithm());
            isEqual = isEqual && this.getHashValue().equalsIgnoreCase(checksum.getHashValue());

            if (isEqual && !this.fileName.equalsIgnoreCase(checksum.getFileName())) {
                logger.warn("Possible Hash Collision: \n\tBetween: {} and {}" +
                                "\n\tUsing Algorithm: {}" +
                                "\nInvestigation Required, contact developer immediately!",
                        this.fileName, checksum.getFileName(), this.algorithm);
                // TODO: add code to alert the user and developer (with users' permission, duh!)
            }
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return (this.getHashValue() + this.getAlgorithm()).hashCode();
    }
}
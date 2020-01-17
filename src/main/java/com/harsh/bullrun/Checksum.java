package com.harsh.bullrun;

/**
 * This interface defines the fundamental quantities/properties for fully defining a
 * cryptographic message digest of a file.
 */
public interface Checksum {

    /**
     * Returns the name of the file corresponding to the hash.
     *
     * @return name of the file
     */
    public String getFileName();

    /**
     * Returns the cryptographic algorithm's name used for calculating the file's digest.
     *
     * @return name of the cryptographic algorithm used
     */
    public String getAlgorithm();

    /**
     * Returns the hash/digest/checksum of the file.
     *
     * @return hash/digest/checksum of the file
     */
    public String getHashValue();

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
    public Boolean isVerified();

}

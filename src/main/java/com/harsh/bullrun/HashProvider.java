package com.harsh.bullrun;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

/**
 * A provider of various hash algorithm implementations. The default library of hash providers
 * used here is the BouncyCastle, developed and maintained by
 * <a href="https://www.bouncycastle.org/">Legion of Bouncy Castle</a>.
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public class HashProvider {
    private static final Logger logger = LoggerFactory.getLogger(HashProvider.class);
    private static final String BOUNCY_CASTLE = "BC";

    /**
     * List of supported digests.
     */
    private Set<String> supportedDigests;

    /**
     * Constructor that allows for specifying custom cryptographic algorithm's libraries.
     *
     * @param serviceProvider a provider of cryptographic algorithms
     */
    HashProvider(Provider serviceProvider) {
        Security.addProvider(serviceProvider);

        this.supportedDigests = new HashSet<>();
        Set<Provider.Service> services = serviceProvider.getServices();
        for (Provider.Service srv : services) {
            if (srv.getType().equals("MessageDigest")) {
                this.supportedDigests.add(srv.getAlgorithm().toUpperCase());
            }
        }
    }

    /**
     * Constructor for creating an instance of this class with BouncyCastle as the cryptographic
     * provider.
     */
    HashProvider() {
        this(new BouncyCastleProvider());
    }

    /**
     * Computes the specified digest for the specified file in hexadecimal format.
     *
     * @param file whose digest is required. The path should be absolute.
     * @param algorithm the digest algorithm to use. Check whether the algorithm is supported
     *                  using {@link this#supports(String)} method before calling this method.
     * @return the file's digest as a hexadecimal string. The return value is always in lowercase.
     * @throws IOException if an I/O exception occurs while reading the file
     * @throws NoSuchAlgorithmException if th specified digest algorithm is not found
     */
    String computeHash(Path file, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        String hexCode = null;
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance(algorithm, HashProvider.BOUNCY_CASTLE);
            DigestUtils digestUtils = new DigestUtils(messageDigest);
            hexCode = digestUtils.digestAsHex(new File(file.toUri())).toLowerCase();
        } catch (NoSuchProviderException except) {
            // Package BouncyCastle library with this application
            logger.error("Library Missing: Bouncy Castle crypto-provider not found!");
            System.exit(-1);
        }

        return hexCode;
    }

    /**
     * Checks whether the specified digest algorithm is supported by <code>this</code> provider.
     *
     * @param algorithm cryptographic algorithm to check for
     * @return true if the specified algorithm is supported
     */
    boolean supports(String algorithm) {
        return this.supportedDigests.contains(algorithm.toUpperCase());
    }
}

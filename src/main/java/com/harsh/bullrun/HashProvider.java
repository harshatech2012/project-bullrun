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

public class HashProvider {
    private static final Logger logger = LoggerFactory.getLogger(HashProvider.class);
    private static final String BOUNCY_CASTLE = "BC";
    private Set<String> supportedDigests;

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

    HashProvider() {
        this(new BouncyCastleProvider());
    }

    // doc should highlight that the hexCode is always returned in lowercase
    String computeHash(Path file, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        String hexCode = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm, HashProvider.BOUNCY_CASTLE);
            DigestUtils digestUtils = new DigestUtils(messageDigest);
            hexCode = digestUtils.digestAsHex(new File(file.toUri())).toLowerCase();
        } catch (NoSuchProviderException except) {
            // Package BouncyCastle library with this application
            logger.error("Library Missing: Bouncy Castle crypto-provider not found!");
            System.exit(-1);
        }

        return hexCode;
    }

    boolean supports(String algorithm) {
        return this.supportedDigests.contains(algorithm.toUpperCase());
    }
}

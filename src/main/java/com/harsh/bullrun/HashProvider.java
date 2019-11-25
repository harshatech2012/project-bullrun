package com.harsh.bullrun;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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
    private static final String BOUNCY_CASTLE = "BC";
    private Set<String> supportedDigests = new HashSet<>();

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

    public String computeHash(Path file, String algorithm)
            throws IOException, NoSuchAlgorithmException {
        String hexCode = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm, HashProvider.BOUNCY_CASTLE);
            DigestUtils digestUtils = new DigestUtils(messageDigest);
            hexCode = digestUtils.digestAsHex(new File(file.toUri()));
        } catch (NoSuchProviderException except) {
            // Package BouncyCastle library with this application
            System.err.println("Library Missing: Bouncy Castle crypto-provider not found!");
        }

        return hexCode;
    }

    boolean supports(String algorithm) {
        return this.supportedDigests.contains(algorithm.toUpperCase());
    }
}
package com.harsh.bullrun;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

public class HashProvider {
    private static final String BOUNCY_CASTLE = "BC";
    private static HashProvider hashProvider = null;

    private HashProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static HashProvider getInstance() {
        if (HashProvider.hashProvider == null) {
            HashProvider.hashProvider = new HashProvider();
        }

        return HashProvider.hashProvider;
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
}

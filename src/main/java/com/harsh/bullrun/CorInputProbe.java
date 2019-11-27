package com.harsh.bullrun;

import com.google.common.collect.Sets;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorInputProbe implements InputProbingStrategy {
    private static final String name = "COR Linear Strategy";

    private HashProvider hashProvider;
    private RequestHandler requestHandlerChain;

    private enum ProbeParameters { FILE_PATHS, ALGORITHMS, HASHES, CHECKS, STRICT_CHECK, OMIT_HASH}

    {
        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionShort = "a";
                String[] algorithms = request.getOptionValues(optionShort);
                for (String algo : algorithms) {
                    if (!hashProvider.supports(algo)) {
                        throw new IllegalArgumentException(
                                String.format("Unsupported Algorithm: %s", algo));
                    }
                }

                request.addParameter(ProbeParameters.ALGORITHMS.name(), algorithms);
                this.getDelegate().handle(request);
            }
        });

        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionShort = "f";
                String[] filePaths = request.getOptionValues(optionShort);
                try {
                    for (int i = 0; i < filePaths.length; i++) {
                        // check for file existence and replace with absolute path
                        filePaths[i] = Paths.get(filePaths[i]).toRealPath().toString();
                    }
                } catch (IOException except) {
                    try {
                        // check if NoSuchFileException was thrown,
                        // in which case the getMessage() returns the file's path
                        throw new IllegalArgumentException(String.format(
                                "File Not Found: %s",
                                Paths.get(except.getMessage()).toString()));
                    } catch (InvalidPathException verify) {
                        throw new IllegalStateException(
                                "Error: Ensure the application has necessary privileges and retry");
                    }

                }

                request.addParameter(ProbeParameters.FILE_PATHS.name(), filePaths);
                this.getDelegate().handle(request);
            }
        });

        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionLong = "one-to-one";
                String[] filePaths = (String[]) request.getParameter(ProbeParameters.FILE_PATHS.name());
                String[] algorithms = (String[]) request.getParameter(ProbeParameters.ALGORITHMS.name());
                String[][] hashes;

                if (request.hasOption(optionLong)) {
                    if (filePaths.length != algorithms.length) {
                        throw new IllegalArgumentException(
                                String.format("Cannot establish one-to-one mapping between file(s) and " +
                                        "algorithm(s). File [%d] and algorithm [%s] counts " +
                                        "don't match.", filePaths.length, algorithms.length));
                    }

                    hashes = new String[filePaths.length][2];
                    for (int i = 0; i < filePaths.length; i++) { // could have also used algorithms.length
                        hashes[i][0] = filePaths[i];
                        hashes[i][1] = algorithms[i];
                    }
                } else {
                    Set<List<String>> product = Sets.cartesianProduct(
                            new HashSet<>(Arrays.asList(filePaths)),
                            new HashSet<>(Arrays.asList(algorithms))
                    );
                    hashes = new String[product.size()][2];
                    int i = 0;
                    for (List<String> s : product) {
                        hashes[i][0] = s.get(0);
                        hashes[i][1] = s.get(1);
                        i++;
                    }

                }

                request.addParameter(ProbeParameters.HASHES.name(), hashes);
                this.getDelegate().handle(request);
            }
        });

        handlers.add(new RequestHandler() {
            // assume smallest digest size of 64 bits
            private Pattern pattern = Pattern.compile("(\\p{XDigit}){16,}", Pattern.CASE_INSENSITIVE);

            @Override
            public void handle(Request request) {
                final String optionShort = "c";
                if (request.hasOption(optionShort)) {
                    String[] checkers = request.getOptionValues(optionShort);

                    Set<String> checkAgainst = new HashSet<>();
                    BufferedReader reader = null;
                    String line;
                    Matcher matcher;
                    try {
                        for (String check : checkers) {
                            if (this.pattern.matcher(check).matches()) {
                                // it's a hash
                                checkAgainst.add(check.toLowerCase());
                            } else {
                                // it could be a file
                                reader = Files.newBufferedReader(
                                        Paths.get(check).toRealPath(), StandardCharsets.UTF_8);
                                while ((line = reader.readLine()) != null) {
                                    matcher = this.pattern.matcher(line);
                                    while (matcher.find()) {
                                        // debug: check if this is properly parsing hashes
                                        checkAgainst.add(matcher.group().toLowerCase());
                                    }
                                }
                            }
                        }
                    } catch (IOException except) {
                        throw new IllegalArgumentException(
                                String.format("File not found: %s", except.getMessage()));
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException error) {
                                // Nothing to do here
                            }
                        }
                    }

                    request.addParameter(ProbeParameters.CHECKS.name(), checkAgainst.toArray(new String[0]));
                }

                this.getDelegate().handle(request);
            }
        });

        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionLong = "strict-check";
                if (request.hasOption(optionLong)) {
                    if (!request.hasOption("c")) { // fixme: do this step while parsing console input
                        // cannot use 'strict-check' without 'c'
                        throw new IllegalArgumentException("Invalid Argument: Cannot use 'strict-check' without 'c'");
                    }

                    String[] checks = (String[]) request.getParameter(ProbeParameters.CHECKS.name());
                    String[][] hashes = (String[][]) request.getParameter(ProbeParameters.HASHES.name());
                    if (checks.length < hashes.length) {
                        throw new IllegalArgumentException(String.format(
                                "Cannot check strictly. Hashes [%d] more than Checks [%d]",
                                hashes.length, checks.length
                        ));
                    }

                    // fixme: replace this with bit masking parameter
                    request.addParameter(ProbeParameters.STRICT_CHECK.name(), "");
                }

                this.getDelegate().handle(request);
            }
        });

        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionLong = "omit-hash";
                if (request.hasOption(optionLong)) {
                    if (!request.hasOption("c")) { // fixme: do this step while parsing console input
                        // cannot use 'strict-check' without 'c'
                        throw new IllegalArgumentException("Invalid Argument: Cannot use 'omit-hash' without 'c'");
                    }

                    // fixme: replace this with bit masking parameter
                    request.addParameter(ProbeParameters.OMIT_HASH.name(), "");
                }
            }
        });

        RequestHandler lastHandler = null;
        for (RequestHandler delegate : handlers) { // linking/chaining handlers
            if (lastHandler == null) {
                this.requestHandlerChain = delegate;
                lastHandler = delegate;
            } else {
                lastHandler.setDelegate(delegate);
                lastHandler = delegate;
            }
        }
    }

    CorInputProbe(HashProvider hashProvider) {
        this.hashProvider = hashProvider;
    }

    @Override
    public void handleRequest (Request request) {
        try {
            this.requestHandlerChain.handle(request); // only configures the 'request' instance

            Set<String> checks = null;
            if (request.hasParameter(ProbeParameters.CHECKS.name())) {
                checks = new HashSet<>(Arrays.asList(
                        (String[]) request.getParameter(ProbeParameters.CHECKS.name())
                ));
            }

            String[][] hashes = (String[][]) request.getParameter(ProbeParameters.HASHES.name());
            Path filePath;
            String hashValue;
            Set<Checksum> checksums = new HashSet<>();
            for (String[] hashParam : hashes) {
                filePath = Paths.get(hashParam[0]);
                hashValue = this.hashProvider.computeHash(filePath, hashParam[1]);
                if ((checks != null) && (request.hasParameter(ProbeParameters.STRICT_CHECK.name()) ||
                        checks.contains(hashValue))) {
                    checksums.add(new SimpleChecksum(
                            filePath.getFileName().toString(), hashParam[1].toUpperCase(), hashValue,
                            checks.remove(hashValue))
                    );
                } else {
                    checksums.add(new SimpleChecksum(
                            filePath.getFileName().toString(), hashParam[1].toUpperCase(), hashValue)
                    );
                }
            }

            this.render(checksums, request);
        } catch (IOException except) {
            // possible, despite '-f' request handler, due to TOCTTOU
            // NOTE: this is not a check. Checking is done only in '-f' request handler
            throw new IllegalArgumentException(String.format(
                    "File not found: %s", except.getMessage()));
        } catch (NoSuchAlgorithmException except) {
            // fixme: replace System.err and printStackTrace() calls with logging
            System.err.println("This shouldn't be reached. Else '-a' request handler is not working");
            except.printStackTrace();
            // this shouldn't happen
        }
    }

    private void render(Set<Checksum> checksums, Request request) {
        // todo: implement this
        final byte columnWidth = 80;
        System.out.println();
    }

    @Override
    public String getName() {
        return CorInputProbe.name;
    }
}

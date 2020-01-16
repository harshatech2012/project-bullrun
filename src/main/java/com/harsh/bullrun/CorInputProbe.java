package com.harsh.bullrun;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * A chain of responsibility based implementation for handling command-line inputs pertaining to
 * the checksum module.
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public class CorInputProbe implements InputProbingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CorInputProbe.class);

    /**
     * Name of this strategy
     */
    private static final String name = "COR Linear Strategy";

    private HashProvider hashProvider;
    private RequestHandler requestHandlerChain;

    /**
     * Additional parameters used by this strategy for maintaining processes' state, while
     * handling a request.
     */
    private enum ProbeParameters { FILE_PATHS, ALGORITHMS, HASHES, CHECKS, STRICT_CHECK, OMIT_HASH }

    {   // chain of responsibility for request handling
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
            } else {
                lastHandler.setDelegate(delegate);
            }

            lastHandler = delegate;
        }
    }

    /**
     * Constructor for creating instances of this strategy.
     *
     * @param hashProvider a provider of hash algorithms and their implementations
     */
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

            this.render(new ArrayList<>(checksums), request);
        } catch (IOException except) {
            // possible, despite '-f' request handler, due to TOCTTOU
            // NOTE: this is not a check. Checking is done only in '-f' request handler
            throw new IllegalArgumentException(String.format(
                    "File not found: %s. Possible TOCTTOU based error.", except.getMessage()));
        } catch (NoSuchAlgorithmException except) {
            // this shouldn't happen
            logger.error("The request handler for '-a' not working.", except);
            System.exit(-1);
        }
    }

    /**
     * Displays the calculated checksums in a tabular format. Each of the checksum's corresponds
     * to a unique hash and file combination.
     *
     * @param checksums list of calculated checksums, each corresponding to a unique file and
     *                  hash combination
     * @param request the request instance passed to the {@link this#handleRequest(Request)}
     *               method of <code>this</code> class
     */
    private void render(List<Checksum> checksums, Request request) {
        class Headers {
            private final static String FILE_NAME = "File Name";
            private final static String ALGORITHM = "Algorithm";
            private final static String HASH_VALUE = "Hash Value";
            private final static String CHECK_STATUS = "Check Status";
        }

        int fileLength = Headers.FILE_NAME.length(); // fixme: find a way to simplify this logic
        int algoLength = Headers.ALGORITHM.length();
        int hashLength = Headers.HASH_VALUE.length();
        for (Checksum s : checksums) {
            fileLength = Math.max(fileLength, s.getFileName().length());
            algoLength = Math.max(algoLength, s.getAlgorithm().length());
            hashLength = Math.max(hashLength, s.getHashValue().length());
        }

        final int checkLength = Headers.CHECK_STATUS.length(); // length of the Check Status column
        final int lineLength = 4 + fileLength + 3 + algoLength +
                (request.hasParameter(ProbeParameters.CHECKS.name()) ?
                (3 + checkLength) + (request.hasParameter(ProbeParameters.OMIT_HASH.name()) ? 0 : 3 + hashLength) :
                (3 + hashLength));
        String rowSeparator = new String(new char[lineLength]).replace("\0", "-");
        Checksum checksum;
        for (int i = -1; i < checksums.size(); i++) {
            if (i <= 0) {
                System.out.println(rowSeparator);
            }
            checksum = i == -1 ? null : checksums.get(i);

            System.out.print(String.format("| %" + fileLength + "s | %" + algoLength + "s |",
                    checksum == null ? Headers.FILE_NAME : checksum.getFileName(),
                    checksum == null ? Headers.ALGORITHM : checksum.getAlgorithm().toUpperCase()));
            if (request.hasParameter(ProbeParameters.CHECKS.name())) {
                System.out.print(String.format(" %" + checkLength + "s |",
                        checksum == null ? Headers.CHECK_STATUS : (
                                checksum.isVerified() == null ? "Unchecked" :
                        (checksum.isVerified() ? "Verified" : "Corrupt"))
                ));

                if (!request.hasParameter(ProbeParameters.OMIT_HASH.name())) {
                    System.out.print(String.format(" %" + hashLength + "s |",
                            checksum == null ? Headers.HASH_VALUE : checksum.getHashValue()));
                }
            } else {
                System.out.print(String.format(" %" + hashLength + "s |",
                        checksum == null ? Headers.HASH_VALUE : checksum.getHashValue()));
            }

            System.out.print("\n");
        }
        System.out.println(rowSeparator);
    }

    @Override
    public String getName() {
        return CorInputProbe.name;
    }
}

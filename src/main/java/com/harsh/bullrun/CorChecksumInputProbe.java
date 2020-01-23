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
public class CorChecksumInputProbe implements InputProbingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CorChecksumInputProbe.class);

    /**
     * Name of this strategy
     */
    private static final String name = "COR Linear Strategy";

    private HashProvider hashProvider;
    private RequestHandler requestHandlerChain;

    /**
     * Abstract handler class for declaring handlers according to the chain of responsibility
     * design pattern.
     */
    private static abstract class RequestHandler {
        private RequestHandler delegate;

        /**
         * Handles the specified request, if possible. Optionally, passes on the request to it's
         * delegate.
         *
         * @param request containing the input parameters
         */
        public abstract void handle(Request request);

        /**
         * Sets the delegate for this instance.
         *
         * @param delegate for passing-on/delegating the request to, if and when required.
         */
        public void setDelegate(RequestHandler delegate) {
            this.delegate = delegate;
        }

        /**
         * Returns the delegate assigned to this instance.
         *
         * @return delegate instance assigned to this class, if any. Other wise returns
         * <code>null</code>.
         */
        public RequestHandler getDelegate() {
            return this.delegate;
        }
    }

    /**
     * Additional parameters used by this strategy for maintaining processes' state, while
     * handling a request.
     */
    private enum ProbeParameters {
        FILE_PATHS, ALGORITHMS, HASHES, CHECKS, STRICT_CHECK, OMIT_HASH
    }

    {   // chain of responsibility for request handling
        List<RequestHandler> handlers = new ArrayList<>();
        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionShort = "a";
                String[] algorithms = (String[]) request.getParameter(optionShort);
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
                String[] filePaths = (String[]) request.getParameter(optionShort);
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
                String[] filePaths = (String[]) request.getParameter(
                        ProbeParameters.FILE_PATHS.name());
                String[] algorithms = (String[]) request.getParameter(
                        ProbeParameters.ALGORITHMS.name());
                String[][] hashes;

                if (request.hasParameter(optionLong)) {
                    if (filePaths.length != algorithms.length) {
                        // 'one-to-one' requires algorithms and files have equal count
                        throw new IllegalArgumentException(
                                String.format("Cannot establish one-to-one mapping between file" +
                                        "(s) and algorithm(s). File [%d] and algorithm [%s] " +
                                        "counts don't match.",
                                        filePaths.length,
                                        algorithms.length));
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
            private Pattern pattern = Pattern.compile(
                    "(\\p{XDigit}){16,}", Pattern.CASE_INSENSITIVE);

            @Override
            public void handle(Request request) {
                final String optionShort = "c";
                if (request.hasParameter(optionShort)) {
                    String[] checkers = (String[]) request.getParameter(optionShort);

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

                    request.addParameter(ProbeParameters.CHECKS.name(),
                            checkAgainst.toArray(new String[0]));
                }

                this.getDelegate().handle(request);
            }
        });

        handlers.add(new RequestHandler() {
            @Override
            public void handle(Request request) {
                final String optionLong = "strict-check";
                if (request.hasParameter(optionLong)) {
                    String[] checks = (String[]) request.getParameter(
                            ProbeParameters.CHECKS.name());
                    String[][] hashes = (String[][]) request.getParameter(
                            ProbeParameters.HASHES.name());
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
                if (request.hasParameter(optionLong)) {
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
    CorChecksumInputProbe(HashProvider hashProvider) {
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
                            filePath.getFileName().toString(),
                            hashParam[1].toUpperCase(), hashValue,
                            checks.remove(hashValue)
                    ));
                } else {
                    checksums.add(new SimpleChecksum(
                            filePath.getFileName().toString(),
                            hashParam[1].toUpperCase(), hashValue
                    ));
                }
            }

            request.addParameter(this.RESULT, checksums);
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

    @Override
    public String getName() {
        return CorChecksumInputProbe.name;
    }
}

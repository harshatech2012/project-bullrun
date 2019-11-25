package com.harsh.bullrun;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CorInputProbe implements InputProbingStrategy {
    private static final String name = "COR Linear Strategy";

    private HashProvider hashProvider;
    private RequestHandler requestHandlerChain;

    private enum ProbeParameters { FILE_PATHS, ALGORITHMS, HASHES, CHECKSUMS, CHECK_POLICY, DISPLAY_POLICY }
    private enum CheckPolicy { STRICT, NOT_STRICT }
    private enum DisplayPolicy {
        SHOW_CHECK,
        OMIT_HASH // SHOW_CHECK is implicit
    }

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
                String[][] hashes = null;

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
            @Override
            public void handle(Request request) {
                final String optionShort = "c";
                if (request.hasOption(optionShort)) {
                    String[] checkers = request.getOptionValues(optionShort);

                    request.addParameter(ProbeParameters.CHECK_POLICY.name(), CheckPolicy.NOT_STRICT.name());
                    request.addParameter(ProbeParameters.DISPLAY_POLICY.name(), DisplayPolicy.SHOW_CHECK.name());
                    // todo: check whether the specified values are hashes / files
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

                    // todo: verify that number of checksums >= number of hashes

                    request.addParameter(ProbeParameters.CHECK_POLICY.name(), CheckPolicy.STRICT.name());
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

                    request.addParameter(ProbeParameters.DISPLAY_POLICY.name(), DisplayPolicy.OMIT_HASH.name());
                }
            }
        });

        RequestHandler lastHandler = null;
        for (RequestHandler delegate : handlers) { // linking/chaining handlers
            if (lastHandler == null) {
                lastHandler = delegate;
            } else {
                lastHandler.setDelegate(delegate);
                lastHandler = delegate;
            }
        }
        this.requestHandlerChain = handlers.get(0); // first handler in the chain
    }

    CorInputProbe(HashProvider hashProvider) {
        this.hashProvider = hashProvider;
    }

    @Override
    public void handleRequest (Request request) {
        this.requestHandlerChain.handle(request); // only configures the 'request' instance

        // todo: add the logic for handling the request based on configured request object
        String[] files;
        String[] algorithms;

    }

    @Override
    public String getName() {
        return CorInputProbe.name;
    }
}

package com.harsh.bullrun;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <code>ChecksumInterface</code> defines the command-line interface for the "checksum"
 * application module. The module provides the functionality for calculating digests or hashes of
 * file(s) specified by the user.
 *
 * <p>This module only defines the user-interface options but not the strategy used for handling
 * them. See {@link InputProbingStrategy} for more information.</p>
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
class ChecksumInterface implements ConsoleInterface {
    private static final Logger logger = LoggerFactory.getLogger(ChecksumInterface.class);

    /**
     * Strategy for handling command-line options pertaining to this module.
     */
    private InputProbingStrategy requestHandlingStrategy;

    /**
     * Properties/configuration information required by this module.
     */
    private Properties applicationProperties;

    /**
     * Options for handling checksum/digest related command-line inputs. For outputs that are
     * dependent on the input, like calculating hash which depends on the files specified.
     */
    private Options hashInterface;

    /**
     * Options for handling module related command-line inputs. For inputs like
     * "--version" and "--help", the output depends only on the module.
     */
    private Options genericInterface;

    {   // structure of the CLI
        this.hashInterface = new Options();
        this.hashInterface.addOption(
                Option.builder("a").longOpt("algorithms").hasArgs().required()
                        .desc("algorithm(s) for computing hash values").build()
        );
        this.hashInterface.addOption(
                Option.builder("f").longOpt("files").hasArgs().required()
                        .desc("file(s) whose hash is required").build()
        );
        this.hashInterface.addOption(
                Option.builder().longOpt("one-to-one").hasArg(false)
                        .desc("specify for one-to-one mapping of file(s) and algorithm(s). " +
                                "Requires the number of files and algorithms be same. If not " +
                                "specified, a cartesian product of file(s) and algorithm(s) " +
                                "gives number of hash values").build()
        );
        this.hashInterface.addOption(
                Option.builder("c").longOpt("check").hasArgs()
                        .desc("hash values to compare against. Can be either a list of hash " +
                                "value(s) or file(s) with hash value(s) in them").build()
        );
        this.hashInterface.addOption(
                Option.builder().longOpt("omit-hash").hasArg(false)
                        .desc("specify to omit hash values from output. Should only be used " +
                                "with 'check' flag.").build()
        );
        this.hashInterface.addOption(
                Option.builder().longOpt("strict-check").hasArg(false).
                        desc("specify to perform strict verification. Requires more hash values" +
                        " to check against than to compute").build()
        );

        this.genericInterface = new Options();
        this.genericInterface.addOptionGroup(
                new OptionGroup()
                        .addOption(
                                Option.builder("v").longOpt("version").hasArg(false)
                                        .desc("version of this tool").build())
                        .addOption(
                                Option.builder("h").longOpt("help").hasArg(false)
                                        .desc("display help").build())
        );
    }

    /**
     * Constructor for creating instances of this user-interface.
     *
     * @param processingStrategy strategy for handling command-line options defined by this
     *                           user-interface
     * @param appProperties properties or configuration information required by this module
     */
    ChecksumInterface (InputProbingStrategy processingStrategy, Properties appProperties) {
        this.requestHandlingStrategy = processingStrategy;
        this.applicationProperties = appProperties;
    }

    @Override
    @SuppressWarnings(value="unchecked")
    public void processRequest(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(this.genericInterface, args, true);
            if (commandLine.hasOption("help")) {
                // hasArg(false) does not invalidate options, but only arguments
                this.checkInvalidArguments(commandLine.getArgList());
                this.handleHelp();
            } else if (commandLine.hasOption("version")) {
                // hasArg(false) does not invalidate options, but only arguments
                this.checkInvalidArguments(commandLine.getArgList());
                this.handleVersion();
            } else {
                commandLine = parser.parse(this.hashInterface, args, false);

                // enforcing interface semantics
                int fileCount = commandLine.getOptionValues("files").length;
                int algoCount = commandLine.getOptionValues("algorithms").length;
                if (commandLine.hasOption("one-to-one") && (fileCount != algoCount)) {
                    // 'one-to-one' requires algorithms and files have equal count
                    throw new IllegalArgumentException(
                            String.format("Cannot establish one-to-one mapping between file(s) and " +
                                    "algorithm(s). File [%d] and algorithm [%s] counts " +
                                    "don't match.", fileCount, algoCount));
                }

                if (commandLine.hasOption("strict-check") && (!commandLine.hasOption("check"))) {
                    // use of 'strict-check' without 'check' doesn't make sense
                    throw new IllegalArgumentException(
                            "Invalid Argument: Cannot use 'strict-check' without 'check' option");
                }

                if (commandLine.hasOption("omit-hash") && (!commandLine.hasOption("check"))) {
                    // use 'omit-hash' without 'check' doesn't make sense
                    throw new IllegalArgumentException(
                            "Invalid Argument: Cannot use 'omit-hash' without 'check' option");
                }

                // setting-up console request instance
                Option[] options = commandLine.getOptions();
                Map<String, Object> optionValuePairs = new HashMap<>();
                String option;
                for (Option o : options) {
                    option = (o.getOpt() == null) ? o.getLongOpt() : o.getOpt();
                    optionValuePairs.put(option, commandLine.getOptionValues(option));
                }

                optionValuePairs.put(ConsoleRequest.CONSOLE_OPTIONS,
                        optionValuePairs.keySet().toArray(new String[0]));
                optionValuePairs.put(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION, false);
                Request consoleRequest = new ConsoleRequest(optionValuePairs);
                this.requestHandlingStrategy.handleRequest(consoleRequest);

                // @SuppressWarnings(value="unchecked") to suppress class casting
                this.render(new ArrayList<Checksum>(Set.class.cast(
                        consoleRequest.getParameter(InputProbingStrategy.RESULT))),
                        commandLine);
            }
        } catch (ParseException except) {
            logger.error(except.getMessage(), except);
            throw new IllegalArgumentException(except);
        }
    }

    /**
     * Displays the calculated checksums in a tabular format. Each of the checksum's corresponds
     * to a unique hash and file combination.
     *
     * @param checksums list of calculated checksums, each corresponding to a unique file and
     *                  hash combination
     * @param commandLine the commandLine instance containing the parsed user arguments
     */
    private void render(List<Checksum> checksums, CommandLine commandLine) {
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
                (commandLine.hasOption("checks") ?
                        (3 + checkLength) + (
                                commandLine.hasOption("omit-hash") ? 0 : (3 + hashLength)
                        ) : (3 + hashLength));
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
            if (commandLine.hasOption("checks")) {
                System.out.print(String.format(" %" + checkLength + "s |",
                        checksum == null ? Headers.CHECK_STATUS : (
                                checksum.isVerified() == null ? "Unchecked" :
                                        (checksum.isVerified() ? "Verified" : "Corrupt"))
                ));

                if (!commandLine.hasOption("omit-hash")) {
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

    /**
     * Handles "-h" and "--help" command-line options.
     */
    private void handleHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar validate.jar checksum", this.hashInterface, true);
    }

    /**
     * Handles "-v" and "--version" command-line options.
     */
    private void handleVersion() {
        System.out.println(String.format(
                "%s Module Version: %s",
                Launcher.LauncherModules.CHECKSUM.name(),
                this.applicationProperties.getProperty(
                        Launcher.LauncherModules.CHECKSUM.getModulePrefix() + ".version")));
    }

    /**
     *  Checks for invalid command-line options passed with stand-alone command-line options like
     *  --help, --version. Neither the method {@link Option.Builder#hasArg(boolean)} and nor does
     *  an {@link OptionGroup} prevent other options from being chained to stand-alone a option.
     *
     * @param invalidArguments any remaining command-line arguments passed
     */
    private void checkInvalidArguments(List<String> invalidArguments) {
        if (!invalidArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Unknown Option(s) %s", invalidArguments.toString())
            );
        }
    }
}

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

import java.util.List;
import java.util.Properties;

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
public class ChecksumInterface implements ConsoleInterface {
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
    public void processRequest(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(this.genericInterface, args, true);
            if (commandLine.hasOption("h")) {
                // hasArg(false) does not invalidate options, but only arguments
                this.checkInvalidArguments(commandLine.getArgList());
                this.handleHelp();
            } else if (commandLine.hasOption("v")) {
                // hasArg(false) does not invalidate options, but only arguments
                this.checkInvalidArguments(commandLine.getArgList());
                this.handleVersion();
            } else {
                commandLine = parser.parse(this.hashInterface, args, false);
                Request request = new ConsoleRequest(commandLine);
                this.requestHandlingStrategy.handleRequest(request);
            }
        } catch (ParseException | IllegalArgumentException except) {
            logger.error(except.getMessage(), except);
            System.exit(-1); // exit immediately, not return
        }
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

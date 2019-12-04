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

public class ChecksumInterface implements ConsoleInterface {
    private static final Logger logger = LoggerFactory.getLogger(ChecksumInterface.class);

    private InputProbingStrategy requestHandlingStrategy;
    private Properties applicationProperties;
    private Options hashInterface;
    private Options genericInterface;

    {
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
                        .desc("specify for one-to-one mapping of file(s) and algorithm(s). Requires the number of " +
                                "files and algorithms be same. If not specified, a cartesian product of file(s) and " +
                                "algorithm(s) gives number of hash values").build()
        );
        this.hashInterface.addOption(
                Option.builder("c").longOpt("check").hasArgs()
                        .desc("hash values to compare against. Can be either a list of hash value(s) or " +
                                "file(s) with hash value(s) in them").build()
        );
        this.hashInterface.addOption(
                Option.builder().longOpt("omit-hash").hasArg(false)
                        .desc("specify to omit hash values from output. Should only be used with 'check' flag.")
                        .build()
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
                this.checkInvalidArguments(commandLine.getArgList());
                this.handleHelp();
            } else if (commandLine.hasOption("v")) {
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

    private void handleHelp() {
        // TODO: figure out how to print help for -h and -v tags
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("java -jar validate.jar checksum", this.hashInterface, true);
    }

    private void handleVersion() {
        System.out.println(String.format("Application Version: %s",
                this.applicationProperties.getProperty("app.version")));
    }

    private void checkInvalidArguments(List<String> invalidArguments) {
        if (!invalidArguments.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Unknown Option(s) %s", invalidArguments.toString())
            );
        }
    }
}

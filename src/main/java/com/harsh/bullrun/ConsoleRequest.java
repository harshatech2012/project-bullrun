package com.harsh.bullrun;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConsoleRequest extends Request {
    private CommandLine commandLine;

    public ConsoleRequest(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    @Override
    public Set<String> getOptions() {
        Option[] options = this.commandLine.getOptions();

        Set<String> opts = new HashSet<>();
        for (Option o : options) {
            opts.add((o.getOpt() == null) ? o.getLongOpt() : o.getOpt());
        }

        return opts;
    }

    @Override
    public String getOptionValue(String option) {
        return this.commandLine.getOptionValue(option);
    }

    @Override
    public String[] getOptionValues(String option) {
        return this.commandLine.getOptionValues(option);
    }

    @Override
    public Properties getOptionProperties(String option) {
        return this.commandLine.getOptionProperties(option);
    }

    @Override
    public boolean hasOption(String option) {
        return this.commandLine.hasOption(option);
    }
}

package com.harsh.bullrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Launcher is responsible for launching the application module corresponding to the
 * command-line input. Also contains the main method for this application.
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final String PROPERTIES_FILE = "application.properties";

    enum LauncherModules {
        CHECKSUM ("app.module.checksum"),
        GPG ("app.module.gpg"),
        GUI ("app.module.gui");

        private String modulePrefix;

        private LauncherModules(String modulePrefix) {
            this.modulePrefix = modulePrefix;
        }

        String getModulePrefix() {
            return this.modulePrefix;
        }
    }

    private Properties appProperties;

    private Launcher(Properties appProperties) {
        this.appProperties = appProperties;
    }
                            
    // todo: mention might throw IllegalArgumentExceptions
    private void launchModule(LauncherModules module, String[] arguments) {
        switch (module) {
            case CHECKSUM:
                ConsoleInterface ui = new ChecksumInterface(
                        new CorInputProbe(new HashProvider()),
                        this.appProperties
                );
                ui.processRequest(arguments);
                break;
            case GPG:
                logger.warn("GPG console invoked. Not implemented yet.");
                // todo: implement gpg based verification
                break;
            case GUI:
                logger.warn("GUI invoked. Not implemented yet.");
                // todo: implement gui
                break;
            default:
                logger.error("Unmatched interface module detected - {}", module.name());
                System.exit(-1); // exit immediately, not return
        }
    }

    private void handleHelp() {
        System.out.println("Select a module to view help:");
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Supported Modules:");
            LauncherModules[] modules = LauncherModules.values();
            for (LauncherModules mod : modules) {
                System.out.println(String.format(
                        "\t%d - %s", mod.ordinal() + 1, mod.name().toLowerCase()));
            }
            System.out.print("Enter the module's name to view help: ");
            String input = reader.readLine();

            this.launchModule(LauncherModules.valueOf(input.toUpperCase()), new String[]{"-h"});
        } catch (IOException except) {
            logger.error("Unable to read console input. Stopping Execution.", except);
        } catch (IllegalArgumentException except) {
            logger.error(except.getMessage());
        }
    }

    private void handleVersion() {
        System.out.println("Application Version: " + this.appProperties.getProperty("app.version"));
        System.out.println("With the following modules:");
        for (LauncherModules module : LauncherModules.values()) {
            System.out.println(String.format(
                    "\t- %s Version: %s",
                    module.name(),
                    this.appProperties.getProperty(module.getModulePrefix() + ".version")));
        }
        System.out.println("\n\n");
    }

    /**
     * Main entry point for launching application and its various modules.
     *
     * @param args command-line arguments
     */
    private void launchApplication(String[] args) {
        if (args.length == 0) {
            this.handleHelp();
        } else if (Pattern.matches("((-h)|(--help))", args[0].toLowerCase())){
            if (args.length != 1) {
                System.out.println("Invalid Usage: -h or --help are standalone command-line " +
                        "options and don't take any arguments.");
            }
            this.handleHelp();
        } else if (Pattern.matches("((-v)|(--version))", args[0].toLowerCase())) {
            if (args.length != 1) {
                System.out.println("Invalid Usage: -v or --version are standalone command-line " +
                        "options and don't take any arguments.");
            }
            this.handleVersion();
        } else {
            try {
                String moduleArgument = args[0].toUpperCase();
                LauncherModules module;
                for (LauncherModules m: LauncherModules.values()) {
                    if (moduleArgument.equals(m.name())) {
                        module = LauncherModules.valueOf(moduleArgument);
                        String[] arguments;
                        if (args.length == 1) {
                            arguments = new String[] {"-h"};
                        } else {
                            arguments = new String[args.length - 1];
                            System.arraycopy(args, 1, arguments, 0, args.length - 1);
                        }

                        this.launchModule(module, arguments);
                        return; // execute this code once
                    }
                }

                // reached if module name is invalid
                System.out.println(String.format("Invalid Module Name: %s", args[0]));
                this.handleHelp();
            } catch (IllegalArgumentException except) {
                 logger.error(except.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            Properties appProperties = new Properties();
            InputStream inputStream = Launcher.class.getClassLoader()
                    .getResourceAsStream(Launcher.PROPERTIES_FILE);
            if (inputStream == null) {
                throw new FileNotFoundException(String.format(
                        "Unable to find %s file. View the application log for more information.",
                        Launcher.PROPERTIES_FILE));
            }
            appProperties.load(new InputStreamReader(inputStream));

            Launcher launcher = new Launcher(appProperties);
            launcher.launchApplication(args);
        } catch (IOException except) {
            logger.error(except.getMessage());
            System.out.println(except.getMessage());
        }
    }
}

// todo: register a shutdown hook and dump inputs (for later debugging and simulation)

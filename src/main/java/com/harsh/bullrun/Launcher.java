package com.harsh.bullrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Launcher {
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final String PROPERTIES_FILE = "application.properties";

    public enum LauncherModules { CHECKSUM, GPG, GUI }

    // todo: mention might throw IllegalArgumentExceptions
    private void launch(LauncherModules module, String[] arguments, Properties appProperties) {
        switch (module) {
            case CHECKSUM:
                ConsoleInterface ui = new ChecksumInterface(
                        new CorInputProbe(new HashProvider()),
                        appProperties
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

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        LauncherModules module;
        String[] arguments;
        try {
            Properties appProperties = new Properties();
            InputStream inputStream = Launcher.class.getClassLoader()
                    .getResourceAsStream(Launcher.PROPERTIES_FILE);
            if (inputStream == null) {
                logger.error("Unable to find {} file. Stopping execution.", Launcher.PROPERTIES_FILE);
                return; // only because it's main()
            }
            appProperties.load(new InputStreamReader(inputStream));

            if (args.length == 0) {
                System.out.println("Select a module to view help:");
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(System.in))) {
                    System.out.println("Supported Modules:");
                    LauncherModules[] modules = LauncherModules.values();
                    for (LauncherModules mod : modules) {
                        System.out.println(String.format("\t%d - %s", mod.ordinal() + 1, mod.name().toLowerCase()));
                    }
                    System.out.print("Select a module to view help: ");
                    String input = reader.readLine();

                    module = LauncherModules.valueOf(input.toUpperCase());
                    arguments = new String[] {"-h"};
                } catch (IOException except) {
                    logger.error("Unable to read console input. Stopping Execution.", except);
                    return; // only because it's main()
                }
            } else {
                module = LauncherModules.valueOf(args[0].toUpperCase());
                if (args.length == 1) {
                    arguments = new String[] {"-h"};
                } else {
                    arguments = new String[args.length - 1];
                    System.arraycopy(args, 1, arguments, 0, args.length - 1);
                }
            }

            launcher.launch(module, arguments, appProperties);
        } catch (IllegalArgumentException | IOException except) {
            logger.error(except.getMessage());
        }
    }
}

// todo: register a shutdown hook and dump inputs (for later debugging and simulation)

package com.harsh.bullrun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class Launcher {
    public enum LauncherModules { CHECKSUM, GPG, GUI }

    private void launch(LauncherModules module, String[] arguments) {
        // todo: handle IllegalArgumentExceptions here
        switch (module) {
            case CHECKSUM:
                ConsoleInterface ui = new ChecksumInterface(
                        new CorInputProbe(new HashProvider()),
                        new Properties()
                );
                ui.processRequest(arguments);
                break;
            case GPG:
                System.out.println("Oh! Still under development.");
                // todo: implement gpg based verification
                break;
            case GUI:
                System.out.println("Oh! Still under development.");
                // todo: implement gui
                break;
            default:
                // This can never happen
                break;
        }
    }

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        LauncherModules module = null;
        String[] arguments = null;
        try {
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
                    except.printStackTrace();
                    return;
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

            launcher.launch(module, arguments);
        } catch (IllegalArgumentException except) {
            // todo: replace with more user-friendly message
            except.printStackTrace();
        }
    }
}

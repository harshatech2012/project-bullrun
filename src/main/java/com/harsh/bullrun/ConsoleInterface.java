package com.harsh.bullrun;

/**
 * This interface is for defining console (same as command-line) based user-interface for
 * an application module. Different modules might have different command-line options and
 * strategies for handling them, but they should all confirm to this interface.
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public interface ConsoleInterface {

    /**
     * Processes the request corresponding to the command-line arguments. Of all the
     * command-line arguments passed by the user, only the module relate arguments should be
     * passed to this method.
     *
     * @param args command-line arguments pertaining to this module
     */
    public void processRequest(String[] args);
}

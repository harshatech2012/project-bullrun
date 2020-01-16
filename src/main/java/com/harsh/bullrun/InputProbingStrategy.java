package com.harsh.bullrun;

/**
 * Input probing strategy defines a methodology for handling user inputs.
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public interface InputProbingStrategy {

    /**
     * Handles the user request encapsulated in a {@link Request} instance, containing the user
     * input. The user request could be based on input from a console-interface or a GUI.
     *
     * @param request contains the user-input and any additional parameters required for
     *                properly handling the request. The additional parameters are dependent
     *                strategy implementation.
     */
    public void handleRequest(Request request);

    /**
     * Returns a name for identifying a strategy. The naming should be such that it can be
     * uniquely identified, among other 3rd party strategies.
     *
     * <p>It's recommended to prefix the strategy name with the group name of the library of
     * which this strategy is a part of.</p>
     *
     * @return unique name for identifying the strategy
     */
    public String getName();
}

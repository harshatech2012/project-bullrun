package com.harsh.bullrun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A context object for encapsulating a console request.
 *
 * <p>This class uses the concept of "reserved-parameter-names" for dealing with accidental
 * deletion or overwriting of data. A reserved-parameter's are those parameters whose values
 * can neither be deleted or be altered (over-written). These reserved-parameter names are
 * passed as a string array (of type <code>String[]</code>) against the
 * {@link ConsoleRequest#CONSOLE_OPTIONS} parameter. And it's mandatory to pass this parameter,
 * regardless of whether reserved names are required for the specific use case. <b>Note:</b> if
 * one doesn't want to declare any reserved names just assign an empty string array against the
 * CONSOLE_OPTIONS parameter.</p>
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public class ConsoleRequest extends Request {
    /**
     * Identifier for this request type. This is used while casting a {@link Request} instance to
     * this type, to gain access to the extended functionality provided by this class.
     */
    public static final String REQUEST_TYPE = ConsoleRequest.class.getName();

    /**
     * The key for retrieving an array of command-line option(s) passed by the user. The value
     * corresponding to this parameter is a String array (of type <code>String[]</code>).
     *
     * <p>The options and their arguments, if any, are stored just like any other key-value pair
     * as parameters of this request instance. So, the only way to distinguish other parameters
     * from option-parameters is to check against this list of options.</p>
     */
    public static final String CONSOLE_OPTIONS = "console_options";
    private final List<String> consoleOptions;

    /**
     * Constructor for creating a console request instance.
     *
     * <p>A map of key-value pairs are passed as an argument to this constructor. These request
     * parameters define the state of the request. The following parameters should confirm to the
     * following rules:
     * <ul>
     *     <li>(<b>Mandatory</b>) The command-line option and its corresponding argument(s) as a
     *     string array (of type <code>String[]</code>), for each of the option(s) passed.</li>
     *
     *     <li>(<b>Mandatory</b>) A list of all command-line option(s) as a String array (of type
     *     <code>String[]</code>) against {@link ConsoleRequest#CONSOLE_OPTIONS} as the parameter
     *     key. This provides the only way of distinguishing command-line options from other
     *     parameters contained by this request instance.<br>An IllegalArgumentException is
     *     thrown if this parameter is not found in the method argument.</li>
     *
     *     <li>(<b>Optional</b>) Any other key-value pairs required for handling the console
     *     request</li>
     * </ul>
     *
     * @param requestParameters key-value pairs required for handling the console request. See
     *                          this method's javadoc for a detailed description.
     * @throws IllegalArgumentException if CONSOLE_OPTIONS parameter is not found in the request
     * parameters argument. See this class's documentation.
     */
    public ConsoleRequest(Map<String, Object> requestParameters) {
        // registering command-line options
        if (!requestParameters.containsKey(ConsoleRequest.CONSOLE_OPTIONS)) {
            throw new IllegalArgumentException(String.format(
                    "Mandatory parameter %s not found.",
                    ConsoleRequest.CONSOLE_OPTIONS));
        }

        this.consoleOptions =
                Arrays.asList((String[]) requestParameters.get(ConsoleRequest.CONSOLE_OPTIONS));

        for (Map.Entry<String, Object> e : requestParameters.entrySet()) {
            // this.addParameter prevents reserved name parameter additions
            super.addParameter(e.getKey(), e.getValue());
        }
    }

    @Override
    public String getRequestType() {
        return ConsoleRequest.REQUEST_TYPE;
    }

    /**
     *
     * @param name of the parameter to be deleted
     * @return value corresponding to the parameter just deleted or <code>null</code> if the
     * parameter is not found.
     * @throws IllegalArgumentException in case the parameter name collides with any of the
     * reserved names. See this class's documentation.
     */
    @Override
    public Object removeParameter(String name) {
        if (this.consoleOptions.contains(name) ||
                name.equalsIgnoreCase(ConsoleRequest.CONSOLE_OPTIONS)) {
            throw new IllegalArgumentException(String.format(
                    "Reserved Parameter '%s' cannot be deleted. See " +
                            "method documentation for more information.",
                    name));
        }

        return super.removeParameter(name);
    }

    /**
     * Adds the parameter-value pair to this request instance. But the parameter name cannot be
     * any of the reserved name(s). Reserved names are the command-line options used by the
     * command-line interface, which generated this request instance. And the value under the
     * {@link ConsoleRequest#CONSOLE_OPTIONS} variable. In case of name collision an
     * IllegalArgumentException is thrown.
     *
     * @param name of the parameter to add
     * @param value corresponding to the parameter
     * @throws IllegalArgumentException in case the parameter name collides with any of the
     * reserved names. See this class's documentation.
     */
    @Override
    public void addParameter(String name, Object value) {
        if (this.consoleOptions.contains(name) ||
                name.equalsIgnoreCase(ConsoleRequest.CONSOLE_OPTIONS)) {
            throw new IllegalArgumentException(String.format(
                    "Reserved Parameter Name '%s': Cannot overwrite parameter value. " +
                            "\nParameter name cannot collide with any of the reserved names. See " +
                            "method documentation for more information.",
                    name));
        }

        super.addParameter(name, value);
    }
}

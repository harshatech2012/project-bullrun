package com.harsh.bullrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A context object for encapsulating a console request.
 *
 * <p>This class considers the command-line options and their corresponding arguments (if any),
 * reserved parameters.These options are passed as a string array with
 * {@link ConsoleRequest#CONSOLE_OPTIONS} as the parameter-name. Also, the suppressReservedException
 * parameter (see {@link Request#setReservedParameters(Set, boolean)} for more information) is
 * passed as a boolean primitive with {@link ConsoleRequest#SUPPRESS_RESERVED_EXCEPTION} as the
 * parameter-name.</p>
 *
 * <p><b>Note:</b>It's mandatory to pass this parameter, regardless of whether reserved names are
 * required for the specific use case. However, if one doesn't want to declare any reserved
 * parameter names just assign an empty string array against the CONSOLE_OPTIONS parameter and
 * set QUITE_ENFORCEMENT to true.</p>
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public class ConsoleRequest extends Request {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleRequest.class);

    /**
     * Identifier for this request type. This is used while casting a {@link Request} instance to
     * this type, to gain access to the extended functionality provided by this class.
     */
    public static final String REQUEST_TYPE = ConsoleRequest.class.getName();

    /**
     * The key for passing the array of command-line option(s) as reserved parameter names.
     *
     * <p>The options and their arguments, if any, are stored just like any other key-value pair
     * as parameters of this request instance. So, the only way to distinguish other parameters
     * from option-parameters is to check against this list of options. One can do so by calling
     * the {@link Request#isReservedParameter(String)} method, see its documentation for more
     * information.</p>
     */
    public static final String CONSOLE_OPTIONS = "console_options";

    /**
     * The key for passing the quiteEnforcement parameter. See this class's documentation.
     */
    public static final String SUPPRESS_RESERVED_EXCEPTION = "suppress_reserved_exception";

    /**
     * Constructor for creating a console request instance.
     *
     * <p>A map of key-value pairs are passed as an argument to this constructor. These request
     * parameters define the state of the request. The parameters should confirm to the
     * following rules:
     * <ul>
     *     <li>(<b>Mandatory</b>) The command-line option and its corresponding argument(s) as a
     *     string array, for each of the options passed.</li>
     *
     *     <li>(<b>Mandatory</b>) A list of all command-line option(s) as a String array with
     *     {@link ConsoleRequest#CONSOLE_OPTIONS} as the parameter name. This provides the only
     *     way to distinguish command-line options from other parameter names contained by this
     *     request instance.</li>
     *
     *     <li>(<b>Mandatory</b>) The quiteEnforcement parameter passed as a boolean primitive with
     *     {@link ConsoleRequest#SUPPRESS_RESERVED_EXCEPTION} as the parameter name. See this
     *     class's documentation for more information.</li>
     *
     *     <li>(<b>Optional</b>) Any other key-value pairs required for handling the console
     *     request</li>
     * </ul>
     *
     * <p><b>Note: </b>An IllegalArgumentException is thrown if the mandatory parameters are not
     * found in the requestParameters method argument.</P
     *
     * @param requestParameters key-value pairs required for handling the console request. See
     *                          this method's javadoc for a detailed description.
     * @throws IllegalArgumentException if CONSOLE_OPTIONS or QUITE_ENFORCEMENT or if all the
     * option-argument pairs are not passed parameter are not found in the requestParameters
     * method argument.
     */
    public ConsoleRequest(Map<String, Object> requestParameters) {
        // registering command-line options
        if (!requestParameters.containsKey(ConsoleRequest.CONSOLE_OPTIONS)) {
            throw new IllegalArgumentException(String.format(
                    "Mandatory parameter %s not found.",
                    ConsoleRequest.CONSOLE_OPTIONS));
        }

        if (!requestParameters.containsKey(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION)) {
            throw new IllegalArgumentException(String.format(
                    "Mandatory parameter %s not found.",
                    ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION));
        }

        Set<String> consoleOptions = new HashSet<>(Arrays.asList((String[])
                requestParameters.get(ConsoleRequest.CONSOLE_OPTIONS)));
        super.setReservedParameters(consoleOptions,
                (boolean) requestParameters.get(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION));

        if (consoleOptions.removeAll(requestParameters.keySet()) &&
                (!consoleOptions.isEmpty())) {
            throw new IllegalArgumentException(String.format(
                    "Missing parameters for the following console options: %s",
                    consoleOptions.toString()));
        }

        for (Map.Entry<String, Object> e : requestParameters.entrySet()) {
            if (!this.addParameter(e.getKey(), e.getValue())) {
                logger.error("Unable to add key-value pair: {} - {}",
                        e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public String getRequestType() {
        return ConsoleRequest.REQUEST_TYPE;
    }

    @Override
    void setReservedParameters(Set<String> reservedParameterNames,
                               boolean suppressReservedException) {
        throw new UnsupportedOperationException(
                "Invalid Method Call: this method should only be called from the constructor " +
                        "during instance creation.");
    }
}

package com.harsh.bullrun;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A POJO for encapsulating the request parameters, based on the Context-Object design pattern.
 *
 * <p>This class also leaves room for further extending the functionality of the request
 * instance, through the {@link ConsoleRequest#getRequestType()} method. The fully qualified
 * classname to explicitly typecast the {@link Request} instance to it's actual reference
 * type, exposing the additional functionality. The explicit type cast can be performed using
 * the following snippet:</p>
 * <code>
 *     Class.forName(<request-instance>.getType()).cast(<request-instance>);
 * </code>
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public abstract class Request {
    private Map<String, Object> parameters = new HashMap<>();

    /**
     * Returns the fully qualified classname of this request instance.
     *
     * <p>Each subclass of this class, must return it's fully qualified class name for type
     * identification. This type can then be used for casting the request instance to the
     * corresponding sub-class type. This is useful for exposing additional functionality
     * provided by that sub-class.</p>
     *
     * @return fully qualified classname of this request instance
     */
    public abstract String getRequestType();

    /**
     * Adds the following parameter name and its corresponding values. If the parameter name
     * already exists then the existing value is replaced with the new value. Hence, to prevent
     * loss of existing data, it's recommended to check if the parameter name already exists
     * using {@link this#hasParameter(String)} and take appropriate action.
     *
     * @param name of the parameter to add
     * @param value corresponding to the parameter
     */
    public void addParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    /**
     * Deletes the parameter with the specified name and returns its value. If the
     * parameter with the specified name doesn't exist then null is returned.
     *
     * @param name of the parameter to be deleted
     * @return value corresponding to the parameter just deleted or <code>null</code> if the
     * parameter is not found.
     */
    public Object removeParameter(String name) {
        return this.parameters.remove(name);
    }

    /**
     * Returns the value of the specified parameter.
     *
     * @param name of the parameter whose values is needed
     * @return value of the specified parameter. If the parameter is not found <code>null</code>
     * is returned. It's recommended to check for the existence of a parameter using
     * {@link this#hasParameter(String)} to avoid NullPointerException with the caller.
     */
    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    /**
     * Checks whether the specified parameter name is present.
     *
     * @param name of the parameter to check for. This check is case-sensitive.
     * @return true if the parameter is present
     */
    public boolean hasParameter(String name) {
        return this.parameters.containsKey(name);
    }

    /**
     * Returns a list of parameter names carried by this instance. Any modifications to the
     * returned list should not effect the underlying list of parameters.
     *
     * @return a non-backing copy of parameter names carried by this instance
     */
    public Set<String> listParameterNames() {
        return ImmutableSet.copyOf(this.parameters.keySet());
    }
}

package com.harsh.bullrun;

import java.util.HashMap;
import java.util.HashSet;
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
 * <p>This class supports special class of parameters called the "reserved-parameters".
 * A set of parameters that hold special immutable-and-protected status amongst other parameters
 * in this request instance.For these parameters, once their value is set, it can neither be
 * deleted or edited. Hence, these parameters should be set during instance creation (passed as a
 * constructor argument). It is advised that all subclasses extending this class should throw
 * override {@link this#setReservedParameters(Set, boolean)} and throw an
 * {@link UnsupportedOperationException}. This is to ensure that reserved parameters are only set
 * through the constructor during instance creation.</p>
 *
 * @author Harsha Vardhan
 * @since v1.0.0
 */
public abstract class Request {
    private Map<String, Object> parameterValueMap = new HashMap<>();
    private Set<String> reservedParameters = new HashSet<>();
    private boolean suppressReservedException;

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
     * Sets the reserved parameters names. These parameters should be set during instance
     * creation (passed as a constructor argument). It is advised that all subclasses extending
     * this class should throw override this method and throw an
     * {@link UnsupportedOperationException}. This is to ensure that reserved parameters are only set
     * through the constructor during instance creation.
     *
     * @param reservedParameterNames set of reserved parameter names
     * @param suppressReservedException if true this instance won't throw an
     *                         UnsupportedOperationException on trying to add/delete an existing
     *                         reserved parameter
     */
    void setReservedParameters(Set<String> reservedParameterNames, boolean suppressReservedException) {
        // reservedParameterNames is a reference type, can be changed after assignment
        this.reservedParameters = new HashSet<>(reservedParameterNames);
        this.suppressReservedException = suppressReservedException;
    }

    /**
     * Returns a non-backing set of reserved parameters.
     *
     * <p>Here non-backing means that any changes to the returned list won't effect the this
     * instance, in any way.</p>
     *
     * @return non-backing set of reserved parameters.
     */
    public Set<String> getReservedParameters() {
        return new HashSet<>(this.reservedParameters);
    }

    /**
     * Checks whether the given parameter name is reserved.
     *
     * @param name of the parameter to check for
     * @return true if the name is a reserved parameter, false otherwise
     */
    public boolean isReservedParameter(String name) {
        return this.reservedParameters.contains(name);
    }

    /**
     * Adds the following parameter name and its corresponding values. If the parameter name
     * already exists then the existing value is replaced with the new value. Hence, to prevent
     * loss of existing data, it's recommended to check if the parameter name already exists
     * using {@link this#hasParameter(String)} and take appropriate action.
     *
     * @param name of the parameter to add
     * @param value corresponding to the parameter
     * @return true if the parameter has been added, false otherwise
     * @throws UnsupportedOperationException if the parameter name matches a reserved parameter
     * and its value has already been set. Use hasParameter(String) and isReservedParameter
     * (String) to prevent this exception. This exception can be silenced by setting the
     * quiteEnforcement parameter to true, see {@link this#setReservedParameters(Set, boolean)}
     * for more information.
     */
    public boolean addParameter(String name, Object value) {
        if (this.isReservedParameter(name) && this.hasParameter(name)) {
            if (this.suppressReservedException) {
                return false;
            } else {
                throw new UnsupportedOperationException(String.format(
                        "Reserved Parameter %s: Cannot edit its value.", name));
            }
        }

        this.parameterValueMap.put(name, value);
        return true;
    }

    /**
     * Deletes the parameter with the specified name, and returns operation status. If the
     * parameter exists and is successfully deleted then returns true, and false in the following
     * cases:
     * <ul>
     *     <li>The parameter with the name doesn't exist</li>
     *     <li>The parameter with the name exists but is one of the reserved parameters, in
     *     which case it won't be deleted</li>
     * </ul>
     *
     * <b>Caution:</b> the parameter names are identified using {@link String#equals(Object)}
     * method.
     *
     * @param name of the parameter to be deleted
     * @return true if the parameter exists and has been removed, false otherwise
     * @throws UnsupportedOperationException if the parameter exists and its name matches a
     * reserved parameter. Use hasParameter(String) and isReservedParameter
     * (String) to prevent this exception. This exception can be silenced by setting the
     * quiteEnforcement parameter to true, see {@link this#setReservedParameters(Set, boolean)}
     * for more information.
     */
    public boolean removeParameter(String name) {
        if (this.hasParameter(name)) {
            if (this.isReservedParameter(name)) {
                if (this.suppressReservedException) {
                    return false;
                } else {
                    throw new UnsupportedOperationException(String.format(
                            "Reserved Parameter %s: Cannot delete it.", name));
                }
            } else {
                this.parameterValueMap.remove(name);
                return true;
            }
        } else {
            return false;
        }
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
        return this.parameterValueMap.get(name);
    }

    /**
     * Checks whether the specified parameter name is present.
     *
     * @param name of the parameter to check for. This check is case-sensitive.
     * @return true if the parameter is present
     */
    public boolean hasParameter(String name) {
        return this.parameterValueMap.containsKey(name);
    }

    /**
     * Returns a list of parameter names carried by this instance. Any modifications to the
     * returned list should not effect the underlying list of parameters.
     *
     * @return a non-backing copy of parameter names carried by this instance
     */
    public Set<String> listParameterNames() {
        return new HashSet<>(this.parameterValueMap.keySet());
    }
}

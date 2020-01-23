package com.harsh.bullrun;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Test: Request implementation using an anonymous class")
public class RequestTest {
    private Request request;

    @BeforeEach
    void setup() {
        request = new Request() {
            @Override
            public String getRequestType() {
                return this.getClass().getName();
            }
        };
    }

    @Test
    void Reserved_parameter_argument_should_be_cloned_not_referenced() {
        Set<String> reservedParameterArgument = new HashSet<>(
                Arrays.asList("whisky", "tango", "foxtrot", "roger", "zulu", "x-ray"));
        request.setReservedParameters(reservedParameterArgument, false);

        reservedParameterArgument.removeAll(Arrays.asList("whisky", "roger", "x-ray"));
        Assertions.assertTrue(request.isReservedParameter("whisky"));
        Assertions.assertTrue(request.isReservedParameter("roger"));
        Assertions.assertTrue(request.isReservedParameter("x-ray"));
    }

    @Test
    void Adding_and_removing_reserved_parameters_with_reserved_exception_suppression() {
        Set<String> reservedParameters = new HashSet<>(
                Arrays.asList("whisky", "tango", "foxtrot", "roger", "zulu", "x-ray"));
        request.setReservedParameters(reservedParameters, true);

        // adding parameters
        Assertions.assertTrue(request.addParameter("whisky", "beta") &&
                request.hasParameter("whisky"));
        Assertions.assertTrue(request.addParameter("foxtrot", "charlie") &&
                request.hasParameter("foxtrot"));

        // editing parameter
        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertFalse(request.addParameter("whisky", "alpha"));
        });
        Assertions.assertEquals(request.getParameter("whisky"), "beta");

        // removing parameters
        Assertions.assertDoesNotThrow(() -> {
            Assertions.assertFalse(request.removeParameter("foxtrot"));
        });
        Assertions.assertTrue(request.hasParameter("foxtrot")); // existing parameter
        Assertions.assertFalse(request.removeParameter("x-ray")); // non-existing parameter
    }

    @Test
    void Adding_and_removing_reserved_parameters_without_reserved_exception_suppression() {
        Set<String> reservedParameters = new HashSet<>(
                Arrays.asList("whisky", "tango", "foxtrot", "roger", "zulu", "x-ray"));
        request.setReservedParameters(reservedParameters, false);

        // adding parameters
        Assertions.assertTrue(request.addParameter("whisky", "alpha") &&
                request.hasParameter("whisky"));
        Assertions.assertTrue(request.addParameter("tango", "charlie") &&
                request.hasParameter("tango"));

        // editing parameter
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            request.addParameter("whisky","beta");
        });
        Assertions.assertEquals(request.getParameter("whisky"), "alpha");

        // removing parameter
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            request.removeParameter("tango"); // existing parameter
        });
        Assertions.assertTrue(request.hasParameter("tango"));
        Assertions.assertDoesNotThrow(() -> {
            // non-existing parameter
            Assertions.assertFalse(request.removeParameter("zulu"));
        });
    }

    @Test
    void Get_non_backing_set_of_parameters_on_calling_getReservedParameters() {
        Set<String> reservedParameters = new HashSet<>(
                Arrays.asList("whisky", "tango", "foxtrot", "roger", "zulu", "x-ray"));
        request.setReservedParameters(reservedParameters, false);

        Set<String> nonBackingParams = request.getReservedParameters();
        nonBackingParams.removeAll(Arrays.asList("whisky", "roger", "x-ray"));
        nonBackingParams.addAll(Arrays.asList("charlie", "alpha"));
        Assertions.assertTrue(request.isReservedParameter("whisky"));
        Assertions.assertTrue(request.isReservedParameter("roger"));
        Assertions.assertTrue(request.isReservedParameter("x-ray"));
        Assertions.assertFalse(request.isReservedParameter("charlie"));
        Assertions.assertFalse(request.isReservedParameter("alpha"));
    }

    @Test
    void Adding_and_removing_parameters_should_be_null_agnostic() {
        Set<String> reservedParameterArgument = new HashSet<>(
                Arrays.asList("whisky", "tango", "foxtrot", "roger", "zulu", "x-ray"));
        request.setReservedParameters(reservedParameterArgument, false);

        // adding null key and non-null value
        Assertions.assertTrue(request.addParameter(null, "mike") &&
                request.hasParameter(null));
        Assertions.assertEquals(request.getParameter(null), "mike");

        // adding non-null key and null value
        Assertions.assertTrue(request.addParameter("oscar", null)); // un-reserved
        Assertions.assertNull(request.getParameter("oscar"));
        Assertions.assertTrue(request.hasParameter("oscar"));
        Assertions.assertTrue(request.addParameter("tango", null)); // reserved
        Assertions.assertNull(request.getParameter("tango"));
        Assertions.assertTrue(request.hasParameter("tango"));

        // adding null key and null value
        Assertions.assertTrue(request.addParameter(null, null));
        Assertions.assertNull(request.getParameter(null));
        Assertions.assertTrue(request.hasParameter(null));
    }

    @Test
    void Get_non_backing_set_of_parameters_on_calling_listParameterNames() {
        request.addParameter("charlie", "dog");
        request.addParameter("alpha", "beta");
        request.addParameter("essex", "foxtrot");
        Set<String> nonBackingParams = request.listParameterNames();
        nonBackingParams.addAll(Arrays.asList("zulu", "x-ray"));
        nonBackingParams.removeAll(Arrays.asList("charlie", "alpha"));

        Assertions.assertTrue(request.hasParameter("charlie"));
        Assertions.assertTrue(request.hasParameter("alpha"));
        Assertions.assertFalse(request.hasParameter("zulu"));
        Assertions.assertFalse(request.hasParameter("x-ray"));
    }
}

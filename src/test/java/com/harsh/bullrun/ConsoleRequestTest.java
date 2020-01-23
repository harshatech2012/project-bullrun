package com.harsh.bullrun;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@DisplayName("Test: Console Request implementation")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ConsoleRequestTest {
    private Map<String, Object> requestParameters;

    @BeforeEach
    public void setupEach() {
        requestParameters = new HashMap<>();
        requestParameters.put("algorithms", new String[] {"sha-256"});
        requestParameters.put("files", new String[]{"doesnt_matter.txt"});
    }

    @Test
    public void No_CONSOLE_OPTIONS_parameter_should_throw_IllegalArgumentException() {
        requestParameters.put(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION, false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ConsoleRequest(requestParameters);
        });
    }

    @Test
    public void No_SUPPRESS_RESERVED_EXCEPTION_parameter_should_throw_IllegalArgumentException() {
        requestParameters.put(ConsoleRequest.CONSOLE_OPTIONS,
                new String[] {"algorithms", "files"});
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ConsoleRequest(requestParameters);
        });
    }

    @Test
    public void Missing_console_options_in_requestParameters_but_present_in_CONSOLE_OPTIONS() {
        requestParameters.put(ConsoleRequest.CONSOLE_OPTIONS,
                new String[] {"algorithms", "files", "check", "strict-check"});
        requestParameters.put(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION, false);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new ConsoleRequest(requestParameters);
        });
    }

    @Test
    public void Calling_setReservedParameters_should_throw_UnsupportedOperationException() {
        requestParameters.put(ConsoleRequest.CONSOLE_OPTIONS,
                requestParameters.keySet().toArray(new String[0]));
        requestParameters.put(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION, false);
        Request request = new ConsoleRequest(requestParameters);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
           request.setReservedParameters(new HashSet<>(), false);
        });
    }

    @Test
    public void Transfer_of_requestParameters_to_ConsoleRequest_instance() {
        requestParameters.put("one-to-one", null);
        requestParameters.put("strict-check", null);
        requestParameters.put(ConsoleRequest.CONSOLE_OPTIONS,
                requestParameters.keySet().toArray(new String[0]));
        requestParameters.put(ConsoleRequest.SUPPRESS_RESERVED_EXCEPTION, false);
        Request consoleRequest = new ConsoleRequest(requestParameters);

        Assertions.assertTrue(consoleRequest.hasParameter("algorithms"));
        Assertions.assertTrue(consoleRequest.hasParameter("files"));
        Assertions.assertTrue(consoleRequest.hasParameter("one-to-one"));
        Assertions.assertTrue(consoleRequest.hasParameter("strict-check"));

        requestParameters.remove("one-to-one");
        requestParameters.remove("strict-check");
        Assertions.assertTrue(consoleRequest.hasParameter("one-to-one"));
        Assertions.assertTrue(consoleRequest.hasParameter("strict-check"));
    }
}

package com.harsh.bullrun;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class Request {
    private Map<String, Object> parameters = new HashMap<>();

    public abstract Set<String> getOptions();
    public abstract String getOptionValue(String option);
    public abstract String[] getOptionValues(String option);
    public abstract Properties getOptionProperties(String option);
    public abstract boolean hasOption(String option);

    public void addParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    public Object removeParameter(String name) {
        return this.parameters.remove(name);
    }
    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    public boolean hasParameter(String name) {
        return this.parameters.containsKey(name);
    }

    public Set<String> listParameterNames() {
        return ImmutableSet.copyOf(this.parameters.keySet());
    }
}

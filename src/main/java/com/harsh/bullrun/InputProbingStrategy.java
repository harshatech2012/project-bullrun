package com.harsh.bullrun;

public interface InputProbingStrategy {
    public void handleRequest(Request request);
    public String getName();
}

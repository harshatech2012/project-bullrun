package com.harsh.bullrun;

public abstract class RequestHandler {
    private RequestHandler delegate;

    public abstract void handle(Request request);

    public void setDelegate(RequestHandler delegate) {
        this.delegate = delegate;
    }

    public RequestHandler getDelegate() {
        return this.delegate;
    }
}

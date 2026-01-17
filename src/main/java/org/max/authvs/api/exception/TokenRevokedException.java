package org.max.authvs.api.exception;

public class TokenRevokedException extends RuntimeException {
    public TokenRevokedException() {
        super();
    }
    
    public TokenRevokedException(String message) {
        super(message);
    }
}

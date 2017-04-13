package com.projectx.exception;

/**
 * Created by ivan on 13.04.17.
 */
public class DuplicateEntryException extends Exception {
    public DuplicateEntryException(Exception ex) {
        super(ex);
    }
}

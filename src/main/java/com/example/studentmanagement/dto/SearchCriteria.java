package com.example.studentmanagement.dto;

/**
 * Represents a single search condition with key, operation, and value.
 * Supported operations:
 *   : → equality (exact match for non-String, LIKE for String)
 *   > → greater than
 *   < → less than
 *   ! → not equal
 */
public class SearchCriteria {

    private String key;
    private String operation;
    private Object value;

    public SearchCriteria() {
    }

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

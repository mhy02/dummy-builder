package com.akaz.dummybuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PopulateCondition {

    private final Map<String, Object> setFields = new HashMap<>();
    private final List<String> ignoreFieldNames = new ArrayList<>();
    private final List<Class<?>> ignoreAnnotations = new ArrayList<>();

    public Map<String, Object> getSetFields() {
        return this.setFields;
    }

    public List<String> getIgnoreFieldNames() {
        return this.ignoreFieldNames;
    }

    public List<Class<?>> getIgnoreAnnotations() {
        return this.ignoreAnnotations;
    }

    public void addSetField(String fieldName, Object fieldValue) {
        this.setFields.put(fieldName, fieldValue);
    }

    public void addIgnoreFieldName(String fieldName) {
        this.ignoreFieldNames.add(fieldName);
    }

    public void addIgnoreAnnotation(Class<?> clazz) {
        if (clazz.isAnnotation()) {
            this.ignoreAnnotations.add(clazz);
        } else {
            throw new IllegalArgumentException(clazz.getName() + " is not annotation");
        }
    }

}

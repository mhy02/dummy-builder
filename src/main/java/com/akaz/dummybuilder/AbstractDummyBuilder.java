package com.akaz.dummybuilder;

import org.dummycreator.ClassBindings;

import com.akaz.dummybuilder.factories.ClassBasedFactory;

public abstract class AbstractDummyBuilder {

    private Class<?> clazz;
    private Object instance;
    private PopulateCondition populateCondition = new PopulateCondition();

    /**
     * Create populated dummy objects of a given class. This will recursively run through its fields and try to come up with newly
     * populated objects for those fields.
     */
    protected AbstractDummyBuilder(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Populate dummy data into a given instance. This will recursively run through its fields and try to come up with newly
     * populated objects for those fields.
     */
    protected AbstractDummyBuilder(Object instance) {
        this.instance = instance;
    }

    protected abstract ClassBindings getClassBindings();
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T build() {
        Object dummy = null;

        if (this.clazz != null) {
            dummy = new ClassBasedFactory(clazz, populateCondition).createDummy(getClassBindings());
        } else if (this.instance != null) {
            dummy = new ClassBasedFactory(populateCondition).populate(this.instance, getClassBindings());
        } else {
            throw new UnknownError();
        }

        return (T)dummy;
    }

    /**
     * Populate specified field with specified value.
     */
    public AbstractDummyBuilder set(String fieldName, Object fieldValue) {
        populateCondition.addSetField(fieldName, fieldValue);
        return this;
    }

    /**
     * Specifies the filed name to ignore. The specified field will not be populated.
     */
    public AbstractDummyBuilder ignore(String fieldName) {
        populateCondition.addIgnoreFieldName(fieldName);
        return this;
    }

    /**
     * Specifies the annotation to ignore. The annotated field will not be populated.
     */
    public AbstractDummyBuilder ignoreAnnotated(Class<?> annotation) {
        populateCondition.addIgnoreAnnotation(annotation);
        return this;
    }

}

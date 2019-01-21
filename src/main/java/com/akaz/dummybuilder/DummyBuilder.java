package com.akaz.dummybuilder;

import java.util.Date;

import org.dummycreator.ClassBindings;
import org.joda.time.DateTime;

import com.akaz.dummybuilder.factories.DateDummyFactory;
import com.akaz.dummybuilder.factories.DateTimeDummyFactory;

public class DummyBuilder extends AbstractDummyBuilder {

    private DummyBuilder(Object instance) {
        super(instance);
    }

    private DummyBuilder(Class<?> clazz) {
        super(clazz);
    }

    /**
     * Populate dummy data into a given instance. This will recursively run through its fields and try to come up with newly
     * populated objects for those fields.
     */
    public static DummyBuilder fromInstance(Object instance) {
        return new DummyBuilder(instance);
    }

    /**
     * Create populated dummy objects of a given class. This will recursively run through its fields and try to come up with newly
     * populated objects for those fields.
     */
    public static DummyBuilder newInstance(Class<?> clazz) {
        return new DummyBuilder(clazz);
    }

    @Override
    protected ClassBindings getClassBindings() {
        ClassBindings classBindings = ClassBindings.defaultBindings();

        classBindings.add(Date.class, new DateDummyFactory());
        classBindings.add(DateTime.class, new DateTimeDummyFactory());

        return classBindings;
    }

}

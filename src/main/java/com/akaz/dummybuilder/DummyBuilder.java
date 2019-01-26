package com.akaz.dummybuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dummycreator.ClassBindings;
import org.joda.time.DateTime;

import com.akaz.dummybuilder.factories.ClassBasedFactory;
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
    
    /**
     * Populate dummy data into a given builder. This will recursively run through its fields and try to come up with newly
     * populated objects for those fields.
     */
    public static <T> T fromBuilder(T builder) {
        return new DummyBuilder(builder).build();
    }

    @Override
    protected ClassBindings getClassBindings() {
        ClassBindings classBindings = new ClassBindings();

        classBindings.add(List.class, new ClassBasedFactory<>(ArrayList.class));
        classBindings.add(Map.class, new ClassBasedFactory<>(HashMap.class));
        classBindings.add(Set.class, new ClassBasedFactory<>(HashSet.class));
        classBindings.add(Date.class, new DateDummyFactory());
        classBindings.add(DateTime.class, new DateTimeDummyFactory());

        return classBindings;
    }

}

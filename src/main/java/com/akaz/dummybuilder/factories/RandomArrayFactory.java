package com.akaz.dummybuilder.factories;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.dummycreator.ClassBindings;
import org.dummycreator.ClassUsageInfo;
import org.dummycreator.RandomCreator;
import org.dummycreator.dummyfactories.DummyFactory;

public class RandomArrayFactory<T> extends DummyFactory<T> {

    private final Class<T> clazz;

    public RandomArrayFactory(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public T createDummy(final Type[] genericMetaData,
                         final Map<String, ClassUsageInfo<?>> knownInstances,
                         final ClassBindings classBindings,
                         final List<Exception> exceptions) {
        final int length = RandomCreator.getInstance().getRandomInt(2) + 2;
        final Object dummyArray = Array.newInstance(clazz.getComponentType(), length);
        for (int i = 0; i < length; i++) {
            Array.set(dummyArray,
                      i,
                      new ClassBasedFactory(clazz.getComponentType()).createDummy(genericMetaData, knownInstances, classBindings, exceptions));
        }
        return (T)dummyArray;
    }
}
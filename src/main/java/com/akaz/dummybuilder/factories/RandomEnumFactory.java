package com.akaz.dummybuilder.factories;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.dummycreator.ClassBindings;
import org.dummycreator.ClassUsageInfo;
import org.dummycreator.RandomCreator;
import org.dummycreator.dummyfactories.DummyFactory;

public class RandomEnumFactory<T extends Enum<?>> extends DummyFactory<T> {

    private final Class<T> clazz;

    public RandomEnumFactory(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T createDummy(final Type[] genericMetaData,
                         final Map<String, ClassUsageInfo<?>> knownInstances,
                         final ClassBindings classBindings,
                         final List<Exception> exceptions) {
        final T[] enums = clazz.getEnumConstants();

        if (enums.length == 1) {
            return enums[0];
        }

        return enums[RandomCreator.getInstance().getRandomInt(enums.length - 1)];
    }

}
package com.akaz.dummybuilder.factories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.dummycreator.ClassBindings;
import org.dummycreator.ClassUsageInfo;
import org.dummycreator.dummyfactories.DummyFactory;

public class ConstructorBasedFactory<T> extends DummyFactory<T> {

    private final Constructor<T> constructor;

    public ConstructorBasedFactory(final Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public T createDummy(final Type[] genericMetaData,
                         final Map<String, ClassUsageInfo<?>> knownInstances,
                         final ClassBindings classbindings,
                         final List<Exception> exceptions) {
        @SuppressWarnings("unchecked") final Class<T>[] parameters = (Class<T>[])constructor.getParameterTypes();
        try {
            if (parameters.length > 0) {
                final Object[] params = new Object[parameters.length];
                for (int i = 0; i < params.length; i++) {
                    params[i] = new ClassBasedFactory<T>(parameters[i]).createDummy(genericMetaData,
                                                                                    knownInstances,
                                                                                    classbindings,
                                                                                    exceptions);
                }
                return constructor.newInstance(params);
            } else {
                return constructor.newInstance();
            }
        } catch (final InvocationTargetException e) {
            exceptions.add(e);
        } catch (final InstantiationException e) {
            exceptions.add(e);
        } catch (final IllegalAccessException e) {
            exceptions.add(e);
        }
        return null;
    }
}
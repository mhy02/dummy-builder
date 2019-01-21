package com.akaz.dummybuilder.factories;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dummycreator.ClassBindings;
import org.dummycreator.ClassUsageInfo;
import org.dummycreator.RandomCreator;
import org.dummycreator.ReflectionCache;
import org.dummycreator.dummyfactories.DummyFactory;

import com.akaz.dummybuilder.PopulateCondition;

public class ClassBasedFactory<T> extends DummyFactory<T> {

    private static final Logger logger = Logger.getLogger(ClassBasedFactory.class);
    private final Class<T> clazz;
    private static final ReflectionCache constructorCache = new ReflectionCache();
    private final PopulateCondition populateCondition;

    public ClassBasedFactory() {
        this.clazz = null;
        this.populateCondition = null;
    }

    public ClassBasedFactory(final Class<T> clazz) {
        this.clazz = clazz;
        this.populateCondition = null;
    }

    public ClassBasedFactory(PopulateCondition populateCondition) {
        this.clazz = null;
        this.populateCondition = populateCondition;
    }

    public ClassBasedFactory(final Class<T> clazz, PopulateCondition populateCondition) {
        this.clazz = clazz;
        this.populateCondition = populateCondition;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createDummy(final Type[] nextGenericsMetaData, final Map<String, ClassUsageInfo<?>> knownInstances, final ClassBindings classBindings, final List<Exception> exceptions) {
        final String typeMarker = createTypeMarker(clazz, nextGenericsMetaData);
        if (knownInstances.get(typeMarker) == null || !knownInstances.get(typeMarker).isPopulated()) {

            final T ret = create(nextGenericsMetaData, knownInstances, classBindings, exceptions);

            if (ret != null) {
                return ret;
            } else {
                logger.error("tried but failed to produce dummy object...");
                if (!exceptions.isEmpty()) {
                    logger.error("errors logged:");
                    for (final Exception e : exceptions) {
                        logger.error(e.getMessage(), e);
                    }
                }
                throw new IllegalArgumentException(String.format("Could not instantiate object for type [%s], is it abstract and missing a binding?", clazz));
            }
        } else {
            return (T)knownInstances.get(typeMarker).getInstance();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private T create(final Type[] genericMetaData, final Map<String, ClassUsageInfo<?>> knownInstances, final ClassBindings classBindings, final List<Exception> exceptions) {
        T ret = null;

        final DummyFactory<T> factory = classBindings.find(clazz);

        if (factory != null) {
            ret = factory.createDummy(genericMetaData, knownInstances, classBindings, exceptions);
        }

        if (ret == null && clazz.isArray()) {
            ret = new RandomArrayFactory<T>(clazz).createDummy(genericMetaData, knownInstances, classBindings, exceptions);
        }

        if (ret == null) {
            final ClassUsageInfo<T> usedInfo = new ClassUsageInfo<T>();
            usedInfo.setInstance(ret);

            if (!isKnownLoopSafeType(clazz)) {
                knownInstances.put(createTypeMarker(clazz, genericMetaData), usedInfo);
            }

            if (clazz.isEnum()) {
                return (T)new RandomEnumFactory((Class<Enum>)clazz).createDummy(genericMetaData, knownInstances, classBindings, exceptions);
            }

            List<Constructor<?>> cachedConstructors = constructorCache.getConstructorCache(clazz);
            if (cachedConstructors == null) {
                cachedConstructors = new ArrayList<Constructor<?>>();
                final Constructor<?>[] foundConstructors = clazz.getConstructors();
                java.util.Arrays.sort(foundConstructors, new Comparator<Constructor<?>>() {
                    @Override
                    public int compare(final Constructor<?> o1, final Constructor<?> o2) {
                        final int num_o1 = o1.getParameterTypes().length;
                        final int num_o2 = o2.getParameterTypes().length;
                        return num_o1 < num_o2 ? -1 : num_o1 == num_o2 ? 0 : 1;
                    }
                });
                cachedConstructors.addAll(Arrays.asList(foundConstructors));
                constructorCache.add(clazz, cachedConstructors.toArray(new Constructor<?>[] {}));
            }

            final Constructor<T> preferedConstructor = (Constructor<T>)constructorCache.getPreferedConstructor(clazz);
            if (preferedConstructor != null) {
                ret = new ConstructorBasedFactory<T>(preferedConstructor).createDummy(genericMetaData, knownInstances, classBindings, exceptions);
            }

            if (ret == null) {
                for (final Constructor<?> co : cachedConstructors) {
                    ret = new ConstructorBasedFactory<T>((Constructor<T>)co).createDummy(genericMetaData, knownInstances, classBindings, exceptions);
                    if (ret != null) {
                        constructorCache.setPreferedConstructor(clazz, co);
                        break;
                    }
                }
            }

            usedInfo.setInstance(ret);
            usedInfo.setPopulated(true);
            populateObject(ret, genericMetaData, knownInstances, classBindings, exceptions);
        }

        return ret;
    }

    protected static String createTypeMarker(final Class<?> clazz, final Type[] genericMetaData) {
        return "|" + clazz.getName() + "|" + createTypeMarker(genericMetaData);
    }

    private static String createTypeMarker(final Type[] genericMetaData) {
        String typeMarker = "";
        if (genericMetaData != null) {
            if (genericMetaData.length == 1) {
                final Type nextGenericMetaData = genericMetaData[0];
                if (nextGenericMetaData instanceof Class) {
                    typeMarker += ((Class<?>)nextGenericMetaData).getName() + "|";
                } else {
                    typeMarker += ((Class<?>)((ParameterizedType)nextGenericMetaData).getRawType()).getName() + "|";
                    typeMarker += createTypeMarker(((ParameterizedType)nextGenericMetaData).getActualTypeArguments());
                }
            } else {
                for (final Type type : genericMetaData) {
                    typeMarker += createTypeMarker(new Type[] {type});
                }
            }
        }
        return typeMarker;
    }

    private boolean isKnownLoopSafeType(final Class<T> clazz) {
        final List<Class<?>> safeClasses = new ArrayList<Class<?>>();
        safeClasses.add(Integer.class);
        safeClasses.add(Long.class);
        safeClasses.add(Float.class);
        safeClasses.add(Boolean.class);
        safeClasses.add(Character.class);
        safeClasses.add(Byte.class);
        safeClasses.add(Short.class);
        safeClasses.add(Double.class);
        safeClasses.add(String.class);
        return safeClasses.contains(clazz);
    }

    public T populate(final T subject, final ClassBindings classBindings) {
        populateObject(subject, null, new HashMap<String, ClassUsageInfo<?>>(), classBindings, new ArrayList<Exception>());
        return subject;
    }

    @SuppressWarnings({"unchecked"})
    private void populateObject(final T subject, final Type[] genericMetaData, final Map<String, ClassUsageInfo<?>> knownInstances, final ClassBindings classBindings, final List<Exception> exceptions) {
        if (subject instanceof Collection) {
            populateCollection((Collection<Object>)subject, genericMetaData, knownInstances, classBindings, exceptions);
        } else if (subject instanceof Map) {
            populateMap((Map<Object, Object>)subject, genericMetaData, knownInstances, classBindings, exceptions);
        } else {
            doPopulateObject(subject, subject.getClass().getDeclaredFields(), genericMetaData, knownInstances, classBindings, populateCondition, exceptions);
            for (Class<?> clazz = subject.getClass().getSuperclass(); clazz != null; clazz = clazz.getSuperclass()) {
                doPopulateObject(subject, clazz.getDeclaredFields(), genericMetaData, knownInstances, classBindings, populateCondition, exceptions);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doPopulateObject(T subject,
                                  Field[] fields,
                                  Type[] genericMetaData,
                                  Map<String, ClassUsageInfo<?>> knownInstances,
                                  ClassBindings classBindings,
                                  PopulateCondition populateCondition,
                                  List<Exception> exceptions) {
        for (final Field field : fields) {
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            if (populateCondition != null) {
                boolean ignore = false;

                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    if (populateCondition.getIgnoreAnnotations().contains(annotation.annotationType())) {
                        ignore = true;
                    }
                }

                if (ignore) {
                    continue;
                }

                if (populateCondition.getIgnoreFieldNames().contains(field.getName())) {
                    continue;
                }

                if (populateCondition.getSetFields().containsKey(field.getName())) {
                    field.setAccessible(true);
                    try {
                        field.set(subject, populateCondition.getSetFields().get(field.getName()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    continue;
                }
            }

            ClassBasedFactory<T> factory;

            if (genericMetaData == null) {
                factory = new ClassBasedFactory<T>((Class<T>)field.getType());
            } else {
                factory = new ClassBasedFactory<T>((Class<T>)genericMetaData[0]);
            }

            final Type genericParameterType = field.getGenericType();
            Type[] nextGenericsMetaData = null;

            if (genericParameterType instanceof ParameterizedType) {
                nextGenericsMetaData = ((ParameterizedType)genericParameterType).getActualTypeArguments();
            }

            final Object parameter = factory.createDummy(nextGenericsMetaData, knownInstances, classBindings, exceptions);
            try {
                field.setAccessible(true);
                field.set(subject, parameter);
            } catch (final Exception e) {
                logger.error("error set field [" + field.getName() + "]", e);
            }
        }
    }

    private void populateCollection(final Collection<Object> subject,
                                    final Type[] genericMetaData,
                                    final Map<String, ClassUsageInfo<?>> knownInstances,
                                    final ClassBindings classBindings,
                                    final List<Exception> exceptions) {
        for (int i = 0; i < RandomCreator.getInstance().getRandomInt(2) + 2; i++) {
            final ClassAndGenericMetaData<?> classInfo = extractClassInfo(subject.getClass(), genericMetaData, 0);
            @SuppressWarnings("unchecked") final ClassBasedFactory<?> factory = new ClassBasedFactory<Object>((Class<Object>)classInfo.getClazz());
            final Object dummyObject = factory.createDummy(classInfo.getGenericMetaData(), knownInstances, classBindings, exceptions);
            subject.add(dummyObject);
        }
    }

    private void populateMap(final Map<Object, Object> subject,
                             final Type[] genericMetaData,
                             final Map<String, ClassUsageInfo<?>> knownInstances,
                             final ClassBindings classBindings,
                             final List<Exception> exceptions) {
        for (int i = 0; i < RandomCreator.getInstance().getRandomInt(2) + 2; i++) {
            final ClassAndGenericMetaData<?> keyClassInfo = extractClassInfo((Class<?>)subject.getClass(), genericMetaData, 0);
            final ClassAndGenericMetaData<?> valueClassInfo = extractClassInfo((Class<?>)subject.getClass(), genericMetaData, 1);
            @SuppressWarnings("unchecked") final ClassBasedFactory<?> dummyKeyFactory = new ClassBasedFactory<Object>((Class<Object>)keyClassInfo.getClazz());
            @SuppressWarnings("unchecked") final ClassBasedFactory<?> dummyValueFactory = new ClassBasedFactory<Object>((Class<Object>)valueClassInfo.getClazz());
            final Object dummyKey = dummyKeyFactory.createDummy(keyClassInfo.getGenericMetaData(), knownInstances, classBindings, exceptions);
            final Object dummyValue = dummyValueFactory.createDummy(valueClassInfo.getGenericMetaData(), knownInstances, classBindings, exceptions);
            subject.put(dummyKey, dummyValue);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ClassAndGenericMetaData<?> extractClassInfo(Class<?> clazz, Type[] genericMetaData, int index) {
        final ClassAndGenericMetaData<Object> keyClassInfo = new ClassAndGenericMetaData<Object>();

        final Type[] genericTypes = ((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments();
        if (genericTypes.length > index && genericTypes[index] instanceof Class) {
            keyClassInfo.setClazz((Class<Object>)genericTypes[index]);
        } else if (genericMetaData != null) {
            final Class<?> keyClassFromGenerics;

            if (genericMetaData[index] instanceof Class) {
                keyClassFromGenerics = (Class<?>)genericMetaData[index];
            } else {
                keyClassFromGenerics = (Class<?>)((ParameterizedType)genericMetaData[index]).getRawType();
                keyClassInfo.setGenericMetaData(((ParameterizedType)genericMetaData[index]).getActualTypeArguments());
            }

            keyClassInfo.setClazz((Class<Object>)keyClassFromGenerics);
        } else {
            keyClassInfo.setClazz((Class)String.class);
        }
        return keyClassInfo;
    }

    private static class ClassAndGenericMetaData<T> {
        private Class<T> clazz;
        private Type[] genericMetaData;

        public Class<T> getClazz() {
            return clazz;
        }

        public void setClazz(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Type[] getGenericMetaData() {
            return genericMetaData;
        }

        public void setGenericMetaData(Type[] genericMetaData) {
            this.genericMetaData = genericMetaData;
        }
    }

    public Object createDummy(ClassBindings classBindings, PopulateCondition populateCondition) {

        return null;
    }

}
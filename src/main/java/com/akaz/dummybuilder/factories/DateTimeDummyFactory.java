package com.akaz.dummybuilder.factories;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.dummycreator.ClassBindings;
import org.dummycreator.ClassUsageInfo;
import org.dummycreator.dummyfactories.DummyFactory;
import org.joda.time.DateTime;

public class DateTimeDummyFactory extends DummyFactory<DateTime> {

    @Override
    public DateTime createDummy(Type[] genericMetaData,
                                Map<String, ClassUsageInfo<?>> knownInstances,
                                ClassBindings classBindings,
                                List<Exception> exceptions) {
        return DateTime.now();
    }

}

package com.akaz.dummybuilder.factories;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dummycreator.ClassBindings;
import org.dummycreator.ClassUsageInfo;
import org.dummycreator.dummyfactories.DummyFactory;

public class DateDummyFactory extends DummyFactory<Date> {

    @Override
    public Date createDummy(Type[] genericMetaData,
                            Map<String, ClassUsageInfo<?>> knownInstances,
                            ClassBindings classBindings,
                            List<Exception> exceptions) {
        return new Date();
    }

}

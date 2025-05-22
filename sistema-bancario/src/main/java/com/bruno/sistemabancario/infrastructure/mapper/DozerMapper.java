package com.bruno.sistemabancario.infrastructure.mapper;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

import java.util.ArrayList;
import java.util.List;

public class DozerMapper {

    private static final Mapper mapper = DozerBeanMapperBuilder.buildDefault();

    public static <O, D> D parseObject(O origin, Class<D> destinationClass) {
        return mapper.map(origin, destinationClass);
    }

    public static <O, D> List<D> parseListObjects(List<O> origin, Class<D> destinationClass) {
        List<D> destinationObjects = new ArrayList<>();
        for (O object : origin) {
            destinationObjects.add(mapper.map(object, destinationClass));
        }
        return destinationObjects;
    }
}

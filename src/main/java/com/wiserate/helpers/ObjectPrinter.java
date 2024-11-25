package com.wiserate.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectPrinter {

    private static final Logger log = LoggerFactory.getLogger(ObjectPrinter.class);

    public static void printObjectFields(Object obj) throws IllegalAccessException {
        printObjectFields(obj, new HashSet<>());
    }

    private static void printObjectFields(Object obj, Set<Object> visitedObjects) throws IllegalAccessException {
        if (obj == null) {
            log.info("Object is null");
            return;
        }

        // Prevent revisiting already visited objects
        if (visitedObjects.contains(obj)) {
            log.info("Skipping already visited object: {}", obj.getClass().getName());
            return;
        }

        // Add the current object to the visited set
        visitedObjects.add(obj);

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true); // Try making the field accessible
                Object value = field.get(obj); // Get the field value

                if (value == null) {
                    log.info("{} is null", field.getName());
                    continue;
                }

                if (isPrimitiveOrWrapper(field.getType()) || field.getType().equals(String.class)) {
                    // Log primitive or String fields
                    log.info("{}: {}", field.getName(), value);
                } else {
                    // Recursively print nested objects
                    log.info("{}: (nested object)", field.getName());
                    printObjectFields(value, visitedObjects);
                }
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                log.warn("Cannot access field: {}. Reason: {}", field.getName(), e.getMessage());
            }
        }
    }

    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(Boolean.class) || type.equals(Byte.class) || type.equals(Character.class) ||
                type.equals(Double.class) || type.equals(Float.class) || type.equals(Integer.class) ||
                type.equals(Long.class) || type.equals(Short.class);
    }
}

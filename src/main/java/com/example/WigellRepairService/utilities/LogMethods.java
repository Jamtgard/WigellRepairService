package com.example.WigellRepairService.utilities;

import java.lang.reflect.Field;

public class LogMethods {

    public static <T> String logUpdateBuilder(T oldObject, T newObject, String... fieldsToCompare) {
        StringBuilder log = new StringBuilder();
        for (String fieldName : fieldsToCompare) {
            try {
                Field field = oldObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object oldValue = field.get(oldObject);
                Object newValue = field.get(newObject);
                boolean changed = (oldValue != null && !oldValue.equals(newValue))
                        || (oldValue == null && newValue != null);
                if (changed) {
                    log.append(String.format("%n\t%s: '%s' -> '%s'; ", fieldName, oldValue, newValue));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.append(String.format("%n\t%s: [error reading field]; ", fieldName));
            }
        }
        return log.toString();
    }

    public static <T> String logBuilder(T object, String... fieldsToLog) {
        StringBuilder log = new StringBuilder();
        for (String fieldName : fieldsToLog) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(object);
                log.append(String.format("%n\t%s: '%s'", fieldName, value));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.append(String.format("%n\t%s: [error reading field]; ", fieldName));
            }
        }
        return log.toString();
    }
}

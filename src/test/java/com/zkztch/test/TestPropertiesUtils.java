package com.zkztch.test;

import com.google.common.io.Resources;

import java.io.*;
import java.util.Properties;

public class TestPropertiesUtils {

    private static final Properties testProperties = new Properties();

    static {
        try (InputStream inputStream = Resources.newInputStreamSupplier(Resources.getResource("test.properties")).getInput()) {
            testProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a named property from the test-gitlab4j.properties file.
     *
     * @param key the key of the property to get
     * @return the named property from the test-gitlab4j.properties file
     */
    public static final String getProperty(String key) {
        return (testProperties.getProperty(key));
    }

    /**
     * Get a named property from the test-gitlab4j.properties file,
     * will return the defaultValue if null or empty.
     *
     * @param key          the key of the property to get
     * @param defaultValue the value to return if property is null or empty
     * @return the named property from the test-gitlab4j.properties file
     */
    public static final String getProperty(String key, String defaultValue) {

        String value = getProperty(key);
        if (value != null && value.trim().length() > 0) {
            return (value);
        }

        if (defaultValue != null) {
            testProperties.setProperty(key, defaultValue);
        }

        return (defaultValue);
    }

    /**
     * Set a named property, this will amend and overwrite properties read from the test-gitlab4j.properties file.
     *
     * @param key the key of the property to get
     * @return the named property from the test-gitlab4j.properties file
     */
    public static final void setProperty(String key, String value) {
        if (value == null) {
            testProperties.remove(key);
        } else {
            testProperties.setProperty(key, value);
        }
    }

    /**
     * Get a random integer between 1 and the specified value (inclusive).
     *
     * @param maxValue the maximum value to return
     * @return a random integer between 1 and the specified value (inclusive)
     */
    public static final int getRandomInt(int maxValue) {
        return ((int) (Math.random() * maxValue + 1));
    }

    /**
     * Get a random integer between 1 and Integer.MAX_VALUE (inclusive).
     *
     * @return a random integer between 1 and Integer.MAX_VALUE (inclusive)
     */
    public static final int getRandomInt() {
        return (getRandomInt(Integer.MAX_VALUE));
    }
}

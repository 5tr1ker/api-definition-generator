package com.ideatec.definition_generator.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {

    public static Properties load(String path) throws IOException {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        }

        return props;
    }

}
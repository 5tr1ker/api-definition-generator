package com.ideatec.definition_generator.util;

import com.ideatec.definition_generator.resources.JDBCDriver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Arrays;

@Getter
@Configuration
@PropertySource("file:${user.dir}/setting/dbconnector/dbconnector.properties")
public class DataSourceGenerate {

    @Value("${dbconnect.database.type}")
    private JDBCDriver databaseType;

    @Value("${dbconnect.database.url}")
    private String url;

    @Value("${dbconnect.database.username}")
    private String username;

    @Value("${dbconnect.database.password}")
    private String password;

    @Bean
    public DataSource dataSource() throws Exception {
        String currentPath = System.getProperty("user.dir") + "\\setting\\driver\\" + databaseType.getType();
        File dir = new File(currentPath);

        File[] jarFiles = dir.listFiles((d, name) -> name.endsWith(".jar"));
        URL[] jarUrls = Arrays.stream(jarFiles)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);

        URLClassLoader combinedLoader = new URLClassLoader(
                jarUrls ,
                this.getClass().getClassLoader()
        );

        Class<?> driverClass = Class.forName(databaseType.getDriver(), true, combinedLoader);
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
        DriverManager.registerDriver(new DriverShim(driver));

        Thread.currentThread().setContextClassLoader(combinedLoader);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(databaseType.getDriver());
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);

        return new HikariDataSource(config);
    }

}

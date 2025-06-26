package com.ideatec.definition_generator.core;

import com.ideatec.definition_generator.util.DataSourceGenerate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Component
public class DBConnector {

    private DataSource dataSource = null;
    private Connection connection = null;

    public boolean connectDB(DataSource dataSource) {
        this.dataSource = dataSource;

        if(dataSource == null) {
            System.out.println("DataSource 연결 정보가 없습니다.");

            return false;
        }
        try {
            connection = dataSource.getConnection();
            System.out.println("연결 성공 : " + connection.getMetaData().getURL() );

            return true;
        } catch (SQLException sqlException) {
            System.out.println("예외가 발생했습니다 : " + sqlException.getMessage());

            return false;
        }
    }

    public List<String> getSchema(String prefix) {
        return null;
    }

    public void printSchema(String prefix) {
        List<String> result = getSchema(prefix);
    }

}

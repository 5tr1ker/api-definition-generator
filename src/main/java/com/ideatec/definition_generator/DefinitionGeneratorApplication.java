package com.ideatec.definition_generator;

import com.ideatec.definition_generator.core.*;
import com.ideatec.definition_generator.util.DataSourceGenerate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javax.sql.DataSource;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@RequiredArgsConstructor
public class DefinitionGeneratorApplication {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(DefinitionGeneratorApplication.class, args);
        /**
         * DB 관련 Bean
         */
        DataSourceGenerate dataSourceGenerate = context.getBean(DataSourceGenerate.class);
        DBConnector DBConnector = context.getBean(DBConnector.class);

        /**
         * Excel 관련 Bean
         */
        ExcelFormGenerator excelFormGenerator = context.getBean(ExcelFormGenerator.class);

        // 실행 코드
        // DBMS 연결
        /*
        DataSource dataSource = dataSourceGenerate.dataSource();
        DBConnector.connectDB(dataSource);
        */

        excelFormGenerator.generatorExcelForm();
    }

}

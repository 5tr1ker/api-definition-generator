package com.ideatec.definition_generator;

import com.ideatec.definition_generator.core.ExcelFormGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@RequiredArgsConstructor
public class DefinitionGeneratorApplication {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(DefinitionGeneratorApplication.class, args);
        /**
         * Excel 관련 Bean
         */
        ExcelFormGenerator excelFormGenerator = context.getBean(ExcelFormGenerator.class);

        excelFormGenerator.generatorExcelForm();
    }

}

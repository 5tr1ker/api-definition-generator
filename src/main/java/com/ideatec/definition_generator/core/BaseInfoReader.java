package com.ideatec.definition_generator.core;

import com.ideatec.definition_generator.util.PropertyLoader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Component
public class BaseInfoReader {

    private boolean isCamelCase = false;

    public HashMap<String, Object> readProperties(File file) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        Properties properties = PropertyLoader.load(file.getPath());

        // API 정보
        result.put("api-id", properties.getProperty("interface.definition.api-id"));
        result.put("api-group", properties.getProperty("interface.definition.api-group"));
        result.put("api-name", properties.getProperty("interface.definition.api-name"));
        result.put("description", properties.getProperty("interface.definition.description"));

        // API 목록에 보여질 정보
        result.put("source-system", properties.getProperty("interface.definition.source-system"));
        result.put("target-system", properties.getProperty("interface.definition.target-system"));
        result.put("interface-id", properties.getProperty("interface.definition.interface-id"));
        result.put("interface-name", properties.getProperty("interface.definition.interface-name"));
        result.put("route-api-id", properties.getProperty("interface.definition.route-api-id"));
        result.put("route-system", properties.getProperty("interface.definition.route-system"));

        // REST 정보
        result.put("uri", properties.getProperty("interface.definition.uri"));
        result.put("method", properties.getProperty("interface.definition.method"));
        result.put("content-type", properties.getProperty("interface.definition.content-type"));

        // 설정값
        result.put("camel-case", properties.getProperty("interface.definition.convert-camel-case"));
        setCamelCase(properties.getProperty("interface.definition.convert-camel-case"));

        return result;
    }

    public List<HashMap<String, Object>> readSchema(File file) throws IOException {
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);
        List<HashMap<String, Object>> result = new LinkedList<>();
        // sheet 에서 필요한 값의 인덱스 찾기
        Row title = sheet.getRow(0);
        int columnIndex = -1;
        int typeIndex = -1;
        int requiredIndex = -1;
        int commentIndex = -1;

        for(int i = 0; i < title.getPhysicalNumberOfCells(); i++) {
            String value = title.getCell(i).getStringCellValue();

            if(value.equals("컬럼명")) {
                columnIndex = i;
            } else if (value.equals("Data Type")) {
                typeIndex = i;
            } else if (value.equals("Not Null")) {
                requiredIndex = i;
            } else if (value.equals("Comment")) {
                commentIndex = i;
            }
        }

        for(int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            HashMap<String, Object> map = new HashMap<>();
            Row row = sheet.getRow(i);

            if (columnIndex > -1) {
                String index = row.getCell(columnIndex).getStringCellValue();
                map.put("key" , isCamelCase ? toCamelCase(index) : index);
            }
            if (typeIndex > -1) {
                String index = convertDateType(row.getCell(typeIndex).getStringCellValue());
                map.put("type" , isCamelCase ? toCamelCase(index) : index);
            }
            if (typeIndex > -1) {
                String index = extractionSize(row.getCell(typeIndex).getStringCellValue());
                map.put("length" , isCamelCase ? toCamelCase(index) : index);
            }
            if (requiredIndex > -1) {
                String index = row.getCell(requiredIndex).getBooleanCellValue() ? "true" : "";
                map.put("required" , isCamelCase ? toCamelCase(index) : index);
            }
            if (commentIndex > -1) {
                String index = row.getCell(commentIndex).getStringCellValue();
                map.put("comment" , isCamelCase ? toCamelCase(index) : index);
            }

            result.add(map);
        }
        workbook.close();

        return result;
    }

    private String convertDateType(String input) {
        if(input.contains("(") && input.contains(")")) {
            return input.replaceAll("\\(.*\\)", "").trim();
        }

        return input;
    }

    // 괄호 안 숫자 추출
    private String extractionSize(String input) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\((\\d+)\\)").matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    private String toCamelCase(String value) {
        String input = value.toLowerCase();
        StringBuilder result = new StringBuilder();
        boolean toUpper = false;

        for (char ch : input.toCharArray()) {
            if (ch == '_') {
                toUpper = true;
            } else {
                if (toUpper) {
                    result.append(Character.toUpperCase(ch));
                    toUpper = false;
                } else {
                    result.append(ch);
                }
            }
        }

        return result.toString();
    }

    private void setCamelCase(String value) throws Exception {
        if(value == null) {
            return;
        }

        if(value.equalsIgnoreCase("true")) {
            this.isCamelCase = true;
        } else if (value.equalsIgnoreCase("false")) {
            this.isCamelCase = false;
        } else {
            throw new Exception("isCamelCase 값은 true, false 만 허용됩니다. 입력 값 : " + value);
        }
    }

}

package com.ideatec.definition_generator.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExcelFormGenerator {

    private final BaseInfoReader baseInfoReader;

    public void generatorExcelForm() throws Exception {
        File folder_element = new File(System.getProperty("user.dir") + "/setting/element");
        if (!folder_element.exists()) {
            folder_element.mkdirs();
        }

        File folder_result = new File(System.getProperty("user.dir") + "/setting/result");
        if (!folder_result.exists()) {
            folder_result.mkdirs();
        }

        buildCatalogXslx();
        buildContentXlsx();
        mergeXlsx();
    }

    private void mergeXlsx() throws Exception {
        // 속성들 가져오기
        InputStream baseExcel = new FileInputStream(System.getProperty("user.dir") + "/setting/element/catalog.xlsx");
        Workbook workbook = WorkbookFactory.create(baseExcel);

        // 컨텐츠 병합
        Workbook resultWorkbook = addContentSheet(workbook);

        // 파일 저장
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "/setting/result/result.xlsx");
        resultWorkbook.write(fos);
        baseExcel.close();
        fos.close();

        // 리소스 제거
        File resource = new File(System.getProperty("user.dir") + "/setting/element");
        removeResource(resource);
    }

    private Workbook addContentSheet(Workbook workbook) throws Exception {
        File[] xlsxFiles = Arrays.stream(getElementFolder())
                .filter(f -> f.getName().contains("content-"))
                .toArray(File[]::new);

        for(File xlsx : xlsxFiles) {
            Workbook targetXlsx = WorkbookFactory.create(xlsx);
            Sheet sourceSheet = targetXlsx.getSheetAt(0);
            String[] nameSplit = xlsx.getName().split("content-");
            String interfaceName = nameSplit[nameSplit.length - 1].replace(".xlsx" , "");

            Sheet newSheet = workbook.createSheet(interfaceName);

            // 복사
            copySheet(sourceSheet, newSheet);

            targetXlsx.close();
        }

        return workbook;
    }

    private void copySheet(Sheet sourceSheet, Sheet newSheet) {
        Workbook newWorkbook = newSheet.getWorkbook();

        for(int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
            Row sourceRow = sourceSheet.getRow(i);
            Row newRow = newSheet.createRow(i);
            if (sourceRow == null) continue;

            newRow.setHeight(sourceRow.getHeight());
            for(int j = 0; j < sourceRow.getLastCellNum(); j++) {
                Cell sourceCell = sourceRow.getCell(j);
                Cell newCell = newRow.createCell(j);

                if (sourceCell == null) continue;
                copyCellValue(sourceCell, newCell);
                copyCellStyle(sourceCell, newCell, newWorkbook);
            }
        }

        // 열 넓이 복사
        for (int i = 0; i <= sourceSheet.getRow(0).getLastCellNum(); i++) {
            newSheet.setColumnWidth(i, sourceSheet.getColumnWidth(i));
        }

        // 병합 셀 복사
        for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sourceSheet.getMergedRegion(i);
            newSheet.addMergedRegion(mergedRegion.copy());
        }
    }

    private void copyCellValue(Cell src, Cell dest) {
        switch (src.getCellType()) {
            case STRING -> dest.setCellValue(src.getStringCellValue());
            case NUMERIC -> dest.setCellValue(src.getNumericCellValue());
            case BOOLEAN -> dest.setCellValue(src.getBooleanCellValue());
            case FORMULA -> dest.setCellFormula(src.getCellFormula());
            case BLANK -> {}
            default -> dest.setCellValue(src.toString());
        }
    }

    private void copyCellStyle(Cell src, Cell dest, Workbook newWorkbook) {
        CellStyle newStyle = newWorkbook.createCellStyle();
        newStyle.cloneStyleFrom(src.getCellStyle());
        dest.setCellStyle(newStyle);
    }

    private void buildContentXlsx() throws Exception {
        // 엑셀 데이터 연결
        File[] jarFiles = getWorkFolder();
        long index = 1L;

        for(File directory : jarFiles) {
            // 템플릿 가져오기
            InputStream templateStream = new ClassPathResource("frame-content.xlsx").getInputStream();
            Context context = new Context();

            // 작업 파일에서 데이터 가져와 조회
            File properties = directory.listFiles((d, name) -> name.endsWith(".properties"))[0];
            File xlsx = directory.listFiles((d, name) -> name.endsWith(".xlsx"))[0];
            HashMap<String, Object> propertiesValue = baseInfoReader.readProperties(properties);
            List<HashMap<String, Object>> schemaValue = baseInfoReader.readSchema(xlsx);

            context.putVar("excelData", buildPropertiesContext(propertiesValue));
            context.putVar("columns", buildSchemaValue(schemaValue));
            context.putVar("example", buildReqResContent(propertiesValue, schemaValue));

            // Xlsx 파일 생성
            OutputStream out = new FileOutputStream( System.getProperty("user.dir") + "/setting/element/" + (index++) + "content-" + propertiesValue.get("interface-id") + ".xlsx");
            JxlsHelper.getInstance().processTemplate(templateStream, out, context);
            templateStream.close();
            out.close();
        }
    }

    private void buildCatalogXslx() throws Exception {
        // 엑셀 데이터 연결
        InputStream templateStream = new ClassPathResource("frame-catalog.xlsx").getInputStream();
        Context context = new Context();
        List<HashMap<String, Object>> catalog = new LinkedList<>();

        // 데이터 읽기
        File[] jarFiles = getWorkFolder();

        for(File directory : jarFiles) {
            File properties = directory.listFiles((d, name) -> name.endsWith(".properties"))[0];
            HashMap<String, Object> propertiesValue = baseInfoReader.readProperties(properties);

            catalog.add(buildPropertiesContext(propertiesValue));
        }
        context.putVar("excelData", catalog);

        // Excel 파일 생성
        OutputStream out = new FileOutputStream( System.getProperty("user.dir") + "/setting/element/catalog.xlsx");
        JxlsHelper.getInstance().processTemplate(templateStream, out, context);
        out.close();
    }

    private File[] getWorkFolder() throws Exception {
        return getFile(System.getProperty("user.dir") + "/setting/sheet");
    }

    private File[] getElementFolder() throws Exception {
        return getFile(System.getProperty("user.dir") + "/setting/element");
    }

    private File[] getFile(String path) throws Exception {
        File dir = new File(path);
        File[] jarFiles = dir.listFiles();

        if(jarFiles == null || jarFiles.length <= 0) {
            throw new Exception("작업 경로에 폴더가 없습니다.");
        }

        return jarFiles;
    }

    private HashMap<String, Object> buildPropertiesContext(HashMap<String, Object> propertiesValue) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("api_id" , propertiesValue.get("api-id"));
        result.put("api_group" , propertiesValue.get("api-group"));
        result.put("api_name" , propertiesValue.get("api-name"));
        result.put("source_system" , propertiesValue.get("source-system"));
        result.put("target_system" , propertiesValue.get("target-system"));
        result.put("uri" , propertiesValue.get("uri"));
        result.put("description" , propertiesValue.get("description"));
        result.put("interface_id" , propertiesValue.get("interface-id"));
        result.put("interface_name" , propertiesValue.get("interface-name"));
        result.put("route_api_id" , propertiesValue.get("route-api-id"));
        result.put("route_system" , propertiesValue.get("route-system"));
        result.put("method" , propertiesValue.get("method").toString().toUpperCase());
        result.put("content_type" , propertiesValue.get("content-type"));
        result.put("body_type" , propertiesValue.get("method").toString().equalsIgnoreCase("get") ? "Request Param" : "Request Body");

        return result;
    }

    private List<HashMap<String, Object>> buildSchemaValue(List<HashMap<String, Object>> schemaValue) {
        long no = 1L;
        for(HashMap<String, Object> resource : schemaValue) {
            resource.put("no" , no++);
            resource.put("level", 1);
        }

        return schemaValue;
    }

    private String buildRequestURL(String url, List<HashMap<String, Object>> schemaValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(url).append("?");

        for(HashMap<String, Object> resource : schemaValue) {
            sb.append(resource.get("key")).append("=").append(buildSampleDataByType(resource.get("type").toString())).append("&");
        }

        return sb.substring(0, sb.length() - 1);
    }

    private String buildSampleDataByType(String input) {
        String type = input.toLowerCase();

        if(type.contains("number") || type.contains("int") || type.contains("long")) {
            return "100";
        } else if(type.contains("timestamp") || type.contains("date") || type.contains("time") || type.contains("year")) {
            return "2000-01-01";
        }

        return "example";
    }

    private HashMap<String, Object> buildReqResContent(HashMap<String, Object> propertiesValue, List<HashMap<String, Object>> schemaValue)
            throws JsonProcessingException {
        HashMap<String, Object> result = new HashMap<>();
        String method = propertiesValue.get("method").toString().toLowerCase();
        String requestContent;

        if(method.equals("get")) {
            requestContent = buildRequestURL(propertiesValue.get("uri").toString(), schemaValue);
        } else {
            requestContent = buildRequestBody(schemaValue);
        }
        String responseContent = buildResponseBody(schemaValue);

        result.put("request" , requestContent);
        result.put("response" , responseContent);

        return result;
    }

    private String buildRequestBody(List<HashMap<String, Object>> schemaValue) throws JsonProcessingException {
        HashMap<String, Object> body = new HashMap<>();
        for(HashMap<String, Object> resource : schemaValue) {
            body.put(resource.get("key").toString() , buildSampleDataByType(resource.get("type").toString()));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(body);
    }

    private String buildResponseBody(List<HashMap<String, Object>> schemaValue) throws JsonProcessingException {
        List<HashMap<String, Object>> content = new LinkedList<>();
        HashMap<String, Object> content_body = new HashMap<>();
        for(HashMap<String, Object> resource : schemaValue) {
            content_body.put(resource.get("key").toString() , buildSampleDataByType(resource.get("type").toString()));
        }

        for(int i = 0; i < 2;i++) {
            content.add(content_body);
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("statusCode" , 200);
        root.put("message" , mapper.valueToTree((content)));

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private void removeResource(File file) {
        if (!file.exists()) return;

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    removeResource(child);
                }
            }
        }
        file.delete();
    }

}

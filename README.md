# API 명세서 생성기 프로그램

## 1. 소개

**API 명세서 생성기 프로그램**은 DB 테이블 정보를 기반으로 API 명세서를 자동 생성해주는 도구입니다. 사용자가 정의한 설정과 엑셀 시트를 바탕으로 명세서를 생성하며, 명세서의 통일성과 생산성을 향상시킬 수 있습니다.

## 2. 필요 의존성

- Java 21 이상

## 3. 사용 방법

### 3.0 주의 사항

- 하나의 **시트 폴더**는 하나의 엑셀 시트를 의미하며, 각 시트 폴더에는 반드시 `properties` 파일과 `xlsx` 파일이 **각각 1개씩** 존재해야 합니다.
  - **2개 이상 존재할 경우 무작위 하나만 적용**됩니다.
  - 예시 : 10개의 엑셀 시트를 생성하려면, 10개의 시트 폴더와 각각의 시트 폴더 안에는 1개의 `properties` 및 `xlsx` 파일이 있어야 합니다.
- 결과 엑셀 파일의 **첫 번째 시트는 "API 목록"** 으로 자동 생성되며, 전체 시트의 목차 역할을 합니다.
  - 따라서 10개의 시트 폴더를 생성한 경우, 최종 엑셀에는 총 11개의 시트가 생성됩니다.
- 작성 양식 및 예제는 `%프로젝트 폴더%/setting/sheet` 경로에 존재합니다. (총 3개의 예제 제공)
  - 작업 시 예제는 삭제 후 사용 바랍니다.

### 3.1 시트 폴더 생성

- `%프로젝트 폴더%/setting/sheet` 경로에 원하는 이름의 폴더를 생성합니다.
  - 폴더명은 시트명에 영향을 주지 않습니다.
  - 각 폴더 내부에는 다음 2개의 파일이 있어야 합니다:
    - 하나의 `.properties` 파일
    - 하나의 `.xlsx` 파일

### 3.2 properties 파일 작성

- 한 시트 폴더에 하나의 `.properties` 파일만 존재해야 합니다.

#### [API 정보]
| 키 | 설명 |
|----|------|
| `interface.definition.api-id` | API ID 식별자 (API 목록 시트에서만 표시) |
| `interface.definition.api-group` | API Group 이름 |
| `interface.definition.api-name` | API 이름 |
| `interface.definition.description` | API 설명 |

#### [API 목록에 표시될 정보]
| 키 | 설명 |
|----|------|
| `interface.definition.source-system` | 요청을 보내는 시스템 |
| `interface.definition.target-system` | 요청을 받는 시스템 |
| `interface.definition.interface-id` | 인터페이스 ID (시트 이름) |
| `interface.definition.interface-name` | 인터페이스 이름 |
| `interface.definition.route-api-id` | 연계 시스템 ID |
| `interface.definition.route-system` | 연계 시스템 정보 ( 다른 시스템에서 API를 받거나 보낼 때 시스템 정보 )  |

#### [Rest 설정]
| 키 | 설명 |
|----|------|
| `interface.definition.uri` | 요청 URI |
| `interface.definition.method` | 요청 HTTP Method |
| `interface.definition.content-type` | 요청 Content-Type |

#### [기타 설정]
| 키 | 설명 |
|----|------|
| `interface.definition.convert-camel-case` | xlsx의 컬럼명을 카멜케이스로 변환 여부 |

### 3.3 xlsx 파일 작성

- 컬럼은 **최대 15개**까지 지원됩니다. (버전 1.0.0 기준)
- **DBeaver**에서 테이블 정보를 복사해 작성하는 것을 권장합니다.
  - 테이블 연결 > 테이블 우클릭 > `View Table` > `Columns` → 전체 선택 → `Copy Advanced Info` → Excel에 붙여넣기
  - ![image](https://github.com/user-attachments/assets/40fafff6-57cd-4720-b0ca-87d0253bb534)

- 필수 값은 아니지만, 해당 키 값이 정보가 엑셀에 표현됩니다:
  - 컬럼명
  - Data Type
  - Comment
  - Not Null 여부

## 4. 프로그램 실행

- `%프로젝트 폴더%` 기준에서 다음 명령어로 실행:

  ```bash
  java -jar definition_generator-1.0.0.jar
  ```
- 또는 다음 배치 파일을 실행:
  ```bash
  execute_program.bat
  ```


## 5. 결과 문서

- 생성된 엑셀 명세서는 다음 경로에 저장됩니다:
  ```
  %프로젝트 폴더%/setting/result/result.xlsx
  ```

## 6. Class 설계도

![image](https://github.com/user-attachments/assets/07c544eb-24bd-4c49-a0dd-b99b48d7999e)

# Backend Coding Convention

## 1. 기본 문법 스타일
### 1.1 들여쓰기 및 줄바꿈
* 들여쓰기: 탭(Tab) 사용을 원칙으로 함
    * 공백(Space) 기반 들여쓰기 금지
* 줄바꿈: LF (Unix 스타일)
* 파일 인코딩: UTF-8 (NOT BOM)
### 1.2 네이밍 규칙
* 클래스/인터페이스: `PascalCase`
* 메소드/변수: `camelCase`
* 상수: `UPPER_SNAKE_CASE`
* 패키지명: **모두 소문자**, 점(.)으로 계층 구분
    * ex) `com.codensap.api.user`
* 파일명: 클래스명과 동일하게 유지
### 1.3 주석 스타일
* 클래스 및 메소드: JavaDoc 사용 권장
* 로직 설명: `// 한 줄 주석` 또는 `/* 여러 줄 */`
* TODO: `// TODO: [작성자명] 설명`
* FIXME: `// FIXME: [작성자명] 설명`

## 2. 디렉토리 및 패키지 구조
```
src/main/java/com/codesnap
├── api
│   ├── user
│   │   ├── web        # Controller 계층
│   │   ├── dto        # 요청/응답 DTO
│   │   └── service
│   │       └── impl   # 구현 클래스
│   ├── snippet
│   │   ├── web
│   │   ├── dto
│   │   └── service
│   │       └── impl
├── config             # 설정 관련 클래스
├── util               # 유틸성 공통 클래스
├── exception           # 공통 예외 처리
└── ...
```
위 예시처럼 작성
> * 각 기능은 api 하위에 독립된 패키지로 구성  
> * 기능 패키지 내에는 web,dto,service 하위 패키지 사용
> * 서비스로직 구현체, Mybatis Mapper 인터페이스는 service.impl에 위치

> 계층 간 참조는 단방향 유지, `web -> service -> mapper` 흐름을 기본으로 사용  
`DTO Class`의 경우 서비스 레이어 전달까지 사용,   
`Entity Class`의 경우 서비스 레이어에서 Repositry(DB) 까지 사용함

## 3. 클래스 및 메서드 규칙
### 3.1 클래스 구조
* 생성자 -> public 메소드 -> private 메소드 순서 유지
* Lombok 사용 시 `@RequiredArgsConstructor` 사용 권장
* 역할 명확한 어노테이션 명시: `@Service` `@RestController`...
### 3.2 메소드 작성 기준
* 하나의 메소드는 **하나의 역할만 수행**하도록 작성(SRP 원칙)
* 메소드 길이 30줄 이하를 기본 기준으로 유지
* **들여쓰기 2단 이상**이 발생하면 메소드 분리를 고려 할 것
    * ❌ BAD:
        ```java
        public void doSomething() {
            if (a) {
                for (...) {
                    // ...
                }
            }
        }
        ```
    * ✅ GOOD:
        ```java
        public void doSomething() {
            if (a) {
                handleLoop();
            }
        }

        private void handleLoop() {
            for (...) {
                // ...
            }
        }
        ```
### 3.3 코드 일관성
* 중괄호는 항상 개행 없이 한 줄에 작성
``` java
if (condition) {
    // ...
}
```
* 불필요한 줄바꿈, 주석, 임포트 제거
* `var` 사용 지양, 명시적 타입 사용

## 4. 예외 및 응답 처리
> `@RestController`에서 반환하는 모든 응답은 반드시 **공통 ResponseDto (`ApiResponse<T>`)**를 사용하여 일관된 응답 구조 유지

### 4.1 공통 예외 처리
* `@ControllerAdvice` + `@ExceptionHandler` 조합 사용
* 사용자 정의 예외는 `ApplicationException`을 상속하여 구현
* 예외 코드는 Enum으로 관리 (`ErrorCode`)
### 4.2 응답 객체 구조
* 공통 응답 포맷: `ApiResponse<T>`
* 응답 본문은 명확한 key-value 형태로 작성
* 성공/실패 여부를 명시하는 `success` 필드 포함 권장

## 5. 코드 품질 및 도구
### 5.1 테스트 작성
* 단위 테스트는 JUnit5 기반 작성
* 테스트 클래스 명: `ClassNameTest`
* `Given-When-Then` 구조로 테스트 명확히 구분
* Mock 객체는 `@MockBean`, `Mockito` 활용
### 5.2 기타 권장사항
* `Optional` 적극 사용하여 NPE 방지
* DTO간 변환은 7번에 명시된 방법 활용
* 컨트롤러는 얇게 유지, 비즈니스 로직은 서비스로 위임

## 6. DTO ↔ Entity 매핑 전략
### 6.1 수동 매핑 (명시적 매핑)
```java
public SnippetDto toDto(Snippet entity) {
	return new SnippetDto(entity.getId(), entity.getTitle(), entity.getLanguage());
}
```
* **장점**: 명확하고 디버깅 쉬움, 커스터마이징 용이
* **단점**: 코드 반복 많음

### 6.2 MapStruct 사용 (컴파일 타임 코드 생성)
```java
@Mapper(componentModel = "spring")
public interface SnippetMapper {
	SnippetDto toDto(Snippet entity);
	Snippet toEntity(SnippetDto dto);
}
```
* **장점**: 성능 우수, 명확한 구조, Spring 연동 용이
* **단점**: 초기 학습 필요, 세부 제어 위해 어노테이션 필요



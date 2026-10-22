# HR Bank - SB15기 팀 03

> 임직원 정보와 조직을 효율적으로 관리하기 위한 HR 관리 시스템

- **팀 협업 문서:** [Notion](https://app.notion.com/p/2f86b25f577c443ab6cc6bf129d8a44c?v=383afdf095dc43ecbfa516345405373b)
- **GitHub Repository:** [b-15---HR-Banck--team-3-](https://github.com/gksrmf1210-droid/b-15---HR-Banck--team-3-)

---

## 팀원 구성

- **이준영** — [GitHub](https://github.com/gksrmf1210-droid)
- **이겸희** — [GitHub](https://github.com/LeeKyumhi)
- **이대원** — [GitHub](https://github.com/Oneday04)
- **박종우** — [GitHub](https://github.com/Park-jongwoo)

---

## 프로젝트 소개

HR Bank는 임직원 및 부서 정보를 관리하고, 조직 운영에 필요한 데이터를 효율적으로 조회·관리하기 위한 Spring Boot 기반 HR 관리 시스템입니다.

### 주요 목표

- 임직원 정보 등록 및 관리
- 부서 정보 등록 및 관리
- 직원과 부서 간 관계 관리
- 공통 응답 및 예외 처리 구조 구축
- 파일 저장 및 관리
- 데이터 백업 및 변경 이력 관리

- **프로젝트 기간:** 2026.10.14 ~ 2026.10.23

---

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Backend | Spring Boot, Spring Web |
| ORM | Spring Data JPA, Hibernate |
| Query | QueryDSL |
| Database | H2 |
| Build Tool | Gradle |
| API Documentation | Swagger / OpenAPI |
| Test | JUnit |
| Collaboration | Git, GitHub, Notion, Discord |
| IDE | IntelliJ IDEA |

---

## 주요 기능

### 1. 직원 관리

- 직원 정보 등록, 조회, 수정 및 삭제
- 사원번호 자동 생성
- 소속 부서 및 재직 상태 관리
- 직원 목록 검색, 정렬 및 커서 기반 페이지네이션
- 직원 수 및 분포·추이 통계 조회

### 2. 부서 관리

- 부서 등록, 조회, 수정 및 삭제
- 부서명 중복 방지
- 소속 직원이 존재하는 부서의 삭제 제한
- 부서 목록 검색 및 커서 기반 페이지네이션
- 부서별 직원 수 조회

### 3. 파일 관리

- 직원 프로필 이미지 등 파일 관리
- `FileStorage` 인터페이스를 통한 저장 방식 추상화
- `LocalFileStorage`를 이용한 로컬 디스크 저장
- `FileMeta`를 통한 파일 메타데이터 관리
- 파일 다운로드 및 삭제 API
- 파일 저장 실패 시 불완전한 파일 정리

### 4. 데이터 백업

- 백업 필요 여부 판단
- 직원 데이터를 CSV 형식으로 생성
- 청크 단위 처리를 통한 대용량 데이터 처리
- 백업 상태 및 성공·실패 관리
- 스케줄 기반 및 수동 백업 실행
- 최근 백업 및 백업 목록 조회

### 5. 변경 이력 관리

- 직원 정보 변경 이력 기록
- `ChangeLog` 및 `ChangeLogDiff`를 통한 변경 이력 관리
- 변경 사항 비교 및 차이 계산
- 이력 목록, 상세 정보 및 건수 조회
- 최신 변경 시각 조회

### 6. 공통 시스템

- `BaseEntity`를 통한 생성·수정 시각 관리
- JPA Auditing 설정
- 공통 오류 응답 및 전역 예외 처리
- `ErrorCode` 및 `BusinessException` 기반 오류 관리
- QueryDSL 기반 동적 조회
- 커서 페이지네이션 공통 구조
- Swagger / OpenAPI API 문서화
- 클라이언트 IP 추출 유틸리티

---

## 팀원별 구현 기능

### 이준영

**프로젝트 기반 설정 및 개발 환경**
- Spring Boot 프로젝트 구성 및 공통 의존성 설정
- H2 데이터베이스 및 애플리케이션 설정
- QueryDSL 설정 및 `JPAQueryFactory` 빈 구성
- Swagger / OpenAPI 설정
- 프런트엔드 정적 리소스 및 CORS 연동 설정

**공통 시스템**
- `BaseEntity` 및 JPA Auditing 설정
- 공통 오류 응답 구조와 오류 코드 관리
- `BusinessException` 및 `GlobalExceptionHandler` 기반 예외 처리
- 커서 페이지네이션 공통 구조 및 가이드
- `ClientIpUtil` 및 테스트 코드 작성

**파일 관리**
- `FileStorage` 인터페이스 설계
- `LocalFileStorage` 구현
- `FileMeta` 엔티티 및 Repository 관련 작업
- 파일 업로드·다운로드·삭제 관련 기능
- 파일 저장 실패 시 불완전한 파일 정리 및 테스트 보완

**초기 데이터 및 문서화**
- `DummyDataInitializer` 구현
- 개발용 부서 5개 및 직원 20개 초기 데이터 생성
- API 연동 테스트를 위한 데이터 준비
- GitHub 저장소 및 협업 규칙 설정
- 이슈·PR 템플릿 구성
- README 및 프로젝트 문서 작성

### 이겸희

**직원 관리**
- `Employee` 엔티티 및 재직 상태 enum
- 직원 등록·상세 조회·수정·삭제 API
- 사원번호 자동 생성
- 직원 쓰기 테스트

**부서 관리**
- `Department` 엔티티
- 부서 등록·수정·삭제·상세 조회 API
- 부서 목록 조회 API
- 부서 관련 테스트

**프런트엔드 연동**
- 부서 및 직원 화면 연동 확인

### 이대원

**변경 이력 관리**
- `ChangeLog`, `ChangeLogDiff` 엔티티
- `ChangeLogService` 인터페이스 및 구현체
- 직원 서비스 연동을 통한 변경 이력 자동 기록
- 변경 사항 비교(diff) 계산 로직
- 최신 변경 시각 조회

**직원 조회 및 통계**
- 직원 목록 동적 필터링
- 직원 목록 정렬 및 커서 페이지네이션
- 직원 수 조회 API
- 직원 분포 및 추이 통계 API

**이력 조회 API**
- 이력 목록 조회 API(동적 쿼리 및 커서)
- 이력 상세 조회 및 건수 조회 API

**프런트엔드 연동**
- 대시보드 및 변경 이력 화면 연동 확인

### 박종우

**백업 설계 및 모델링**
- 백업 흐름 설계
- `Backup` 엔티티 및 상태 enum
- 백업 필요 여부 판단 로직

**백업 처리**
- 직원 CSV 청크 생성
- 대용량 데이터 처리를 고려한 메모리 사용량 관리
- 백업 성공·실패 처리
- 배치 스케줄러
- 수동 백업 API

**백업 조회 API**
- 최근 백업 조회 API
- 백업 목록 조회 API(동적 쿼리 및 커서)

**검증 및 연동**
- 진행 중 이력 선등록(STEP 2)
- 대용량·실패·동시 실행 관련 백업 테스트
- 백업 화면 연동 확인

### 공통 작업

- 요구사항 분석 및 작업 분담
- API 개발 및 프런트엔드 연동
- 테스트 코드 작성 및 통합 테스트
- 코드 리뷰 및 GitHub 협업
- 공통 문제 해결 및 기능 통합

---

## 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/gksrmf1210-droid/b-15---HR-Banck--team-3-.git
cd b-15---HR-Banck--team-3-
```

### 2. 애플리케이션 실행

Windows 환경에서 프로젝트 루트 디렉터리 기준으로 실행합니다.

```powershell
.\gradlew.bat bootRun
```

### 3. 테스트 실행

```powershell
.\gradlew.bat clean test
```

### 4. API 문서 확인

애플리케이션 실행 후 Swagger UI에 접속합니다.

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html#/`



---

## 데이터베이스

현재 개발 환경에서는 H2 데이터베이스를 사용합니다.

- **Database:** H2
- **Database URL:** `jdbc:h2:mem:hrbank`
- **H2 Console:** `/h2-console`


---

## 협업 규칙

### 시간
- 데일리 스크럼은 매일 10:00에 진행하며 15분 이내로 마무리합니다.
- 회의 불참 시 최소 하루 전에 공지합니다.

### 소통
- 혼자 2시간 이상 문제 해결이 어려우면 팀에 공유합니다.
- 질문할 때는 시도한 작업, 발생한 오류, 해결을 위해 시도한 방법을 함께 설명합니다.
- 메시지를 확인하면 이모지 등으로 반응합니다.

### 업무
- PR 등록 후 24시간 이내 코드 리뷰를 진행합니다.
- 코딩·커밋·PR 컨벤션을 준수합니다.
- 매일 스크럼에서 Task 보드를 최신 상태로 유지합니다.

### 의사결정
- 의견이 다르면 각자의 근거를 공유한 뒤 다수결로 결정합니다.
- 동점인 경우 해당 도메인 담당자가 결정합니다.
- 결정 사항은 회의록에 기록합니다.

---

## 향후 개선 사항

- 운영 환경 데이터베이스 연동
- 인증 및 권한 관리 강화
- API 테스트 자동화
- 파일 저장소 및 관리 기능 확장
- 배포 환경 구성 및 운영 모니터링

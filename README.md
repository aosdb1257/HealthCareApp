# HealthCareApp 🏥

스마트 헬스케어 시스템  
- 아두이노 기반 생체 센서와 안드로이드 앱, Spring Boot 백엔드 서버를 연동하여 사용자의 건강 데이터를 측정, 저장, 시각화하는 통합 플랫폼입니다.

---

## 📅 프로젝트 기간
- **2025.05.26 ~ 2025.06.26 (4주)**
![서비스 구성도](./images/develope_schedule.png)
  
## 📌 서비스 구성도

![서비스 구성도](./images/service_diagram.png)

---

## 💻 개발 환경

### 🧰 기술 스택

| 구분 | 기술 |
|------|------|
| **Frontend** | Android Studio |
| **Backend** | Spring Boot, Hibernate, Spring Legacy (MVC), Java 11, Servlet |
| **Database** | Oracle 11g XE, MySQL |
| **Infra** | AWS EC2 (Ubuntu), Amazon RDS |
| **API** | Kakao 지도 API, Google/Kakao/Naver 소셜 로그인 |
| **Tool** | GitHub, MobaXterm, MyBatis |

---

## ⚙️ 개발 기능

### ✅ 건강 데이터 측정 및 관측
- 맥박, 산소 포화도, 체온을 실시간 측정
- 측정 데이터 저장 및 시각화 (차트 기반 관측 화면 제공)

### ✅ 운동 추적 기능
- 자이로 센서를 활용한 걸음 수 측정
- GPS 기반 거리 측정
- 걸음 시간, 칼로리 소모량, 목표 걸음수/칼로리 제공

---

## 📦 시스템 구조

- **Local 개발 환경**에서 GitHub로 소스 코드 푸시
- AWS EC2에 Spring Boot 서버 및 MySQL 배포
- Android App에서 API 호출 → 건강 정보 저장/조회

---

## 🧑‍💻 팀원 및 역할 분담

| 이름 | 역할 |
|------|------|
| 최헌성 (팀장) | 아두이노 및 센서 연동, 보고서 작성 |
| 엄태원 | 프론트엔드 개발, 자료조사 |
| 황도균 | 백엔드 서버 개발, 기술 조사, API 연동 |

---

## 💡 핵심 구현

| 기능 | 설명 |
|------|------|
| 센서 연동 | 아두이노 + 체온/맥박/산소 센서 연동 |
| 데이터 저장 | Spring Boot → Hibernate → MySQL 연동 |
| 운동 추적 | GPS 및 자이로 기반 운동 분석 기능 |
| API 통신 | RESTful API로 앱 ↔ 서버 간 통신 |
| 배포 | GitHub → AWS EC2 배포 자동화 |

---

## 🔧 개선 예정 사항

- [ ] Firebase 연동을 통한 실시간 알림 기능
- [ ] 사용자 건강 이력 기반 통계/추천 알고리즘 적용
- [ ] UI/UX 디자인 고도화 및 다크 모드 대응

---

## 📁 GitHub Repository

🔗 [https://github.com/aosdb1257/HealthCareApp.git](https://github.com/aosdb1257/HealthCareApp.git)

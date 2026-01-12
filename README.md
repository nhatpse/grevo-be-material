```
grevo-be-material/
├── src/
│   ├── main/
│   │   ├── java/org/grevo/grevobematerial/
│   │   │   ├── config/              # Cấu hình ứng dụng
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   ├── ChangePasswordRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   └── UpdateProfileRequest.java
│   │   │   │   └── response/
│   │   │   │       └── AuthResponse.java
│   │   │   ├── entity/              # JPA Entities (16 entities)
│   │   │   │   ├── Users.java
│   │   │   │   ├── Citizens.java
│   │   │   │   ├── Enterprise.java
│   │   │   │   ├── Collectors.java
│   │   │   │   ├── WasteReports.java
│   │   │   │   ├── WasteTypes.java
│   │   │   │   ├── ServiceAreas.java
│   │   │   │   ├── ReportLifecycle.java
│   │   │   │   ├── StatusHistory.java
│   │   │   │   ├── Feedback.java
│   │   │   │   ├── FeedbackImage.java
│   │   │   │   ├── WasteReportImage.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── PointRules.java
│   │   │   │   ├── Rewards.java
│   │   │   │   ├── EnterpriseArea.java
│   │   │   │   └── enums/
│   │   │   │       └── Role.java
│   │   │   ├── repository/          # JPA Repositories (16 repositories)
│   │   │   ├── security/            # Security components
│   │   │   │   ├── JwtFilter.java
│   │   │   │   └── JwtUtils.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── UserService.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   └── impl/
│   │   │   │       ├── AuthService.java
│   │   │   │       └── UserServiceImpl.java
│   │   │   └── GrevoBeMaterialApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── uploads/                         # Thư mục lưu file upload
├── Dockerfile                       # Docker configuration
├── pom.xml                         # Maven dependencies
└── ERD_JSON.json                   # Database schema diagram
```

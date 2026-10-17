# Hệ Thống Quản Lý Đào Tạo & Sinh Viên (Course & Student Management System)

> Dự án RESTful API hoàn chỉnh được phát triển bằng **Java 17**, **Spring Boot 3.5.3**, **Spring Security 6**, **JWT (JSON Web Token)**, **Spring Data JPA / Hibernate 6.6**, cơ sở dữ liệu **MySQL 8.x**.
> 
> Dự án đã hoàn thành trọn vẹn 2 nhiệm vụ cốt lõi theo yêu cầu:
> 1. **Kế thừa & áp dụng toàn diện Unified `HibernateDao<T>`** cho tất cả các đối tượng trong hệ thống (`Student`, `Department`, `ClassInfo`, `Document`, `AppDetail`), chuẩn hóa công cụ lọc động (Dynamic Specification Parser), phân trang an toàn (Pagination Bounds) và bảo vệ sắp xếp (Sort Whitelist).
> 2. **Hệ thống Bảo mật & Phân quyền chuẩn Doanh nghiệp (Role-Based Access Control - RBAC)** tích hợp Spring Security Stateless + JWT Bearer Token, kiểm soát truy cập đa tầng (URL Security FilterChain + Method Security `@PreAuthorize`) với 3 vai trò: `ROLE_ADMIN`, `ROLE_LECTURER`, `ROLE_STUDENT`.

---

## 1. Tổng Quan Kiến Trúc Hệ Thống

```
com.example.studentmanagement/
├── config/
│   └── DataInitializer.java           # Khởi tạo sẵn tài khoản Admin, Giảng viên, Sinh viên
├── dto/
│   ├── ApiResponse.java               # Chuẩn wrapper JSON response thống nhất toàn hệ thống
│   ├── PagedResponse.java             # Metadata phân trang (content, page, size, totalPages,...)
│   ├── SearchCriteria.java            # Cấu trúc tiêu chí tìm kiếm (key, operation, value)
│   ├── LoginRequest.java              # Request đăng nhập
│   ├── RegisterRequest.java           # Request đăng ký tài khoản
│   ├── JwtResponse.java               # Response chứa token JWT và thông tin roles
│   └── UserDto.java                   # DTO người dùng & danh sách quyền
├── entity/
│   ├── RoleType.java                  # Enum quyền: ROLE_ADMIN, ROLE_LECTURER, ROLE_STUDENT
│   ├── User.java                      # Entity người dùng (username, password BCrypt, email, enabled, roles)
│   ├── Student.java                   # Entity Sinh viên & các Named Query
│   ├── Department.java                # Entity Khoa đào tạo
│   ├── ClassInfo.java                 # Entity Lớp học
│   ├── Document.java                  # Entity Tài liệu đính kèm
│   └── AppDetail.java                 # Entity Chi tiết ứng dụng
├── HibernateDao/
│   ├── HibernateDao.java              # CỐT LÕI: Engine lọc động, phân trang, sort whitelist
│   ├── IGenericDao.java               # Interface Generic DAO mở rộng với filter & getHibernateDao
│   ├── HibernateGenericDao.java       # Base Generic DAO tích hợp EntityManager & HibernateDao
│   ├── StudentHibernateDao.java       # Kế thừa HibernateDao<Student> (whitelist: name, dob, department.name)
│   ├── DepartmentHibernateDao.java    # Kế thừa HibernateDao<Department> (whitelist: name, description)
│   ├── ClassInfoHibernateDao.java     # Kế thừa HibernateDao<ClassInfo> (whitelist: id, name, description)
│   ├── DocumentHibernateDao.java      # Kế thừa HibernateDao<Document> (whitelist: name, appId, owner,...)
│   └── AppDetailHibernateDao.java     # Kế thừa HibernateDao<AppDetail> (whitelist: appId, appName,...)
├── Dao/
│   ├── IStudentDao.java & impl/StudentDaoImpl.java
│   ├── IDepartmentDao.java & impl/DepartmentDaoImpl.java
│   ├── IClassInfoDao.java & impl/ClassInfoDaoImpl.java
│   ├── IDocumentDao.java & impl/DocumentDaoImpl.java
│   └── IAppDetailDao.java & impl/AppDetailDaoImpl.java
├── repository/
│   ├── UserRepository.java            # Spring Data JPA Repository quản lý tài khoản User
│   ├── StudentRepository.java         # JpaRepository & JpaSpecificationExecutor
│   └── DepartmentRepository.java
├── security/
│   ├── WebSecurityConfig.java         # Cấu hình SecurityFilterChain, BCrypt, Stateless, CORS, MethodSecurity
│   ├── jwt/
│   │   ├── JwtUtils.java              # Tạo, ký số và thẩm định tính hợp lệ của JWT token
│   │   ├── AuthTokenFilter.java       # Filter chặn mỗi request để trích xuất Bearer Token
│   │   ├── AuthEntryPointJwt.java     # Xử lý phản hồi lỗi 401 Unauthorized theo chuẩn ApiResponse
│   │   └── CustomAccessDeniedHandler.java # Xử lý phản hồi lỗi 403 Forbidden theo chuẩn ApiResponse
│   └── service/
│       ├── UserDetailsImpl.java       # Chuyển đổi User Entity sang Spring Security UserDetails
│       └── UserDetailsServiceImpl.java# Nạp thông tin người dùng từ UserRepository
├── service/
│   ├── StudentService.java & impl/StudentServiceImpl.java
│   ├── DepartmentService.java & impl/DepartmentServiceImpl.java
│   ├── ClassInfoService.java & impl/ClassInfoServiceImpl.java
│   ├── DocumentService.java & impl/DocumentServiceImpl.java
│   └── AppDetailService.java & impl/AppDetailServiceImpl.java
└── rest/
    ├── AuthController.java            # /api/auth/login, /api/auth/register, /api/auth/me
    ├── AdminController.java           # /api/admin/users, /roles, /status (chỉ ROLE_ADMIN)
    ├── StudentController.java         # CRUD & /api/students/filter
    ├── DepartmentController.java      # CRUD & /api/departments/filter, /search
    ├── ClassInfoRestImpl.java         # CRUD & /api/class-info/filter
    └── DocumentController.java        # Upload, Download, Delete & /api/documents/filter, /search
```

---

## 2. Nhiệm Vụ 1: Kế Thừa & Ứng Dụng Toàn Diện `HibernateDao<T>`

Lớp `com.example.studentmanagement.HibernateDao.HibernateDao` cung cấp giải pháp thống nhất toàn diện:
- **Parse điều kiện lọc động**: Hỗ trợ cú pháp tìm kiếm linh hoạt `key:value` (LIKE), `key>value` (Lớn hơn), `key<value` (Nhỏ hơn), `key!value` (Khác), liên kết nhiều tiêu chí bằng dấu phẩy `,` (AND logic), hỗ trợ liên kết bảng (Join Specification như `department.name:CNTT`).
- **Kiểm soát phân trang (Pagination Bounds)**: Giới hạn số trang hợp lệ (`page >= 0`), kích thước trang (`1 <= size <= 100`, chống tấn công quá tải bộ nhớ).
- **Bảo mật sắp xếp (Sort Whitelist)**: Chỉ cho phép sắp xếp theo danh sách các trường được khai báo whitelist cụ thể trên từng Entity, chặn hoàn toàn nguy cơ SQL / Property Injection.

### 2.1. Cấu trúc kế thừa trực tiếp:
Các lớp con kế thừa `HibernateDao<T>` và đăng ký làm Spring Service/Component:
- **`StudentHibernateDao extends HibernateDao<Student>`**:
  - Whitelist Search: `id`, `name`, `dob`, `department.name`
  - Whitelist Sort: `id`, `name`, `dob`
- **`DepartmentHibernateDao extends HibernateDao<Department>`**:
  - Whitelist Search: `id`, `name`, `description`
  - Whitelist Sort: `id`, `name`, `description`
- **`ClassInfoHibernateDao extends HibernateDao<ClassInfo>`**:
  - Whitelist Search: `id`, `name`, `description`
  - Whitelist Sort: `id`, `name`
- **`DocumentHibernateDao extends HibernateDao<Document>`**:
  - Whitelist Search: `id`, `name`, `appId`, `owner`, `path`, `version`, `deleted`, `active`, `creationTime`
  - Whitelist Sort: `id`, `name`, `appId`, `owner`, `version`, `creationTime`, `modificationTime`, `active`, `deleted`
- **`AppDetailHibernateDao extends HibernateDao<AppDetail>`**:
  - Whitelist Search: `id`, `appId`, `appName`, `description`
  - Whitelist Sort: `id`, `appId`, `appName`

### 2.2. Tích hợp Generic DAO:
Interface `IGenericDao<T, ID>` và lớp triển khai `HibernateGenericDao<T, ID>` được mở rộng thêm các hàm:
- `PagedResponse<T> filter(String search, Integer page, Integer size, List<String> sortParams)`
- `PagedResponse<T> filter(Specification<T> spec, Pageable pageable)`
- `HibernateDao<T> getHibernateDao()`

Nhờ đó, tất cả các DAO (`StudentDaoImpl`, `DepartmentDaoImpl`, `ClassInfoDaoImpl`, `DocumentDaoImpl`, `AppDetailDaoImpl`) đều có thể tận dụng engine lọc động thống nhất này.

---

## 3. Nhiệm Vụ 2: Hệ Thống Phân Quyền Toàn Diện (RBAC + JWT)

Dự án thiết lập hệ thống bảo mật theo mô hình **Stateless Security** dựa trên **JSON Web Token (JWT)** và thuật toán mã hóa mật khẩu một chiều **BCrypt**.

### 3.1. Các vai trò trong hệ thống (Roles)
1. **`ROLE_ADMIN` (Quản trị viên)**:
   - Toàn quyền quản trị hệ thống.
   - Quản lý tài khoản người dùng và phân quyền (`/api/admin/**`).
   - Thêm, sửa, xóa Khoa đào tạo (`POST`, `PUT`, `DELETE` `/api/departments/**`).
   - Xóa sinh viên (`DELETE /api/students/{id}`).
   - Xóa tài liệu (`DELETE /api/documents/{id}`).
2. **`ROLE_LECTURER` (Giảng viên)**:
   - Thêm mới và cập nhật hồ sơ sinh viên (`POST`, `PUT` `/api/students/**`).
   - Tải lên tài liệu giảng dạy (`POST /api/documents`).
   - Xem, tìm kiếm và tải về tài liệu giảng dạy.
   - Không được phân quyền người dùng, không được xóa sinh viên hoặc thay đổi cơ cấu khoa.
3. **`ROLE_STUDENT` (Sinh viên)**:
   - Tra cứu danh sách, xem chi tiết sinh viên, khoa, lớp học.
   - Tìm kiếm, lọc động và tải về tài liệu học tập (`GET /api/documents/**`, `/download`).
   - Xem hồ sơ cá nhân (`GET /api/auth/me`).
   - Không được thực hiện các thao tác ghi dữ liệu (POST, PUT, DELETE).

### 3.2. Ma trận phân quyền chi tiết (RBAC Matrix)

| Endpoint | Method | Vai trò được phép | Ghi chú |
| :--- | :---: | :--- | :--- |
| `/api/auth/login` | `POST` | Tất cả (Public) | Đăng nhập nhận JWT Token |
| `/api/auth/register` | `POST` | Tất cả (Public) | Đăng ký tài khoản mới |
| `/api/auth/me` | `GET` | `ADMIN`, `LECTURER`, `STUDENT` | Lấy thông tin tài khoản hiện tại |
| `/api/admin/**` | `GET`, `PUT` | `ROLE_ADMIN` | Quản lý danh sách User, cấp Role, bật/tắt kích hoạt |
| `/api/departments` (Tạo, Sửa, Xóa) | `POST`, `PUT`, `DELETE` | `ROLE_ADMIN` | Chỉ Admin được sửa đổi cơ cấu khoa |
| `/api/students` (Thêm, Sửa) | `POST`, `PUT` | `ADMIN`, `LECTURER` | Giảng viên & Admin được thêm/sửa sinh viên |
| `/api/students/{id}` (Xóa) | `DELETE` | `ROLE_ADMIN` | Chỉ Admin được phép xóa sinh viên |
| `/api/documents` (Tải lên) | `POST` | `ADMIN`, `LECTURER` | Đăng tải tài liệu giảng dạy/hồ sơ |
| `/api/documents/{id}` (Xóa) | `DELETE` | `ROLE_ADMIN` | Chỉ Admin được xóa tài liệu |
| Tra cứu (`/filter`, `/search`, `GET`) | `GET` | Công khai / Toàn bộ Roles | Xem, tìm kiếm, lọc động, tải tài liệu |

### 3.3. Tài khoản demo được tạo sẵn tự động:
Hệ thống sử dụng `DataInitializer.java` tự động khởi tạo 3 tài khoản mẫu ngay khi ứng dụng chạy:

| Tên đăng nhập (Username) | Mật khẩu (Password) | Vai trò (Role) | Mục đích demo |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | `ROLE_ADMIN` | Test toàn quyền quản trị và phân quyền |
| **`lecturer`** | `lecturer123` | `ROLE_LECTURER` | Test quyền giảng viên (thêm/sửa sinh viên, upload tài liệu) |
| **`student`** | `student123` | `ROLE_STUDENT` | Test quyền sinh viên (tra cứu, tải file) |

---

## 4. Danh Sách REST API Chi Tiết

### 4.1. Xác thực & Tài khoản (`/api/auth/**`)
- `POST /api/auth/login`: Đăng nhập với username & password, trả về JWT Token và roles.
- `POST /api/auth/register`: Đăng ký tài khoản người dùng mới.
- `GET /api/auth/me`: Lấy thông tin user đăng nhập từ JWT Token (yêu cầu Header: `Authorization: Bearer <token>`).

### 4.2. Quản trị Phân quyền (`/api/admin/**` - Yêu cầu `ROLE_ADMIN`)
- `GET /api/admin/users`: Lấy danh sách toàn bộ người dùng trong hệ thống kèm vai trò và trạng thái.
- `PUT /api/admin/users/{id}/roles`: Cập nhật vai trò cho người dùng (Body: `["ROLE_ADMIN", "ROLE_LECTURER"]`).
- `PUT /api/admin/users/{id}/status?enabled=true`: Kích hoạt hoặc khóa tài khoản người dùng.

### 4.3. Lọc động, Phân trang & Sắp xếp với `HibernateDao`
- **Sinh viên**:
  - `GET /api/students/filter?search=name:Nguyen,dob>2002-01-01&page=0&size=10&sort=name,asc`
  - `GET /api/students/filter?search=department.name:Computer` (Join Specification)
- **Khoa**:
  - `GET /api/departments/filter?search=name:Tech&page=0&size=10&sort=name,asc`
  - `GET /api/departments/search?search=name:Tech` (Alias)
- **Lớp học**:
  - `GET /api/class-info/filter?search=name:Java&page=0&size=10&sort=id,asc`
- **Tài liệu**:
  - `GET /api/documents/filter?search=name:Report,version:1&page=0&size=10&sort=creationTime,desc`
  - `GET /api/documents/search?search=appId:app-001` (Alias)

---

## 5. Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 5.1. Yêu cầu môi trường
- **Java**: JDK 17 trở lên (`java -version`).
- **Maven**: 3.8+ (`mvn -version`).
- **MySQL**: 8.0+ đang chạy tại cổng `3306` (database `educaze`, user `root`, password `123456`).

### 5.2. Chạy ứng dụng
```bash
# Di chuyển vào thư mục dự án
cd D:\course-management-system-main

# Chạy ứng dụng qua Spring Boot Maven Plugin
mvn spring-boot:run
```
Ứng dụng sẽ khởi chạy tại: `http://localhost:8080`.

### 5.3. Chạy toàn bộ 199 Unit & Integration Tests
Dự án được bảo vệ bởi **199 bài kiểm thử tự động**, bao gồm đầy đủ các kịch bản kiểm tra kế thừa `HibernateDao`, JWT, Spring Security RBAC và REST Controllers:
```bash
mvn test
```
**Kết quả cam kết:**
```
[INFO] Results:
[INFO] Tests run: 199, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 6. Hướng Dẫn Kiểm Thử Bằng Postman Collection

Toàn bộ các API và kịch bản kiểm thử tự động đã được tích hợp đầy đủ trong **1 file JSON duy nhất**:
📁 **`Course_Management_System_Complete.postman_collection.json`**

### 6.1. Hướng Dẫn Kiểm Thử Tự Động 100% (1-Click Run - Khuyên dùng khi báo cáo cho Thầy)
1. Mở phần mềm **Postman**.
2. Nhấn vào nút **Import** (ở góc trên bên trái) -> Chọn file `Course_Management_System_Complete.postman_collection.json`.
3. Bạn sẽ thấy Collection: **`Course & Student Management System - Complete API & AutoTest Collection`**.
4. **Cách chạy tự động toàn bộ hệ thống**:
   - Nhấn vào tên Collection -> Click vào nút **Run** (biểu tượng Play).
   - Chọn thư mục **`0. 🚀 AUTO-TEST SUITE (Chạy Tự Động 100% RBAC & HibernateDao)`** (hoặc để mặc định toàn bộ).
   - Nhấn **Run Course & Student Management System...**.
5. **Kết quả**:
   - Postman sẽ tự động gửi tuần tự 24 kịch bản kiểm thử (tự động lấy token JWT, tự kiểm tra quyền 200/403, tự kiểm tra lọc động và validate 400).
   - Toàn bộ kết quả hiển thị **PASS 100% (Màu xanh)**.

---

### 6.2. Cấu Trúc Các Thư Mục API Trong Collection
Ngoài bộ Auto-Test, collection còn cung cấp đầy đủ từng API riêng lẻ để bạn tùy chỉnh và gửi thủ công:
- **`0. 🚀 AUTO-TEST SUITE`**: 24 kịch bản tự động kiểm tra RBAC + HibernateDao từ A-Z.
- **`1. Authentication & Authorization (JWT & Roles)`**: Đăng nhập Admin, Giảng viên, Sinh viên, Đăng ký, Lấy profile (`/api/auth/me`).
- **`2. Departments`**: CRUD Khoa và lọc động qua HibernateDao (`/api/departments/filter`, `/search`).
- **`3. Students - Basic CRUD`**: CRUD Sinh viên cơ bản.
- **`4. Entity Named Queries`**: Kiểm thử các truy vấn Named Query trong Entity.
- **`5. Filter, Pagination & Sorting`**: 14 kịch bản kiểm thử lọc động chi tiết cho Sinh viên.
- **`6. Document Filter & Pagination (HibernateDao)`**: Tải lên, tải về, xóa mềm và 14 kịch bản lọc tài liệu.
- **`7. Admin User Management (RBAC)`**: Quản trị người dùng, cấp vai trò, khóa/mở tài khoản.
- **`8. ClassInfo Filter & Pagination (HibernateDao)`**: Lọc động và phân trang Lớp học.



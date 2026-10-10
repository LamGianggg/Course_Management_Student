# Dự án Quản lý Sinh viên & Khoa (Course & Student Management System)

Dự án REST API được xây dựng bằng **Java 17**, **Spring Boot 3.5.3**, **Spring Data JPA**, cơ sở dữ liệu **MySQL 8.x**. Dự án đã được triển khai và hoàn thiện đầy đủ 3 tính năng cốt lõi theo chuẩn Spring Data:
1. **REST Query Language with Spring Data JPA Specifications**
2. **Pagination using Spring Data JPA**
3. **Sorting Query Results with Spring Data**

Đồng thời đi kèm sẵn file **Postman Collection v2.1** hoàn chỉnh để kiểm thử trực quan tất cả các kịch bản.

---

## 1. Công nghệ & Môi trường sử dụng

| Công nghệ | Phiên bản | Ghi chú |
| :--- | :--- | :--- |
| **Java** | OpenJDK 17 (LTS) | Biên dịch & tương thích tốt nhất |
| **Spring Boot** | 3.5.3 | Quản lý dependency & REST Web services |
| **Spring Data JPA** | Hibernate 6.6.x | ORM & dynamic query execution |
| **Database** | MySQL 8.x | Cơ sở dữ liệu quan hệ lưu trữ dữ liệu |
| **Build Tool** | Apache Maven 3.9+ | Quản lý vòng đời build & dependencies |
| **Testing** | JUnit 5 & Spring MockMvc | Kiểm thử tự động tầng Service, Repo & Web |
| **API Testing Tool** | Postman Collection v2.1 | Cung cấp sẵn các request mẫu |

---

## 2. Cấu trúc Database & Cấu hình kết nối

### 2.1. Cấu hình `src/main/resources/application.properties`
```properties
server.port=8080

# Cấu hình kết nối MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/educaze?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA & Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> **Lưu ý**: Trước khi chạy ứng dụng lần đầu, hãy đảm bảo MySQL Service đã bật và database `educaze` đã được tạo (nếu chưa, mở MySQL CLI chạy `CREATE DATABASE IF NOT EXISTS educaze;`).

### 2.2. Database Schema chính
- **Bảng `departments`**:
  - `id` (BIGINT, Primary Key, Auto Increment)
  - `name` (VARCHAR, Tên khoa)
  - `description` (VARCHAR, Mô tả)
- **Bảng `students`**:
  - `id` (BIGINT, Primary Key, Auto Increment)
  - `name` (VARCHAR, Họ và tên)
  - `dob` (DATE, Ngày sinh)
  - `department_id` (BIGINT, Foreign Key liên kết `departments.id`)

---

## 3. Kiến trúc Triển khai 3 Tính Năng Cốt Lõi

Dự án áp dụng mô hình phân lớp chuẩn công nghiệp (Clean Architecture):

```
com.example.studentmanagement/
├── dto/
│   ├── ApiResponse.java             # Chuẩn wrapper response JSON đồng nhất
│   ├── PagedResponse.java           # DTO metadata phân trang (content, page, totalPages,...)
│   └── SearchCriteria.java          # Model chứa điều kiện lọc (key, operation, value)
├── entity/
│   ├── Department.java              # Entity Khoa
│   └── Student.java                 # Entity Sinh viên (chứa các Named Query)
├── specification/
│   ├── StudentSpecification.java    # Chuyển SearchCriteria thành JPA Criteria Predicate
│   └── StudentSpecificationBuilder.java # Parser chuỗi truy vấn sang Specification (AND logic)
├── HibernateDao/
│   ├── HibernateDao.java            # Unified Engine: parse Specification, validate Pagination bounds & Sort whitelist
│   ├── HibernateGenericDao.java     # Base Generic DAO với JPA EntityManager operations
│   └── GenericDao.java              # Generic DAO interface
├── Dao/
│   ├── IDocumentDao.java            # Document DAO interface kế thừa JpaSpecificationExecutor & HibernateDao
│   └── impl/DocumentDaoImpl.java    # Triển khai Document DAO tích hợp HibernateDao<Document>
├── repository/
│   ├── DepartmentRepository.java
│   └── StudentRepository.java       # Kế thừa JpaRepository & JpaSpecificationExecutor
├── service/
│   ├── DocumentService.java
│   ├── impl/DocumentServiceImpl.java# Nghiệp vụ tài liệu tích hợp filterDocuments qua HibernateDao
│   ├── StudentService.java
│   └── impl/StudentServiceImpl.java # Thực thi truy vấn động ở database-level
└── rest/
    ├── DocumentController.java      # Endpoints /api/documents/filter, /search, upload, download, delete
    ├── StudentController.java       # Endpoint GET /api/students/filter
    └── DepartmentController.java
```

### 3.1. REST Query Language với JPA Specification
- **File thành phần**: `SearchCriteria.java`, `StudentSpecification.java`, `StudentSpecificationBuilder.java`.
- **Cơ chế hoạt động**:
  - `key:value` : Tìm kiếm chuỗi có chứa từ khóa (LIKE `%value%`, không phân biệt hoa thường).
  - `key>value` : Lớn hơn (áp dụng cho ngày tháng `LocalDate`, số `Long`).
  - `key<value` : Nhỏ hơn.
  - `key!value` : Phép so sánh khác (Not Equal).
  - Dấu phẩy `,` liên kết các điều kiện bằng logic `AND`.
  - Hỗ trợ **Join Specification**: `department.name:CNTT` lọc theo tên khoa thông qua quan hệ bảng.
  - **Bảo mật Whitelist**: Chỉ cho phép tìm kiếm trên các trường hợp lệ (`name`, `dob`, `department.name`), tránh injection.

### 3.2. Phân trang (Pagination)
- **File thành phần**: `PagedResponse.java`, `Pageable`, `PageRequest.of(page, size, sort)`.
- **Tham số**:
  - `page`: Số trang (0-indexed, mặc định: 0).
  - `size`: Kích thước trang (mặc định: 10, giới hạn từ 1 đến 100).
- **Metadata trả về**: `page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `content`.

### 3.3. Sắp xếp (Sorting)
- **Cơ chế**: Sử dụng Spring Data `Sort.by(...)` với danh sách `Sort.Order`.
- **Cú pháp hỗ trợ linh hoạt**:
  - Đơn tiêu chí: `sort=name,asc` hoặc `sort=dob,desc`.
  - Đa tiêu chí: `sort=name,asc&sort=dob,desc`.
  - **Bảo mật Whitelist**: Chỉ cho phép sắp xếp theo `id`, `name`, `dob`. Báo lỗi `400 Bad Request` nếu truyền trường lạ.

---

## 4. Danh sách REST API Endpoints

### 4.1. Endpoint Bộ Lọc Động, Phân Trang & Sắp Xếp (Trọng tâm)
| Method | Endpoint | Query Parameters | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/students/filter` | `search`, `page`, `size`, `sort` | Lọc động theo Specification, phân trang và sắp xếp kết quả |

**Ví dụ các URL mẫu:**
- **Tìm kiếm theo tên**:
  `GET /api/students/filter?search=name:Nguyen`
- **Lọc sinh viên sinh sau ngày chỉ định**:
  `GET /api/students/filter?search=dob>2003-01-01`
- **Lọc kết hợp nhiều điều kiện (AND)**:
  `GET /api/students/filter?search=name:Van,dob>2002-01-01`
- **Lọc theo tên Khoa (Join Specification)**:
  `GET /api/students/filter?search=department.name:Computer`
- **Phân trang trang 0, lấy 5 bản ghi**:
  `GET /api/students/filter?page=0&size=5`
- **Sắp xếp theo tên tăng dần**:
  `GET /api/students/filter?sort=name,asc`
- **Kết hợp đồng thời Tìm kiếm + Phân trang + Sắp xếp**:
  `GET /api/students/filter?search=dob>2002-01-01&page=0&size=5&sort=name,asc`

### 4.2. Quản lý Sinh viên cơ bản (CRUD)
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/students` | Lấy toàn bộ danh sách sinh viên |
| `GET` | `/api/students/{id}` | Lấy chi tiết sinh viên theo ID |
| `POST` | `/api/students` | Thêm mới sinh viên |
| `PUT` | `/api/students/{id}` | Cập nhật thông tin sinh viên |
| `DELETE` | `/api/students/{id}` | Xóa sinh viên |
| `GET` | `/api/students/department/{deptId}` | Lấy danh sách sinh viên theo Khoa |

### 4.3. Quản lý Khoa (Departments)
| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/departments` | Lấy tất cả các khoa |
| `GET` | `/api/departments/{id}` | Lấy thông tin khoa theo ID |
| `POST` | `/api/departments` | Tạo mới khoa |
| `PUT` | `/api/departments/{id}` | Cập nhật khoa |
| `DELETE` | `/api/departments/{id}` | Xóa khoa |

### 4.4. Quản lý Tài liệu (Documents) Tích hợp HibernateDao
| Method | Endpoint | Query Parameters | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/documents/filter` | `search`, `page`, `size`, `sort` | Lọc động Specification, phân trang & sắp xếp qua HibernateDao |
| `GET` | `/api/documents/search` | `search`, `page`, `size`, `sort` | Endpoint alias tương đương của `/api/documents/filter` |
| `POST` | `/api/documents` | Multipart `files`, `appId` | Tải lên một hoặc nhiều tài liệu gắn với appId |
| `GET` | `/api/documents/{id}` | Path variable `id` | Lấy thông tin metadata tài liệu |
| `GET` | `/api/documents/{id}/download` | Path variable `id` | Tải về file vật lý của tài liệu |
| `DELETE` | `/api/documents/{id}` | Path variable `id` | Xóa mềm tài liệu (active = false, deleted = true) |

**Cấu hình bảo mật & Whitelist cho Document:**
- **Trường tìm kiếm cho phép (`search`)**: `id`, `name`, `appId`, `owner`, `path`, `version`, `deleted`, `active`, `creationTime`
- **Trường sắp xếp cho phép (`sort`)**: `id`, `name`, `appId`, `owner`, `version`, `creationTime`, `modificationTime`, `active`, `deleted`
- **Hành vi mặc định**: Sắp xếp `creationTime` giảm dần (`DESC`), `page = 0`, `size = 10`.

**Ví dụ các URL mẫu cho Tài liệu:**
- **Tìm kiếm theo tên tài liệu**:
  `GET /api/documents/filter?search=name:Project`
- **Lọc theo mã ứng dụng (appId)**:
  `GET /api/documents/filter?search=appId:app-001`
- **Lọc theo phiên bản (version)**:
  `GET /api/documents/filter?search=version:1`
- **Lọc theo ngày tạo (creationTime)**:
  `GET /api/documents/filter?search=creationTime>2026-01-01`
- **Kết hợp nhiều điều kiện AND**:
  `GET /api/documents/filter?search=name:Design,version:2`
- **Phân trang & sắp xếp**:
  `GET /api/documents/filter?page=0&size=5&sort=name,asc`
- **Sắp xếp đa trường**:
  `GET /api/documents/filter?sort=creationTime,desc&sort=name,asc`

---

## 5. Hướng dẫn Khởi chạy Dự án

### 5.1. Khởi chạy bằng Dòng lệnh (Terminal / PowerShell)
1. Di chuyển vào thư mục dự án:
   ```bash
   cd D:\course-management-system-main
   ```
2. Khởi động ứng dụng Spring Boot:
   ```bash
   mvn spring-boot:run
   ```
3. Ứng dụng sẽ khởi chạy tại: `http://localhost:8080`.

### 5.2. Khởi chạy bằng IntelliJ IDEA / Eclipse
1. Mở IDE -> Chọn **Open** -> Trỏ tới thư mục `D:\course-management-system-main`.
2. Chờ Maven tải và đồng bộ xong các thư viện.
3. Tìm đến file `src/main/java/com/example/studentmanagement/StudentManagementApplication.java`.
4. Nhấn chuột phải -> Chọn **Run 'StudentManagementApplication'**.

### 5.3. Chạy Kiểm thử Tự động (Tests)
Chạy toàn bộ 77 test case (bao gồm HibernateDao, Document Filter/Pagination/Sorting, Student Specification & Controller tests):
```bash
mvn test
```

---

## 6. Hướng dẫn Kiểm thử Chi tiết bằng Postman (Step-by-step)

File Postman Collection được chuẩn bị sẵn tại thư mục gốc của dự án:
📁 **`Student_Management_System.postman_collection.json`**

### Bước 1: Import Collection vào Postman
1. Mở ứng dụng **Postman**.
2. Nhấn vào nút **Import** (ở góc trên cùng bên trái cạnh logo Postman).
3. Chọn thẻ **Files** hoặc kéo-thả trực tiếp file `Student_Management_System.postman_collection.json` vào giao diện Postman.
4. Nhấn **Import** để hoàn tất. Bạn sẽ thấy collection mang tên:
   **`Student Management System API`**.

### Bước 2: Kiểm tra biến môi trường `baseUrl`
- Collection đã cài đặt sẵn biến `baseUrl` = `http://localhost:8080`.
- Nếu bạn đổi cổng chạy server, bạn có thể click vào Collection `Student Management System API` -> chọn tab **Variables** -> sửa giá trị cột **Current Value** của `baseUrl`.

### Bước 3: Chuẩn bị dữ liệu mẫu (Khuyến nghị thực hiện trước khi test lọc)
1. Mở thư mục **`1. Departments`**:
   - Chạy request `Create Department` 2 lần để tạo ít nhất 2 khoa (ví dụ: Khoa CNTT và Khoa Toán).
2. Mở thư mục **`2. Students - Basic CRUD`**:
   - Chạy request `Create Student` từ 4 đến 5 lần với các thông tin tên và ngày sinh (`dob`) khác nhau để có tập dữ liệu phong phú (ví dụ các năm sinh 2001, 2002, 2003, 2004).

### Bước 4: Kiểm thử thư mục `4. Filter, Pagination & Sorting`
Thư mục này gồm **14 request mẫu được cấu hình sẵn 100%**, bao gồm:

| STT | Tên Request trong Postman | Mục đích kiểm thử | Query String mẫu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `1. Filter by Name (LIKE search)` | Tìm kiếm tên gần đúng (LIKE) | `?search=name:Nguyen` | Danh sách sinh viên có tên chứa "Nguyen" |
| **2** | `2. Filter by DOB > Date` | Lọc sinh viên sinh sau một ngày | `?search=dob>2003-01-01` | Các sinh viên có ngày sinh > 2003-01-01 |
| **3** | `3. Filter by DOB < Date` | Lọc sinh viên sinh trước một ngày | `?search=dob<2004-01-01` | Các sinh viên có ngày sinh < 2004-01-01 |
| **4** | `4. Filter by Department Name` | Lọc qua tên Khoa (Join bảng) | `?search=department.name:Computer` | Các sinh viên thuộc khoa chứa chữ "Computer" |
| **5** | `5. Multi-criteria Filter` | Kết hợp nhiều điều kiện AND | `?search=name:Van,dob>2002-01-01` | Vừa có tên chứa "Van" vừa sinh sau 2002-01-01 |
| **6** | `6. Pagination - Page 0, Size 5` | Lấy trang đầu tiên | `?page=0&size=5` | Tối đa 5 bản ghi, kèm `page: 0`, `first: true` |
| **7** | `7. Pagination - Page 1, Size 5` | Lấy trang kế tiếp | `?page=1&size=5` | 5 bản ghi tiếp theo, `page: 1` |
| **8** | `8. Sorting - Sort by Name ASC` | Sắp xếp tên từ A đến Z | `?sort=name,asc` | Danh sách sắp xếp theo `name` tăng dần |
| **9** | `9. Sorting - Sort by DOB DESC` | Sắp xếp ngày sinh giảm dần | `?sort=dob,desc` | Sinh viên trẻ tuổi nhất nằm ở đầu danh sách |
| **10** | `10. Multi-field Sorting` | Sắp xếp nhiều cột cùng lúc | `?sort=name,asc&sort=dob,desc` | Ưu tiên tên tăng dần, trùng tên thì xếp ngày sinh |
| **11** | `11. Combined - All Features` | Kết hợp cả Lọc + Phân trang + Sắp xếp | `?search=dob>2002-01-01&page=0&size=5&sort=name,asc` | Lọc theo ngày sinh, phân trang 5 bản ghi, xếp theo tên |
| **12** | `12. Validation Test - Invalid Field` | Kiểm tra bảo mật trường lọc | `?search=invalidField:test` | `400 Bad Request` thông báo trường không hợp lệ |
| **13** | `13. Validation Test - Invalid Sort` | Kiểm tra bảo mật trường sắp xếp | `?sort=unknownField,asc` | `400 Bad Request` thông báo sort field không hợp lệ |
| **14** | `14. Validation Test - Invalid Size` | Kiểm tra giới hạn kích thước trang | `?size=0` | `400 Bad Request` thông báo page size phải từ 1-100 |

### Bước 5: Kiểm thử thư mục `5. Document Filter & Pagination (HibernateDao)`
Thư mục này gồm **14 request mẫu được cấu hình sẵn 100% cho Document**, bao gồm:

| STT | Tên Request trong Postman | Mục đích kiểm thử | Query String mẫu | Kết quả mong đợi |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `1. Get All Documents` | Lấy danh sách tài liệu với phân trang mặc định | (none) | Danh sách sắp xếp `creationTime DESC`, `size: 10` |
| **2** | `2. Filter by Document Name` | Tìm kiếm tên tài liệu (LIKE) | `?search=name:Project` | Các tài liệu có tên chứa "Project" |
| **3** | `3. Filter by App ID` | Lọc theo mã ứng dụng | `?search=appId:app-001` | Các tài liệu thuộc appId chỉ định |
| **4** | `4. Filter by Version` | Lọc theo phiên bản tài liệu | `?search=version:1` | Các tài liệu có version bằng 1 |
| **5** | `5. Filter by Creation Time > Date` | Lọc tài liệu tạo sau ngày | `?search=creationTime>2026-01-01` | Tài liệu có ngày tạo > 2026-01-01 |
| **6** | `6. Multi-criteria Filter` | Kết hợp nhiều điều kiện AND | `?search=name:Design,version:2` | Tên chứa "Design" và version = 2 |
| **7** | `7. Pagination - Page 0, Size 5` | Phân trang trang đầu | `?page=0&size=5` | Tối đa 5 tài liệu, `page: 0`, `first: true` |
| **8** | `8. Sorting - Sort by Name ASC` | Sắp xếp tên tài liệu tăng dần | `?sort=name,asc` | Sắp xếp A-Z theo `name` |
| **9** | `9. Multi-field Sorting` | Sắp xếp nhiều trường cùng lúc | `?sort=creationTime,desc&sort=name,asc` | Ưu tiên ngày tạo giảm dần, rồi đến tên |
| **10** | `10. Combined - All Features` | Kết hợp Lọc + Phân trang + Sắp xếp | `?search=name:Report&page=0&size=5&sort=name,asc` | Lọc theo tên, phân trang 5 bản ghi, xếp A-Z |
| **11** | `11. Search Alias Endpoint` | Kiểm thử endpoint alias `/api/documents/search` | `?search=name:Report&page=0&size=10` | Hoạt động giống `/api/documents/filter` |
| **12** | `12. Validation Test - Invalid Sort` | Kiểm tra bảo mật sort whitelist | `?sort=hackerField,asc` | `400 Bad Request` thông báo sort field không hợp lệ |
| **13** | `13. Validation Test - Invalid Search` | Kiểm tra bảo mật search whitelist | `?search=unsupportedField:val` | `400 Bad Request` thông báo search field không hợp lệ |
| **14** | `14. Validation Test - Invalid Size` | Kiểm tra giới hạn kích thước trang | `?size=0` | `400 Bad Request` thông báo page size phải từ 1-100 |

### Bước 6: Xem định dạng kết quả trả về trong Postman
Mỗi request thành công đều trả về JSON chuẩn theo khuôn mẫu sau:
```json
{
    "success": true,
    "message": "Students filtered successfully",
    "data": {
        "content": [
            {
                "id": 1,
                "name": "Nguyen Van A",
                "dob": "2003-05-20",
                "department": {
                    "id": 1,
                    "name": "Computer Science",
                    "description": "Khoa Khoa hoc May tinh"
                }
            }
        ],
        "page": 0,
        "size": 5,
        "totalElements": 1,
        "totalPages": 1,
        "first": true,
        "last": true
    },
    "timestamp": "2026-10-03 14:34:06"
}
```

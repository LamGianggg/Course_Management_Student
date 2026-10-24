# 📖 API Documentation & OpenAPI Specification

Thư mục này chứa đặc tả giao diện lập trình ứng dụng (API Specification) định dạng **OpenAPI 3.1 / Swagger** của hệ thống **Educaze Course & Student Management System**.

---

## 📌 1. Phiên bản hiện tại (Current Version)
- **API Version:** Đồng bộ tự động theo thẻ `<version>` trong file [`pom.xml`](../pom.xml).
- **File đặc tả:** [`openapi.json`](./openapi.json)

---

## 🚀 2. Cách xem tài liệu Swagger trực tiếp trên GitHub

Khi xem trên GitHub mà không cần chạy Backend:
1. Tải hoặc sao chép nội dung file [`docs/openapi.json`](./openapi.json).
2. Mở trình duyệt vào [Swagger Editor (https://editor.swagger.io)](https://editor.swagger.io/).
3. Chọn **File -> Import File** và tải file `openapi.json` lên. Toàn bộ giao diện Swagger UI trực quan sẽ hiện ra ngay lập tức!
4. Hoặc kéo file `openapi.json` này thả vào **Postman** để tự động import toàn bộ Collection API.

---

## 💻 3. Cách xem Swagger khi chạy Backend tại Local
1. Khởi chạy ứng dụng Spring Boot:
   ```bash
   mvn spring-boot:run
   ```
2. Mở trình duyệt truy cập:
   - **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **OpenAPI JSON Endpoint:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🔄 4. Quy trình Cập nhật Version & Đẩy lên GitHub
Mỗi khi bạn nâng cấp phiên bản API hoặc chỉnh sửa endpoint:
1. Mở file `pom.xml`, đổi số phiên bản:
   ```xml
   <version>1.0.0</version> <!-- ví dụ đổi lên 1.0.0 -->
   ```
2. Chạy ứng dụng và cập nhật file `docs/openapi.json`:
   ```powershell
   .\export-swagger.ps1
   ```
3. Commit và đẩy toàn bộ lên GitHub:
   ```bash
   git add .
   git commit -m "feat: release API version 1.0.0 with updated swagger spec"
   git push
   ```
Mã nguồn và file tài liệu `docs/openapi.json` trên GitHub sẽ luôn mang phiên bản mới nhất!

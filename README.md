# 🏨 Hotel Booking Management System (Quản Lý Khách Sạn)

Ứng dụng web quản lý & đặt phòng khách sạn được xây dựng bằng **Java Spring Boot**, theo kiến trúc MVC truyền thống (Controller – Service – Repository) với giao diện render phía server bằng **Thymeleaf**.

Dự án mô phỏng một hệ thống khách sạn thực tế với 2 luồng nghiệp vụ tách biệt:
- **Khách hàng (ROLE_USER):** tìm phòng trống theo ngày, đặt phòng, áp mã giảm giá, xem lịch sử đặt phòng, đánh giá phòng sau khi lưu trú.
- **Quản trị viên (ROLE_ADMIN):** quản lý phòng/loại phòng, duyệt & theo dõi trạng thái đơn đặt phòng, tạo mã giảm giá, kiểm duyệt đánh giá, quản lý người dùng và xem thống kê tổng quan.

> ⚠️ Đây là đồ án học tập nhóm, được xây dựng nhằm thực hành thiết kế hệ thống backend với Spring Boot, Spring Security và Spring Data JPA.

---

## 📌 Tính năng chính

### Phía khách hàng
- Đăng ký / đăng nhập / đăng xuất, hỗ trợ **ghi nhớ đăng nhập** (remember-me 7 ngày)
- Quên mật khẩu / đặt lại mật khẩu
- Xem danh sách phòng, tìm phòng trống theo khoảng ngày check-in/check-out
- Đặt nhiều phòng cùng lúc trong 1 lượt đặt — hệ thống tự động gán các phòng trống cùng hạng phòng và gom chung vào một **mã nhóm đặt phòng** (group code)
- Kiểm tra tự động: đủ số lượng phòng trống, không cho đặt trùng lịch
- Áp dụng mã giảm giá (theo phần trăm có giới hạn mức giảm tối đa, hoặc giảm số tiền cố định) với điều kiện đơn hàng tối thiểu / số đêm tối thiểu / thời hạn hiệu lực
- Xem lịch sử & chi tiết đơn đặt phòng
- Đánh giá (review) phòng sau khi hoàn tất lưu trú

### Phía quản trị (Admin)
- Dashboard thống kê tổng quan
- Quản lý loại phòng (CRUD): giá, sức chứa, loại giường, tiện nghi
- Quản lý phòng (CRUD), upload hình ảnh phòng
- Quản lý đơn đặt phòng: duyệt đơn, chuyển trạng thái theo luồng
  `PENDING → CONFIRMED → STAYING → COMPLETED` (hoặc `CANCELLED`)
- Quản lý mã giảm giá: tạo mã theo % hoặc số tiền cố định, thiết lập điều kiện áp dụng
- Kiểm duyệt đánh giá của khách hàng (ẩn/hiện)
- Quản lý tài khoản người dùng

### Bảo mật
- **Spring Security** với phân quyền theo route (`/admin/**` yêu cầu `ROLE_ADMIN`, `/booking/**`, `/profile/**`, `/reviews/**` yêu cầu `ROLE_USER`)
- Mật khẩu được mã hoá bằng **BCrypt**
- Trang lỗi 403 tuỳ chỉnh khi truy cập trái phép

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Backend Framework | Spring Boot (Spring MVC, Spring Data JPA, Spring Security) |
| Template Engine | Thymeleaf + Thymeleaf Extras Spring Security6 |
| Cơ sở dữ liệu | MySQL |
| ORM | Hibernate / Spring Data JPA |
| Build tool | Maven (kèm Maven Wrapper) |
| Khác | Lombok, HTML/CSS/JavaScript thuần (vanilla JS cho toast, dashboard, quick view...) |

---

## 📂 Cấu trúc thư mục

```
src/main/java/com/example/quanlykhachsanjava/
├── config/          # Cấu hình WebMvc (phục vụ file upload qua /uploads/**)
├── controller/       # Controller cho public, user và admin
├── dto/              # Data Transfer Object (DiscountResult...)
├── model/             # Entity: User, Room, RoomCategory, Booking, Coupon, Review, PasswordResetToken
├── repository/        # Spring Data JPA Repository
├── security/          # Cấu hình Spring Security & UserDetailsService
├── service/            # Business logic (Booking, Coupon, Room, Review, User, File storage)
├── DataSeeder.java     # Khởi tạo dữ liệu mẫu khi chạy ứng dụng
└── QuanLyKhachSanJavaApplication.java

src/main/resources/
├── static/            # CSS, JavaScript
└── templates/          # Giao diện Thymeleaf (trang public + khu vực admin)
```

---

## 🚀 Cài đặt & chạy dự án

### Yêu cầu
- JDK 21+
- MySQL Server đang chạy
- (Không bắt buộc) Maven — dự án đã có sẵn Maven Wrapper (`mvnw` / `mvnw.cmd`)

### Các bước

1. **Clone dự án**
   ```bash
   git clone https://github.com/Khangle272/Quan-Ly-Khach-San-Java.git
   cd Quan-Ly-Khach-San-Java
   ```

2. **Cấu hình cơ sở dữ liệu**

   Mở `src/main/resources/application.properties` và chỉnh lại thông tin kết nối MySQL cho phù hợp với máy của bạn:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=<username_mysql_cua_ban>
   spring.datasource.password=<password_mysql_cua_ban>
   ```
   Database sẽ **tự động được tạo** nếu chưa tồn tại (`createDatabaseIfNotExist=true`), và các bảng sẽ tự động sinh ra nhờ `spring.jpa.hibernate.ddl-auto=update`.

   > 🔒 **Lưu ý bảo mật:** file cấu hình mẫu hiện đang chứa thông tin kết nối cho môi trường local của tác giả. Khi deploy thật, nên chuyển các thông tin nhạy cảm này sang biến môi trường thay vì hard-code trong `application.properties`, và thêm file này vào `.gitignore` nếu chứa dữ liệu thật.

3. **Chạy ứng dụng**
   ```bash
   ./mvnw spring-boot:run        # macOS/Linux
   mvnw.cmd spring-boot:run      # Windows
   ```

4. Truy cập ứng dụng tại: **http://localhost:8080**

### Tài khoản mẫu (được tạo sẵn bởi `DataSeeder`)

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Quản trị viên | `admin` | `admin123` |
| Khách hàng | `testuser` | `password123` |

Ứng dụng cũng tự sinh sẵn 2 loại phòng (Standard, VIP) cùng vài phòng và một đơn đặt phòng mẫu để tiện demo ngay sau khi khởi động.

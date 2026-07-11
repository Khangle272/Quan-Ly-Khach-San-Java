# 🏨 Hệ thống Quản lý Đặt phòng Khách sạn (Hotel Booking Management System)

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3+-brightgreen?style=for-the-badge&logo=spring-boot)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue?style=for-the-badge&logo=mysql)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template-green?style=for-the-badge&logo=thymeleaf)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple?style=for-the-badge&logo=bootstrap)

Một ứng dụng web quản lý khách sạn và đặt phòng trực tuyến được xây dựng bằng **Java Spring Boot** và **MVC Architecture**. Dự án cung cấp giải pháp toàn diện cho cả khách hàng (tìm kiếm, đặt phòng, quản lý lịch sử) và quản trị viên (quản lý phòng, doanh thu, mã giảm giá và phê duyệt đơn).

## 🚀 Tính năng nổi bật (Key Features)

### 👤 Dành cho Khách hàng (Customer)
* **Tìm kiếm & Lọc phòng thông minh:** Tìm phòng trống theo ngày nhận/trả, sức chứa, khoảng giá và loại phòng.
* **Đặt phòng linh hoạt:** Hỗ trợ đặt nhiều phòng cùng lúc, nhập mã giảm giá (Coupon) và tính toán tổng tiền realtime.
* **Quản lý lịch sử lưu trú:** Xem chi tiết các đơn đặt phòng, theo dõi trạng thái đơn (Chờ duyệt -> Đã xác nhận -> Đang lưu trú -> Hoàn tất).
* **Hủy phòng & Đánh giá:** Cho phép hủy đơn khi ở trạng thái chờ duyệt. Đánh giá và nhận xét phòng sau khi hoàn tất lưu trú.
* **Bảo mật tài khoản:** Đăng ký, đăng nhập (Spring Security), và tính năng Quên mật khẩu.

### 🛡️ Dành cho Quản trị viên (Admin)
* **Dashboard Thống kê:** Trực quan hóa doanh thu, số lượng đơn đặt phòng và khách hàng mới theo tháng bằng biểu đồ (Chart.js). Cảnh báo số lượng phòng trống sắp hết.
* **Quản lý Đặt phòng (Booking Workflow):** Xử lý luồng trạng thái đơn hàng và cập nhật trạng thái thanh toán (Chưa thanh toán, Đã thanh toán, Hoàn tiền).
* **Quản lý Mã giảm giá (Coupon):** Tạo mã giảm giá theo phần trăm (%) hoặc số tiền cố định, thiết lập điều kiện áp dụng (giá trị đơn tối thiểu, số đêm tối thiểu, thời hạn).
* **Quản lý Phòng & Loại phòng:** Thêm/sửa/xóa thông tin, tải lên hình ảnh phòng (File Storage), quản lý tiện nghi và trạng thái bảo trì.
* **Quản lý Người dùng & Đánh giá:** Khóa/mở khóa tài khoản khách hàng, kiểm duyệt các đánh giá trước khi hiển thị công khai.

## 🛠️ Công nghệ sử dụng (Tech Stack)

* **Backend:** Java 21, Spring Boot, Spring Security, Spring Data JPA, Hibernate.
* **Frontend:** HTML5, CSS3, JavaScript, Thymeleaf, Bootstrap 5.3, Chart.js.
* **Database:** MySQL.
* **Build Tool:** Maven.

## ⚙️ Hướng dẫn cài đặt (Installation & Setup)

1. **Clone repository:**
   ```bash
   git clone [https://github.com/yourusername/Quan-Ly-Khach-San-Java.git](https://github.com/yourusername/Quan-Ly-Khach-San-Java.git)
   cd Quan-Ly-Khach-San-Java

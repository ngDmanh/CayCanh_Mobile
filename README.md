# 🌿 Cây Cảnh - Mobile (Android)

Ứng dụng di động cho hệ thống quản lý và kinh doanh cây cảnh (mua + cho thuê).
Một ứng dụng phục vụ cả **khách hàng** và **quản trị viên** — điều hướng theo vai trò sau khi đăng nhập.

> 🖥️ Máy chủ (Backend Spring Boot): xem repo riêng — https://github.com/ngDmanh/CayCanh_Backend

---

## 1. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| Giao diện | Jetpack Compose |
| Kiến trúc | MVVM + Clean Architecture |
| Gọi API | Retrofit2 |
| Tiêm phụ thuộc | Hilt |
| Lưu trữ cục bộ | DataStore (lưu mã đăng nhập) |

---

## 2. Yêu cầu môi trường

- **Android Studio** (bản mới, khuyến nghị Hedgehog trở lên)
- **JDK 17+** (thường đi kèm Android Studio)
- Thiết bị Android thật hoặc máy ảo, **Android 7.0 (API 24)** trở lên
- **Máy chủ backend đã chạy** (xem repo backend) — ứng dụng cần kết nối tới API

---

## 3. Tải mã nguồn

```bash
git clone <ĐƯỜNG_DẪN_REPO_MOBILE>
```

Sau đó mở thư mục dự án bằng **Android Studio** và chờ đồng bộ Gradle hoàn tất.

---

## 4. Cấu hình địa chỉ máy chủ (BASE_URL)

Tìm hằng số `BASE_URL` trong dự án (thường ở lớp cấu hình mạng / Retrofit)
và đặt đúng địa chỉ máy chủ:

```kotlin
// Khi chạy bằng máy ảo Android (emulator):
const val BASE_URL = "http://10.0.2.2:8080/"

// Khi chạy bằng điện thoại thật (cùng mạng Wi-Fi với máy chủ):
// const val BASE_URL = "http://192.168.x.x:8080/"   // IP máy tính chạy backend
```

> 💡 `10.0.2.2` là địa chỉ trỏ về máy tính chủ khi nhìn từ máy ảo Android.
> Với điện thoại thật, thay bằng địa chỉ IP của máy tính (xem bằng `ipconfig` trên Windows
> hoặc `ifconfig` trên macOS/Linux), và bảo đảm hai thiết bị cùng mạng Wi-Fi.

---

## 5. Chạy ứng dụng

1. Mở Android Studio, chọn thiết bị (máy ảo hoặc điện thoại đã bật chế độ gỡ lỗi USB).
2. Bấm **Run** (▶) hoặc tổ hợp `Shift + F10`.
3. Android Studio sẽ build và cài ứng dụng lên thiết bị.

---

## 6. Tài khoản dùng thử

- **Khách hàng:** đăng ký tài khoản mới ngay trong ứng dụng (cần xác thực OTP qua email).
- **Quản trị viên:** dùng tài khoản admin do máy chủ khởi tạo sẵn
  *(xem hướng dẫn tạo tài khoản admin trong repo backend)*.

---

## 7. Chức năng chính

**Khách hàng:**
- Đăng ký / đăng nhập / quên mật khẩu (xác thực OTP qua email)
- Duyệt, tìm kiếm cây theo danh mục; xem chi tiết và đánh giá
- Giỏ hàng (mua và thuê); đặt hàng với nhiều luồng thanh toán
- Theo dõi đơn hàng, theo dõi và gia hạn hợp đồng thuê
- Đánh giá sản phẩm, quản lý tài khoản

**Quản trị viên:**
- Tổng quan thống kê (dashboard)
- Quản lý cây cảnh (thêm/sửa/xóa, tải nhiều ảnh)
- Quản lý đơn hàng, quản lý cho thuê (giao cây, thu hồi)
- Quản lý danh mục, báo cáo doanh thu, quản lý người dùng

---

## 8. Xử lý lỗi thường gặp

| Lỗi | Nguyên nhân & cách khắc phục |
|---|---|
| Không gọi được API / timeout | Máy chủ backend chưa chạy, hoặc sai `BASE_URL` |
| Lỗi kết nối trên điện thoại thật | Hai thiết bị không cùng mạng Wi-Fi, hoặc dùng `10.0.2.2` cho máy thật (phải dùng IP máy tính) |
| Không nhận được OTP | Kiểm tra cấu hình email phía máy chủ |
| Gradle sync thất bại | Kiểm tra kết nối mạng, để Android Studio tải đủ phụ thuộc |

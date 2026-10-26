# KẾ HOẠCH VÀ LUỒNG PHỐI HỢP THỰC HIỆN
## Task: Thiết Kế & Xây Dựng Giao Diện Quản Trị Viên (Admin Client UI - Version 1)

---

- **Tên Task**: `Admin_UI_Version_1` (Mã Feature Spec Kit: `001-admin-client-ui`)
- **Người thực hiện**: Trần Minh Triết (Phụ trách UI Phân hệ Admin)
- **Cộng sự hỗ trợ**: AI Assistant (Spec-Driven Development qua GitHub Spec Kit)
- **Phạm vi giai đoạn**: Version 1 
- **Công nghệ nền tảng**: Java 21, Java Swing, FlatLaf Theme, JFreeChart, Mock Data

---

## 1. Mục Tiêu & Ranh Giới Dự Án

1. **Mục tiêu Version 1**:
   - Xây dựng hoàn chỉnh mã nguồn giao diện Desktop cho toàn bộ 9 nhóm chức năng của Admin.
   - Giao diện hiện đại, chuyên nghiệp, sử dụng thư viện FlatLaf và biểu đồ JFreeChart.
   - Sử dụng dữ liệu giả lập (Mock Data) để hiển thị đầy đủ thông tin trên bảng (`JTable`) và biểu đồ (`JFreeChart`).
   - Mỗi màn hình được phân rã thành một thành phần (Panel/Dialog) riêng biệt, có logic hiển thị/bộ lọc riêng, và có **commit Git riêng biệt**.

2. **Ranh giới kỹ thuật (Bất biến)**:
   - Toàn bộ công việc nằm gọn trong module **`admin-client`**.
   - **Tuyệt đối không** kết nối trực tiếp PostgreSQL từ client.
   - **Chưa thực hiện** kết nối TCP Socket với backend ở giai đoạn này.

---

## 2. Mô Hình Phối Hợp Song Song (Human Designer + Spec Kit)

Quy trình phối hợp giữa Người thực hiện và AI Assistant được tổ chức theo sơ đồ khép kín dưới đây:

```
[ GIAI ĐOẠN 1: PHÂN RÃ & SINH PROMPT ]
       Spec Kit ($speckit-specify) tạo spec.md
       AI trích xuất bộ Prompt thiết kế cho 9 màn hình
                          │
                          ▼
[ GIAI ĐOẠN 2: THIẾT KẾ TRỰC QUAN TRÊN STITCH ]
       Dán Prompt vào Stitch/Figma
       Tuỳ chỉnh màu sắc, layout, chọn bộ icon (Lucide/Feather)
       Xuất file giao diện hoàn chỉnh
                          │
                          ▼
[ GIAI ĐOẠN 3: BÀN GIAO TÀI NGUYÊN VÀO DỰ ÁN ]
       Lưu thiết kế vào: docs/mockups/
       Lưu bộ Icon PNG/SVG vào: admin-client/src/main/resources/icons/
                          │
                          ▼
[ GIAI ĐOẠN 4: KHÓA KIẾN TRÚC & PHÂN RÃ ĐẦU VIỆC ]
       Spec Kit ($speckit-plan) lập kế hoạch cấu trúc file theo đúng mockup
       Spec Kit ($speckit-tasks) sinh bảng 10 đầu việc độc lập (tasks.md)
                          │
                          ▼
[ GIAI ĐOẠN 5: TRIỂN KHAI CODE & COMMIT TỪNG MÀN HÌNH ]
       Code từng Panel/Dialog Swing theo đúng thiết kế
       Chạy thử trên máy -> Nghiệm thu -> Commit Git riêng từng màn hình
       Chụp ảnh giao diện lưu vào docs/screenshots/admin/ để làm báo cáo
```

---

## 3. Chi Tiết 5 Giai Đoạn Thực Hiện

### Giai đoạn 1: Phân rã yêu cầu & Khởi tạo Spec Kit
- **Hành động**: Gọi lệnh `$speckit-specify Admin Client UI for Version 1`.
- **Kết quả**:
  - Tạo thư mục feature `specs/001-admin-client-ui/`.
  - Tạo file `spec.md` phân rã chi tiết 9 nhóm chức năng thành các User Stories độc lập.
  - AI cung cấp bảng danh sách các đoạn prompt chuẩn tiếng Anh/tiếng Việt để dán trực tiếp vào Stitch.

### Giai đoạn 2: Thiết kế & Tinh chỉnh trên Stitch (Triết)
- **Hành động**: 
  - Dùng Stitch (hoặc Figma) để sinh giao diện dựa trên prompt.
  - Điều chỉnh layout: Thống nhất dùng layout **Sidebar bên trái + Bảng nội dung bên phải**.
  - Lựa chọn bộ icon miễn phí phù hợp (khuyến nghị Lucide Icons hoặc Feather Icons dạng PNG 16x16 / 20x20).
  - Chọn bảng màu chủ đạo (ví dụ: FlatLightLaf hoặc FlatDarkLaf).

### Giai đoạn 3: Bàn giao tài nguyên thiết kế vào Workspace
- **Hành động**: Đưa các sản phẩm đã chốt vào dự án:
  - Ảnh mockup: lưu tại `docs/mockups/` (ví dụ: `01_login.png`, `02_user_management.png`, ...).
  - File icon: lưu tại `admin-client/src/main/resources/icons/`.

### Giai đoạn 4: Lập kế hoạch kiến trúc & Bảng Task (`plan.md` & `tasks.md`)
- **Hành động**: 
  - Kích hoạt `$speckit-plan`: Phân tích các ảnh trong `docs/mockups/`, định nghĩa danh sách các class Java Swing tương ứng trong `com.chat.admin.view` và `chart`.
  - Kích hoạt `$speckit-tasks`: Sinh file `tasks.md` gồm đúng 10 task độc lập, mỗi task gắn liền với 1 thông điệp commit Git cụ thể.

### Giai đoạn 5: Triển khai Code, Test và Commit Git từng màn hình
- **Hành động**:
  - Triển khai tuần tự từng task trong `tasks.md`.
  - Chạy ứng dụng kiểm tra trực quan trên máy qua `AdminApplication.java`.
  - Thực hiện commit Git riêng biệt cho từng màn hình hoàn thành.
  - Chụp ảnh màn hình thực tế lưu vào `docs/screenshots/admin/` làm minh chứng nộp báo cáo.

---

## 4. Bảng Phân Rã Công Việc & Quy Chuẩn Git Commit (10 Tasks = 10 Commits)

| STT | Tên màn hình / Thành phần | Tên File mã nguồn Java | Mục tiêu chức năng & Mock Logic | Mẫu Git Commit Message |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Khung chính & Điều hướng** | `AdminMainFrame.java` | Khung giao diện chính với Sidebar điều hướng, Header hiển thị thông tin Admin, vùng hiển thị `CardLayout`. | `feat(admin): create admin main frame with modern sidebar navigation` |
| **2** | **Đăng nhập Quản trị** | `AdminLoginDialog.java` | Hộp thoại đăng nhập, kiểm tra hợp lệ form, giả lập xác thực và mở MainFrame. | `feat(admin): implement admin login dialog with validation` |
| **3** | **Quản lý Người dùng** | `UserManagementPanel.java` | Bảng `JTable` danh sách tài khoản, tìm kiếm, lọc trạng thái, nút Khóa/Mở khóa đổi trạng thái trực tiếp, nút Thêm/Sửa/Đổi MK. | `feat(admin): implement user management panel with search and action triggers` |
| **4** | **Lịch sử Login & Bạn bè** | `UserDetailHistoryDialog.java` | Hộp thoại hiển thị chi tiết lịch sử đăng nhập theo thời gian và danh sách bạn bè trực tiếp / bạn của bạn. | `feat(admin): implement user login history and friend network detail dialog` |
| **5** | **Quản lý Nhóm trò chuyện** | `GroupManagementPanel.java` | Bảng danh sách nhóm chat, lọc theo tên nhóm, sắp xếp ngày tạo, dialog xem thành viên và admin nhóm. | `feat(admin): implement group management panel with member and admin viewer` |
| **6** | **Xử lý Báo cáo Spam** | `SpamReportPanel.java` | Bảng danh sách báo cáo spam, bộ lọc thời gian và username, nút Khóa tài khoản người bị báo cáo. | `feat(admin): implement spam report management panel with account ban action` |
| **7** | **User Đăng ký mới** | `NewRegistrationPanel.java` | Bộ lọc khoảng thời gian (Từ ngày - Đến ngày), lọc tên, bảng hiển thị tài khoản tạo mới. | `feat(admin): implement new user registration panel with date range filter` |
| **8** | **Thống kê Bạn bè** | `FriendStatsPanel.java` | Bảng hiển thị chỉ số bạn bè trực tiếp & bạn của bạn, bộ lọc so sánh số lượng bạn bè (`=`, `>`, `<`). | `feat(admin): implement friend statistics panel with comparison filters` |
| **9** | **Thống kê Hoạt động** | `UserActivityPanel.java` | Bảng thống kê số lần mở app, số người chat, số nhóm chat; lọc khoảng thời gian và số lượng hoạt động (`=`, `>`, `<`). | `feat(admin): implement user activity panel with custom activity filters` |
| **10**| **Biểu đồ Thống kê (JFreeChart)**| `RegistrationChartPanel.java`<br>`ActivityChartPanel.java` | Biểu đồ cột/đường JFreeChart: Số lượng đăng ký mới theo 12 tháng và Số người dùng mở app theo 12 tháng (chọn năm). | `feat(admin): implement registration and activity statistics charts using JFreeChart` |

---

## 5. Quy Ước Cấu Trúc Lưu Trữ Tài Nguyên

```text
Chat-System/
├── docs/
│   ├── Plan_Admin_UI_Version_1.md       # Văn bản kế hoạch và luồng phối hợp (File này)
│   ├── mockups/                         # Nơi lưu ảnh xuất từ Stitch/Figma
│   └── screenshots/admin/               # Nơi lưu ảnh chụp màn hình Java Swing hoàn thiện để nộp báo cáo
│
├── specs/001-admin-client-ui/           # Thư mục quản lý bởi GitHub Spec Kit
│   ├── spec.md                          # Đặc tả chi tiết từng User Story / Màn hình
│   ├── plan.md                          # Kế hoạch kiến trúc Swing chi tiết
│   └── tasks.md                         # Danh sách 10 đầu việc cần tick hoàn thành
│
└── admin-client/src/main/
    ├── resources/icons/                 # Nơi lưu các file icon PNG/SVG đã chọn
    └── java/com/chat/admin/
        ├── AdminApplication.java       # Điểm khởi chạy chính ứng dụng
        ├── view/                        # Chứa 8 Panels và Dialogs giao diện
        ├── chart/                       # Chứa 2 Panels biểu đồ JFreeChart
        └── model/                       # Chứa các model dữ liệu mẫu (Mock DTOs)
```

---

## 6. Tiêu Chí Nghiệm Thu Bàn Giao Version 1

- [ ] Toàn bộ 10 thành phần giao diện chạy mượt mà, không gặp lỗi Exception trên Swing EDT.
- [ ] Giao diện tuân thủ Look & Feel hiện đại của FlatLaf, bố cục cân đối theo mockup đã chốt.
- [ ] Dự án biên dịch thành công qua lệnh `mvn test-compile`.
- [ ] Có đầy đủ 10 commit Git rõ ràng trên nhánh làm việc.
- [ ] Đầy đủ ảnh chụp màn hình trong thư mục `docs/screenshots/admin/` để đưa vào Báo cáo Version 1.

# KẾ HOẠCH VÀ LUỒNG PHỐI HỢP THỰC HIỆN
## Task: Thiết Kế & Xây Dựng Giao Diện Người Dùng (User Client UI - Version 1)

---

- **Tên Task**: `User_UI_Version_1` (Feature Spec Kit dự kiến: `002-user-client-ui`)
- **Người thực hiện**: Thành viên phụ trách User Client UI (B – Tiến theo phân công trong `project.txt`)
- **Cộng sự hỗ trợ**: AI Assistant (hỗ trợ phân tích, thiết kế và triển khai)
- **Phạm vi giai đoạn**: Version 1
- **Công nghệ nền tảng**: Java 25 LTS, Java Swing, FlatLaf, Mock Data

---

## 1. Mục Tiêu & Ranh Giới Dự Án

1. **Mục tiêu Version 1**:
   - Xây dựng mã nguồn giao diện Desktop cho các nhóm chức năng User được yêu cầu trong đồ án.
   - Thiết kế giao diện nhất quán, dễ sử dụng, triển khai bằng Java Swing và FlatLaf.
   - Dùng dữ liệu giả lập để trình bày danh sách, trạng thái, lịch sử trò chuyện và các thao tác mẫu.
   - Phân chia giao diện thành Frame, Panel và Dialog phù hợp để dễ phát triển, tích hợp và chụp ảnh nghiệm thu.

2. **Phạm vi chức năng**:
   - Tài khoản: đăng nhập, đăng ký, khôi phục mật khẩu, hồ sơ và đổi mật khẩu.
   - Bạn bè và tìm kiếm: danh sách bạn bè, trạng thái online, lời mời, tìm kiếm, hủy kết bạn và block.
   - Chat cá nhân: trò chuyện, lịch sử, tìm kiếm, xóa tin nhắn và hỗ trợ soạn tin bằng LLM.
   - Chat nhóm: tạo nhóm, đổi tên, quản lý thành viên/quyền admin, nhắn tin và thể hiện trạng thái mã hóa.
   - Báo cáo spam: tạo báo cáo cho tài khoản đã có tương tác trò chuyện.

3. **Ranh giới kỹ thuật (bất biến)**:
   - Mã nguồn giao diện thuộc module **`user-client`**; tài nguyên thiết kế lưu trong `docs/` và `user-client/src/main/resources/`.
   - Không kết nối PostgreSQL hoặc truy cập dữ liệu trực tiếp từ client.
   - Version 1 chưa tích hợp TCP Socket, SMTP, Groq/LLM API hoặc cơ chế mã hóa thực tế.
   - Không thay đổi hợp đồng giao thức trong `common` hoặc bổ sung module mới chỉ để phục vụ UI.
   - Những quy tắc nghiệp vụ chưa được chốt, đặc biệt chính sách xóa lịch sử chat, cần được thể hiện bằng UI trung tính và để backend/nhóm thống nhất trước khi triển khai Version 2.

---

## 2. Mô Hình Phối Hợp Song Song (Người Thiết Kế + Spec Kit)

```text
[ GIAI ĐOẠN 1: PHÂN RÃ YÊU CẦU ]
       Đối chiếu project.txt và thống nhất phạm vi User UI
       Tạo đặc tả, danh sách màn hình và prompt thiết kế
                          │
                          ▼
[ GIAI ĐOẠN 2: THIẾT KẾ TRỰC QUAN ]
       Dùng Stitch/Figma để phác thảo các luồng chính
       Thống nhất màu sắc, bố cục, trạng thái và icon
                          │
                          ▼
[ GIAI ĐOẠN 3: BÀN GIAO TÀI NGUYÊN ]
       Lưu mockup vào docs/mockups/002-user-client-ui/
       Lưu icon cần thiết vào user-client/src/main/resources/icons/
                          │
                          ▼
[ GIAI ĐOẠN 4: KHÓA CẤU TRÚC & ĐẦU VIỆC ]
       Lập kế hoạch class Swing phù hợp mockup
       Chia đầu việc theo màn hình/luồng sử dụng
                          │
                          ▼
[ GIAI ĐOẠN 5: TRIỂN KHAI & NGHIỆM THU ]
       Dựng giao diện với dữ liệu giả lập
       Chạy thử, kiểm tra trạng thái, chụp ảnh lưu tại docs/screenshots/user/
```

---

## 3. Chi Tiết 5 Giai Đoạn Thực Hiện

### Giai đoạn 1: Phân rã yêu cầu và khởi tạo đặc tả
- **Hành động**:
  - Đối chiếu phạm vi User trong `project.txt`, tập trung vào 12 chức năng và các quy tắc liên quan.
  - Tạo đặc tả User Client UI trong `specs/002-user-client-ui/` nếu nhóm tiếp tục sử dụng GitHub Spec Kit.
  - Phân rã luồng đăng nhập, sử dụng ứng dụng, chat cá nhân và chat nhóm thành các màn hình/đối thoại có thể nghiệm thu độc lập.
- **Kết quả**:
  - Danh sách màn hình và trạng thái chính.
  - Prompt thiết kế cho Stitch/Figma, không thêm nghiệp vụ ngoài phạm vi đồ án.

### Giai đoạn 2: Thiết kế và tinh chỉnh giao diện
- **Hành động**:
  - Thiết kế trước các luồng thường dùng: đăng nhập → danh sách trò chuyện → mở chat → xem thông tin bạn bè/nhóm.
  - Thống nhất mẫu điều hướng, kiểu nút, biểu mẫu, avatar, trạng thái online/offline và thông báo xác nhận.
  - Thiết kế các trạng thái rỗng, không tìm thấy kết quả và dữ liệu mẫu để màn hình vẫn dễ hiểu khi danh sách trống.
- **Định hướng bố cục**:
  - Màn hình chính sau đăng nhập dùng thanh điều hướng và vùng nội dung linh hoạt.
  - Khu vực chat ưu tiên danh sách hội thoại bên trái và nội dung cuộc trò chuyện bên phải.
  - Dialog chỉ dùng cho tác vụ ngắn như tạo nhóm, báo cáo spam, xác nhận xóa hoặc chỉnh sửa hồ sơ.
  - Bảo đảm giao diện có thể sử dụng trên kích thước cửa sổ Desktop phổ biến; không phụ thuộc vào mockup HTML khi triển khai Swing.

### Giai đoạn 3: Bàn giao tài nguyên thiết kế vào Workspace
- **Hành động**:
  - Lưu mockup đã thống nhất tại `docs/mockups/002-user-client-ui/`.
  - Lưu icon vào `user-client/src/main/resources/icons/` và hình ảnh mẫu vào thư mục tài nguyên phù hợp.
  - Lưu ảnh chụp giao diện Java Swing sau khi hoàn thiện tại `docs/screenshots/user/`.
- **Nguyên tắc**:
  - Tái sử dụng tài nguyên sẵn có nếu phù hợp; không bổ sung thư viện/framework chỉ để hiển thị icon hoặc ảnh.
  - Không đưa API key LLM, thông tin SMTP, mật khẩu hay cấu hình bí mật vào tài nguyên client.

### Giai đoạn 4: Lập kế hoạch cấu trúc và bảng đầu việc
- **Hành động**:
  - Ánh xạ mockup sang package hiện có trong `com.chat.user`.
  - Dùng các package `view/`, `component/`, `controller/`, `model/` theo đúng trách nhiệm; chỉ thêm class cần thiết.
  - Chia task theo bảng tại mục 4; gộp hoặc tách class khi mockup và quy ước nhóm yêu cầu.
- **Giới hạn**:
  - Model trong Version 1 chỉ phục vụ trình bày dữ liệu giả lập.
  - Không biến controller giao diện thành nơi chứa nghiệp vụ backend hoặc truy vấn cơ sở dữ liệu.
  - Mọi quy ước nghiệp vụ còn mở phải được xác nhận trước khi tích hợp chức năng thật.

### Giai đoạn 5: Triển khai, kiểm tra và bàn giao
- **Hành động**:
  - Triển khai từng Panel/Dialog bằng Swing, dùng FlatLaf và dữ liệu giả lập nhất quán.
  - Kiểm tra thao tác điều hướng, mở/đóng dialog, hiển thị xác nhận và cập nhật trạng thái mẫu trên Swing EDT.
  - Chạy ứng dụng qua `UserApplication`, rà soát kích thước cửa sổ và chụp ảnh nghiệm thu.
  - Tạo commit theo từng phần nếu phù hợp quy ước nhóm; các commit cần phản ánh đúng nội dung đã hoàn thành.
- **Không thuộc Version 1**:
  - Xác thực tài khoản thật, gửi email khôi phục, giao tin nhắn realtime, lưu trữ lịch sử trên Server.
  - Thực thi quyền block/admin nhóm, gọi Groq hoặc mã hóa/giải mã tin nhắn.

---

## 4. Bảng Phân Rã Công Việc & Mẫu Git Commit (14 Thành Phần UI)

| STT | Màn hình / Thành phần | Tên File mã nguồn Java (đề xuất) | Mục tiêu giao diện & Mock Logic | Mẫu Git Commit (tham khảo) |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Khung chính & Điều hướng** | `UserMainFrame.java` | Cửa sổ chính sau đăng nhập; điều hướng giữa trò chuyện, bạn bè, lời mời, tìm kiếm và hồ sơ; dùng vùng nội dung thay đổi theo lựa chọn. | `feat(user): create main frame with navigation shell` |
| **2** | **Đăng nhập** | `LoginPanel.java` | Form username/mật khẩu, kiểm tra trường bắt buộc ở mức UI và chuyển sang khung chính bằng trạng thái giả lập. | `feat(user): implement login screen with mock navigation` |
| **3** | **Đăng ký tài khoản** | `RegistrationDialog.java` | Form nhập thông tin tài khoản, phản hồi trực quan khi thiếu dữ liệu và hiển thị kết quả tạo tài khoản giả lập. | `feat(user): add account registration dialog` |
| **4** | **Khôi phục mật khẩu** | `PasswordRecoveryDialog.java` | Nhập email và hiển thị thông báo hướng dẫn/đã gửi giả lập; không tạo hay gửi mật khẩu thật từ client. | `feat(user): add password recovery dialog` |
| **5** | **Hồ sơ & Đổi mật khẩu** | `ProfileSettingsPanel.java`<br>`ChangePasswordDialog.java` | Xem/chỉnh sửa thông tin cá nhân và mở biểu mẫu đổi mật khẩu; dữ liệu chỉ cập nhật trong mock state. | `feat(user): add profile settings and password form` |
| **6** | **Danh sách bạn bè** | `FriendsPanel.java` | Danh sách bạn bè, lọc theo tên, trạng thái online/offline, mở chat, hủy kết bạn và block với xác nhận UI. | `feat(user): implement friends list and relationship actions` |
| **7** | **Lời mời kết bạn** | `FriendRequestsPanel.java` | Hiển thị và lọc lời mời theo tên; thao tác chấp nhận/từ chối cập nhật trạng thái giả lập. | `feat(user): add friend request management panel` |
| **8** | **Tìm kiếm người dùng** | `UserSearchPanel.java` | Tìm theo username/họ tên; minh họa kết quả và thao tác mở chat/tạo nhóm. Không hiển thị người đã block trong dữ liệu mẫu của người tìm. | `feat(user): add user search and result actions` |
| **9** | **Chat cá nhân** | `ChatWorkspacePanel.java`<br>`MessageBubble.java` | Danh sách hội thoại và nội dung chat; trình bày tin nhắn online/offline, lịch sử, trạng thái gửi; hỗ trợ chọn tin nhắn, xóa một/nhiều, xóa toàn bộ lịch sử và Unsend bằng mock state. | `feat(user): implement direct chat workspace with message actions` |
| **10** | **Tìm kiếm lịch sử chat** | `ChatHistorySearchDialog.java` | Tìm trong cuộc trò chuyện hiện tại hoặc trên tất cả cuộc trò chuyện; hiển thị kết quả và mô phỏng điều hướng tới đoạn chat tương ứng. | `feat(user): add conversation and global history search` |
| **11** | **Quản lý nhóm chat** | `GroupManagementPanel.java`<br>`CreateGroupDialog.java`<br>`GroupMembersDialog.java` | Xem nhóm, tạo nhóm với tối thiểu một username khác, đổi tên, thêm/xóa thành viên và gán admin nhóm bằng thao tác mẫu. | `feat(user): add group creation and member management UI` |
| **12** | **Chat nhóm** | `GroupChatPanel.java` | Hiển thị tin nhắn nhóm và thông tin thành viên; thể hiện trạng thái/nhãn mã hóa nhóm ở mức giao diện, không thực hiện mã hóa thực tế. | `feat(user): implement group chat screen` |
| **13** | **Báo cáo spam** | `SpamReportDialog.java` | Cho phép bắt đầu báo cáo từ cuộc trò chuyện/tài khoản đã tương tác, nhập lý do và hiển thị xác nhận gửi giả lập. | `feat(user): add spam report dialog` |
| **14** | **Hỗ trợ soạn tin bằng LLM** | `LlmAssistDialog.java` | Cho phép chọn thao tác gợi ý hoặc chỉnh sửa bản nháp; hiển thị kết quả mẫu để người dùng xem trước trước khi chèn vào ô soạn tin. Không gọi API trong Version 1. | `feat(user): add mock LLM compose assistance` |

**Lưu ý nghiệp vụ cần thống nhất trước Version 2**:
- Giao diện phân biệt thao tác xóa tin nhắn của riêng người dùng (Delete for Me) và thu hồi tin nhắn (Unsend); quyền thực hiện, phạm vi đồng bộ và trạng thái sau thao tác do nghiệp vụ Server quyết định.
- Yêu cầu xóa toàn bộ lịch sử chưa xác định rõ chỉ ẩn với người yêu cầu hay xóa dữ liệu dùng chung; Version 1 chỉ mô phỏng xác nhận, không khẳng định chính sách lưu trữ.
- Khôi phục mật khẩu theo yêu cầu đồ án là tạo mật khẩu ngẫu nhiên và gửi tới email; Version 1 chỉ mô phỏng phản hồi, không gửi email hoặc hiển thị mật khẩu mới.
- LLM được project.txt ghi nhận dùng API Groq; Version 1 chỉ mô phỏng giao diện. Khi tích hợp, nhóm cần quyết định cách gọi và bảo vệ API key, không đóng gói secret trong client.
- Mã hóa nhóm được thể hiện bằng trạng thái giao diện; thuật toán, quản lý khóa và quy trình E2EE cần được thiết kế/kiểm chứng riêng trước khi triển khai.

---

## 5. Quy Ước Cấu Trúc Lưu Trữ Tài Nguyên

```text
Chat-System/
├── docs/
│   ├── Plan_User_UI_Version_1.md       # Kế hoạch User Client UI
│   ├── mockups/002-user-client-ui/     # Mockup Stitch/Figma
│   └── screenshots/user/               # Ảnh chụp giao diện hoàn thiện
│
├── specs/002-user-client-ui/           # Đặc tả nếu nhóm dùng GitHub Spec Kit
│   ├── spec.md                         # User stories và tiêu chí nghiệm thu
│   ├── plan.md                         # Kế hoạch cấu trúc Swing
│   └── tasks.md                        # Đầu việc triển khai
│
└── user-client/src/main/
    ├── resources/
    │   ├── icons/                      # Icon giao diện
    │   └── images/                     # Ảnh minh họa/avatar mẫu nếu cần
    └── java/com/chat/user/
        ├── UserApplication.java        # Điểm khởi chạy ứng dụng
        ├── view/                       # Frame, Panels và Dialogs
        ├── component/                  # Thành phần giao diện tái sử dụng
        ├── controller/                 # Điều phối tương tác UI
        ├── model/                      # Model trình bày/mock data
        └── network/                    # SocketClient hiện có; tích hợp sau Version 1
```

---

## 6. Tiêu Chí Nghiệm Thu Bàn Giao Version 1

- [ ] Giao diện bao phủ đủ các nhóm chức năng User trong `project.txt`: tài khoản, bạn bè/tìm kiếm, báo cáo spam, chat cá nhân và chat nhóm.
- [ ] Các màn hình chính có thể mở và điều hướng bằng dữ liệu giả lập; các trạng thái online/offline, lời mời, tin nhắn và nhóm được trình bày nhất quán.
- [ ] Các thao tác xóa/thu hồi, block, báo cáo, hỗ trợ LLM và mã hóa nhóm chỉ mô phỏng ở UI, không tạo cảm giác backend đã hoạt động.
- [ ] Giao diện dùng Swing + FlatLaf, không truy cập PostgreSQL trực tiếp và không chứa secret.
- [ ] Chạy thử ứng dụng, kiểm tra các luồng UI chính không phát sinh Exception trên Swing EDT.
- [ ] Dự án biên dịch thành công qua lệnh `mvn test-compile`.
- [ ] Có ảnh chụp giao diện tại `docs/screenshots/user/` phục vụ báo cáo Version 1.
- [ ] Tiến độ và đóng góp được thể hiện trung thực trong Git theo quy ước nhóm.

## 7. Đồng Bộ Ngôn Ngữ Và Giao Diện

- Tất cả nội dung giao diện hiển thị cho người dùng trong `user-client` dùng tiếng Anh, đồng bộ với Admin Client.
- User Client dùng cùng bảng màu navy/blue, nền sáng, viền, kiểu chữ, nút và khoảng cách với Admin Client; phần điều hướng, header và footer cũng theo cùng phong cách workspace.
- Giữ nguyên các tên riêng trong dữ liệu mẫu; nội dung chức năng và thông báo mẫu hiển thị bằng tiếng Anh.
- Ảnh chụp UI sau khi chạy ứng dụng được lưu tại `docs/screenshots/user/` với 12 tên màn hình hiện có.

---

### **Bản mô tả dự án cuối cùng: LocalPlayer Application**

#### **1. Tổng quan dự án**

* **Tên dự án:** LocalPlayer Application
* **Mục tiêu:** Phát triển một ứng dụng nghe nhạc gốc (native) cho Android, chuyên phát các tệp âm thanh từ bộ nhớ cục bộ. Ứng dụng phải có giao diện hiện đại, mạnh mẽ về tính năng, cá nhân hóa cao, và có khả năng thích ứng toàn diện với nhiều loại thiết bị và cài đặt người dùng.

---

#### **2. Thông số kỹ thuật**

* **Ngôn ngữ:** Kotlin
* **Kiến trúc:** MVVM (Model-View-ViewModel)
* **UI Toolkit:** Jetpack Compose & Material 3
* **Dependency Injection:** Hilt
* **Cơ sở dữ liệu:** Room (để lưu playlists, bài hát yêu thích, lịch sử nghe)
* **Lưu trữ Tùy chọn:** Jetpack DataStore (để lưu tên, ảnh đại diện, và các cài đặt của người dùng)
* **Xử lý bất đồng bộ:** Kotlin Coroutines
* **Điều hướng:** Navigation Compose
* **Xử lý Media:** Jetpack Media3 (ExoPlayer & MediaSessionService)
* **Tải ảnh:** Coil
* **Package Name:** `com.tinhtx.localplayerapplication`
* **minSdk:** 29
* **targetSdk:** 35

---

#### **3. Yêu cầu Cốt lõi & Hành vi Toàn cục**

* **Thiết kế Responsive Toàn diện:**
    * **Thích ứng Kích thước Màn hình:** Giao diện phải tự động điều chỉnh bố cục dựa trên **Material 3 Window Size Classes**. Bố cục phải thích ứng tức thì khi người dùng thay đổi kích thước cửa sổ. Trên màn hình `Expanded` (máy tính bảng), `BottomAppBar` được thay bằng `NavigationRail`.
    * **Thích ứng Kích thước Font:** Giao diện **không được vỡ** khi người dùng thay đổi kích thước font chữ từ Cài đặt hệ thống. Bắt buộc **sử dụng đơn vị `sp` cho `fontSize`** và thiết kế các container linh hoạt.
* **Hệ thống Icon (Material Symbols):** Sử dụng nhất quán các biến thể `Outlined`, `Rounded`, `Sharp` từ thư viện `material-icons-extended`.
* **Thông báo Động (Live Notification):** Tích hợp `MediaStyle` notification được quản lý tự động bởi `MediaSessionService`. Có khả năng tùy chỉnh để thêm các nút hành động riêng.
* **Xử lý Audio Focus:** Ứng dụng phải tự động dừng/phát lại nhạc một cách mượt mà khi có sự kiện âm thanh từ ứng dụng khác.
* **Xử lý Lỗi:** Không bị crash khi gặp tệp nhạc lỗi, thay vào đó sẽ hiển thị thông báo và chuyển bài.
* **Khôi phục Trạng thái:** Phải khôi phục lại đúng trạng thái phát nhạc (bài hát, vị trí, hàng đợi) khi người dùng quay lại ứng dụng sau khi tiến trình bị hệ điều hành tạm dừng.

---

#### **4. Giao diện & Chức năng - Các Màn hình Chính**

##### **a. Màn hình chính (HomeScreen)**

Màn hình khám phá chính, được bố cục trong `Scaffold`.

* **TopAppBar:** Chiều cao tiêu chuẩn (`~64.dp`), chứa `IconButton` tìm kiếm bên trái, tiêu đề "Retro Music" ở giữa, và các `IconButton` cho Cast và Cài đặt bên phải.
* **Nội dung chính (`LazyColumn`):**
    * **Khối Thông tin Người dùng:** Nằm ngay dưới `TopAppBar`, cao khoảng `~90.dp`. Một `Row` chứa `AsyncImage` (ảnh đại diện cắt tròn, `~64x64 dp`) bên trái và một `Column` bên phải hiển thị lời chào và tên người dùng (font lớn, đậm).
    * **Các Nút chức năng nhanh:** Hai hàng, mỗi hàng chứa hai `Card` chức năng (ví dụ: History, Last added, Most played, Shuffle). Các `Card` này có nền sáng, bo góc và chứa một `Icon` cùng `Text`.
    * **Các Danh sách Nhạc:** Nằm ở dưới cùng, chứa các `LazyRow` hiển thị album hoặc các bài hát được gợi ý.

##### **b. Màn hình phát nhạc (PlayerScreen)**

Giao diện đắm chìm, được xây dựng trên `HorizontalPager` để vuốt chuyển bài.

* **Nền động:** Màu nền của toàn màn hình được trích xuất từ màu chủ đạo của ảnh bìa album và chuyển đổi mượt mà.
* **Bố cục trên mỗi trang:**
    * **TopAppBar trong suốt:** Chứa `IconButton` mũi tên đi xuống để đóng màn hình và các `IconButton` cho hàng đợi và tùy chọn khác.
    * **Ảnh bìa Album:** `Card` hình vuông, rộng **80% chiều rộng màn hình**, nằm ở trung tâm phía trên. Có nhãn tên nghệ sĩ xoay dọc ở cạnh phải.
    * **Thông tin bài hát:** Nằm ngay dưới ảnh bìa, gồm tên bài hát (lớn) và tên nghệ sĩ (nhỏ), căn giữa.
    * **Hiệu ứng Sóng nhạc (Visualizer):** Dải đồ họa động hẹp, nằm dưới khối thông tin.
    * **Cụm Điều khiển:** Nằm ở phần dưới của màn hình, gồm `Slider` và 5 nút điều khiển (Shuffle, Previous, Play/Pause, Next, Repeat). Nút Play/Pause ở giữa có kích thước lớn vượt trội (`~72.dp`).

---

#### **5. Giao diện & Chức năng - Các Màn hình Bổ sung**

##### **a. Quản lý Thư viện & Nội dung**
* **Màn hình Tìm kiếm:** Một màn hình riêng để tìm kiếm tức thì trong toàn bộ thư viện.
* **Màn hình Thư viện:** Cần các tab hoặc mục điều hướng để truy cập: Tất cả bài hát, Nghệ sĩ, Album.
* **Màn hình Playlists:** Hiển thị danh sách các playlist người dùng đã tạo. Cho phép tạo playlist mới.
* **Màn hình Chi tiết Playlist:** Hiển thị các bài hát trong một playlist, cho phép sắp xếp lại bằng cách kéo-thả và xóa.
* **Màn hình Yêu thích:** Liệt kê tất cả các bài hát đã được "Like".
* **Màn hình Hàng đợi phát (Queue):** Hiển thị các bài hát sắp phát, cho phép sắp xếp lại.

##### **b. Màn hình Cài đặt (Settings)**
* **Bố cục:** Một `Column` trên toàn màn hình.
* **Phần Hồ sơ Người dùng:**
    * **Ảnh đại diện:** Một `AsyncImage` lớn (`~120x120 dp`) hiển thị ảnh hiện tại. Ngay dưới là một `TextButton` "Thay đổi ảnh" để mở thư viện ảnh của hệ thống (dùng `ActivityResultContracts.PickVisualMedia`). URI ảnh được lưu vào **DataStore**.
    * **Tên người dùng:** Một `OutlinedTextField` với nhãn "Tên của bạn", cho phép người dùng sửa và lưu tên vào **DataStore**.
* **Phần Tùy chọn khác:**
    * Chọn giao diện (Sáng/Tối/Hệ thống).
    * Hẹn giờ tắt nhạc (Sleep Timer).
    * Tùy chỉnh hành vi của tai nghe.
    * Nút "Quét lại thư viện".
    * Thông tin về ứng dụng và chính sách bảo mật.
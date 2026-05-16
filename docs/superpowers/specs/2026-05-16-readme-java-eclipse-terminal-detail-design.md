# Design: Bổ sung hướng dẫn Java, Eclipse, Terminal chi tiết vào README

**Ngày:** 2026-05-16  
**Phạm vi:** Mở rộng `README.md` — không tạo file mới

---

## Mục tiêu

Người đọc clone repo lần đầu (đặc biệt sinh viên dùng Windows) có thể cài đặt và chạy ứng dụng mà không cần tìm tài liệu bên ngoài. Ba điểm yếu hiện tại cần khắc phục:

1. Không có hướng dẫn tải/cài Java — chỉ ghi "JDK 21 trở lên"
2. Eclipse thiếu verify steps và xử lý lỗi thường gặp
3. Terminal thiếu bước `chmod`, verify JAVA_HOME, và troubleshoot

---

## Quyết định thiết kế

**Cách tiếp cận:** Mở rộng trực tiếp vào `README.md` (không tách file riêng).  
**Lý do:** Dự án lớp học — người đọc chỉ mở 1 file, không cần điều hướng.  
**Phân phối Java:** Tập trung Eclipse Adoptium Temurin 21 (LTS, miễn phí, đã được reference trong README).  
**Ưu tiên OS:** Windows chi tiết nhất, macOS và Linux bổ sung ngắn gọn.

---

## Section 1 — Bước 0: Tải và cài Java 21 (Temurin) [MỚI]

Chèn trước "Hướng dẫn Cài đặt" hiện tại.

### Windows (chi tiết)
1. Vào `https://adoptium.net` → chọn **Temurin 21 (LTS)** → **Windows x64 Installer (.msi)**
2. Chạy `.msi` → trong màn hình installer, tích chọn:
   - **"Set JAVA_HOME variable"**
   - **"Add to PATH"**
   - (Hai tùy chọn này tự động thay thế việc set tay JAVA_HOME ở Bước 3)
3. Verify: mở CMD mới → `java -version` → phải thấy `openjdk version "21.x.x"`

### macOS
1. Tải **macOS x64** (Intel) hoặc **aarch64** (Apple Silicon) `.pkg` từ adoptium.net
2. Chạy `.pkg` → cài xong tự cấu hình PATH
3. Verify: `java -version` trong Terminal

### Linux (Ubuntu/Debian)
```bash
sudo apt update && sudo apt install -y wget apt-transport-https
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update && sudo apt install temurin-21-jdk
java -version
```

---

## Section 2 — Nâng cấp phần Eclipse [BỔ SUNG]

Giữ nguyên 4 bước hiện có. Chèn thêm:

### Sau Bước 1 (Import)
> ✅ **Kiểm tra:** Project xuất hiện trong Package Explorer không có dấu ❌ đỏ trên icon → import thành công.

### Sau Bước 2 (Cấu hình JDK)
> ✅ **Kiểm tra:** Chuột phải project → **Properties → Java Build Path → Libraries** → phải thấy `JRE System Library [JavaSE-21]`.

> ❌ **Lỗi "Java compiler compliance" (1.5/1.7/1.8):** Chuột phải project → **Properties → Java Compiler** → bỏ tích "Use compliance from execution environment" → chọn **21** → Apply.

### Sau Bước 3 (Run)
> ✅ **Kiểm tra:** Console Eclipse hiện `JavaFX Application Thread` và cửa sổ 3D mở ra.

> ❌ **Lỗi "No suitable driver found" hoặc DB thất bại:** SQL Server chưa chạy hoặc sai password — kiểm tra lại `DatabaseConnection.java` và đảm bảo SQL Server service đang hoạt động.

> ❌ **Lỗi "Error occurred during initialization of boot layer":** Do chạy `main()` trực tiếp. Phải dùng **Run As → Maven Build → Goals: `javafx:run`**, không nhấn Run thông thường trên `MainApp.java`.

### Sau Bước 4 (SQL trong Eclipse)
> 💡 Nếu không muốn cài DTP, dùng **SQL Server Management Studio (SSMS)** là cách đơn giản hơn.

---

## Section 3 — Nâng cấp phần Terminal [BỔ SUNG]

Thay thế khối lệnh 2 dòng hiện tại bằng cấu trúc đầy đủ.

### Windows CMD/PowerShell
```cmd
:: Kiểm tra Java
java -version
:: Phải thấy: openjdk version "21.x.x"

:: Kiểm tra JAVA_HOME
echo %JAVA_HOME%
:: Phải thấy đường dẫn JDK, không được trống

:: Build và chạy
mvnw.cmd compile
mvnw.cmd javafx:run
```

### macOS / Linux
```bash
# Lần đầu: cấp quyền thực thi
chmod +x ./mvnw

# Kiểm tra Java
java -version

# Build và chạy
./mvnw compile
./mvnw javafx:run
```

### Troubleshoot lỗi thường gặp

| Lỗi | Nguyên nhân | Cách fix |
|---|---|---|
| `JAVA_HOME is not set` | Chưa set biến môi trường | Xem Bước 0 + Bước 3 |
| `'mvnw.cmd' is not recognized` | Chưa `cd` vào thư mục dự án | `cd` đến thư mục `SatelliteSimulator` trước |
| `Connection refused` / `port 1433` | SQL Server chưa chạy | Mở Services → khởi động `SQL Server (MSSQLSERVER)` |
| `BUILD FAILURE` lần đầu | Maven đang tải thư viện | Chờ ~2 phút, cần internet, chạy lại |
| `Permission denied: ./mvnw` | Thiếu quyền thực thi (Mac/Linux) | `chmod +x ./mvnw` |

---

## Vị trí chèn trong README.md

```
[Yêu cầu hệ thống]  ← giữ nguyên
[Bước 0 — Tải và cài Java 21]  ← MỚI, chèn vào đây
[Bước 1 — Cài đặt CSDL]  ← đổi số thứ tự thành Bước 1
[Bước 2 — Cấu hình kết nối]
[Bước 3 — Cấu hình JAVA_HOME]  ← thêm note: nếu dùng installer .msi có tick, bỏ qua bước này
[Bước 4 — Build và chạy]  ← bổ sung verify + troubleshoot
[Chạy bằng Eclipse IDE]  ← bổ sung verify + lỗi thường gặp sau từng bước
```

---

## Không thay đổi

- Kiến trúc, thuật toán, cấu trúc project, hướng dẫn sử dụng UI
- Nội dung tiếng Việt hiện có — chỉ thêm, không xóa
- Các file khác trong repo

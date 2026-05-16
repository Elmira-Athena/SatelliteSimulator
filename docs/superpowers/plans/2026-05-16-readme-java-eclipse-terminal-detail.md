# README Java/Eclipse/Terminal Chi Tiết — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bổ sung hướng dẫn tải/cài Java 21 Temurin, verify steps, và troubleshoot lỗi thường gặp vào `README.md` để sinh viên Windows có thể cài đặt và chạy ứng dụng mà không cần tìm tài liệu bên ngoài.

**Architecture:** Chỉ chỉnh sửa `README.md` — không tạo file mới. Bốn nhóm thay đổi: (1) thêm mục Java installation trước "Hướng dẫn Cài đặt", (2) cập nhật note Bước 3 JAVA_HOME, (3) nâng cấp phần terminal Bước 4, (4) thêm verify + lỗi thường gặp sau từng bước Eclipse.

**Tech Stack:** Markdown, README.md

---

## File thay đổi

- Modify: `README.md` (duy nhất)

---

### Task 1: Thêm mục "Bước 0 — Tải và cài Java 21 (Temurin)"

**Files:**
- Modify: `README.md` — chèn section mới giữa dòng 74 (`---`) và dòng 75 (`## Hướng dẫn Cài đặt`)

- [ ] **Bước 1: Chèn section Bước 0 vào README.md**

Tìm đoạn này trong `README.md` (dòng 72–75):

```markdown
| Hệ điều hành | Windows 10+, macOS 12+, Ubuntu 20.04+ |

---

## Hướng dẫn Cài đặt
```

Thay bằng:

```markdown
| Hệ điều hành | Windows 10+, macOS 12+, Ubuntu 20.04+ |

---

## Bước 0 — Tải và cài Java 21 (Temurin)

> **Bỏ qua bước này nếu đã có JDK 21 trên máy.** Kiểm tra bằng lệnh `java -version` — nếu thấy `openjdk version "21.x.x"` thì tiến thẳng đến Bước 1.

### Windows (khuyến nghị)

1. Vào **[https://adoptium.net](https://adoptium.net)** → chọn **Temurin 21 (LTS)** → **Windows x64 Installer (.msi)**
2. Chạy file `.msi` vừa tải. Trong màn hình **Custom Setup**, đảm bảo tích chọn **cả hai** tùy chọn:
   - ✅ **Set JAVA_HOME variable**
   - ✅ **Add to PATH**
   > 💡 Nếu tích hai ô này, bạn có thể **bỏ qua Bước 3** (cấu hình JAVA_HOME thủ công) ở phần sau.
3. Nhấn **Install** → chờ cài xong → nhấn **Finish**
4. **Verify:** Mở **Command Prompt mới** (quan trọng: phải mở mới) và chạy:
   ```cmd
   java -version
   ```
   Kết quả đúng:
   ```
   openjdk version "21.x.x" ...
   OpenJDK Runtime Environment Temurin-21...
   ```

### macOS

1. Vào **[https://adoptium.net](https://adoptium.net)** → chọn **Temurin 21 (LTS)**:
   - Mac Intel: chọn **macOS x64 (.pkg)**
   - Mac Apple Silicon (M1/M2/M3): chọn **macOS aarch64 (.pkg)**
2. Chạy file `.pkg` → làm theo hướng dẫn cài đặt → PATH được cấu hình tự động
3. **Verify:** Mở Terminal mới và chạy:
   ```bash
   java -version
   ```

### Linux (Ubuntu / Debian)

```bash
sudo apt update && sudo apt install -y wget apt-transport-https
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
  | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" \
  | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update && sudo apt install -y temurin-21-jdk
java -version
```

---

## Hướng dẫn Cài đặt
```

- [ ] **Bước 2: Verify nội dung đã chèn đúng**

Mở `README.md`, tìm dòng `## Bước 0 — Tải và cài Java 21 (Temurin)` — phải xuất hiện ngay trước `## Hướng dẫn Cài đặt` và sau bảng Yêu cầu hệ thống.

- [ ] **Bước 3: Commit**

```bash
git add README.md
git commit -m "docs: add Temurin 21 installation guide (Windows/macOS/Linux)"
```

---

### Task 2: Cập nhật Bước 3 JAVA_HOME — thêm note bỏ qua nếu đã dùng installer

**Files:**
- Modify: `README.md` — heading của Bước 3 JAVA_HOME (hiện tại dòng ~100, sẽ dịch xuống sau Task 1)

- [ ] **Bước 1: Thêm note bỏ qua vào heading Bước 3**

Tìm đoạn này trong `README.md`:

```markdown
### Bước 3 — Cấu hình JAVA_HOME (bắt buộc trên Windows)

Maven wrapper yêu cầu biến môi trường `JAVA_HOME` trỏ đến thư mục JDK:
```

Thay bằng:

```markdown
### Bước 3 — Cấu hình JAVA_HOME (bắt buộc trên Windows)

> 💡 **Bỏ qua bước này** nếu bạn đã tích chọn **"Set JAVA_HOME variable"** trong installer Temurin ở Bước 0. Kiểm tra: mở CMD mới → `echo %JAVA_HOME%` → nếu thấy đường dẫn JDK thì đã xong.

Maven wrapper yêu cầu biến môi trường `JAVA_HOME` trỏ đến thư mục JDK:
```

- [ ] **Bước 2: Verify**

Mở `README.md`, tìm `### Bước 3` — phải thấy blockquote `💡 **Bỏ qua bước này**` ngay dưới heading.

- [ ] **Bước 3: Commit**

```bash
git add README.md
git commit -m "docs: add skip note to JAVA_HOME step for Temurin installer users"
```

---

### Task 3: Nâng cấp phần terminal Bước 4 — thêm verify và troubleshoot

**Files:**
- Modify: `README.md` — section Bước 4 Build và chạy (hiện tại dòng ~112)

- [ ] **Bước 1: Thay thế nội dung Bước 4**

Tìm đoạn này trong `README.md`:

```markdown
### Bước 4 — Build và chạy ứng dụng

**Windows (Command Prompt / PowerShell):**
```cmd
mvnw.cmd compile
mvnw.cmd javafx:run
```

**macOS / Linux:**
```bash
./mvnw compile
./mvnw javafx:run
```

> **Lưu ý:** Lần đầu chạy Maven sẽ tự động tải thư viện JavaFX 21 và JDBC driver (~100MB). Cần kết nối internet.
```

Thay bằng:

````markdown
### Bước 4 — Build và chạy ứng dụng

**Windows (Command Prompt / PowerShell):**
```cmd
:: 1. Kiểm tra Java trước khi chạy
java -version
:: Phải thấy: openjdk version "21.x.x" — nếu không thấy, xem lại Bước 0

:: 2. Kiểm tra JAVA_HOME
echo %JAVA_HOME%
:: Phải thấy đường dẫn JDK (ví dụ: C:\Program Files\Eclipse Adoptium\jdk-21...)
:: Nếu trống → thực hiện Bước 3

:: 3. Build và chạy (phải đứng trong thư mục SatelliteSimulator)
mvnw.cmd compile
mvnw.cmd javafx:run
```

**macOS / Linux:**
```bash
# Lần đầu tiên: cấp quyền thực thi cho Maven wrapper
chmod +x ./mvnw

# Kiểm tra Java
java -version
# Phải thấy: openjdk version "21.x.x"

# Build và chạy
./mvnw compile
./mvnw javafx:run
```

> **Lưu ý:** Lần đầu chạy Maven sẽ tự động tải thư viện JavaFX 21 và JDBC driver (~100MB). Cần kết nối internet và chờ ~2 phút.

#### Troubleshoot lỗi thường gặp khi chạy terminal

| Lỗi | Nguyên nhân | Cách fix |
|---|---|---|
| `JAVA_HOME is not set` | Chưa set biến môi trường | Xem lại **Bước 0** (tích ô trong installer) hoặc **Bước 3** (set thủ công) |
| `'mvnw.cmd' is not recognized` | Chưa `cd` vào thư mục dự án | Chạy `cd đường-dẫn\SatelliteSimulator` trước |
| `Connection refused` / lỗi `port 1433` | SQL Server chưa chạy | Mở **Services** (Win+R → `services.msc`) → tìm `SQL Server (MSSQLSERVER)` → Start |
| `BUILD FAILURE` ở lần đầu | Maven đang tải thư viện | Chờ ~2 phút, cần internet, sau đó chạy lại |
| `Permission denied: ./mvnw` | Thiếu quyền thực thi (Mac/Linux) | Chạy `chmod +x ./mvnw` một lần |
````

- [ ] **Bước 2: Verify**

Mở `README.md`, kiểm tra `### Bước 4` có bảng Troubleshoot 5 hàng và có khối `chmod +x ./mvnw`.

- [ ] **Bước 3: Commit**

```bash
git add README.md
git commit -m "docs: enhance terminal section with verify steps and troubleshoot table"
```

---

### Task 4: Thêm verify + lỗi thường gặp vào phần Eclipse

**Files:**
- Modify: `README.md` — section "Chạy bằng Eclipse IDE" (4 bước Eclipse)

- [ ] **Bước 1: Thêm verify sau Eclipse Bước 1 (Import)**

Tìm đoạn này trong `README.md`:

```markdown
5. Chờ Eclipse tải toàn bộ thư viện Maven (lần đầu ~100MB, cần internet)

### Bước 2 — Cấu hình JDK 21
```

Thay bằng:

```markdown
5. Chờ Eclipse tải toàn bộ thư viện Maven (lần đầu ~100MB, cần internet)

> ✅ **Kiểm tra:** Project xuất hiện trong **Package Explorer** mà không có dấu ❌ đỏ trên icon → import thành công.

### Bước 2 — Cấu hình JDK 21
```

- [ ] **Bước 2: Thêm verify + lỗi thường gặp sau Eclipse Bước 2 (Cấu hình JDK)**

Tìm đoạn này trong `README.md`:

```markdown
5. Chuột phải vào project → **Maven → Update Project** → nhấn **OK**

### Bước 3 — Chạy ứng dụng (cách đơn giản nhất)
```

Thay bằng:

```markdown
5. Chuột phải vào project → **Maven → Update Project** → nhấn **OK**

> ✅ **Kiểm tra:** Chuột phải project → **Properties → Java Build Path → Libraries** → phải thấy `JRE System Library [JavaSE-21]`.

> ❌ **Lỗi "Java compiler compliance" hiển thị 1.5 / 1.7 / 1.8:** Chuột phải project → **Properties → Java Compiler** → bỏ tích **"Use compliance from execution environment"** → chọn **21** → **Apply and Close**.

### Bước 3 — Chạy ứng dụng (cách đơn giản nhất)
```

- [ ] **Bước 3: Thêm verify + lỗi thường gặp sau Eclipse Bước 3 (Run)**

Tìm đoạn này trong `README.md`:

```markdown
Ứng dụng sẽ khởi động. Lần sau chỉ cần nhấn nút **Run** (▶) màu xanh lá.

### Bước 4 — Chạy Database Setup trong Eclipse
```

Thay bằng:

```markdown
Ứng dụng sẽ khởi động. Lần sau chỉ cần nhấn nút **Run** (▶) màu xanh lá.

> ✅ **Kiểm tra:** Console Eclipse hiển thị dòng `JavaFX Application Thread` và cửa sổ 3D mô phỏng vệ tinh xuất hiện.

> ❌ **Lỗi "No suitable driver found" hoặc DB connection thất bại:** SQL Server chưa chạy hoặc sai mật khẩu. Kiểm tra lại `DatabaseConnection.java` (Bước 2 phần Hướng dẫn Cài đặt) và đảm bảo SQL Server service đang hoạt động.

> ❌ **Lỗi "Error occurred during initialization of boot layer" (JavaFX module error):** Nguyên nhân do nhấn **Run** trực tiếp trên `MainApp.java` thay vì Maven. Phải dùng **Run As → Maven Build → Goals: `javafx:run`** — không chạy `MainApp.java` bằng nút Run thông thường.

### Bước 4 — Chạy Database Setup trong Eclipse
```

- [ ] **Bước 4: Thêm note sau Eclipse Bước 4 (SQL)**

Tìm đoạn này trong `README.md`:

```markdown
> **Nếu không thấy Data Source Explorer:** Cài thêm plugin **Eclipse Data Tools Platform (DTP)** qua **Help → Eclipse Marketplace** → tìm "DTP".
```

Thay bằng:

```markdown
> **Nếu không thấy Data Source Explorer:** Cài thêm plugin **Eclipse Data Tools Platform (DTP)** qua **Help → Eclipse Marketplace** → tìm "DTP".

> 💡 **Cách đơn giản hơn:** Nếu không muốn cài DTP, dùng **SQL Server Management Studio (SSMS)** để chạy script SQL — xem lại Bước 1 phần Hướng dẫn Cài đặt.
```

- [ ] **Bước 5: Verify toàn bộ Eclipse section**

Mở `README.md`, cuộn qua phần `## Chạy bằng Eclipse IDE`, kiểm tra:
- Sau Bước 1: có blockquote `✅ Kiểm tra: Project xuất hiện...`
- Sau Bước 2: có `✅ Kiểm tra: JRE System Library [JavaSE-21]` và `❌ Lỗi "Java compiler compliance"`
- Sau Bước 3: có `✅ Kiểm tra: Console...` và 2 blockquote `❌` lỗi
- Sau Bước 4: có `💡 Cách đơn giản hơn...`

- [ ] **Bước 6: Commit**

```bash
git add README.md
git commit -m "docs: add verify steps and error handling to Eclipse IDE section"
```

---

## Self-Review

**Spec coverage:**
- ✅ Section 1 (Java Temurin install Windows/macOS/Linux) → Task 1
- ✅ Section 2 (Eclipse verify + errors sau mỗi bước) → Task 4
- ✅ Section 3 (Terminal verify + troubleshoot) → Task 3
- ✅ Bước 3 JAVA_HOME skip note → Task 2

**Placeholder scan:** Không có TBD, TODO, hay "similar to Task N".

**Type consistency:** Chỉ là Markdown edits, không có type/method references.

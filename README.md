# Satellite Orbit Simulator & Router

Ứng dụng mô phỏng **3D quỹ đạo vệ tinh** và **định tuyến mạng không gian thời gian thực**, xây dựng bằng Java 21 và JavaFX 21. Hệ thống tính toán vị trí vệ tinh dựa trên vật lý thực tế và tìm tuyến truyền tin có độ trễ thấp nhất qua lưới vệ tinh bằng thuật toán Dijkstra.

---

## Tính năng chính

| Tính năng | Mô tả |
|---|---|
| **Mô phỏng 3D** | Hiển thị Trái Đất với texture độ phân giải cao, hỗ trợ xoay và zoom camera tự do |
| **Vật lý quỹ đạo** | Tính vận tốc quỹ đạo thực tế theo định luật hấp dẫn Newton; vệ tinh di chuyển liên tục ~60 FPS |
| **Line of Sight** | Kiểm tra tia nhìn bằng ray casting 3D — loại trừ các kết nối bị Trái Đất che khuất |
| **Định tuyến động** | Xây dựng đồ thị mạng mỗi frame; thuật toán Dijkstra tìm đường ít trễ nhất |
| **Live Routing** | Chọn hai trạm mặt đất, đường đi được hiển thị trực quan bằng tia laser xanh lá |
| **Benchmark** | Tạo kịch bản ngẫu nhiên 10–200 vệ tinh, đo tỉ lệ kết nối, số hop và độ trễ trung bình |
| **Thêm vật thể** | Thêm vệ tinh / trạm mặt đất mới trực tiếp qua UI, lưu vào cơ sở dữ liệu ngay lập tức |

---

## Kiến trúc hệ thống

Dự án tuân theo kiến trúc **N-Layer** với chiều phụ thuộc một chiều: `UI → Services → Core → Data`.

```
┌─────────────────────────────────────────────────┐
│  UI Layer (com.satellitesim.ui)                  │
│  MainApp · SpaceView · ObjectRenderer · RenderUtils│
│  Khởi tạo ứng dụng, xử lý sự kiện, render JavaFX │
└──────────────────┬──────────────────────────────┘
                   │ gọi
┌──────────────────▼──────────────────────────────┐
│  Service Layer (com.satellitesim.services)        │
│  PhysicsEngine · SimulationEngine                │
│  routing/ RoutingEngine · RoutingEvaluator        │
│  Vật lý quỹ đạo, game loop, Dijkstra, benchmark  │
└──────────────────┬──────────────────────────────┘
                   │ sử dụng
┌──────────────────▼──────────────────────────────┐
│  Core Layer (com.satellitesim.core)               │
│  models/ SpaceObject · Satellite · GroundStation  │
│          RouteEdge · SpaceObjectType              │
│  constants/ PhysicsConstants                      │
│  utils/ CoordinateConverter                       │
│  Domain model thuần túy, không phụ thuộc gì khác │
└──────────────────┬──────────────────────────────┘
                   │ đọc/ghi
┌──────────────────▼──────────────────────────────┐
│  Data Layer (com.satellitesim.data)               │
│  db/ DatabaseConnection (Singleton)               │
│  repository/ SpaceObjectRepository + Impl         │
│  Truy cập SQL Server qua JDBC                     │
└─────────────────────────────────────────────────┘
```

### Luồng dữ liệu chính

1. **Khởi động:** `MainApp` tải dữ liệu từ SQL Server, tạo các node JavaFX `Sphere` tương ứng.
2. **Game loop (~60 FPS):** `SimulationEngine` cập nhật kinh độ vệ tinh (`PhysicsEngine`), dịch chuyển node 3D, nếu có tuyến đang hiển thị thì xây đồ thị → Dijkstra → render laser.
3. **Định tuyến:** Mỗi frame, đồ thị được dựng lại O(N²) do vệ tinh di chuyển liên tục.

---

## Yêu cầu hệ thống

| Thành phần | Phiên bản tối thiểu |
|---|---|
| Java JDK | **21** trở lên |
| Apache Maven | **3.8** trở lên (hoặc dùng `./mvnw` đính kèm) |
| SQL Server | **2019** trở lên |
| Hệ điều hành | Windows 10+, macOS 12+, Ubuntu 20.04+ |

---

## Hướng dẫn Cài đặt

### Bước 1 — Cài đặt Cơ sở dữ liệu

1. Mở **SQL Server Management Studio (SSMS)** và kết nối đến instance SQL Server cục bộ.
2. Mở và chạy toàn bộ file:
   ```
   src/main/resources/sql/database_setup.sql
   ```
3. Script sẽ tự động:
   - Tạo database `SatelliteSimulation`
   - Tạo bảng `SpaceObjects` với các cột: `Id`, `Name`, `Type`, `Latitude`, `Longitude`, `Altitude`
   - Chèn dữ liệu mẫu: **5 trạm mặt đất** (Hà Nội, New York, London, Tokyo, Sydney) và **10 vệ tinh**

### Bước 2 — Cấu hình kết nối

Mở file `src/main/java/com/satellitesim/data/db/DatabaseConnection.java` và cập nhật thông tin đăng nhập nếu khác mặc định:

```java
private static final String URL =
    "jdbc:sqlserver://localhost:1433;databaseName=SatelliteSimulation;encrypt=false;trustServerCertificate=true;";
private static final String USER     = "sa";            // Thay bằng username của bạn
private static final String PASSWORD = "your_password"; // Thay bằng password của bạn
```

### Bước 3 — Cấu hình JAVA_HOME (bắt buộc trên Windows)

Maven wrapper yêu cầu biến môi trường `JAVA_HOME` trỏ đến thư mục JDK:

1. Tìm đường dẫn JDK đã cài: `where java` trong CMD, lấy phần trước `\bin\java.exe`
   - Ví dụ: `C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot`
2. Vào **System Properties → Advanced → Environment Variables → New** (System variables):
   - Name: `JAVA_HOME`
   - Value: đường dẫn JDK ở trên
3. Thêm `%JAVA_HOME%\bin` vào biến `Path`
4. Mở lại Command Prompt để áp dụng thay đổi

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

---

## Chạy bằng Eclipse IDE

### Yêu cầu Eclipse

- **Eclipse IDE for Java Developers** phiên bản **2023-09** trở lên (hỗ trợ Java 21)
- Đã cài JDK 21 trên máy và thiết lập `JAVA_HOME` (xem Bước 3 ở trên)

### Bước 1 — Import dự án vào Eclipse

1. Mở Eclipse, chọn **File → Import...**
2. Chọn **Maven → Existing Maven Projects** → nhấn **Next**
3. Tại **Root Directory**, nhấn **Browse** và chọn thư mục gốc `SatelliteSimulator`
4. Eclipse tự động nhận diện `pom.xml` — tích chọn project → nhấn **Finish**
5. Chờ Eclipse tải toàn bộ thư viện Maven (lần đầu ~100MB, cần internet)

### Bước 2 — Cấu hình JDK 21

Nếu Eclipse báo lỗi Java version, cần thêm JDK 21:

1. Vào **Window → Preferences → Java → Installed JREs → Add...**
2. Chọn **Standard VM** → **Next**
3. Tại **JRE home**, nhấn **Directory** và chọn thư mục JDK 21
   - Ví dụ: `C:\Program Files\Eclipse Adoptium\jdk-21.0.3.9-hotspot`
4. Nhấn **Finish** → tích chọn JDK 21 vừa thêm → **Apply and Close**
5. Chuột phải vào project → **Maven → Update Project** → nhấn **OK**

### Bước 3 — Chạy ứng dụng (cách đơn giản nhất)

**Dùng Maven Build (khuyến nghị — không cần cấu hình thêm):**

1. Chuột phải vào tên project trong **Package Explorer**
2. Chọn **Run As → Maven Build...**
3. Tại ô **Goals**, nhập: `javafx:run`
4. Nhấn **Run**

Ứng dụng sẽ khởi động. Lần sau chỉ cần nhấn nút **Run** (▶) màu xanh lá.

### Bước 4 — Chạy Database Setup trong Eclipse

Thay vì dùng SSMS, có thể chạy script SQL ngay trong Eclipse:

1. Vào **Window → Show View → Other → Data Management → Data Source Explorer**
2. Chuột phải **Database Connections → New...**
3. Chọn **SQL Server**, điền thông tin kết nối:
   - Host: `localhost`, Port: `1433`
   - Username: `sa`, Password: mật khẩu của bạn
4. Sau khi kết nối thành công, mở file `src/main/resources/sql/database_setup.sql`
5. Chuột phải vào nội dung file → **Execute SQL**

> **Nếu không thấy Data Source Explorer:** Cài thêm plugin **Eclipse Data Tools Platform (DTP)** qua **Help → Eclipse Marketplace** → tìm "DTP".

---

## Hướng dẫn Sử dụng

### Điều khiển camera

| Thao tác | Kết quả |
|---|---|
| Giữ chuột trái + kéo | Xoay Trái Đất |
| Cuộn con lăn chuột | Zoom in / zoom out |

### Panel Live Routing (bên phải)

1. Chọn **trạm nguồn** tại ô "From".
2. Chọn **trạm đích** tại ô "To".
3. Ứng dụng tự động hiển thị tuyến ngắn nhất bằng **tia laser xanh**. Nhãn `CONNECTED` / `NO ROUTE FOUND` cho biết trạng thái tức thời.

### Thêm vật thể mới

Điền đầy đủ thông tin vào form **Add Space Object**:
- **Name:** Tên định danh
- **Type:** `SATELLITE` hoặc `GROUND_STATION`
- **Lat / Lon / Alt:** Vĩ độ (−90 đến 90), Kinh độ (−180 đến 180), Độ cao km (= 0 với trạm mặt đất)

Nhấn **Add to Orbit & Database** — vật thể xuất hiện ngay trên màn hình và được lưu vào SQL Server.

### Benchmark

| Nút | Ý nghĩa |
|---|---|
| `10 / 50 / 100 / 200` | Tạo kịch bản với số vệ tinh ngẫu nhiên tương ứng |
| `Reset to DB` | Xóa vệ tinh ngẫu nhiên, trở về dữ liệu gốc trong CSDL |

Kết quả hiển thị sau khi benchmark hoàn tất:
- **Success:** Tỉ lệ cặp trạm kết nối được (%)
- **Hops:** Số bước nhảy trung bình
- **Latency:** Độ trễ trung bình (giây)

---

## Cấu trúc Dự án

```
SatelliteSimulator/
├── docs/
│   └── ALGORITHMS.md              # Giải thích chi tiết toán học và thuật toán
├── src/
│   ├── main/
│   │   ├── java/com/satellitesim/
│   │   │   ├── core/
│   │   │   │   ├── constants/
│   │   │   │   │   └── PhysicsConstants.java   # Hằng số vật lý (G, M_earth, c, ...)
│   │   │   │   ├── models/
│   │   │   │   │   ├── SpaceObject.java         # Abstract base: id, lat, lon, alt
│   │   │   │   │   ├── Satellite.java           # Kế thừa SpaceObject, tính v_orbital
│   │   │   │   │   ├── GroundStation.java       # Kế thừa SpaceObject, altitude = 0
│   │   │   │   │   ├── SpaceObjectType.java     # Enum: SATELLITE | GROUND_STATION
│   │   │   │   │   └── RouteEdge.java           # Cạnh đồ thị: source, dest, totalDelay
│   │   │   │   └── utils/
│   │   │   │       └── CoordinateConverter.java # Spherical <-> Cartesian (JavaFX)
│   │   │   ├── data/
│   │   │   │   ├── db/
│   │   │   │   │   └── DatabaseConnection.java  # Singleton JDBC connection pool
│   │   │   │   └── repository/
│   │   │   │       ├── SpaceObjectRepository.java      # Interface
│   │   │   │       └── SpaceObjectRepositoryImpl.java  # SQL Server implementation
│   │   │   ├── services/
│   │   │   │   ├── PhysicsEngine.java           # LOS check, distance, delay, velocity
│   │   │   │   ├── SimulationEngine.java        # AnimationTimer game loop (~60 FPS)
│   │   │   │   ├── SpaceObjectService.java      # CRUD facade qua Repository
│   │   │   │   └── routing/
│   │   │   │       ├── RoutingEngine.java       # Xây đồ thị + thuật toán Dijkstra
│   │   │   │       └── RoutingEvaluator.java    # Tạo kịch bản ngẫu nhiên, benchmark
│   │   │   └── ui/
│   │   │       ├── MainApp.java                 # Entry point JavaFX Application
│   │   │       ├── SpaceView.java               # SubScene 3D, camera, ánh sáng
│   │   │       ├── ObjectRenderer.java          # Tạo/cập nhật Sphere và Cylinder
│   │   │       └── RenderUtils.java             # Tiện ích tính toán hình học 3D
│   │   └── resources/
│   │       ├── sql/
│   │       │   └── database_setup.sql           # Script khởi tạo CSDL
│   │       └── textures/
│   │           └── earth.jpg                    # Texture Trái Đất độ phân giải cao
│   └── test/
│       └── java/com/satellitesim/core/
│           └── DomainModelTest.java             # JUnit 5 unit tests
├── .gitignore
├── pom.xml                                      # Maven: Java 21, JavaFX 21, JDBC
└── README.md
```

---

## Thuật toán & Vật lý

Dự án áp dụng 5 nhóm toán học và thuật toán:

| # | Thuật toán | Mô tả ngắn |
|---|---|---|
| 1 | **Chuyển đổi tọa độ** | Spherical (lat/lon/alt) → Cartesian (X/Y/Z) cho JavaFX |
| 2 | **Vận tốc quỹ đạo** | `v = sqrt(GM / (R + h))` — Newton, tính tốc độ bay thực tế |
| 3 | **Line of Sight** | Ray casting + dot product: kiểm tra tia sóng có bị Trái Đất chặn không |
| 4 | **Dijkstra** | Tìm đường trễ thấp nhất trong đồ thị động, trọng số = `d/c + 0.01s` |
| 5 | **Laser 3D** | Dùng Cylinder + Cross/Dot product để vẽ kết nối giữa hai điểm trong không gian |

Chi tiết đầy đủ các công thức và bằng chứng toán học: xem [docs/ALGORITHMS.md](docs/ALGORITHMS.md).

---

## Chạy Tests

```bash
# Chạy toàn bộ test suite
./mvnw test

# Chạy một class test cụ thể
./mvnw test -Dtest=DomainModelTest
```

File test: `src/test/java/com/satellitesim/core/DomainModelTest.java`

| Nhóm test | Nội dung kiểm thử |
|---|---|
| `SatelliteTests` | Tạo vệ tinh, kiểm tra vận tốc quỹ đạo theo độ cao |
| `GroundStationTests` | Tạo trạm mặt đất, xác nhận altitude = 0 |
| `CoordinateConverterTests` | Round-trip Spherical → Cartesian, kiểm tra sai số |
| `PhysicsConstantsTests` | Xác nhận giá trị các hằng số vật lý |

> Tests không yêu cầu SQL Server — chạy độc lập ở tầng `core`.

---

## Nhóm phát triển

**Đội Antigravity** — Dự án môn học Lập trình Hướng đối tượng.

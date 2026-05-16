# PHÂN CÔNG CÔNG VIỆC

**Đề tài:** Mô phỏng và Định tuyến Vệ tinh 3D (Java + JavaFX + SQL Server)

| Thông tin | |
|---|---|
| Thành viên | Nguyễn Đắc Vinh — Bùi Nguyễn Thảo Nguyên |
| Tỉ lệ đóng góp | 50% / 50% |
| Công nghệ | Java 21, JavaFX 21, SQL Server 2019 |

---

## Nguyễn Đắc Vinh

### Yêu cầu 1 — Kiến trúc hệ thống & Giao diện 3D

- Thiết kế kiến trúc N-Layer: tách biệt hoàn toàn các tầng UI → Services → Core → Data, tuân thủ nguyên tắc SOLID
- Xây dựng cảnh 3D bằng JavaFX: render hành tinh bằng `Sphere` + `PhongMaterial` (texture map từ solarsystemscope.com) + `PerspectiveCamera`
- Lập trình tương tác chuột: xoay (rotate) và phóng to/thu nhỏ (zoom) quanh hành tinh
- Xây dựng UI Controls: panel cấu hình số lượng vệ tinh, chọn điểm nguồn A và điểm đích B, nút "Start Simulation" và "Find Route"
- Hiển thị danh sách vật thể trên sidebar và số liệu tuyến đường (route metrics) theo thời gian thực
- Xây dựng `SpaceObjectService`: tải dữ liệu vật thể từ Repository, cung cấp cho `SimulationEngine` và `ObjectRenderer` khởi tạo cảnh 3D

**Files phụ trách:**
- `src/main/java/com/satellitesim/ui/MainApp.java`
- `src/main/java/com/satellitesim/ui/SpaceView.java`
- `src/main/java/com/satellitesim/ui/ObjectRenderer.java`
- `src/main/java/com/satellitesim/ui/RenderUtils.java`
- `src/main/java/com/satellitesim/services/SpaceObjectService.java`

---

### Yêu cầu 3 — Engine Mô phỏng Vật lý

- Đóng gói toàn bộ hằng số vật lý (G, M, R, c) trong class `PhysicsConstants` — không hardcode rải rác
- Tính toán vận tốc quỹ đạo ban đầu theo công thức:

$$v = \sqrt{\frac{G \cdot M}{R + h}}$$

- Xây dựng Game Loop sử dụng `AnimationTimer` (~60 FPS): mỗi frame cập nhật góc quay vệ tinh theo vận tốc góc $\omega = v / (R + h)$, tính toán lại tọa độ mới, đẩy sang View render — không blocking UI thread
- Cài đặt thuật toán kiểm tra Line of Sight (LOS): kiểm tra đoạn thẳng 3D nối 2 node có giao với mặt cầu hành tinh hay không (ray-sphere intersection)
- Cài đặt `CoordinateConverter`: chuyển đổi tọa độ cầu (Lat, Lon, Alt) sang tọa độ Descartes (X, Y, Z) phục vụ render pipeline

$$X = (R + h)\cos(\varphi)\cos(\lambda), \quad Y = (R + h)\cos(\varphi)\sin(\lambda), \quad Z = (R + h)\sin(\varphi)$$

**Files phụ trách:**
- `src/main/java/com/satellitesim/core/constants/PhysicsConstants.java`
- `src/main/java/com/satellitesim/services/PhysicsEngine.java`
- `src/main/java/com/satellitesim/services/SimulationEngine.java`
- `src/main/java/com/satellitesim/core/utils/CoordinateConverter.java`

---

### Testing

- Viết và duy trì toàn bộ unit tests với JUnit 5
- Bao gồm: kiểm tra tính toán vận tốc vệ tinh, `CoordinateConverter` round-trip, validation domain models, `PhysicsConstants`

**Files phụ trách:**
- `src/test/java/com/satellitesim/core/DomainModelTest.java`

---

## Bùi Nguyễn Thảo Nguyên

### Yêu cầu 2 — Cơ sở dữ liệu & Toán học không gian

- Thiết kế schema SQL Server: bảng `SpaceObjects` (ID, Name, Type, Latitude, Longitude, Altitude), script khởi tạo DB và seed dữ liệu mẫu (5 trạm mặt đất + 10 vệ tinh)
- Cài đặt `DatabaseConnection` singleton với JDBC, quản lý kết nối đến SQL Server
- Xây dựng Repository pattern: interface `SpaceObjectRepository` và implementation `SpaceObjectRepositoryImpl` — tách biệt hoàn toàn logic DB khỏi tầng Service
- Xây dựng các domain model: `SpaceObject` (abstract), `Satellite`, `GroundStation`, `SpaceObjectType`
- Thiết kế công thức chuyển đổi tọa độ cầu → Descartes (được implement bởi `CoordinateConverter` trong pipeline render)

**Files phụ trách:**
- `src/main/resources/sql/database_setup.sql`
- `src/main/java/com/satellitesim/data/db/DatabaseConnection.java`
- `src/main/java/com/satellitesim/data/repository/SpaceObjectRepository.java`
- `src/main/java/com/satellitesim/data/repository/SpaceObjectRepositoryImpl.java`
- `src/main/java/com/satellitesim/core/models/SpaceObject.java`
- `src/main/java/com/satellitesim/core/models/Satellite.java`
- `src/main/java/com/satellitesim/core/models/GroundStation.java`
- `src/main/java/com/satellitesim/core/models/SpaceObjectType.java`

---

### Yêu cầu 4 — Thuật toán Định tuyến Mạng

- Xây dựng đồ thị động (Dynamic Graph): tại mỗi request, snapshot trạng thái vệ tinh hiện tại để dựng lại đồ thị — O(N²) LOS checks
- Cài đặt trọng số cạnh là tổng độ trễ:

$$D_{total} = D_{prop} + D_{proc} = \frac{distance}{c} + 0.01s$$

- Cài đặt thuật toán Dijkstra tìm đường đi có $D_{total}$ nhỏ nhất từ điểm A đến điểm B — không cho phép cạnh trực tiếp giữa hai trạm mặt đất
- Render đường tín hiệu: vẽ cylinder 3D màu xanh lá dọc theo các node trên tuyến đường tìm được
- Cài đặt `RoutingEvaluator`: đánh giá chất lượng tuyến (tổng độ trễ, số hop, thông lượng)

**Files phụ trách:**
- `src/main/java/com/satellitesim/services/routing/RoutingEngine.java`
- `src/main/java/com/satellitesim/services/routing/RoutingEvaluator.java`
- `src/main/java/com/satellitesim/core/models/RouteEdge.java`

---

### Yêu cầu 5 — Tài liệu & Báo cáo

- Viết hướng dẫn cài đặt đầy đủ (README): cài Java 21, JavaFX, SQL Server, cấu hình môi trường, chạy ứng dụng
- Viết tài liệu kỹ thuật (ALGORITHMS.md): giải thích các công thức toán học và thuật toán được sử dụng
- Thực hiện và báo cáo performance test: đo độ trễ, số hop, tỉ lệ thành công khi scale số vệ tinh (10 → 50 → 100 → 200)
- Soạn tài liệu ngữ cảnh AI (`AI_context/`): mô tả kiến trúc, vật lý, định tuyến, CSDL bằng tiếng Anh

**Files phụ trách:**
- `README.md`
- `ALGORITHMS.md`
- `AI_context/`

---

## Tổng hợp

| Thành viên | Yêu cầu phụ trách | Files |
|---|---|---|
| Nguyễn Đắc Vinh | YC1 (Kiến trúc & UI 3D) · YC3 (Physics Engine) · Testing | 10 files |
| Bùi Nguyễn Thảo Nguyên | YC2 (CSDL & Toán tọa độ) · YC4 (Định tuyến) · YC5 (Tài liệu) | 11 files |

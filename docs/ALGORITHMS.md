# Chi tiết Phép toán & Thuật toán Dự án Satellite Simulator

Tài liệu này giải thích chi tiết các nền tảng toán học và thuật toán khoa học máy tính được áp dụng trong hệ thống mô phỏng và định tuyến vệ tinh.

---

## 1. Toán học Không gian (3D Geometry & Coordinates)

### 1.1. Chuyển đổi hệ tọa độ (Spherical to Cartesian)
Hệ tọa độ thường dùng trên màn hình máy tính (và JavaFX) là hệ **Đề-các 3D (Cartesian - X, Y, Z)**. Tuy nhiên, Trái Đất sử dụng **Hệ tọa độ Cầu (Spherical)** thông qua Vĩ độ (Latitude), Kinh độ (Longitude), và Độ cao (Altitude).

Để máy tính vẽ được điểm lên không gian 3D, ta dùng thuật toán lượng giác để chuyển đổi:
- **Bán kính quỹ đạo (R):** $R_{orbit} = R_{earth} + \text{Altitude}$
- Đổi Lat/Lon từ Độ (Degrees) sang Radian: $\theta = \text{Lat} \cdot \frac{\pi}{180}$, $\phi = \text{Lon} \cdot \frac{\pi}{180}$

**Công thức chiếu lên không gian 3D:**
- Trục $X = R_{orbit} \cdot \cos(\theta) \cdot \cos(\phi)$
- Trục $Y = R_{orbit} \cdot \sin(\theta)$
- Trục $Z = R_{orbit} \cdot \cos(\theta) \cdot \sin(\phi)$

*(Xem code tại `CoordinateConverter.java`)*

---

## 2. Vật lý Không gian (Orbital Mechanics)

Để các vệ tinh bay tự nhiên trên màn hình thay vì đứng yên, chúng ta áp dụng Định luật Vạn vật Hấp dẫn của Newton.

### 2.1. Tính vận tốc quỹ đạo
Một vệ tinh bay quanh Trái Đất chịu lực hút hướng tâm. Vận tốc lý tưởng để nó không rơi xuống đất và không văng ra ngoài không gian (Vận tốc vũ trụ cấp 1 tại độ cao h) là:
$$v = \sqrt{\frac{G \cdot M}{R_{earth} + h}}$$
*Trong đó:*
- $G$: Hằng số hấp dẫn
- $M$: Khối lượng Trái Đất
- Phép tính trong code gộp $G \cdot M = 3.986 \times 10^5 \text{ km}^3/\text{s}^2$

### 2.2. Tính toán dịch chuyển (Kinematics)
Trong vòng lặp thời gian thực của game (Game Loop), sau lượng thời gian $\Delta t$ (giây):
- Vận tốc góc: $\omega = \frac{v}{R_{orbit}}$ (radian/giây)
- Góc Kinh độ thay đổi: $\Delta\text{Lon} = \omega \cdot \Delta t$
- Kinh độ mới = Kinh độ cũ + $\Delta\text{Lon}$

---

## 3. Thuật toán tầm nhìn (Line of Sight - LOS)

Bởi vì sóng từ vệ tinh A nối đến trạm mặt đất B đi theo đường thẳng, nó không thể xuyên qua Trái Đất (bị khối cầu cản lại). Nên hệ thống cần thuật toán **Ray Casting (Tìm Giao Cắt)** để biết A có "nhìn thấy" B không.

**Cách giải toán hình học Vectơ (Tìm khoảng cách từ tâm O đến đoạn thẳng AB):**
1. Đặt tâm Trái Đất là gốc tọa độ $O(0, 0, 0)$.
2. Vectơ tia truyền sóng $\vec{AB} = B - A$. Vectơ từ điểm phát qua tâm là $\vec{AO} = -A$.
3. Dùng tích vô hướng (Dot Product) để tìm **điểm gần tâm Trái Đất nhất** nằm trên bề mặt đường thẳng đi qua A và B:
   $$t = \frac{\vec{AO} \cdot \vec{AB}}{\vec{AB} \cdot \vec{AB}}$$
4. Nếu $t < 0$ hoặc $t > 1$, điểm gần nhất bị lọt ra ngoài ngoài phạm vi phân đoạn thẳng $[\text{A, B}]$. Ép $t$ về đoạn $[0, 1]$.
5. Tọa độ điểm gần O nhất: $C = A + t \cdot \vec{AB}$.
6. **Kết luận:** Khoảng cách $|\vec{OC}|$ chính là khoảng cách từ tâm Trái Đất đến tia sóng. 
   Nếu $|\vec{OC}| \ge R_{earth} - 0.1$ km, => Sóng KHÔNG chạm đất => Có Line of Sight. Ngược lại là bị chặn.

*(Xem code tại `PhysicsEngine.hasLineOfSight`)*

---

## 4. Lý thuyết Đồ thị & Định tuyến (Routing Algorithm)

### 4.1. Đồ thị động (Dynamic Graph)
Hệ thống mạng lưới được biểu diễn bằng một "Đồ thị" $G(V, E)$.
- **Đỉnh (Vertex - V):** Là tập hợp các Trạm mặt đất và Vệ tinh.
- **Cạnh (Edge - E):** Là kết nối khả thi giữa chúng. Nếu 2 đỉnh có `Line of Sight`, ta vẽ một cạnh nối chúng. Đồ thị này được tái tạo liên tục khoảng **60 lần/giây** (vì vệ tinh bay liên tục).
- **Trọng số cạnh (Weight):** Độ trễ tín hiệu $\text{Latency} = \frac{\text{Distance}}{\text{Tốc độ ánh sáng c}} + \text{Thời gian xử lý ở trạm}$.

### 4.2. Thuật toán Dijkstra tìm đường đi ngắn nhất
Đây là "linh hồn" của chức năng định tuyến, sẽ tìm một chuỗi các vệ tinh trên trời (Hops) để gửi dữ liệu từ Hà Nội tới New York sao cho tổng độ trễ là thấp nhất.

**Các bước hoạt động của Dijkstra trong dự án:**
1. Khai báo 2 bảng: `minDelay` (để nhớ độ trễ tốt nhất tới mỗi trạm) và `cameFrom` (để truy vết).
2. Dùng một Hàng đợi ưu tiên (`PriorityQueue`), chọn xuất phát ở trạm Nguồn `A` (độ trễ = 0).
3. Tại một trạm `U`, quét tia sóng xem các trạm/vệ tinh `V` xung quanh.
4. Tính độ trễ tiềm năng tới `V`: $\text{Delay}_{new} = \text{minDelay}[U] + \text{Weight}(U \to V)$.
5. Nếu $\text{Delay}_{new}$ nhỏ hơn kỷ lục trước đó tại `V`, cập nhật lại `minDelay[V]` và trỏ lại vào bảng truy vết là "Tôi đến `V` từ `U`". Đẩy `V` vào Hàng đợi.
6. Lặp lại cho đến khi Trạm Đích được lấy ra khỏi Hàng đợi. Dùng bảng `cameFrom` lần ngược từ Đích về Nguồn để ra Danh sách lộ trình ngắn nhất (Đó chính là tia Laser trên màn hình).

---

## 5. Đồ họa (3D Vector Math for UI)

JavaFX có hỗ trợ vẽ khối cầu tròn (`Sphere`), nhưng không hỗ trợ vẽ cạnh thẳng 3D (`Line3D`). Để vẽ dải Laser nổi giữa không trung, chúng tôi thủ thuật bằng cách dùng khối hình trụ Cột trụ (`Cylinder`) và xoay nó trong không trung cho đường kính trùng với tia kết nối.

**Toán học xoay 3D (Vector Math):**
1. Tìm điểm giữa (Midpoint): Dời trục tọa độ Cột Trụ tới phần giữa đoạn thẳng AB trên không gian.
2. Cột trụ mặc định sinh ra theo Trục dọng $Y$. Để xoay Cột trụ từ hướng $Y$ sang hướng mục tiêu là Vectơ sóng $\vec{AB}$, ta dùng:
   - Trục gốc quay: Tích có hướng (Cross Product) $\vec{Axis} = \vec{AB} \times \vec{Y}$. Đây là trục vuông góc với cả cột trụ và đường nối mạng. 
   - Góc quay: Tích vô hướng (Dot Product) $\theta = \arccos\left(\frac{\vec{AB} \cdot \vec{Y}}{|\vec{AB}| \cdot |\vec{Y}|}\right)$. 
3. Áp dụng góc xoay này vào hệ trục tự nhiên của JavaFX (`Affine` transform), biến cột trụ thông thường thành một luồng Lazer chéo vút cắt ngang không gian!

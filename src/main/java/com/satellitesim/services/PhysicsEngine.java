package com.satellitesim.services;

import com.satellitesim.core.constants.PhysicsConstants;
import com.satellitesim.core.models.Satellite;
import com.satellitesim.core.utils.CoordinateConverter;

/**
 * Engine mô phỏng vật lý quỹ đạo vệ tinh (Physics Simulation Engine).
 *
 * <p>Xử lý các tính toán vật lý bao gồm:</p>
 * <ul>
 *   <li>Cập nhật vị trí vệ tinh theo thời gian (orbital mechanics)</li>
 *   <li>Kiểm tra Line of Sight (LOS) giữa hai điểm</li>
 *   <li>Tính toán độ trễ truyền dẫn (propagation delay)</li>
 * </ul>
 *
 * <p>Sẽ được tích hợp với AnimationTimer của JavaFX trong Game Loop.</p>
 */
public class PhysicsEngine {

    /**
     * Cập nhật vị trí vệ tinh sau một khoảng thời gian deltaTime.
     *
     * <p>Vệ tinh di chuyển trên quỹ đạo tròn, cập nhật kinh độ (longitude)
     * dựa trên vận tốc góc tính từ vận tốc tuyến tính.</p>
     *
     * @param satellite Vệ tinh cần cập nhật
     * @param deltaTimeSeconds Khoảng thời gian trôi qua (giây)
     */
    public void updatePosition(Satellite satellite, double deltaTimeSeconds) {
        double orbitalRadius = PhysicsConstants.EARTH_RADIUS_KM + satellite.getAltitude();

        // Vận tốc góc (rad/s) = v (km/s) / r (km)
        double angularVelocity = satellite.getOrbitalVelocity() / orbitalRadius;

        // Góc quay trong deltaTime (radians)
        double deltaAngle = angularVelocity * deltaTimeSeconds;

        // Cập nhật kinh độ (đơn giản hóa: quỹ đạo trên mặt phẳng xích đạo)
        double newLongitude = satellite.getLongitude() + (deltaAngle * PhysicsConstants.RAD_TO_DEG);

        // Normalize longitude về [-180, 180]
        newLongitude = normalizeLongitude(newLongitude);

        satellite.setLongitude(newLongitude);
    }

    /**
     * Tính khoảng cách giữa hai điểm trong không gian 3D (km).
     *
     * @param lat1 Vĩ độ điểm 1 (degrees)
     * @param lon1 Kinh độ điểm 1 (degrees)
     * @param alt1 Độ cao điểm 1 (km)
     * @param lat2 Vĩ độ điểm 2 (degrees)
     * @param lon2 Kinh độ điểm 2 (degrees)
     * @param alt2 Độ cao điểm 2 (km)
     * @return Khoảng cách (km)
     */
    public double calculateDistance(double lat1, double lon1, double alt1,
                                    double lat2, double lon2, double alt2) {
        double[] pos1 = CoordinateConverter.sphericalToCartesian(lat1, lon1, alt1);
        double[] pos2 = CoordinateConverter.sphericalToCartesian(lat2, lon2, alt2);

        double dx = pos1[0] - pos2[0];
        double dy = pos1[1] - pos2[1];
        double dz = pos1[2] - pos2[2];

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Tính độ trễ truyền dẫn (Propagation Delay) giữa hai điểm.
     *
     * <p>Công thức: D_prop = distance / c</p>
     *
     * @param distanceKm Khoảng cách (km)
     * @return Thời gian trễ (giây)
     */
    public double calculatePropagationDelay(double distanceKm) {
        return distanceKm / PhysicsConstants.SPEED_OF_LIGHT_KM_S;
    }

    /**
     * Tính tổng độ trễ (Total Delay) tại một hop.
     *
     * <p>Công thức: D_total = D_prop + D_proc</p>
     *
     * @param distanceKm Khoảng cách (km)
     * @return Tổng độ trễ (giây)
     */
    public double calculateTotalDelay(double distanceKm) {
        return calculatePropagationDelay(distanceKm) + PhysicsConstants.PROCESSING_DELAY_S;
    }

    /**
     * Kiểm tra Line of Sight (LOS) giữa hai điểm.
     *
     * <p>Hai điểm CÓ LOS nếu đoạn thẳng nối chúng KHÔNG cắt qua
     * khối cầu hành tinh (bán kính = R).</p>
     *
     * <p>Sử dụng phương pháp hình học: tìm khoảng cách nhỏ nhất từ
     * tâm hành tinh đến đoạn thẳng AB. Nếu khoảng cách > R thì có LOS.</p>
     *
     * @param lat1 Vĩ độ điểm A (degrees)
     * @param lon1 Kinh độ điểm A (degrees)
     * @param alt1 Độ cao điểm A (km)
     * @param lat2 Vĩ độ điểm B (degrees)
     * @param lon2 Kinh độ điểm B (degrees)
     * @param alt2 Độ cao điểm B (km)
     * @return true nếu có Line of Sight (không bị che bởi hành tinh)
     */
    public boolean hasLineOfSight(double lat1, double lon1, double alt1,
                                   double lat2, double lon2, double alt2) {
        double[] a = CoordinateConverter.sphericalToCartesian(lat1, lon1, alt1);
        double[] b = CoordinateConverter.sphericalToCartesian(lat2, lon2, alt2);

        // Vector AB = B - A
        double abX = b[0] - a[0];
        double abY = b[1] - a[1];
        double abZ = b[2] - a[2];

        // Vector AO = O - A = -A (vì O = gốc tọa độ = tâm hành tinh)
        double aoX = -a[0];
        double aoY = -a[1];
        double aoZ = -a[2];

        double abDotAb = abX * abX + abY * abY + abZ * abZ;
        double aoDotAb = aoX * abX + aoY * abY + aoZ * abZ;

        // Tham số t cho điểm gần tâm nhất trên đoạn AB
        double t = aoDotAb / abDotAb;
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp [0, 1] để giới hạn trên đoạn

        // Tọa độ điểm gần tâm nhất
        double closestX = a[0] + t * abX;
        double closestY = a[1] + t * abY;
        double closestZ = a[2] + t * abZ;

        // Khoảng cách từ tâm hành tinh đến điểm gần nhất
        double distToCenter = Math.sqrt(
                closestX * closestX + closestY * closestY + closestZ * closestZ
        );

        // Sử dụng dung sai khoảng 0.1km để các trạm mặt đất (độ cao = 0, khoảng cách đúng bằng R)
        // không bị chặn do lỗi dấu phẩy động hoặc do phép so sánh lớn hơn tuyệt đối.
        return distToCenter >= PhysicsConstants.EARTH_RADIUS_KM - 0.1;
    }

    // ==================== Utility ====================

    /**
     * Normalize kinh độ về phạm vi [-180, 180].
     */
    private double normalizeLongitude(double longitude) {
        while (longitude > 180.0) longitude -= 360.0;
        while (longitude < -180.0) longitude += 360.0;
        return longitude;
    }
}

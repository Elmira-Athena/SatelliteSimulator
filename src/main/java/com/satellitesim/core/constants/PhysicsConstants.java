package com.satellitesim.core.constants;

/**
 * Hằng số vật lý sử dụng trong toàn bộ hệ thống mô phỏng.
 *
 * <p>Tập trung tất cả magic numbers vào một nơi duy nhất theo nguyên tắc SOLID.
 * Được tham chiếu bởi Physics Engine, Routing Service, và Coordinate Converter.</p>
 *
 * <p><strong>Quy ước đơn vị:</strong></p>
 * <ul>
 *   <li>Khoảng cách: km (trừ khi ghi chú khác)</li>
 *   <li>Khối lượng: kg</li>
 *   <li>Thời gian: s (giây)</li>
 *   <li>Vận tốc: km/s hoặc m/s (xem ghi chú từng hằng số)</li>
 * </ul>
 */
public final class PhysicsConstants {

    /** Private constructor - không cho phép khởi tạo instance. */
    private PhysicsConstants() {
        throw new UnsupportedOperationException("PhysicsConstants is a utility class and cannot be instantiated.");
    }

    // ==================== Hằng số Vật lý Cơ bản ====================

    /**
     * Hằng số hấp dẫn vạn vật (Gravitational Constant).
     * <p>Đơn vị: m³·kg⁻¹·s⁻² (SI)</p>
     */
    public static final double GRAVITATIONAL_CONSTANT = 6.674e-11;

    /**
     * Khối lượng Trái Đất.
     * <p>Đơn vị: kg</p>
     */
    public static final double EARTH_MASS_KG = 5.972e24;

    /**
     * Bán kính trung bình Trái Đất.
     * <p>Đơn vị: km</p>
     */
    public static final double EARTH_RADIUS_KM = 6_371.0;

    /**
     * Tốc độ ánh sáng trong chân không.
     * <p>Đơn vị: km/s</p>
     * <p>Dùng để tính độ trễ truyền dẫn (propagation delay).</p>
     */
    public static final double SPEED_OF_LIGHT_KM_S = 299_792.458;

    // ==================== Hằng số Mô phỏng ====================

    /**
     * Độ trễ xử lý tại mỗi node vệ tinh (Processing Delay).
     * <p>Đơn vị: giây (s)</p>
     * <p>Mặc định: 10ms = 0.01s</p>
     */
    public static final double PROCESSING_DELAY_S = 0.01;

    /**
     * Tích G * M (Gravitational Parameter μ) - tính sẵn để tối ưu hiệu năng.
     * <p>Đơn vị: m³·s⁻²</p>
     * <p>Dùng trong công thức: v = sqrt(μ / r)</p>
     */
    public static final double GRAVITATIONAL_PARAMETER = GRAVITATIONAL_CONSTANT * EARTH_MASS_KG;

    // ==================== Hằng số Chuyển đổi ====================

    /**
     * Hệ số chuyển đổi Degrees → Radians.
     * <p>Tương đương {@code Math.PI / 180.0}</p>
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0;

    /**
     * Hệ số chuyển đổi Radians → Degrees.
     * <p>Tương đương {@code 180.0 / Math.PI}</p>
     */
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    // ==================== Hằng số Hiển thị (Rendering) ====================

    /**
     * Hệ số tỉ lệ để render 3D (1 đơn vị render = bao nhiêu km thực).
     * <p>Có thể điều chỉnh tùy theo kích thước cửa sổ JavaFX.</p>
     */
    public static final double RENDER_SCALE_FACTOR = 0.01;
}

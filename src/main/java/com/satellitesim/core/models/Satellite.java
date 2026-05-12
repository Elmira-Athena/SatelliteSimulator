package com.satellitesim.core.models;

import com.satellitesim.core.constants.PhysicsConstants;

/**
 * Đại diện cho một vệ tinh quay quanh quỹ đạo hành tinh.
 *
 * <p>Vệ tinh có thêm thuộc tính vận tốc quỹ đạo, được tính toán tự động
 * dựa trên công thức vật lý: {@code v = sqrt(G * M / (R + h))}</p>
 *
 * <p>Trong hệ thống mô phỏng, vệ tinh là node trung gian trong đồ thị
 * định tuyến truyền thông.</p>
 */
public class Satellite extends SpaceObject {

    /**
     * Vận tốc quỹ đạo hiện tại (km/s).
     * Được tính toán dựa trên độ cao theo công thức cơ học quỹ đạo.
     */
    private double orbitalVelocity;

    /**
     * Tạo vệ tinh với ID tự động sinh.
     *
     * @param name      Tên vệ tinh (ví dụ: "SAT-001")
     * @param latitude  Vĩ độ ban đầu (degrees)
     * @param longitude Kinh độ ban đầu (degrees)
     * @param altitude  Độ cao quỹ đạo (km), phải > 0 cho vệ tinh
     */
    public Satellite(String name, double latitude, double longitude, double altitude) {
        super(name, latitude, longitude, altitude);
        this.orbitalVelocity = calculateOrbitalVelocity(altitude);
    }

    /**
     * Tạo vệ tinh với ID chỉ định (dùng khi load từ DB).
     *
     * @param id        Định danh duy nhất
     * @param name      Tên vệ tinh
     * @param latitude  Vĩ độ ban đầu (degrees)
     * @param longitude Kinh độ ban đầu (degrees)
     * @param altitude  Độ cao quỹ đạo (km)
     */
    public Satellite(String id, String name, double latitude, double longitude, double altitude) {
        super(id, name, latitude, longitude, altitude);
        this.orbitalVelocity = calculateOrbitalVelocity(altitude);
    }

    // ==================== Orbital Mechanics ====================

    /**
     * Tính vận tốc quỹ đạo theo công thức:
     * <pre>v = sqrt(G * M / (R + h))</pre>
     *
     * @param altitudeKm Độ cao so với bề mặt (km)
     * @return Vận tốc quỹ đạo (km/s)
     */
    public static double calculateOrbitalVelocity(double altitudeKm) {
        double radiusFromCenter = PhysicsConstants.EARTH_RADIUS_KM + altitudeKm;
        return Math.sqrt(
                (PhysicsConstants.GRAVITATIONAL_CONSTANT * PhysicsConstants.EARTH_MASS_KG)
                        / (radiusFromCenter * 1000.0) // Chuyển km -> m cho đồng nhất đơn vị
        ) / 1000.0; // Kết quả chuyển m/s -> km/s
    }

    /**
     * Cập nhật lại vận tốc quỹ đạo khi độ cao thay đổi.
     */
    public void recalculateOrbitalVelocity() {
        this.orbitalVelocity = calculateOrbitalVelocity(getAltitude());
    }

    // ==================== Getters & Setters ====================

    public double getOrbitalVelocity() {
        return orbitalVelocity;
    }

    /**
     * Override setAltitude để tự động tính lại vận tốc khi thay đổi độ cao.
     */
    @Override
    public void setAltitude(double altitude) {
        super.setAltitude(altitude);
        this.orbitalVelocity = calculateOrbitalVelocity(altitude);
    }

    // ==================== Abstract Implementation ====================

    @Override
    public SpaceObjectType getType() {
        return SpaceObjectType.SATELLITE;
    }

    @Override
    public String getDescription() {
        return String.format("Satellite '%s' | Alt: %.0f km | Orbital Velocity: %.3f km/s",
                getName(), getAltitude(), orbitalVelocity);
    }

    @Override
    public String toString() {
        return String.format("Satellite{id='%s', name='%s', lat=%.4f, lon=%.4f, alt=%.0f km, v=%.3f km/s}",
                getId(), getName(), getLatitude(), getLongitude(), getAltitude(), orbitalVelocity);
    }
}

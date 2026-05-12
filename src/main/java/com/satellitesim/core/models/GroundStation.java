package com.satellitesim.core.models;

/**
 * Đại diện cho một trạm mặt đất (Ground Station).
 *
 * <p>Trạm mặt đất nằm trên bề mặt hành tinh (altitude = 0) và đóng vai trò
 * là điểm đầu/cuối (source/destination) trong đồ thị định tuyến truyền thông.</p>
 *
 * <p>Trong bài toán routing, GroundStation là nơi phát sinh và kết thúc
 * yêu cầu truyền tin qua mạng lưới vệ tinh.</p>
 */
public class GroundStation extends SpaceObject {

    /**
     * Tạo trạm mặt đất với ID tự động sinh.
     * Altitude mặc định = 0 (trên bề mặt hành tinh).
     *
     * @param name      Tên trạm (ví dụ: "Hanoi Station")
     * @param latitude  Vĩ độ (degrees)
     * @param longitude Kinh độ (degrees)
     */
    public GroundStation(String name, double latitude, double longitude) {
        super(name, latitude, longitude, 0.0);
    }

    /**
     * Tạo trạm mặt đất với ID chỉ định (dùng khi load từ DB).
     *
     * @param id        Định danh duy nhất
     * @param name      Tên trạm
     * @param latitude  Vĩ độ (degrees)
     * @param longitude Kinh độ (degrees)
     */
    public GroundStation(String id, String name, double latitude, double longitude) {
        super(id, name, latitude, longitude, 0.0);
    }

    /**
     * Tạo trạm mặt đất ở độ cao tùy chỉnh (ví dụ: trạm trên núi).
     *
     * @param id        Định danh duy nhất
     * @param name      Tên trạm
     * @param latitude  Vĩ độ (degrees)
     * @param longitude Kinh độ (degrees)
     * @param altitude  Độ cao (km), thường rất nhỏ so với vệ tinh
     */
    public GroundStation(String id, String name, double latitude, double longitude, double altitude) {
        super(id, name, latitude, longitude, altitude);
    }

    // ==================== Abstract Implementation ====================

    @Override
    public SpaceObjectType getType() {
        return SpaceObjectType.GROUND_STATION;
    }

    @Override
    public String getDescription() {
        return String.format("Ground Station '%s' | Location: (%.4f°, %.4f°)",
                getName(), getLatitude(), getLongitude());
    }

    @Override
    public String toString() {
        return String.format("GroundStation{id='%s', name='%s', lat=%.4f, lon=%.4f}",
                getId(), getName(), getLatitude(), getLongitude());
    }
}

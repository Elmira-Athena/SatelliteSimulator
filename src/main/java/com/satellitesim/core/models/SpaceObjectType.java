package com.satellitesim.core.models;

/**
 * Enum phân loại các loại vật thể không gian trong hệ thống.
 * Tương ứng với cột Type trong bảng SpaceObjects (DB).
 *
 * <ul>
 *   <li>{@link #GROUND_STATION} (0) - Trạm mặt đất</li>
 *   <li>{@link #SATELLITE} (1) - Vệ tinh</li>
 * </ul>
 */
public enum SpaceObjectType {

    /** Trạm mặt đất - điểm thu/phát tín hiệu trên bề mặt hành tinh. */
    GROUND_STATION(0, "Ground Station"),

    /** Vệ tinh - vật thể quay quanh quỹ đạo hành tinh. */
    SATELLITE(1, "Satellite");

    private final int code;
    private final String displayName;

    SpaceObjectType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * Mã số lưu trong CSDL.
     *
     * @return Giá trị integer tương ứng
     */
    public int getCode() {
        return code;
    }

    /**
     * Tên hiển thị thân thiện.
     *
     * @return Tên hiển thị
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Chuyển đổi từ mã số (DB) sang enum.
     *
     * @param code Mã số từ CSDL
     * @return SpaceObjectType tương ứng
     * @throws IllegalArgumentException nếu mã không hợp lệ
     */
    public static SpaceObjectType fromCode(int code) {
        for (SpaceObjectType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SpaceObjectType code: " + code);
    }
}

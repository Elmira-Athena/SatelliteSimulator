package com.satellitesim.core.models;

import java.util.Objects;
import java.util.UUID;

/**
 * Lớp trừu tượng đại diện cho một vật thể trong không gian.
 * Lưu trữ tọa độ theo hệ cầu (Spherical Coordinates): Kinh độ, Vĩ độ, Độ cao.
 *
 * <p>Đây là base class cho tất cả các vật thể trong hệ thống mô phỏng,
 * bao gồm Vệ tinh (Satellite) và Trạm mặt đất (GroundStation).</p>
 */
public abstract class SpaceObject {

    /** Định danh duy nhất của vật thể. */
    private String id;

    /** Tên hiển thị của vật thể. */
    private String name;

    /** Vĩ độ (Latitude) tính bằng độ (degrees), phạm vi [-90, 90]. */
    private double latitude;

    /** Kinh độ (Longitude) tính bằng độ (degrees), phạm vi [-180, 180]. */
    private double longitude;

    /** Độ cao so với bề mặt hành tinh (km). */
    private double altitude;

    /**
     * Constructor đầy đủ tham số.
     *
     * @param id        Định danh duy nhất
     * @param name      Tên vật thể
     * @param latitude  Vĩ độ (degrees)
     * @param longitude Kinh độ (degrees)
     * @param altitude  Độ cao (km)
     */
    protected SpaceObject(String id, String name, double latitude, double longitude, double altitude) {
        this.id = id;
        this.name = name;
        setLatitude(latitude);
        setLongitude(longitude);
        setAltitude(altitude);
    }

    /**
     * Constructor với ID tự động sinh (UUID).
     *
     * @param name      Tên vật thể
     * @param latitude  Vĩ độ (degrees)
     * @param longitude Kinh độ (degrees)
     * @param altitude  Độ cao (km)
     */
    protected SpaceObject(String name, double latitude, double longitude, double altitude) {
        this(UUID.randomUUID().toString(), name, latitude, longitude, altitude);
    }

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     * Thiết lập vĩ độ với validation.
     *
     * @param latitude Vĩ độ (degrees), phải nằm trong [-90, 90]
     * @throws IllegalArgumentException nếu giá trị ngoài phạm vi
     */
    public void setLatitude(double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90 degrees. Got: " + latitude);
        }
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Thiết lập kinh độ với validation.
     *
     * @param longitude Kinh độ (degrees), phải nằm trong [-180, 180]
     * @throws IllegalArgumentException nếu giá trị ngoài phạm vi
     */
    public void setLongitude(double longitude) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180 degrees. Got: " + longitude);
        }
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    /**
     * Thiết lập độ cao với validation.
     *
     * @param altitude Độ cao (km), không được âm
     * @throws IllegalArgumentException nếu giá trị âm
     */
    public void setAltitude(double altitude) {
        if (altitude < 0) {
            throw new IllegalArgumentException(
                    "Altitude must be non-negative. Got: " + altitude);
        }
        this.altitude = altitude;
    }

    // ==================== Abstract Methods ====================

    /**
     * Trả về loại vật thể (dùng cho persistence và phân biệt loại).
     *
     * @return SpaceObjectType enum value
     */
    public abstract SpaceObjectType getType();

    /**
     * Trả về mô tả chi tiết của vật thể (dùng cho hiển thị UI).
     *
     * @return Chuỗi mô tả
     */
    public abstract String getDescription();

    // ==================== Utility Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpaceObject that = (SpaceObject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', name='%s', lat=%.4f, lon=%.4f, alt=%.2f km}",
                getClass().getSimpleName(), id, name, latitude, longitude, altitude);
    }
}

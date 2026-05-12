package com.satellitesim.core.utils;

import com.satellitesim.core.constants.PhysicsConstants;
import com.satellitesim.core.models.SpaceObject;

/**
 * Tiện ích chuyển đổi hệ tọa độ giữa Spherical (Cầu) và Cartesian (Descartes).
 *
 * <p>CSDL lưu trữ tọa độ cầu (Latitude, Longitude, Altitude) vì mang ý nghĩa
 * địa lý. Khi render lên JavaFX 3D, cần chuyển sang tọa độ Descartes (X, Y, Z).</p>
 *
 * <p><strong>Công thức chuyển đổi:</strong></p>
 * <pre>
 *   X = (R + h) * cos(lat) * cos(lon)
 *   Y = (R + h) * cos(lat) * sin(lon)
 *   Z = (R + h) * sin(lat)
 * </pre>
 *
 * <p><strong>Lưu ý:</strong> Input ở đơn vị Degrees, phải chuyển sang Radians
 * trước khi sử dụng hàm Math.</p>
 */
public final class CoordinateConverter {

    /** Private constructor - utility class. */
    private CoordinateConverter() {
        throw new UnsupportedOperationException("CoordinateConverter is a utility class.");
    }

    /**
     * Chuyển đổi tọa độ cầu sang tọa độ Descartes.
     *
     * @param latitudeDeg  Vĩ độ (degrees)
     * @param longitudeDeg Kinh độ (degrees)
     * @param altitudeKm   Độ cao (km)
     * @return Mảng double[3] chứa {X, Y, Z} tính bằng km
     */
    public static double[] sphericalToCartesian(double latitudeDeg, double longitudeDeg, double altitudeKm) {
        double latRad = latitudeDeg * PhysicsConstants.DEG_TO_RAD;
        double lonRad = longitudeDeg * PhysicsConstants.DEG_TO_RAD;
        double r = PhysicsConstants.EARTH_RADIUS_KM + altitudeKm;

        double x = r * Math.cos(latRad) * Math.cos(lonRad);
        double y = r * Math.cos(latRad) * Math.sin(lonRad);
        double z = r * Math.sin(latRad);

        return new double[]{x, y, z};
    }

    /**
     * Chuyển đổi tọa độ cầu sang tọa độ Descartes cho JavaFX render.
     * Áp dụng hệ số tỉ lệ {@link PhysicsConstants#RENDER_SCALE_FACTOR}.
     *
     * @param latitudeDeg  Vĩ độ (degrees)
     * @param longitudeDeg Kinh độ (degrees)
     * @param altitudeKm   Độ cao (km)
     * @return Mảng double[3] chứa {X, Y, Z} đã scale cho render
     */
    public static double[] sphericalToCartesianScaled(double latitudeDeg, double longitudeDeg, double altitudeKm) {
        double[] cartesian = sphericalToCartesian(latitudeDeg, longitudeDeg, altitudeKm);
        double scale = PhysicsConstants.RENDER_SCALE_FACTOR;
        return new double[]{
                cartesian[0] * scale,
                cartesian[1] * scale,
                cartesian[2] * scale
        };
    }

    /**
     * Tiện ích: Chuyển đổi trực tiếp từ SpaceObject.
     *
     * @param obj SpaceObject cần chuyển đổi
     * @return Mảng double[3] chứa {X, Y, Z} tính bằng km
     */
    public static double[] toCartesian(SpaceObject obj) {
        return sphericalToCartesian(obj.getLatitude(), obj.getLongitude(), obj.getAltitude());
    }

    /**
     * Chuyển đổi ngược từ Descartes sang tọa độ cầu.
     *
     * @param x Tọa độ X (km)
     * @param y Tọa độ Y (km)
     * @param z Tọa độ Z (km)
     * @return Mảng double[3] chứa {Latitude (deg), Longitude (deg), Altitude (km)}
     */
    public static double[] cartesianToSpherical(double x, double y, double z) {
        double r = Math.sqrt(x * x + y * y + z * z);
        double altitudeKm = r - PhysicsConstants.EARTH_RADIUS_KM;
        double latRad = Math.asin(z / r);
        double lonRad = Math.atan2(y, x);

        return new double[]{
                latRad * PhysicsConstants.RAD_TO_DEG,
                lonRad * PhysicsConstants.RAD_TO_DEG,
                altitudeKm
        };
    }

    /**
     * Tính khoảng cách Euclid giữa hai SpaceObject trong không gian 3D.
     *
     * @param a Vật thể thứ nhất
     * @param b Vật thể thứ hai
     * @return Khoảng cách (km)
     */
    public static double distanceBetween(SpaceObject a, SpaceObject b) {
        double[] posA = toCartesian(a);
        double[] posB = toCartesian(b);

        double dx = posA[0] - posB[0];
        double dy = posA[1] - posB[1];
        double dz = posA[2] - posB[2];

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

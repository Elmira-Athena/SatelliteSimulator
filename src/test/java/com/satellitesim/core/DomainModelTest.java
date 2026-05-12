package com.satellitesim.core;

import com.satellitesim.core.constants.PhysicsConstants;
import com.satellitesim.core.models.GroundStation;
import com.satellitesim.core.models.Satellite;
import com.satellitesim.core.models.SpaceObjectType;
import com.satellitesim.core.utils.CoordinateConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho các Domain Model trong core layer.
 */
class DomainModelTest {

    // ==================== SpaceObject / Satellite Tests ====================

    @Nested
    @DisplayName("Satellite Tests")
    class SatelliteTests {

        @Test
        @DisplayName("Tạo vệ tinh LEO (400km) - vận tốc quỹ đạo ≈ 7.67 km/s")
        void createLeoSatellite_shouldCalculateCorrectVelocity() {
            Satellite sat = new Satellite("SAT-001", 0.0, 0.0, 400.0);

            assertEquals("SAT-001", sat.getName());
            assertEquals(SpaceObjectType.SATELLITE, sat.getType());
            assertEquals(400.0, sat.getAltitude(), 0.001);

            // ISS orbits at ~400km → v ≈ 7.67 km/s
            double velocity = sat.getOrbitalVelocity();
            assertTrue(velocity > 7.5 && velocity < 7.9,
                    "LEO velocity should be ~7.67 km/s, got: " + velocity);
        }

        @Test
        @DisplayName("Vệ tinh GEO (35786km) - vận tốc quỹ đạo ≈ 3.07 km/s")
        void createGeoSatellite_shouldCalculateCorrectVelocity() {
            Satellite sat = new Satellite("GEO-001", 0.0, 100.0, 35_786.0);

            double velocity = sat.getOrbitalVelocity();
            assertTrue(velocity > 3.0 && velocity < 3.2,
                    "GEO velocity should be ~3.07 km/s, got: " + velocity);
        }

        @Test
        @DisplayName("Thay đổi altitude → tự động tính lại velocity")
        void setAltitude_shouldRecalculateVelocity() {
            Satellite sat = new Satellite("SAT-002", 0.0, 0.0, 400.0);
            double v1 = sat.getOrbitalVelocity();

            sat.setAltitude(800.0);
            double v2 = sat.getOrbitalVelocity();

            assertTrue(v2 < v1, "Higher altitude should result in lower velocity");
        }

        @Test
        @DisplayName("Latitude ngoài phạm vi → IllegalArgumentException")
        void invalidLatitude_shouldThrowException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Satellite("SAT-ERR", 91.0, 0.0, 400.0));
            assertThrows(IllegalArgumentException.class,
                    () -> new Satellite("SAT-ERR", -91.0, 0.0, 400.0));
        }

        @Test
        @DisplayName("Longitude ngoài phạm vi → IllegalArgumentException")
        void invalidLongitude_shouldThrowException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Satellite("SAT-ERR", 0.0, 181.0, 400.0));
        }

        @Test
        @DisplayName("Altitude âm → IllegalArgumentException")
        void negativeAltitude_shouldThrowException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Satellite("SAT-ERR", 0.0, 0.0, -100.0));
        }
    }

    // ==================== GroundStation Tests ====================

    @Nested
    @DisplayName("GroundStation Tests")
    class GroundStationTests {

        @Test
        @DisplayName("Tạo trạm mặt đất - altitude mặc định = 0")
        void createGroundStation_defaultAltitudeIsZero() {
            GroundStation gs = new GroundStation("Hanoi Station", 21.0285, 105.8542);

            assertEquals("Hanoi Station", gs.getName());
            assertEquals(SpaceObjectType.GROUND_STATION, gs.getType());
            assertEquals(0.0, gs.getAltitude(), 0.001);
            assertEquals(21.0285, gs.getLatitude(), 0.0001);
            assertEquals(105.8542, gs.getLongitude(), 0.0001);
        }

        @Test
        @DisplayName("Tạo trạm mặt đất với ID chỉ định")
        void createGroundStation_withId() {
            GroundStation gs = new GroundStation("gs-001", "Tokyo Station", 35.6762, 139.6503);

            assertEquals("gs-001", gs.getId());
            assertEquals("Tokyo Station", gs.getName());
        }
    }

    // ==================== CoordinateConverter Tests ====================

    @Nested
    @DisplayName("CoordinateConverter Tests")
    class CoordinateConverterTests {

        @Test
        @DisplayName("Chuyển đổi (0°, 0°, 0km) → (R, 0, 0)")
        void zeroLatLon_shouldReturnRadiusOnXAxis() {
            double[] xyz = CoordinateConverter.sphericalToCartesian(0.0, 0.0, 0.0);

            assertEquals(PhysicsConstants.EARTH_RADIUS_KM, xyz[0], 0.1, "X should be R");
            assertEquals(0.0, xyz[1], 0.1, "Y should be 0");
            assertEquals(0.0, xyz[2], 0.1, "Z should be 0");
        }

        @Test
        @DisplayName("Chuyển đổi (90°N, 0°, 0km) → (0, 0, R)")
        void northPole_shouldReturnRadiusOnZAxis() {
            double[] xyz = CoordinateConverter.sphericalToCartesian(90.0, 0.0, 0.0);

            assertEquals(0.0, xyz[0], 0.1, "X should be ~0");
            assertEquals(0.0, xyz[1], 0.1, "Y should be ~0");
            assertEquals(PhysicsConstants.EARTH_RADIUS_KM, xyz[2], 0.1, "Z should be R");
        }

        @Test
        @DisplayName("Chuyển đổi (0°, 90°E, 0km) → (0, R, 0)")
        void eastMeridian_shouldReturnRadiusOnYAxis() {
            double[] xyz = CoordinateConverter.sphericalToCartesian(0.0, 90.0, 0.0);

            assertEquals(0.0, xyz[0], 0.1, "X should be ~0");
            assertEquals(PhysicsConstants.EARTH_RADIUS_KM, xyz[1], 0.1, "Y should be R");
            assertEquals(0.0, xyz[2], 0.1, "Z should be ~0");
        }

        @Test
        @DisplayName("Chuyển đổi khứ hồi (round-trip): Spherical → Cartesian → Spherical")
        void roundTrip_shouldReturnOriginalValues() {
            double lat = 21.0285;  // Hanoi
            double lon = 105.8542;
            double alt = 400.0;

            double[] xyz = CoordinateConverter.sphericalToCartesian(lat, lon, alt);
            double[] result = CoordinateConverter.cartesianToSpherical(xyz[0], xyz[1], xyz[2]);

            assertEquals(lat, result[0], 0.001, "Latitude round-trip failed");
            assertEquals(lon, result[1], 0.001, "Longitude round-trip failed");
            assertEquals(alt, result[2], 0.1, "Altitude round-trip failed");
        }

        @Test
        @DisplayName("Khoảng cách giữa 2 điểm đối xứng trên xích đạo = 2R")
        void distanceBetweenAntipodal_shouldBeTwoR() {
            GroundStation gs1 = new GroundStation("A", 0.0, 0.0);
            GroundStation gs2 = new GroundStation("B", 0.0, 180.0);

            double distance = CoordinateConverter.distanceBetween(gs1, gs2);
            double expectedDiameter = 2 * PhysicsConstants.EARTH_RADIUS_KM;

            assertEquals(expectedDiameter, distance, 1.0,
                    "Distance between antipodal points should be 2R");
        }
    }

    // ==================== PhysicsConstants Tests ====================

    @Nested
    @DisplayName("PhysicsConstants Tests")
    class PhysicsConstantsTests {

        @Test
        @DisplayName("Hằng số G có giá trị hợp lệ")
        void gravitationalConstant_shouldBePositive() {
            assertTrue(PhysicsConstants.GRAVITATIONAL_CONSTANT > 0);
            assertEquals(6.674e-11, PhysicsConstants.GRAVITATIONAL_CONSTANT, 1e-14);
        }

        @Test
        @DisplayName("Bán kính Trái Đất ≈ 6371 km")
        void earthRadius_shouldBeCorrect() {
            assertEquals(6371.0, PhysicsConstants.EARTH_RADIUS_KM, 1.0);
        }

        @Test
        @DisplayName("Tốc độ ánh sáng ≈ 299792 km/s")
        void speedOfLight_shouldBeCorrect() {
            assertEquals(299792.458, PhysicsConstants.SPEED_OF_LIGHT_KM_S, 0.001);
        }

        @Test
        @DisplayName("DEG_TO_RAD * RAD_TO_DEG ≈ 1.0")
        void conversionFactors_shouldBeInverse() {
            assertEquals(1.0, PhysicsConstants.DEG_TO_RAD * PhysicsConstants.RAD_TO_DEG, 1e-10);
        }
    }
}

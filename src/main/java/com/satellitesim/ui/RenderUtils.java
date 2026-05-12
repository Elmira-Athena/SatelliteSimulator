package com.satellitesim.ui;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.io.InputStream;

/**
 * Tiện ích hỗ trợ khởi tạo Material và Hình khối 3D.
 */
public final class RenderUtils {

    private RenderUtils() {}

    /**
     * Tạo hình khối cầu biểu diễn Trái Đất.
     * Áp dụng vật liệu phong (PhongMaterial) kết hợp texture từ file.
     *
     * @param radius Bán kính (theo scale của JavaFX)
     * @return Đối tượng Sphere biểu diễn Trái Đất
     */
    public static Sphere createEarthSphere(double radius) {
        Sphere earth = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();

        try {
            // Tải file texture từ resources (src/main/resources/textures/earth.jpg)
            InputStream is = RenderUtils.class.getResourceAsStream("/textures/earth.jpg");
            if (is != null) {
                Image earthImage = new Image(is);
                material.setDiffuseMap(earthImage);
                // Giảm nhẹ phản chiếu ánh sáng để thực tế hơn
                material.setSpecularColor(Color.rgb(30, 30, 30));
            } else {
                System.err.println("Warning: earth.jpg not found in /textures. Falling back to color.");
                material.setDiffuseColor(Color.BLUE);
            }
        } catch (Exception e) {
            System.err.println("Error loading earth texture: " + e.getMessage());
            material.setDiffuseColor(Color.BLUE);
        }

        earth.setMaterial(material);
        return earth;
    }

    /**
     * Tạo Sphere với màu sắc cơ bản (dành cho vệ tinh, trạm mặt đất).
     *
     * @param radius Bán kính
     * @param color  Màu sắc
     * @return Sphere mang màu tương ứng
     */
    public static Sphere createColoredSphere(double radius, Color color) {
        Sphere sphere = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(Color.WHITE);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Tạo một tia laser (Hình trụ nhỏ) kết nối 2 điểm trong không gian 3D.
     * Sử dụng toán học Vector và biến đổi Transform để xoay chuẩn.
     *
     * @param a Điểm 3D nguồn
     * @param b Điểm 3D đích
     * @param color Màu tia laser
     * @return Cylinder kết xuất
     */
    public static javafx.scene.shape.Cylinder createConnectionLine(javafx.geometry.Point3D a, javafx.geometry.Point3D b, Color color) {
        javafx.geometry.Point3D yAxis = new javafx.geometry.Point3D(0, 1, 0); // Trục mặc định của Cylinder
        javafx.geometry.Point3D diff = b.subtract(a);
        double height = diff.magnitude();

        // Điểm giữa (MidPoint) của đoạn AB
        javafx.geometry.Point3D mid = a.midpoint(b);

        // Cylinder (Bán kính nhỏ xíu để giống tia laser)
        javafx.scene.shape.Cylinder line = new javafx.scene.shape.Cylinder(0.2, height);

        // Set Material
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        // Phát sáng mạnh giống laser
        material.setSpecularColor(Color.WHITE);
        line.setMaterial(material);

        // Di chuyển trung tâm Line đến MidPoint
        line.setTranslateX(mid.getX());
        line.setTranslateY(mid.getY());
        line.setTranslateZ(mid.getZ());

        // Xoay Axis - Cross product giữa vec trục Y mặc định và diff vector
        javafx.geometry.Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));

        javafx.scene.transform.Rotate rotateAroundCenter = new javafx.scene.transform.Rotate(-Math.toDegrees(angle), axisOfRotation);
        line.getTransforms().add(rotateAroundCenter);

        return line;
    }
}

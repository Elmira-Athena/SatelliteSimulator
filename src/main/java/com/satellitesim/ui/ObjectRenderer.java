package com.satellitesim.ui;

import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.core.models.SpaceObjectType;
import com.satellitesim.core.utils.CoordinateConverter;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xử lý việc hiển thị các vật thể (Vệ tinh, Trạm mặt đất) lên SpaceView.
 */
public class ObjectRenderer {

    private final Group objectGroup;
    
    // Map lưu giữ liên kết giữa dữ liệu (ID) và Graphic Object (Sphere)
    // Để sau này (phần Animation) có thể update trực tiếp vị trí thay vì tạo lại mới
    private final Map<String, Sphere> renderedObjects = new HashMap<>();

    public ObjectRenderer(Group objectGroup) {
        this.objectGroup = objectGroup;
    }

    /**
     * Khởi tạo và vẽ toàn bộ các vật thể tĩnh lên không gian 3D.
     * 
     * @param spaceObjects Danh sách các vật thể (từ DB)
     */
    public void renderObjects(List<SpaceObject> spaceObjects) {
        objectGroup.getChildren().clear();
        renderedObjects.clear();

        for (SpaceObject obj : spaceObjects) {
            Sphere node = createGraphicalNode(obj);
            updateNodePosition(node, obj);
            
            // Thêm vào danh sách quản lý và hiển thị
            renderedObjects.put(obj.getId(), node);
            objectGroup.getChildren().add(node);
        }
    }

    /**
     * Tạo hình khối tương ứng tùy vào loại Object.
     */
    private Sphere createGraphicalNode(SpaceObject obj) {
        if (obj.getType() == SpaceObjectType.GROUND_STATION) {
            // Trạm mặt đất: Hình cầu nhỏ màu xanh ngọc
            return RenderUtils.createColoredSphere(0.8, Color.CYAN);
        } else {
            // Vệ tinh: Hình cầu lớn hơn một chút màu cam sáng
            return RenderUtils.createColoredSphere(1.2, Color.ORANGE);
        }
    }

    /**
     * Cập nhật vị trí X, Y, Z cho Graphic Node dựa trên tọa độ cầu của SpaceObject.
     */
    public void updateNodePosition(Sphere node, SpaceObject obj) {
        // Tọa độ đã scale phù hợp với hiển thị
        double[] cartesianScaled = CoordinateConverter.sphericalToCartesianScaled(
                obj.getLatitude(), obj.getLongitude(), obj.getAltitude());

        node.setTranslateX(cartesianScaled[0]);
        node.setTranslateY(cartesianScaled[1]); // Trục Y JavaFX hướng xuống, nhưng trong render cầu 3D không bị ảnh hưởng nhiều nếu dùng góc nhìn chuẩn.
        node.setTranslateZ(cartesianScaled[2]);
    }

    /**
     * Lấy graphic node tương ứng của một đối tượng bằng ID.
     */
    public Sphere getNode(String id) {
        return renderedObjects.get(id);
    }

    // Danh sách các tia laser Line của Route hiện tại để có thể xoá bỏ khi update frame
    private final Group pathGroup = new Group();

    /**
     * Render một Route thẳng lên không gian.
     * Tự động xóa route cũ nếu có.
     */
    public void renderPath(List<SpaceObject> path) {
        // Xóa đường nối của những frame cũ
        objectGroup.getChildren().remove(pathGroup);
        pathGroup.getChildren().clear();

        if (path == null || path.size() < 2) {
            return;
        }

        for (int i = 0; i < path.size() - 1; i++) {
            SpaceObject current = path.get(i);
            SpaceObject next = path.get(i + 1);

            double[] scaledCurrent = CoordinateConverter.sphericalToCartesianScaled(
                    current.getLatitude(), current.getLongitude(), current.getAltitude());
            double[] scaledNext = CoordinateConverter.sphericalToCartesianScaled(
                    next.getLatitude(), next.getLongitude(), next.getAltitude());

            javafx.geometry.Point3D a = new javafx.geometry.Point3D(scaledCurrent[0], scaledCurrent[1], scaledCurrent[2]);
            javafx.geometry.Point3D b = new javafx.geometry.Point3D(scaledNext[0], scaledNext[1], scaledNext[2]);

            javafx.scene.shape.Cylinder laser = RenderUtils.createConnectionLine(a, b, javafx.scene.paint.Color.LIMEGREEN);
            pathGroup.getChildren().add(laser);
        }

        objectGroup.getChildren().add(pathGroup);
    }
}

package com.satellitesim.ui;

import com.satellitesim.core.constants.PhysicsConstants;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Quản lý View 3D không gian (Trái Đất, Vệ tinh, Trạm mặt đất).
 *
 * <p>Cung cấp khả năng xoay bằng cách kéo rê chuột và zoom
 * bằng cách cuộn chuột.</p>
 */
public class SpaceView {

    private final SubScene subScene;
    private final Group root3D;
    private final Group objectGroup; // Chứa vệ tinh và trạm
    
    private final PerspectiveCamera camera;
    
    // Thuộc tính để xoay bằng chuột
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private double angleX = 0;
    private double angleY = 0;
    private double startMouseX;
    private double startMouseY;

    // Các Rotate Transforms
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    public SpaceView(double width, double height) {
        root3D = new Group();
        objectGroup = new Group();
        
        // 1. Tạo quả cầu hành tinh (Earth)
        double scaledRadius = PhysicsConstants.EARTH_RADIUS_KM * PhysicsConstants.RENDER_SCALE_FACTOR;
        Sphere earth = RenderUtils.createEarthSphere(scaledRadius);
        root3D.getChildren().add(earth);
        root3D.getChildren().add(objectGroup);
        
        // Căn quả cầu hơi xoay để thấy rõ các lục địa ban đầu
        earth.getTransforms().add(new Rotate(-20, Rotate.X_AXIS));

        // 2. Setup Camera
        camera = new PerspectiveCamera(true);
        // Lùi camera ra xa tâm để nhìn toàn cảnh hành tinh
        camera.setTranslateZ(-(scaledRadius * 3.5));
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        // Áp dụng phép xoay trực tiếp lên Group chứa mọi thứ để góc nhìn thay đổi
        root3D.getTransforms().addAll(rotateX, rotateY);

        // 3. Setup Lights (Ánh sáng mặt trời)
        AmbientLight ambientLight = new AmbientLight(Color.rgb(100, 100, 100)); // Sáng mờ xung quanh
        PointLight sunLight = new PointLight(Color.WHITE);
        sunLight.setTranslateX(1000);
        sunLight.setTranslateY(-500);
        sunLight.setTranslateZ(-3000); // Ánh sáng chiếu từ góc chéo
        root3D.getChildren().addAll(ambientLight, sunLight);

        // 4. Khởi tạo SubScene 3D
        subScene = new SubScene(root3D, width, height, true, javafx.scene.SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#0a0a2e"));
        subScene.setCamera(camera);

        // 5. Cài đặt các xử lý sự kiện tương tác bằng chuột
        initMouseControls();
    }

    /**
     * Khởi tạo logic điều khiển camera quanh hành tinh.
     */
    private void initMouseControls() {
        // Sự kiện Click và kéo (Drag) để xoay hành tinh
        subScene.setOnMousePressed(event -> {
            startMouseX = event.getSceneX();
            startMouseY = event.getSceneY();
            anchorAngleX = angleX;
            anchorAngleY = angleY;
        });

        subScene.setOnMouseDragged(event -> {
            angleX = anchorAngleX - (event.getSceneY() - startMouseY) * 0.2;
            angleY = anchorAngleY + (event.getSceneX() - startMouseX) * 0.2;

            // Xoay Group tổng thể
            rotateX.setAngle(angleX);
            rotateY.setAngle(angleY);
        });

        // Sự kiện Cuộn (Scroll) để Zoom In/Out
        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            double newZ = camera.getTranslateZ() + delta * 5.0; // Tốc độ zoom
            
            // Giới hạn zoom (không cho zoom quá sát hoặc quá xa)
            double scaledRadius = PhysicsConstants.EARTH_RADIUS_KM * PhysicsConstants.RENDER_SCALE_FACTOR;
            double minZ = -(scaledRadius * 1.5);
            double maxZ = -(scaledRadius * 10);
            
            if (newZ < minZ && newZ > maxZ) {
                camera.setTranslateZ(newZ);
            }
        });
    }

    public SubScene getSubScene() {
        return subScene;
    }

    public Group getObjectGroup() {
        return objectGroup;
    }
}

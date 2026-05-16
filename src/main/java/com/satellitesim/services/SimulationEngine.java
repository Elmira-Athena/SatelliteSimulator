package com.satellitesim.services;

import com.satellitesim.core.models.Satellite;
import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.core.models.SpaceObjectType;
import com.satellitesim.ui.ObjectRenderer;
import javafx.animation.AnimationTimer;
import javafx.scene.shape.Sphere;

import java.util.List;

import com.satellitesim.services.routing.RoutingEngine;

/**
 * Engine điều tiết vòng lặp mô phỏng chính (Game Loop).
 * Sử dụng AnimationTimer của JavaFX để đảm bảo không chặn luồng UI.
 */
public class SimulationEngine extends AnimationTimer {

    private final PhysicsEngine physicsEngine;
    private final ObjectRenderer objectRenderer;
    private final List<SpaceObject> spaceObjects;
    private final RoutingEngine routingEngine;

    private SpaceObject activeRouteSource;
    private SpaceObject activeRouteDest;

    private long lastUpdate = 0;
    
    // Hệ số tua nhanh thời gian (Time multiplier). 
    // Mặc định x100 để có thể thấy vệ tinh di chuyển rõ rệt.
    private double timeScale = 100.0;

    public SimulationEngine(PhysicsEngine physicsEngine, RoutingEngine routingEngine, ObjectRenderer objectRenderer, List<SpaceObject> spaceObjects) {
        this.physicsEngine = physicsEngine;
        this.routingEngine = routingEngine;
        this.objectRenderer = objectRenderer;
        this.spaceObjects = spaceObjects;
    }

    /**
     * Set cặp trạm nguồn - đích để mô phỏng định tuyến luân phiên.
     */
    public void setActiveRoute(SpaceObject source, SpaceObject dest) {
        this.activeRouteSource = source;
        this.activeRouteDest = dest;
    }

    @Override
    public void start() {
        lastUpdate = System.nanoTime();
        super.start();
        System.out.println("Simulation Engine Started with timeScale = " + timeScale);
    }

    @Override
    public void handle(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }

        // Tính khoảng thời gian trôi qua giữa 2 frame (giây)
        double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;

        // Nhân với timeScale để tăng tốc độ mô phỏng
        double simulationDeltaTime = elapsedSeconds * timeScale;

        try {
            updateState(simulationDeltaTime);
        } catch (Exception e) {
            System.err.println("SimulationEngine: lỗi trong frame, bỏ qua - " + e.getMessage());
        }

        lastUpdate = now;
    }

    /**
     * Cập nhật trạng thái từng vật thể trong một Frame.
     * @param deltaTimeSeconds Thời gian mô phỏng trong frame này.
     */
    private void updateState(double deltaTimeSeconds) {
        for (SpaceObject obj : spaceObjects) {
            // Chỉ cập nhật những vật thể có chuyển động (Vệ tinh)
            if (obj.getType() == SpaceObjectType.SATELLITE && obj instanceof Satellite satellite) {
                
                // 1. Cập nhật góc quay theo vận tốc quỹ đạo v = sqrt(GM/(R+h))
                physicsEngine.updatePosition(satellite, deltaTimeSeconds);
                
                // 2. Cập nhật lại giao diện UI của vệ tinh đó
                Sphere node = objectRenderer.getNode(satellite.getId());
                if (node != null) {
                    objectRenderer.updateNodePosition(node, satellite);
                }
            }
        }

        // 3. Xử lý Định tuyến Thời gian thực (Continuous Routing)
        if (activeRouteSource != null && activeRouteDest != null) {
            // Re-build Graph theo Snapshot của frame này
            routingEngine.buildGraph(spaceObjects);
            // Tìm lại đường đi
            List<SpaceObject> newPath = routingEngine.findShortestPath(activeRouteSource, activeRouteDest);
            // Render đường laser bắn giữa các trạm
            objectRenderer.renderPath(newPath);
            
            if (routeStatusListener != null) {
                boolean connected = !newPath.isEmpty();
                int hops = connected ? newPath.size() - 1 : 0;
                double latency = connected ? calculatePathLatency(newPath) : 0;
                routeStatusListener.accept(new RouteUpdate(connected, hops, latency));
            }
        }
    }

    public record RouteUpdate(boolean connected, int hops, double totalLatency) {}

    private java.util.function.Consumer<RouteUpdate> routeStatusListener;

    public void setRouteStatusListener(java.util.function.Consumer<RouteUpdate> listener) {
        this.routeStatusListener = listener;
    }

    private double calculatePathLatency(List<SpaceObject> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            SpaceObject a = path.get(i), b = path.get(i + 1);
            double dist = physicsEngine.calculateDistance(
                a.getLatitude(), a.getLongitude(), a.getAltitude(),
                b.getLatitude(), b.getLongitude(), b.getAltitude());
            total += physicsEngine.calculateTotalDelay(dist);
        }
        return total;
    }

    public void setTimeScale(double timeScale) {
        this.timeScale = timeScale;
    }
    
    public double getTimeScale() {
        return timeScale;
    }

    public void setSpaceObjects(List<SpaceObject> spaceObjects) {
        this.spaceObjects.clear();
        this.spaceObjects.addAll(spaceObjects);
    }
}

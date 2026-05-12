package com.satellitesim.services.routing;

import com.satellitesim.core.models.GroundStation;
import com.satellitesim.core.models.Satellite;
import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.services.PhysicsEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Module hỗ trợ Benchmark và Đánh giá hiệu năng mạng.
 */
public class RoutingEvaluator {

    private final RoutingEngine routingEngine;
    private final Random random = new Random();

    public RoutingEvaluator(RoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

    /**
     * Tạo danh sách vệ tinh ngẫu nhiên để mô phỏng kịch bản.
     */
    public List<SpaceObject> generateRandomSatellites(int count, List<SpaceObject> existingGroundStations) {
        List<SpaceObject> allObjects = new ArrayList<>(existingGroundStations);

        for (int i = 0; i < count; i++) {
            String name = "Rand-Sat-" + (i + 1);
            // Random kinh độ: -180 đến 180
            double lon = (random.nextDouble() * 360) - 180;
            // Random vĩ độ: -90 đến 90 (phân phối đều trên mặt phẳng chiếu)
            double lat = (random.nextDouble() * 180) - 90;
            // Random độ cao: 500km đến 2000km (LEO)
            double alt = 500 + random.nextDouble() * 1500;

            allObjects.add(new Satellite(name, lat, lon, alt));
        }

        return allObjects;
    }

    /**
     * Chạy Benchmark giữa các cặp trạm mặt đất ngẫu nhiên.
     */
    public BenchmarkResult runBenchmark(List<SpaceObject> objects, int trials) {
        // Lấy danh sách các trạm mặt đất
        List<GroundStation> groundStations = new ArrayList<>();
        for (SpaceObject obj : objects) {
            if (obj instanceof GroundStation gs) {
                groundStations.add(gs);
            }
        }

        if (groundStations.size() < 2) {
            return new BenchmarkResult(0, 0, 0, 0);
        }

        // Fix Thread-concurrency: Use local engines for background benchmarks
        PhysicsEngine localPhysics = new PhysicsEngine();
        RoutingEngine localRouting = new RoutingEngine(localPhysics);
        localRouting.buildGraph(objects);

        int successfulRoutes = 0;
        int totalHops = 0;
        double totalLatency = 0;

        for (int i = 0; i < trials; i++) {
            // Chọn ngẫu nhiên 2 trạm khác nhau
            int idxA = random.nextInt(groundStations.size());
            int idxB;
            do {
                idxB = random.nextInt(groundStations.size());
            } while (idxA == idxB);

            GroundStation source = groundStations.get(idxA);
            GroundStation dest = groundStations.get(idxB);

            List<SpaceObject> path = localRouting.findShortestPath(source, dest);

            if (!path.isEmpty()) {
                successfulRoutes++;
                // Số hop = số node - 1
                totalHops += (path.size() - 1);

                // Tính latency cho path này
                totalLatency += calculatePathLatency(path);
            }
        }

        double successRate = (double) successfulRoutes / trials * 100;
        double avgHops = successfulRoutes > 0 ? (double) totalHops / successfulRoutes : 0;
        double avgLatency = successfulRoutes > 0 ? totalLatency / successfulRoutes : 0;

        return new BenchmarkResult(successRate, avgHops, avgLatency, successfulRoutes);
    }

    private double calculatePathLatency(List<SpaceObject> path) {
        double pathLatency = 0;
        PhysicsEngine physics = new PhysicsEngine(); // Tạm dùng instance local để tính nhanh
        for (int i = 0; i < path.size() - 1; i++) {
            SpaceObject a = path.get(i);
            SpaceObject b = path.get(i + 1);
            double dist = physics.calculateDistance(
                    a.getLatitude(), a.getLongitude(), a.getAltitude(),
                    b.getLatitude(), b.getLongitude(), b.getAltitude());
            pathLatency += physics.calculateTotalDelay(dist);
        }
        return pathLatency;
    }

    /**
     * Data class để lưu kết quả Benchmark
     */
    public record BenchmarkResult(double successRate, double avgHops, double avgLatency, int count) {
        @Override
        public String toString() {
            return String.format("Reachability: %.1f%% | Avg Hops: %.2f | Avg Latency: %.3f s (Successful: %d)",
                    successRate, avgHops, avgLatency, count);
        }
    }
}

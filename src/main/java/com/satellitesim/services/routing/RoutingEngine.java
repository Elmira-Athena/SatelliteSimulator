package com.satellitesim.services.routing;

import com.satellitesim.core.models.RouteEdge;
import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.core.models.SpaceObjectType;
import com.satellitesim.services.PhysicsEngine;

import java.util.*;

/**
 * Xử lý thuật toán dựng đồ thị động và tìm đường ngắn nhất (Dijkstra).
 */
public class RoutingEngine {

    private final PhysicsEngine physicsEngine;

    // Đồ thị được lưu dạng Adjacency List: Object ID -> Danh sách các kết nối (Cạnh)
    private final Map<String, List<RouteEdge>> adjacencyList = new HashMap<>();

    public RoutingEngine(PhysicsEngine physicsEngine) {
        this.physicsEngine = physicsEngine;
    }

    /**
     * Dựng lại đồ thị tại một thời điểm t.
     * Cần gọi hàm này trước khi tìm đường do vệ tinh di chuyển liên tục.
     *
     * @param allObjects Tất cả các vệ tinh và trạm hiện tại trên quỹ đạo.
     */
    public void buildGraph(List<SpaceObject> allObjects) {
        adjacencyList.clear();

        // Cấp phát sẵn danh sách rỗng để tránh null
        for (SpaceObject obj : allObjects) {
            adjacencyList.put(obj.getId(), new ArrayList<>());
        }

        // Quét tất cả các cặp đỉnh (O(N^2))
        for (int i = 0; i < allObjects.size(); i++) {
            SpaceObject a = allObjects.get(i);
            
            for (int j = i + 1; j < allObjects.size(); j++) {
                SpaceObject b = allObjects.get(j);

                // Nếu A và B đều là trạm mặt đất -> Không nói chuyện qua nhau (phải qua vệ tinh)
                if (a.getType() == SpaceObjectType.GROUND_STATION && b.getType() == SpaceObjectType.GROUND_STATION) {
                    continue; // Skip
                }

                // Kiểm tra Line of Sight: Trái Đất có che khuất tia nhìn không?
                if (physicsEngine.hasLineOfSight(
                        a.getLatitude(), a.getLongitude(), a.getAltitude(),
                        b.getLatitude(), b.getLongitude(), b.getAltitude())) {
                    
                    // Nếu nhìn thấy, tính trọng số (Độ trễ = D_prop + D_proc)
                    double distance = physicsEngine.calculateDistance(
                            a.getLatitude(), a.getLongitude(), a.getAltitude(),
                            b.getLatitude(), b.getLongitude(), b.getAltitude());
                    
                    double dTotal = physicsEngine.calculateTotalDelay(distance);

                    // Thêm cạnh 2 chiều vào danh sách kề
                    adjacencyList.get(a.getId()).add(new RouteEdge(a, b, dTotal));
                    adjacencyList.get(b.getId()).add(new RouteEdge(b, a, dTotal));
                }
            }
        }
    }

    /**
     * Thuật toán Dijkstra tìm đường đi ngắn nhất (ít trễ nhất) từ A đến B.
     * @param source Trạm gửi
     * @param dest Trạm nhận
     * @return Danh sách các Node đi qua theo đúng thứ tự (rỗng nếu không có đường đi)
     */
    public List<SpaceObject> findShortestPath(SpaceObject source, SpaceObject dest) {
        if (source == null || dest == null || !adjacencyList.containsKey(source.getId()) || !adjacencyList.containsKey(dest.getId())) {
            return Collections.emptyList();
        }

        // Mảng lưu vết đường đi (NodeId -> Cạnh đã dẫn tới Node đó)
        Map<String, RouteEdge> cameFrom = new HashMap<>();
        
        // Mảng lưu tổng độ trễ nhỏ nhất từ Source đến các Node (Khởi tạo vô cực)
        Map<String, Double> minDelay = new HashMap<>();
        for (String id : adjacencyList.keySet()) {
            minDelay.put(id, Double.POSITIVE_INFINITY);
        }
        minDelay.put(source.getId(), 0.0);

        // Hàng đợi ưu tiên, lấy Node có minDelay nhỏ nhất
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.delay));
        queue.add(new NodeDistance(source, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            SpaceObject currentObj = current.node;

            // Đã duyệt tới đích
            if (currentObj.getId().equals(dest.getId())) {
                break;
            }

            // Nếu khoảng cách lưu trong queue lớn hơn khoảng cách thực tế nhỏ nhất hiện tại thì loại bỏ
            if (current.delay > minDelay.get(currentObj.getId())) {
                continue;
            }

            // Duyệt các đỉnh kề
            for (RouteEdge edge : adjacencyList.get(currentObj.getId())) {
                SpaceObject neighbor = edge.getDestination();
                double newDelayToNeighbor = minDelay.get(currentObj.getId()) + edge.getTotalDelay();

                // Nếu tìm được đường đi mới tốt hơn đường cũ
                if (newDelayToNeighbor < minDelay.get(neighbor.getId())) {
                    minDelay.put(neighbor.getId(), newDelayToNeighbor);
                    cameFrom.put(neighbor.getId(), edge); // Đánh dấu dấu chân khứ hồi
                    queue.add(new NodeDistance(neighbor, newDelayToNeighbor));
                }
            }
        }

        // Truy vết đường đi nếu tìm thấy đích
        if (!cameFrom.containsKey(dest.getId())) {
            return Collections.emptyList(); // Không có đường đi (LOS blocked entire net)
        }

        return reconstructPath(cameFrom, dest);
    }

    /**
     * Dựng lại đường đi từ map dấu chân (cameFrom) đi ngược về đầu.
     */
    private List<SpaceObject> reconstructPath(Map<String, RouteEdge> cameFrom, SpaceObject currentDest) {
        List<SpaceObject> path = new ArrayList<>();
        SpaceObject curr = currentDest;
        
        while (curr != null) {
            path.add(curr);
            RouteEdge edge = cameFrom.get(curr.getId());
            if (edge != null) {
                curr = edge.getSource();
            } else {
                curr = null;
            }
        }
        
        Collections.reverse(path);
        return path;
    }

    /** Helper class phụ trợ PriorityQueue cho Dijkstra */
    private static class NodeDistance {
        SpaceObject node;
        double delay;

        NodeDistance(SpaceObject node, double delay) {
            this.node = node;
            this.delay = delay;
        }
    }
}

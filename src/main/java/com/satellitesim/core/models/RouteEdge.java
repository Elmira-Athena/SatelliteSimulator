package com.satellitesim.core.models;

/**
 * Đại diện cho một cạnh (kết nối) trong đồ thị định tuyến.
 * Chứa thông tin Node nguồn, Node đích và trọng số thời gian trễ.
 */
public class RouteEdge {
    private final SpaceObject source;
    private final SpaceObject destination;
    private final double totalDelay; // Trọng số D_total (giây)

    public RouteEdge(SpaceObject source, SpaceObject destination, double totalDelay) {
        this.source = source;
        this.destination = destination;
        this.totalDelay = totalDelay;
    }

    public SpaceObject getSource() {
        return source;
    }

    public SpaceObject getDestination() {
        return destination;
    }

    public double getTotalDelay() {
        return totalDelay;
    }
}

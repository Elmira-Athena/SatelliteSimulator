package com.satellitesim.ui;

import com.satellitesim.core.models.GroundStation;
import com.satellitesim.core.models.Satellite;
import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.core.models.SpaceObjectType;
import com.satellitesim.data.repository.SpaceObjectRepositoryImpl;
import com.satellitesim.services.PhysicsEngine;
import com.satellitesim.services.routing.RoutingEvaluator;
import com.satellitesim.services.SimulationEngine;
import com.satellitesim.services.SpaceObjectService;
import com.satellitesim.services.routing.RoutingEngine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Entry point chính của ứng dụng JavaFX.
 */
public class MainApp extends Application {

    private static final String APP_TITLE = "Satellite Orbit Simulator & Router";
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    private SpaceObjectService spaceObjectService;
    private ObjectRenderer objectRenderer;
    private SimulationEngine simulationEngine;
    private RoutingEvaluator routingEvaluator;
    private RoutingEngine routingEngine;
    private List<SpaceObject> currentObjects;

    // UI Controls cần truy cập từ nhiều method
    private ComboBox<SpaceObject> sourceCombo;
    private ComboBox<SpaceObject> destCombo;
    private Label[] benchmarkLabels;
    private Label[] liveStatsLabels;
    private GridPane liveStatsPanel;
    private final ArrayDeque<Boolean> routeHistory = new ArrayDeque<>();
    private static final int ROUTE_HISTORY_SIZE = 120;

    @Override
    public void init() throws Exception {
        SpaceObjectRepositoryImpl repository = new SpaceObjectRepositoryImpl();
        spaceObjectService = new SpaceObjectService(repository);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a2e;");

        SpaceView spaceView = new SpaceView(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.setCenter(spaceView.getSubScene());

        objectRenderer = new ObjectRenderer(spaceView.getObjectGroup());
        PhysicsEngine physicsEngine = new PhysicsEngine();
        routingEngine = new RoutingEngine(physicsEngine);
        routingEvaluator = new RoutingEvaluator(routingEngine);

        // Khởi tạo list rỗng để tránh UnsupportedOperationException khi clear/addAll
        currentObjects = new ArrayList<>(loadAndRenderObjectsFromDb());
        
        simulationEngine = new SimulationEngine(physicsEngine, routingEngine, objectRenderer, currentObjects);
        simulationEngine.start();

        VBox sideBar = createBenchmarkSidebar();
        root.setRight(sideBar);

        // Setup ban đầu cho Combo box
        updateRoutingNodes(currentObjects);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, true);
        scene.setFill(Color.web("#0a0a2e"));

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> 
            spaceView.getSubScene().setWidth(newVal.doubleValue() - sideBar.getWidth())
        );
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> 
            spaceView.getSubScene().setHeight(newVal.doubleValue())
        );

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createBenchmarkSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(300);
        sidebar.setStyle("-fx-background-color: rgba(10, 10, 40, 0.9); -fx-border-color: #3f3fbf; -fx-border-width: 0 0 0 2;");

        // --- PHẦN 1: ROUTING TƯƠNG TÁC ---
        Label routingTitle = new Label("LIVE ROUTING");
        routingTitle.setTextFill(Color.CYAN);
        routingTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        sourceCombo = new ComboBox<>();
        destCombo = new ComboBox<>();
        sourceCombo.setPromptText("Select Source...");
        destCombo.setPromptText("Select Destination...");
        sourceCombo.setMaxWidth(Double.MAX_VALUE);
        destCombo.setMaxWidth(Double.MAX_VALUE);
        sourceCombo.setOnAction(e -> updateActiveRoute());
        destCombo.setOnAction(e -> updateActiveRoute());

        Label statusLabel = new Label("Status: Waiting...");
        statusLabel.setTextFill(Color.YELLOW);
        
        simulationEngine.setRouteStatusListener(update -> {
            Platform.runLater(() -> {
                if (update.connected()) {
                    statusLabel.setText("Status: CONNECTED");
                    statusLabel.setTextFill(Color.LIME);
                } else {
                    statusLabel.setText("Status: NO ROUTE FOUND");
                    statusLabel.setTextFill(Color.RED);
                }
                if (liveStatsPanel != null && liveStatsPanel.isVisible()) {
                    if (routeHistory.size() >= ROUTE_HISTORY_SIZE) routeHistory.poll();
                    routeHistory.add(update.connected());
                    long successCount = routeHistory.stream().filter(b -> b).count();
                    double rate = (double) successCount / routeHistory.size() * 100;
                    liveStatsLabels[0].setText(String.format("%.1f%%", rate));
                    liveStatsLabels[1].setText(update.connected() ? String.valueOf(update.hops()) : "-");
                    liveStatsLabels[2].setText(update.connected() ? String.format("%.3f s", update.totalLatency()) : "-");
                }
            });
        });

        GridPane liveStats = new GridPane();
        liveStats.setVgap(5); liveStats.setHgap(10);
        Label srL = createValueLabel("-"); Label hL2 = createValueLabel("-"); Label lL2 = createValueLabel("-");
        this.liveStatsLabels = new Label[]{srL, hL2, lL2};
        liveStats.add(createStyledLabel("Success:"), 0, 0); liveStats.add(srL, 1, 0);
        liveStats.add(createStyledLabel("Hops:"),    0, 1); liveStats.add(hL2, 1, 1);
        liveStats.add(createStyledLabel("Latency:"), 0, 2); liveStats.add(lL2, 1, 2);
        this.liveStatsPanel = liveStats;
        liveStatsPanel.setVisible(false);
        liveStatsPanel.setManaged(false);

        VBox routingBox = new VBox(8, routingTitle, new Label("From:"), sourceCombo, new Label("To:"), destCombo, statusLabel, liveStatsPanel);
        routingBox.setStyle("-fx-text-fill: white;");
        for(javafx.scene.Node n : routingBox.getChildren()) if(n instanceof Label && n != routingTitle && n != statusLabel) ((Label)n).setTextFill(Color.LIGHTGRAY);

        // --- PHẦN 2: THÊM VẬT THỂ MỚI ---
        Label addTitle = new Label("ADD SPACE OBJECT");
        addTitle.setTextFill(Color.CYAN);
        addTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        GridPane addForm = new GridPane();
        addForm.setVgap(8); addForm.setHgap(8);
        
        TextField nameIn = new TextField(); nameIn.setPromptText("Name...");
        ComboBox<SpaceObjectType> typeIn = new ComboBox<>(FXCollections.observableArrayList(SpaceObjectType.values()));
        typeIn.setValue(SpaceObjectType.SATELLITE);
        TextField latIn = new TextField(); latIn.setPromptText("Lat (-90, 90)");
        TextField lonIn = new TextField(); lonIn.setPromptText("Lon (-180, 180)");
        TextField altIn = new TextField(); altIn.setPromptText("Alt (km)");

        addForm.add(new Label("Name:"), 0, 0); addForm.add(nameIn, 1, 0);
        addForm.add(new Label("Type:"), 0, 1); addForm.add(typeIn, 1, 1);
        addForm.add(new Label("Lat:"), 0, 2); addForm.add(latIn, 1, 2);
        addForm.add(new Label("Lon:"), 0, 3); addForm.add(lonIn, 1, 3);
        addForm.add(new Label("Alt:"), 0, 4); addForm.add(altIn, 1, 4);
        for(javafx.scene.Node n : addForm.getChildren()) if(n instanceof Label) ((Label)n).setTextFill(Color.WHITE);

        Button addBtn = new Button("Add to Orbit & Database");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-base: #1a5a1a; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            try {
                String id = UUID.randomUUID().toString();
                String name = nameIn.getText();
                SpaceObjectType type = typeIn.getValue();
                double lat = Double.parseDouble(latIn.getText());
                double lon = Double.parseDouble(lonIn.getText());
                double alt = Double.parseDouble(altIn.getText());

                SpaceObject newObj = (type == SpaceObjectType.GROUND_STATION) 
                        ? new GroundStation(id, name, lat, lon)
                        : new Satellite(id, name, lat, lon, alt);

                // Lưu DB
                if (spaceObjectService.saveSpaceObject(newObj)) {
                    refreshSystemData(); // Reload everything
                    nameIn.clear(); latIn.clear(); lonIn.clear(); altIn.clear();
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Input: " + ex.getMessage());
                alert.show();
            }
        });

        // --- PHẦN 3: BENCHMARK ---
        Label benchTitle = new Label("NETWORK BENCHMARK");
        benchTitle.setTextFill(Color.CYAN);
        benchTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");

        HBox scenarioBtns = new HBox(5);
        int[] counts = {10, 50, 100, 200};
        for (int c : counts) {
            Button b = new Button(String.valueOf(c));
            b.setOnAction(e -> runScenario(c));
            scenarioBtns.getChildren().add(b);
        }

        Button resetBtn = new Button("Reset to DB");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(e -> refreshSystemData());

        GridPane metrics = new GridPane();
        metrics.setVgap(5); metrics.setHgap(10);
        Label sL = createValueLabel("-"); Label hL = createValueLabel("-"); Label lL = createValueLabel("-");
        this.benchmarkLabels = new Label[]{sL, hL, lL};
        metrics.add(createStyledLabel("Success:"), 0, 0); metrics.add(sL, 1, 0);
        metrics.add(createStyledLabel("Hops:"), 0, 1);    metrics.add(hL, 1, 1);
        metrics.add(createStyledLabel("Latency:"), 0, 2); metrics.add(lL, 1, 2);

        sidebar.getChildren().addAll(
            routingBox, new Separator(), 
            addTitle, addForm, addBtn, new Separator(),
            benchTitle, new Label("Random Scenario:"), scenarioBtns, resetBtn, metrics
        );
        return sidebar;
    }

    private void updateActiveRoute() {
        SpaceObject s = sourceCombo.getValue();
        SpaceObject d = destCombo.getValue();
        boolean bothSelected = s != null && d != null;
        if (bothSelected) {
            simulationEngine.setActiveRoute(s, d);
            routeHistory.clear();
            liveStatsLabels[0].setText("-"); liveStatsLabels[1].setText("-"); liveStatsLabels[2].setText("-");
        }
        liveStatsPanel.setVisible(bothSelected);
        liveStatsPanel.setManaged(bothSelected);
    }

    private void updateRoutingNodes(List<SpaceObject> objects) {
        List<SpaceObject> groundStations = objects.stream()
                .filter(o -> o.getType() == SpaceObjectType.GROUND_STATION)
                .collect(Collectors.toList());
        sourceCombo.setItems(FXCollections.observableArrayList(groundStations));
        destCombo.setItems(FXCollections.observableArrayList(groundStations));
    }

    private Label createStyledLabel(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.LIGHTGRAY);
        return lbl;
    }

    private Label createValueLabel(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.LIME);
        lbl.setStyle("-fx-font-weight: bold;");
        return lbl;
    }

    private void runScenario(int count) {
        List<SpaceObject> gss = currentObjects.stream()
                .filter(o -> o.getType() == SpaceObjectType.GROUND_STATION)
                .collect(Collectors.toList());
        
        List<SpaceObject> scenario = routingEvaluator.generateRandomSatellites(count, gss);
        updateLiveSimulation(scenario);

        // Run benchmark in background thread to avoid UI lag
        new Thread(() -> {
            RoutingEvaluator.BenchmarkResult res = routingEvaluator.runBenchmark(scenario, 100);
            Platform.runLater(() -> {
                benchmarkLabels[0].setText(String.format("%.1f%%", res.successRate()));
                benchmarkLabels[1].setText(String.format("%.2f", res.avgHops()));
                benchmarkLabels[2].setText(String.format("%.3f s", res.avgLatency()));
            });
        }).start();
    }

    private void refreshSystemData() {
        currentObjects = new ArrayList<>(loadAndRenderObjectsFromDb());
        updateLiveSimulation(currentObjects);
        updateRoutingNodes(currentObjects);
        benchmarkLabels[0].setText("-"); benchmarkLabels[1].setText("-"); benchmarkLabels[2].setText("-");
    }

    private void updateLiveSimulation(List<SpaceObject> objects) {
        simulationEngine.stop();
        objectRenderer.renderObjects(objects);
        simulationEngine.setSpaceObjects(objects);
        updateActiveRoute();
        simulationEngine.start();
    }

    private List<SpaceObject> loadAndRenderObjectsFromDb() {
        List<SpaceObject> objects = spaceObjectService.getAllSpaceObjects();
        objectRenderer.renderObjects(objects);
        return objects;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

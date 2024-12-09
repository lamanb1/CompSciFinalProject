package edu.wit.cs1050.finalProject.base;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompProjectorMain extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV
    }

    private VideoCapture videoCapture;
    private Circle trackerCircle;
    private ExecutorService executorService;
    private List<Enemy> enemies;

    static double smoothedX = 0;
    static double smoothedY = 0;

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showMainMenu();
    }

    private void showMainMenu() {
        Pane menuRoot = new Pane();

        Text title = new Text("Tennis Ball Tracker Game");
        title.setFont(new Font(24));
        title.setFill(Color.BLACK);
        title.setLayoutX(250);
        title.setLayoutY(200);

        Button startButton = new Button("Start Game");
        startButton.setLayoutX(350);
        startButton.setLayoutY(300);
        startButton.setOnAction(event -> startGame());

        menuRoot.getChildren().addAll(title, startButton);

        Scene menuScene = new Scene(menuRoot, 800, 600);
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Main Menu");
        primaryStage.show();
    }

    private void startGame() {
        Pane gameRoot = new Pane();
        trackerCircle = new Circle(10, Color.RED);
        trackerCircle.setCenterX(400); // Spawn ball in the center
        trackerCircle.setCenterY(300);
        gameRoot.getChildren().add(trackerCircle);

        // Initialize enemies on edges
        enemies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            spawnEnemy(gameRoot);
        }

        // Set up a timeline to add new enemies every 3 seconds
        Timeline enemySpawnTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> spawnEnemy(gameRoot)));
        enemySpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        enemySpawnTimeline.play();

        Scene gameScene = new Scene(gameRoot, 800, 600);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("NO");

        videoCapture = new VideoCapture(0); // Open the default camera

        if (!videoCapture.isOpened()) {
            System.err.println("Error: Could not open camera.");
            Platform.exit();
            return;
        }

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> processVideo(gameRoot));
    }

    private void spawnEnemy(Pane gameRoot) {
        double x = 0, y = 0;

        // Randomly pick an edge for spawning
        int edge = (int) (Math.random() * 4);
        switch (edge) {
            case 0: // Top edge
                x = Math.random() * 800;
                y = 20;
                break;
            case 1: // Bottom edge
                x = Math.random() * 800;
                y = 580;
                break;
            case 2: // Left edge
                x = 20;
                y = Math.random() * 600;
                break;
            case 3: // Right edge
                x = 780;
                y = Math.random() * 600;
                break;
        }

        // Randomly decide between a normal enemy and an aggressive enemy
        boolean whichEnemy = Math.random() > 0.8;  // 50% chance of being aggressive

        Enemy enemy;
        if (whichEnemy) {
            // Create an aggressive enemy that moves towards the player
            enemy = new FastEnemy(
                    x,
                    y,
                    15,
                    Color.GREEN,
                    Math.random() * 10 - 2, // Random x-velocity
                    Math.random() * 10 - 2  // Random y-velocity
            );
        } else {
            // Create a normal enemy with random movement
            enemy = new NormalEnemy(
                    x,
                    y,
                    15,
                    Color.BLUE,
                    Math.random() * 4 - 2, // Random x-velocity
                    Math.random() * 4 - 2  // Random y-velocity
            );
        }

        enemies.add(enemy);
        gameRoot.getChildren().add(enemy.getShape());
    }

    private void processVideo(Pane gameRoot) {
        Mat frame = new Mat();
        while (videoCapture.isOpened()) {
            if (!videoCapture.read(frame)) {
                System.err.println("Error: Could not read frame.");
                break;
            }

            // Process frame to find the tennis ball
            Point ballCenter = detectTennisBall(frame);

            if (ballCenter != null) {
                // Flip x-coordinate for mirrored effect
                ballCenter.x = frame.width() - ballCenter.x;

                // Smooth the movement
                smoothedX = smoothedX + (ballCenter.x - smoothedX) * 0.2;
                smoothedY = smoothedY + (ballCenter.y - smoothedY) * 0.2;

                Platform.runLater(() -> {
                    trackerCircle.setCenterX(smoothedX);
                    trackerCircle.setCenterY(smoothedY);

                    // Update enemy positions and check for collisions
                    for (Enemy enemy : enemies) {
                        enemy.move();
                        enemy.checkBounds(800, 600);
                        if (checkCollision(trackerCircle, enemy.getShape())) {
                            handleCollision();
                        }
                    }
                });
            }
        }
    }

    private Point detectTennisBall(Mat frame) {
        Mat hsv = new Mat();
        Mat mask = new Mat();

        // Convert to HSV color space
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

        // Define color range for a tennis ball (adjust values if necessary)
        Scalar lowerBound = new Scalar(29, 86, 6);
        Scalar upperBound = new Scalar(64, 255, 255);

        // Create a mask for the color
        Core.inRange(hsv, lowerBound, upperBound, mask);

        // Find contours
        Mat hierarchy = new Mat();
        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Point ballCenter = null;

        // Find the largest contour
        double maxArea = 0;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                Rect boundingRect = Imgproc.boundingRect(contour);
                ballCenter = new Point(boundingRect.x + boundingRect.width / 2.0,
                        boundingRect.y + boundingRect.height / 2.0);
            }
        }

        return ballCenter;
    }

    private boolean checkCollision(Circle player, Circle enemy) {
        double distance = Math.sqrt(Math.pow(player.getCenterX() - enemy.getCenterX(), 2) +
                Math.pow(player.getCenterY() - enemy.getCenterY(), 2));
        return distance < (player.getRadius() + enemy.getRadius());
    }

    private void handleCollision() {
        Platform.runLater(() -> {
            // Stop camera and executor
            if (videoCapture != null) {
                videoCapture.release();
            }
            if (executorService != null) {
                executorService.shutdownNow();
            }
            // Show main menu
            showMainMenu();
        });
    }

    @Override
    public void stop() throws Exception {
        if (videoCapture != null) {
            videoCapture.release();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

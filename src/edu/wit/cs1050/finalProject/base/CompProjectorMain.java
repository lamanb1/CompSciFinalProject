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

/* Ball Frenzy is a game where you use a tennis ball to control the player on screen
 * You need a camera and a tennis Ball
 * 
 * 
 * 
 * Known bugs -- Randomly die - caused by an error of camera jittering or not being able to capture the frame
 * Also could be caused by it detecting a large amount of yellow somewhere else for a split second teleporting
 * the ball there then back instantly causing the player to die: Also seems to most after
 * you restart the game not on the first go so if you completely close the game and start after every death
 * seems to happen a lot less. : Also what causes error message in console I believe- NO known fix
 * 
 * Jittering of charector - Caused by color not being consistent of the ball so the detection is off
 * Could fix by using object tracking as well as color tracking but then if you hit the edges of the camera
 * it wont work So don't really know full fix yet
 * */

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
    private int score = 0; 
    private int highScore = 0; 
    private Timeline scoreTimeline; 

    private Text noBallDetectedText;
    private Text scoreText; 
    private Text highScoreText; 
    private Stage primaryStage;
    private boolean ballDetected;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showMainMenu();
    }

    // Main menu stuff
    private void showMainMenu() {
        Pane menuRoot = new Pane();

        Text title = new Text("BALL FRENZY!!!");
        title.setFont(new Font(24));
        title.setFill(Color.WHITE);
        title.setLayoutX(300);
        title.setLayoutY(250);

        Button startButton = new Button("Start Game");
        startButton.setLayoutX(350);
        startButton.setLayoutY(300);
        startButton.setOnAction(event -> startGame());

        Button instructionsButton = new Button("Instructions");
        instructionsButton.setLayoutX(350);
        instructionsButton.setLayoutY(350);
        instructionsButton.setOnAction(event -> showInstructionsMenu());

        menuRoot.getChildren().addAll(title, startButton, instructionsButton);

        Scene menuScene = new Scene(menuRoot, 800, 600);
        menuScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // Add stylesheet
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Main Menu");
        primaryStage.show();
    }


    private void startGame() {
        Pane gameRoot = new Pane();
        trackerCircle = new Circle(10, Color.RED);
        trackerCircle.setCenterX(400); 
        trackerCircle.setCenterY(300);
        gameRoot.getChildren().add(trackerCircle);

        // Create and display the score text
        scoreText = new Text("Score: " + score);
        scoreText.setFont(new Font(18));
        scoreText.setFill(Color.BLACK);
        scoreText.setLayoutX(10);
        scoreText.setLayoutY(20);
        gameRoot.getChildren().add(scoreText);

        // Create and display the high score text
        highScoreText = new Text("High Score: " + highScore);
        highScoreText.setFont(new Font(18));
        highScoreText.setFill(Color.BLACK);
        highScoreText.setLayoutX(650);
        highScoreText.setLayoutY(20);
        gameRoot.getChildren().add(highScoreText);
        
        // Create and dont display the NO ball detected text
        noBallDetectedText = new Text("No ball detected");
        noBallDetectedText.setFont(new Font(24));
        noBallDetectedText.setFill(Color.RED);
        noBallDetectedText.setLayoutX(300);  // Center horizontally
        noBallDetectedText.setLayoutY(300);  // Center vertically
        noBallDetectedText.setVisible(false); // Initially hidden
        gameRoot.getChildren().add(noBallDetectedText);

        // Start enemies on edges
        enemies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            spawnEnemy(gameRoot);
        }

        // Set up a timeline to add new enemies every 3 seconds
        Timeline enemySpawnTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> spawnEnemy(gameRoot)));
        enemySpawnTimeline.setCycleCount(Timeline.INDEFINITE);
        enemySpawnTimeline.play();

        // Set up a timeline to increase score every second
        scoreTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> increaseScore()));
        scoreTimeline.setCycleCount(Timeline.INDEFINITE);
        scoreTimeline.play();
        
        //Sets up scene
        Scene gameScene = new Scene(gameRoot, 800, 600);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Ball Frenzy");

        videoCapture = new VideoCapture(0); // Opens the default camera can change based on camera 1 = next camera and so on

        if (!videoCapture.isOpened()) {
            System.err.println("Error: Could not open camera.");
            Platform.exit();
            return;
        }

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> processVideo(gameRoot));
    }
    
    //Instructions Menu
    private void showInstructionsMenu() 
    {
        Pane instructionsRoot = new Pane();

        Text title = new Text("Instructions");
        title.setFont(new Font(24));
        title.setFill(Color.WHITE);
        title.setLayoutX(300);
        title.setLayoutY(100);

        // Add instructions text
        Text instructions = new Text(
            "1. Use a tennis ball to control the red circle on the screen.\n" +
            "2. Avoid enemies and survive as long as you can.\n" +
            "3. Your score increases every second while the ball is detected.\n" +
            "4. If the red circle collides with an enemy, the game ends.\n" +
            "5. Stay safe and aim for the highest score!\n\n\n" +
            "Tips:\nStay towards the middle so the ball doesnt bug out!\nHave good lighting on the ball so its less lagy!"
            
            
        );
        instructions.setFont(new Font(18));
        instructions.setFill(Color.WHITE);
        instructions.setLayoutX(100);
        instructions.setLayoutY(150);

        Button backButton = new Button("Back to Main Menu");
        backButton.setLayoutX(350);
        backButton.setLayoutY(400);
        backButton.setOnAction(event -> showMainMenu());

        instructionsRoot.getChildren().addAll(title, instructions, backButton);

        Scene instructionsScene = new Scene(instructionsRoot, 800, 600);
        instructionsScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // Add stylesheet
        primaryStage.setScene(instructionsScene);
        primaryStage.setTitle("Instructions");
        primaryStage.show();
    }


    // As name implies, spawns the enemy
    private void spawnEnemy(Pane gameRoot) 
    {
    	if(!ballDetected && enemies.size() >= 5)
    	{
    		return;
    	}
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

        // Randomly decide between Enemys
        double Random = Math.random(); 
        

        Enemy enemy;
        if (Random > 0.8) //20% chance for a follow enemy
        {
            // Create a follow enemy that follows the player
            enemy = new FastEnemy(
                    x,
                    y,
                    15,
                    Color.GREEN,
                    Math.random() * 6 - 2, // Speed x
                    Math.random() * 6 - 2  // Speed y
            );
        } 
        else if(Random > 0.5)// 30% chance for a fast 1 direction enemy
        {
        	//Create a FastBounceEnemy that only moves in 1 direction but fast
        	enemy = new FastBounceEnemy(
                    x,
                    y,
                    15,
                    Color.PURPLE,
                    Math.random() * 6 + 4, // Speed x
                    Math.random() * 6 + 4  // Speed y
                    );
        }
        else //50% chance for a normal enemy
        {
            // Create a normal enemy with random movement
            enemy = new NormalEnemy(
                    x,
                    y,
                    15,
                    Color.BLUE,
                    Math.random() * 4 - 2, // Speed x
                    Math.random() * 4 - 2  // Speed y
            );
        }

        enemies.add(enemy);
        gameRoot.getChildren().add(enemy.getShape());
    }

    private void processVideo(Pane gameRoot) 
    {
        Mat frame = new Mat();
        ballDetected = false;  
        while (videoCapture.isOpened()) 
        {
            if (!videoCapture.read(frame)) 
            {
                System.err.println("Error: Could not read frame.");
                break;
            }

            // Process frame to find the tennis ball
            Point ballCenter = detectTennisBall(frame);

            if (ballCenter != null) 
            {
                ballDetected = true;  // Ball detected in this frame

                ballCenter.x = frame.width() - ballCenter.x;

                smoothedX = smoothedX + (ballCenter.x - smoothedX) * 0.2;
                smoothedY = smoothedY + (ballCenter.y - smoothedY) * 0.2;

                Platform.runLater(() -> {
                    trackerCircle.setCenterX(smoothedX);
                    trackerCircle.setCenterY(smoothedY);

                    // Update enemy positions and check for collisions
                    for (Enemy enemy : enemies) 
                    {
                        enemy.move();
                        enemy.checkBounds(800, 600);
                        if (checkCollision(trackerCircle, enemy.getShape())) {
                            handleCollision();
                        }
                    }
                });
                noBallDetectedText.setVisible(false);
            }
            else 
            {
                ballDetected = false;  // No ball detected
                noBallDetectedText.setVisible(true);
            }
        }
    }


    // Detects the tennis ball and draws a box around it
    private Point detectTennisBall(Mat frame) 
    {
        Mat hsv = new Mat();
        Mat mask = new Mat();

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

        // The color range for the tennis ball
        Scalar lowerBound = new Scalar(29, 86, 6);
        Scalar upperBound = new Scalar(64, 255, 255);

        Core.inRange(hsv, lowerBound, upperBound, mask);

        // Draws a bounding box around everything with such color
        Mat hierarchy = new Mat();
        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        Point ballCenter = null;

        // Find the largest bounding box
        double maxArea = 0;
        for (MatOfPoint contour : contours) 
        {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) 
            {
                maxArea = area;
                Rect boundingRect = Imgproc.boundingRect(contour);
                ballCenter = new Point(boundingRect.x + boundingRect.width / 2.0,
                        boundingRect.y + boundingRect.height / 2.0);
            }
        }

        return ballCenter;
    }

    // Checks the collision of the enemy and player
    private boolean checkCollision(Circle player, Circle enemy) 
    {
        double distance = Math.sqrt(Math.pow(player.getCenterX() - enemy.getCenterX(), 2) +
        		Math.pow(player.getCenterY() - enemy.getCenterY(), 2));
        return distance < (player.getRadius() + enemy.getRadius());
    }

    // Ends game if collision is detected
    private void handleCollision() 
    {
        Platform.runLater(() -> 
        {
            // Stop video capture and executor
            if (videoCapture != null) 
            {
                videoCapture.release();
            }
            if (executorService != null) 
            {
                executorService.shutdownNow();
            }

            // Update high score if necessary
            if (score > highScore) {
                highScore = score;
            }

            
            showDeathMenu();
        });
    }

    // Show death menu
    private void showDeathMenu() 
    {
        Pane deathMenuRoot = new Pane();
        deathMenuRoot.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // add stylesheet

        Text title = new Text("You Died!");
        title.setFont(new Font(24));
        title.setFill(Color.WHITE);
        title.setLayoutX(325);
        title.setLayoutY(200);

        // Display score on death menu
        Text scoreDisplay = new Text("Score: " + score);
        scoreDisplay.setFont(new Font(18));
        scoreDisplay.setFill(Color.WHITE);
        scoreDisplay.setLayoutX(150);
        scoreDisplay.setLayoutY(225);

        // Display high score
        Text highScoreDisplay = new Text("High Score: " + highScore);
        highScoreDisplay.setFont(new Font(18));
        highScoreDisplay.setFill(Color.WHITE);
        highScoreDisplay.setLayoutX(500);
        highScoreDisplay.setLayoutY(225);

        Button restartButton = new Button("Restart Game");
        restartButton.setLayoutX(350);
        restartButton.setLayoutY(350);
        restartButton.setOnAction(event -> restartGame());

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.setLayoutX(350);
        mainMenuButton.setLayoutY(400);
        mainMenuButton.setOnAction(event -> showMainMenu());

        deathMenuRoot.getChildren().addAll(title, scoreDisplay, highScoreDisplay, restartButton, mainMenuButton);

        Scene deathMenuScene = new Scene(deathMenuRoot, 800, 600);
        primaryStage.setScene(deathMenuScene);
        primaryStage.setTitle("Game Over");
        primaryStage.show();
        score = 0;  
        enemies.clear();
        smoothedX = 0;
        smoothedY = 0;

        // Stops the score from going up
        if (scoreTimeline != null) 
        {
            scoreTimeline.stop();
        }

     
    }

    // Restart the game
    private void restartGame() 
    {
        startGame();
    }

    // Increase score over time
    private void increaseScore() 
    {
    	if(ballDetected)
    	{
    		score++;
            Platform.runLater(() -> scoreText.setText("Score: " + score));
    	}

    }

    public static void main(String[] args) 
    {
        launch(args);
    }
}

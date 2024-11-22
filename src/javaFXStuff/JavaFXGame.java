package javaFXStuff;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.core.Rect;
import java.util.List;

public class JavaFXGame extends Application {

    private VideoCapture capture;

    @Override
    public void start(Stage primaryStage) {
        // Create a Pane layout for JavaFX
        Pane pane = new Pane();  // Pane allows absolute positioning of children

        // Set up the mouse click event to place the image where you click
        pane.setOnMouseClicked(e -> {
            // When the pane is clicked, we will show a PNG image at the click position
            double xPos = e.getX(); // Get the x-coordinate of the click
            double yPos = e.getY(); // Get the y-coordinate of the click

            // Load the PNG image (make sure to provide the correct path to your .png image)
            Image image = new Image("file:/C:/Users/ericw/Downloads/pixil-frame-0%20(16).png"); // Replace with your PNG image path
            ImageView imageView = new ImageView(image);

            // Resize the image (adjust values to make it smaller or larger)
            imageView.setFitWidth(10); // Set width to 50 pixels (resize the image)
            imageView.setFitHeight(10); // Set height to 50 pixels (resize the image)
            imageView.setPreserveRatio(true); // Maintain the aspect ratio

            // Set the image position at the click location
            imageView.setX(xPos - imageView.getFitWidth() / 2); // Center the image on the click point
            imageView.setY(yPos - imageView.getFitHeight() / 2); // Center the image on the click point

            // Add the image to the pane
            pane.getChildren().add(imageView);
        });

        // Create a scene with the pane layout
        Scene scene = new Scene(pane, 600, 400);

        // Set up and display the primary stage
        primaryStage.setTitle("JavaFX Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start OpenCV in a new thread to capture and display frames
        new Thread(this::startCamera).start();
    }

    public void startCamera() {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Set up VideoCapture to use the default camera (ID 0)
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Error: Camera not found.");
            return;
        }

        // Continuously capture and display frames from the camera
        captureAndDisplayFrames();
    }

    private void captureAndDisplayFrames() {
        Mat frame = new Mat();
        Mat hsvFrame = new Mat();
        Mat mask = new Mat();
        Mat outputFrame = new Mat();

        while (true) {
            if (capture.read(frame)) {
                // Convert frame to HSV color space
                Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

                // Define the range of yellow color in HSV
                Scalar lowerYellow = new Scalar(20, 100, 100);  // Lower bound for yellow
                Scalar upperYellow = new Scalar(40, 255, 255);  // Upper bound for yellow

                // Threshold the image to get only yellow regions
                Core.inRange(hsvFrame, lowerYellow, upperYellow, mask);

                // Perform morphological operations (optional) to remove noise and smooth the mask
                Imgproc.erode(mask, mask, new Mat(), new org.opencv.core.Point(-1, -1), 2);
                Imgproc.dilate(mask, mask, new Mat(), new org.opencv.core.Point(-1, -1), 2);

                // Find contours in the mask
                List<MatOfPoint> contours = new java.util.ArrayList<>();
                Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                // Loop through all contours to find the tennis ball
                for (MatOfPoint contour : contours) {
                    // Calculate the area of each contour
                    double area = Imgproc.contourArea(contour);
                    if (area > 100) {  // Filter out small contours (noise)
                        // Get the bounding box around the contour
                        Rect boundingBox = Imgproc.boundingRect(contour);

                        // Draw the bounding box around the detected tennis ball
                        Imgproc.rectangle(frame, boundingBox.tl(), boundingBox.br(), new Scalar(0, 255, 0), 2);
                    }
                }

                // Show the processed frame in an OpenCV window
                HighGui.imshow("Camera Feed", frame);

                // Break the loop if the user presses any key
                if (HighGui.waitKey(1) >= 0) {
                    break;
                }
            }
        }

        // Release the capture and close all OpenCV windows
        capture.release();
        HighGui.destroyAllWindows();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

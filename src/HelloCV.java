import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

public class HelloCV {

    private VideoCapture capture;

    public static void main(String[] args) {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        HelloCV helloCV = new HelloCV();
        helloCV.startCamera();
    }

    public void startCamera() {
        // Set up VideoCapture to use the default camera (ID 0)
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Error: Camera not found.");
            return;
        }

        // Start capturing frames from the camera and display them
        captureAndDisplayFrames();
    }

    // Method to capture and display frames using OpenCV's imshow
    private void captureAndDisplayFrames() {
        Mat frame = new Mat();
        while (true) {
            if (capture.read(frame)) {
                // Display the frame in an OpenCV window
                // "Camera Feed" is the name of the window
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
}

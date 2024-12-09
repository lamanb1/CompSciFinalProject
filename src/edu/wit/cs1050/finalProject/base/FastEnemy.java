package edu.wit.cs1050.finalProject.base;

public class FastEnemy extends Enemy {
    public FastEnemy(double x, double y, double radius, javafx.scene.paint.Color color, double speedX, double speedY) {
        super(x, y, radius, color, speedX, speedY);
    }

    @Override
    public void move() {
        // Get the target position (the ball's position)
        double targetX = CompProjectorMain.smoothedX;  // Player's X position
        double targetY = CompProjectorMain.smoothedY;  // Player's Y position

        // Calculate the difference in X and Y direction
        double deltaX = targetX - shape.getCenterX();
        double deltaY = targetY - shape.getCenterY();

        // Calculate the distance to the target (the ball)
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // If the distance is not zero, normalize the direction and update speed
        if (distance > 0) {
            // Normalize the direction (make it a unit vector) and scale by speed
            speedX = (deltaX / distance) * 3;  // Speed factor 3 (adjust as needed)
            speedY = (deltaY / distance) * 3;  // Speed factor 3 (adjust as needed)
        }

        // Update the enemy's position
        shape.setCenterX(shape.getCenterX() + speedX);
        shape.setCenterY(shape.getCenterY() + speedY);
    }
}

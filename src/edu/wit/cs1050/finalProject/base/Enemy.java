package edu.wit.cs1050.finalProject.base;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public abstract class Enemy implements Movable {
    protected Circle shape;
    protected double speedX;
    protected double speedY;

    public Enemy(double x, double y, double radius, Color color, double speedX, double speedY) {
        this.shape = new Circle(x, y, radius, color);
        this.speedX = speedX;
        this.speedY = speedY;
    }
    

    public Circle getShape() {
        return shape;
    }

    public void checkBounds(double width, double height) {
        if (shape.getCenterX() <= 0 || shape.getCenterX() >= width) {
            speedX = -speedX;
        }
        if (shape.getCenterY() <= 0 || shape.getCenterY() >= height) {
            speedY = -speedY;
        }
    }

    @Override
    public abstract void move();
}

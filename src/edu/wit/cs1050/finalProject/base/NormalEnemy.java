package edu.wit.cs1050.finalProject.base;

//Just bounces random and such
public class NormalEnemy extends Enemy {
    public NormalEnemy(double x, double y, double radius, javafx.scene.paint.Color color, double speedX, double speedY) {
        super(x, y, radius, color, speedX, speedY);
    }

    @Override
    public void move() {
        shape.setCenterX(shape.getCenterX() + speedX);
        shape.setCenterY(shape.getCenterY() + speedY);
    }
}

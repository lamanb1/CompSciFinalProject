package edu.wit.cs1050.finalProject.base;

//Only moves in 1 direction but fast
public class FastBounceEnemy extends Enemy {

    public FastBounceEnemy(double x, double y, double radius, javafx.scene.paint.Color color, double speedX, double speedY) 
    {
        super(x, y, radius, color, speedX, speedY);

        //Set movement direction based on spawn location
        if (y == 20 || y == 580) 
        { 
            this.speedX = 0;  //No horizontal movement
        } 
        else if (x == 20 || x == 780) 
        { 
            this.speedY = 0;  //No vertical movement
        }
    }

    @Override
    public void move() {
        //Move vertically if spawned on top/bottom
        if (speedX == 0) 
        {
            shape.setCenterY(shape.getCenterY() + speedY);

            //Reverse movement when hitting top or bottom boundaries
            if (shape.getCenterY() <= 20 || shape.getCenterY() >= 580) 
            {
                speedY = -speedY; 
                if (shape.getCenterY() <= 20) {
                    shape.setCenterY(21); 
                } else if (shape.getCenterY() >= 580) {
                    shape.setCenterY(579); 
                }
            }
        } 
        //Move horizontally if spawned on left/right
        else if (speedY == 0) 
        {
            shape.setCenterX(shape.getCenterX() + speedX);

            //Reverse movement when hitting left or right boundaries
            if (shape.getCenterX() <= 20 || shape.getCenterX() >= 780) 
            {
                speedX = -speedX; 
                if (shape.getCenterX() <= 20) {
                    shape.setCenterX(21); 
                } else if (shape.getCenterX() >= 780) {
                    shape.setCenterX(779); 
                }
            }
        }
    }
}

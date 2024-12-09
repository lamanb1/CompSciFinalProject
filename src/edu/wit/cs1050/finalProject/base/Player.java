package edu.wit.cs1050.finalProject.base;


import javafx.scene.shape.Shape;

public class Player extends Shape implements Movable
{
	private int coinCount = 0;
	private int health = 3;
	
	public int getCoins()
	{
		return coinCount;
	}
	public void setCoins(int num)
	{
		coinCount = num;
	}
	public int getHealth()
	{
		if(health <= 0)
		{
			health = -1;
		}
		return health;
	}
	public void loseHealth(int num)
	{
		health -= num;
	}
	public void addHealth(int num)
	{
		health += num;
	}
	
	@Override
	public void move()
	{
		
	}
	
}

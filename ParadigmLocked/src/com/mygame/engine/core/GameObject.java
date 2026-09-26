package com.mygame.engine.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/*
    Important Notes:
    This is an abstract class for GameObject, used for any instance of a game object though there will be subclass implementations
 */

public abstract class GameObject {
    //Field variables
   protected int x, y; // Changed from private to protected
   protected int width, height;
   protected BufferedImage sprite;
    //Default Constructor
    public GameObject(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    //Field Variable Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth(){ return width; }
    public int getHeight(){ return height; }

    //Mutators
    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }
    public void translate(int dx, int dy){
        this.x += dx;
        this.y += dy;
    }

    //unsure if needed, just to remove the unused variable comment (hope this doesnt produce excess methods)
    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    //Abstract Methods
    public abstract void draw(Graphics2D g2d);
    public abstract void update(double deltaTime);
 
    public java.awt.Rectangle getBounds(){
        return new java.awt.Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public boolean collidesWith(GameObject other){
        return getBounds().intersects(other.getBounds());
    }

}

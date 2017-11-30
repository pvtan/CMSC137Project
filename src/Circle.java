import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.awt.FontFormatException;

import java.io.IOException;
import java.io.File;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.LinkedList;

public class Circle extends GameObject {
  private Font font;

  private float width = 20, height = 20;
  private DatagramSocket socket;
  private float prevX;
  private float prevY;
  private String server;

  public Circle(float x, float y, String name, InetAddress inetAddress, int portNumber, String server) {
    super(x, y, name, inetAddress, portNumber, "circle");
    this.server = server;
    
    try {
		  GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		  font = Font.createFont(Font.TRUETYPE_FONT, new File("assets/fonts/OpenSans-Regular.ttf"));
		  
		  ge.registerFont(font);
		} catch (IOException|FontFormatException e) {
		  e.printStackTrace();
		}
    
    try {
      socket = new DatagramSocket();      
    } catch (Exception e) {
    }
  }

  private void send(Circle tempObject) {
    String message = "PLAYER " + 
                    tempObject.getName() + " " + 
                    tempObject.getX() + " " + 
                    tempObject.getY() + " " + 
                    tempObject.getScore() + " " +
                    tempObject.isAlive();

    try {
      byte[] buffer = message.getBytes();
      InetAddress address = InetAddress.getByName(server);
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4444);
      socket.send(packet);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void tick(LinkedList<GameObject> objects) {
    prevX = getX(); 
    prevY = getY();
    setX(getX()+getVelX());
    setY(getY()+getVelY());

    if (prevX != getX() || prevY != getY()){
      send(this);
    }

    collision(objects);
  }

  public void collision(LinkedList<GameObject> objects){
    for(int i = 0; i < objects.size(); i++) {
      GameObject tempObject = objects.get(i);
      
      if(tempObject.getType().equals("block")) {
        if(getBounds().intersects(tempObject.getBounds()) ||
          getBoundsTop().intersects(tempObject.getBounds()) ||
          getBoundsLeft().intersects(tempObject.getBounds()) ||
          getBoundsRight().intersects(tempObject.getBounds())
        ) {
          this.isDead();
          send(this);
          objects.remove(this);
        }
      } else if(tempObject.getType().equals("food")) {
        if(getBounds().intersects(tempObject.getBounds()) ||
          getBoundsTop().intersects(tempObject.getBounds()) ||
          getBoundsLeft().intersects(tempObject.getBounds()) ||
          getBoundsRight().intersects(tempObject.getBounds())
        ) {
          setScore(5);
          width += 2;
          height += 2;
          send(this);
          objects.remove(tempObject);
        }
      } else if(tempObject.getType().equals("bomb")) {
        if(getBounds().intersects(tempObject.getBounds()) ||
          getBoundsTop().intersects(tempObject.getBounds()) ||
          getBoundsLeft().intersects(tempObject.getBounds()) ||
          getBoundsRight().intersects(tempObject.getBounds())
        ) {
          this.isDead();
          send(this);
          objects.remove(this);
          objects.remove(tempObject);
        }
      } else if(tempObject.getType().equals("circle")) { //collided with other players
        Circle temp = (Circle) tempObject;
        if(getBounds().intersects(tempObject.getBounds()) ||
          getBoundsTop().intersects(tempObject.getBounds()) ||
          getBoundsLeft().intersects(tempObject.getBounds()) ||
          getBoundsRight().intersects(tempObject.getBounds())
        ) {
          if(temp.getWidth() < this.getWidth() && !temp.hasPowerup()) {
            width += temp.getWidth()/4;
            height += temp.getHeight()/4;
            setScore(15);
            temp.isDead();
            send(temp);
            objects.remove(tempObject);
          }
        }
      } else if(tempObject.getType().equals("powerup")) {
        if(getBounds().intersects(tempObject.getBounds()) ||
          getBoundsTop().intersects(tempObject.getBounds()) ||
          getBoundsLeft().intersects(tempObject.getBounds()) ||
          getBoundsRight().intersects(tempObject.getBounds())
        ) {
          if(!this.hasPowerup()) {
            this.setPowerup(!hasPowerup()); //TO DO: power up time limit
            objects.remove(tempObject);
          }
        }
      }
    }
  }

  public void render(Graphics g) {
    g.setColor(getColor());
    g.fillOval((int)getX(), (int)getY(), (int)width, (int)height);

    font = font.deriveFont(Font.PLAIN, 17);
    Graphics2D g2d = (Graphics2D) g;
    g.setColor(Color.white);
    g2d.setFont(font);
    g2d.drawString(getName(), (int)(getX()+(width/3)), (int)(getY()+height));
    g2d.drawString(Integer.toString(this.getScore()), (int)(getX()+(width/3)), (int)(getY()+(height/2)));
  }

  public Rectangle getBounds() {
    return new Rectangle((int) ((int)getX() + (width/2) - ((width/2)/2)), (int) ((int)getY()+(height/2)), (int)width/2, (int)height/2);
  }
  public Rectangle getBoundsTop() {
    return new Rectangle((int) ((int)getX() + (width/2) - ((width/2)/2)), (int)getY(), (int)width/2, (int)height/2);
  }
  public Rectangle getBoundsRight() {
    return new Rectangle((int)((int)getX() + width - 5), (int)getY(), (int)5, (int)height);
  }
  public Rectangle getBoundsLeft() {
    return new Rectangle((int)getX(), (int)getY(), (int)5, (int)height);
  }

  public float getHeight() {
    return this.height;
  }

  public float getWidth() {
    return this.width;
  }
}
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.LinkedList;

public class Circle extends GameObject {

  private float width = 40, height = 40;
  private DatagramSocket socket;
  private float prevX;
  private float prevY;

  public Circle(float x, float y, String name, InetAddress inetAddress, int portNumber) {
    super(x, y, name, inetAddress, portNumber);
    try {
      socket = new DatagramSocket();      
    } catch (Exception e) {
    }
  }

  public void tick(LinkedList<GameObject> objects) {
    prevX = x; 
    prevY = y;
    x += velX;
    y += velY;

    if (prevX != x || prevY != y){
      String message = "PLAYER " + objectName + " " + x + " " + y;

      try {
        byte[] buffer = message.getBytes();
        InetAddress address = InetAddress.getByName("localhost");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4444);
        socket.send(packet);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    collision(objects);
  }

  public void collision(LinkedList<GameObject> objects){
    for(int i = 0; i < objects.size(); i++) {
      GameObject tempObject = objects.get(i);
      //check if other players
      //name can be used for identity
      if(tempObject.getName().equals("block")) {
        if(getBounds().intersects(tempObject.getBounds())) {
          
          y = tempObject.getY() - height;
          velY = 0;
        }

        if(getBoundsTop().intersects(tempObject.getBounds())) {
          y = tempObject.getY() + 5;
          velY = 0;
        }

        if(getBoundsLeft().intersects(tempObject.getBounds())) {
          x = tempObject.getX() + 5;
          velX = 0;
        }

        if(getBoundsRight().intersects(tempObject.getBounds())) {
          x = tempObject.getX() - width;
          velX = 0;
        }
      }
    }
  }

  public void render(Graphics g) {
    g.setColor(playerColor);
    g.fillOval((int)x, (int)y, (int)width, (int)height);

    Graphics2D g2d = (Graphics2D) g;
    g.setColor(Color.yellow);
    g2d.draw(getBounds());
    g2d.draw(getBoundsRight());
    g2d.draw(getBoundsLeft());
    g2d.draw(getBoundsTop());
  }

  public Rectangle getBounds() {
    return new Rectangle((int) ((int)x + (width/2) - ((width/2)/2)), (int) ((int)y+(height/2)), (int)width/2, (int)height/2);
  }
  public Rectangle getBoundsTop() {
    return new Rectangle((int) ((int)x + (width/2) - ((width/2)/2)), (int)y, (int)width/2, (int)height/2);
  }
  public Rectangle getBoundsRight() {
    return new Rectangle((int)((int)x + width - 5), (int)y, (int)5, (int)height);
  }
  public Rectangle getBoundsLeft() {
    return new Rectangle((int)x, (int)y, (int)5, (int)height);
  }
}
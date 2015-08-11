package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;
import java.io.*;



public class copycat extends Applet {
  Image offscreen;
  Graphics offs;

  boolean done_loading_image = false;

  public Image loadimage(String s){
    done_loading_image = false;
    Image i = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("/org/speakeasy/copycat/" + s));
    offs.drawImage(i,0,0,this);
    return i;
  }
  public void init(){
     offscreen = createImage(10,10);
     offs = offscreen.getGraphics();

     //GraphicsObject.watch = loadimage("watchsm.gif");
     GraphicsObject.wood = loadimage("wood.gif");
     GraphicsObject.bin = loadimage("bin.gif");

     TestWindow thewindow = new TestWindow(this);
  }

  public boolean imageUpdate(Image img, int infoflags, int x, int y,
                         int width, int height){
     if (infoflags == ALLBITS){
        done_loading_image = true;
        return false;
     }
     else return true;
  }

}
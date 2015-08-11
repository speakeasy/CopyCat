package org.speakeasy.copycat;

import java.applet.*;
import java.awt.*;

public class frametest extends Applet {
  public void init(){
     TestWindow thewindow = new TestWindow(this);
  }
}

class testapp extends Applet {
 public void start(){
   repaint();
 }
 public void paint(Graphics g){
   g.drawLine(0,0,300,300);
 }
}

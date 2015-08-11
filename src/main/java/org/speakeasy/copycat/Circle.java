package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Circle extends GraphicsObject {
   boolean Solid;
   Circle(int px1,int py1,int px2,int py2){
     foreground=Color.black; background=Color.white;
     x1=px1; y1=py1; x2=px2; y2=py2;
     Solid=false;
     Redraw=false;
     Resize=true;
   }
   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     if (Resize) Calculate_Coors();
     if (Solid) {
       g.setColor(background);
       g.fillOval(sx1,sy1,sx2-sx1,sy2-sy1);
     }


     // calculate the colours of the object
     if ((parentframe!=null)&&(parentframe.Selected==true))
           g.setColor(Color.white);
     else g.setColor(foreground); 
     g.drawOval(sx1,sy1,sx2-sx1,sy2-sy1);
     Redraw=false;
     Resize=false;
   }
}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Line extends GraphicsObject {
   Line(int px1,int py1,int px2,int py2){
     x1=px1; y1=py1; x2=px2; y2=py2;
     foreground=Color.black; background=Color.white;
     Redraw = false;
     Resize = true;
   }
   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     if (Resize) Calculate_Coors();

     if ((parentframe!=null)&&(parentframe.Selected==true))
           g.setColor(Color.white);
     else g.setColor(foreground); 

     g.drawLine(sx1,sy1,sx2,sy2);
     Redraw=false;
     Resize=false;
   }
}
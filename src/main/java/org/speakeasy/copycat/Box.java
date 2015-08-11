package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Box extends GraphicsObject {
   boolean Solid;
   Box(int px1,int py1,int px2,int py2){
     x1=px1; y1=py1; x2=px2; y2=py2;
     Solid=false;
     foreground=Color.black; background=Color.white;
     Redraw=false;
     Resize=true;
   }
   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     if (Resize) Calculate_Coors();

     if (Solid){
       g.setColor(background);
       g.fillRect(sx1+1,sy1+1,sx2-sx1-1,sy2-sy1-1);
     }
     if (foreground!=null){
       g.setColor(foreground);
       g.drawRect(sx1,sy1,sx2-sx1,sy2-sy1);
     }
     Redraw=false;
     Resize=false;
   }
}
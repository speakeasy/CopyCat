package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class MyImage extends GraphicsObject {
  Image theImage;
  MyImage(int px1,int py1,int px2,int py2,Image i){
     x1=px1; y1=py1; x2=px2; y2=py2;
     theImage = i;
     Redraw = false;
     Resize = true;
   }
   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     if (Resize) Calculate_Coors();
     boolean ok =g.drawImage(theImage,sx1,sy1,sx2-sx1,sy2-sy1,null);
     Redraw=false;
     Resize=false;
     if (!ok) parentarea.Redraw = true;
   }


}
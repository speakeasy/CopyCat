package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Frames extends GraphicsObject {
   Vector objects; 
   Frames(int xp1, int yp1, int xp2, int yp2, Area ara){
      x1 = xp1; x2 = xp2; y1 = yp1; y2 = yp2;
      objects = new Vector();
      Selected = false;
      ara.AddObject(this);
      background=Color.white;
      Redraw=false;
      Resize=true;
   } 
   Frames(int xp1, int yp1, int xp2, int yp2, Area ara, Color back){
      x1 = xp1; x2 = xp2; y1 = yp1; y2 = yp2;
      objects = new Vector();
      Selected = false;
      ara.AddObject(this);
      background=back;
      Redraw=false;
      Resize=true;
   } 
   
   public void AddObject(GraphicsObject Obj){
     objects.addElement(Obj);
     Obj.parentframe = this;
     Obj.parentarea = this.parentarea;
     Obj.Redraw=true;
     Obj.Resize = true;
   }

   public void Draw(){
     Graphics g;
     GraphicsObject ob;
     g=parentarea.screen;
     if (Resize) Calculate_Coors();
     if (Redraw) {     
       if (Selected) g.setColor(Color.black);
       else g.setColor(background);
       g.fillRect(sx1,sy1,sx2-sx1,sy2-sy1);
       g.setColor(Color.black);
       g.drawRect(sx1,sy1,sx2-sx1,sy2-sy1); 
     }
     // draw objects
     for (int i=0; i<objects.size(); i++){
       ob=(GraphicsObject)objects.elementAt(i);
       if (Redraw) ob.Redraw = true;
       if (Resize) ob.Resize = true;
       ob.Draw();
     }
     Redraw=false;
     Resize=false;
   }
   public boolean PointerIn(int x, int y){
     if ((x>=sx1)&&(x<=sx2)&&(y>=sy1)&&(y<=sy2)) return true;
     return false;

   }

}
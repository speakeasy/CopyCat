package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class Area extends GraphicsObject {
   Vector objects;
   Graphics screen;

   int oldwidth = -1;
   int oldheight = -1; // the old dimensions of the screen
   boolean Shift_Right = false;  // if true, shifts the window right by
                // half the screen

   Area(int xp1, int yp1, int xp2, int yp2){
      x1= xp1; x2= xp2; y1 = yp1; y2 = yp2;
      objects = new Vector();
      background = Color.white;
      foreground = Color.black;
      Redraw= false;
      Shift_Right = false;
   }

   public void create_screen(int width, int height){
      double ratiox, ratioy;

      ratiox=((double)(width))/((double)(1000.0));
      ratioy=((double)(height))/((double)(1000.0));

      sx1 = (int)(ratiox*(double)(x1));
      sx2 = (int)(ratiox*(double)(x2));
      sy1 = (int)(ratioy*(double)(y1));
      sy2 = (int)(ratioy*(double)(y2));

   }

   public void Draw(Graphics g,int width, int height){
     double ratiox, ratioy;
     GraphicsObject ob;
     boolean rd = Redraw;
     Redraw = false;

     screen = g;
     if (Shift_Right)
        g.translate(half_width,0);

     if ((oldwidth!=width)||(oldheight!=oldheight))
       create_screen(width,height);

     if ((rd)||(RedrawAll)){
       screen.setColor(background);
       screen.fillRect(sx1,sy1,sx2-sx1,sy2-sy1);
       screen.setColor(Color.black);
       screen.drawRect(sx1,sy1,sx2-sx1,sy2-sy1);
       g.setColor(Color.black);
       g.drawLine(sx1+2,sy2+1,sx2+1,sy2+1); 
       g.drawLine(sx2+1,sy1+2,sx2+1,sy2+1);

     }

     // draw objects
     boolean show = false;
     for (int i=0; i<objects.size(); i++){
       ob=(GraphicsObject)objects.elementAt(i);
       ob.parentarea = this;
       if ((Resize)||(ResizeAll)) ob.Resize = true;
       if ((rd)||(RedrawAll)) ob.Redraw=true;
       if ((ob.Redraw==true)&&(draw_proposed)) { ob.Draw(); show = true; }
       if ((ob.Redraw==true)&&(!draw_proposed)&&
           (ob.level>2)) { ob.Draw(); show = true; }
     }

     if ((show==true)&&(!RedrawAll)){
       // add this to the windows to be redrawn
       Areas.RedrawAreas.addElement(this);

     }

     Resize=false;
     if (Shift_Right)
        g.translate(-half_width,0);

   }
   public void AddObject(GraphicsObject Obj){
     objects.addElement(Obj);
     Obj.parentarea = this;
     Obj.parentframe = null;
     Obj.Redraw=true;
   }

   public void AddObject(GraphicsObject Obj, int lev){
     Obj.level = lev;
     if (lev==1) Obj.foreground = GraphicsObject.Green;
     if (lev==2) Obj.foreground = GraphicsObject.DarkGreen;
     if (lev==3) Obj.foreground = Color.black;
     if ((lev<3)&&(draw_proposed)) Redraw=true;
     int pos = objects.size();
     int start = 0;
     while ((start<pos)&&
       (lev>((GraphicsObject)objects.elementAt(start)).level)) start++;
     if (start>=pos) objects.addElement(Obj);
     else objects.insertElementAt(Obj,start);
     Obj.parentarea = this;
     Obj.parentframe = null;
     Obj.Redraw=true;
   }

   public void DeleteObject(GraphicsObject Obj){
     if (!objects.contains(Obj)) return;
     objects.removeElement(Obj);
     if ((draw_proposed)||(Obj.level>2)) Redraw=true;
   }
   public GraphicsObject FindObject(int x, int y){
     GraphicsObject g,ob;
     g=this;

     for (int i=0; i<objects.size(); i++) {
      ob=(GraphicsObject)objects.elementAt(i);
      if (Shift_Right) { if (ob.PointerIn(x-half_width,y)) g=ob; }
      else if (ob.PointerIn(x,y)) g=ob;
     }
     return g;
   }
}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class GraphicsObject {
   static int half_width = 0;
   static Image watch;
   static Image wood;
   static Image bin;
   static Color Grey = new Color(200,200,200);
   static Color DarkGrey = new Color(150,150,150);
   static Color Green = new Color(150,255,150);
   static Color DarkGreen = new Color(0,180,0);
   public static Color[] Colours = new Color[50];
   public static boolean draw_proposed = false;
   int level = 5;

   int x1,y1,x2,y2;       // coordinates of object (0-1000) inside parent
   int sx1,sy1,sx2,sy2;   // coordinates on graphics screen
   Color foreground,background;
   boolean Resize;  // true if object needs to be resized
   boolean Redraw;  // true if object needs to be redrawn
   static boolean RedrawAll=false;
   static boolean ResizeAll=false;
   Frames  parentframe;
   Area   parentarea;
   boolean Selected;
   boolean Visible = true;
   Vector Values = new Vector();  // used for slipnodes to graph etc.

   public static void InitColours(){
     boolean cok = false;

     for (int x=0; x<50; x++){
       int c1=0,c2=0,c3=0;
       cok = false;
       while (!cok){
         c1 = (int)(random.rnd()*255.0);
         c2 = (int)(random.rnd()*255.0);
         c3 = (int)(random.rnd()*255.0);
         if ((c1>200.0)&&(c2>200.0)&&(c3>200.0)) cok=false;
         else if ((c1>200.0)||(c2>200.0)||(c3>200.0)) cok=true;
         else cok= false;
       }

       Colours[x]=new Color(c1,c2,c3);
     }

   }


   public void Calculate_Coors(){
     // calculates the screen coordinates of the object
     double ratiox, ratioy;
     Frames f;
     GraphicsObject parent;
     if (parentframe==null) parent = parentarea; else parent = parentframe;
     ratiox=(double)(((double)(parent.sx2-parent.sx1))/((double)(1000.0)));
     ratioy=(double)(((double)(parent.sy2-parent.sy1))/((double)(1000.0)));
     sx1=parent.sx1+(int)(ratiox*((double)(x1)));
     sx2=parent.sx1+(int)(ratiox*((double)(x2)));
     sy1=parent.sy1+(int)(ratioy*((double)(y1)));
     sy2=parent.sy1+(int)(ratioy*((double)(y2)));

     // calculate the colours of the object
     if (RedrawAll) Redraw=true;
     if (Redraw==false){
        if (parentarea!=null) if (parentarea.Redraw) Redraw=true;
        if (parentframe!=null) if (parentframe.Redraw) Redraw=true;

     }
   }
	
   public int Xcoor(int x){
      // given the screen coordinates, return the coordinates relative to
      // the given Area
     double ratiox;
     int sc;
     ratiox=(double)(((double)(sx2-sx1))/((double)(1000.0)));
     sc=(int)(((double)(x-sx1))/ratiox);
     return sc;
   }
	
   public int Ycoor(int y){
      // given the screen coordinates, return the coordinates relative to
      // the given Area
     double ratioy;
     int sc;
     ratioy=(double)(((double)(sy2-sy1))/((double)(1000.0)));
     sc=(int)(((double)(y-sy1))/ratioy);
     return sc;
   }

   public void Draw(){  }
   public void AddObject(GraphicsObject g){};
   public boolean PointerIn(int x, int y){ return false; }
   public void Click(){}
   public void Drop(int x, int y){}
   public GraphicsObject FindObject(int x, int y) {return this; };
}
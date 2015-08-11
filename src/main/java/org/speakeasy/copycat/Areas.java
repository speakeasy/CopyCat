package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Areas {
  static Vector Areas= new Vector();
  static Vector RedrawAreas;
  static Area NewArea(int x1, int y1, int x2, int y2){
    Area w = new Area(x1,y1,x2,y2);
    Areas.addElement(w);
    return w;
  }
  static void Draw(Graphics g, int width, int height){
     Area ob;
     RedrawAreas = new Vector();
     for (int i=0; i<Areas.size(); i++){
       ob=(Area)Areas.elementAt(i);
       if (ob.Visible) ob.Draw(g,width,height);
     }
     GraphicsObject.RedrawAll = false;
     GraphicsObject.ResizeAll = false;
  }

  static void ResizeAllAreas(){
     Area ob;
     for (int i=0; i<Areas.size(); i++){
       ob=(Area)Areas.elementAt(i);
       ob.Redraw = true; ob.Resize = true;
     }
     GraphicsObject.RedrawAll = true;
     GraphicsObject.ResizeAll = true;
  }

  static Area FindArea(int x,int y){
     // returns the area if any that the click occured within
     Area ob,found;
     found = null;
     for (int i=0; i<Areas.size(); i++){
       ob=(Area)Areas.elementAt(i);
       if ((ob.sy1<=y)&&(ob.sy2>=y)&&(ob.Visible)){
          if (ob.Shift_Right){
            if ((ob.sx1<=(x-GraphicsObject.half_width))
                  &&(ob.sx2>=(x-GraphicsObject.half_width)))
               found = ob;
          }
          else if ((ob.sx1<=x)&&(ob.sx2>=x)) found = ob;
       }
     } 
     return found;
  }

  static GraphicsObject FindObject(int x, int y){
      // returns the deepest level of object that is selected
     GraphicsObject found;
     Area ob;
     found = null;
     for (int i=0; i<Areas.size(); i++){
       ob=(Area)Areas.elementAt(i);

       if ((ob.sy1<=y)&&(ob.sy2>=y)&&(ob.Visible)){
          if (ob.Shift_Right){
            if ((ob.sx1<=(x-GraphicsObject.half_width))
                  &&(ob.sx2>=(x-GraphicsObject.half_width)))
               found = ob.FindObject(x,y);
          }
          else if ((ob.sx1<=x)&&(ob.sx2>=x)) found = ob.FindObject(x,y);
       }
     } 
     return found;
  }
}
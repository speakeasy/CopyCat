package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class GraphLine extends GraphicsObject {
   GraphicsObject thing_to_graph = null;
   static int c1,c2,c3;

   GraphLine(GraphicsObject ttd){
      thing_to_graph = ttd;
      do {rndcolor(); } while (!colorsok());
      foreground = new Color(c1,c2,c3);
   }

   public void rndcolor(){
     c1 = (int)(Math.random()*255.0);
     c2 = (int)(Math.random()*255.0);
     c3 = (int)(Math.random()*255.0);

   }

   public boolean colorsok(){
     if ((c1>200.0)&&(c2>200.0)&&(c3>200.0)) return false;
     if ((c1>200.0)||(c2>200.0)||(c3>200.0)) return true;
     return false;
   }

   public void Draw(){
     Graphics g;
     g=parentarea.screen;

     if (thing_to_graph.Values.size()<2) return;
     int startx = parentframe.sx1+1;
     int starty = parentframe.sy1+1;
     double xfrac = ((double)(parentframe.sx2 - startx -1))/
                     (double)(thing_to_graph.Values.size()-1);
     double yfrac = ((double)(parentframe.sy2 - starty -1))/100.0;


     g.setColor(foreground);
     double val = ((Double)(thing_to_graph.Values.elementAt(0))).doubleValue();
     int fromx, fromy, tox, toy;
     fromx = startx;
     fromy = starty+(int)(yfrac*(100.0-val));
     for (int x=1; x<thing_to_graph.Values.size(); x++){
       tox = startx+(int)(((double)(x))*xfrac);
       val = ((Double)(thing_to_graph.Values.elementAt(x))).doubleValue();
       toy = starty+(int)(yfrac*(100.0-val));
       g.drawLine(fromx,fromy,tox,toy);
       fromx=tox; fromy=toy; 
     }

     Redraw=false;
     Resize=false;
   }
}
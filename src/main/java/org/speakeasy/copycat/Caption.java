package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Caption extends GraphicsObject {
   String text;
   String oldtext;
   int fontsize,oldwidth,oldheight,xoff,yoff;
   Font currfont;
   boolean Filled;
   boolean no_border = false;
   int Tick; // 0 = no tick, 1=tick, 2 = cross;
   static double FontScale = 1.0;

   Caption(int px1,int py1,int px2,int py2,String txt){
     x1=px1; y1=py1; x2=px2; y2=py2;
     text=txt;
     oldtext=txt;
     foreground=Color.black; background=Color.white;
     Redraw=false;
     Filled = true;
     Tick=0;
     Resize=true;
   }
   public void CalculateSize(Graphics g){
     oldwidth = sx2-sx1;
     oldheight = sy2-sy1;
     Font testfont = new Font("TimesRoman",Font.PLAIN,100);
     g.setFont(testfont);
     int width = g.getFontMetrics().stringWidth(text);
     int height = g.getFontMetrics().getHeight();
     double xratio = ((double)(oldwidth-1))/((double)width);
     double yratio = ((double)(oldheight-1))/((double)height);
     double fsize;
     if (xratio>yratio) {
       // use yratio to calculate size
       fsize=100.0*yratio;
     }
     else fsize=100.0*xratio;
     if (fsize<1.0) fsize=1.0;
     fsize = fsize*FontScale;
     currfont = new Font("TimesRoman",Font.PLAIN,(int)fsize);
     g.setFont(currfont);
     xoff = (sx2-sx1-g.getFontMetrics().stringWidth(text))/2;
     yoff = g.getFontMetrics().getDescent()+1;
   }

   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     if (Resize) Calculate_Coors();
     if (Resize) CalculateSize(g);
     else if (!(text.equals(oldtext))) CalculateSize(g);
     oldtext=text;
     if (Filled){
        g.setColor(background);
        g.fillRect(sx1,sy1,sx2-sx1,sy2-sy1);
        g.setColor(Color.black);
        if (!no_border) g.drawRect(sx1,sy1,sx2-sx1,sy2-sy1);

     }
     g.setFont(currfont);
     g.setColor(foreground);

     g.drawString(text,sx1+xoff,sy2-yoff);

     Redraw=false;
     Resize=false;
   }
   public void Change_Caption(String s){
     Redraw = true;
     Resize = true;
     oldwidth =0; oldheight=0;
     text = s;
   }
   public boolean PointerIn(int x, int y){
     if ((x>=sx1)&&(x<=sx2)&&(y>=sy1)&&(y<=sy2)) return true;
     return false;

   }

}
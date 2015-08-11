package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class slipnode_minimised extends GraphicsObject{
  static int number_of_slipnodes = 0;
	
  slipnode dad;
  int fontsize,oldwidth,oldheight,xoff,yoff;
  Font currfont;

  slipnode_minimised(slipnode daddy){	
    dad = daddy;
    int col = number_of_slipnodes/12;
    int row = number_of_slipnodes - (col*12);
    number_of_slipnodes++;
    x1=row*83; y1=100+180*col; x2=x1+83; y2=y1+180;
    if (x2>1990) x2=1000;
    Redraw = false;
    Resize = true;
    slipnet.SlipnetSmall.AddObject(this);
  	
  }

   public void CalculateSize(Graphics g){
       oldwidth = sx2-sx1;
       oldheight = sy2-sy1;
       Font testfont;
    
       testfont = new Font("TimesRoman",Font.PLAIN,100);
   	
       int width = g.getFontMetrics(testfont).stringWidth(dad.short_name);
       int height = g.getFontMetrics(testfont).getHeight();
   	
       double xratio = ((double)(oldwidth))/((double)width);
       double yratio = ((double)(oldheight))/((double)height);

   	   double fsize;
       if (xratio>yratio) {
         // use yratio to calculate size
         fsize=100.0*yratio;
       }
       else fsize=100.0*xratio;
       if (fsize<1.0) fsize=1.0;
       fsize = fsize * Caption.FontScale;
   	
       currfont = new Font("TimesRoman",Font.PLAIN,(int)fsize);

       xoff = (sx2-sx1-g.getFontMetrics(currfont).stringWidth(dad.short_name))/2;
       yoff = g.getFontMetrics(currfont).getDescent();

   }


 public void Draw(){
    if (Resize){
     Calculate_Coors();
    }


    Graphics g;
    Color cl;
    int c;
    g=parentarea.screen;

    c=((int)dad.activation)*255/100;
    if (c<0) c=0; if (c>255) c=255;
    cl=new Color(c,c,c);
    g.setColor(cl);
    g.fillRect(sx1+1,sy1+1,sx2-sx1-1,sy2-sy1-1);
    if (c==255){
      g.setColor(Color.red);
      g.drawRect(sx1+1,sy1+1,sx2-sx1-2,sy2-sy1-2);
    }
    g.setColor(Color.red);
    if (dad.clamp) g.drawOval(sx1+1,sy1+1,sx2-sx1-2,sy2-sy1-2);

     if (Resize) CalculateSize(g);
     g.setFont(currfont);
     if (dad.activation>50.0) g.setColor(Color.black);
     else g.setColor(Color.white);
     if (dad.foreground!=null) g.setColor(dad.foreground);
     g.drawString(dad.short_name,sx1+xoff,sy2-yoff);

    Redraw = false; Resize = false;
 }
   public boolean PointerIn(int x, int y){
     if ((x>=sx1)&&(x<=sx2)&&(y>=sy1)&&(y<=sy2)) return true;
     return false;

   }

}
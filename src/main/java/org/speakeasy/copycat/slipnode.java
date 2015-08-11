package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class slipnode extends GraphicsObject{
  double activation, buffer, old_activation;
  boolean clamp,oldclamp = false;
  double intrinsic_link_length,shrunk_link_length = 0.0;
  double conceptual_depth = 0.0;
  double usual_conceptual_depth = 0.0;
  double bond_facet_factor = 0.0;
  String pname;  //a string giving the name of this node
  String short_name;
  Vector category_links, instance_links, has_property_links;
  Vector lateral_slip_links, lateral_nonslip_links, incoming_links;
  Vector outgoing_links;
  Vector codelets; // a set of strings representing the codelets to post
  int fontsize,oldwidth,oldheight,xoff,yoff;
  Font currfont;
  slipnode_minimised child;
  static boolean clamp_bdoa = false;

  slipnode (int px1, int py1, double cd,String pn, String sn){
     category_links = new Vector();
     instance_links = new Vector();
     has_property_links = new Vector();;
     lateral_slip_links = new Vector();
     lateral_nonslip_links = new Vector();
     incoming_links = new Vector();;
     outgoing_links = new Vector();
     codelets = new Vector();

     activation=0.0; buffer=0.0;
     clamp=false;
     conceptual_depth=cd;
     usual_conceptual_depth=cd;
     bond_facet_factor=0.0;
     pname=pn;
     short_name=sn;
     //System.out.println("new node "+short_name+" "+cd);
     intrinsic_link_length = 0.0;
     shrunk_link_length = 0.0;

     foreground=null; background=Color.white;
     x1=px1-25; y1=py1-25; x2=px1+25; y2=py1+25;
     Redraw=false; Resize = true;
     child = new slipnode_minimised(this);

  }

  slipnode (int px1, int py1, double cd,String pn, String sn, double len){
     category_links = new Vector();
     instance_links = new Vector();
     has_property_links = new Vector();;
     lateral_slip_links = new Vector();
     lateral_nonslip_links = new Vector();
     incoming_links = new Vector();;
     outgoing_links = new Vector();

     activation=0.0; buffer=0.0;
     clamp=false;
     conceptual_depth=cd;
     usual_conceptual_depth=cd;
     bond_facet_factor=0.0;
     pname=pn;
     codelets = new Vector();
     short_name=sn;
     intrinsic_link_length = len;
     shrunk_link_length = len*0.4;
     //System.out.println("new node "+short_name+" "+cd+intrinsic_link_length);

     foreground=null; background=Color.white;
     x1=px1-25; y1=py1-25; x2=px1+25; y2=py1+25;
     Redraw=false; Resize = true;
     child = new slipnode_minimised(this);
  }

  public slipnode category(){
    if (category_links.size()==0) return null;
    slipnet_link sl = (slipnet_link)category_links.elementAt(0);
    return sl.to_node;
  }

  public double bond_degree_of_association(){
    double dof;
    // used to calculate strength of bonds of this type
    if ((!clamp_bdoa)&&(activation==100.0)) dof=shrunk_link_length;
    else dof=intrinsic_link_length;
    dof=100.0-dof; dof=Math.sqrt(dof)*11.0;   /// usually *11.0- but lets see what happens
    if (dof>100.0) dof=100.0;
    return dof;
  }

  public double degree_of_association(){
    double dof;
    // used in calculating link lengths
    if (activation==100.0) dof=shrunk_link_length;
    else dof=intrinsic_link_length;
    dof=100.0-dof;
    return dof;
  }

   public void CalculateSize(Graphics g){
     oldwidth = sx2-sx1;
     oldheight = sy2-sy1;
     Font testfont;
    
        testfont = new Font("TimesRoman",Font.PLAIN,100);
     g.setFont(testfont);
     int width = g.getFontMetrics().stringWidth(short_name);
     int height = g.getFontMetrics().getHeight();
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

     g.setFont(currfont);
     xoff = (sx2-sx1-g.getFontMetrics().stringWidth(short_name))/2;
     yoff = g.getFontMetrics().getDescent();
   }


   public void Draw(){
     Graphics g;
     Color cl;
     int c;
     int xdiff, ydiff;

     g=parentarea.screen;
     if ((Resize)||(ResizeAll)) Calculate_Coors();

     c=((int)activation)*255/100;
     if (c<0) c=0; if (c>255) c=255;
     cl=new Color(c,c,c);

     if (oldclamp!=clamp) {
        g.setColor(Color.white);
        g.fillRect(sx1,sy1,1+sx2-sx1,1+sy2-sy1);
     }

     g.setColor(cl);
     g.fillOval(sx1,sy1,sx2-sx1,sy2-sy1);
     g.setColor(Color.black);
     if (activation==(double)100.0) g.setColor(Color.red);
     g.drawOval(sx1,sy1,sx2-sx1,sy2-sy1);

     if (clamp){
        xdiff=(sx2-sx1); ydiff=(sy2-sy1);
        g.drawRect(sx1,sy1,xdiff,ydiff);
     }
      
     if ((Resize)||(ResizeAll))  CalculateSize(g);
     g.setFont(currfont);
     if (activation>50.0) g.setColor(Color.black);
     else g.setColor(Color.white);
     if (foreground!=null) g.setColor(foreground);
     g.drawString(short_name,sx1+xoff,sy2-yoff);
     Redraw=false; Resize=false;
     if (oldclamp!=clamp){
        // draw all bonds - as they will have been erased
        oldclamp = clamp;
        for (int x=0; x<outgoing_links.size(); x++){
           slipnet_link s= (slipnet_link)outgoing_links.elementAt(x);
           s.Draw();
        }
     }

   }
   public boolean PointerIn(int x, int y){
     if ((x>=sx1)&&(x<=sx2)&&(y>=sy1)&&(y<=sy2)) return true;
     return false;

   }
   public void Click() {
     activation=(double) 100.0;

   }

}
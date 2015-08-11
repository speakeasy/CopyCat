package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class slipnet_link extends GraphicsObject{
  slipnode from_node, to_node, label;
  double fixed_length;
  boolean slip_link = false;

  // if a link does not have a label, then it is assigned a fixed length
  slipnet_link (slipnode fr, slipnode to, slipnode lab){
     from_node = fr; to_node = to; label = lab;
     Redraw=false;
     Resize = true;
     fixed_length = 0.0;
     fr.outgoing_links.addElement(this);
     x1 = (from_node.x1+from_node.x2)/2;
     y1 = (from_node.y1+from_node.y2)/2;
     x2 = (to_node.x1+to_node.x2)/2;
     y2 = (to_node.y1+to_node.y2)/2;
  }
  slipnet_link (slipnode fr, slipnode to, double len){
     from_node = fr; to_node = to; fixed_length = len;
     label=null;
     Redraw=false;
     Resize = true;
     fr.outgoing_links.addElement(this);
     x1 = (from_node.x1+from_node.x2)/2;
     y1 = (from_node.y1+from_node.y2)/2;
     x2 = (to_node.x1+to_node.x2)/2;
     y2 = (to_node.y1+to_node.y2)/2;  
}

  public double intrinsic_degree_of_association(){
     if (fixed_length>1.0) return (100.0-fixed_length);
     if (label!=null) return (100.0-label.intrinsic_link_length); 
     return 0.0;
  }

  public double degree_of_association(){
    if ((fixed_length>0.0)||(label==null)) return 100.0-fixed_length;
    return label.degree_of_association();
  }

  public void Draw(){
     Graphics g;
     Color cl;
     int x1,y1,x2,y2;
     int offx1,offy1,offx2,offy2;
     double a;

     g=parentarea.screen;
     if ((Resize)||(ResizeAll)) Calculate_Coors();

     if (!slip_link) g.setColor(GraphicsObject.Grey);
     else g.setColor(new Color(100,100,255));
     g.drawLine(sx1,sy1,sx2,sy2);
     from_node.Draw();
     to_node.Draw();

     Redraw=false;
     Resize = false;
  }

}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class description extends workspace_structure {
  workspace_object object;
  slipnode description_type;
  slipnode descriptor;
  boolean visible;
  String text;
  int fontsize,oldwidth,oldheight,xoff,yoff;
  Font currfont;
  int oldstrength;

  description (workspace_object ob, slipnode dt, slipnode dc){
     object = ob; string = ob.string;
     description_type = dt;
     descriptor = dc;
  }

  public void build_description(){
       description_type.buffer=100.0;
       descriptor.buffer=100.0;

     if (object.has_description(descriptor)) return;
     visible = true;
     x1=object.x1; x2=object.x2;
     y1=object.y2+(object.descriptions.size()*38); y2=y1+38;
     object.descriptions.addElement(this);
     foreground=Color.black;
     Redraw=true;
     text = descriptor.pname;
     oldstrength=0;

       if (!workspace.workspace_structures.contains(this)){
         workspace.WorkspaceArea.AddObject(this);
         workspace.workspace_structures.addElement(this);
       }
       workspace.check_visibility();

  }

  description (workspace_object ob, workspace_string s, slipnode dt, slipnode dc){
     object = ob;
     string = s;
     description_type = dt;
     descriptor = dc;
     visible = true;
     x1=ob.x1; x2=ob.x2;
     y1=ob.y2+(ob.descriptions.size()*38); y2=y1+38;
     //workspace.WorkspaceArea.AddObject(this);
     foreground=Color.black;
     Redraw=true;
     text = dc.pname;
     oldstrength=0;
     //workspace.workspace_structures.addElement(this);

  }

   public void break_description(){
     workspace.workspace_structures.removeElement(this);

     object.descriptions.removeElement(this);
     workspace.WorkspaceArea.DeleteObject(this);
     workspace.check_visibility();
     workspace.WorkspaceArea.Redraw=true;
   }


   public String toString(){
     String s;
     s="description("+descriptor.pname+") of "+object;
     if (object.string==workspace.initial)
       s=s+" in initial string";
     else s=s+" in target string";
     return s;
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
     currfont = new Font("TimesRoman",Font.PLAIN,(int)fsize);
     g.setFont(currfont);
     xoff = (sx2-sx1-g.getFontMetrics().stringWidth(text))/2;
     yoff = g.getFontMetrics().getDescent()+1;
   }

   public void Draw(){
     oldstrength = (int)(total_strength/10.0);
     if (!visible) return; 
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     Calculate_Coors();
 
     if (((sx2-sx1)!=oldwidth)||((sy2-sy1)!=oldheight))   CalculateSize(g);
  
     g.setColor(foreground);
     g.drawRect(sx1,sy1,sx2-sx1,sy2-sy1);
     g.setFont(currfont);

     int cval =(int) (((double)oldstrength)*255.0/10.0);
     if (cval<0) cval=0;
     if (cval>255) cval=255;

     Color col = new Color(cval,0,0);
     g.setColor(col);

     g.drawString(text,sx1+xoff,sy2-yoff);

     Redraw=false;
   }

  public void calculate_internal_strength(){
    internal_strength = descriptor.conceptual_depth;
  }
  public void calculate_external_strength(){
    external_strength = (local_support()+description_type.activation)/2.0;
    Redraw = true;
  }

  public double local_support(){
    int num_supporting_objects = 0;
    // num_supporting_objects = the number of objects in the string
    // with a description of the given object facet
    for (int i=0; i<workspace.workspace_objects.size(); i++){
      workspace_object wo = (workspace_object)workspace.workspace_objects.elementAt(i);
      if (wo!=object){
        if (!(object.recursive_group_member(wo)||wo.recursive_group_member(object))){
            // check to see if this obejct has descriptions that have same description_type
            for (int x=0; x<wo.descriptions.size(); x++){
               description ds = (description)wo.descriptions.elementAt(x);
               if (ds.description_type==description_type) num_supporting_objects++;
            }
        }
      }
    }
    if (num_supporting_objects==0) return 0.0;
    if (num_supporting_objects==1) return 20.0;
    if (num_supporting_objects==2) return 60.0;
    if (num_supporting_objects==3) return 90.0;
    return 100.0;
  }
}

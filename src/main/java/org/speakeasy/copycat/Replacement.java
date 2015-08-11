package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class Replacement extends workspace_structure{
  workspace_object from_obj,to_obj;
  slipnode relation;

  Replacement(workspace_object from, workspace_object to,slipnode rel){
    from_obj=from;
    to_obj=to;
    relation=rel;
     x1=(from_obj.x1+from_obj.x2)/2; x2=(to_obj.x1+to_obj.x2)/2;
     y1=from_obj.y1-75; y2=to_obj.y1+75;
     foreground=Color.black;
     Redraw=true;
    //System.out.println("new replacement formed");
    workspace.WorkspaceSmall.AddObject(this);

  }
   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     if (relation==slipnet.sameness) {Redraw=false; return;} 
     Calculate_Coors();
  
     g.setColor(foreground);
     g.drawArc(sx1,sy1,sx2-sx1,sy2-sy1,0,180);
        // there is a link in a certain direction
        int xpos=(sx1+sx2)/2;
        int hlength = (sx2-sx1)/20;
        int vlength = (sy2-sy1)/10;
        if ((relation==slipnet.successor)||(relation==null)){
          g.drawLine(xpos,sy1,xpos-hlength,sy1+vlength);
          g.drawLine(xpos,sy1,xpos-hlength,sy1-vlength);
        }
        if ((relation==slipnet.predecessor)||(relation==null)){
          g.drawLine(xpos,sy1,xpos+hlength,sy1+vlength);
          g.drawLine(xpos,sy1,xpos+hlength,sy1-vlength);
        }

     
     Redraw=false;
   }
}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class letter extends workspace_object {
   letter(workspace_string s, int lf, int rt, int px1, int py1, int px2, int py2){
     string = s;
     group = null;
     left_string_position = lf;
     leftmost = (lf==1);
     right_string_position = rt;
     rightmost = (rt==(s.length));
     spans_string = false;
     if ((lf==1)&&(rt==(s.length))) spans_string=true;
     x1=px1; y1=py1; x2=px2; y2=py2;
     foreground=Color.black;
     Redraw=true;
     replacement = null;


     descriptions = new Vector();
     extrinsic_descriptions = new Vector();
     outgoing_bonds = new Vector();
     incoming_bonds = new Vector();
     bonds = new Vector();
     left_bond = null;
     right_bond = null;
     group = null;
     correspondence = null;
     changed = false;
     new_answer_letter = false;
     clamp_salience = false;
     pname = "";
     workspace.workspace_objects.addElement(this);
     string.objects.addElement(this);
   }


}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class group extends workspace_object {
   slipnode group_category, direction_category;
   slipnode bond_facet, bond_category;
   Vector bond_list,object_list;
   group(workspace_string s, slipnode gc, slipnode dc, slipnode bf, Vector ol, Vector bl){
     string = s;
     group = null;
     group_category = gc;
     direction_category = dc;
     bond_facet = bf;
     object_list = ol;
     bond_list = bl;
     bond_category = slipnet_formulas.get_related_node(
          group_category,slipnet.bond_category);

     workspace_object leftob = (workspace_object)ol.elementAt(0);
     workspace_object rightob = (workspace_object)ol.elementAt(ol.size()-1);
     left_string_position = leftob.left_string_position;
     leftmost = (left_string_position==1);
     right_string_position = rightob.right_string_position;
     rightmost = (right_string_position==(s.length));

     spans_string = (leftmost&rightmost);
   
     x1=leftob.x1; y1=leftob.y1-50; x2=rightob.x2; y2=leftob.y2;
     if ((rightob.y1-50)<y1) y1=rightob.y1-50;
     foreground=Color.black;
     Redraw=true;


     descriptions = new Vector();
     extrinsic_descriptions = new Vector();
     outgoing_bonds = new Vector();
     incoming_bonds = new Vector();
     bonds = new Vector();
     left_bond = null;
     right_bond = null;
     correspondence = null;
     changed = false;
     new_answer_letter = false;
     clamp_salience = false;
     pname = "";



     if ((bond_list!=null)&&(bond_list.size()>0)){
        bond b = (bond)bond_list.elementAt(0);
        slipnode bbf = b.bond_facet;
        add_bond_description(new description(this,slipnet.bond_facet,bbf));
     }
     add_bond_description(new description(this,slipnet.bond_category,
                 bond_category));

     this.add_description(slipnet.object_category,slipnet.group);
     this.add_description(slipnet.group_category,group_category);
     if (direction_category==null){
        // sameness group - find letter_category
        slipnode letter = ((workspace_object)object_list.elementAt(0)).get_description(bond_facet);
        this.add_description(bond_facet,letter);
     }

     if (direction_category!=null) this.add_description(slipnet.direction_category,direction_category);
     if (spans_string) this.add_description(slipnet.string_position_category,slipnet.whole);
     else if (left_string_position==1)
        this.add_description(slipnet.string_position_category,slipnet.leftmost);
     else if (right_string_position==string.length)
        this.add_description(slipnet.string_position_category,slipnet.rightmost);
     else if (this.middle_object())
        this.add_description(slipnet.string_position_category,slipnet.middle);

     // check whether or not to add length description category
     double prob = length_description_probability();
     if (random.rnd()<prob){
       int length = this.object_list.size();
       if (length<6) this.add_description(slipnet.length,slipnet.slipnet_numbers[length-1]);
     }
   }

   public boolean middle_object(){
     // returns true if this is the middle object in the string
     boolean leftmost_neighbor = false;
     boolean rightmost_neighbor = false;

     for (int x=0; x<string.objects.size(); x++){
       workspace_object ob = (workspace_object)string.objects.elementAt(x);
       if ((ob.leftmost)&&(ob.right_string_position==left_string_position-1))
         leftmost_neighbor = true;
       if ((ob.rightmost)&&(ob.left_string_position==right_string_position+1))
         rightmost_neighbor = true;

     }
     if ((leftmost_neighbor)&&(rightmost_neighbor)) return true;
     return false;
   }

   public void add_bond_description(description d){
     bond_descriptions.addElement(d);
   }

   public double single_letter_group_probability(){
     int loc = number_of_local_supporting_groups();
     double exp;
     if (loc==0) {
        //System.out.println("single letter prob = 0.0");
        return 0.0;
     }
     if (loc==1) exp = 4.0;
     else if (loc==2) exp = 2.0;
     else exp=1.0;
     double val = formulas.temperature_adjusted_probability(
        Math.pow((local_support()/100.0)*
        (slipnet.length.activation/100.0),exp));
     //System.out.println("single letter prob ="+val);
     return val;
   }

   public group flipped_version(){
     // returns a flipped version of this group
     Vector new_bond_list = new Vector();
     for (int x=0; x<bond_list.size(); x++)
       new_bond_list.addElement(((bond)bond_list.elementAt(x)).flipped_version());
     group flipped_group = 
        new group(string,slipnet_formulas.get_related_node(group_category,slipnet.opposite),
            slipnet_formulas.get_related_node(direction_category,slipnet.opposite),
            bond_facet,object_list,new_bond_list);
     return flipped_group;
   } 

   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     Calculate_Coors();
     if (!Redraw) return;
     if (foreground!=null){
       g.setColor(foreground);
       g.drawRect(sx1,sy1,sx2-sx1,sy2-sy1);
       g.drawRect(sx1+1,sy1+1,sx2-sx1-2,sy2-sy1-2);
     }
     Redraw=false;
   }

   public void build_group(){
     workspace.workspace_objects.addElement(this);
     workspace.workspace_structures.addElement(this);
     string.objects.addElement(this);
     workspace.WorkspaceArea.AddObject(this,3);
     workspace.WorkspaceSmall.AddObject(this);
     for (int i=0; i<object_list.size(); i++){
        workspace_object wo = (workspace_object)object_list.elementAt(i);
        wo.group = this;
     }
     build_descriptions();
     workspace.check_visibility();
     this.activate_descriptions();

   }

   public void activate_descriptions(){
     for (int x=0; x<descriptions.size(); x++){
       description d = (description)descriptions.elementAt(x);
       (d.descriptor).buffer=100.0;
     }
   }

   public double length_description_probability(){
     int length = object_list.size();
     if (length>5) return 0.0;
     double cube = (double)(length*length*length);
     double prob = Math.pow(0.5,cube*
                    ((100.0-slipnet.length.activation)/100.0));
     
     double val=formulas.temperature_adjusted_probability(prob);
     if (val<0.06) val = 0.0; // otherwise 1/20 chance always
     //System.out.println(this.toString()+" length description prob = "+val);
     return val;
   }

   public void break_group(){
      //System.out.println("breaking group "+this);
     while (descriptions.size()>0){
        description d = (description)descriptions.elementAt(0);
        d.break_description();
     }
      
     for (int i=0; i<object_list.size(); i++){
        workspace_object wo = (workspace_object)object_list.elementAt(i);
        wo.group = null;
     }
     if (group!=null) group.break_group();

      workspace.WorkspaceArea.DeleteObject(this);
      workspace.WorkspaceSmall.DeleteObject(this);
      workspace.workspace_structures.removeElement(this);
      workspace.workspace_objects.removeElement(this); 
     string.objects.removeElement(this);
     if (correspondence!=null) correspondence.break_correspondence();

     workspace.check_visibility();
     if (left_bond!=null) left_bond.break_bond();
     if (right_bond!=null) right_bond.break_bond();
     workspace.WorkspaceArea.Redraw = true;
   }

   public void calculate_internal_strength(){
     double bff,lc;
     if (bond_facet==slipnet.letter_category) bff=1.0; else bff=0.5;
     double bc = (slipnet_formulas.get_related_node(group_category,slipnet.bond_category)).degree_of_association();
     //System.out.println("related node:"+(slipnet_formulas.get_related_node(group_category,slipnet.bond_category)).pname);
     int len = object_list.size();
     if (len==1) lc=5.0; 
     else if (len==2) lc = 20.0;
     else if (len==3) lc=60.0;
     else lc = 90.0;

     double bcw = Math.pow(bc,0.98);
     double lcw = 100.0-bcw;
     internal_strength = formulas.weighted_average(bc,bcw,lc,lcw);     
     //System.out.println(this+" bc:"+bc+" bcw:"+bcw+" lc:"+lc+" lcw:"+" internal strength = "+internal_strength);
   }

   public void calculate_external_strength(){
      if (spans_string) external_strength = 100.0;
      else external_strength = local_support();
      //System.out.println(this+" external strength = "+external_strength);
   }

   public double local_support(){
     double num = (double)(number_of_local_supporting_groups());
     if (num==0.0) return 0.0;
     double density = local_density();
     double ad= 100.0*Math.sqrt(density/100.0);
     double nf = Math.pow(0.6,1/(num*num*num));
     if (nf>1.0) nf=1.0;
     //System.out.println("local support  density="+density+"num="+num+" ad="+ad+"nf="+nf);
     return ad*nf;
   }

   public int number_of_local_supporting_groups(){
     int count =0;
     for (int i=0; i<string.objects.size(); i++){
       workspace_object wo = (workspace_object) string.objects.elementAt(i);
       if (wo instanceof group){
         group g = (group) wo;
         if ((g.right_string_position<left_string_position)||
             (g.left_string_position>right_string_position))
           if ((g.group_category==group_category)&&
               (g.direction_category==direction_category)) count++;
       }
     }
     return count;
   }

   public double local_density(){
     double sg = (double) number_of_local_supporting_groups();
     double ln = ((double)(string.length))/2.0;
     //System.out.println(this+" local density="+ln+"  sg="+sg+" ln="+ln);
     return 100.0*sg/ln;
   }
}
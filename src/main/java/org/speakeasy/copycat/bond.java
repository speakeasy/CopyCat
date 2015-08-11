package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class bond extends workspace_structure {
  workspace_object from_obj, to_obj;
  workspace_object left_obj, right_obj;
  slipnode bond_category;
  slipnode direction_category;
  boolean bidirectional;  // true if sameness bond
  boolean right; // true if to_obj is on the right
  slipnode bond_facet,from_obj_descriptor,to_obj_descriptor;
  int obj1_ht,obj2_ht;


  bond (workspace_object obfr, workspace_object obto, slipnode bondc,
        slipnode bondf, slipnode fromdes, slipnode todes){
     from_obj = obfr; to_obj = obto;
     if (from_obj.left_string_position>to_obj.right_string_position)
        {left_obj = to_obj; right_obj = from_obj;
          direction_category = slipnet.left; }
        else {left_obj = from_obj; right_obj = to_obj;
          direction_category = slipnet.right; }
     bond_facet = bondf;
     from_obj_descriptor = fromdes;
     to_obj_descriptor = todes;
     bond_category = bondc;

     x1=(left_obj.x1+left_obj.x2)/2; x2=(right_obj.x1+right_obj.x2)/2;
     y1=(left_obj.y1-25);y2=(right_obj.y1-25); if (y2<y1) y1=y2;
     obj1_ht=left_obj.y1+(left_obj.y1-y1);
     obj2_ht=right_obj.y1+(right_obj.y1-y1);     


     right=(to_obj==right_obj);
     bidirectional = (fromdes==todes);
     if (bidirectional) direction_category = null;
     foreground=Color.black;
     Redraw=true;
     string = from_obj.string;

  }

   public bond flipped_version(){
     //returns the flipped version of this bond
     return new bond(to_obj,from_obj,
        slipnet_formulas.get_related_node(bond_category,slipnet.opposite),
        bond_facet, to_obj_descriptor,
        from_obj_descriptor);

   }

   public String toString(){
     String s;
     s=bond_category.pname+" bond between "+left_obj+" and "+right_obj;
     return s;
   }


   public void build_bond(){
    workspace.WorkspaceArea.AddObject(this,3);
    workspace.WorkspaceSmall.AddObject(this);
    workspace.workspace_structures.addElement(this);
    string.bonds.addElement(this);
    bond_category.buffer=100.0;
    if (direction_category!=null) direction_category.buffer=100.0;
    left_obj.right_bond = this;
    right_obj.left_bond = this;
    left_obj.bonds.addElement(this);
    right_obj.bonds.addElement(this);
   }

   public void break_bond(){
      workspace.WorkspaceArea.DeleteObject(this);
      workspace.WorkspaceSmall.DeleteObject(this);
      workspace.workspace_structures.removeElement(this);
      string.bonds.removeElement(this);
    left_obj.right_bond = null;
    right_obj.left_bond = null;
    left_obj.bonds.removeElement(this);
    right_obj.bonds.removeElement(this);
    workspace.WorkspaceArea.Redraw = true;
   }

   public Vector get_incompatible_correspondences(){
      // returns a list of correspondences that are incompatible with
      // this bond
      Vector incc = new Vector();
      if ((left_obj.leftmost)&&(left_obj.correspondence!=null)){
         Correspondence loc = left_obj.correspondence;
         workspace_object wo;
         if (string==workspace.initial)
              wo=(left_obj.correspondence).obj2;
             else wo=(left_obj.correspondence).obj1;
         if ((wo.leftmost)&&(wo.right_bond!=null)){
           if (((wo.right_bond).direction_category!=null)&&
               ((wo.right_bond).direction_category!=direction_category))
                 incc.addElement(loc);
         }
      }

      if ((right_obj.rightmost)&&(right_obj.correspondence!=null)){

         Correspondence roc = right_obj.correspondence;
         workspace_object wo;
         if (string==workspace.initial)
              wo=(right_obj.correspondence).obj2;
             else wo=(right_obj.correspondence).obj1;

         if ((wo.rightmost)&&(wo.left_bond!=null)){
           if (((wo.left_bond).direction_category!=null)&&
               ((wo.left_bond).direction_category!=direction_category))
                 incc.addElement(roc);
         }
      }
      if (incc.size()==0) return null;
      return incc;
   }

   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;

     g.setColor(foreground);
     y2=obj1_ht;
     Calculate_Coors();
     g.drawArc(sx1,sy1,sx2-sx1,sy2-sy1,90,90);  
     int sy22 = sy2;

     y2=obj2_ht;
     Calculate_Coors();
     g.drawArc(sx1,sy1,sx2-sx1,sy2-sy1,0,90);  

     if (!bidirectional){
        // there is a link in a certain direction
        int xpos=(sx1+sx2)/2;
        int hlength = (sx2-sx1)/5;
        if (right) hlength=-hlength;
        int vlength = (sy2-sy1)/3;
        if (sy22<sy2) vlength = (sy22-sy1)/3;
        g.drawLine(xpos,sy1,xpos+hlength,sy1+vlength);
        g.drawLine(xpos,sy1,xpos+hlength,sy1-vlength);

     }
     Redraw=false;
   }

  public void calculate_internal_strength(){
     // bonds between objects of same type(ie. letter or group) are
     // stronger than bonds between different types
     boolean fromgp = (from_obj.left_string_position!=from_obj.right_string_position);
     boolean togp = (to_obj.left_string_position!=to_obj.right_string_position);
     double member_compatibility;
     if (fromgp==togp) member_compatibility=1.0; else member_compatibility=0.7;

     // letter category bonds are stronger
     double bff;
     if (bond_facet==slipnet.letter_category) bff=1.0; else bff=0.7;
     double intstr;
     intstr = member_compatibility*bff*bond_category.bond_degree_of_association();
     //System.out.println(bond_category.pname+" bdoa:"+bond_category.bond_degree_of_association());
     if (intstr>100.0) intstr = 100.0;
     internal_strength = intstr;
     //System.out.println(this+" internal strength:"+internal_strength);
  }

  public int number_of_local_supporting_bonds(){
    int sb = 0;
    for (int i=0; i<string.bonds.size(); i++){
      bond ob = (bond) string.bonds.elementAt(i);
      if (ob.string == from_obj.string){
        if ((!(left_obj.letter_distance(ob.left_obj)==0))&&
            (!(right_obj.letter_distance(ob.right_obj)==0))
            &&(bond_category==ob.bond_category)&&
            (direction_category==ob.direction_category)) sb++;

      }
    }
    return sb;
  }

  public double local_density(){
    // returns a rough measure of the density in the string
    // of the same bond-category and the direction-category of
    // the given bond
    double slot_sum=0.0, support_sum=0.0;

    for (int i=0; i<workspace.workspace_objects.size(); i++){
      workspace_object ob1 = (workspace_object) workspace.workspace_objects.elementAt(i);
      if (ob1.string==string){
         for (int x=0; x<workspace.workspace_objects.size(); x++){
           workspace_object ob2 = (workspace_object) workspace.workspace_objects.elementAt(x);
           if (ob1.string==ob2.string)
             if ((ob1.left_string_position==(ob2.right_string_position+1))||
                 (ob1.right_string_position==(ob2.left_string_position-1))){
                 // they are neighbours
                 slot_sum+=1.0;
                 for (int y=0; y<string.bonds.size(); y++){
                   bond b=(bond)string.bonds.elementAt(y);
                   if ((b!=this)&&(((from_obj==ob1)&&(to_obj==ob2))||
                                   ((from_obj==ob2)&&(to_obj==ob1))))
                   
                   if ((b.bond_category==bond_category)&&
                       (b.direction_category==direction_category))
                       support_sum+=1.0;
                   
                 }
             }
         }
      }
    }

    if (slot_sum==0.0) return 0.0;
    return 100.0*support_sum/slot_sum;
  }

  public void calculate_external_strength(){
     // equals the local support

     double extstr = 0.0;
     double num = (double)(number_of_local_supporting_bonds());
     if (num>0.0){
        double density=local_density();
        density/=100; density = (Math.sqrt(density))*100.0;
        double nf = Math.pow(0.6,(1.0/(num*num*num)));
        if (nf<1.0) nf=1.0;
        extstr = nf*density;
     }

     external_strength = extstr;
     //System.out.println(this+" external strength:"+external_strength);

  }

  public Vector get_incompatible_bonds(){
    Vector bnds = new Vector();
    for (int i=0; i<string.bonds.size(); i++){
      bond b=(bond) string.bonds.elementAt(i);
      if ((b.left_obj==left_obj)||(b.right_obj==right_obj))
        bnds.addElement(b);
    }
    return bnds;
  }
}
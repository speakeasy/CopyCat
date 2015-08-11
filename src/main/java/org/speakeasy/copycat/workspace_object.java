package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class workspace_object extends workspace_structure {
   workspace_string string; // the string the object is in
   int left_string_position;
   boolean leftmost;        // true if the object is the leftmost in the string
   int right_string_position;
   boolean rightmost;       // true if the object is the rightmost in the string
   double raw_importance;
   double relative_importance;
   double intra_string_happiness;
   double intra_string_unhappiness;
   double inter_string_happiness;
   double inter_string_unhappiness;
   double total_happiness;
   double total_unhappiness;
   double intra_string_salience;
   double inter_string_salience;
   double total_salience;
   Vector descriptions;
   Vector extrinsic_descriptions;
   Vector bond_descriptions = new Vector();  // if it is a group 
           // these are the types of bonds holding it together
   Vector outgoing_bonds;
   Vector incoming_bonds;
   bond left_bond;
   bond right_bond;
   Vector bonds;  // used in calculating intra string happiness
                  // = number of bonds attached to the object
   group group; // the group the object is a part of if it is in a group
   Correspondence correspondence; // if a Correspondence has been made
   Replacement replacement;// if a replacement has been made
   boolean changed;    // true if it is the changed letter
   boolean new_answer_letter;
   boolean clamp_salience;
   boolean spans_string; // true if it spans the whole string
   String pname;
   slipnode flavor_type;  // a string for identifying the type
                        // ie. letter or group

   workspace_object(){}



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


   public int letter_span() { return (right_string_position -
                           left_string_position + 1); }
   public Vector relevant_descriptions(){
     Vector v = new Vector();
     for (int x=0; x<descriptions.size(); x++){
       description d = (description)descriptions.elementAt(x);
       if (d.description_type.activation==100.0) v.addElement(d);
     }
     return v;
   }

   public Vector get_possible_descriptions(slipnode description_type){
      Vector v = new Vector();
      for (int x=0; x<description_type.instance_links.size(); x++){
         slipnet_link sl = (slipnet_link)description_type.instance_links.elementAt(x);
         slipnode sn = sl.to_node;
         if ((sn==slipnet.first)&&(has_description(slipnet.slipnet_letters[0])))
            v.addElement(sn);
         if ((sn==slipnet.last)&&(has_description(slipnet.slipnet_letters[25])))
            v.addElement(sn);
         for (int y=0; y<=4; y++){
            if ((sn==slipnet.slipnet_numbers[y])&&(this instanceof group)&&
                (((group)this).object_list.size()==y+1))
            v.addElement(sn);
         }
         if ((sn==slipnet.middle)&&(this.middle_object()))
             v.addElement(sn);
      }
      return v;
   }

   public boolean has_description(slipnode ds){
     for (int x=0; x<descriptions.size(); x++){
       description d = (description)descriptions.elementAt(x);
       if (d.descriptor==ds) return true;
     }
     return false;
   }

   public Vector relevant_distinguishing_descriptors(){
     Vector v= new Vector();
     Vector rel = relevant_descriptions();
     for (int x=0; x<rel.size(); x++){
       description d = (description)rel.elementAt(x);
       if (distinguishing_descriptor(d.descriptor))
            v.addElement(d.descriptor);
     }
     return v;
   }

   public boolean distinguishing_descriptor(slipnode descriptor){
      // returns true if no other object of the same type (ie. letter or group)
      // has the same descriptor

      if (descriptor==slipnet.letter) return false;
      if (descriptor==slipnet.group) return false;
      for (int x=0; x<slipnet.slipnet_numbers.length; x++)
        if (slipnet.slipnet_numbers[x]==descriptor) return false;
      
      Vector obs = string.objects;
      for (int x=0; x<obs.size(); x++){
        workspace_object wo = (workspace_object)obs.elementAt(x);
        if (wo!=this){
          // check to see if they are of the same type
          if (((this instanceof letter)&&(wo instanceof letter))||
               ((this instanceof group)&&(wo instanceof group))){
              // check all descriptions for the descriptor
              for (int y=0; y<wo.descriptions.size(); y++){
                 description d = (description)wo.descriptions.elementAt(y);
                 if (d.descriptor==descriptor) return false;
              }
          }
        }
      } 

      return true;
   }

   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     Calculate_Coors();
     if (!Redraw) return;
     if (foreground!=null){
       g.setColor(foreground);
       g.drawRect(sx1,sy1,sx2-sx1,sy2-sy1);
     }
     Redraw=false;
   }

   public String toString(){
     String s;
     if (left_string_position==right_string_position) 
        s="letter ("+left_string_position+")";
     else s="group ("+left_string_position+"-"+right_string_position+")";
     return s;
   }

   public void build_descriptions(){
     for (int x=0; x<descriptions.size(); x++){
       description d = (description)descriptions.elementAt(x);
       d.description_type.buffer=100.0;
       d.descriptor.buffer=100.0;

       if (!workspace.workspace_structures.contains(d)){
         workspace.WorkspaceArea.AddObject(d);
         workspace.workspace_structures.addElement(d);
       }

     }
     workspace.check_visibility();
   }

   public void add_description(slipnode dt, slipnode d){
      description ds = new description(this, string, dt, d);
      descriptions.addElement(ds);
   }

   public void add_descriptions(Vector v){
     if (v==null) return;
     for (int x=0; x<v.size(); x++){
       description d = (description)v.elementAt(x);
       if (!has_description(d)) add_description(
              d.description_type,d.descriptor);
     }
     build_descriptions();
   }

   public boolean has_description(description d){
      for (int x=0; x<descriptions.size(); x++){
         description d2 = (description) descriptions.elementAt(x);
         if ((d.description_type==d2.description_type)&&
             (d.descriptor==d2.descriptor)) return true;
      }
      return false;
   }

   public boolean recursive_group_member(workspace_object group){
      if ((left_string_position>=group.left_string_position)&&
          (right_string_position<=group.right_string_position))
          return true;
      return false;
   }

   public void update_object_value(){
      // calculate the raw importance of the object
      // = sum of all relevant descriptions
      double sum = 0.0;
      for (int i=0; i<descriptions.size(); i++){
         description d = (description) descriptions.elementAt(i);
         if (d.description_type.activation==100.0) sum+=d.descriptor.activation;
         else sum+=(d.descriptor.activation/20.0); // just in case some are not active
      }
      if (group!=null) sum*=2.0/3.0;
      if (changed) sum*=2.0;
      raw_importance = sum;

      // calculate the intra-string-happiness of the object
      double result = 0.0;
      if (spans_string) result = 100.0;
      else {
        if (group!=null) result = group.total_strength;
        else {
          double bondstrength=0.0;
          for (int i=0; i<bonds.size(); i++){
            bond b = (bond)bonds.elementAt(i);
            bondstrength+=b.total_strength;
          }
          if (spans_string)
            bondstrength/=3.0;
          else bondstrength/=6.0;
          result = bondstrength;

        }
      }
      intra_string_happiness = result;


      // calculate intra-string-unhappiness
      intra_string_unhappiness = 100.0-intra_string_happiness;

      // calculate inter-string-happiness
      inter_string_happiness = 0.0;
      if (correspondence!=null) inter_string_happiness = correspondence.total_strength;

      // calculate inter-string-unhappiness
      inter_string_unhappiness = 100.0-inter_string_happiness;

      // calculate total-happienss
      total_happiness = (inter_string_happiness+intra_string_happiness)/2.0;

      // calculate total-unhappiness
      total_unhappiness = 100.0-total_happiness;

      // calculate intra_string_salience
      if (clamp_salience) intra_string_salience = 100.0;
      else intra_string_salience = formulas.weighted_average(
          relative_importance,0.2,intra_string_unhappiness,0.8);

      // calculate inter_string_salience
      if (clamp_salience) inter_string_salience = 100.0;
      else inter_string_salience = formulas.weighted_average(
          relative_importance,0.8,inter_string_unhappiness,0.2);

      // calculate total salience
      total_salience = (intra_string_salience+inter_string_salience)/2;
      //System.out.println(this+" salience:"+total_salience+" raw importance:"+raw_importance+" relative importance:"+relative_importance+" intra string salience:"+intra_string_salience+" inter string salience:"+inter_string_salience+" intra su:"+intra_st

//ring_unhappiness+" inter su:"+inter_string_unhappiness+"
//total_unhappiness:"+total_unhappiness );

   }

   public int letter_distance(workspace_object ob2){
      if (ob2.left_string_position>right_string_position)
         return ob2.left_string_position-right_string_position;
      if (left_string_position>ob2.right_string_position)
         return left_string_position-ob2.right_string_position;
      return 0;  
   }

   public slipnode get_description(slipnode description_type){
      // returns the description attached to this object of the specified description type
      for (int i=0; i<descriptions.size(); i++){
        description d = (description)descriptions.elementAt(i);
        if (d.description_type==description_type) return d.descriptor;
      }
      return null;
   }

   public slipnode get_description_type(slipnode description){
      // returns the description_type attached to this object of the specified description
      for (int i=0; i<descriptions.size(); i++){
        description d = (description)descriptions.elementAt(i);
        if (d.descriptor==description) return d.description_type;
      }
      return null;
   }
}
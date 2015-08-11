package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class workspace_string {
  Vector letter_vector;  // a vector containing the letter objects
  int length; // the number of letters in the string
  double intra_string_unhappiness; 
  Vector bonds;
  Vector objects;

  workspace_string (String s, int x1, int y1, int x2, int y2){
    bonds = new Vector();
    objects = new Vector();
    letter_vector = new Vector();
    // for the given string, define the individual letters as objects
    int len = s.length(); if (len==0) return;
    length=len;
    double ratio = 100.0;  // every letter is 100 long unless >(x2-x1)/len
    if (((x2-x1)/len)<100){
       ratio = ((double)(x2-x1))/((double) len);
    }
    int st = ((x1+x2)/2) - (int)(ratio*((double)len)/2.0);
    int xf,xt;
    for (int x=0; x<len; x++){
      xf=st+x*(int)ratio; xt=xf+(int)ratio;
      String s2 = s.substring(x,x+1);
      Caption cp = new Caption(xf,y1,xt,y2,s2);
      cp.Filled = false;
      workspace.WorkspaceArea.AddObject(cp);
      workspace.WorkspaceSmall.AddObject(cp);

      // create workspace objects corresponding to the letter, and
      // add the descriptors eg. latter category, and position

      workspace_object wo = new letter(this,x+1,x+1,xf,y1,xt,y2);
      workspace.WorkspaceArea.AddObject(wo);
      workspace.WorkspaceSmall.AddObject(wo);
      wo.add_description(slipnet.object_category,slipnet.letter);
      letter_vector.addElement(wo);

      char[] charequiv = s2.toCharArray();
      int letval = charequiv[0];
      if (letval > 96) letval-=97; else letval-=65;
      wo.add_description(slipnet.letter_category,slipnet.slipnet_letters[letval]);

      if ((x==0)&&(len==1)) wo.add_description(slipnet.string_position_category,slipnet.single);
      if ((x==0)&&(len>1)) wo.add_description(slipnet.string_position_category,slipnet.leftmost);
      if ((x==(len-1))&&(len>1)) wo.add_description(slipnet.string_position_category,slipnet.rightmost);
      if ((len>2)&&((x*2)==(len-1))) wo.add_description(slipnet.string_position_category,slipnet.middle); 
      wo.build_descriptions();
    }
  }

  public void update_relative_importance(){
    // updates the normalized importances of all the objects in the string
    double total_raw_importance = 0;
    for (int i=0; i<objects.size(); i++){
       workspace_object ob = (workspace_object) objects.elementAt(i);
       //System.out.println("raw importance = " + ob.raw_importance);
       total_raw_importance+=ob.raw_importance;
    }
    for (int i=0; i<objects.size(); i++){
       workspace_object ob = (workspace_object) objects.elementAt(i);
       if (total_raw_importance==0.0) ob.relative_importance=0.0;
       else ob.relative_importance=ob.raw_importance/total_raw_importance;
    }
  }

  public group group_present(group wg){
    // searches for the group in the string
    // if an equivalent group exists, return this group
    // otherwise return null;
    for (int x=0; x<objects.size(); x++){
      workspace_object wo = (workspace_object)objects.elementAt(x);
      if (wo instanceof group){
        group gp=(group)wo;
        if ((gp.left_string_position==wg.left_string_position)&&
            (gp.right_string_position==wg.right_string_position)&&
            (gp.bond_category==wg.bond_category)&&
            (gp.direction_category==wg.direction_category))
             return gp;
      }
    }
    return null;
  }

  public void update_intra_string_unhappiness(){
     //returns the average of the the intra-string unhapinesses of all
     // the objects in the string
     double isu = 0.0;
    for (int i=0; i<objects.size(); i++){
       workspace_object ob = (workspace_object) objects.elementAt(i);
       isu+=ob.intra_string_unhappiness;
    }
    double len = (double)(objects.size());
    if (len>0) isu/=len;
    intra_string_unhappiness = isu;

  }
}
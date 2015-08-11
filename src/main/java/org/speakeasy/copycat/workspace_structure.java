package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class workspace_structure extends GraphicsObject {
  workspace_string string;
  double internal_strength, external_strength, total_strength;
  public void update_strength_value(){
    calculate_internal_strength();
    calculate_external_strength();
    calculate_total_strength();
    };
  public void calculate_internal_strength(){}
  public void calculate_external_strength(){}
  public void calculate_total_strength(){
    total_strength = formulas.weighted_average(internal_strength,
        internal_strength, external_strength, (100.0-internal_strength));
    //System.out.println(this+" total strength:"+total_strength);
  }
  public double total_weakness(){
    return 100.0-Math.pow(total_strength,0.95);
  }

   public String toString(){
     String s;
     s="ws ";
     return s;
   }

}
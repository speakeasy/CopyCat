package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class concept_mapping{
  workspace_object obj1, obj2;
  slipnode label = null;  // if the concept_mapping has a linking concept
  slipnode description_type1;
  slipnode description_type2;
  slipnode descriptor1, descriptor2;
  public String toString(){
    return descriptor1.pname+" -> "+descriptor2.pname;
  }
  concept_mapping(slipnode dt1, slipnode dt2, slipnode d1, slipnode d2,
         workspace_object o1, workspace_object o2){
     description_type1 = dt1;
     description_type2 = dt2;
     descriptor1 = d1;
     descriptor2 = d2;
     obj1 = o1; obj2 = o2;
     label = slipnet_formulas.get_bond_category(d1,d2);
  }         

  public boolean in_vector(Vector v){
    // returns true in the concept mapping is in the vector
    for (int x=0; x<v.size(); x++){
      concept_mapping c = (concept_mapping)v.elementAt(x);
      if ((c.description_type1==description_type1)&&
          (c.description_type2==description_type2)&&
          (c.descriptor1==descriptor1)) return true;
    }
    return false;
  }

  public static boolean all_opposite_mappings(Vector v){
    // returns true if all mappings are opposite
    for (int x=0; x<v.size(); x++)
      if (((concept_mapping)v.elementAt(x)).label!=slipnet.opposite)
      return false;
    return true;
  }
  public double slipability(){
    double d_o_a = this.degree_of_association();
    if (d_o_a==100.0) return 100.0;
    double v = (this.conceptual_depth()/100.0);
    return (d_o_a*(1-(v*v)));
  }

  public double strength(){
    if (degree_of_association()==100.0) return 100.0;
    double cd=(conceptual_depth()/100.0);
    return (degree_of_association()*(1+(cd*cd)));
  }

  public boolean slippage(){
    //if (label==null) return false;
    if (label==slipnet.sameness) return false;
    if (label==slipnet.identity) return false;
    return true;
  }
  public boolean relevant(){
    return (((description_type1.activation)==100.0)&&
             ((description_type2.activation)==100.0));
  }
  public double conceptual_depth(){
    return (descriptor1.conceptual_depth+descriptor2.conceptual_depth)/2.0;
  }

  public double degree_of_association(){
    // assumes the 2 descriptors are connected in the
    // slipnet by at most 1 link
    if (descriptor1==descriptor2) return 100.0;
    for (int x=0; x<descriptor1.lateral_slip_links.size(); x++){
      slipnet_link l = (slipnet_link)descriptor1.lateral_slip_links.elementAt(x);
      if (l.to_node==descriptor2) return l.degree_of_association();
   
    }
    return 0.0;
  }

  public boolean distinguishing(){
    if ((descriptor1==slipnet.whole)&&(descriptor2==slipnet.whole))
       return false;
    return ((obj1.distinguishing_descriptor(descriptor1))&&
            (obj2.distinguishing_descriptor(descriptor2)));
  }
  public concept_mapping symmetric_version(){
    if ((label==slipnet.identity)||(label==slipnet.sameness))
       return this;
    if (!(slipnet_formulas.get_bond_category(descriptor2,descriptor1)==label))
       return this;  
    return new concept_mapping(description_type2,description_type1,
            descriptor2,descriptor1,obj1,obj2);
  }
  public static Vector get_concept_mapping_list(workspace_object w1,
      workspace_object w2, Vector ds1, Vector ds2){
     Vector rtv = new Vector();
     for (int x=0; x<ds1.size(); x++){
        description d1 = (description)ds1.elementAt(x);
        for (int y=0; y<ds2.size(); y++){
           description d2 = (description)ds2.elementAt(y);
           if (((d1.description_type)==(d2.description_type))&&(              ((d1.descriptor)==(d2.descriptor))||
              slipnet_formulas.slip_linked(d1.descriptor,d2.descriptor)))
           rtv.addElement(new concept_mapping(d1.description_type,
                   d2.description_type,d1.descriptor, d2.descriptor,
                   w1, w2));
        }
     }
     return rtv;
  }
}
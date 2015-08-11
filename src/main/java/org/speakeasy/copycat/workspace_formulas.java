package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;

class workspace_formulas {
  static boolean clamp_temperature = false;  // external clamp
  static double very_low_distribution[] = {5.0,150.0,5.0,2.0,1.0,1.0,1.0,1.0,1.0,1.0};
  static double low_distribution[] = {2.0,5.0,150.0,5.0,2.0,1.0,1.0,1.0,1.0,1.0};
  static double medium_distribution[] = {1.0,2.0,5.0,150.0,5.0,2.0,1.0,1.0,1.0,1.0};
  static double high_distribution[] = {1.0,1.0,2.0,5.0,150.0,5.0,2.0,1.0,1.0,1.0};
  static double very_high_distribution[] = {1.0,1.0,1.0,2.0,5.0,150.0,5.0,2.0,1.0,1.0};
  
  static int choose(double vals[]){
    double tot = 0.0;
    for (int x=0; x<vals.length; x++) tot+=vals[x];
    double pos = tot*random.rnd();
    tot=0.0;
    for (int x=0; x<vals.length; x++) {
       tot+=vals[x];
       if (tot>=pos) return (x+1);
    }
    return vals.length;
  }

  static void update_temperature(){
     workspace.calculate_intra_string_unhappiness();
     workspace.calculate_inter_string_unhappiness();
     workspace.calculate_total_unhappiness();
     
     double rule_weakness=100.0;
     if (workspace.rule!=null){
         (workspace.rule).update_strength_value();
         rule_weakness=100.0-(workspace.rule).total_strength;
     }

     formulas.actual_temperature = formulas.weighted_average(workspace.total_unhappiness,0.8, rule_weakness,0.2);
     if (Temperature.clamped) formulas.actual_temperature = 100.0;
     Temperature.Update(formulas.actual_temperature);
     if (!clamp_temperature) formulas.temperature =formulas.actual_temperature;
     Temperature.Update(formulas.temperature);
     workspace.tvh.Values.addElement(new Double(100.0-workspace.total_unhappiness));
     workspace.temp.Values.addElement(new Double(formulas.actual_temperature));
  }

  static double temperature_adjusted_probability(double val){
    return formulas.temperature_adjusted_probability(val);    
  }

  static boolean flip_coin(double val){
    if (random.rnd()<val) return true;
    return false;

  }

  static Vector similar_has_property_links(slipnode s){
    Vector v = new Vector();
    for (int x=0; x<s.has_property_links.size(); x++){
      slipnet_link sl = (slipnet_link)s.has_property_links.elementAt(x);
      if (flip_coin(temperature_adjusted_probability(
            sl.degree_of_association()/100.0)))
        v.addElement(sl);
    }
    return v;
  }

  static double temperature_adjusted_value(double val){
    return Math.pow(val,((100.0-formulas.temperature)/30.0)+0.5);    
  }

  static int select_list_position(Vector list){
     double totalval=0.0;
     for (int i=0; i<list.size(); i++){
        totalval+=((MDouble)list.elementAt(i)).doubleValue();
     }

     double pos = totalval*random.rnd();
     int listpos = -1;
     totalval=0.0;
     for (int i=0; i<list.size(); i++){
        totalval+=((MDouble)list.elementAt(i)).doubleValue();
        if ((listpos==-1)&&(totalval>=pos))
          listpos=i;
     }
     if (listpos==-1) return 0;
     return listpos;
  }

  static workspace_object choose_object_from_list(Vector list, String variable){
     // chooses an object from the the list by a variable
     // eg "intra-string-salience" probabilistically adjusted for temperature
     if (list.size()==0) return null;  

     Vector object_probs = new Vector();
     for (int i=0; i<list.size(); i++){
        workspace_object ob = (workspace_object)list.elementAt(i);
        double val = 1.0;
        if (variable.equals("intra_string_salience"))
           val=temperature_adjusted_value(ob.intra_string_salience);
        if (variable.equals("inter_string_salience"))
           val=temperature_adjusted_value(ob.inter_string_salience);
        if (variable.equals("total_salience"))
           val=temperature_adjusted_value(ob.total_salience);
        if (variable.equals("relative_importance"))
           val=temperature_adjusted_value(ob.relative_importance);
        object_probs.addElement(new MDouble(val));
     }
     return (workspace_object)list.elementAt(select_list_position(object_probs));
  }

  static description choose_relevant_description_by_activation(workspace_object wo){
     Vector v = wo.relevant_descriptions();
     if ((v==null)||(v.size()==0)) return null;
     Vector vals = new Vector();
     for (int x=0; x<v.size(); x++) {
       description d = (description)v.elementAt(x);
       double val = (d.descriptor).activation;
       vals.addElement(new MDouble(val));
       }
     return (description)v.elementAt(select_list_position(vals));
  }


  static workspace_object choose_object(String variable,Vector oblist){
     // chooses an object from oblist by a variable
     // eg "intra-string-salience" probabilistically adjusted for temperature
     Vector objects = new Vector();

     for (int i=0; i<oblist.size(); i++){
        workspace_object ob = (workspace_object)oblist.elementAt(i);
        if (ob.string!=workspace.modified) objects.addElement(ob);
     }

     return choose_object_from_list(objects,variable);
  }

  static slipnode choose_slipnode_by_conceptual_depth(Vector slist){
    if (slist.size()==0) return null;

     Vector object_probs = new Vector();
     for (int i=0; i<slist.size(); i++){
        slipnode s = (slipnode)slist.elementAt(i);
        double val = temperature_adjusted_value(s.conceptual_depth);
        object_probs.addElement(new MDouble(val));
     }
     return (slipnode)slist.elementAt(select_list_position(object_probs));
    
  }

  static workspace_object choose_neighbor(workspace_object fromob){
     Vector objects = new Vector();

     for (int i=0; i<workspace.workspace_objects.size(); i++){
        workspace_object ob = (workspace_object)workspace.workspace_objects.elementAt(i);
        if (ob.string==fromob.string){
          if ((ob.left_string_position==(fromob.right_string_position+1))||
            (fromob.left_string_position==(ob.right_string_position+1)))
            objects.addElement(ob);
        }
     }
     return choose_object_from_list(objects,"intra_string_salience");
  }

  static workspace_object choose_left_neighbor(workspace_object fromob){
     Vector objects = new Vector();

     for (int i=0; i<workspace.workspace_objects.size(); i++){
        workspace_object ob = (workspace_object)workspace.workspace_objects.elementAt(i);
        if (ob.string==fromob.string){
          if (fromob.left_string_position==(ob.right_string_position+1))
            objects.addElement(ob);
        }
     }
     return choose_object_from_list(objects,"intra_string_salience");
  }

  static workspace_object choose_right_neighbor(workspace_object fromob){
     Vector objects = new Vector();

     for (int i=0; i<workspace.workspace_objects.size(); i++){
        workspace_object ob = (workspace_object)workspace.workspace_objects.elementAt(i);
        if (ob.string==fromob.string){
          if (ob.left_string_position==(fromob.right_string_position+1))
            objects.addElement(ob);
        }
     }
     return choose_object_from_list(objects,"intra_string_salience");
  }


  static slipnode choose_bond_facet(workspace_object fromob,workspace_object toob){
     Vector fromob_facets = new Vector();
     Vector bond_facets = new Vector();

     for (int i=0; i<fromob.descriptions.size(); i++){
       description d = (description)fromob.descriptions.elementAt(i);
       slipnode dt = d.description_type;
       if (slipnet.bond_facets.contains(dt))  fromob_facets.addElement(dt);
     }

     for (int i=0; i<toob.descriptions.size(); i++){
       description d = (description)toob.descriptions.elementAt(i);
       slipnode dt = d.description_type;
       if (fromob_facets.contains(dt))  bond_facets.addElement(dt);
     }

     if (bond_facets.size()==0) return null;


     Vector object_probs = new Vector();
     for (int i=0; i<bond_facets.size(); i++){
        slipnode ob = (slipnode)bond_facets.elementAt(i);
        double val = total_description_type_support(ob,fromob.string);
        object_probs.addElement(new MDouble(val));
     }
     return (slipnode)bond_facets.elementAt(select_list_position(object_probs));
  }

  static slipnode get_descriptor(workspace_object wo, slipnode dt){
     for (int i=0; i<wo.descriptions.size(); i++){
        description d = (description) wo.descriptions.elementAt(i);
        if (d.description_type==dt) return d.descriptor;
     }
     return null;
  }

  static double local_description_type_support(slipnode description_type, workspace_string s){
    // returns the proportion of objects in the string that have
    // a description with this description_type
    double number_of_objects = 0.0, total_number_of_objects = 0.0;
    for (int i=0; i<workspace.workspace_objects.size(); i++){
      workspace_object ob = (workspace_object)workspace.workspace_objects.elementAt(i);
      if (ob.string==s){
         total_number_of_objects+=1.0;
         for (int x=0; x<ob.descriptions.size(); x++){
           description d = (description)ob.descriptions.elementAt(x);
           if (d.description_type == description_type) number_of_objects+=1.0;
         }
      }
    }
    return number_of_objects/total_number_of_objects;
  }

  static double total_description_type_support(slipnode description, workspace_string s){
     return (description.activation+local_description_type_support(description,s))/2.0;
  }

  static boolean structure_vs_structure(workspace_structure s1, double w1,
                                        workspace_structure s2, double w2){
     s1.update_strength_value();
     s2.update_strength_value();
     double v1=s1.total_strength*w1;
     double v2=s2.total_strength*w2;
     v1=temperature_adjusted_value(v1);
     v2=temperature_adjusted_value(v2);
     if (((v1+v2)*random.rnd())>v1) return false;
     return true;

  }
 
  static boolean fight_it_out(workspace_structure wo,double v1, Vector structs, double v2){
     if (structs.size()==0) return true;
     for (int i=0; i<structs.size(); i++){
        workspace_structure ws = (workspace_structure)structs.elementAt(i);
        if (!structure_vs_structure(wo,v1,ws,v2)) return false;
     } 

     return true;
  }


  static double local_bond_category_relevance(workspace_string string, slipnode cat){
    // is a function of how many bonds in the string have this bond category
    double oll=0.0, bc=0.0;
    if (string.objects.size()==1) return 0.0;
    for (int i=0; i<string.objects.size(); i++){
      workspace_object wo = (workspace_object) string.objects.elementAt(i);
      if (!wo.spans_string){
        oll+=1.0;
        if (wo.right_bond!=null)
          if (wo.right_bond.bond_category==cat) bc+=1.0;
      }
    }    
   return 100.0*bc/(oll-1.0);
  }

  static double local_direction_category_relevance(workspace_string string, slipnode dir){
    // is a function of how many bonds in the string have this bond category
    double oll=0.0, bc=0.0;
    for (int i=0; i<string.objects.size(); i++){
      workspace_object wo = (workspace_object) string.objects.elementAt(i);
      if (!wo.spans_string){
        oll+=1.0;
        if (wo.right_bond!=null)
          if (wo.right_bond.direction_category==dir) bc+=1.0;
      }
    }    
   return 100.0*bc/(oll-1.0);
  }


  static Vector get_common_groups(workspace_object from_obj,workspace_object to_obj){
    workspace_string st = from_obj.string;
    Vector v = new Vector();

    for (int i=0; i<st.objects.size(); i++){
      workspace_object wo = (workspace_object)st.objects.elementAt(i);
      if ((from_obj.left_string_position>=wo.left_string_position)&&
          (from_obj.right_string_position<=wo.right_string_position)&&
          (to_obj.left_string_position>=wo.left_string_position)&&
          (to_obj.right_string_position<=wo.right_string_position))
         v.addElement(wo);
    }
    return v;
  }

  static Vector get_incompatible_groups(group obj){
    Vector v = new Vector();

    for (int i=0; i<obj.object_list.size(); i++){
      workspace_object wo = (workspace_object)obj.object_list.elementAt(i);
      while (wo.group!=null){
         v.addElement(wo.group);
         wo=wo.group;
      }
    }
    return v;
  }


  static boolean same_group(group gp1, group gp2){
    if (gp1.left_string_position!=gp2.left_string_position) return false;
    if (gp1.right_string_position!=gp2.right_string_position) return false;
    if (gp1.group_category!=gp2.group_category) return false;
    if (gp1.direction_category!=gp2.direction_category) return false;
    if (gp1.bond_facet!=gp2.bond_facet) return false;
    return true;

  }

  static boolean group_present(group proposed){
    // returns true if a group matching this description already exists
    workspace_string st = proposed.string;
    for (int i=0; i<st.objects.size(); i++){
      workspace_object wo = (workspace_object)st.objects.elementAt(i);
     if (wo instanceof group)
        if (same_group(proposed,(group)wo)) return true;
    }
    return false;
  }

  static workspace_object equivalent_group(group proposed){
    // returns true if a group matching this description already exists
    workspace_string st = proposed.string;
    for (int i=0; i<st.objects.size(); i++){
      workspace_object wo = (workspace_object)st.objects.elementAt(i);
     if (wo instanceof group)
        if (same_group(proposed,(group)wo)) return wo;
    }
    return null;
  }


  static Vector unrelated_objects(){
    // returns a list of all objects in the workspace that have at least
    // one bond slot open
    Vector uo=new Vector();
    for (int i=0; i<workspace.workspace_objects.size();i++){
      workspace_object wo=(workspace_object)workspace.workspace_objects.elementAt(i);
      boolean ok =((wo.string==workspace.initial)||(wo.string==workspace.target));
      boolean left = ((wo.left_bond==null)&&(!wo.leftmost));
      boolean right = ((wo.right_bond==null)&&(!wo.rightmost));
      if ((ok)&&(!wo.spans_string)){
        if ((right)||(left)) uo.addElement(wo);;
      }
    }
    return uo;
  }

  static Vector ungrouped_objects(){
    // returns a list of all objects in the workspace that are not
    // in a group

    Vector uo=new Vector();
    for (int i=0; i<workspace.workspace_objects.size();i++){
      workspace_object wo=(workspace_object)workspace.workspace_objects.elementAt(i);
      boolean ok =((wo.string==workspace.initial)||(wo.string==workspace.target));
      if ((ok)&&(!wo.spans_string)){
        if (wo.group==null) uo.addElement(wo);;
      }
    }
    return uo;
  }

  static Vector unreplaced_objects(){
    // returns a list of all objects in the initial string that are not
    // replaced

    Vector uo=new Vector();
    for (int i=0; i<workspace.workspace_objects.size();i++){
      workspace_object wo=(workspace_object)workspace.workspace_objects.elementAt(i);
      boolean ok =((wo.string==workspace.initial)&&(wo instanceof letter));
      if ((ok)&&(wo.replacement==null)){
        uo.addElement(wo);;
      }
    }
    return uo;
  }

  static Vector uncorresponding_objects(){
    // returns a list of all objects in the initial string that are not
    // replaced

    Vector uo=new Vector();
    for (int i=0; i<workspace.workspace_objects.size();i++){
      workspace_object wo=(workspace_object)workspace.workspace_objects.elementAt(i);
      boolean ok =((wo.string==workspace.initial)||(wo.string==workspace.target));
      if ((ok)&&(wo.correspondence==null)){
        uo.addElement(wo);;
      }
    }
    return uo;
  }

  static Vector possible_group_bond_list(slipnode bond_cat,
          slipnode direction, slipnode bond_facet, Vector bond_list){
    Vector new_list = new Vector();
      for (int x=0; x<bond_list.size(); x++){
        bond b = (bond)bond_list.elementAt(x);
        if ((b.bond_category==bond_cat)&&
             (b.direction_category==direction)) new_list.addElement(b);
        else {
          // a modified bond might be made
          if (bond_cat==slipnet.sameness) return null; // a different bond 
                          // cannot be made here
          if ((b.bond_category==bond_cat)||
              (b.direction_category==direction)) return null; // a different bond
                          // cannot be made here
          if (b.bond_category==slipnet.sameness) return null;
          b = new bond(b.to_obj,b.from_obj,bond_cat,bond_facet,
                       b.to_obj_descriptor, b.from_obj_descriptor);
          new_list.addElement(b);
        }
      }

    return new_list;
  }
}





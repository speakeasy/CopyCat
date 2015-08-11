package org.speakeasy.copycat;

import org.speakeasy.copycat.Replacement;
import java.util.*;
import java.applet.*;
import java.awt.*;

class codelet {
   static Vector initial_codelets = new Vector();
   String name;
   Vector arguments;
   int time_stamp;
   int urgency;
   public static int Urgency_Sum = 0;  // the sum of the urgencies of the last block
   public static int Urgency_Count = 0;
   int position; // position in the coderack, if it differs, redraw
   int removal_probability;
   static int print_pos = 0;
   Coderack_Pressure Pressure_Type= null;

   codelet(String tp, int u){
     name = tp;
     position = -1;
     time_stamp=coderack.codelets_run;
     arguments = new Vector();
     urgency = u;
   }

   public void reset_print() {
      print_pos = 0;
      for (int x=0; x<9; x++) 
        coderack.CodeletInfo_Captions[x].Change_Caption(" ");
   }

   public void print(String s){
      if (print_pos<9) {
        coderack.CodeletInfo_Captions[print_pos].Change_Caption(s);
      }
      print_pos++;
      coderack.CoderackInfoArea.Redraw=true;
   }

   public boolean run(){
      reset_print();
      Urgency_Count++;
      Urgency_Sum+=urgency;
      if (Urgency_Count==15){
        Urgency_Count = 0;
        double d = ((double)Urgency_Sum)*100.0/(15.0*7.0);
        coderack.Codelet_Run.Values.addElement(new Double(d));
        Urgency_Sum=0;
      }
      coderack.codelets_run++;
      workspace.codelets_run.Change_Caption("Codelets Run = "+(coderack.codelets_run));
      workspace.Workspace_Comments.Change_Caption(name);
      print("#### "+name+" ####");
      coderack.Codelet_Run.Change_Caption(name+":"+urgency);
//**********************************************************************
//                          Breaker codelet
//*******************************************************************
     if (name.equals("breaker")){
           //decide whether or not to fizzle based on temperature
        double prob = (100.0-formulas.temperature)/100.0;
        print("deciding whether or not to fizzle.");
        print("fizzle probability = "+prob);
        if (workspace_formulas.flip_coin(prob)){
             //fizzle
             print("decided to fizzle!");
             return false;
        }
        // choose a structure at random
        print("did not fizzle.");
        Vector structures = new Vector();
        for (int x=0; x<workspace.workspace_structures.size(); x++){
           workspace_structure ws = (workspace_structure)workspace.workspace_structures.elementAt(x);
           if (ws instanceof group) structures.addElement(ws);
           if (ws instanceof bond) structures.addElement(ws);
           if (ws instanceof Correspondence) structures.addElement(ws);
        }

        int wsize = structures.size();
        if (wsize==0) {
           print("There are no structures built: fizzle");
           return false;
        }

        int pos = (int)(random.rnd()*((double)wsize));
        if (pos>=wsize) pos = 0;
        workspace_structure ws = (workspace_structure)structures.elementAt(pos);
        String st = "";
        if (ws instanceof workspace_object){
           st=" from target string";
           if (((workspace_object)ws).string==workspace.initial)
              st=" from initial string";
        }
         
        print("object chosen = "+ws+st);
        print("break probability = "+(1.0-workspace_formulas.temperature_adjusted_probability(ws.total_strength/100.0)));
        Vector break_objects = new Vector(); break_objects.addElement(ws);
        if (ws instanceof bond){
           bond b = (bond)ws;
           workspace_object ob = b.from_obj;
           group g = ob.group;
           if (g==((workspace_object)b.to_obj).group)
              if (g!=null) break_objects.addElement(g);
        } 
        // try to break all objects
        for (int x=0; x<break_objects.size(); x++){
           workspace_structure w = (workspace_structure)break_objects.elementAt(x);
           double p = workspace_formulas.temperature_adjusted_probability(w.total_strength/100.0);
           if (workspace_formulas.flip_coin(p)){
              print("couldn't break structure: Fizzle!");
              return false;
           }
        }
        // break all objects
        for (int x=0; x<break_objects.size(); x++){
           workspace_structure w = (workspace_structure)break_objects.elementAt(x);
           if (w instanceof bond) ((bond)w).break_bond();
           if (w instanceof group) ((group)w).break_group();
           if (w instanceof Correspondence) ((Correspondence)w).break_correspondence();
        }
        print("Succeeded");
     }

//*******************************************************************
//                             DESCRIPTION CODELETS
//*******************************************************************

     else if (name.equals("bottom-up-description-scout")){

         workspace_object chosen_object = workspace_formulas.choose_object("total_salience",workspace.workspace_objects);
         String st=" from target string";
         if (chosen_object.string==workspace.initial)
              st=" from initial string";
         print("chosen object: "+chosen_object+st);
         description d = workspace_formulas.choose_relevant_description_by_activation(chosen_object);
         if (d==null){
             // no relevant descriptions to choose from
             print("no relevant descriptions: Fizzle");
             return false;
         }
         slipnode chosen_descriptor = d.descriptor;
         print("chosen descriptor = "+chosen_descriptor.pname);
         Vector hpl = workspace_formulas.similar_has_property_links(chosen_descriptor);
         if ((hpl==null)||(hpl.size()==0)){
            // no has property links
            print("has no property links: Fizzle!");
            return false;
         }
         Vector v=new Vector();
         for (int x=0; x<hpl.size(); x++){
           slipnet_link sl=(slipnet_link)hpl.elementAt(x);
           v.addElement(new MDouble(sl.degree_of_association()*
                     sl.to_node.activation));
         }
         slipnet_link chosen = (slipnet_link)hpl.elementAt(
                    workspace_formulas.select_list_position(v));
         slipnode chosen_property = chosen.to_node;
         coderack.propose_description(chosen_object,chosen_property.category(),
                         chosen_property,this);
         print("proposing description "+chosen_property.pname);
         return true;
     }



     else if (name.equals("top-down-description-scout")){
        slipnode description_type = (slipnode)arguments.elementAt(0);
         workspace_object chosen_object = workspace_formulas.choose_object("total_salience",workspace.workspace_objects);
         String st=" from target string";
         if (chosen_object.string==workspace.initial)
              st=" from initial string";
         print("chosen object: "+chosen_object+st);
         print("looking for "+description_type.pname+" descriptor");
         Vector v = chosen_object.get_possible_descriptions(description_type);
         if (v.size()==0) {
             print("couldn't find any descriptions");
             return false;
         }

         Vector act=new Vector();
         for (int x=0; x<v.size(); x++){
           slipnode sn=(slipnode)v.elementAt(x);
           act.addElement(new MDouble(sn.activation));
         }

         slipnode chosen_property = (slipnode)v.elementAt(
                workspace_formulas.select_list_position(act));
         coderack.propose_description(chosen_object,chosen_property.category(),
                         chosen_property,this);
         print("proposing description "+chosen_property.pname);
         return true;

      
     }

     else if (name.equals("description-strength-tester")){
         description d = (description)arguments.elementAt(0);
         d.descriptor.buffer=100.0;
         d.update_strength_value();

         double strength = d.total_strength;
         print(d.toString());
         double prob = workspace_formulas.temperature_adjusted_probability(strength/100.0);
         print("description strength = "+strength);
         if (!workspace_formulas.flip_coin(prob)){
            print("not strong enough: Fizzle!");
            return false; 
         }
         // it is strong enough - post builder  & activate nodes
       
          print("succeeded: posting description-builder");
          codelet nc = new codelet("description-builder",coderack.get_urgency_bin(strength));
          nc.arguments = arguments;
          nc.Pressure_Type = this.Pressure_Type;
          if (coderack.remove_terraced_scan) nc.run();
          else coderack.Post(nc);
          return true;
     }

     else if (name.equals("description-builder")){
         description d = (description)arguments.elementAt(0);
         print(d.toString());
         if (!workspace.workspace_objects.contains(d.object)){
            print("object no longer exists: Fizzle!");
            return false;
         }
         if (d.object.has_description(d.descriptor)){
            print("description already exists: Fizzle!");
            d.description_type.buffer=100.0;
            d.descriptor.buffer=100.0;
            return false;
         }         
         d.build_description();
         print("building description");
         return true;
     }
//*******************************************************************************
 //                               BOND CODELETS
//*******************************************************************************

      else if (name.equals("bottom-up-bond-scout")){
         workspace_object fromob = workspace_formulas.choose_object("intra_string_salience",workspace.workspace_objects);
         // choose neighbour
         workspace_object toob = workspace_formulas.choose_neighbor(fromob);

         String st = fromob.toString();
         if (fromob.string==workspace.initial) st+=" in initial string";
         else st+=" in target string";
         print("initial object chosen: "+st);

         if (toob==null) {
         print("object has no neighbour - fizzle");                 
         return false; }
         print("to object: "+toob);                 
         
         slipnode bond_facet = workspace_formulas.choose_bond_facet(fromob,toob);
         if (bond_facet==null) { print(" no possible bond-facet - fizzle");
                                 return false; }
         print("chosen bond facet: "+bond_facet.pname);                 
         slipnode from_descriptor = workspace_formulas.get_descriptor(fromob,bond_facet);
         slipnode to_descriptor = workspace_formulas.get_descriptor(toob,bond_facet);
         if ((from_descriptor==null)||(to_descriptor==null)){
            print(" no possible bond-facet - fizzle");
            return false;
         }
         print("from object descriptor: "+from_descriptor.pname);
         print("to object descriptior: "+to_descriptor.pname);
         slipnode bond_category = slipnet_formulas.get_bond_category(from_descriptor,to_descriptor);
         if (bond_category==null){
            print(" no suitable link - fizzle");
            return false;
         }
         if (bond_category==slipnet.identity) bond_category=slipnet.sameness;
         // there is a possible bond, so propose it
         print("proposing "+bond_category.pname+" bond ");                 
            coderack.propose_bond(fromob,toob,bond_category,bond_facet,from_descriptor, to_descriptor,this);
      }

      if (name.equals("top-down-bond-scout--category")){
         slipnode bond_category = (slipnode) arguments.elementAt(0); 
         print("searching for "+bond_category.pname);                 

         double i_relevance = workspace_formulas.local_bond_category_relevance(
                                 workspace.initial,bond_category);
         double t_relevance = workspace_formulas.local_bond_category_relevance(
                                 workspace.target,bond_category);
         double i_unhappiness = workspace.initial.intra_string_unhappiness;
         double t_unhappiness = workspace.target.intra_string_unhappiness;
         // choose string

         //print("about to choose string:");
         //print("initial string: relevance="+i_relevance+", unhappiness="+i_unhappiness);
         //print("target string: relevance="+t_relevance+", unhappiness="+t_unhappiness);
         

         workspace_string string = workspace.initial;
         if ((random.rnd()*(i_relevance+i_unhappiness+t_relevance+t_unhappiness))>
             (i_relevance+i_unhappiness))  string = workspace.target;
         if (string==workspace.initial) print("initial string selected");
         else  print("target string selected");                 

         workspace_object fromob = workspace_formulas.choose_object("intra_string_salience",string.objects);
         // choose neighbour
         print("initial object: "+fromob);
         workspace_object toob = workspace_formulas.choose_neighbor(fromob);
         if (toob==null) {
                          print("object has no neighbour: Fizzle!");
                          return false; }
         print("to object : "+toob);
         slipnode bond_facet = workspace_formulas.choose_bond_facet(fromob,toob);
         if (bond_facet==null) {
                                 print("no possible bond facet: Fizzle");
                                 return false; }
         print("chosen bond facet :"+bond_facet.pname);
         slipnode from_descriptor = workspace_formulas.get_descriptor(fromob,bond_facet);
         slipnode to_descriptor = workspace_formulas.get_descriptor(toob,bond_facet);
         if ((from_descriptor==null)||(to_descriptor==null)){
            print("both objects do not have this descriptor: Fizzle!");
            return false;
         }

         print("from object descriptor: "+from_descriptor.pname);
         print("to object descriptor: "+to_descriptor.pname);
         slipnode b1 = slipnet_formulas.get_bond_category(from_descriptor,to_descriptor);
         slipnode b2 = slipnet_formulas.get_bond_category(to_descriptor,from_descriptor);
         if (b1==slipnet.identity) {
            b1=slipnet.sameness; b2=slipnet.sameness;
         }

         if ((bond_category!=b1)&&(bond_category!=b2)){
            print("no suitable link: Fizzle!");
            return false;

         }
         // there is a possible bond, so propose it
            print(bond_category.pname+" bond proposed");
         if (bond_category==b1)   coderack.propose_bond(fromob,toob,bond_category,bond_facet,from_descriptor, to_descriptor,this);
         else coderack.propose_bond(toob,fromob,bond_category,bond_facet,to_descriptor,from_descriptor,this);
      }

      if (name.equals("top-down-bond-scout--direction")){
         slipnode direction = (slipnode) arguments.elementAt(0); 

         print("trying to build a "+direction.pname+" bond");
         double i_relevance = workspace_formulas.local_direction_category_relevance(
                                 workspace.initial,direction);
         double t_relevance = workspace_formulas.local_direction_category_relevance(
                                 workspace.target,direction);
         double i_unhappiness = workspace.initial.intra_string_unhappiness;
         double t_unhappiness = workspace.target.intra_string_unhappiness;
         // choose string

         //print("about to choose string:");
         //print("initial string: relevance="+i_relevance+", unhappiness="+i_unhappiness);
         //print("target string: relevance="+t_relevance+", unhappiness="+t_unhappiness);

         workspace_string string = workspace.initial;
         if ((random.rnd()*(i_relevance+i_unhappiness+t_relevance+t_unhappiness))>
             (i_relevance+i_unhappiness)) string = workspace.target;
         if (string==workspace.initial) print("initial string selected");
         else  print("target string selected");                 

         workspace_object fromob = workspace_formulas.choose_object("intra_string_salience",string.objects);
         print("initial object: "+fromob);
         // choose neighbour
         workspace_object toob = null;
         if (direction==slipnet.left) toob=workspace_formulas.choose_left_neighbor(fromob);
         else toob=workspace_formulas.choose_right_neighbor(fromob);
         if (toob==null) {print (fromob+" has no neighbour: Fizzle!");
                          return false; }
         print("to object: "+toob);
         slipnode bond_facet = workspace_formulas.choose_bond_facet(fromob,toob);
         if (bond_facet==null) { print ("no possible bond-facet: Fizzle!");
                                 return false; }
         print("chosen bond facet = "+bond_facet.pname);
         slipnode from_descriptor = workspace_formulas.get_descriptor(fromob,bond_facet);
         slipnode to_descriptor = workspace_formulas.get_descriptor(toob,bond_facet);
         if ((from_descriptor==null)||(to_descriptor==null)){
            print("both objects do not have this descriptor: Fizzle!");
            return false;
         }
         print("from descriptor: "+from_descriptor.pname);
         print("to descriptor: "+to_descriptor.pname);
         slipnode bond_category = slipnet_formulas.get_bond_category(from_descriptor,to_descriptor);
         if (bond_category==slipnet.identity) bond_category = slipnet.sameness;
         if ((bond_category==null)){
            print("no suitable link: Fizzle!");
            return false;
         }
         // there is a possible bond, so propose it
            print(bond_category.pname+" bond proposed");
         coderack.propose_bond(fromob,toob,bond_category,bond_facet,from_descriptor, to_descriptor,this);
      }


      if (name.equals("bond-strength-tester")){
         bond b = (bond)arguments.elementAt(0);
         b.update_strength_value();
         double strength = b.total_strength;
         String st = "bond = "+b;
         if (b.left_obj.string==workspace.initial)
            st+=" in initial string";
            else st+=" in target string";
         print(st);
         double prob = workspace_formulas.temperature_adjusted_probability(strength/100.0);
         print("bond strength = "+strength);
         if (!workspace_formulas.flip_coin(prob)){
            print("not strong enough: Fizzle!");
            return false; 
         }
         // it is strong enough - post builder  & activate nodes
            b.bond_facet.buffer=100.0;
            b.from_obj_descriptor.buffer=100.0;
            b.to_obj_descriptor.buffer=100.0;
          print("succeeded: posting bond-builder");
          codelet nc = new codelet("bond-builder",coderack.get_urgency_bin(strength));
          nc.arguments = arguments;
          nc.Pressure_Type = this.Pressure_Type;

          if (coderack.remove_terraced_scan) nc.run();
          else  { coderack.Post(nc);
          workspace.WorkspaceArea.AddObject(b,2); }

      }

      if (name.equals("bond-builder")){
         bond b = (bond)arguments.elementAt(0);

         String st = "trying to build "+b;
         if (b.left_obj.string==workspace.initial)
            st+=" in initial string";
            else st+=" in target string";

         print(st);
         b.update_strength_value();
         print("strength = "+b.total_strength);
         Vector compeditors = b.get_incompatible_bonds();
         if ((workspace.workspace_objects.contains(b.from_obj))&&
             (workspace.workspace_objects.contains(b.to_obj))){
            for (int i=0; i<b.string.bonds.size(); i++){
              bond b2=(bond) b.string.bonds.elementAt(i);
              if ((b.left_obj==b2.left_obj)&&(b.right_obj==b2.right_obj)){
                 // check to see if this is the same bond
                 if ((b.direction_category==b2.direction_category)&&
                     (b.bond_category==b2.bond_category)){
                     // bond already exists
                     if (b.direction_category!=null) b.direction_category.buffer=100.0;
                     b.bond_category.buffer=100.0;
                     print("already exists: activate descriptors & Fizzle!");
                     return true;
                 }
              }
            }
            
            // check for incompatible structures
            Vector incb = b.get_incompatible_bonds();
            if (incb.size()!=0){
               print("trying to break incompatible bonds");
               // try to break all incompatible bonds
               if (workspace_formulas.fight_it_out(b,1.0,incb,1.0)){
                  // beat all competing bonds
                  print("won");
               }
               else {
                  print("failed: Fizzle!");
                  return false;
               }
            }
            else print("no incompatible bonds!");

            // fight all groups containing these objects
            Vector incg = workspace_formulas.get_common_groups(b.from_obj,b.to_obj);
            if (incg.size()!=0){
               print("trying to break incompatible groups");
               // try to break all incompatible groups
               if (workspace_formulas.fight_it_out(b,1.0,incg,1.0)){
                  // beat all competing groups
                  print("won");
                }
               else {
                 print("failed: Fizzle!");
                 return false;
               }
            }
            else print("no incompatible groups!");

            // fight all incompatible correspondences
            Vector incc = null;
            if (((b.left_obj).leftmost)||((b.right_obj).rightmost))
              if (b.direction_category!=null){
                //System.out.println("looking for incompatible correspondences");
                incc=b.get_incompatible_correspondences();
                if (incc!=null){
                   print("trying to break incompatible correspondences");
                   if (!workspace_formulas.fight_it_out(b,2.0,incc,3.0))
                       { print("lost: Fizzle!");
                        return false; // ie lost the fight
                        }
                   print("won");
                }
                //else //System.out.println("no incompatible correspondences found");
            }

            for (int i=0; i<incb.size(); i++){
              bond br=(bond)incb.elementAt(i);
              br.break_bond();
            }
            for (int i=0; i<incg.size(); i++){
              group gr=(group)incg.elementAt(i);
              gr.break_group();
            }
            if (incc!=null) for (int i=0; i<incc.size(); i++){
               Correspondence c = (Correspondence)incc.elementAt(i);
               c.break_correspondence();
            }
            print("building bond");
            b.build_bond();
          }
          else {
             print("objects do no longer exists: Fizzle!");
             return false;
          }
      }

//********************************************************************************
 //                               GROUP CODELETS
//********************************************************************************

      if (name.equals("top-down-group-scout--category")){
         slipnode group_cat = (slipnode) arguments.elementAt(0);
         print("trying to build "+group_cat.pname+" group");
         
         slipnode bond_category= slipnet_formulas.get_related_node(group_cat,slipnet.bond_category);
         if (bond_category==null) {
           //System.out.println("<c> no bond-category found");
           return true;
         }
         double i_relevance = workspace_formulas.local_bond_category_relevance(
                                 workspace.initial,bond_category);
         double t_relevance = workspace_formulas.local_bond_category_relevance(
                                 workspace.target,bond_category);
         double i_unhappiness = workspace.initial.intra_string_unhappiness;
         double t_unhappiness = workspace.target.intra_string_unhappiness;

         print("about to choose string:");
         print("initial string: relevance="+i_relevance+", unhappiness="+i_unhappiness);
         print("target string: relevance="+t_relevance+", unhappiness="+t_unhappiness);
       
         workspace_string string = workspace.initial;
         if ((random.rnd()*(i_relevance+i_unhappiness+t_relevance+t_unhappiness))>
             (i_relevance+i_unhappiness)) {
                  string = workspace.target;
                  print("target string selected");
                  }
             else print("initial string selected");
         
         // choose an object on the workspace by intra-string-salience
         workspace_object fromob = workspace_formulas.choose_object("intra_string_salience",string.objects);
         print("object chosen: "+fromob);
         if (fromob.spans_string){
            print("chosen object spans the string. fizzle"); 
            return false;
         }
         slipnode direction;
         if (fromob.leftmost) direction=slipnet.right;
         else if (fromob.rightmost) direction=slipnet.left;
         else {
            Vector v = new Vector(); 
            v.addElement(new MDouble(slipnet.left.activation));
            v.addElement(new MDouble(slipnet.right.activation));
            if (workspace_formulas.select_list_position(v)==0) direction = slipnet.left; else direction = slipnet.right;                 }
         print("trying from "+fromob+" "+bond_category.pname+" checking to "+direction.pname+" first");
         
         bond first_bond;
         if (direction==slipnet.left) first_bond = fromob.left_bond;
         else first_bond = fromob.right_bond;
       
         if ((first_bond==null)||(first_bond.bond_category!=bond_category))
            {
             // check the other side of object
 
             if (direction==slipnet.right) first_bond = fromob.left_bond;
                else first_bond = fromob.right_bond;
             if ((first_bond==null)||(first_bond.bond_category!=bond_category))
                 {  
                   // this is a single letter group
                   if ((bond_category!=slipnet.sameness)||
                       (!(fromob instanceof letter))) {
                     print("no bonds of this type found: fizzle!");
                     return false; // fizzle
                       }
                    else {
                       print("thinking about a single letter group");
                       Vector oblist = new Vector(); 
                       oblist.addElement(fromob);
                       group g = new group(fromob.string,slipnet.samegrp,
                           null,slipnet.letter_category,
                           oblist, new Vector());
                       double prob = g.single_letter_group_probability();
                       if (random.rnd()<prob){
                          // propose single letter group
                          coderack.propose_group(oblist,new Vector(),
                               slipnet.samegrp,
                                null,slipnet.letter_category,this);
                          print("single letter group proposed");         
                       }
                       else print("failed");
                       return true;
                    }
                  }
             }
         direction = first_bond.direction_category;
         boolean search = true;
         slipnode bond_facet = null; 
         Vector object_list = new Vector();
         Vector bond_list = new Vector();
         // find leftmost object in group with these bonds
         while (search){
           search = false;
           if (fromob.left_bond!=null){
             if (fromob.left_bond.bond_category==bond_category){
                if ((fromob.left_bond.direction_category==null)||
                    (fromob.left_bond.direction_category==direction)){
                   if ((bond_facet==null)||(bond_facet==fromob.left_bond.bond_facet)){
                   bond_facet = fromob.left_bond.bond_facet;
                   direction = fromob.left_bond.direction_category;
                   fromob = fromob.left_bond.left_obj;
                   search=true;}
                }
             }
           }
         }

         // find rightmost object in group with these bonds
         search = true; workspace_object toob = fromob;
         while (search){
           search = false;
           if (toob.right_bond!=null){
             if (toob.right_bond.bond_category==bond_category){
                if ((toob.right_bond.direction_category==null)||
                    (toob.right_bond.direction_category==direction)){
                   if ((bond_facet==null)||(bond_facet==toob.right_bond.bond_facet)){
                   bond_facet = toob.right_bond.bond_facet;
                   direction = fromob.right_bond.direction_category;
                   toob = toob.right_bond.right_obj;
                   search=true;}
                }
             }
           }
         }
       
         if (toob == fromob) {
            print("no possible group - fizzle");
            return false;
         }
         print("proposing group from "+fromob+" to "+toob);
         object_list.addElement(fromob);
         while (fromob!=toob){
           bond_list.addElement(fromob.right_bond);
           object_list.addElement(fromob.right_bond.right_obj);
           fromob=fromob.right_bond.right_obj;
         }
        
         coderack.propose_group(object_list,bond_list,group_cat,direction,bond_facet,this);
      }


      if (name.equals("top-down-group-scout--direction")){
         slipnode direction = (slipnode) arguments.elementAt(0);
         print("looking for "+direction.pname+" group");
         double i_relevance = workspace_formulas.local_direction_category_relevance(
                                 workspace.initial,direction);
         double t_relevance = workspace_formulas.local_direction_category_relevance(
                                 workspace.target,direction);
         double i_unhappiness = workspace.initial.intra_string_unhappiness;
         double t_unhappiness = workspace.target.intra_string_unhappiness;

         print("about to choose string:");
         print("initial string: relevance="+i_relevance+", unhappiness="+i_unhappiness);
         print("target string: relevance="+t_relevance+", unhappiness="+t_unhappiness);
       
         workspace_string string = workspace.initial;
         if ((random.rnd()*(i_relevance+i_unhappiness+t_relevance+t_unhappiness))>
             (i_relevance+i_unhappiness)) {
                  string = workspace.target;
                  print("target string selected");
                  }
             else print("initial string selected");
         
         // choose an object on the workspace by intra-string-salience
         workspace_object fromob = workspace_formulas.choose_object("intra_string_salience",string.objects);
         print("object chosen = "+fromob.toString());
         if (fromob.spans_string){
            print("chosen object spans the string. fizzle"); return false;
         }
         
         slipnode bond_category=null;


         slipnode mydirection;
         if (fromob.leftmost) mydirection=slipnet.right;
         else if (fromob.rightmost) mydirection=slipnet.left;
         else {
            Vector v = new Vector(); 
            v.addElement(new MDouble(slipnet.left.activation));
            v.addElement(new MDouble(slipnet.right.activation));
            if (workspace_formulas.select_list_position(v)==0) mydirection = slipnet.left; else mydirection = slipnet.right;                          
         }

         bond first_bond;
         if (mydirection==slipnet.left) first_bond = fromob.left_bond;
         else first_bond = fromob.right_bond;
         if ((first_bond!=null)&&(first_bond.direction_category==null))
             direction=null;
         if ((first_bond==null)||(first_bond.direction_category!=direction))
            {  
              if (mydirection==slipnet.right) first_bond = fromob.left_bond;
               else first_bond = fromob.right_bond;
              if ((first_bond!=null)&&(first_bond.direction_category==null))
                 direction=null;
              if ((first_bond==null)||(first_bond.direction_category!=direction))
               { print("no possible group: fizzle!"); return false; }
             }
         bond_category = first_bond.bond_category;
          
         if (bond_category==null){
            print("no bond in the "+direction.pname+" direction was found: fizzle.");
            return false;
         }
         slipnode group_category= slipnet_formulas.get_related_node(bond_category,slipnet.group_category);

         print("trying from "+fromob+" "+bond_category.pname);
         
         boolean search = true;
         slipnode bond_facet = null; 
         Vector object_list = new Vector();
         Vector bond_list = new Vector();
         // find leftmost object in group with these bonds
         while (search){
           search = false;
           if (fromob.left_bond!=null){
             if (fromob.left_bond.bond_category==bond_category){
                if ((fromob.left_bond.direction_category==null)||
                    (fromob.left_bond.direction_category==direction)){
                   if ((bond_facet==null)||(bond_facet==fromob.left_bond.bond_facet)){
                   bond_facet = fromob.left_bond.bond_facet;
                   direction = fromob.left_bond.direction_category;
                   fromob = fromob.left_bond.left_obj;
                   search=true;}
                }
             }
           }
         }

         // find rightmost object in group with these bonds
         search = true; workspace_object toob = fromob;
         while (search){
           search = false;
           if (toob.right_bond!=null){
             if (toob.right_bond.bond_category==bond_category){
                if ((toob.right_bond.direction_category==null)||
                    (toob.right_bond.direction_category==direction)){
                   if ((bond_facet==null)||(bond_facet==toob.right_bond.bond_facet)){
                   bond_facet = toob.right_bond.bond_facet;
                   direction = fromob.right_bond.direction_category;
                   toob = toob.right_bond.right_obj;
                   search=true;}
                }
             }
           }
         }
       
         if (toob == fromob) {
            print("no possible group - fizzle");
            return false;
         }
         print("proposing group from "+fromob+" to "+toob);
         object_list.addElement(fromob);
         while (fromob!=toob){
           bond_list.addElement(fromob.right_bond);
           object_list.addElement(fromob.right_bond.right_obj);
           fromob=fromob.right_bond.right_obj;
         }
        
         coderack.propose_group(object_list,bond_list,group_category,direction,bond_facet,this);
      }



      if (name.equals("group-scout--whole-string")){
                  
         print("about to choose string");
         workspace_string string = workspace.initial;
         if (random.rnd()>0.5){
                  string = workspace.target;
                  print("target string selected");
                  }
             else print("initial string selected");

         // find leftmost object & the highest group to which it belongs
         workspace_object leftmost = null;
         for (int x=0; x<string.objects.size(); x++){
           workspace_object w = (workspace_object)string.objects.elementAt(x);
           if (w.leftmost) leftmost = w;
         }
         while ((leftmost.group!=null)&&((leftmost.group).bond_category==slipnet.sameness))
               leftmost=leftmost.group;
         if (leftmost.spans_string){
            // the object already spans the string - propose this object
            group g = (group)leftmost;
            coderack.propose_group(g.object_list,
               g.bond_list,g.group_category,g.direction_category,
               g.bond_facet,this);
            print("selected object already spans string: propose");
            return true;      
         }         

         Vector bond_list = new Vector();
         Vector object_list = new Vector(); 
         object_list.addElement(leftmost);

         while (leftmost.right_bond!=null){
            bond_list.addElement(leftmost.right_bond);
            leftmost = (leftmost.right_bond).right_obj;
            object_list.addElement(leftmost);
         }
         if (!(leftmost.rightmost)){
            print("no spanning bonds - fizzle");
            return false;
         }
         // choose a random bond from list
         int pos = (int)(random.rnd()*(double)(bond_list.size()));
         if (pos>=bond_list.size()) pos=0;
         bond chosen_bond = (bond)bond_list.elementAt(pos);
         slipnode bond_category = chosen_bond.bond_category;
         slipnode direction_category = chosen_bond.direction_category;
         slipnode bond_facet = chosen_bond.bond_facet;
         bond_list = workspace_formulas.possible_group_bond_list(bond_category,
                           direction_category, bond_facet, bond_list);
         if (bond_list==null){
            print("no possible group - fizzle");
            return false;
         }

         slipnode group_category= slipnet_formulas.get_related_node(bond_category,slipnet.group_category);
         print("proposing "+bond_category.pname+" group");
         coderack.propose_group(object_list,bond_list,group_category,direction_category,bond_facet,this);
      }



      if (name.equals("group-strength-tester")){
         // update strength value of the group
         group g = (group)arguments.elementAt(0);
         String st = g.toString();
         if (g.string==workspace.initial) st+=" in initial string";
         else st+=" in target string";
         print("evaluating  "+st);
         g.update_strength_value();
         double strength = g.total_strength;
         double prob = workspace_formulas.temperature_adjusted_probability(strength/100.0);
         print("strength = "+strength+", adjusted prob.= "+prob);
         if (random.rnd()>prob){
            print("not strong enough: fizzled!");
            return false; 
         }
         // it is strong enough - post builder  & activate nodes
            (slipnet_formulas.get_related_node(g.group_category,slipnet.bond_category)).buffer=100.0;
            if (g.direction_category!=null) g.direction_category.buffer=100.0;
            workspace.Workspace_Comments.text+=": succeeded ";
          if (!coderack.remove_terraced_scan) workspace.WorkspaceArea.AddObject(g,2);
          codelet nc = new codelet("group-builder",coderack.get_urgency_bin(strength));
          nc.arguments = arguments;
          nc.Pressure_Type = this.Pressure_Type;
          if (coderack.remove_terraced_scan) nc.run();
          else coderack.Post(nc);
          print("posting group builder codelet");

      }

      if (name.equals("group-builder")){
         // update strength value of the group
         group g = (group)arguments.elementAt(0);
         String st = g.toString();
         if (g.string==workspace.initial) st+=" in initial string";
         else st+=" in target string";
         print("trying to build "+st);

         if (workspace_formulas.group_present(g)){
           print("already exists...activate descriptors & fizzle");
           g.activate_descriptions();
           workspace_object wo = workspace_formulas.equivalent_group(g);
           wo.add_descriptions(g.descriptions);
           return false;   

         }

         // check to see if all objects are still there
         for (int i=0; i<g.object_list.size(); i++){
            if (!(workspace.workspace_objects.contains((workspace_object)g.object_list.elementAt(i)))) {
            print("objects no longer exist! - fizzle");
            return false;}
         }

         // check to see if bonds are there of the same direction
         Vector incb = new Vector();  // incompatible bond list
         for (int i=1; i<g.object_list.size(); i++){
            bond b=((workspace_object)g.object_list.elementAt(i)).left_bond;
            if (b!=null){
               workspace_object ob2=b.left_obj;
               if ((ob2!=(workspace_object)g.object_list.elementAt(i-1))||
                   (b.direction_category!=g.direction_category))
                 incb.addElement(b);
            }
         }

         if (g.object_list.size()>1){
            bond b=((workspace_object)g.object_list.elementAt(0)).right_bond;
            if (b!=null){
               workspace_object ob2=b.right_obj;
               if ((ob2!=(workspace_object)g.object_list.elementAt(1))||
                   (b.direction_category!=g.direction_category))
                 incb.addElement(b);
            }
         }
         // if incompatible bonds exist - fight
         g.update_strength_value();
            if (incb.size()!=0){
               print("fighting incompatible bonds");
               // try to break all incompatible groups
               if (workspace_formulas.fight_it_out(g,1.0,incb,1.0)){
                  // beat all competing groups
                  print("won!");
               }
               else {
                print("couldn't break incompatible bonds: fizzle!");
                return false;
               }
            }

         // fight incompatible groups
            // fight all groups containing these objects
            Vector incg = workspace_formulas.get_incompatible_groups(g);
            if (incg.size()!=0){
               print("fighting incompatible groups");
               // try to break all incompatible groups
               if (workspace_formulas.fight_it_out(g,1.0,incg,1.0)){
                  // beat all competing groups
                  print("won");
               }
               else {
                print("couldn't break incompatible groups: fizzle");
                return false;
               }
            }

         // destroy incompatible bonds
            for (int i=0; i<incb.size(); i++){
              bond b=(bond)incb.elementAt(i);
              b.break_bond();
            }
           
         // create new bonds
            g.bond_list.removeAllElements();
            for (int i=1;i<g.object_list.size();i++){
              workspace_object ob1=(workspace_object)g.object_list.elementAt(i-1);
              workspace_object ob2=(workspace_object)g.object_list.elementAt(i);
              if (ob1.right_bond==null){
                 workspace_object from_obj,to_obj;
                 if (g.direction_category==slipnet.right){
                     from_obj=ob1; to_obj=ob2;
                 }
                 else {from_obj=ob2; to_obj=ob1;}
                 slipnode bond_category=slipnet_formulas.get_related_node(g.group_category,slipnet.bond_category);
                 bond nb = new bond(from_obj,to_obj,bond_category,g.bond_facet,from_obj.get_description(g.bond_facet),to_obj.get_description(g.bond_facet));
                nb.build_bond();
              }
              g.bond_list.addElement(ob1.right_bond);
            }

         // destroy incompatible groups
            for (int i=0; i<incg.size(); i++){
              group gr=(group)incg.elementAt(i);
              gr.break_group();
            }

         g.build_group();
         g.activate_descriptions();
         print("building group");
      }

      ///////////////////////////////////////////////////////////////////////////////
      //                         REPLACEMENT FINDER
      ////////////////////////////////////////////////////////////////////////////////
      if (name.equals("replacement-finder")){
         // choose random letter in initial string
         Vector letters = new Vector();
         for (int i=0; i<workspace.initial.objects.size(); i++){
            workspace_object wo = (workspace_object)workspace.initial.objects.elementAt(i);
            if (wo instanceof letter) letters.addElement(wo);
         }
         double size = (double)(letters.size());
         double pos = random.rnd()*size; if (pos>=size) pos=size-1.0;
         workspace_object i_letter = (workspace_object) letters.elementAt((int)pos);
         print("selected letter in initial string ="+i_letter);
         if (i_letter.replacement!=null){
             print("replacement already found for this object. Fizzle!");
             return false;
         }
         
         int position = i_letter.left_string_position;
         workspace_object m_letter = null;
         for (int i=0; i<workspace.modified.objects.size(); i++){
            workspace_object wo = (workspace_object)workspace.modified.objects.elementAt(i);
            if ((wo instanceof letter)&&(wo.left_string_position==position)) m_letter=wo;
         }
         if (m_letter==null){
             print("Error - no corresponding letter could be found. Fizzle!");
             return false;

         }
         slipnode relation = null;
         position-=1;
         char[] initial_chars = workspace.initial_string.toCharArray();
         char[] modified_chars = workspace.modified_string.toCharArray();
         if (initial_chars[position]==modified_chars[position]) relation=slipnet.sameness;
         if (initial_chars[position]==(modified_chars[position]-1)) relation=slipnet.successor;
         if (initial_chars[position]==(modified_chars[position]+1)) relation=slipnet.predecessor;
         if (relation!=null) print(relation.pname+" relation found");
         else print("no relation found");      

         i_letter.replacement=new Replacement(i_letter,m_letter,relation);
         workspace.WorkspaceArea.AddObject(i_letter.replacement);
         if (relation!=slipnet.sameness) {
            i_letter.changed=true;
            workspace.changed_object = i_letter;
         }
         print("building replacement");
      }


//////////////////////////////////////////////////////////////////////////////////////////////////////
//                                RULE CODELETS
//////////////////////////////////////////////////////////////////////////////////////////////////////
      if (name.equals("rule-scout")){
         if ((workspace_formulas.unreplaced_objects()).size()!=0){
            print("not all replacements have been found. Fizzle");
            return false;
         }
         workspace_object changed = null;
         // find changed object;
         for (int i=0; i<workspace.initial.objects.size(); i++){
            workspace_object wo=(workspace_object)workspace.initial.objects.elementAt(i);
            if (wo.changed) changed=wo;
         }
         
         // if there are no changed objects, propose a rule with no changes
         if (changed==null) {
            print("there are no changed objects!");
            print("proposing null rule");
            coderack.propose_rule(null,null,null,null,this);
         }
          
         // generate a list of distinguishing descriptions for the first object
         // ie. string-position (leftmost,rightmost,middle or whole) or letter category
         // if it is the only one of its type in the string
         Vector object_list = new Vector();
         slipnode position = changed.get_description(slipnet.string_position_category);
         if (position!=null) object_list.addElement(position);
         slipnode letter = changed.get_description(slipnet.letter_category);
         boolean only_letter = true;  // if this is true, the letter can be thought of as a
                                      // distinguishing feature        
         for (int i=0; i<workspace.initial.objects.size(); i++){
            workspace_object wo=(workspace_object)workspace.initial.objects.elementAt(i);
            if ((wo.get_description_type(letter)!=null)&&(wo!=changed)) only_letter=false;
         }
         if (only_letter) object_list.addElement(letter);

         // if this object corresponds to another object in the workspace
         // object_list = the union of this and the distingushing descriptors
         if (changed.correspondence!=null){
              workspace_object obj2 = changed.correspondence.obj2;
              Vector new_list = new Vector();
              Vector slippages = workspace.slippage_list();
              for (int x=0; x<object_list.size(); x++){
                 slipnode s = (slipnode) object_list.elementAt(x);
                 s=Rule.apply_slippages(s,slippages);
                 if (obj2.has_description(s)) 
                   if (obj2.distinguishing_descriptor(s))
                     new_list.addElement(s);
              }
              object_list = new_list;
         }

         if (object_list.size()==0){
             print("no distinguishing descriptions could be found. fizzle");
             return false;
         }         
         
         // use conceptual depth to choose a description
         Vector value_list = new Vector();
         print("choosing a description based on conceptual depth:");
         for (int i=0; i<object_list.size(); i++){
            double value=workspace_formulas.temperature_adjusted_value(((slipnode)object_list.elementAt(i)).conceptual_depth); 
            value_list.addElement(new MDouble(value));
         }
         slipnode descriptor = (slipnode)object_list.elementAt(workspace_formulas.select_list_position(value_list));
         print("Chosen descriptor: "+descriptor.pname);

         // choose the relation(change the letmost object to..xxxx) i.e. "successor" or "d"
         object_list.removeAllElements();
         if (changed.replacement.relation!=null) object_list.addElement(changed.replacement.relation);
         object_list.addElement(changed.replacement.to_obj.get_description(slipnet.letter_category));

         // use conceptual depth to choose a relation
         value_list.removeAllElements();
         print("choosing relation based on conceptual depth:");
         for (int i=0; i<object_list.size(); i++){
            double value=workspace_formulas.temperature_adjusted_value(((slipnode)object_list.elementAt(i)).conceptual_depth); 
            value_list.addElement(new MDouble(value));
         }
         slipnode relation = (slipnode)object_list.elementAt(workspace_formulas.select_list_position(value_list));
         print("Chosen relation: "+relation.pname);
         coderack.propose_rule(slipnet.letter_category,descriptor,slipnet.letter,relation,this);
        
         print("proposing rule:");
         print("change letter-cat of "+descriptor.pname+" letter to "+ relation.pname);

      }

      if (name.equals("rule-strength-tester")){
         Rule r = (Rule)arguments.elementAt(0);
         print("testing: "+r.toString());
         r.update_strength_value(); 
         double strength = r.total_strength;
         double prob = workspace_formulas.temperature_adjusted_probability(strength/100.0);
         print("strength = "+strength+", adjusted prob.="+prob);
         if (random.rnd()>prob) {
             print("not strong enough: fizzle ");
             return false; // not strong enough
         }
        codelet nc = new codelet("rule-builder",coderack.get_urgency_bin(strength));
        nc.arguments.addElement(r);
        nc.Pressure_Type = this.Pressure_Type;
        print("posting rule-builder");

          if (coderack.remove_terraced_scan) nc.run();
          else coderack.Post(nc);

      }

      if (name.equals("rule-builder")){
         Rule r = (Rule)arguments.elementAt(0);
         print("trying to build "+r.toString());
         if (r.rule_equal(workspace.rule)){
            // rule already exists: fizzle, but activate concepts
            print("already exists - activate concepts");
            r.activate_rule_descriptions();
            return false;
         }

         r.update_strength_value(); 
         double strength = r.total_strength;
         if (strength==0.0) {
             print("the rule is incompatible with correspondences: Fizzle");
             return false;
         }

         // fight against other rules
         if (workspace.rule!=null){
             print("Fighting against existing rule");
             print("existing rule strength: "+workspace.rule.total_strength);
             print("this rule strength: "+r.total_strength);
             if (!workspace_formulas.structure_vs_structure(
                    r,1.0,workspace.rule,1.0)){
                 // lost the fight
                 print("lost the fight: fizzle!");
                 return false;
             }
             else print("won!");
         }
         r.build_rule();
         print("building rule");
      }



      if (name.equals("rule-translator")){
         double bond_density;
         if (workspace.rule==null) return false;
         if ((workspace.initial.length==1.0)&&(
             workspace.target.length==1.0)) bond_density=1.0;
         else {
            bond_density = ((double)(workspace.initial.bonds.size()+
                            workspace.target.bonds.size()))/
                (double)(workspace.initial.length+
                         workspace.target.length-2);
         }
         double distribution[];
         if (bond_density>1.0) bond_density=1.0;
         print("bond density : "+bond_density);
         if (bond_density>0.8) distribution = workspace_formulas.very_low_distribution;
         else if (bond_density>0.6) distribution = workspace_formulas.low_distribution;
         else if (bond_density>0.4) distribution = workspace_formulas.medium_distribution;
         else if (bond_density>0.2) distribution = workspace_formulas.high_distribution;
         else distribution = workspace_formulas.very_high_distribution;

         double cutoff = ((double)(workspace_formulas.choose(distribution)))*10.0;
         print("temperature cutoff = "+cutoff);
         if (cutoff<formulas.actual_temperature){
            // not high enough
            print("not high enough: Fizzle");
            return false;
         }
         print("building translated rule!");
         print("Slippages used in translation:");
         Vector slippage_list = workspace.slippage_list();
         for (int x=0; x<slippage_list.size(); x++){
            print(((concept_mapping)slippage_list.elementAt(x)).toString());
         }
         //System.out.println("number of codelets = "+coderack.codelets_run);
         if ((workspace.rule).build_translated_rule(coderack.codelets_run)==true)
           workspace.found_answer=true;
          else {
            Temperature.clamp_time = coderack.codelets_run+100;
            Temperature.clamped = true;
            formulas.temperature = 100.0;
          }
      }

      ////////////////////////////////////////////////////////////////
      //   CORRESPONDENCE CODELETS
      ////////////////////////////////////////////////////////////////
      if (name.equals("bottom-up-correspondence-scout")){
         workspace_object obj1 = workspace_formulas.choose_object("inter_string_salience",workspace.initial.objects);
         workspace_object obj2 = workspace_formulas.choose_object("inter_string_salience",workspace.target.objects); 
         Vector concept_mapping_list = new Vector();  
         boolean flip_obj2 = false;      
         print("trying a correspondence between "+obj1+" and "+obj2);
 

         // if one object spans the string and the other doesn't - fizzle
         if (obj1.spans_string!=obj2.spans_string){
             // fizzle
             print("only one object spans the string: fizzle");
             return false;
         }
          
         // get the posible concept-mappings
         concept_mapping_list = concept_mapping.get_concept_mapping_list(
             obj1, obj2,
             obj1.relevant_descriptions(), obj2.relevant_descriptions());

         // check the slippability of concept mappings
         boolean cm_possible = false;
         for (int x=0; x<concept_mapping_list.size(); x++){
            concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
            double slip_prob = workspace_formulas.temperature_adjusted_probability(cm.slipability()/100.0);
            if (workspace_formulas.flip_coin(slip_prob)) cm_possible = true; 
        }

         if (concept_mapping_list.size()==0) {
             print("no possible mappings exist: fizzle");
               return false;  // no possible mappings
         }
         if (!cm_possible) {
             print("cannot make appropriate slippage: fizzle");
              return false; //cannot make necessary slippages
         }

         //find out if any are distinguishing
         Vector distinguishing_mappings = new Vector();
  
         for (int x=0; x<concept_mapping_list.size(); x++){
            concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
            if (cm.distinguishing()) distinguishing_mappings.addElement(cm);
         }
         
         if (distinguishing_mappings.size()==0) {
            print("no distinguishing mappings found: fizzle");
            return false; // no distinguishing mappings
         }
    
         // if both objects span the strings, check to see if the
         // string description needs to be flipped

         Vector possible_opp_mappings = new Vector();
         for (int x=0; x<distinguishing_mappings.size(); x++){
            concept_mapping cm = (concept_mapping)distinguishing_mappings.elementAt(x);
            if ((cm.description_type1==slipnet.string_position_category)&&
                 (cm.description_type1!=slipnet.bond_facet))
            possible_opp_mappings.addElement(cm);
        }
        
        Vector dt1 = new Vector();
        for (int x=0; x<possible_opp_mappings.size(); x++)
           dt1.addElement(((concept_mapping)possible_opp_mappings.elementAt(x)).description_type1);
     

        if ((obj1.spans_string)&&
            (obj2.spans_string)&&
            (dt1.contains(slipnet.direction_category))&&
            (concept_mapping.all_opposite_mappings(possible_opp_mappings))&&
            (slipnet.opposite.activation!=100.0)){
             obj2 = ((group)obj2).flipped_version();
 
             concept_mapping_list = concept_mapping.get_concept_mapping_list(
                 obj1, obj2,
                 obj1.relevant_descriptions(), obj2.relevant_descriptions());
             flip_obj2 = true;            
        }


         print("Proposing correspondence with concept mappings:");
         for (int x=0; x<concept_mapping_list.size(); x++){
            concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
            print(cm.toString());        
        }
         coderack.propose_correspondence(obj1,obj2,concept_mapping_list,flip_obj2,this);
     }


      if (name.equals("important-object-correspondence-scout")){
         workspace_object obj1 = workspace_formulas.choose_object("relative_importance",workspace.initial.objects);
         
         print("object chosen from initial string: "+obj1);
         Vector v = obj1.relevant_distinguishing_descriptors();
         slipnode s = workspace_formulas.choose_slipnode_by_conceptual_depth(v);
         if (s==null) {
             print("no relevant distinguishing descriptors found: fizzle");
             return false;
         }

         Vector slist = workspace.slippage_list();
         slipnode obj1_descriptor = null;
         for (int x=0; x<slist.size(); x++){
            concept_mapping cm = (concept_mapping)slist.elementAt(x);
            if (cm.descriptor1==s) obj1_descriptor = cm.descriptor2;
         }
         if (obj1_descriptor==null) obj1_descriptor = s;
         Vector obj2_candidates = new Vector();

         for (int x=0; x<workspace.target.objects.size(); x++){
           workspace_object wo = (workspace_object)(workspace.target.objects.elementAt(x));
           Vector rdes = wo.relevant_descriptions();
           for (int y=0; y<rdes.size(); y++){
             description d = (description)rdes.elementAt(y);
             if (d.descriptor==obj1_descriptor) obj2_candidates.addElement(wo);
           }
         }

         if (obj2_candidates.size()==0) {
            print("no corresponding objects found: fizzle");
            return false;   
         }      

         workspace_object obj2 = workspace_formulas.choose_object("inter_string_salience",obj2_candidates); 
         Vector concept_mapping_list = new Vector();  
         boolean flip_obj2 = false;      
         print("trying a correspondence between "+obj1+" and "+obj2);
 
         // if one object spans the string and the other doesn't - fizzle
         if (obj1.spans_string!=obj2.spans_string){
             // fizzle
             print("only one object spans the string: fizzle"); 
             return false;
         }
          
         // get the posible concept-mappings
         concept_mapping_list = concept_mapping.get_concept_mapping_list(
             obj1, obj2,
             obj1.relevant_descriptions(), obj2.relevant_descriptions());

         // check the slippability of concept mappings
         boolean cm_possible = false;
         for (int x=0; x<concept_mapping_list.size(); x++){
            concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
            double slip_prob = workspace_formulas.temperature_adjusted_probability(cm.slipability()/100.0);
            if (workspace_formulas.flip_coin(slip_prob)) cm_possible = true; 
        }

         if (concept_mapping_list.size()==0) return false;  // no possible mappings
         if (!cm_possible) return false; //cannot make necessary slippages

         //find out if any are distinguishing
         Vector distinguishing_mappings = new Vector();
  
         for (int x=0; x<concept_mapping_list.size(); x++){
            concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
            if (cm.distinguishing()) distinguishing_mappings.addElement(cm);
         }
         
         if (distinguishing_mappings.size()==0) return false; // no distinguishing mappings
    
         // if both objects span the strings, check to see if the
         // string description needs to be flipped

         Vector possible_opp_mappings = new Vector();
         for (int x=0; x<distinguishing_mappings.size(); x++){
            concept_mapping cm = (concept_mapping)distinguishing_mappings.elementAt(x);
            if ((cm.description_type1==slipnet.string_position_category)&&
                 (cm.description_type1!=slipnet.bond_facet))
            possible_opp_mappings.addElement(cm);
        }
        
        Vector dt1 = new Vector();
        for (int x=0; x<possible_opp_mappings.size(); x++)
           dt1.addElement(((concept_mapping)possible_opp_mappings.elementAt(x)).description_type1);
     

        if ((obj1.spans_string)&&
            (obj2.spans_string)&&
            (dt1.contains(slipnet.direction_category))&&
            (concept_mapping.all_opposite_mappings(possible_opp_mappings))&&
            (slipnet.opposite.activation!=100.0)){
             obj2 = ((group)obj2).flipped_version();
 
             concept_mapping_list = concept_mapping.get_concept_mapping_list(
                 obj1, obj2,
                 obj1.relevant_descriptions(), obj2.relevant_descriptions());
             flip_obj2 = true;            
        }


         print("proposing correspondence with concept mappings:");
         for (int x=0; x<concept_mapping_list.size(); x++){
            concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
            print(cm.toString());        
        }
         coderack.propose_correspondence(obj1,obj2,concept_mapping_list,flip_obj2,this);
     }



     if (name.equals("correspondence-strength-tester")){
        Correspondence c = (Correspondence)arguments.elementAt(0); 
        workspace_object obj1 = c.obj1;
        workspace_object obj2 = c.obj2;
        print("evaluating correspondence from "+obj1+" to "+obj2);
        if (!(workspace.workspace_objects.contains(obj1))||
           ((!(workspace.workspace_objects.contains(obj2)))&&
            (!((c.flip_obj2)&&((workspace.target).group_present(
                  ((group)obj2).flipped_version())==null)))))
              {

                print("objects no longer exist");
                return false;  // objects no longer exist
              }

         c.update_strength_value();
         double strength = c.total_strength;
         double prob = workspace_formulas.temperature_adjusted_probability(strength/100.0);
         print("strength = "+prob);
         if (random.rnd()>prob) {
             print("not strong enough: fizzle ");
             return false; // not strong enough
         }
         // activate some concepts
         for (int x=0; x<c.concept_mapping_list.size(); x++){
           concept_mapping cm = (concept_mapping)c.concept_mapping_list.elementAt(x);
           cm.description_type1.buffer=100.0;
           cm.descriptor1.buffer=100.0;
           cm.description_type2.buffer=100.0;
           cm.descriptor2.buffer=100.0;
         } 

     codelet ncd = new codelet("correspondence-builder",coderack.get_urgency_bin(strength));
     ncd.arguments.addElement(c); 
     print("posting correspondence builder");
     if (!coderack.remove_terraced_scan) workspace.WorkspaceArea.AddObject(c,2);
     ncd.Pressure_Type = this.Pressure_Type;
          if (coderack.remove_terraced_scan) ncd.run();
          else coderack.Post(ncd);
      
     }

     if (name.equals("correspondence-builder")){

        Correspondence c = (Correspondence)arguments.elementAt(0); 
        workspace_object obj1 = c.obj1;
        workspace_object obj2 = c.obj2;
        print("trying correspondence from "+obj1+" to "+obj2);
        if (!(workspace.workspace_objects.contains(obj1))||
           ((!(workspace.workspace_objects.contains(obj2)))&&
            (!((c.flip_obj2)&&((workspace.target).group_present(
                  ((group)obj2).flipped_version())==null)))))
              {

                print("objects no longer exist");
                return false;  // objects no longer exist
              }
        // if this correspondence is present, add any new concept mappings
        if (workspace.correspondence_present(c)){
           // if the correspondence exists, activate concept mappings
           // and add new ones to the existing corr.
           Correspondence existing = (c.obj1).correspondence;
           for (int x=0; x<c.concept_mapping_list.size(); x++){
               concept_mapping cm = (concept_mapping)c.concept_mapping_list.elementAt(x);
               if (cm.label!=null) (cm.label).buffer=100.0;
               if (!(existing.concept_mapping_present(cm)))
                   existing.concept_mapping_list.addElement(cm); 
            }
          print("correspondence is already present");
          print("activate concept mappings & fizzle");
          return false;
        }
        Vector incc = c.get_incompatible_correspondences();
        // fight against all correspondences
        if (incc.size()>0){
        print("fighting incompatible correspondences");
        for (int x=0; x<incc.size(); x++){
          Correspondence comp = (Correspondence)incc.elementAt(x);
          int csize = (c.obj1).letter_span()+(c.obj2).letter_span();
          int compsize = (comp.obj1).letter_span()+(comp.obj2).letter_span();
          if (!(workspace_formulas.structure_vs_structure(
                c,(double)csize,comp,(double)compsize))){
              print("not strong enough: fizzle");
              return false;  // fizzle as it has lost
          }
        }
        print("won!");
        }
        bond incompatible_bond = null;
        group incompatible_group = null;

        // if there is an incompatible bond then fight against it
        if (((c.obj1).leftmost)||((c.obj1).rightmost)&&
            ((c.obj2).leftmost)||((c.obj2).rightmost)){
            // search for the incompatible bond
            incompatible_bond = c.get_incompatible_bond();
            if (incompatible_bond!=null){
                print("fighting incompatible bond");
                // bond found - fight against it
               if (!(workspace_formulas.structure_vs_structure(
                c,3.0,incompatible_bond,2.0))){
                   print("lost: fizzle!");
                   return false;  // fizzle as it has lost
                }
                print("won");
                // won against incompatible bond
                incompatible_group = (c.obj2).group;
                if (incompatible_group!=null){
                    print("fighting incompatible group");
                    if (!(workspace_formulas.structure_vs_structure(
                       c,1.0,incompatible_group,1.0))){
                         print("lost: fizzle!");
                         return false;  // fizzle as it has lost
                     }
                    print("won");
                }
            }
        }

        // if there is an incompatible rule, fight against it
        Rule incompat_rule = null;
        if (Rule.incompatible_rule_corr(workspace.rule,c)){
           incompat_rule=workspace.rule;
           print("Fighting against incompatible Rule"); 
                    if (!(workspace_formulas.structure_vs_structure(
                       c,1.0,incompat_rule,1.0))){
                         print("lost: fizzle!");
                         return false;  // fizzle as it has lost
                     }
                    print("won");         
        }

        for (int x=0; x<incc.size(); x++){
          Correspondence comp = (Correspondence)incc.elementAt(x);
          comp.break_correspondence();
        }
        // break incompatible group and bond if they exist
        if (incompatible_bond!=null) incompatible_bond.break_bond();
        if (incompatible_group!=null) incompatible_group.break_group();
        if (incompat_rule!=null) incompat_rule.break_rule();
        print("building correspondence");
        c.build_correspondence();
     }

      return true;
   }

  static void add_to_initial_codelets(String s){
    initial_codelets.addElement(s);
  }

}
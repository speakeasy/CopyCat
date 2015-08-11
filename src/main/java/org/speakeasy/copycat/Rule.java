package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class Rule extends workspace_structure{
  slipnode object_category,descriptor_facet,descriptor;
  slipnode relation;
  String final_answer = "";

  Rule(slipnode f, slipnode d, slipnode oc, slipnode r){
    relation=r;
    descriptor_facet=f;
    object_category=oc;
    descriptor=d;
  }

   public boolean rule_equal(Rule r){
     if (r==null) return false;
     if ((relation==r.relation)&&
         (descriptor_facet==r.descriptor_facet)&&
         (object_category==r.object_category)&&
         (descriptor==r.descriptor)) return true;
     return false;
   }

   public void activate_rule_descriptions(){
     if (relation!=null) relation.buffer=100.0;
     if (descriptor_facet!=null) descriptor_facet.buffer=100.0;
     if (object_category!=null) object_category.buffer=100.0;
     if (descriptor!=null) descriptor.buffer=100.0;
   }

   public String toString(){
     String s;
     s="replace "+descriptor_facet.pname+" of "+descriptor.pname+" "+object_category.pname+" by "+relation.pname;
     return s;
   }

   public void build_rule(){
     workspace.Workspace_Rule.Change_Caption("rule : " +this.toString());
     if (workspace.rule!=null) workspace.workspace_structures.removeElement(workspace.rule);
     workspace.rule= this;
     workspace.workspace_structures.addElement(this);
     activate_rule_descriptions();
   }

   public static void break_rule(){
     workspace.rule = null;
     workspace.Workspace_Rule.Change_Caption("no rule");    
   }

   public static boolean incompatible_rule_corr(Rule r, Correspondence c){
     if ((r==null)||(c==null)) return false;
         workspace_object changed = null;
         // find changed object;
         for (int i=0; i<workspace.initial.objects.size(); i++){
            workspace_object wo=(workspace_object)workspace.initial.objects.elementAt(i);
            if (wo.changed) changed=wo;
         }

         if (c.obj1!=changed) return false;
         // it is incompatible if the rule descriptor is not in the mapping list
         for (int i=0; i<c.concept_mapping_list.size(); i++){
           concept_mapping cm = (concept_mapping) c.concept_mapping_list.elementAt(i);
           if (cm.descriptor1==r.descriptor) return false;
         }


     return true;
   }

   public static slipnode apply_slippages(slipnode sn,Vector slippages){
      for (int x=0; x<slippages.size(); x++){
        concept_mapping cm = (concept_mapping)slippages.elementAt(x);
        if (sn==cm.descriptor1) return cm.descriptor2; 
      }
      return sn;
   }

   public String change_string(String s){
     // applies the changes to this string ie. successor
     boolean stringok = true;
     if (descriptor_facet==slipnet.length){
        if (relation==slipnet.predecessor)
           return s.substring(0,s.length()-1);
        if (relation==slipnet.successor){
          String addon = s.substring(0,1);
          return s+addon;
        }
        return s;
     }
     // apply character changes
     char[] st = s.toCharArray();
     char ch =36;
     char[] charname = (relation.short_name).toCharArray();

     for (int x=0; x<st.length; x++){
        if (relation==slipnet.predecessor) {
           st[x]=(char)(st[x]-1);
           if (st[x]<97) return "NULL";
        }
        else if (relation==slipnet.successor) {
            st[x]=(char)(st[x]+1);
            if (st[x]>122) return "NULL";
        }
        else st[x]=(char)((int)(charname[0])+32);

     }

     return new String(st); 

   }

   public boolean build_translated_rule(int codelets_run) {
     Vector v = workspace.slippage_list();
     object_category = apply_slippages(object_category,v);
     descriptor_facet = apply_slippages(descriptor_facet,v);
     descriptor = apply_slippages(descriptor,v);
     relation = apply_slippages(relation,v);
     //System.out.println(this);

     // generate the final string
     final_answer = workspace.target_string;
     workspace_object changed_ob = null;
     for (int x=0; x<workspace.target.objects.size(); x++){
       workspace_object wo = (workspace_object)workspace.target.objects.elementAt(x);
       if ((wo.has_description(descriptor))&&
           (wo.has_description(object_category))) changed_ob = wo;
     }
     if (changed_ob!=null){
       //System.out.println("changed object = "+changed_ob);
       int string_length = final_answer.length();
       int start_pos = changed_ob.left_string_position;
       int end_pos = changed_ob.right_string_position;
       String start_string = "";
       String middle_string = "";
       String end_string = "";
       if (start_pos>1) start_string = final_answer.substring(0,start_pos-1);
       middle_string = 
         change_string(final_answer.substring(start_pos-1,end_pos));
       if (end_pos<string_length) end_string =
              final_answer.substring(end_pos);
       final_answer = start_string + middle_string + end_string;
       if (middle_string.equals("NULL")) return false;
     }
     System.out.println(final_answer+" "+codelets_run+" "+
         formulas.actual_temperature);
     workspace.Workspace_Answer.Change_Caption(final_answer);
     workspace.Workspace_Comments.Change_Caption("translated rule : "+this.toString());
     return true;
   }

   public void calculate_internal_strength(){
     double cdd = descriptor.conceptual_depth-relation.conceptual_depth;
     if (cdd<0.0) cdd=-cdd;
     cdd=100.0-cdd;
     double av = (descriptor.conceptual_depth+relation.conceptual_depth)/2.0;
     av=Math.pow(av,1.1);

     double shared_descriptor_term = 0.0;

     // see if the object corresponds to an object
     // if so, see if the descriptor is present (modulo slippages) in the
     // corresponding object

         workspace_object changed = null;
         // find changed object;
         for (int i=0; i<workspace.initial.objects.size(); i++){
            workspace_object wo=(workspace_object)workspace.initial.objects.elementAt(i);
            if (wo.changed) changed=wo;
         }

     if ((changed!=null)&&(changed.correspondence!=null)){

              workspace_object obj2 = changed.correspondence.obj2;
              Vector slippages = workspace.slippage_list();
                 slipnode s = descriptor;
                 s=Rule.apply_slippages(s,slippages);
                 if (obj2.has_description(s)) 
                   shared_descriptor_term = 100.0;
               else {
                  internal_strength = 0.0; return;
               }
     }

     double shared_descriptor_weight = Math.pow(((100.0-descriptor.conceptual_depth)/10.0),1.4);
 
     internal_strength = formulas.weighted_average(cdd,12,av,18,
                          shared_descriptor_term, shared_descriptor_weight);
     if (internal_strength>100.0) internal_strength=100.0;
   }

   public void calculate_external_strength(){
     external_strength=internal_strength;
   }
}
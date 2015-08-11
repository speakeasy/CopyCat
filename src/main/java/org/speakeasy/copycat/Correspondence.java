package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class Correspondence extends workspace_structure{

  workspace_object obj1, obj2;  // the objects between which it is built
  Vector concept_mapping_list = new Vector();
  Vector accessory_concept_mapping_list = new Vector();
  boolean flip_obj2;

  Correspondence(workspace_object o1, workspace_object o2, Vector map, boolean fo2){
    obj1 = o1; 
    obj2 = o2;
    concept_mapping_list = map;
    flip_obj2 = fo2;

     x1=(obj1.x1+obj1.x2)/2; x2=(obj2.x1+obj2.x2)/2;
     y1=obj1.y2; y2=obj2.y1;
     if ((o1.spans_string)&&(o2.spans_string)){
       x1=obj1.x1; x2=obj2.x1;
       y1=(obj1.y1+obj1.y2)/2; y2=(obj2.y1+obj2.y2)/2; 
     }
     foreground=Color.black;
     Redraw=true;
  }

  public boolean concept_mapping_present(concept_mapping cm){
    // returns true if a concept mapping of the same sort is
    // present in the workspace
    for (int x=0; x<concept_mapping_list.size(); x++){
      concept_mapping c = (concept_mapping)concept_mapping_list.elementAt(x);
      if ((c.description_type1==cm.description_type1)&&
          (c.description_type2==cm.description_type2)&&
          (c.descriptor1==cm.descriptor1)&&
          (c.descriptor2==cm.descriptor2)) return true;
    }
     return false;
  }

  public static boolean incompatible_concept_mappings(
      concept_mapping cm1, concept_mapping cm2){
   // Concept-mappings (a -> b) and (c -> d) are incompatible if a is 
   // related to c or if b is related to d, and the a -> b relationship is 
   // different from the c -> d relationship. E.g., rightmost -> leftmost
   // is incompatible with right -> right, since rightmost is linked 
   // to right, but the relationships (opposite and identity) are different.  
   // Notice that slipnet distances are not looked at, only slipnet links. This 
   // should be changed eventually.
    if (!((slipnet_formulas.related(cm1.descriptor1,cm2.descriptor1))||
       (slipnet_formulas.related(cm1.descriptor2,cm2.descriptor2))))
           return false;
    if ((cm1.label==null)||(cm2.label==null)) return false;
    if (!(cm1.label==cm2.label)) return true;
    return false;
  }

  public bond get_incompatible_bond(){
    bond bond1 = null;
    if (obj1.leftmost) bond1=obj1.right_bond;
    if (obj1.rightmost) bond1=obj1.left_bond;
    bond bond2 = null;
    if (obj2.leftmost) bond2=obj2.right_bond;
    if (obj2.rightmost) bond2=obj2.left_bond;
    if ((bond1!=null)&&(bond2!=null)){
       if ((bond1.direction_category!=null)&&
           (bond2.direction_category!=null)){
            concept_mapping cm = new concept_mapping(
                slipnet.direction_category,slipnet.direction_category,
                bond1.direction_category,bond2.direction_category,
                null, null);
            for (int x=0; x<concept_mapping_list.size(); x++){
              concept_mapping c = (concept_mapping)concept_mapping_list.elementAt(x);
              if (Correspondence.incompatible_concept_mappings(c,cm))
                   return bond2;
            }
       }
    }
    return null;
  }


  public static boolean supporting_concept_mappings(
         concept_mapping cm1, concept_mapping cm2){
   // Concept-mappings (a -> b) and (c -> d) support each other if a is related
    // to c and if b is related to d and the a -> b relationship is the same as the
    // c -> d relationship.  E.g., rightmost -> rightmost supports right -> right 
    // and leftmost -> leftmost.  Notice that slipnet distances are not looked 
    // at, only slipnet links.  This should be changed eventually.

    // If the two concept-mappings are the same, then return t.  This
    // means that letter->group supports letter->group, even though these
    // concept-mappings have no label.

    if ((cm1.descriptor1==cm2.descriptor1)&&(cm1.descriptor2==cm2.descriptor2)) return true;
    // if the descriptors are not related return false
    if (!(slipnet_formulas.related(cm1.descriptor1,cm2.descriptor1)||
         slipnet_formulas.related(cm1.descriptor2,cm2.descriptor2))) return false;
   if ((cm1.label==null)||(cm2.label==null)) return false;
   if ((cm1.label).equals(cm2.label)) return true;
   return false;
  }


   public boolean internally_coherent(){
     // returns true if there is any pair of relevant_distinguish
     // cms that support each other
     Vector cm_list = this.relevant_distinguishing_cms();
     for (int x=0; x<cm_list.size(); x++)
     for (int y=0; y<cm_list.size(); y++)
       if (x!=y){
         if (supporting_concept_mappings(
            (concept_mapping)cm_list.elementAt(x),
            (concept_mapping)cm_list.elementAt(y))) return true;
       }
     return false;
   }

   public void Draw(){
     Graphics g;
     g=parentarea.screen;
     if (!Redraw) return;
     Calculate_Coors();
     g.setColor(foreground);
     if ((obj1.spans_string)&&(obj2.spans_string)){
       int leftp = sx1; if (sx2<sx1) leftp = sx2;
       leftp = (parentarea.sx1+leftp)/2;
       g.drawLine(leftp,sy1,leftp,sy2);
       g.drawLine(leftp,sy1,sx1,sy1);
       g.drawLine(leftp,sy2,sx2,sy2);
     }
     else g.drawLine(sx1,sy1,sx2,sy2);
     Redraw=false;
   }


  public Vector distinguishing_concept_mappings(){
    Vector v = new Vector();
    for (int x=0; x<concept_mapping_list.size(); x++){
      concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
      if (cm.distinguishing()) v.addElement(cm);
    }
    return v;
  }

  public Vector relevant_distinguishing_cms(){
    Vector v = new Vector();
    for (int x=0; x<concept_mapping_list.size(); x++){
      concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
      if ((cm.relevant())&&(cm.distinguishing())) v.addElement(cm);
    }
    return v;
  }

  public Vector slippage_list(){
    Vector v = new Vector();
    for (int x=0; x<concept_mapping_list.size(); x++){
      concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
      if (cm.slippage()) v.addElement(cm);
    }

    for (int x=0; x<accessory_concept_mapping_list.size(); x++){
      concept_mapping cm = (concept_mapping)accessory_concept_mapping_list.elementAt(x);
      if (cm.slippage()) v.addElement(cm);
    }

    return v;
  }

  public void build_correspondence(){

    workspace.WorkspaceArea.AddObject(this,3);
    workspace.WorkspaceSmall.AddObject(this);
    workspace.workspace_structures.addElement(this);
    if (obj1.correspondence!=null) (obj1.correspondence).break_correspondence();
    if (obj2.correspondence!=null) (obj2.correspondence).break_correspondence();
    obj1.correspondence=this;
    obj2.correspondence=this;

     // add mappings to accessory-concept-mapping-list
     Vector v = this.relevant_distinguishing_cms();
     for (int x=0; x<v.size(); x++){
       concept_mapping cm = (concept_mapping) v.elementAt(x);
       if (cm.slippage())
         accessory_concept_mapping_list.addElement(cm.symmetric_version());
     }
     if ((obj1 instanceof group)&&(obj2 instanceof group)){
        Vector cmv = concept_mapping.get_concept_mapping_list(obj1,obj2,
           obj1.bond_descriptions,obj2.bond_descriptions);
        for (int x=0; x<cmv.size(); x++){
           concept_mapping cm = (concept_mapping) cmv.elementAt(x);
           accessory_concept_mapping_list.addElement(cm);
           if (cm.slippage())
             accessory_concept_mapping_list.addElement(cm.symmetric_version());
         }
     }
     Vector cml = this.concept_mapping_list;
        for (int x=0; x<cml.size(); x++){
           concept_mapping cm = (concept_mapping) cml.elementAt(x);
           if (cm.label!=null) cm.label.activation=100.0;
         }
  }

   public void break_correspondence(){
      workspace.WorkspaceArea.DeleteObject(this);
      workspace.WorkspaceSmall.DeleteObject(this);
      workspace.workspace_structures.removeElement(this);
      obj1.correspondence = null;
      obj2.correspondence = null;
      workspace.WorkspaceArea.Redraw = true;
   }


  public String toString(){
     String s;
     s="Correspondence between "+obj1+" and "+obj2;
     return s;
   }


  public void calculate_internal_strength(){
    // a function of how many concept-mapping there are, how strong they
    // are, and how much internal coherence there is among concept mappings
    Vector relevant_dcms = this.relevant_distinguishing_cms();
    if (relevant_dcms.size()==0) internal_strength=0;
    else {
       // average of the strengths of all
       double average_strength = 0.0;
       for (int x=0; x<relevant_dcms.size(); x++)
         average_strength+=((concept_mapping)relevant_dcms.elementAt(x)).strength();
       average_strength/=(double)(relevant_dcms.size());
       double num_of_concept_mappings = (double)relevant_dcms.size();
       double num_of_concept_mappings_factor;
       if (num_of_concept_mappings==1.0) num_of_concept_mappings_factor = 0.8;
       else if (num_of_concept_mappings==2.0) num_of_concept_mappings_factor = 1.2;
       else num_of_concept_mappings_factor = 1.6;
       
       double internal_coherence_factor;
       if (this.internally_coherent()) internal_coherence_factor = 2.5;
       else internal_coherence_factor = 1.0;
       internal_strength = average_strength*internal_coherence_factor*
                           num_of_concept_mappings_factor;
       if (internal_strength>100.0) internal_strength=100.0;
    }
  }

  static public boolean incompatible_correspondences(
           Correspondence c1, Correspondence c2){
     if (c1.obj1==c2.obj1) return true;
     if (c1.obj2==c2.obj2) return true;
     for (int x=0; x<c1.concept_mapping_list.size(); x++)
       for (int y=0; y<c2.concept_mapping_list.size(); y++){
        if (incompatible_concept_mappings(
                (concept_mapping)c1.concept_mapping_list.elementAt(x),
                (concept_mapping)c2.concept_mapping_list.elementAt(y))) return true;
     } 
     return false;
  }

  public Vector get_incompatible_correspondences(){
    // returns a list of all existing correspondences that are incompatible
    // with this proposed correspondence
    Vector incc = new Vector();
    for (int x=0; x<workspace.initial.objects.size(); x++){
       workspace_object w  = (workspace_object)workspace.initial.objects.elementAt(x);
       if (w.correspondence!=null){
         if (incompatible_correspondences(this,w.correspondence))
             incc.addElement(w.correspondence);
       }
    }
    
    return incc;
  }

  static public boolean supporting_correspondences(
           Correspondence c1, Correspondence c2){
       // Returns t if c1 supports c2, nil otherwise.  For now, c1 is 
       // defined to support c2 if c1 is not incompatible with c2, and 
       // has a concept-mapping that supports the concept-mappings of c2.
     if ((c1.obj1==c2.obj1)||(c1.obj2==c2.obj2)) return false;
     if (incompatible_correspondences(c1,c2)) return false;
     Vector dcm1 = c1.distinguishing_concept_mappings();
     Vector dcm2 = c2.distinguishing_concept_mappings();
     for (int x=0; x<dcm1.size(); x++)
        for (int y=0; y<dcm2.size(); y++){
          if (supporting_concept_mappings(
             (concept_mapping)dcm1.elementAt(x),
             (concept_mapping)dcm2.elementAt(y))) return true;

        }
      return false;
  }

  public double support(){
     // For now there are three levels of compatibility: 
      // supporting, not incompatible but not supporting, and incompatible.
      // This returns the sum of the strengths of other correspondences that
      // support this one (or 100, whichever is lower).  If one of the objects is the 
      // single letter in its string, then the support is 100.
    if ((obj1 instanceof letter)&&(obj1.spans_string)) return 100.0;
    if ((obj2 instanceof letter)&&(obj2.spans_string)) return 100.0;
    double support_sum = 0.0;
    for (int x=0; x<workspace.workspace_structures.size(); x++){
       workspace_structure ws = (workspace_structure)workspace.workspace_structures.elementAt(x);
       if (ws instanceof Correspondence)
          if ((ws!=this)&&(supporting_correspondences(this,(Correspondence)ws)))
             support_sum+=ws.total_strength;
    }
    if (support_sum>100.0) return 100.0;
    else return support_sum;
  }

  public void calculate_external_strength(){
    external_strength = this.support();
  }  
}
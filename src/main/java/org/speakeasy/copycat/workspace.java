package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;


class workspace {
  static boolean found_answer = false;
  static Area WorkspaceArea,WorkspaceSmall;
  static Frames MaximiseWorkspace;
  static Caption Workspace_Answer;
  static Caption codelets_run;
  static workspace_object changed_object = null;
  static Vector workspace_objects = new Vector();
  static Vector workspace_structures = new Vector();
  static String initial_string = "abc";
  static String modified_string = "abd";
  static String target_string = "ijk";
  static workspace_string initial, modified, target;
  static Caption Workspace_Comments;
  static Caption Workspace_Rule;
  static double total_unhappiness = 0.0;
  static double intra_string_unhappiness = 0.0;
  static double inter_string_unhappiness = 0.0;
  static Rule rule = null;
  static total_happiness_value tvh = new total_happiness_value();
  static temperature_value temp = new temperature_value();

  static int number_of_objects(){
    return workspace_objects.size();
  }

  static void Reset(){
    temp.Values = new Vector();
    tvh.Values = new Vector();
    found_answer = false;
    changed_object = null;
    WorkspaceArea.objects = new Vector();
    WorkspaceSmall.objects = new Vector();
    workspace_objects = new Vector();
    workspace_structures = new Vector();
    rule = null;
    initial = new workspace_string(initial_string,50,200,350,300);
    modified = new workspace_string(modified_string,650,200,950,300);
    target = new workspace_string(target_string,50,610,450,710);
    Workspace_Comments = new Caption(0,950,1000,1000,"initialised");
    Workspace_Rule = new Caption(200,50,800,100,"no rule");
    codelets_run = new Caption(550,900,950,950,"Codelets Run = 0");
    codelets_run.no_border = true;
    WorkspaceArea.AddObject(Workspace_Rule);
    WorkspaceArea.AddObject(Workspace_Comments);
    WorkspaceArea.AddObject(codelets_run);
    Caption c = new Caption(0,0,1000,50,"Workspace");
    c.background = GraphicsObject.Grey; WorkspaceArea.AddObject(c);
    Workspace_Answer = new Caption (650,610,950,710,"?");
    WorkspaceArea.AddObject(Workspace_Answer);
    WorkspaceSmall.AddObject(Workspace_Answer);

    Line l = new Line(500,660,600,660);
    WorkspaceArea.AddObject(l); WorkspaceSmall.AddObject(l);
    l = new Line(575,635,600,660);
    WorkspaceArea.AddObject(l); WorkspaceSmall.AddObject(l);
    l = new Line(575,685,600,660);
    WorkspaceArea.AddObject(l); WorkspaceSmall.AddObject(l);
    l = new Line(450,250,550,250);
    WorkspaceArea.AddObject(l); WorkspaceSmall.AddObject(l);
    l = new Line(525,225,550,250);
    WorkspaceArea.AddObject(l); WorkspaceSmall.AddObject(l);
    l = new Line(525,275,550,250);
    WorkspaceArea.AddObject(l); WorkspaceSmall.AddObject(l);


    c= new Caption(0,0,900,100,"Workspace");
    c.background = GraphicsObject.Grey;  WorkspaceSmall.AddObject(c);
    MaximiseWorkspace = new Frames(900,0,1000,100,WorkspaceSmall,
                                    GraphicsObject.Grey);
    icons.Maximise(MaximiseWorkspace);

  }

  static void Init(){
   WorkspaceArea = Areas.NewArea(5,5,745,661);
   WorkspaceSmall = Areas.NewArea(5,671,495,995);
   WorkspaceSmall.Visible = false;
   Reset();
  }

  static void check_visibility(){
    // checks the visibility of all descriptions attached to objects in the
    // workspace.  if the object is part of a group - the description is invisible
    for (int i=0; i<workspace_objects.size(); i++){
      workspace_object wo= (workspace_object)workspace_objects.elementAt(i);
      boolean vis = (wo.group==null);
      for (int x=0; x<wo.descriptions.size(); x++){
        description d = (description)wo.descriptions.elementAt(x);
        if (d.visible!=vis) {
           d.visible = vis;
           WorkspaceArea.Redraw = true;
        }

      }

    }
  }

  static void calculate_intra_string_unhappiness(){
     double isu=0;
     for (int i=0; i<workspace_objects.size(); i++){
      workspace_object wo = (workspace_object) workspace_objects.elementAt(i);
      isu+=wo.relative_importance*wo.intra_string_unhappiness;
     }
     isu/=2;
     if (isu>100.0) isu=100.0;
     //System.out.println("workspace intra string unhappiness = "+isu);
     intra_string_unhappiness=isu;
  }

  static void calculate_inter_string_unhappiness(){
     double isu=0;
     for (int i=0; i<workspace_objects.size(); i++){
      workspace_object wo = (workspace_object) workspace_objects.elementAt(i);
      isu+=wo.relative_importance*wo.inter_string_unhappiness;
     }
     isu/=2;
     if (isu>100.0) isu=100.0;
     //System.out.println("workspace inter string unhappiness = "+isu);
     inter_string_unhappiness=isu;
  }


  static void calculate_total_unhappiness(){
     double tu=0;
     for (int i=0; i<workspace_objects.size(); i++){
      workspace_object wo = (workspace_object) workspace_objects.elementAt(i);
      tu+=wo.relative_importance*wo.total_unhappiness;
      //System.out.println(wo.toString()+" rel importance = "+wo.relative_importance);
     }
     tu/=2;
     if (tu>100.0) tu=100.0;
     //System.out.println("workspace total string unhappiness = "+tu);
     total_unhappiness=tu;
  }

  static Vector slippage_list(){
    Vector sl = new Vector();
    if ((changed_object!=null)&&(changed_object.correspondence!=null)){
       Correspondence c = changed_object.correspondence;
         Vector slip_list = c.concept_mapping_list;
         for (int y=0; y<slip_list.size(); y++){
            concept_mapping cm = (concept_mapping)slip_list.elementAt(y);
            sl.addElement(cm);
         }
    }
    for (int x=0; x<workspace.initial.objects.size(); x++){
      workspace_object wo =(workspace_object)workspace.initial.objects.elementAt(x);
      if (wo.correspondence!=null){
         Correspondence c = wo.correspondence;
         Vector slip_list = c.slippage_list();
         for (int y=0; y<slip_list.size(); y++){
            concept_mapping cm = (concept_mapping)slip_list.elementAt(y);
            if (!(cm.in_vector(sl))) sl.addElement(cm);
         }
      }
    }
    return sl;
  }

  static int number_of_bonds(){
    //returns the number of bonds in the workspace
    int nob=0;
    for (int i=0; i<workspace_structures.size(); i++){
      workspace_structure ws=(workspace_structure)workspace_structures.elementAt(i);
    if (ws instanceof bond) nob++;
    }
    return nob;
  }
  public static boolean correspondence_present(Correspondence c){
    if ((c.obj1).correspondence==null) return false;
    if (((c.obj1).correspondence).obj2==c.obj2) return true;
    return false;
  }
}
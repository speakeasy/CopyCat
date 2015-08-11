package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;
import java.io.*;

class rungroup {

  Area SlipnetArea,MainControls,TempBar;
  Area MainArea, LeftArea, RightArea;
  Area MainVisible, LeftVisible, RightVisible;
  Area Bin;
  Frames ss,pp,Splay,st,PlayMode;
  boolean Drawing = true;
	static int last_update = 0;

  public static void myinit(){   
   GraphicsObject.InitColours();

   slipnet.Init();
   Temperature.Init();
   coderack.Init();
   workspace.Init();
   Graph.Init();
   GroupRun.Init();
  
   }


  public static void Update_Everything(){
     for (int i=0; i<workspace.workspace_structures.size(); i++){
        workspace_structure ws = (workspace_structure)workspace.workspace_structures.elementAt(i);
        ws.update_strength_value();
     }

     // update the the object values of all objects in the workspace
     for (int i=0; i<workspace.workspace_objects.size(); i++){
        workspace_object wo = (workspace_object)workspace.workspace_objects.elementAt(i);
        wo.update_object_value();
     }

     // update the relative importances of initial and target strings
     workspace.initial.update_relative_importance();
     workspace.target.update_relative_importance();

     // update the intra string unhappiness of initial and target strings
     workspace.initial.update_intra_string_unhappiness();
     workspace.target.update_intra_string_unhappiness();

     if (coderack.codelets_run>0){
        coderack.post_top_down_codelets();
        coderack.post_bottom_up_codelets();
     }
     slipnet.Update();
     workspace_formulas.update_temperature();
     Coderack_Pressure.Calculate_Pressures();
  }

  public static void mainloop(){
     if (coderack.codelets_run>=Temperature.clamp_time)
          Temperature.clamped = false;
    if (((coderack.codelets_run-last_update)>=slipnet.time_step_length)||
            (coderack.codelets_run==0))
                { Update_Everything(); last_update=coderack.codelets_run; }

 
       // if coderack is empty, clamp initially clamped slipnodes and
       // post initial_codelets;
       if (coderack.total_num_of_codelets()==0){
          coderack.post_initial_codelets();
       }

       // choose and run a codelet
       codelet cl=coderack.choose();
       if (cl!=null) cl.run();

  }

  public static void runtrial(){
    for (int num = 0; num<1000; num++) {
       slipnet.Reset();
       workspace.Reset();
       coderack.Reset();
       last_update=0;
     	 Temperature.clamped = false;
    	
       //slipnet.opposite.activation=100.0;
     
     random.setseed(num);

     // runs a whole trial
     while (!workspace.found_answer) mainloop();
     }
  }
  
  public static void main(String args[]){
    workspace.target_string="jffwww";
    workspace.initial_string = "abc";
    workspace.modified_string = "abd";
    myinit();
  	System.out.println(workspace.initial_string);
  	System.out.println(workspace.modified_string);
  	System.out.println(workspace.target_string);
  	
    slipnet.set_conceptual_depths(50.0);

    //workspace_formulas.clamp_temperature = true;
    //formulas.temperature = 100.0;
    //slipnet.opposite.clamp = true;
    //slipnet.left.clamp = true;
    //slipnet.right.clamp = true;
    //slipnet.predecessor.clamp = true;
    //slipnet.successor.clamp = true;
    //slipnet.predgrp.clamp = true;
    //slipnet.succgrp.clamp = true;
    //coderack.speed_up_bonds = true; 
   runtrial();
  }

}
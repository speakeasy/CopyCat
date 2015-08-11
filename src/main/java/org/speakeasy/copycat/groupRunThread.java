package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

public class groupRunThread extends Thread {
  Applet myapp; // what is updated?
	long oldtime = 0;
	Date d = new Date();
  int last_update=0; 
  GroupRun currentGroup = null;
	
	
	// variables needed to run it 

  public groupRunThread(Applet a,GroupRun g){
    myapp = a;
  	currentGroup = g;
  }

  public void Update_Everything(){
     Graph.GraphFrame.Redraw = true;
     Graph.GraphMinFrame.Redraw = true;
     // update the strength values of all structures in the workspace
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


  public void mainloop(){
     if (coderack.codelets_run>=Temperature.clamp_time)
          Temperature.clamped = false;
     if (!workspace.found_answer){
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
  }


  public void run(){

    long oldtime = d.getTime();

    while (true){
      slipnet.Reset();
      workspace.Reset();
      coderack.Reset();
      GraphicsObject.RedrawAll=true;
      last_update=0;
     	 Temperature.clamped = false;
    	
    	
      random.setseed((currentGroup.RunStrings).size());
      System.out.println("seed = " + ((currentGroup.RunStrings).size()));

     // runs a whole trial
      while (!workspace.found_answer) {
      	mainloop();
        long time = d.getTime();
        	if ((time-oldtime)>200){
    	      try {
    	         sleep(50);
    	      }catch (InterruptedException e){}
    	  	  oldtime = time;
    	    }	
      	
      }
      currentGroup.RunStrings.addElement(workspace.rule.final_answer);
      currentGroup.Recalculate(); // now add the run
      currentGroup.PlayMode = currentGroup.play;
      currentGroup.play.Selected = true;
      currentGroup.stop.Selected = false;
    	
    	
      GroupRun.GroupRunArea.Visible = true;
      GroupRun.GroupRunArea.Redraw = true;    	
    	
      myapp.repaint();
    }
  }
}
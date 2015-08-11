package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;
import java.io.*;



class copycatapp extends Applet {


  AlgorithmThread T = null;  // the thread that runs the redraw routine
  Image appletImg;
  Graphics appletG;
  Color Grey;
  static int Width=0, Height=0;
  boolean initialised = false;
  GroupRun currentGroup = null;
	groupRunThread grt = null;
  boolean UpdateScreen = true;
  int Old_x,Old_y, Drag_x,Drag_y;
  Font Drag_font;
  String Drag_text;

  Area SlipnetArea,MainControls,TempBar;
  Area MainArea, LeftArea, RightArea;
  Area MainVisible, LeftVisible, RightVisible;
  Area Bin;
  Frames ss,pp,Splay,st,PlayMode;
  boolean Drawing = true;
  int last_update=0; 
	
	int codeletsBeforeUpdate = 1;
	int tempCount = 0;
	int pauseAt = 0;

  public void start(){
    repaint();
  }

  public void myinit(){   
   GraphicsObject.InitColours();
   appletImg = createImage(size().width, size().height);
   appletG = appletImg.getGraphics();
   Width = size().width; Height = size().height;

   Grey= new Color(200,200,200);
   //SlipnetArea = Areas.NewArea(5,5,745,595);
   slipnet.Init();
   Temperature.Init();
   MainControls = Areas.NewArea(755,5,995,45);
   Bin = Areas.NewArea(755,505,870,661);
   MyImage i = new MyImage(0,0,1000,1000,GraphicsObject.bin);
   Bin.AddObject(i);
   coderack.Init();
   workspace.Init();
   Graph.Init();
   GroupRun.Init();
  
   //f1= new Frames(950,0,1000,50,SlipnetArea);
   ss = new Frames(750,0,1000,1000,MainControls,Grey);
   pp = new Frames(500,0,750,1000,MainControls,Grey);
   Splay = new Frames(250,0,500,1000,MainControls,Grey); 
   st = new Frames(0,0,250,1000,MainControls,Grey); st.Selected = true;
   PlayMode = st;

   //s1=new SlipNode(100,100,130,140); SlipnetArea.AddObject(s1);
   //s2=new SlipNode(200,100,230,140); SlipnetArea.AddObject(s2); s2.ConceptualDepth=80;

   icons.Single_Step(ss);
   icons.Pause(pp);
   icons.Play(Splay);
   icons.Stop(st);
   GraphicsObject.RedrawAll=true;

   MainArea = workspace.WorkspaceArea;  MainVisible = MainArea;
   LeftArea = slipnet.SlipnetSmall;     LeftVisible = LeftArea;
   RightArea = coderack.CoderackSmall;  RightVisible = RightArea;

   initialised=true;  
   }

  public void ShowMainArea(){
    if (MainVisible!=MainArea){
       MainVisible.Visible = false;
       MainVisible = MainArea;
    }
    if (MainArea==coderack.CoderackArea) coderack.update_captions();
    MainArea.Visible = true;
    MainArea.Redraw = true;
  }

  public void ShowAllAreas(){
    if (MainArea!=MainVisible){
       MainVisible.Visible = false;
       MainVisible = MainArea;
    }
    MainArea.Visible = true;
    MainArea.Redraw = true;
    if (LeftArea!=LeftVisible){
       LeftVisible.Visible = false;
       LeftVisible = LeftArea;
    }
    LeftArea.Visible = true;
    RightArea.Visible = true;
    if (RightArea!=RightVisible){
       RightVisible.Visible = false;
       RightVisible = RightArea;
    }
    LeftArea.Redraw = true;
    RightArea.Redraw = true;
  }

  public void MaximiseWindow(Area selected){
    MainArea.Visible = false;
    selected.Visible = false;
    MainVisible.Visible = false;

    Area minversion = slipnet.SlipnetSmall;
    if (MainArea==workspace.WorkspaceArea) minversion = workspace.WorkspaceSmall;
    if (MainArea==coderack.CoderackArea) minversion = coderack.CoderackSmall;
    minversion.Shift_Right = selected.Shift_Right;
    if (minversion.Shift_Right) RightArea = minversion;
    else LeftArea = minversion;
    if (minversion==workspace.WorkspaceSmall) minversion.Resize=true;

    Area maxversion = slipnet.SlipnetArea;
    if (selected==workspace.WorkspaceSmall) maxversion = workspace.WorkspaceArea;
    if (selected==coderack.CoderackSmall) maxversion = coderack.CoderackArea;
    MainArea = maxversion;
    if (maxversion==workspace.WorkspaceArea) maxversion.Resize = true;

    ShowAllAreas();
  }

  public void init() {
    repaint();
  }

  public void stop() {
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
  	 //System.out.println("seed = " + random.rndseed);
  	 //System.out.println("update = " + last_update);
  	if (coderack.codelets_run<5){
  	   // print out cricial info
  	   //System.out.println("seed = " + random.rndseed);
  		 // System.out.println("number of codelets = "+coderack.codelets.size());
 		
  	}
  	
     if (coderack.codelets_run>=Temperature.clamp_time)
          Temperature.clamped = false;
     if (!workspace.found_answer){
       UpdateScreen = true;
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
       if (cl!=null) {
       	   cl.run();
       	   //System.out.println(cl.name+":"+cl.urgency);
       }

     }  
  	else {
       if (T!=null) {
         T.stop(); 
         while (T.isAlive());
         T = null;
       }
  		
  	}

  }

  public void runtrial(){
     boolean running = (currentGroup.PlayMode==currentGroup.play);
       slipnet.Reset();
       workspace.Reset();
       coderack.Reset();
       GraphicsObject.RedrawAll=true;
       last_update=0;
     	 Temperature.clamped = false;
  	

     random.setseed((currentGroup.RunStrings).size());
     System.out.println("seed = " + ((currentGroup.RunStrings).size()));

     // runs a whole trial
     while (!workspace.found_answer) mainloop();
  	
     currentGroup.RunStrings.addElement(workspace.rule.final_answer);
     currentGroup.Recalculate();
       MainArea.Visible = false;
       GroupRun.GroupRunArea.Visible = true;
       GroupRun.GroupRunArea.Redraw = true;

     if (running){
        currentGroup.PlayMode = currentGroup.play;
        currentGroup.play.Selected = true;
        currentGroup.stop.Selected = false;
     }
     try {
       T.sleep(1000);
     } catch (InterruptedException e) {  }
  }

  public void paint(Graphics g){
    int wid = size().width;
    int ht = size().height;
    GraphicsObject.half_width = wid/2;
    if (!initialised) myinit();
    if ((wid!=Width)||(ht!=Height)){
    	tempCount=0;
      Width=wid; Height=ht;
      if (appletG!=null) appletG.dispose();
      appletImg=createImage(wid,ht);
      appletG = appletImg.getGraphics();
      Areas.ResizeAllAreas();
      //Areas.Draw(g,Width,Height);
    	//appletG.drawImage(
	
    }
    if (appletG!=null){
    if (DragInfo.Drag==false){
      if (PlayMode==Splay) mainloop();
      else if (slipnet.Splay.Selected){
        slipnet.Update();
        Graph.GraphIcon.Redraw=true;
      }
    	
    	// draw only if updated at right time
    	tempCount--;
    	if (!(PlayMode==Splay)) tempCount=0;
    	if (tempCount<=0){
    	  tempCount = codeletsBeforeUpdate;
     	 Areas.Draw(appletG,Width,Height);
        g.drawImage(appletImg,0,0,this);  		
    	}
   		
      GraphicsObject.RedrawAll=false;
    }
    if (DragInfo.Drag==true){
      if ((Old_x!=Drag_x)||(Old_y!=Drag_y)){
        g.drawImage(appletImg,0,0,this);
        g.setColor(Color.black);
        g.setFont(Drag_font);
        g.drawString(Drag_text,Drag_x-DragInfo.x1,Drag_y-DragInfo.y1);
        Old_x=Drag_x; Old_y=Drag_y;
        }
    }
    }
    UpdateScreen = false;
    if ((currentGroup.GroupRunArea.Visible)&&
        (GroupRun.play.Selected)) {
         //runtrial();
         }
  	if ((PlayMode!=pp)&&(coderack.codelets_run==pauseAt)&&
  		   (pauseAt>0)){
  		 pressPause();
  	}
  	
  	if ((workspace.found_answer)&&(T!=null)){
        T.stop(); 
        while (T.isAlive());
        T = null;
 	      tempCount = 0;
        repaint(); 		
  	}
  }
 
	public void pressPause(){
     MainControls.Redraw = true;
     PlayMode.Selected = false;
		 PlayMode = pp;
      if (T!=null) {
         T.stop(); 
         while (T.isAlive());
         T = null;
       }
  		PlayMode.Selected = true;

 	    tempCount = 0;
      repaint();		
	}
	
	public void pressStop(){
		
     MainControls.Redraw = true;
     PlayMode.Selected = false;
		 PlayMode = st;
      if (T!=null) {
         T.stop(); 
         while (T.isAlive());
         T = null;
       }
  		PlayMode.Selected = true;

		
       slipnet.Reset();
       workspace.Reset();
       coderack.Reset();
       last_update=0;
     	 Temperature.clamped = false;
       GraphicsObject.RedrawAll=true;
	    tempCount = 0;
      repaint();	
		
	}
	
  public void pressControl(int x, int y){
     // if the main controls have been pressed
     MainControls.Redraw = true;
     PlayMode.Selected = false;
     GraphicsObject g;    
     g=Areas.FindObject(x,y);
     if (g==st) {
       PlayMode = st;
       if (T!=null) {
         T.stop(); 
         while (T.isAlive());
         T = null;
       }
     }
  	
     if (g==Splay) {
         PlayMode = Splay;
         if (T==null){
           T = new AlgorithmThread(this);
           T.start();
         }
     }
     if (g==pp) {
        pressPause();
     }
     if (g==ss) {
        PlayMode = ss;
        PlayMode = pp;
        mainloop();
     }
  	
     PlayMode.Selected = true;

     if (PlayMode==st){
       slipnet.Reset();
       workspace.Reset();
       coderack.Reset();
       last_update=0;
     	 Temperature.clamped = false;
       GraphicsObject.RedrawAll=true;

     }
  	 tempCount = 0;
     repaint();
  }

  public boolean mouseDown(Event evt, int x, int y){
    GraphicsObject g = null;
    Area selarea;
    selarea=Areas.FindArea(x,y);
    Drag_x=x; Drag_y=y;
    if (selarea==MainControls) pressControl(x,y);
    if (selarea==slipnet.SlipnetArea){
       slipnet.Click(x,y);
       Graph.GraphIcon.Redraw=true;
       }
    if (selarea==Graph.GraphIcon){
       MainVisible.Visible = false;
       MainArea.Visible = false;
       Graph.GraphArea.Visible = true;
       Graph.GraphArea.Redraw = true;
       MainVisible = Graph.GraphArea;
    }
    if (selarea==GroupRun.GroupRunArea){
       // check to see if a problem has been clicked on
       int pos = currentGroup.find_problem(x,y);
       if (pos>=0){
          // set up that problem to run
          GroupRun.GroupRunArea.Visible = false;
          MainArea.Visible = true;
          MainArea.Redraw = true;
          MainVisible = MainArea;

          PlayMode.Selected = false; PlayMode = st; st.Selected =  true;
          random.setseed(pos);
          System.out.println("seed = " + pos);
          slipnet.Reset();
          workspace.Reset();
          coderack.Reset();
       	  last_update = 0;
       	  Temperature.clamped = false;
          GraphicsObject.RedrawAll=true;
       }
    }


    if (selarea==slipnet.SlipnetAct) slipnet.SlipnetAct_Click(x,y);

    g = null;
    if (selarea!=null) g=Areas.FindObject(x,y);
    if (g==Graph.GraphMinimise){
       ShowMainArea();
       Graph.GraphArea.Visible = false;
    }
    if (g==coderack.MinimiseInfoArea){
       RightVisible.Visible = false;
       RightVisible = RightArea;
       RightArea.Visible = true;
       RightArea.Redraw = true;
    }

    if (g==slipnet.SlipnetKeyMinimise){
       RightVisible.Visible = false;
       RightVisible = RightArea;
       RightArea.Visible = true;
       RightArea.Redraw = true;
    }

    if (g==slipnet.MaximiseSlipnet) MaximiseWindow(selarea);
    if (g==workspace.MaximiseWorkspace) MaximiseWindow(selarea);
    if (g==coderack.MaximiseCoderack){
       coderack.update_captions();
       MaximiseWindow(selarea);
    }

    if (g==GroupRun.minimise){
       GroupRun.GroupRunArea.Visible = false;
       ShowMainArea();
    }
    if ((g==GroupRun.stop)||(g==GroupRun.play)||(g==GroupRun.single_step)){
       GroupRun.GroupRunArea.Redraw = true;

       GroupRun.PlayMode.Selected = false;
       if (g==GroupRun.single_step){
         GroupRun.stop.Selected = true;
         runtrial();
       }  
       else {
        g.Selected = true;
        GroupRun.PlayMode = (Frames)g;
        if (g==GroupRun.stop){
          currentGroup.PlayMode = currentGroup.stop;
          currentGroup.play.Selected = false;
          currentGroup.stop.Selected = true;
        	
           if (grt!=null) {
              grt.stop(); 
              while (grt.isAlive());
              grt = null;
           }
        }
        else if (g==GroupRun.play){
          currentGroup.PlayMode = currentGroup.play;
          currentGroup.play.Selected = true;
          currentGroup.stop.Selected = false;
        	
          if (grt!=null){
     	       grt.stop();
     	    while (grt.isAlive());
          }	
          grt = new groupRunThread(this,currentGroup);
          grt.start();
        }

       }
    }
 
     if ((slipnet.SelectedAction==slipnet.ActDrop)&&
         (g instanceof slipnode_minimised)){
        slipnode sna = ((slipnode_minimised)g).dad;
        if (sna.activation<50.0) {
           sna.activation=100.0;
           sna.buffer=100.0; }
        else { sna.activation=0.0; sna.buffer=0.0; }
        sna.Redraw=true; 
        g.Redraw = true; }

     if ((slipnet.SelectedAction==slipnet.ActClamp)&&
         (g instanceof slipnode_minimised)){
        slipnode snb = ((slipnode_minimised)g).dad;
        if (snb.clamp) snb.clamp = false; else snb.clamp=true;
        snb.Redraw=true;
        g.Redraw=true;
     }
   
    DragInfo.DragObject=null;

    if (selarea==Temperature.TempBar) g=workspace.temp;

    if ((g!=null)&&(slipnet.SelectedAction==slipnet.ActArrow)){
      DragInfo.DragObject=g;
      DragInfo.x1=(g.sx2-g.sx1)/2;
      DragInfo.y1=(g.sy2-g.sy1)/2;
      if (g instanceof slipnode){
          slipnode s = (slipnode)g;
          DragInfo.x1-=s.xoff;
          Drag_font = s.currfont; Drag_text = s.short_name;
      }
      else if (g instanceof Coderack_Pressure){
          Coderack_Pressure c= (Coderack_Pressure)g;
          DragInfo.x1-=c.xoff;
          Drag_font = c.currfont; Drag_text = c.text;  
      }
      else if (g instanceof slipnode_minimised){
          slipnode_minimised s = (slipnode_minimised)g;
          DragInfo.x1-=s.xoff;
          Drag_font = s.currfont; Drag_text = s.dad.short_name;
      }
      else if ((g instanceof Caption)&&(selarea==Graph.GraphArea)){
          Caption c = (Caption)g;
          DragInfo.x1-=c.xoff;
          Drag_font = c.currfont; Drag_text = c.text;
      }
      else if (g==workspace.temp){
          Caption c = Temperature.heading;
          DragInfo.x1=(c.sx2-c.sx1)/2;
          DragInfo.y1=(c.sy2-c.sy1)/2;
          DragInfo.x1-=c.xoff;
          Drag_font = c.currfont; Drag_text = c.text;     
      }

      else DragInfo.DragObject = null;
    }
    DragInfo.Drag = false;
  	tempCount = 0;
    repaint();
    return true;
  }

  public void update(Graphics g){
     paint(g);  
  }

  public void ShowGroupRun(GroupRun g){
       currentGroup = g;
       MainArea.Visible = false;
       MainVisible.Visible = false;
       g.Recalculate();
       g.GroupRunArea.Visible = true;
       g.GroupRunArea.Redraw = true;
       MainVisible = g.GroupRunArea;
  	   tempCount = 0;
       repaint();
  }

  public void ShowCoderackInfo(){
     RightVisible.Visible = false;
 	   RightArea.Visible = false;  	
     coderack.CoderackInfoArea.Visible = true;
     RightVisible = coderack.CoderackInfoArea;
     RightVisible.Redraw = true;
  }

 public void ShowSlipnetKey(){
     RightVisible.Visible = false;
 	   RightArea.Visible = false;
     slipnet.SlipnetKey.Visible = true;
     RightVisible = slipnet.SlipnetKey;
     RightVisible.Redraw = true;
  }


  public boolean mouseDrag(Event evt, int x, int y) {
    Drag_x=x; Drag_y=y;
    if (DragInfo.DragObject!=null) {
    	DragInfo.Drag = true;
    	tempCount = 0;
      repaint();
    }
    return true;
  }

  public boolean mouseUp(Event evt, int x, int y){
    if (DragInfo.Drag==true) {
      Area selarea=Areas.FindArea(x,y);
      Drag_x=x; Drag_y=y;
      if (selarea==Graph.GraphIcon) Graph.AddObject(DragInfo.DragObject);
      if ((selarea==Bin)&&(DragInfo.DragObject instanceof Caption))
         {  Graph.RemoveObject(DragInfo.DragObject);
            slipnet.SlipnetSmall.Redraw = true;
         }
    	tempCount = 0;
      repaint();	
    }
    DragInfo.Drag = false;
    return true;
 }


}
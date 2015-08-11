package org.speakeasy.copycat;

import java.applet.*;
import java.awt.*;
import java.util.*;

class TestWindow extends Frame {
  Applet parent;

  GroupRun mygroup;

  MenuBar MainMenu;
  Vector gruns = new Vector();  //group runs
  Menu FileMenu;
  MenuItem FileNewItem,FileLoadGroup,FileQuitItem;

  Menu RunMenu;
	MenuItem changeSpeed;
	MenuItem pauseAt;
  MenuItem SetSeed;
  MenuItem GroupRunItem;
  MenuItem ChangeFont;

  Menu SpecialMenu;
  CheckboxMenuItem ClampTemperature;
  CheckboxMenuItem RemoveTerracedScan;
  CheckboxMenuItem RemoveBreakerCodelets;

  Menu SlipnetMenu;
  CheckboxMenuItem EqualizeConceptualDepth;
  CheckboxMenuItem RemoveSpreadingActivation;
  CheckboxMenuItem RemoveActivationJump;
  CheckboxMenuItem Clampbdoa;

  Menu VisibleMenu;
  MenuItem ShowCoderackInfo;
  MenuItem ShowSlipnetKey;
  CheckboxMenuItem ShowProposedStructures;

  copycatapp theapplet;
  
TestWindow(Applet prt) {
   super("Copycat");
   parent = prt;
   theapplet = new copycatapp();

   MainMenu = new MenuBar();
   setMenuBar(MainMenu);
 
   FileMenu = new Menu("File");
   MainMenu.add(FileMenu);
   FileNewItem = new MenuItem("New"); FileMenu.add(FileNewItem);
   FileLoadGroup = new MenuItem("Load Group Run"); FileMenu.add(FileLoadGroup);
   FileQuitItem = new MenuItem("Exit"); FileMenu.add(FileQuitItem);

   RunMenu = new Menu("Run");
   MainMenu.add(RunMenu);
   changeSpeed = new MenuItem("Change Program Speed");
   RunMenu.add(changeSpeed);
   pauseAt = new MenuItem("Pause At");
   RunMenu.add(pauseAt);
	 SetSeed = new MenuItem("Set Random Seed");
   RunMenu.add(SetSeed);
   GroupRunItem = new MenuItem("Group Run");
   RunMenu.add(GroupRunItem);


   VisibleMenu = new Menu("Visible");
   MainMenu.add(VisibleMenu);
   ShowCoderackInfo = new MenuItem("Show Codelet Information");
   VisibleMenu.add(ShowCoderackInfo);
   ShowSlipnetKey = new MenuItem("Show Slipnet Key");
   VisibleMenu.add(ShowSlipnetKey);
   ShowProposedStructures = new CheckboxMenuItem("Show Proposed Structures");
   VisibleMenu.add(ShowProposedStructures);

   SpecialMenu = new Menu("Special");
   MainMenu.add(SpecialMenu);
   ClampTemperature = new CheckboxMenuItem("clamp temperature");
   SpecialMenu.add(ClampTemperature);
   RemoveTerracedScan = new CheckboxMenuItem("remove parallel terraced scan");
   SpecialMenu.add(RemoveTerracedScan);
   RemoveBreakerCodelets = new CheckboxMenuItem("remove breaker codelets");
   SpecialMenu.add(RemoveBreakerCodelets);
   ChangeFont = new MenuItem("Change Font Size");
   SpecialMenu.add(ChangeFont);


   SlipnetMenu = new Menu("Slipnet");
   MainMenu.add(SlipnetMenu);
   EqualizeConceptualDepth = new CheckboxMenuItem("equalise conceptual depths");
   SlipnetMenu.add(EqualizeConceptualDepth);
   RemoveSpreadingActivation = new CheckboxMenuItem("remove spreading activation");
   SlipnetMenu.add(RemoveSpreadingActivation);
   RemoveActivationJump = new CheckboxMenuItem("remove probabilistic activation jump");
   SlipnetMenu.add(RemoveActivationJump);
   Clampbdoa = new CheckboxMenuItem("clamp bond degree of association");
   SlipnetMenu.add(Clampbdoa);

   add("Center",theapplet);
   reshape(0,0,400,300);
   show();
}

public boolean handleEvent(Event e){
 switch (e.id) {
   case Event.WINDOW_DESTROY:
   dispose();
   return true;
 default:
   return super.handleEvent(e);
  }
 }

 public boolean action(Event evt, Object arg){
   Frame dFrame = ComponentUtil.getFrame(this);
 
   if (evt.target == FileLoadGroup){

     GetValue gv = new GetValue(dFrame,"Load Group Run",
       "please enter the filename:",
       "","ijk","ID:loadgroup");
     
   }

   if ("ID:loadgroup".equals(arg)){
     GroupRun g = GroupRun.LoadGroupRun(GetValue.CurrentValue);
     if (GetValue.OK) {
        theapplet.ShowGroupRun(g);
        MenuItem m = new MenuItem(g.name);
        RunMenu.add(m);
        gruns.addElement(m);
     }
   }
   if (evt.target == FileQuitItem)
           dispose();


   if (evt.target == GroupRunItem) {
      mygroup = new GroupRun();
      String name = "group run "+String.valueOf(GroupRun.GroupRuns.size()+1);
      GetValue g = new GetValue(dFrame,"New Group Run",
        "please enter the name of this run",
        "",name,"ID:newgrouprun");
   }
   if ("ID:newgrouprun".equals(arg)){
      mygroup.name = GetValue.CurrentValue;
      GroupRun.GroupRuns.addElement(mygroup);
      theapplet.ShowGroupRun(mygroup);
      theapplet.repaint();
      MenuItem m = new MenuItem(mygroup.name);
      RunMenu.add(m);
      gruns.addElement(m);
   }
 
   if (gruns.contains(evt.target)){
      // show the specified group
      int pos = gruns.indexOf(evt.target);
      GroupRun g = (GroupRun)GroupRun.GroupRuns.elementAt(pos);
   	  // set the strings accordingly
   	  workspace.initial_string = g.initial;
  	  workspace.modified_string = g.modified;
  	  workspace.target_string = g.target;
  	
      theapplet.ShowGroupRun(g);
   }
 	
 	
   if (evt.target == SetSeed) {
     String name = String.valueOf(random.rndseed);
     GetValue g = new GetValue(dFrame,"Set Random Seed",
       "please enter the new Random Seed",
       "",name,"ID:setseed");
   }
   if ("ID:setseed".equals(arg)) {
      Integer ival = Integer.valueOf(GetValue.CurrentValue);
      random.rndseed = ival.intValue();      
   }

  if (evt.target == changeSpeed) {
     String name = String.valueOf(theapplet.codeletsBeforeUpdate);
     GetValue g = new GetValue(dFrame,"Set number of codelets per screen update",
       "please enter the new value (1=default)",
       "",name,"ID:setspeed");
   }
   if ("ID:setspeed".equals(arg)) {
      Integer ival = Integer.valueOf(GetValue.CurrentValue);
   	  int i = ival.intValue();
   	  if (i<1) i = 1;
      theapplet.codeletsBeforeUpdate = i;      
   }

  if (evt.target == pauseAt) {
     String name = String.valueOf(theapplet.pauseAt);
     GetValue g = new GetValue(dFrame,"Set program pause position",
       "please enter the new value (0=default)",
       "",name,"ID:pauseat");
   }
   if ("ID:pauseat".equals(arg)) {
      Integer ival = Integer.valueOf(GetValue.CurrentValue);
   	  int i = ival.intValue();
   	  if (i<0) i = 0;
      theapplet.pauseAt = i;      
   }
 	
 	
   if (evt.target == ChangeFont) {
     String name = String.valueOf(Caption.FontScale);
     GetValue g = new GetValue(dFrame,"Change Font Scale",
       "please enter the new Scale",
       "",name,"ID:setscale");
   }
   if ("ID:setscale".equals(arg)) {
      Double ival = Double.valueOf(GetValue.CurrentValue);
      Caption.FontScale = ival.doubleValue();      
      copycatapp.Width = 1;
   }


   if (evt.target == ClampTemperature) {
     boolean ct = ClampTemperature.getState();
     ClampTemperature.setState(false);
     workspace_formulas.clamp_temperature = false;
     if (ct==true){
       String name = String.valueOf(formulas.temperature);
       GetValue g = new GetValue(dFrame,"Clamp Temperature",
         "please enter value at which you want the temperature clamped",
         "",name,"ID:clamptemp");
     }
   }

   if ("ID:clamptemp".equals(arg)){
     ClampTemperature.setState(true);
     Double ival = Double.valueOf(GetValue.CurrentValue);
     formulas.temperature = ival.doubleValue();
     workspace_formulas.clamp_temperature = true;
   }

   if (evt.target == FileNewItem){
      GetStrings g = new GetStrings(dFrame,"ID:new");
      if (g.OK){
       slipnet.Reset();
       workspace.Reset();
       coderack.Reset();
       theapplet.last_update=0;
    	 Temperature.clamped = false;      	
       theapplet.PlayMode.Selected = false;
       theapplet.PlayMode = theapplet.st;
       theapplet.PlayMode.Selected = true;
       GraphicsObject.RedrawAll=true;
      }
   }
   if ("ID:new".equals(arg)) { 
   	 theapplet.pressStop();
   }

   if (evt.target == RemoveBreakerCodelets)
      coderack.remove_breaker_codelets = RemoveBreakerCodelets.getState();
   if (evt.target == Clampbdoa)
      slipnode.clamp_bdoa = Clampbdoa.getState();    

   if (evt.target == RemoveTerracedScan)
      coderack.remove_terraced_scan = RemoveTerracedScan.getState();
   if (evt.target == RemoveSpreadingActivation)
      slipnet.remove_spreading_activation = RemoveSpreadingActivation.getState();

   if (evt.target == RemoveActivationJump)
      slipnet.remove_activation_jump = RemoveActivationJump.getState();
   if (evt.target == EqualizeConceptualDepth){
      boolean v = EqualizeConceptualDepth.getState();
      EqualizeConceptualDepth.setState(false);
      if (v){
       GetValue g = new GetValue(dFrame,"Set Conceptual Depths",
         "please enter value at which you want all the conceptual depths",
         "","50","ID:eqcondepth");

      }
      else slipnet.reset_conceptual_depths();
   }

   if ("ID:eqcondepth".equals(arg)) {
     EqualizeConceptualDepth.setState(true);
     Double ival = Double.valueOf(GetValue.CurrentValue);
     slipnet.set_conceptual_depths(ival.doubleValue());
   }


   if (evt.target == ShowCoderackInfo)
      theapplet.ShowCoderackInfo();
   if (evt.target == ShowSlipnetKey)
      theapplet.ShowSlipnetKey();
   if (evt.target == ShowProposedStructures){
      GraphicsObject.draw_proposed = ShowProposedStructures.getState();
      workspace.WorkspaceArea.Redraw = true;
   }
   return true;
 }
}
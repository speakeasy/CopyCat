package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;

class slipnet {
  static boolean remove_spreading_activation = false;
  static boolean remove_activation_jump = false;
  static Vector initially_clamped_slipnodes = new Vector();
  static Vector sliplinks = new Vector();
  static Vector slipnodes = new Vector();
  static Vector bond_facets = new Vector(); // a list of descriptor types that bonds can be formed from
  static slipnode slipnet_numbers[];
  static int time_step_length = 15;
  static int number_of_updates = 0;

   static Area SlipnetArea,SlipnetSmall,SlipnetAct,SlipnetKey;
   static Frames MaximiseSlipnet,SlipnetKeyMinimise;
   static boolean SlipnetMaximised = false;
   static Frames ActArrow;
   static Frames ActClamp,ActDrop,ActMagnify;
   static Frames Sstep,Splay,Sstop,Spause,Sspeed;
   static Frames PlayMode;
   static Color Grey;
   static Caption Decay,Spread,Clamp,Active,Length,Slabel;
   static GraphicsObject SelectedAction;

   // important slipnodes that were initialised
  static slipnode slipnet_letters[], bond_facet;
  static slipnode string_position_category, leftmost, rightmost;
  static slipnode middle, letter_category, bond_category;
  static slipnode single, object_category, letter, group, whole;
  static slipnode sameness, identity, opposite, group_category, direction_category;
  static slipnode left,right,successor,predecessor;
  static slipnode predgrp, succgrp, samegrp,length;
  static slipnode first, last;

   static void reset_conceptual_depths(){
     for (int i=0; i<slipnodes.size(); i++){
       slipnode ob=(slipnode)slipnodes.elementAt(i);
       ob.conceptual_depth = ob.usual_conceptual_depth;
     }
   }


   static void set_conceptual_depths(double val){
     for (int i=0; i<slipnodes.size(); i++){
       slipnode ob=(slipnode)slipnodes.elementAt(i);
       ob.conceptual_depth = val;
     }
   }


   static void Reset(){
     number_of_updates=0;
     for (int i=0; i<slipnodes.size(); i++){
       slipnode ob=(slipnode)slipnodes.elementAt(i);
       ob.buffer = 0.0;
       ob.activation = 0.0;
       if (((int)(ob.activation/10.0))!=((int)(ob.old_activation/10.0)))
         {  ob.Redraw=true;  ob.child.Redraw = true; }
       ob.Values = new Vector();

       }
     // clamp initially clamped slipnodes
     for (int x=0; x<initially_clamped_slipnodes.size(); x++){
        slipnode s = (slipnode) initially_clamped_slipnodes.elementAt(x);
        s.clamp=true; s.activation=100.0;
        s.Redraw = true;
     }
   }

   static void Init(){
     int x;
     slipnode s;
     slipnet_link sl;
     Grey= new Color(200,200,200);


     SlipnetArea = Areas.NewArea(5,5,745,661); SlipnetArea.Visible = false;
     SlipnetSmall = Areas.NewArea(5,671,495,995);
     Caption cp = new Caption(0,0,900,100,"Slipnet"); cp.background=Grey;
       SlipnetSmall.AddObject(cp);
     MaximiseSlipnet = new Frames(900,0,1000,100,SlipnetSmall,Grey);
     icons.Maximise(MaximiseSlipnet);


     Sstep= new Frames(950,0,1000,50,SlipnetArea,Grey);
     Spause = new Frames(900,0,950,50,SlipnetArea,Grey);
     Splay = new Frames(850,0,900,50,SlipnetArea,Grey);
     Sstop = new Frames(800,0,850,50,SlipnetArea,Grey); Sstop.Selected=true;
     PlayMode = Sstop;
     Caption c = new Caption(0,0,800,50,"Slipnet"); c.background = Grey;
     SlipnetArea.AddObject(c);
     Slabel = c;

     icons.Single_Step(Sstep);
     icons.Pause(Spause);
     icons.Play(Splay);
     icons.Stop(Sstop);


     // set up the slipnet actions box
     SlipnetAct = Areas.NewArea(760,60,865,490);
     ActArrow = new Frames(0,0,1000,333,SlipnetAct);
       icons.Arrow(ActArrow); ActArrow.Selected=true;
       SelectedAction = ActArrow;

     ActClamp = new Frames (0,333,1000,666,SlipnetAct);
     icons.Clamp(ActClamp);

     ActDrop = new Frames (0,666,1000,1000,SlipnetAct);
     icons.EyeDrop(ActDrop);

     init_slipnet();
     Reset();

     SlipnetKey = Areas.NewArea(505,671,995,995);
   	 SlipnetKey.Visible = false;
     SlipnetKeyMinimise = new Frames(566,0,666,100,SlipnetKey,Grey);
   	 SlipnetKeyMinimise.Visible = false;
     icons.Minimise(SlipnetKeyMinimise);
     cp = new Caption(0,0,566,100,"Slipnet Key"); cp.background=Grey;
     SlipnetKey.AddObject(cp);
     int xpos=0, xend=333, ypos=100;
     boolean ok=true;

     for (int i=0; i<slipnodes.size(); i++){
       slipnode ob=(slipnode)slipnodes.elementAt(i);
       ok=true;
       for (x=0; x<5; x++)
          if (ob==slipnet_numbers[x]) ok=false;
       for (x=0; x<26; x++)
          if (ob==slipnet_letters[x]) ok=false;
       if (ok){
          c = new Caption(xpos,ypos,xend,ypos+100,
                     ob.short_name+" = "+ob.pname);  
          c.Filled = false;
          SlipnetKey.AddObject(c);
          ypos+=100;
          if (ypos>=1000){
             ypos=100;
             xpos+=333; xend = xpos+333;
             if (xpos>=666) ypos=0;
          }
       }
     }
   }

   static void Click(int x, int y){
     GraphicsObject ob;
     ob=Areas.FindObject(x,y);
      if ((ob==Splay)||(ob==Spause)||(ob==Sstop)){
         PlayMode.Selected=false; PlayMode.Redraw=true;
         PlayMode=(Frames) ob;
         ob.Selected=true; ob.Redraw=true;
         if (ob==Sstop) Reset();
         return;
      }
     if (ob==Sstep) { Update(); return; }

     if ((ob==ActDrop)||(ob==ActClamp)||(ob==ActArrow)){
        SelectedAction.Selected=false; SelectedAction.Redraw=true;
        SelectedAction=(Frames) ob;
        ob.Selected=true;ob.Redraw=true;
        return;
     }

     if ((SelectedAction==ActDrop)&&(ob instanceof slipnode)){
        slipnode sna = (slipnode) ob;
        if (sna.activation<50.0) {
           sna.activation=100.0;
           sna.buffer=100.0; }
        else { sna.activation=0.0; sna.buffer=0.0; }
        sna.Redraw=true; }

     if ((SelectedAction==ActClamp)&&(ob instanceof slipnode)){
        slipnode snb = (slipnode) ob;
        if (snb.clamp) snb.clamp = false; else snb.clamp=true;
        snb.Redraw=true;
     }
   }

   static void SlipnetAct_Click(int x, int y){
      GraphicsObject ob;
      ob=Areas.FindObject(x,y);
      if (SelectedAction!=null) SelectedAction.Selected=false;
      SelectedAction=ob;
      ob.Selected = true;
      SlipnetAct.Redraw = true;
   }


   static void Update(){
     // this procedure updates the slipnet
     int i,x;
     slipnode ob;

     number_of_updates++;
     // unclamp initially clamped slipnodes if #of updates = 50

     if (number_of_updates==50){
       for (x=0; x<initially_clamped_slipnodes.size(); x++){
          slipnode s = (slipnode) initially_clamped_slipnodes.elementAt(x);
          s.clamp=false;
          s.Redraw = true;
       }
     }

     // for all nodes set old_activation to activation
     for (i=0; i<slipnodes.size(); i++){
       ob=(slipnode)slipnodes.elementAt(i);
       ob.old_activation=ob.activation;
       ob.buffer-=ob.activation*((100.0-ob.conceptual_depth)/100.0);
       if (ob==successor){
         //System.out.println("activation ="+ob.activation+" buffer="+ob.buffer);
         //System.out.println("number of nodes = "+slipnodes.size());
       }

       }


   // spreading activation
   // for all incomming links, if the activation of the sending node = 100
   // add the percentage of its activation to activation buffer
     slipnet_link sl;
     double s;
   if (!remove_spreading_activation){
     for (i=0; i<slipnodes.size(); i++){
       ob =(slipnode)slipnodes.elementAt(i);
          for (x=0; x<ob.outgoing_links.size(); x++){
          sl = (slipnet_link)ob.outgoing_links.elementAt(x);
          if (ob.activation==100.0){
              (sl.to_node).buffer+=sl.intrinsic_degree_of_association();
          }
       }
     }  
   }
   // for all nodes add the activation activation_buffer
   // if activation>100 or clamp=true, activation=100
     for (i=0; i<slipnodes.size(); i++){
       ob=(slipnode)slipnodes.elementAt(i);
       if (!(ob.clamp)) ob.activation+=ob.buffer;
       if (ob.activation>100.0) ob.activation=100.0;
       if (ob.activation<0.0) ob.activation = 0.0;
       }


   // check for probabablistic jump to 100%
     double act;
   if (!remove_activation_jump){
     for (i=0; i<slipnodes.size(); i++){
       ob=(slipnode)slipnodes.elementAt(i);
       act=ob.activation/100.0; act=act*act*act;
       if ((ob.activation>55.0)&&(random.rnd()<act)&&
           (!(ob.clamp))) ob.activation=100.0;
       }      
   }


   // check for redraw; and reset buffer values to 0
     for (i=0; i<slipnodes.size(); i++){
       ob=(slipnode)slipnodes.elementAt(i);
       ob.buffer = 0.0;
       if (((int)(ob.activation/10.0))!=((int)(ob.old_activation/10.0)))
         {  ob.Redraw=true;  ob.child.Redraw = true; }
       ob.Values.addElement(new Double(ob.activation));

       }

  }


  static slipnode add_slipnode(double cd,String pn, String sn){
    int x1,y1;
    x1=(int)(random.rnd()*900.0)+50;
    y1=(int)(random.rnd()*900.0)+50;

    slipnode s = new slipnode(x1,y1,cd,pn,sn);
    slipnodes.addElement(s);
    SlipnetArea.AddObject(s);
    return s;
  }

  static slipnode add_slipnode(int x, int y, double cd,String pn, String sn){

    slipnode s = new slipnode(x*25,y*25,cd,pn,sn);
    slipnodes.addElement(s);
    SlipnetArea.AddObject(s);
    return s;
  }


  static slipnode add_slipnode(double cd,String pn, String sn, double len){
    int x1,y1;
    x1=(int)(random.rnd()*900.0)+50;
    y1=(int)(random.rnd()*900.0)+50;

    slipnode s = new slipnode(x1,y1,cd,pn,sn,len);
    SlipnetArea.AddObject(s);
    slipnodes.addElement(s);

    return s;
  }

  static slipnode add_slipnode(int x, int y,double cd,String pn, String sn, double len){


    slipnode s = new slipnode(x*25,y*25,cd,pn,sn,len);
    SlipnetArea.AddObject(s);
    slipnodes.addElement(s);

    return s;
  }

  static slipnet_link add_category_link(slipnode fr, slipnode to, slipnode lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.category_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     to.incoming_links.addElement(nl);
     return nl;
  }

  static slipnet_link add_category_link(slipnode fr, slipnode to, double lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.category_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_instance_link(slipnode fr, slipnode to, slipnode lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.instance_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_instance_link(slipnode fr, slipnode to, double lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.instance_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_property_link(slipnode fr, slipnode to, slipnode lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.has_property_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_property_link(slipnode fr, slipnode to, double lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.has_property_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_slip_link(slipnode fr, slipnode to, slipnode lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     nl.slip_link = true;
     sliplinks.addElement(nl);
     fr.lateral_slip_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_slip_link(slipnode fr, slipnode to, double lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     nl.slip_link = true;
     sliplinks.addElement(nl);
     fr.lateral_slip_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_nonslip_link(slipnode fr, slipnode to, slipnode lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.lateral_nonslip_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }

  static slipnet_link add_nonslip_link(slipnode fr, slipnode to, double lab){
     slipnet_link nl = new slipnet_link(fr,to,lab);
     sliplinks.addElement(nl);
     fr.lateral_nonslip_links.addElement(nl);
     to.incoming_links.addElement(nl);
     SlipnetArea.AddObject(nl);
     return nl;
  }


  static void init_slipnet(){
    // initialises all nodes in the slipnet

    // initialises letter nodes
    slipnet_letters = new slipnode[26];
    char[] myChar = {'a'};
    int x1=2, y1=25;
    String letname;
    for (int i=0; i<=25; i++){
      myChar[0]=(char)(i+65);
      letname = new String(myChar);
      slipnet_letters[i]=add_slipnode(x1,y1,10.0,letname,letname);
      if ((x1==2)&&(y1>5)) y1-=3;
      else if (x1<38) x1+=3;
      else y1+=3;

    }

    // initialises slipnet numbers
    slipnet_numbers = new slipnode[5];
    x1=26; y1=38;
    for (int i=0; i<=4; i++){
      myChar[0]=(char)(i+49);
      letname = new String(myChar);
      slipnet_numbers[i]=add_slipnode(x1,y1,30.0,letname,letname);
      if ((x1==2)&&(y1>2)) y1-=3;
      else if (x1<38) x1+=3;
      else y1+=3;
    }

    // string positions
    leftmost = add_slipnode(17,18,40.0,"leftmost","lm");
    rightmost = add_slipnode(27,18,40.0,"rightmost","rm");
    middle = add_slipnode(27,26,40.0,"middle","md");
    single = add_slipnode(33,26,40.0,"single","sl");
    whole = add_slipnode(30,26,40.0,"whole","wh");

    // alphabetic positions
    first = add_slipnode(19,15,60.0,"first","fs");
    last = add_slipnode(25,15,60.0,"last","ls");

    // directions
    left = add_slipnode(17,22,40.0,"left","lf");
    left.codelets.addElement("top-down-bond-scout--direction");
    left.codelets.addElement("top-down-group-scout--direction");
    right = add_slipnode(27,22,40.0,"right","rt");
    right.codelets.addElement("top-down-bond-scout--direction");
    right.codelets.addElement("top-down-group-scout--direction");

    // bond types
    predecessor = add_slipnode(14,38,50.0,"predecessor","pd",60.0);
    predecessor.codelets.addElement("top-down-bond-scout--category");

    successor = add_slipnode(14,33,50.0,"successor","sc",60.0);
    successor.codelets.addElement("top-down-bond-scout--category");
    sameness = add_slipnode(10,29,80.0,"sameness","sm",0.0);
    sameness.codelets.addElement("top-down-bond-scout--category");

    // group types
    predgrp = add_slipnode(20,38,50.0,"predecessor group","pg");
    predgrp.codelets.addElement("top-down-group-scout--category");
    succgrp = add_slipnode(20,33,50.0,"successor group","sg");
    succgrp.codelets.addElement("top-down-group-scout--category");
    samegrp = add_slipnode(10,25,80.0,"sameness group","smg");
    samegrp.codelets.addElement("top-down-group-scout--category");

    // other relations
    identity = add_slipnode(2,30,90.0,"identity","id",0.0);
    opposite = add_slipnode(6,30,90.0,"opposite","op",80.0);

    // objects
    letter = add_slipnode(2,38,20.0,"letter","l");
    group = add_slipnode(6,38,80.0,"group","g");

    // categories
    letter_category = add_slipnode(22,9,30.0,"letter category","lc");
    string_position_category = add_slipnode(30,21,70.0,"string position","spc");
    string_position_category.codelets.addElement("top-down-description-scout");
    slipnode alphabetic_position_category = add_slipnode(22,12,80.0,"alphabetic position","apc");
    alphabetic_position_category.codelets.addElement("top-down-description-scout");
    direction_category = add_slipnode(22,25,70.0,"direction category","dc");
    bond_category = add_slipnode(10,33,80.0,"bond category","bc");
    group_category = add_slipnode(17,29,80.0,"group category","gpc");
    length = add_slipnode(36,32,60.0,"length","len");
    object_category = add_slipnode(4,34,90.0,"object category","obc");
    bond_facet = add_slipnode(36,26,90.0,"bond facet","bf");

    // specify the descriptor types that bonds can form between
    bond_facets.addElement(letter_category);
    bond_facets.addElement(length);

    // add initially_clamped_slipnodes
    initially_clamped_slipnodes.addElement(letter_category);
    letter_category.clamp=true;
    initially_clamped_slipnodes.addElement(string_position_category);
    string_position_category.clamp=true;


    //***************************************************************
    //   initialise links between nodes

    // **************   successor and predecessor links
    slipnet_link sl;

    // letters
    for (int i=0; i<=24; i++){
      sl = add_nonslip_link(slipnet_letters[i], slipnet_letters[i+1], successor);
      sl = add_nonslip_link(slipnet_letters[i+1], slipnet_letters[i], predecessor);
    }
    // numbers
    for (int i=0; i<=3; i++){
      sl = add_nonslip_link(slipnet_numbers[i], slipnet_numbers[i+1], successor);
      sl = add_nonslip_link(slipnet_numbers[i+1], slipnet_numbers[i], predecessor);
    }

    // ************** letter category links
    for (int i=0; i<=25; i++){
      sl = add_category_link(slipnet_letters[i], letter_category, letter_category.conceptual_depth-slipnet_letters[i].conceptual_depth);
      sl = add_instance_link(letter_category, slipnet_letters[i], 97.0);
    }
    sl=add_category_link(samegrp,letter_category,50.0);

    // *************** length links
    for (int i=0; i<=4; i++){
      sl = add_category_link(slipnet_numbers[i], length, length.conceptual_depth-slipnet_numbers[i].conceptual_depth);
      sl = add_instance_link(length, slipnet_numbers[i], 100.0);
    }
      sl = add_nonslip_link(predgrp, length, 95.0);
      sl = add_nonslip_link(succgrp, length, 95.0);
      sl = add_nonslip_link(samegrp, length, 95.0);

    // *************** opposite links
      sl = add_slip_link(first, last, opposite);
      sl = add_slip_link(last, first, opposite);
      sl = add_slip_link(leftmost, rightmost, opposite);
      sl = add_slip_link(rightmost,leftmost, opposite);
      sl = add_slip_link(left,right, opposite);
      sl = add_slip_link(right,left, opposite);
      sl = add_slip_link(successor,predecessor, opposite);
      sl = add_slip_link(predecessor,successor, opposite);
      sl = add_slip_link(succgrp,predgrp, opposite);
      sl = add_slip_link(predgrp,succgrp, opposite);

    // ***************** has property links
      sl = add_property_link(slipnet_letters[0],first, 75.0);
      sl = add_property_link(slipnet_letters[25],last, 75.0);

    // ******************* object category links
      sl = add_category_link(letter,object_category, object_category.conceptual_depth-letter.conceptual_depth);
      sl = add_instance_link(object_category, letter, 100.0);
      sl = add_category_link(group,object_category, object_category.conceptual_depth-group.conceptual_depth);
      sl = add_instance_link(object_category, group, 100.0);

    // string position links
      sl = add_category_link(leftmost, string_position_category,string_position_category.conceptual_depth-leftmost.conceptual_depth);
      sl = add_instance_link(string_position_category, leftmost,100.0);
      sl = add_category_link(rightmost, string_position_category,string_position_category.conceptual_depth-rightmost.conceptual_depth);
      sl = add_instance_link(string_position_category, rightmost,100.0);
      sl = add_category_link(middle, string_position_category,string_position_category.conceptual_depth-middle.conceptual_depth);
      sl = add_instance_link(string_position_category, middle,100.0);
      sl = add_category_link(single, string_position_category,string_position_category.conceptual_depth-single.conceptual_depth);
      sl = add_instance_link(string_position_category, single,100.0);
      sl = add_category_link(whole, string_position_category,string_position_category.conceptual_depth-whole.conceptual_depth);
      sl = add_instance_link(string_position_category, whole,100.0);

    // alphabetic position category
      sl = add_category_link(first, alphabetic_position_category,alphabetic_position_category.conceptual_depth-first.conceptual_depth);
      sl = add_instance_link(alphabetic_position_category,first,100.0);
      sl = add_category_link(last, alphabetic_position_category,alphabetic_position_category.conceptual_depth-last.conceptual_depth);
      sl = add_instance_link(alphabetic_position_category,last,100.0);
      
    // direction-category links
      sl = add_category_link(left, direction_category,direction_category.conceptual_depth-left.conceptual_depth);
      sl = add_instance_link(direction_category,left,100.0);
      sl = add_category_link(right, direction_category,direction_category.conceptual_depth-right.conceptual_depth);
      sl = add_instance_link(direction_category,right,100.0);

    // bond-category links
      sl = add_category_link(predecessor, bond_category,bond_category.conceptual_depth-predecessor.conceptual_depth);
      sl = add_instance_link(bond_category,predecessor,100.0);
      sl = add_category_link(successor, bond_category,bond_category.conceptual_depth-successor.conceptual_depth);
      sl = add_instance_link(bond_category,successor,100.0);
      sl = add_category_link(sameness, bond_category,bond_category.conceptual_depth-sameness.conceptual_depth);
      sl = add_instance_link(bond_category,sameness,100.0);
    
    // group-category links
      sl = add_category_link(predgrp, group_category,group_category.conceptual_depth-predgrp.conceptual_depth);
      sl = add_instance_link(group_category,predgrp,100.0);
      sl = add_category_link(succgrp, group_category,group_category.conceptual_depth-succgrp.conceptual_depth);
      sl = add_instance_link(group_category,succgrp,100.0);
      sl = add_category_link(samegrp, group_category,group_category.conceptual_depth-samegrp.conceptual_depth);
      sl = add_instance_link(group_category,samegrp,100.0);

   // associated-group links
      sl = add_nonslip_link(sameness,samegrp,group_category);
      sl.fixed_length=30.0;
      sl = add_nonslip_link(successor,succgrp,group_category);
      sl.fixed_length=60.0;
      sl = add_nonslip_link(predecessor,predgrp,group_category);
      sl.fixed_length=60.0;

   // associated bond links
      sl = add_nonslip_link(samegrp,sameness,bond_category);
      sl.fixed_length=90.0;
      sl = add_nonslip_link(succgrp,successor,bond_category);
      sl.fixed_length=90.0;
      sl = add_nonslip_link(predgrp,predecessor,bond_category);
      sl.fixed_length=90.0;

   // bond facet links
     sl = add_category_link(letter_category,bond_facet,bond_facet.conceptual_depth-letter_category.conceptual_depth);
     sl = add_instance_link(bond_facet,letter_category,100.0);
     sl = add_category_link(length,bond_facet,bond_facet.conceptual_depth-length.conceptual_depth);
     sl = add_instance_link(bond_facet,length,100.0);

   // letter category links
     sl = add_slip_link(letter_category,length,95.0);
     sl = add_slip_link(length,letter_category,95.0);
     
   // letter group links
     sl = add_slip_link(letter,group,90.0);
     sl = add_slip_link(group,letter,90.0);

   // direction-position, direction-neighbor, position-neghbor links
     sl = add_nonslip_link(left,leftmost,90.0);
     sl = add_nonslip_link(leftmost,left,90.0);
     sl = add_nonslip_link(right,leftmost,100.0);
     sl = add_nonslip_link(leftmost,right,100.0);
     sl = add_nonslip_link(right,rightmost,90.0);
     sl = add_nonslip_link(rightmost,right,90.0);
     sl = add_nonslip_link(left,rightmost,100.0);
     sl = add_nonslip_link(rightmost,left,100.0);
     sl = add_nonslip_link(leftmost,first,100.0);
     sl = add_nonslip_link(first,leftmost,100.0);
     sl = add_nonslip_link(rightmost,first,100.0);
     sl = add_nonslip_link(first,rightmost,100.0);
     sl = add_nonslip_link(leftmost,last,100.0);
     sl = add_nonslip_link(last,leftmost,100.0);
     sl = add_nonslip_link(rightmost,last,100.0);
     sl = add_nonslip_link(last,rightmost,100.0);

     // other links
     sl = add_slip_link(single,whole,90.0);
     sl = add_slip_link(whole, single,90.0);

  }

/*  public static void main (String args[]){
    init_slipnet();
  } */
}
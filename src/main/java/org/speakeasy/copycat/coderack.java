package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;


class coderack {
  static boolean speed_up_bonds = false;
  static Area CoderackArea,CoderackSmall,CoderackInfoArea;
  static Frames MaximiseCoderack,MinimiseInfoArea;
  static Caption[] Codelet_Captions;
  static Caption[] CodeletInfo_Captions;
  static Caption Codelet_Run;
  static Vector codelets = new Vector();
  static int codelets_run = 0;
  static int number_of_bins = 7;
  static boolean remove_breaker_codelets = false;
  static boolean remove_terraced_scan = false;

  // define the codelet types
  static int get_urgency_bin(double urgency){
    //int bin = (int)(urgency*random.rnd());
    int bin = (int)urgency;
    bin*=number_of_bins; bin/=100;
    if (bin>=number_of_bins) bin=number_of_bins-1;
    return (bin+1);
  }

  static int total_num_of_codelets(){
     return codelets.size();
  }
  static codelet choose_old_codelet(){
    // selects an old codelet to remove from the coderack
    // more likely to select lower urgency codelets

     if (codelets.size()==0) return null;

     double[] urgency_values = new double[codelets.size()];

     double urgsum = 0.0;
     for (int i=0; i<codelets.size(); i++){
       codelet c = (codelet)codelets.elementAt(i);
       double urg = ((double)(coderack.codelets_run-c.time_stamp))*(double)(7.5-c.urgency);
       urgency_values[i]=urg;
       urgsum+=urg;
     }

     codelet chc = null;
     double chosen = random.rnd()*urgsum;
     urgsum=0.0;
     for (int i=0; i<codelets.size(); i++){
       urgsum+=urgency_values[i];
       if ((chc==null)&&(urgsum>=chosen)) chc=(codelet)codelets.elementAt(i);
     }
     if (chc==null) chc = (codelet)codelets.elementAt(0);
     ////System.out.println("removing "+chc.name+" urgency "+chc.urgency);
    return chc;
  }

  static codelet choose(){
     // selects a codelet to run and removes it from the coderack
     if (codelets.size()==0) return null;

     double[] urgency_values = new double[codelets.size()];

     double scale = ((100.0-formulas.temperature)+10.0)/15.0;
     double urgsum = 0.0;
     for (int i=0; i<codelets.size(); i++){
       codelet c = (codelet)codelets.elementAt(i);
       double urg = Math.pow((double)(c.urgency),scale);
       //scale*(double)(c.urgency);
       urgency_values[i]=urg;
       urgsum+=urg;
     }

     codelet chc = null;
     double chosen = random.rnd()*urgsum;
     urgsum=0.0;
     for (int i=0; i<codelets.size(); i++){
       urgsum+=urgency_values[i];
       if ((chc==null)&&(urgsum>=chosen)) {
       	   chc=(codelet)codelets.elementAt(i);
       //	if (codelets_run<5)
       	//	 System.out.println("chosen : "+i);
       }	
     }
     if (chc==null) chc = (codelet)codelets.elementAt(0);


     remove_codelet(chc);
     //System.out.println("chosen codelet "+chc.name+" urgency = "+chc.urgency);
     //update_captions();
     return chc;
  }

  static void update_captions(){
    // relabels the captions if the order has changed
    codelet tc;
    int e = codelets.size();
    for (int i=0; i<e; i++){
      tc=(codelet)codelets.elementAt(i);
      if (tc.position!=i){
        tc.position=i;
        Codelet_Captions[i].text=tc.name+":"+String.valueOf(tc.urgency);
        Codelet_Captions[i].Redraw=true;
        Codelet_Captions[i].foreground = new Color((tc.urgency-1)*42,0,0);
      }
    }
    if (e!=100) { Codelet_Captions[e].text="-"; 
                  Codelet_Captions[e].foreground = Color.black;
                  Codelet_Captions[e].Redraw=true; }
  }

  static void remove_codelet(codelet c){
    // removes the codelet from the coderack
    codelets.removeElement(c);
    Coderack_Pressure.RemoveCodelet(c);
    if (CoderackArea.Visible) update_captions();

    // removes the corresponding structure from the WorkspaceArea
     if ((c.name.equals("correspondence-strength-tester"))||
         (c.name.equals("correspondence-builder"))){
        Correspondence cr = (Correspondence)c.arguments.elementAt(0); 
        workspace.WorkspaceArea.DeleteObject(cr); 
     } 
     if ((c.name.equals("bond-strength-tester"))||
          (c.name.equals("bond-builder"))){
         bond b = (bond)c.arguments.elementAt(0);
         workspace.WorkspaceArea.DeleteObject(b); 
     }
     if ((c.name.equals("group-strength-tester"))||
          (c.name.equals("group-builder"))) {
         // update strength value of the group
         group g = (group)c.arguments.elementAt(0);
         workspace.WorkspaceArea.DeleteObject(g);
     }
  }

  static void Post(codelet c){
    codelets.addElement(c);
    Coderack_Pressure.AddCodelet(c);
    if (codelets.size()>100){
      codelet nc = choose_old_codelet();
      remove_codelet(nc);
    } // removes a codelet if there are too many
    if (CoderackArea.Visible) update_captions();
  }

  static void post_initial_codelets(){
    for (int i=0; i<codelet.initial_codelets.size(); i++){
      String ci;
      ci = (String)codelet.initial_codelets.elementAt(i);
      for (int x=0; x<workspace.number_of_objects(); x++){
        codelet c = new codelet(ci,1);
        Post(c);
        c = new codelet(ci,1);
        Post(c);

      }
    }

  }

  static double get_post_codelet_probability(String structure_category){
    double pcp=0.0;
    if (structure_category.equals("breaker")) return 1.0;
    if (structure_category.indexOf("description")>-1){
      double d=formulas.temperature;
      pcp=(d/100.0)*(d/100.0);
    }
     else pcp = workspace.intra_string_unhappiness/100.0;
    if (structure_category.indexOf("correspondence")>-1)
       pcp = workspace.inter_string_unhappiness/100.0;

    if (structure_category.indexOf("replacement")>-1){
         if ((workspace_formulas.unreplaced_objects()).size()>0)    
         return 1.0;
         else return 0.0;
       }
    if (structure_category.indexOf("rule")>-1){
         if (workspace.rule==null) return 1.0;
         return workspace.rule.total_weakness()/100.0;
       }
    if (structure_category.indexOf("translated")>-1){
         if (workspace.rule==null) return 0.0;
         return 1.0; 
    }
    return pcp;
  }

  static int rough_num_of_objects(String structure_category){
    int n=0; // number of objects of the specified type in the workspace
    // bond -> unrelated_objects
    // group -> ungrouped_objects
    // replacement -> unreplaced
    // correspondence-> uncorresponding
    
    if (structure_category.indexOf("bond")>-1) n=(workspace_formulas.unrelated_objects()).size();    
    if (structure_category.indexOf("group")>-1) n=(workspace_formulas.ungrouped_objects()).size();    
    if (structure_category.indexOf("replacement")>-1) n=(workspace_formulas.unreplaced_objects()).size();    
    if (structure_category.indexOf("correspondence")>-1) n=(workspace_formulas.uncorresponding_objects()).size();  

    double d=(double) n;  
    if (d<formulas.blur(2.0)) return 1;
    if (d<formulas.blur(4.0)) return 2;
    return 3;
  }

  static int get_num_codelets_to_post(String structure_category){
    if (structure_category.equals("breaker")) return 1;
    if (structure_category.indexOf("description")>-1) return 1;
    if (structure_category.indexOf("translator")>-1){
       if (workspace.rule==null) return 0;
       return 1;
    }
    if (structure_category.indexOf("rule")>-1) return 2;
    if ((structure_category.indexOf("group")>-1)&&(workspace.number_of_bonds()==0)) return 0;
    if ((structure_category.indexOf("replacement")>-1)&&(workspace.rule!=null)) return 0;
    int x=rough_num_of_objects(structure_category);
    return x;
  }

  static void post_top_down_codelets(){
    for (int i=0; i<slipnet.slipnodes.size(); i++){
      slipnode s = (slipnode)slipnet.slipnodes.elementAt(i);
      if (s.activation==100.0){
         for (int x=0; x<s.codelets.size(); x++){
           String st = (String)s.codelets.elementAt(x);
           double prob = get_post_codelet_probability(st);
           int num = get_num_codelets_to_post(st);
             for (int t=1; t<=num; t++){
               if (random.rnd()<prob){
               codelet c = new codelet(st,get_urgency_bin(s.activation*s.conceptual_depth/100.0));
               c.arguments.addElement(s);
               Post(c);
             }            
           }
         }
      }

    }

  }

  static void get_bottom_up_codelets(String st){
    
    double prob = get_post_codelet_probability(st);
    int num = get_num_codelets_to_post(st);
    if ((speed_up_bonds)&&(st.indexOf("bond")>-1)) num*=3;
    if ((speed_up_bonds)&&(st.indexOf("group")>-1)) num*=3;
    for (int t=1; t<=num; t++){
         int urgency = 3;
         if (st.equals("breaker")) urgency = 1;
         if ((formulas.temperature<25.0)&&
             (st.indexOf("translator")>-1)) urgency=5;
         if (random.rnd()<prob){
           codelet c = new codelet(st,urgency);
           Post(c);
         }
    }
  }
  static void post_bottom_up_codelets(){
    get_bottom_up_codelets("bottom-up-description-scout");
    get_bottom_up_codelets("bottom-up-bond-scout");
    get_bottom_up_codelets("group-scout--whole-string");
    get_bottom_up_codelets("bottom-up-correspondence-scout");
    get_bottom_up_codelets("important-object-correspondence-scout");
    get_bottom_up_codelets("replacement-finder");
    get_bottom_up_codelets("rule-scout");
    get_bottom_up_codelets("rule-translator");
    if (!remove_breaker_codelets) get_bottom_up_codelets("breaker");
  }

  static void propose_bond(workspace_object fromob,workspace_object toob,
     slipnode bond_category, slipnode bond_facet,
     slipnode from_descriptor, slipnode to_descriptor,codelet orig){
     bond_facet.buffer=100.0;
     from_descriptor.buffer=100.0;
     to_descriptor.buffer=100.0;
     bond nb = new bond(fromob,toob,bond_category,bond_facet,from_descriptor,to_descriptor);
     if (!remove_terraced_scan) workspace.WorkspaceArea.AddObject(nb,1);
     double urgency = bond_category.bond_degree_of_association();
     codelet nc = new codelet("bond-strength-tester",get_urgency_bin(urgency));
     nc.arguments.addElement(nb);
     nc.Pressure_Type = orig.Pressure_Type;
     if (coderack.remove_terraced_scan) nc.run();
     else coderack.Post(nc);
  }

  static void propose_group(Vector object_list,Vector bond_list, 
                slipnode group_category, slipnode direction_category,
                slipnode bond_facet, codelet orig){
     slipnode bond_category = slipnet_formulas.get_related_node(group_category,slipnet.bond_category);
     bond_category.buffer=100.0;
     if (direction_category!=null) direction_category.buffer=100.0;
     workspace_string string = ((workspace_object)object_list.elementAt(0)).string;
     group ng = new group(string,group_category,direction_category,bond_facet,object_list, bond_list);

     if (!remove_terraced_scan) workspace.WorkspaceArea.AddObject(ng,1);

     double urgency = bond_category.bond_degree_of_association();
     codelet nc = new codelet("group-strength-tester",get_urgency_bin(urgency));
     nc.arguments.addElement(ng);
     nc.Pressure_Type = orig.Pressure_Type;
     if (coderack.remove_terraced_scan) nc.run();
      else coderack.Post(nc);
  }

  static void propose_description(workspace_object ob,
           slipnode description_type, slipnode descriptor, codelet orig){
     description d = new description(ob,description_type,descriptor);
     descriptor.buffer=100.0;
     double urgency = description_type.activation;

     codelet ncd = new codelet("description-strength-tester",get_urgency_bin(urgency));
     ncd.arguments.addElement(d);
     ncd.Pressure_Type = orig.Pressure_Type;
     if (coderack.remove_terraced_scan) ncd.run();
     else coderack.Post(ncd);

  }

  static void propose_correspondence(workspace_object obj1,
         workspace_object obj2, Vector concept_mapping_list,
           boolean flip_obj2,codelet orig){

     Correspondence nc = new Correspondence(obj1,obj2,concept_mapping_list,flip_obj2);
     if (!remove_terraced_scan) workspace.WorkspaceArea.AddObject(nc,1);
     // activate some descriptions
     for (int x=0; x<concept_mapping_list.size(); x++){
        concept_mapping cm = (concept_mapping)concept_mapping_list.elementAt(x);
        cm.description_type1.buffer=100.0;
        cm.descriptor1.buffer=100.0;
        cm.description_type2.buffer=100.0;
        cm.descriptor2.buffer=100.0;
     }
     double urgency = 0.0;
     Vector dcm = nc.distinguishing_concept_mappings();
     for (int x=0; x<dcm.size(); x++){
        urgency+=((concept_mapping)dcm.elementAt(x)).strength();
     }
     double dv = (double)(dcm.size());
     if (dv>0.0) urgency/=dv;
     codelet ncd = new codelet("correspondence-strength-tester",get_urgency_bin(urgency));
     ncd.arguments.addElement(nc);
     ncd.Pressure_Type = orig.Pressure_Type;
     if (coderack.remove_terraced_scan) ncd.run();
       else coderack.Post(ncd);
  }


  static void propose_rule(slipnode facet,slipnode description,slipnode obj_cat,slipnode relation, codelet orig){
    // creates a proposed rule, and posts a rule-strength-tester codelet with urgency
    // a function of the degree of conceptual-depth of the descriptions in the rule
    Rule r=new Rule(facet,description,obj_cat,relation);
    r.update_strength_value();
    double urgency=(Math.sqrt((description.conceptual_depth+relation.conceptual_depth)/200.0))*100.0;
     codelet nc = new codelet("rule-strength-tester",get_urgency_bin(urgency));
     nc.arguments.addElement(r);
     nc.Pressure_Type = orig.Pressure_Type;
     if (coderack.remove_terraced_scan) nc.run();
          else coderack.Post(nc);
  }

  static void Reset(){
    codelets = new Vector();
    for (int x=0; x<100; x++){
      Codelet_Captions[x].text="-";
      Codelet_Captions[x].Redraw=true;
      Codelet_Captions[x].Resize=true;
    }
    codelets_run = 0;
    Temperature.clamped = true;
    Codelet_Run.Change_Caption("No Codelets Run");
    Codelet_Run.Values=new Vector();
    codelet.Urgency_Sum = 0;
    codelet.Urgency_Count = 0;
    Coderack_Pressure.Reset();
  }

  static void Init(){
   CoderackArea = Areas.NewArea(5,5,745,661);
   CoderackArea.Visible = false;
   Caption c = new Caption(0,0,1000,50,"Coderack");
   c.background = GraphicsObject.Grey; CoderackArea.AddObject(c);
   Codelet_Run = new Caption(0,950,1000,1000,"No Codelet Run");
   CoderackArea.AddObject(Codelet_Run);   

   CoderackInfoArea = Areas.NewArea(505,671,995,995);
   MinimiseInfoArea = new Frames(900,0,1000,100,CoderackInfoArea,GraphicsObject.Grey);
   icons.Minimise(MinimiseInfoArea);
   c = new Caption(0,0,900,100,"Codelet Info.");
   c.background = GraphicsObject.Grey; CoderackInfoArea.AddObject(c);
   CodeletInfo_Captions = new Caption[9];
   for (int x=0; x<9; x++){
      c = new Caption(0,x*100+100,1000,x*100+200,"-");
      CodeletInfo_Captions[x] = c;
      c.Filled=false;
      CoderackInfoArea.AddObject(c);
   }
   CoderackInfoArea.Visible = false;

   CoderackSmall = Areas.NewArea(5,671,495,995);
   CoderackSmall.Shift_Right = true;
   MaximiseCoderack = new Frames(900,0,1000,100,CoderackSmall, GraphicsObject.Grey);
   icons.Maximise(MaximiseCoderack);
   c = new Caption(0,0,900,100,"Coderack");
   c.background = GraphicsObject.Grey; CoderackSmall.AddObject(c);

   Coderack_Pressure.Init_Pressures();

   int x,y,x1,y1,p;
   Codelet_Captions = new Caption[100];
   for (x=0; x<=24; x++)
     for (y=0; y<=3; y++){
        x1=y*250; y1=50+x*36;
        p=y*25+x;
        Codelet_Captions[p]=new Caption(x1,y1,x1+250,y1+36,"-");
        CoderackArea.AddObject(Codelet_Captions[p]);

     }
   // initialise the different types of codelet types
   codelet.add_to_initial_codelets("bottom-up-bond-scout");
   codelet.add_to_initial_codelets("replacement-finder");
   codelet.add_to_initial_codelets("bottom-up-correspondence-scout");
  }
}

















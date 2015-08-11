package org.speakeasy.copycat;

// GroupRuns.java
import java.util.*;
import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;

class GroupRun {
  public static Vector GroupRuns = new Vector();

  public static String name = "xxx";
  public static Area GroupRunArea;
  public static Frames GroupRunFrame;
  public static Frames play, stop, single_step, minimise, PlayMode;
  public static Caption Max_Runs, Save;
  public int Maximum_Number_Of_Runs = 1000;

  public Vector RunStrings = new Vector();
     // a vector of the output strings for each of the run examples

  public Vector Answers = new Vector();
  public Vector AnswerCaptions = new Vector();
  public int scores[] = new int[100];
	
	public String initial,modified,target;
	

  public GroupRun() {
  	  initial = workspace.initial_string;
  	  modified = workspace.modified_string;
  	  target = workspace.target_string;
      }

  public GroupRun(Frame f){
     name = "group run "+String.valueOf(GroupRuns.size()+1);
     GetValue g = new GetValue(f,"New Group Run",
       "please enter the name of this run",
       "",name,"ID:newgrouprun");
   //  name = GetValue.CurrentValue;
   //  if (GetValue.OK) GroupRuns.addElement(this);
  }

  public static void Init(){
    GroupRunArea = Areas.NewArea(5,5,745,661);
    GroupRunArea.background = GraphicsObject.Grey;
    GroupRunArea.Visible= false;    
  }

  public void Recalculate(){
    GroupRunArea.objects = new Vector();
    
    Caption c = new Caption (0,0,800,50,name);
    c.background = GraphicsObject.Grey;
    GroupRunArea.AddObject(c);

    stop = new Frames(800,0,850,50,GroupRunArea,GraphicsObject.Grey);
    icons.Stop(stop);  stop.Selected = true; PlayMode = stop; 
    play = new Frames(850,0,900,50,GroupRunArea,GraphicsObject.Grey);
    icons.Play(play); 
    single_step = new Frames(900,0,950,50,GroupRunArea,GraphicsObject.Grey);
    icons.Single_Step(single_step); 
    minimise = new Frames(950,0,1000,50,GroupRunArea,GraphicsObject.Grey);
    icons.Minimise(minimise); 

    GroupRunFrame = new Frames(100,100,900,800,GroupRunArea,Color.white);

    Max_Runs = new Caption(100,850,450,950,"Maximum Runs: "+String.valueOf(Maximum_Number_Of_Runs));
    GroupRunArea.AddObject(Max_Runs);
    Save = new Caption(550,850,900,950,"Save");
    GroupRunArea.AddObject(Save);

    for (int x=0; x<=50; x++) scores[x]=0;

    Answers = new Vector();
    AnswerCaptions = new Vector();
    for (int x=0; x<RunStrings.size(); x++){
      boolean found = false;
      for (int y=0; y<Answers.size(); y++){
        if (((String)Answers.elementAt(y)).equals(
            (String)RunStrings.elementAt(x)))
            {  scores[y]++;  found = true; }
      }
      if (!found) {
        Answers.addElement((String)RunStrings.elementAt(x));
        scores[Answers.size()-1]++;
      }
    }
    int width = 200;
    if (Answers.size()>5) width = 1000/(Answers.size());

    // sort the answers so that the leftmost is the highest
    Object o1, o2;
    int v1, v2;
    int sz = Answers.size();
    int pos = 0;
    if (sz>1){
      while (pos<(sz-1)) {
        v1=scores[pos]; v2=scores[pos+1];
        if (v2>v1) {
          scores[pos]=v2; scores[pos+1]=v1;
          o1=Answers.elementAt(pos); o2=Answers.elementAt(pos+1);
          Answers.setElementAt(o2,pos); Answers.setElementAt(o1,pos+1);
          pos=0;
        }
        else pos++;
      }
    }
    
    for (int x=0; x<Answers.size(); x++){
       double d = ((double)(scores[x]))/((double)(RunStrings.size()));
       Box b = new Box(x*width,900-(int)(d*900.0),x*width+width,900);
       b.Solid = true;
       b.background = GraphicsObject.Colours[x];

       GroupRunFrame.AddObject(b);
       c = new Caption(x*width,900,x*width+width,1000,
                 (String)Answers.elementAt(x));
       AnswerCaptions.addElement(c);
       GroupRunFrame.AddObject(c);
    }
    c = new Caption (5,75,95,125,String.valueOf(RunStrings.size()));
    c.Filled = false; GroupRunArea.AddObject(c);
    c = new Caption (5,700,95,750,"0");
    c.Filled = false; GroupRunArea.AddObject(c);
  }

  public int find_problem(int xpos, int ypos){
    if ((xpos<GroupRunFrame.sx1)||(xpos>GroupRunFrame.sx2)||
         (ypos<GroupRunFrame.sy1)||(ypos>GroupRunFrame.sy2)) return -1;
    String search = "";
    for (int x=0; x<AnswerCaptions.size(); x++){
      GraphicsObject ob = (GraphicsObject)AnswerCaptions.elementAt(x);
      if ((xpos>=ob.sx1)&&(xpos<=ob.sx2)){
         search = (String)Answers.elementAt(x);
      }
    }
    if (search.equals("")) return -1;
   
    // place the numbers generating these into a vector and select from it
    int number = 0;
    for (int x=0; x<RunStrings.size(); x++){
      if (search.equals((String)RunStrings.elementAt(x))) number++;
    }
    if (number==0) return -1;
    int[] numbers = new int[number];  number=0;

    for (int x=0; x<RunStrings.size(); x++){
      if (search.equals((String)RunStrings.elementAt(x))){
          numbers[number]=x;
          number++;
      }
    }
    
     // select random number
     int pos = (int)(Math.random()*((double)number));
     if (pos>=number) pos =0;
     return numbers[pos];
  }

public String StreamToString(InputStream is,
      Vector v1) throws FileNotFoundException, IOException {
        int i=0;
      	int line = 1;
        boolean ok=false;
        int column=0;
        String st = "";
        StringBuffer buffer = new StringBuffer();
        while (i != -1){
                i = is.read();
                ok=true;
                if (i<33) ok = false;
                if (ok) st+=((char) i);
                if ((!ok)&&(!st.equals(""))){
                	if (line<4){
                		String s = new String(st);
                		if (line==1) {
                			  	  initial = st;
                			      workspace.initial_string =st;
                		}
                		if (line==2) {
                			modified = st;
                			workspace.modified_string = st;
                		}
                		if (line==3) {
                			target = st;
                			workspace.target_string = st;
                		}
                		line++;
                		st="";
                		
                	}
                	else {
                    column++; 
                    if (column>3) column=1;
                    String s = new String(st);
                    if (column==1) v1.addElement(s);
                    st="";
                	}
                }
        }
        is.close();
        return(buffer.toString());
} 



public static GroupRun LoadGroupRun(String filename){
  String code;

  Vector v = new Vector();
  GroupRun g = new GroupRun();
	
  URL u = null;
  try { u = new URL("http://www2.psy.uq.edu.au/CogPsych/Copycat/"+filename); }
  catch (MalformedURLException mfurl) {  }
  try {
    URLConnection uc = u.openConnection();
    InputStream is = uc.getInputStream();
    String ccode = g.StreamToString(is,v);
  }
      catch (IOException e){
                 System.out.println("io error");
      }

 g.name = filename;
 GetValue.OK = true;
 g.RunStrings = v;
 if (v.size()==0) GetValue.OK = false;
 if (GetValue.OK) GroupRuns.addElement(g);
  return g;
}



}
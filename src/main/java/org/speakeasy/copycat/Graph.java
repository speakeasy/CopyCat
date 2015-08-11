package org.speakeasy.copycat;

/* Graph.java
   used for graphing

*/

import java.util.*;
import java.applet.*;
import java.awt.*;
import java.io.*;

class Graph {
  public static Vector Objects = new Vector();
  public static Vector Labels = new Vector();
  public static Vector Objects2 = new Vector();

  public static Area GraphArea,GraphIcon;
  public static Frames GraphFrame;
  public static Frames GraphMinFrame;
  public static Frames GraphMinimise;

  public static void ResizeLabels(){
    for (int x=0; x<Labels.size(); x++){
      int ypos = x/2;
      int xpos = x-(ypos*2);
      GraphicsObject g = (GraphicsObject)Labels.elementAt(x);
      g.x1=xpos*500; g.x2=g.x1+500;
      g.y1=600+ypos*80; g.y2=g.y1+80;
      g.Resize = true; g.Redraw = true;
    }
    GraphArea.Redraw = true;
  }

  public static void Init(){
    GraphArea = Areas.NewArea(5,5,745,661);
    GraphArea.background = GraphicsObject.Grey;
    GraphIcon = Areas.NewArea(880,505,995,661);

    Caption c = new Caption(0,0,950,50,"Graph"); c.background = GraphicsObject.Grey;
    GraphArea.AddObject(c);
    c = new Caption(0,0,1000,250,"Graph"); c.background = GraphicsObject.Grey;
    GraphIcon.AddObject(c);


    GraphFrame = new Frames(0,50,1000,600,GraphArea,Color.white);
    Color Grey= new Color(200,200,200);
    GraphMinFrame = new Frames(0,250,1000,1000,GraphIcon,Color.white);

    GraphMinimise = new Frames(950,0,1000,50,GraphArea,Grey);
    icons.Minimise(GraphMinimise);
    GraphArea.Visible = false;
  }

  public static void AddObject(GraphicsObject ob){
    ob.Redraw = true;
    if (ob instanceof slipnode_minimised)
       ob=((slipnode_minimised)ob).dad;

    if (Objects.size()==10) return;

    GraphLine gl = new GraphLine(ob);
    GraphLine gl2 = new GraphLine(ob);
    gl2.foreground = gl.foreground;

    GraphFrame.AddObject(gl);
    GraphMinFrame.AddObject(gl2);
    GraphFrame.Redraw=true;
    GraphMinFrame.Redraw = true;
    String s = "unknown";
    
    if (ob instanceof slipnode)
       s = ((slipnode)ob).pname;
    if (ob instanceof Coderack_Pressure)
       s = ((Coderack_Pressure)ob).name;
    if (ob==workspace.temp) s="temperature";
    Caption c = new Caption(0,0,10,10,s);
    c.background = gl.foreground;
    ob.foreground = gl.foreground;
    c.foreground = Color.black;
    GraphArea.AddObject(c);
    Labels.addElement(c);
    Objects.addElement(gl);
    Objects2.addElement(gl2);
    ResizeLabels();
  }
  public static void RemoveObject(GraphicsObject ob){
    int pos = Labels.indexOf(ob);
    if (pos<0) return;
    GraphLine gl = (GraphLine)(Objects.elementAt(pos));
    Labels.removeElement(ob);
    GraphArea.objects.removeElement(ob);
    Objects.removeElement(gl);
    GraphFrame.objects.removeElement(gl);

    gl = (GraphLine)(Objects2.elementAt(pos));
    Objects2.removeElement(gl);
    GraphMinFrame.objects.removeElement(gl);
    gl.thing_to_graph.foreground = null;
    gl.thing_to_graph.Redraw = true;

    GraphArea.Redraw=true;
    GraphMinFrame.Redraw = true;    
    ResizeLabels();
  }
}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class GetStrings extends Dialog {
  // this frame allows users to change the values of the strings
  public static boolean OK = false;
  public static boolean visible = true;
  TextField initial, target, modified; 
  Frame parent;
  String msg;

  GetStrings(Frame par,String mess){
     super(par,"new problem",true);
     parent = par;
     msg = mess;
     reshape(10,10,330,180);
     setLayout(new GridLayout(5,2));
     add(new Label("please enter new string values"));
     add(new Label(" "));
     add(new Label("initial string: "));
     initial = new TextField(workspace.initial_string,10);
     add(initial);
     add(new Label("modified string:"));
     modified = new TextField(workspace.modified_string,10);
     add(modified);
     add(new Label("target string:  "));
     target = new TextField(workspace.target_string,10);
     add(target);
     add(new Button("  OK  "));
     add(new Button("Cancel"));

     OK = false;
     show();
  }

public boolean handleEvent(Event e){
 switch (e.id) {
   case Event.WINDOW_DESTROY:
   visible = false;
   dispose();
   return true;
 default:
   return super.handleEvent(e);
  }
 }

  public boolean action(Event evt, Object arg){
    if ("  OK  ".equals(arg)){
      OK = true;
      workspace.initial_string = initial.getText();
      workspace.target_string = target.getText();
      workspace.modified_string = modified.getText();
      visible = false;
      parent.postEvent(new Event(this,Event.ACTION_EVENT,msg));
      dispose();
    }
    if ("Cancel".equals(arg)){
      visible = false;
      dispose();
    }
    return true;
  }

}


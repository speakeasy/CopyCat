package org.speakeasy.copycat;

// GetValue.java
import java.util.*;
import java.applet.*;
import java.awt.*;

class GetValue extends Dialog {
  // this frame allows users to change a value
  public static String DefaultValue = "";
  public static String CurrentValue = "";
  public static boolean OK = false;
  public static boolean visible = true;
  TextField input; 
  Frame parent;
  String msg;

  GetValue(Frame par, String framename, String description,
           String deflt, String current, String mesg){

     super(par,framename,true);
     parent = par;
     msg = mesg;
     reshape(10,10,300,120);
     setLayout(new BorderLayout());
     add("North",new Label(description));

     Panel p1 = new Panel(); p1.setLayout(new BorderLayout());
     add("Center",p1);
     if (!(deflt.equals(""))) {
       Panel p2 = new Panel(); p2.setLayout(new BorderLayout());
       p1.add("North",p2);
       p2.add("West",new Label("Default Value:"));
       TextField t = new TextField(deflt,20);
       t.disable();
       p2.add("East",t);

       input = new TextField(current,20);
       Panel p3 = new Panel(); p3.setLayout(new BorderLayout());
       p1.add("South",p3);
       p3.add("West",new Label("Current Value:"));
       p3.add("East",input);
     }
     else {
       input = new TextField(current,20);
       Panel p3 = new Panel(); p3.setLayout(new BorderLayout());
       p1.add("Center",p3);
       p3.add("West",new Label("Current Value:"));
       p3.add("East",input);
     }

     if (!(deflt.equals(""))) {
       Panel p4 = new Panel(); p4.setLayout(new BorderLayout());
       p4.add("West",new Button("Default"));
       p4.add("Center",new Button("OK"));
       p4.add("East",new Button("Cancel"));
       add("South", p4);
     }
     else {
       Panel p4 = new Panel(); p4.setLayout(new BorderLayout());
       p4.add("West",new Button("OK"));
       p4.add("East",new Button("Cancel"));
       add("South", p4);
     }

     OK = false;
     DefaultValue = deflt;
     CurrentValue = current;

     //reshape(10,10,150,50);

     show();
     //reshape(10,10,150,50);

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
    if ("Default".equals(arg)){
      OK = true;
      CurrentValue = DefaultValue;
      visible = false;
      dispose();
    }
    if ("OK".equals(arg)){
      OK = true;
      CurrentValue = input.getText();
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
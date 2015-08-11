package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;




class Temperature {
   static Area TempBar;
   static Frames tempcntrl;
   static Box b1,b2,b3,b4,b5,b7,b8,b9;
   static Line b6;
   static Circle c1,c2;
   static Color Grey;
   static double value;
   static boolean clamped = true;
   static int clamp_time = 30;
   static Caption heading;

   static void Init(){
     Grey = new Color(200,200,200);
     TempBar = Areas.NewArea(880,55,995,495); TempBar.background = Grey;
     MyImage m = new MyImage(0,0,1000,1000,GraphicsObject.wood);
     TempBar.AddObject(m);

     b7 = new Box(400,750,600,850); TempBar.AddObject(b7);
     c1 = new Circle(400,150,600,250); TempBar.AddObject(c1); c1.Solid=true;
     c2 = new Circle(200,800,800,950); TempBar.AddObject(c2); 
     c2.Solid=true; c2.background=Color.red;
     b9 = new Box(400,750,600,850); TempBar.AddObject(b9);
     b9.Solid=true; b9.foreground = null; b9.background = Color.red;
     tempcntrl = new Frames(400,200,600,750,TempBar);
     b9 = new Box(0,500,1000,1000); tempcntrl.AddObject(b9);
     b9.Solid=true; b9.foreground = null; b9.background = Color.red;
     b1 = new Box(0,0,1000,1000); tempcntrl.AddObject(b1);
     b2 = new Box(0,100,1000,900); tempcntrl.AddObject(b2);
     b3 = new Box(0,200,1000,800); tempcntrl.AddObject(b3);
     b4 = new Box(0,300,1000,700); tempcntrl.AddObject(b4);
     b5 = new Box(0,400,1000,600); tempcntrl.AddObject(b5);
     b6 = new Line(0,500,1000,500); tempcntrl.AddObject(b6);
     heading = new Caption(0,0,1000,100,"Temperature");
     TempBar.AddObject(heading);

     int x;
     Caption cp;
     String st;
     for (x=0; x<=10; x++){
       st=String.valueOf(100-(x*10));
       cp=new Caption(650,200+(55*x)-25,950,200+(55*x)+25,st); cp.Filled = false;
       TempBar.AddObject(cp);
     }
   }
   static void Update(double val){
     if (value!=val){
       //System.out.println("yes - we are updating temperature");
       value = val;
       double ht=100.0-val; if (ht<0.0)ht=0.0; if (ht>100.0) ht=100.0;
       b9.y1=(int)(ht*10.0);
       tempcntrl.Redraw=true;       
       b9.Resize = true;
     }

   }
}

package org.speakeasy.copycat;

// graphicsobject.java
import java.util.*;
import java.applet.*;
import java.awt.*;


class icons {
  static void Arrow(GraphicsObject g){
    GraphicsObject c1,c2,c3,c4,c5,c6,c7;
    c1=new Line(200,200,600,300);
    c2=new Line(200,200,300,600);
    c3=new Line(600,300,500,400);
    c4=new Line(300,600,400,500);
    c5=new Line(500,400,800,700);
    c6=new Line(400,500,700,800);
    c7=new Line(800,700,700,800);
    g.AddObject(c1);
    g.AddObject(c2);
    g.AddObject(c3);
    g.AddObject(c4);
    g.AddObject(c5);
    g.AddObject(c6);
    g.AddObject(c7);


  }

  static void Clamp(GraphicsObject g) {
     GraphicsObject c1; 
     c1=new Line(300,200,700,200); g.AddObject(c1);
     c1=new Line(700,200,700,300); g.AddObject(c1);
     c1=new Line(300,800,700,800); g.AddObject(c1);
     c1=new Line(700,800,700,700); g.AddObject(c1);
     c1=new Line(600,300,800,300); g.AddObject(c1);
     c1=new Line(600,700,800,700); g.AddObject(c1);
     c1=new Line(300,200,300,300); g.AddObject(c1);
     c1=new Line(300,700,300,800); g.AddObject(c1);
     c1=new Line(300,300,400,350); g.AddObject(c1);
     c1=new Line(400,350,200,450); g.AddObject(c1);
     c1=new Line(200,450,400,550); g.AddObject(c1);
     c1=new Line(400,550,200,650); g.AddObject(c1);
     c1=new Line(200,650,300,700); g.AddObject(c1);

  }
  static void EyeDrop(GraphicsObject g) {
    GraphicsObject c;
    c = new Line(200,200,250,200); g.AddObject(c);
    c = new Line(250,200,575,525); g.AddObject(c);
    c = new Line(200,200,200,250); g.AddObject(c);
    c = new Line(200,250,525,575); g.AddObject(c);
    c = new Line(450,650,650,450); g.AddObject(c);
    c = new Line(600,500,800,700); g.AddObject(c);
    c = new Line(800,700,800,800); g.AddObject(c);
    c = new Line(800,800,700,800); g.AddObject(c);
    c = new Line(700,800,500,600); g.AddObject(c);

  }

  static void Single_Step(GraphicsObject g){
    GraphicsObject c1,c2,c3,c4;
    c1=new Line(200,200,200,800);
    c2=new Line(200,200,800,500);
    c3=new Line(200,800,800,500);
    c4=new Line(800,200,800,800);
    g.AddObject(c1);
    g.AddObject(c2);
    g.AddObject(c3);
    g.AddObject(c4);
  }
  static void Pause(GraphicsObject g){
    GraphicsObject c1,c2;
    c1=new Line(400,200,400,800);
    c2=new Line(600,200,600,800);
    g.AddObject(c1);
    g.AddObject(c2);
  }
  static void Play(GraphicsObject g){
    GraphicsObject c1,c2,c3;
    c1=new Line(200,200,200,800);
    c2=new Line(200,200,800,500);
    c3=new Line(200,800,800,500);
    g.AddObject(c1);
    g.AddObject(c2);
    g.AddObject(c3);
  }
  static void Stop(GraphicsObject g){
    GraphicsObject c1,c2,c3,c4;
    c1=new Line(200,200,200,800);
    c2=new Line(200,200,800,200);
    c3=new Line(200,800,800,800);
    c4=new Line(800,200,800,800);
    g.AddObject(c1);
    g.AddObject(c2);
    g.AddObject(c3);
    g.AddObject(c4);
  }
  static void Minimise(GraphicsObject g){
    GraphicsObject c1,c2,c3;
    c1=new Line(200,200,800,200);
    c2=new Line(200,200,500,800);
    c3=new Line(500,800,800,200);
    g.AddObject(c1);
    g.AddObject(c2);
    g.AddObject(c3);
  }

  static void Maximise(GraphicsObject g){
    GraphicsObject c1,c2,c3;
    c1=new Line(200,800,800,800);
    c2=new Line(200,800,500,200);
    c3=new Line(500,200,800,800);
    g.AddObject(c1);
    g.AddObject(c2);
    g.AddObject(c3);
  }

} 


package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

public class AlgorithmThread extends Thread {
  Applet myapp; // what is updated?
	long oldtime = 0;
	Date d = new Date();

  public AlgorithmThread(Applet a){
    myapp = a;
  }

  public void run(){
    
    while (true){
      myapp.repaint();
      long time = d.getTime();
    	if ((time-oldtime)>200){
    	  try {
    	     sleep(50);
    	  }catch (InterruptedException e){}
    		oldtime = time;
    	}	
    }
  }
}
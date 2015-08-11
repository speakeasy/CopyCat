package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;

class formulas {
  static double temperature = 100.0;
  static double actual_temperature = 100.0;

  static double weighted_average(double v1, double w1, double v2, double w2){
     return ((v1*w1)+(v2*w2))/(w1+w2);
  }

  static double weighted_average(double v1, double w1, double v2, double w2,
              double v3, double w3){
     return ((v1*w1)+(v2*w2)+(v3*w3))/(w1+w2+w3);
  }

  static double max(double x, double y){
     if (x>y) return x;
     return y;
  }

  static double min(double x, double y){
     if (x<y) return x;
     return y;
  }

  static double temperature_adjusted_probability(double val){
    // if the temperature is 0, no adjustment is made
    // otherwise, values above .5 are lowered and values below .5 are raised
    //System.out.println("the log of 100,10"+Math.pow(100.0,0.1));  
    double rval,t=0.0;
    rval= val;
    if (val==0.0) return 0.0;
    if (val==0.5) return 0.0;
    else if (val<0.5){
      //gets lowered the lower the temperature
     // t = max(Math.abs(Math.log(val,10.0))
       return 1.0-temperature_adjusted_probability(1.0-val);
    }
    else if (val>0.5){
      //gets raised the lower the temperature
      rval = max(1.0-((1.0-val)+((10-Math.sqrt(100.0-Temperature.value))/
             100.0)*(1.0 - (1.0 - val))),0.5);

    }

   /* else if (val<0.5){
      //gets lowered the lower the temperature
      t = (Math.sqrt(100-Temperature.value))/10;
      rval=0.5-val;
      rval=rval*t;
      rval=0.5-rval;

    }
    else if (val>0.5){
      //gets raised the lower the temperature
      t = (Math.sqrt(100-Temperature.value))/10;
      rval=val-0.5;
      rval=rval*t;
      rval=0.5+rval;
    }*/
    //System.out.println("Temperature adjusted value.  Temp="+Temperature.value+" t:"+t+" inital val="+val+" adjusted val="+rval);
    return rval;    
  }


  static double blur(double val){
    double sq=Math.sqrt(val);
    if (random.rnd()<0.5) return val+sq;
    return val-sq;
  }
  static Vector append(Vector v1, Vector v2){
    Vector nw = new Vector();
    for (int x=0; x<v1.size(); x++){
       Object ob = (Object)v1.elementAt(x);
       nw.addElement(ob);
    }

    for (int x=0; x<v2.size(); x++){
       Object ob = (Object)v2.elementAt(x);
       nw.addElement(ob);
    }
    return nw;
  }
}
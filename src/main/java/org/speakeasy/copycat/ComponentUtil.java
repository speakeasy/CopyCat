package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;
import java.io.*;



class ComponentUtil {
  public static Frame getFrame(Component theComponent) {
    Component currParent = theComponent;
    Frame theFrame = null;
    while (currParent!= null) {
      if (currParent instanceof Frame) {
        theFrame = (Frame)currParent;
        break;
      }
      currParent = currParent.getParent();
    }
    return theFrame;
  }


}
package org.speakeasy.copycat;

import java.util.*;
import java.applet.*;
import java.awt.*;

class DragInfo {
   static boolean Drag=false; // true if an item is being dragged
   static int DragType;  // 0 = circle (ie. a node) with width x1, height y1
                             // 1 = a line with origin x1,y1
   static int x1,y1;
   static GraphicsObject DragObject;
}
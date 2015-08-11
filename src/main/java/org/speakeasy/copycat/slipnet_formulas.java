package org.speakeasy.copycat;

// copycat.java
import java.util.*;
import java.applet.*;
import java.awt.*;

class slipnet_formulas {
  static slipnode get_bond_category(slipnode fromnode, slipnode tonode){
    // return the label of the link between these nodes if it exists
    if (fromnode==tonode) return slipnet.identity;
    for (int i=0; i<fromnode.outgoing_links.size(); i++){
      slipnet_link l = (slipnet_link)fromnode.outgoing_links.elementAt(i);
      if (l.to_node==tonode) return l.label;
    }
    return null;
  }

  static boolean slip_linked(slipnode s1, slipnode s2){
     for (int x=0; x<s1.lateral_slip_links.size(); x++){
       slipnet_link sl = (slipnet_link)s1.lateral_slip_links.elementAt(x);
       if (sl.to_node==s2) return true;
     }
     return false;
  }

  static slipnode get_related_node(slipnode category, slipnode relation){
    // return the node that is linked to this node via this relation
    if (relation==slipnet.identity) return category;

    for (int i=0; i<category.outgoing_links.size(); i++){
      slipnet_link l = (slipnet_link)category.outgoing_links.elementAt(i);
      if (l.label==relation) return l.to_node;
    }
    return null;

  }
  
  static boolean linked(slipnode s1, slipnode s2){
    for (int x=0; x<s1.outgoing_links.size(); x++){
      slipnet_link s=(slipnet_link)s1.outgoing_links.elementAt(x);
      if (s.to_node==s2) return true;
    }
    return false;
  }

  static boolean related(slipnode s1, slipnode s2){
    if (s1==s2) return true;
    return linked(s1,s2);
  }
}
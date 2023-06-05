/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * That's pretty much all there is to implementing a drag and drop for JTrees that allows multiple node trasfers. If you want, here's a small test application that sets up a DnDTree with some nodes. Note: this code doesn't implement any of the extra features I had above (undo, redo, cut/copy/paste). However, all of the framework is there and all you need to do is follow the above instructions to create an interface between your application and the DnDTree to utilize the features.
 * @author Zhijun Zhang
 */
public class DnDTreeTester {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(DnDTreeTester.class.getName());
    }
    public static void main(String[] args)
    {
        DnDMutableTreeNode root = new DnDMutableTreeNode("root");
        DnDMutableTreeNode child = new DnDMutableTreeNode("parent 1");
        root.add(child);
        child = new DnDMutableTreeNode("parent 2");
        root.add(child);
        child = new DnDMutableTreeNode("parent 3");
        child.add(new DnDMutableTreeNode("child 1"));
        child.add(new DnDMutableTreeNode("child 2"));
        child.add(new DnDMutableTreeNode("child 3"));
        child.add(new DnDMutableTreeNode("child 4"));
        root.add(child);
        DnDTree tree = new DnDTree(root);
        JFrame frame = new JFrame("Drag and drop JTrees");
        frame.getContentPane().add(tree);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

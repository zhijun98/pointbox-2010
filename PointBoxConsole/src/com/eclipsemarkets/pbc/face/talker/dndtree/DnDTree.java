/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import java.awt.AWTEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
 
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
 


/**
 * In my (@author helloworld922) implementation, I extended JTree so I can support some basic selection tracking in the tree. This isn't that big of a deal when there's only one node to keep track of, but for multiple selected nodes it's the easiest method. So, what we need in our DnDJtree is:
 *
 * 1. Use the default JTree selection methods to get the selection. Method was modified to only get the "top" selections, ie. if a parent and one or more of it's children are selected, only the parent is removed as being selected (decreases data transfer and potential data duplication)
 * 2. Turn on drag and drop. All JComponents have the interface to handle drag and drop, but not all sub-classes implement the necessary components to make it work.
 * 3. Setup a transfer handler. The transfer handler is a fairly intricate class that allows Java to transfer objects either to the system or to your program. It's used in both copy/paste and drag and drop. For our application, we'll only be focusing on the drag and drop portion of handling, but realize that the same infrastructure (and in fact, a lot of the same code) can be reused for copy/paste.
 * 4. Setting the drop mode. I left the default drop mode which only allows you to drop onto the node, but you can change this to allow your DnDTree to allow you to "insert" nodes into a location. This version of code supports the insertion of nodes, but you can turn this off by changing the drop mode.
 * 5. Setup my own TreeModel. See the section about DnDTreeModel, but for the most part it's almost the same as DefaultTreeModel.
 * 6. Setup a basic undo/redo model. It uses a pseudo-stack and keeps track of individual add/remove events (DnDTreeEvent). Performing multiple actions with one undo/redo trigger is kept track with a timer (anything that happens within 100ms of the first event that can be undone is added to that list).
 * 7. Setup a source where different events can be triggered (cut, copy, paste, undo/redo). Note that I chose to leave this part to be implemented externally so the DnDTree and be plugged into any application you want.
 *
 * @author Zhijun Zhang (original author helloworld922)
 */
public class DnDTree extends JTree implements MouseListener, PropertyChangeListener,
		TreeModelListener, ActionListener
{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(DnDTree.class.getName());
    }
    private static final long serialVersionUID = -4260543969175732269L;
    protected DnDTreeNode<AWTEvent> undoLoc;

    private boolean undoStack;
    private boolean doingUndo;

    protected boolean isDoingUndo() {
        return doingUndo;
    }

    /**
     * Constructs a DnDTree with root as the main node.
     * 
     * @param root
     */
    public DnDTree(DnDMutableTreeNode root)
    {
        super();
        this.setModel(new DnDTreeModel(root));
        // turn on the JComponent dnd interface
        this.setDragEnabled(true);
        // setup our transfer handler
        this.setTransferHandler(createTransferHandler());
        // trun on drop
        this.setDropMode(DropMode.ON_OR_INSERT);

        this.setScrollsOnExpand(true);
        // this.addTreeSelectionListener(this);
        this.addMouseListener(this);
        this.getModel().addTreeModelListener(this);
        this.undoLoc = new DnDTreeNode<AWTEvent>(null);
    }
    
    protected TransferHandler createTransferHandler(){
        return new DnDTreeTransferHandler(this);
    }
    
    /**
     * Plugin a DropTargetListener which may help to customize changing mouse cursor
     * @param listener
     * @return - successful or not
     */
    public boolean addDropTargetListener(DropTargetListener listener){
        boolean result = false;
        DropTarget dt = this.getDropTarget();
        try {
            dt.addDropTargetListener(listener); 
            result = true;
        } catch (TooManyListenersException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * 
     * @param e
     */
    protected void addUndo(AWTEvent e)
    {
            this.undoLoc.linkNext(new DnDTreeNode<AWTEvent>(e));
            this.undoLoc = this.undoLoc.next;
    }

    /**
     * Only returns the top level selection<br>
     * ex. if a child and it's parent are selected, only it's parent is returned
     * in the list.
     * 
     * @return an array of TreePath objects indicating the selected nodes, or
     *         null if nothing is currently selected
     */
    @Override
    public TreePath[] getSelectionPaths()
    {
            // get all selected paths
            TreePath[] temp = super.getSelectionPaths();
            if (temp != null)
            {
                    ArrayList<TreePath> list = new ArrayList<TreePath>();
                    for (int i = 0; i < temp.length; i++)
                    {
                            // determine if a node can be added
                            boolean canAdd = true;
                            for (int j = 0; j < list.size(); j++)
                            {
                                    if (temp[i].isDescendant(list.get(j)))
                                    {
                                            // child was a descendant of another selected node,
                                            // disallow add
                                            canAdd = false;
                                            break;
                                    }
                            }
                            if (canAdd)
                            {
                                    list.add(temp[i]);
                            }
                    }
                    return list.toArray(new TreePath[list.size()]);
            }
            else
            {
                    // no paths selected
                    return null;
            }
    }

    /**
     * Implemented a check to make sure that it is possible to de-select all
     * nodes. If this component is added as a mouse listener of another
     * component, that componenet can trigger a deselect of all nodes.
     * <p>
     * This method also allows for de-select if a blank spot inside this tree is
     * selected. Note that using the expand/contract button next to the label
     * will not cause a de-select.
     * <p>
     * if the given mouse event was from a popup trigger, was not BUTTON1, or
     * shift/control were pressed, a deselect is not triggered.
     * 
     * @param e
     **/
    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    /**
     * @param e
     **/
    @Override
    public void mouseEntered(MouseEvent e)
    {}

    /**
     * @param e
     **/
    @Override
    public void mouseExited(MouseEvent e)
    {}

    /**
     * @param e
     **/
    @Override
    public void mousePressed(MouseEvent e)
    {}

    /**
     * @param e
     **/
    @Override
    public void mouseReleased(MouseEvent e)
    {}

    public void performRedo()
    {
            if (this.undoLoc.next != null)
            {
                    this.undoLoc = this.undoLoc.next;
                    if (this.undoLoc.data instanceof DnDTreeUndoEventCap)
                    {
                            // should be start cap. else, diagnostic output
                            if (((DnDTreeUndoEventCap) this.undoLoc.data).isStart())
                            {
                                    this.doingUndo = true;
                                    this.undoLoc = this.undoLoc.next;
                                    while (!(this.undoLoc.data instanceof DnDTreeUndoEventCap))
                                    {
                                            if (this.undoLoc.data instanceof DnDTreeEvent)
                                            {
                                                    // perform the action
                                                    if (this.undoLoc.data instanceof DnDTreeEvent)
                                                    {
                                                            this.performTreeEvent(((DnDTreeEvent) this.undoLoc.data));
                                                    }
                                            }
                                            this.undoLoc = this.undoLoc.next;
                                    }
                                    this.doingUndo = false;
                            }
                            else
                            {
                                    System.out.println("undo stack problems");
                            }
                    }
            }
    }

    public void performUndo()
    {
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            if (this.undoLoc.data instanceof DnDTreeUndoEventCap)
            {
                    // should be end cap. else, diagnostic output
                    if (!((DnDTreeUndoEventCap) this.undoLoc.data).isStart())
                    {
                            this.doingUndo = true;
                            this.undoLoc = this.undoLoc.prev;
                            while (!(this.undoLoc.data instanceof DnDTreeUndoEventCap))
                            {
                                    if (this.undoLoc.data instanceof DnDTreeEvent)
                                    {
                                            // perform inverse
                                            // System.out.println(((AddRemoveEvent)
                                            // this.undoLoc.data).invert());
                                            this.performTreeEvent(((DnDTreeEvent) this.undoLoc.data).invert());
                                    }
                                    this.undoLoc = this.undoLoc.prev;
                            }
                            // move to previous
                            if (this.undoLoc.prev != null)
                            {
                                    this.undoLoc = this.undoLoc.prev;
                            }
                            this.doingUndo = false;
                    }
                    else
                    {
                            System.out.println("undo stack problems");
                    }
            }
    }

    public void performTreeEvent(DnDTreeEvent e)
    {
            DefaultTreeModel model = (DefaultTreeModel) this.getModel();
            if (e.isAdd())
            {
                    model.insertNodeInto(e.getNode(), e.getDestination(), e.getIndex());
            }
            else
            {
                    model.removeNodeFromParent(e.getNode());
            }
    }

    /**
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
            if (evt.getPropertyName().equals(DnDTreeDeleteAction.DO_DELETE))
            {
                    // perform delete
                    DefaultTreeModel model = (DefaultTreeModel) this.getModel();
                    TreePath[] selection = this.getSelectionPaths();
                    if (selection != null)
                    {
                            // something is selected, delete it
                            for (int i = 0; i < selection.length; i++)
                            {
                                    if (((DnDMutableTreeNode) selection[i].getLastPathComponent()).getLevel() > 1)
                                    {
                                            // TODO send out action to partially remove node
                                            model.removeNodeFromParent((DnDMutableTreeNode) selection[i].getLastPathComponent());
                                    }
                            }
                    }
            }
            else if (evt.getPropertyName().equals(DnDTreeUndoAction.DO_UNDO))
            {
                    this.performUndo();
            }
            else if (evt.getPropertyName().equals(DnDTreeRedoAction.DO_REDO))
            {
                    this.performRedo();
            }
            else
            {
                    System.out.println(evt.getPropertyName());
            }
    }

    /**
     * @param e
     */
    @Override
    public void treeNodesChanged(TreeModelEvent e)
    {
            // TODO Auto-generated method stub
            System.out.println("nodes changed");
    }

    /**
     * @param e
     */
    @Override
    public void treeNodesInserted(TreeModelEvent e)
    {
            // TODO Auto-generated method stub
            if (!this.doingUndo)
            {
                    this.checkUndoStatus();
                    System.out.println("inserted");
                    int index = e.getChildIndices()[0];
                    DnDMutableTreeNode parent = (DnDMutableTreeNode) e.getTreePath().getLastPathComponent();
                    this.addUndo(new DnDTreeEvent(this, true, parent, (DnDMutableTreeNode) e.getChildren()[0], index));
            }
    }

    /**
     * @param e
     */
    @Override
    public void treeNodesRemoved(TreeModelEvent e)
    {
            // TODO Auto-generated method stub
            if (!this.doingUndo)
            {
                    this.checkUndoStatus();
                    System.out.println("removed");
                    int index = e.getChildIndices()[0];
                    DnDMutableTreeNode parent = (DnDMutableTreeNode) e.getTreePath().getLastPathComponent();
                    this.addUndo(new DnDTreeEvent(this, false, parent, (DnDMutableTreeNode) e.getChildren()[0], index));
            }
    }

    /**
     * @param e
     */
    @Override
    public void treeStructureChanged(TreeModelEvent e)
    {
            // TODO Auto-generated method stub
            System.out.println("structure changed");
    }

    /**
     * @param e
     */
    protected void checkUndoStatus()
    {
            if (!this.undoStack)
            {
                    this.undoStack = true;
                    this.addUndo(new DnDTreeUndoEventCap(this, true));
                    Timer timer = new Timer(100, this);
                    timer.setRepeats(false);
                    timer.setActionCommand("update");
                    timer.start();
            }
    }

    /**
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
            if (e.getActionCommand().equals("update") && this.undoStack)
            {
                    this.undoStack = false;
                    this.addUndo(new DnDTreeUndoEventCap(this, false));
            }
    }
}


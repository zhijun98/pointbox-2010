/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import java.util.logging.Logger;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
 
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
/**
 * From item 3 above, we added a transfer handler for our JTree. However, JTree's default transfer handler rejects all drops. Obviously, we need to change that. So, for our transfer handler we will need:
 * 
 * 1. A method to check if data import is valid.
 * 2. A method to add nodes to the appropriate drop location, and to remove nodes that were moved rather than copied.
 * 
 * For moving around nodes in a JTree it's best to do so with the tree model because it should handle the event generation/dispatch, as well as update the changes for us. I'm using my own DnDTreeModel because the default one has trouble removing the correct node (it uses an equals() comparison, but what is really necessary is an absolute object equality).
 * 
 * Children will now be moved in the "correct order".
 * ex.
 * 
 * root
 * - child1
 * - child2
 * - child3
 * 
 * move child2 and child3 above child1:
 * 
 * root
 * - child2
 * - child3
 * - child1
 * @author Zhijun Zhang (original author helloworld922)
 */
public class DnDTreeTransferHandler extends TransferHandler
{

    private static final Logger logger;
    static {
        logger = Logger.getLogger(DnDTreeTransferHandler.class.getName());
    }
    
    protected DnDTree tree;
    private static final long serialVersionUID = -6851440217837011463L;

    /**
     * Creates a DnDTreeTransferHandler to handle a certain tree. Note that this
     * constructor does NOT set this transfer handler to be that tree's transfer
     * handler, you must still add it manually.
     * 
     * @param tree
     */
    public DnDTreeTransferHandler(DnDTree tree)
    {
            super();
            this.tree = tree;
    }

    /**
     * @param supp
     * @return
     */
    @Override
    public boolean canImport(TransferSupport supp)
    {
        if (supp.isDataFlavorSupported(DnDTreeList.DnDTreeList_FLAVOR))
        {
            DnDMutableTreeNode[] destPaths = null;
            // get the destination paths
            if (supp.isDrop())
            {
                TreePath dropPath = ((JTree.DropLocation) supp.getDropLocation()).getPath();
                if (dropPath == null)
                {
                    // debugging a few anomalies with dropPath being null.
                    System.out.println("Drop path somehow came out null");
                    return false;
                }
                if (dropPath.getLastPathComponent() instanceof DnDMutableTreeNode)
                {
                    destPaths = new DnDMutableTreeNode[1];
                    destPaths[0] = (DnDMutableTreeNode) dropPath.getLastPathComponent();
                }
            }
            else
            {
                    // cut/copy, get all selected paths as potential drop paths
                TreePath[] paths = this.tree.getSelectionPaths();
                if (paths == null)
                {
                    // possibility no nodes were selected, do nothing
                    return false;
                }
                destPaths = new DnDMutableTreeNode[paths.length];
                for (int i = 0; i < paths.length; i++)
                {
                    destPaths[i] = (DnDMutableTreeNode) paths[i].getLastPathComponent();
                }
            }
            for (int i = 0; i < destPaths.length; i++)
            {
                // check all destinations accept all nodes being transfered
                DataFlavor[] incomingFlavors = supp.getDataFlavors();
                for (int j = 1; j < incomingFlavors.length; j++)
                {
                    if (!destPaths[i].canImport(incomingFlavors[j]))
                    {
                        // found one unsupported import, invalidate whole import
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param c
     * @return null if no nodes were selected, or this transfer handler was not
     *         added to a DnDTree. I don't think it's possible because of the
     *         constructor layout, but one more layer of safety doesn't matter.
     */
    @Override
    protected Transferable createTransferable(JComponent c)
    {
            if (c instanceof DnDTree)
            {
                    ((DnDTree) c).setSelectionPaths(((DnDTree) c).getSelectionPaths());
                    return new DnDTreeList(((DnDTree) c).getSelectionPaths());
            }
            else
            {
                    return null;
            }
    }

    /**
     * @param c
     * @param t
     * @param action
     */
    @Override
    protected void exportDone(JComponent c, Transferable t, int action)
    {
            if (action == TransferHandler.MOVE)
            {
                // we need to remove items imported from the appropriate source.
                try
                {
                    // get back the list of items that were transfered
                    ArrayList<TreePath> list = ((DnDTreeList) t
                                    .getTransferData(DnDTreeList.DnDTreeList_FLAVOR)).getNodes();
                    for (int i = 0; i < list.size(); i++)
                    {
                        // get the source
                        DnDMutableTreeNode sourceNode = (DnDMutableTreeNode) list.get(i).getLastPathComponent();
                        DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
                        model.removeNodeFromParent(sourceNode);
                    }
                    notifyBuddyListSettingsChangedAfterExportDone();
                }
                catch (UnsupportedFlavorException exception)
                {
                    // for debugging purposes (and to make the compiler happy). In
                    // theory, this shouldn't be reached.
                    logger.log(Level.SEVERE, null, exception);
                }
                catch (IOException exception)
                {
                    // for debugging purposes (and to make the compiler happy). In
                    // theory, this shouldn't be reached.
                    logger.log(Level.SEVERE, null, exception);
                }
            }
    }

    /**
     * @param c
     * @return
     */
    @Override
    public int getSourceActions(JComponent c)
    {
            return TransferHandler.COPY_OR_MOVE;
    }

    /**
     * 
     * @param supp
     * @return
     */
    @Override
    public boolean importData(TransferSupport supp)
    {
            if (this.canImport(supp))
            {
                    try
                    {
                            // Fetch the data to transfer
                            Transferable t = supp.getTransferable();
                            ArrayList<TreePath> list;

                            list = ((DnDTreeList) t.getTransferData(DnDTreeList.DnDTreeList_FLAVOR)).getNodes();

                            TreePath[] destPaths;
                            DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
                            if (supp.isDrop())
                            {
                                    // the destination path is the location
                                    destPaths = new TreePath[1];
                                    destPaths[0] = ((javax.swing.JTree.DropLocation) supp.getDropLocation())
                                                    .getPath();
                            }
                            else
                            {
                                    // pasted, destination is all selected nodes
                                    destPaths = this.tree.getSelectionPaths();
                            }
                            // create add events
                            for (int i = 0; i < destPaths.length; i++)
                            {
                                    // process each destination
                                    DnDMutableTreeNode destNode = (DnDMutableTreeNode) destPaths[i].getLastPathComponent();
                                    for (int j = 0; j < list.size(); j++)
                                    {
                                            // process each node to transfer
                                            int destIndex = -1;
                                            DnDMutableTreeNode sourceNode = (DnDMutableTreeNode) list.get(j).getLastPathComponent();
                                            // case where we moved the node somewhere inside of the
                                            // same node
                                            boolean specialMove = false;
                                            if (supp.isDrop())
                                            {
                                                    // chance to drop to a determined location
                                                    destIndex = ((JTree.DropLocation) supp.getDropLocation())
                                                                    .getChildIndex();
                                            }
                                            if (destIndex == -1)
                                            {
                                                    // use the default drop location
                                                    destIndex = destNode.getAddIndex(sourceNode);
                                            }
                                            else
                                            {
                                                    // update index for a determined location in case of
                                                    // any shift
                                                    destIndex += j;
                                            }
                                            model.insertNodeInto(sourceNode, destNode, destIndex);
                                    }
                            }
                            return true;
                    }
                    catch (UnsupportedFlavorException exception)
                    {
                        // TODO Auto-generated catch block
                        logger.log(Level.SEVERE, null, exception);
                    }
                    catch (IOException exception)
                    {
                        // TODO Auto-generated catch block
                        logger.log(Level.SEVERE, null, exception);
                    }
            }
            // import isn't allowed at this time.
            return false;
    }

    void notifyBuddyListSettingsChangedAfterExportDone() {
        //do nothing in this class
    }
}

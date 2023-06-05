/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Zhijun Zhang
 */
class PbcDnDBuddyTreeTransferHandler extends DnDTreeTransferHandler{

    private static final Logger logger;
    private boolean isRegularTree;
    static {
        logger = Logger.getLogger(PbcDnDBuddyTreeTransferHandler.class.getName());
    }

    PbcDnDBuddyTreeTransferHandler(PbcDndBuddyTree tree,boolean isRegularTree) {
        super(tree);
        this.isRegularTree=isRegularTree;
    }

    @Override
    void notifyBuddyListSettingsChangedAfterExportDone() {
        if (tree instanceof PbcDndBuddyTree){
            ((PbcDndBuddyTree)tree).notifyBuddyListSettingsChangedAfterDndHappened();
        }
    }
    
    @Override
    public int getSourceActions(JComponent c)
    {
        if(isRegularTree)
            return TransferHandler.MOVE;
        else
            return TransferHandler.COPY_OR_MOVE;
    }
    /**
     * @param supp
     * @return
     */
    @Override
    public boolean canImport(TransferSupport supp) {
        if (supp.isDataFlavorSupported(DnDTreeList.DnDTreeList_FLAVOR)) {
            //(1) get the destination paths for validation
            TreePath destPath;
            if (supp.isDrop()) {
                destPath = ((javax.swing.JTree.DropLocation) supp.getDropLocation()).getPath();
            } else {
                // cut/copy, get all selected paths as potential drop paths
                TreePath[] destPaths = this.tree.getSelectionPaths();
                if ((destPaths == null) || (destPaths.length != 1)) {
                    // canot past into multiple paths
                    return false;
                }
                destPath = destPaths[0];
            }
            if (destPath == null){
                return false;
            }
            DnDMutableTreeNode destNode = (DnDMutableTreeNode)(destPath.getLastPathComponent());
            if (destNode == null){
                return false;
            }
//            //(2) get the fetched data for validation
//            Transferable t = supp.getTransferable();
//            ArrayList<TreePath> sourceList = null;
//            try {
//                sourceList = ((DnDTreeList) t.getTransferData(DnDTreeList.DnDTreeList_FLAVOR)).getNodes();
//            } catch (UnsupportedFlavorException ex) {
//                return false;
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, null, ex);
//                return false;
//            }
//            if (sourceList == null){
//                return false;
//            }                
            //(3) validation...
            // check all destinations accept all nodes being transfered
            DataFlavor[] incomingFlavors = supp.getDataFlavors();
            for (int j = 1; j < incomingFlavors.length; j++) {
                if (!destNode.canImport(incomingFlavors[j])){
                    // found one unsupported import, invalidate whole import
                    return false;
                }
            }
//                    // validate each source node
//            DnDMutableTreeNode sourceNode = null;
//            for (int j = 0; j < sourceList.size(); j++) {
//                sourceNode = (DnDMutableTreeNode) sourceList.get(j).getLastPathComponent();
//                if (!isImportable(sourceNode, destNode)){
//                    return false;
//                }
//            }//for
            return true;
        }else{
            //wrong data flavor
            return false;
        }
    }
    
    private boolean canImportNodes(TransferSupport supp) {
        if (supp.isDataFlavorSupported(DnDTreeList.DnDTreeList_FLAVOR)) {
            //(1) get the destination paths for validation
            TreePath destPath;
            if (supp.isDrop()) {
                destPath = ((javax.swing.JTree.DropLocation) supp.getDropLocation()).getPath();
            } else {
                // cut/copy, get all selected paths as potential drop paths
                TreePath[] destPaths = this.tree.getSelectionPaths();
                if ((destPaths == null) || (destPaths.length != 1)) {
                    // canot past into multiple paths
                    return false;
                }
                destPath = destPaths[0];
            }
            if (destPath == null){
                return false;
            }
            DnDMutableTreeNode destNode = (DnDMutableTreeNode)(destPath.getLastPathComponent());
            if (destNode == null){
                return false;
            }
            //(2) get the fetched data for validation
            Transferable t = supp.getTransferable();
            ArrayList<TreePath> sourceList;
            try {
                sourceList = ((DnDTreeList) t.getTransferData(DnDTreeList.DnDTreeList_FLAVOR)).getNodes();
            } catch (UnsupportedFlavorException ex) {
                return false;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                return false;
            }
            if (sourceList == null){
                return false;
            }                
            //(3) validation...
            // check all destinations accept all nodes being transfered
            DataFlavor[] incomingFlavors = supp.getDataFlavors();
            for (int j = 1; j < incomingFlavors.length; j++) {
                if (!destNode.canImport(incomingFlavors[j])){
                    // found one unsupported import, invalidate whole import
                    return false;
                }
            }
                    // validate each source node
            DnDMutableTreeNode sourceNode;
            for (int j = 0; j < sourceList.size(); j++) {
                sourceNode = (DnDMutableTreeNode) sourceList.get(j).getLastPathComponent();
                if (!isImportable(sourceNode, destNode)){
                    return false;
                }
            }//for
            return true;
        }else{
            //wrong data flavor
            return false;
        }
    }

    private boolean isImportable(DnDMutableTreeNode sNode, DnDMutableTreeNode dNode) {
        if (sNode.equals(dNode)){
            //they cannot be the same
            return false;
        }
        if (sNode instanceof DnDGroupTreeNode){ //group
            if (isRoot(dNode)){
                return true;
            }else{
                //group cannot get into buddy or another group
                return false;
            }
        }else if (sNode instanceof DnDBuddyTreeNode){   //buddy
            if (dNode instanceof DnDGroupTreeNode){
                return true;
            }else{
                //buddy cannot get into root or buddy node
                return false;
            }
        }else{  //root
            //source cannot be root
            return false;
        }
    }

    private boolean isRoot(DnDMutableTreeNode sNode) {
        if (sNode instanceof DnDGroupTreeNode){ //group
            return false;
        }else if (sNode instanceof DnDBuddyTreeNode){   //buddy
            return false;
        }else{  //root
            return true;
        }
    }

    /**
     * Check if destNode's children has redunant node which is the same as sourceNode
     * @param sourceNode
     * @param destNode
     * @return 
     */
    private boolean isRedundantNode(DnDBuddyTreeNode sourceNode, DnDGroupTreeNode destNode) {
        if (sourceNode.getParent().equals(destNode)){
            return false;
        }
        Enumeration eg = destNode.children();
        Object buddyNodeObj;
        DnDBuddyTreeNode buddyNode;
        IGatewayConnectorBuddy buddy;
        IGatewayConnectorBuddy sourceBuddy;
        boolean result = false;
        while(eg.hasMoreElements()){
            buddyNodeObj = eg.nextElement();
            if (buddyNodeObj instanceof DnDBuddyTreeNode){
                buddyNode = (DnDBuddyTreeNode)buddyNodeObj;
                buddy = buddyNode.getGatewayConnectorBuddy();
                sourceBuddy = sourceNode.getGatewayConnectorBuddy();
                if (buddy.getIMUniqueName().equalsIgnoreCase(sourceBuddy.getIMUniqueName())){
                    if ((buddy.getLoginOwner() != null) && (sourceBuddy.getLoginOwner() != null) 
                            && (buddy.getLoginOwner().getIMUniqueName().equalsIgnoreCase(sourceBuddy.getLoginOwner().getIMUniqueName())))
                    {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public boolean importData(TransferSupport supp) {
        if (this.canImportNodes(supp)) {
            try {
                // Fetch the data to transfer
                Transferable t = supp.getTransferable();
                ArrayList<TreePath> sourceList = ((DnDTreeList) t.getTransferData(DnDTreeList.DnDTreeList_FLAVOR)).getNodes();
                // Get destination path
                TreePath[] destPaths;
                DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
                if (supp.isDrop()) {
                    // the destination path is the location
                    destPaths = new TreePath[1];
                    destPaths[0] = ((javax.swing.JTree.DropLocation) supp.getDropLocation())
                                    .getPath();
                } else {
                    // pasted, destination is all selected nodes
                    destPaths = this.tree.getSelectionPaths();
                }
                // create add events
                for (int i = 0; i < destPaths.length; i++) {
                    // process each destination
                    DnDMutableTreeNode destNode = (DnDMutableTreeNode) destPaths[i].getLastPathComponent();
                    // process each node to transfer
                    for (int j = 0; j < sourceList.size(); j++) {
                        DnDMutableTreeNode sourceNode = (DnDMutableTreeNode) sourceList.get(j).getLastPathComponent();
                        if ((sourceNode instanceof DnDBuddyTreeNode) && (destNode instanceof DnDGroupTreeNode)){
                            if (!isRedundantNode((DnDBuddyTreeNode)sourceNode, (DnDGroupTreeNode)destNode)){
                                insertNodeHelper(supp, sourceNode, destNode, model, j);
                            }
                        }else{
                            insertNodeHelper(supp, sourceNode, destNode, model, j);
                        }//if
                    }//for
                }
                return true;
            }
            catch (UnsupportedFlavorException ex){
                // TODO Auto-generated catch block
                logger.log(Level.SEVERE, null, ex);
            }
            catch (IOException ex){
                // TODO Auto-generated catch block
                logger.log(Level.SEVERE, null, ex);
            }
        }
        // import isn't allowed at this time.
        return false;
    }

    private void insertNodeHelper(TransferSupport supp, DnDMutableTreeNode sourceNode, DnDMutableTreeNode destNode, DefaultTreeModel model, int j) {
        int destIndex = -1;
        // case where we moved the node somewhere inside of the same node
        if (supp.isDrop()) {
            // chance to drop to a determined location
            destIndex = ((JTree.DropLocation) supp.getDropLocation())
                            .getChildIndex();
        }
        if (destIndex == -1){
            // use the default drop location
            destIndex = destNode.getAddIndex(sourceNode);
        }else{
            // update index for a determined location in case of
            // any shift
            destIndex += j;
        }
            model.insertNodeInto(sourceNode, destNode, destIndex);
    }
}

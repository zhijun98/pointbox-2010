/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.model;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckTreePanel;
import com.eclipsemarkets.pbc.face.checktree.IEmsCheckNode;
import com.eclipsemarkets.pbc.face.talker.model.IBuddyTreeNode;
import com.eclipsemarkets.pbc.face.talker.model.IGroupTreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Rueyfarn Wang
 * Created on Aug 07, 2010, 03:26:20 PM
 */
public class GroupListTreeTransferHandler extends TransferHandler
{
   DataFlavor nodesFlavor;

   DataFlavor[] flavors = new DataFlavor[1];

   DefaultMutableTreeNode[] nodesToRemove;

   DefaultMutableTreeNode destination;

   IEmsCheckTreePanel  iEmsCheckTreePanel;

   public GroupListTreeTransferHandler(IEmsCheckTreePanel  iEmsCheckTreePanel )
   {

      this.iEmsCheckTreePanel = iEmsCheckTreePanel;
      try
      {
         String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
               + javax.swing.tree.DefaultMutableTreeNode[].class.getName()
               + "\"";
         nodesFlavor = new DataFlavor(mimeType);
         flavors[0] = nodesFlavor;
      }
      catch (ClassNotFoundException e)
      {
         //System.out.println("ClassNotFound: " + e.getMessage());
      }
   }

   public boolean canImport(TransferHandler.TransferSupport support)
   {
      //System.out.println ("GroupListTreeTransferHandler.canImport");
      if( !support.isDrop())
      {
         return false;
      }

      support.setShowDropLocation(true);
      if( !support.isDataFlavorSupported(nodesFlavor))
      {
         return false;
      }

      JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
      JTree tree = (JTree) support.getComponent();
      int dropRow = tree.getRowForPath(dl.getPath());


      int[] selRows = tree.getSelectionRows();
      for (int i = 0; i < selRows.length; i++)
      {
         if (selRows[i] == dropRow)
         {
            return false;
         }
      }

      TreePath dest = dl.getPath();
      DefaultMutableTreeNode target = (DefaultMutableTreeNode) dest
            .getLastPathComponent();


       //System.out.println ("DropLocation = " + dl);
       //System.out.println ( "LastPathComponent = " + target);
       //System.out.println ( "Path Level = " + target.getLevel());

      int pathLevel = target.getLevel();
      if ( pathLevel == 0 )
      {
         return false;
      }
      else
      if ( pathLevel > 1 )
      {
         TreePath  parentPath = dest.getParentPath();
         parentPath = moveUpToGroupNodePath(parentPath);
         Object possibleTarget =  parentPath.getLastPathComponent();         

         TreePath sourcePath = tree.getSelectionPath();
         DefaultMutableTreeNode source = (DefaultMutableTreeNode) sourcePath
            .getLastPathComponent();

         IEmsCheckNode parent = (IEmsCheckNode)possibleTarget;
         IEmsCheckNode child  = (IEmsCheckNode)source;
         
         if (  isAlreadyAMemberOfTheGroup (parent, child) )
         {
            return false;
         }
         else
         {
            return true;
         }
      }
      else
      {
         TreePath sourcePath = tree.getSelectionPath();
         DefaultMutableTreeNode source = (DefaultMutableTreeNode) sourcePath
            .getLastPathComponent();
         IEmsCheckNode parent = (IEmsCheckNode)target;
         IEmsCheckNode child  = (IEmsCheckNode)source;         
         if ( isAlreadyAMemberOfTheGroup (parent, child) )
         {
            //System.out.println ("already a current member");
            return false;
         }
      }
      return true;
   }

   private TreePath moveUpToGroupNodePath (TreePath curPath)
   {
      TreePath pathToCheck = curPath;
      int pathCount = pathToCheck.getPathCount();
      while ( pathCount != 2)
      {
         pathToCheck = pathToCheck.getParentPath();
         pathCount = pathToCheck.getPathCount();
      }
      return pathToCheck;
   }


   private boolean isAlreadyAMemberOfTheGroup (IEmsCheckNode dropTarget, IEmsCheckNode dropSource)
   {
      boolean rc = dropTarget.isMember(dropSource);
      return rc;
   }


   public boolean importData(TransferHandler.TransferSupport support)
   {
      if( !canImport(support))
      {
         return false;
      }
      // Extract transfer data.
      DefaultMutableTreeNode[] nodes = null;
      try
      {
         Transferable t = support.getTransferable();
         nodes = (DefaultMutableTreeNode[]) t.getTransferData(nodesFlavor);
      }
      catch (UnsupportedFlavorException ufe)
      {
         //System.out.println("UnsupportedFlavor: " + ufe.getMessage());
      }
      catch (java.io.IOException ioe)
      {
         //System.out.println("I/O error: " + ioe.getMessage());
      }

      // Get drop location info.
      JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
      int childIndex = dl.getChildIndex();
      TreePath dest = dl.getPath();
      dest =  moveUpToGroupNodePath (dest);
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest
            .getLastPathComponent();
      JTree tree = (JTree) support.getComponent();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

      // Configure for drop mode.
      int index = childIndex; // DropMode.INSERT
      if( childIndex == -1)
      { // DropMode.ON
         index = parent.getChildCount();
      }

      // Add data to model.
      for (int i = 0; i < nodes.length; i++)
      {
         DefaultMutableTreeNode node = nodes[i];
         IEmsCheckNode childECN = (IEmsCheckNode)node.getUserObject();
         IEmsCheckNode parentECN = (IEmsCheckNode)parent;
         IGatewayConnectorGroup group = (IGatewayConnectorGroup)parentECN.getAssociatedObject();
         IGatewayConnectorBuddy buddy = (IGatewayConnectorBuddy)childECN.getAssociatedObject();
         //this.iEmsCheckTreePanel.insertGroupBuddyNodePair(group, buddy);
         this.iEmsCheckTreePanel.addBuddyToGroup(parentECN, childECN);
      }
      tree.expandPath(dest);
      this.destination = parent;
      return true;
   }

   protected Transferable createTransferable(JComponent c)
   {
      JTree tree = (JTree) c;
      TreePath[] paths = tree.getSelectionPaths();
      if( paths != null)
      {
         List<DefaultMutableTreeNode> copies = new ArrayList<DefaultMutableTreeNode>();
         List<DefaultMutableTreeNode> toRemove = new ArrayList<DefaultMutableTreeNode>();

         DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
               .getLastPathComponent();
         DefaultMutableTreeNode copy = copyTreeNode(node);
         copies.add(copy);
         toRemove.add(node);

         for (int i = 1; i < paths.length; i++)
         {
            DefaultMutableTreeNode next = (DefaultMutableTreeNode) paths[i]
                  .getLastPathComponent();
            // Do not allow higher level nodes to be added to list.
            if( next.getLevel() < node.getLevel())
            {
               break;
            }
            else if( next.getLevel() > node.getLevel())
            { // child node
               copy.add(copyTreeNode(next));
               // node already contains child
            }
            else
            { // sibling
               copies.add(copyTreeNode(next));
               toRemove.add(next);
            }
         }
         DefaultMutableTreeNode[] nodes = copies
               .toArray(new DefaultMutableTreeNode[copies.size()]);
         nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[toRemove
               .size()]);
         return new NodesTransferable(nodes);
      }
      return null;
   }

   protected void exportDone(JComponent source, Transferable data, int action)
   {
      if( (action & MOVE) == MOVE)
      {
         JTree tree = (JTree) source;
         DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
         // Remove nodes saved in nodesToRemove in createTransferable.
         for (int i = 0; i < nodesToRemove.length; i++)
         {
            DefaultMutableTreeNode node = nodesToRemove[i];
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            IEmsCheckNode buddyNode = (IEmsCheckNode)node;
            IEmsCheckNode groupNode = (IEmsCheckNode)parent;
            iEmsCheckTreePanel.removeBuddyFromGroup(groupNode, buddyNode);
            
            //model.removeNodeFromParent(node)
         }
      }
   }

   public int getSourceActions(JComponent c)
   {
      return  COPY_OR_MOVE;
   }

   /** Defensive copy used in createTransferable. */
   private DefaultMutableTreeNode copyTreeNode(DefaultMutableTreeNode node)
   {
       if (node != null){
           //PbsysDebugger.printWarningMessage("Copy Node -> " + node.getClass().getCanonicalName());
           if (node instanceof IBuddyTreeNode){
               return new BuddyTreeNode((IGatewayConnectorBuddy)node.getUserObject());
           }else if (node instanceof IGroupTreeNode){
               return new GroupTreeNode((IGatewayConnectorGroup)node.getUserObject());
           }else{
               return new DefaultMutableTreeNode(node);
           }
       }
       return null;
   }


   public String toString()
   {
      return getClass().getName();
   }

   public class NodesTransferable implements Transferable
   {
      DefaultMutableTreeNode[] nodes;

      public NodesTransferable(DefaultMutableTreeNode[] nodes) {
         this.nodes = nodes;
      }

      public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException
      {
         if( !isDataFlavorSupported(flavor))
         {
            throw new UnsupportedFlavorException(flavor);
         }
         return nodes;
      }

      public DataFlavor[] getTransferDataFlavors()
      {
         return flavors;
      }

      public boolean isDataFlavorSupported(DataFlavor flavor)
      {
         return nodesFlavor.equals(flavor);
      }
   }
}

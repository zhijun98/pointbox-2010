/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.talker.IGroupListTreePanel;
import com.eclipsemarkets.pbc.face.talker.model.GroupListTreeTransferHandler;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Zhijun Zhang
 */
class EmsCheckTreePanel extends JPanel implements IEmsCheckTreePanel, IEmsCheckTreeSelectionListener{
    private static final long serialVersionUID = 1L;
    private CopyOnWriteArrayList<IEmsCheckTreeListener> checkTreeListeners = new CopyOnWriteArrayList<IEmsCheckTreeListener>();
    private CopyOnWriteArrayList<IEmsCheckTreeSelectionListener> selectionListeners = new CopyOnWriteArrayList<IEmsCheckTreeSelectionListener>();
    JTree baseTree;
    private EmsCheckNode rootNode;

    final IPbcKernel kernel;


    private IGroupListTreePanel iGroupListTreePanel;
    
    EmsCheckTreePanel(String rootName, Icon rootIcon, IPbcKernel kernel) {
        this.kernel = kernel;
        this.rootNode = EmsCheckNodeFactory.createRootInstance(rootName, rootIcon);
        initializeEmsCheckTreePanel(rootNode);
    }


    /**
     * Constructs a new CheckBox tree.
     *
     * @param rootNode Node that is the root of this tree.
     */
    EmsCheckTreePanel(String rootName, IPbcKernel kernel) {
        this.kernel = kernel;
        this.rootNode = EmsCheckNodeFactory.createRootInstance(rootName);
        initializeEmsCheckTreePanel(rootNode);
    }
    
    private void initializeEmsCheckTreePanel(EmsCheckNode rootNode){
        baseTree = new JTree(rootNode);        
        baseTree.setCellRenderer(new EmsCheckRenderer());
        baseTree.setRowHeight(18);
        baseTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        baseTree.setToggleClickCount(1000);
        baseTree.putClientProperty("JTree.lineStyle", "Angled");
        baseTree.addMouseListener(new NodeSelectionListener(baseTree));
        baseTree.setDropMode(DropMode.ON);
        baseTree.setDragEnabled(true);
        baseTree.setTransferHandler(new GroupListTreeTransferHandler((IEmsCheckTreePanel)this));

        addCheckTreeSelectionListener(this);
        
        setLayout(new BorderLayout());
        add(baseTree, BorderLayout.CENTER);
    }

    @Override
    public void ensureFirstMemberVisible(final IGatewayConnectorGroup group, final ArrayList<IGatewayConnectorBuddy> members) {
        (new SwingWorker<TreePath, Void>(){
            @Override
            protected TreePath doInBackground() throws Exception {
                TreePath path = null;
                Object obj;
                boolean findGroup = false;
                for (int i = 0; i <= baseTree.getRowCount(); i++) {
                    path = baseTree.getPathForRow(i);
                    if (path != null){
                        obj = path.getLastPathComponent();
                        if (obj instanceof EmsTreeNode){
                            obj = ((EmsTreeNode)obj).getAssociatedObject();
                            if (obj instanceof IGatewayConnectorGroup){
                                if (((IGatewayConnectorGroup)obj).getGroupName().equalsIgnoreCase(group.getGroupName())){
                                    findGroup = true;
                                }
                            }
                            if (findGroup && (obj instanceof IGatewayConnectorBuddy)){
                                break;
                            }
                        }
                    }
                }//for
                return path;
            }

            @Override
            protected void done() {
                try {
                    TreePath path = get();
                    if (path != null){
                        baseTree.scrollPathToVisible(path);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(EmsCheckTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(EmsCheckTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
    }

    @Override
    public void checkNodeSelected(IEmsCheckNode aCheckNode) {
        if (aCheckNode instanceof BuddyCheckNode){
            BuddyCheckNode buddyCheckNode = (BuddyCheckNode)aCheckNode;
            Object obj = aCheckNode.getAssociatedObject();
            if ((obj != null) && (obj instanceof IGatewayConnectorBuddy)){
                Object objNode = buddyCheckNode.getParent();
                if (objNode instanceof GroupCheckNode){
                    GroupCheckNode aGroupNode = (GroupCheckNode)objNode;
                    aGroupNode.setSelectedSimply(true);
                }
                selectBuddyCheckNode(buddyCheckNode.getBuddy());
            }
        }
    }

    @Override
    public void checkNodeUnselected(IEmsCheckNode aCheckNode) {
        if (aCheckNode instanceof BuddyCheckNode){
            unselectBuddyCheckNode(((BuddyCheckNode)aCheckNode).getBuddy());
        }
    }

    @Override
    public void addEmsCheckTreeListener(IEmsCheckTreeListener listener){
        if (!checkTreeListeners.contains(listener)){
            checkTreeListeners.add(listener);
        }
    }

    void fireGroupCheckNodeInsertedEvent(IEmsCheckNode groupNode) {
        for (IEmsCheckTreeListener listener : checkTreeListeners){
            listener.groupCheckNodeInsertedEvent(groupNode);
        }
    }

    void fireBuddyCheckNodeInsertedEvent(IEmsCheckNode buddyNode) {
        for (IEmsCheckTreeListener listener : checkTreeListeners){
            listener.buddyCheckNodeInsertedEvent(buddyNode);
        }
    }

    void fireGroupCheckNodeDeletedEvent(IEmsCheckNode groupNode) {
        for (IEmsCheckTreeListener listener : checkTreeListeners){
            listener.groupCheckNodeDeletedEvent(groupNode);
        }
    }

    void fireBuddyCheckNodeDeletedEvent(IEmsCheckNode buddyNode) {
        for (IEmsCheckTreeListener listener : checkTreeListeners){
            listener.buddyCheckNodeDeletedEvent(buddyNode);
        }
    }

    @Override
    public synchronized void addCheckTreeSelectionListener(IEmsCheckTreeSelectionListener listerner) {
        if (!selectionListeners.contains(listerner)){
            selectionListeners.add(listerner);
        }
    }

    @Override
    public ArrayList<IGatewayConnectorGroup> retrieveCheckedGroups() {
        return retrieveGroups(true);
    }

    @Override
    public ArrayList<IGatewayConnectorGroup> retrieveAllGroups() {
        return retrieveGroups(false);
    }

    @Override
    public boolean isDistGroupExisted(String distGroupName) {
        return (findGroupCheckNode(distGroupName) != null);
    }

    @Override
    public IEmsCheckNode findGroupCheckNode(IGatewayConnectorGroup group) {
        if (group == null){
            return null;
        }
        return findGroupCheckNode(group.getGroupName());
    }
    
    private IEmsCheckNode findGroupCheckNode(String distGroupName) {
        IEmsCheckNode target = null;
        if (DataGlobal.isNonEmptyNullString(distGroupName)){
            Vector children = rootNode.getChilds();
            Object obj;
            EmsCheckNode node;
            if (children != null){
                for (int i = 0; i < children.size(); i++){
                    obj = children.get(i);
                    if (obj instanceof EmsCheckNode){
                        node = (EmsCheckNode)obj;
                        if (node.getAssociatedObject() instanceof IGatewayConnectorGroup){
                            if (((IGatewayConnectorGroup)(node.getAssociatedObject())).getGroupName().equalsIgnoreCase(distGroupName)){
                                target = node;
                                break;
                            }
                        }
                    }
                }//for
            }
        }//if
        return target;
    }

    private ArrayList<IGatewayConnectorGroup> retrieveGroups(boolean checked) {
        ArrayList<IGatewayConnectorGroup> groups = new ArrayList<IGatewayConnectorGroup>();
        Vector children = rootNode.getChilds();
        Object obj;
        EmsCheckNode node;
        if (children != null){
            for (int i = 0; i < children.size(); i++){
                obj = children.get(i);
                if (obj instanceof EmsCheckNode){
                    node = (EmsCheckNode)obj;
                    if (checked){
                        if (node.isSelected()){
                            if (node.getAssociatedObject() instanceof IGatewayConnectorGroup){
                                groups.add((IGatewayConnectorGroup)(node.getAssociatedObject()));
                            }
                        }
                    }else{
                        if (node.getAssociatedObject() instanceof IGatewayConnectorGroup){
                            groups.add((IGatewayConnectorGroup)(node.getAssociatedObject()));
                        }
                    }
                }
            }
        }
        return groups;
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> retrieveBuddiesOfGroup(IGatewayConnectorGroup group) {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        Vector children = rootNode.getChilds();
        Object obj;
        EmsCheckNode groupNode;
        for (int i = 0; i < children.size(); i++){
            obj = children.get(i);
            if (obj instanceof EmsCheckNode){
                groupNode = (EmsCheckNode)obj;
                if (((IGatewayConnectorGroup)groupNode.getAssociatedObject()).getGroupName().equalsIgnoreCase(group.getGroupName())){
                    buddies.addAll(retrieveBuddiesFromGroupNode(groupNode, false));
                    break;
                }
            }
        }
        return buddies;
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> retrieveCheckedBuddiesForBroadcast() {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        Vector children = rootNode.getChilds();
        Object obj;
        for (int i = 0; i < children.size(); i++){
            obj = children.get(i);
            if (obj instanceof EmsCheckNode){
                buddies.addAll(retrieveBuddiesFromGroupNodeForBroadcast((EmsCheckNode)obj, true));
            }
        }
        return buddies;
    }

    private ArrayList<IGatewayConnectorBuddy> retrieveBuddiesFromGroupNodeForBroadcast(EmsCheckNode groupNode, 
                                                                                       boolean checkedNode)
    {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        if (checkedNode){
            if (groupNode.isSelected()){
                buddies.add((IGatewayConnectorBuddy)(groupNode.getAssociatedObject()));
            }
        }
        return buddies;
    }

    @Override
    public ArrayList<IGatewayConnectorBuddy> retrieveCheckedBuddies() {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        Vector children = rootNode.getChilds();
        Object obj;
        for (int i = 0; i < children.size(); i++){
            obj = children.get(i);
            if (obj instanceof EmsCheckNode){
                buddies.addAll(retrieveCheckedBuddiesFromGroupNode((EmsCheckNode)obj));
            }
        }
        return buddies;
    }

    private ArrayList<IGatewayConnectorBuddy> retrieveCheckedBuddiesFromGroupNode(EmsCheckNode groupNode) {
        return retrieveBuddiesFromGroupNode(groupNode, true);
    }

    private ArrayList<IGatewayConnectorBuddy> retrieveBuddiesFromGroupNode(EmsCheckNode groupNode, boolean checkedNode) {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        Vector children = groupNode.getChilds();
        Object obj;
        EmsCheckNode node;
        for (int i = 0; i < children.size(); i++){
            obj = children.get(i);
            if (obj instanceof EmsCheckNode){
                node = (EmsCheckNode)obj;
                if (checkedNode){
                    if (node.isSelected() && node.isLeaf()){
                        buddies.add((IGatewayConnectorBuddy)(node.getAssociatedObject()));
                    }
                }else{
                    buddies.add((IGatewayConnectorBuddy)(node.getAssociatedObject()));
                }
            }
        }
        return buddies;
    }

    @Override
    public synchronized JTree getBaseTree() {
        return baseTree;
    }

    @Override
    public synchronized void checkAllTreeNodes() {
        if (rootNode == null){
            return;
        }
        rootNode.setSelected(true);
        this.baseTree.updateUI();
    }

    @Override
    public synchronized void uncheckAllTreeNodes() {
        if (rootNode == null){
            return;
        }
        rootNode.setSelected(false);
        this.baseTree.updateUI();
    }

    @Override
    public synchronized IEmsCheckNode getRootCheckNode() {
        return (IEmsCheckNode)(baseTree.getModel().getRoot());
    }

    @Override
    public synchronized IEmsCheckNode createNewBuddyCheckNode(IGatewayConnectorBuddy buddy, Icon icon) {
        return EmsCheckNodeFactory.createBuddyCheckNodeInstance(buddy, icon);
    }

    @Override
    public synchronized void insertGroupBuddyNodePair(IGatewayConnectorGroup group, IGatewayConnectorBuddy buddy, boolean buddySelected) {
        IEmsCheckNode rootCheckNode = getRootCheckNode();
        IEmsCheckNode groupNode = rootCheckNode.retrieveChildrenNode(group);
        if (groupNode == null){
            groupNode = EmsCheckNodeFactory.createGroupCheckNodeInstance(group, getImageIcon(GatewayServerType.PBIM_SERVER_TYPE));
            rootCheckNode.addChildCheckNode(groupNode);
            fireGroupCheckNodeInsertedEvent(groupNode);
        }
        IEmsCheckNode buddyNode = groupNode.retrieveChildrenNode(buddy);
        if (buddyNode == null){
            buddyNode = EmsCheckNodeFactory.createBuddyCheckNodeInstance(buddy, getImageIcon(buddy.getIMServerType()));
            buddyNode.setAssociatedObject(buddy);
            groupNode.addChildCheckNode(buddyNode);
            fireBuddyCheckNodeInsertedEvent(buddyNode);
        }
        if (buddySelected){
            if (!groupNode.isSelected()){
                groupNode.setSelected(true);
                //fire event???
            }
        }
        buddyNode.setSelected(buddySelected);
        expandCheckTree();
    }

    @Override
    public void addBuddyToGroup(IEmsCheckNode groupNode, IEmsCheckNode buddyNode) {
        if (groupNode == null || buddyNode == null ) {
           return;
        }
        IEmsCheckNode memberToRemove = null;
        this.syncUpGroupListSettings(groupNode, buddyNode, memberToRemove);
    }

    @Override
    public void removeBuddyFromGroup(IEmsCheckNode groupNode, IEmsCheckNode buddyNode) {
        if (groupNode == null || buddyNode == null ) {
           return;
        }
        IEmsCheckNode memberToAdd = null;
        syncUpGroupListSettings (groupNode, memberToAdd, buddyNode);
    }

    private void syncUpGroupListSettings (IEmsCheckNode groupNode, IEmsCheckNode memberToAdd, IEmsCheckNode memberToRemove ) {
        if ( this.iGroupListTreePanel != null) {
           Enumeration memberEnum = groupNode.getChildrenEnumeration();
           ArrayList<IGatewayConnectorBuddy> buddyMembers = new ArrayList<IGatewayConnectorBuddy>();

           IGatewayConnectorBuddy  buddyNotToAdd = null;
           if ( memberToRemove != null) {
              buddyNotToAdd = (IGatewayConnectorBuddy)memberToRemove.getAssociatedObject();
           }

           while ( memberEnum.hasMoreElements()) {
              IEmsCheckNode member = (IEmsCheckNode)memberEnum.nextElement();
              IGatewayConnectorBuddy  curBuddy = (IGatewayConnectorBuddy)member.getAssociatedObject();

              if ( buddyNotToAdd == null   || (sameBuddy (curBuddy, buddyNotToAdd) == false)) {
                 buddyMembers.add(curBuddy);
              }
           }
           IGatewayConnectorGroup group = (IGatewayConnectorGroup)groupNode.getAssociatedObject();

           if ( memberToAdd != null) {
              IGatewayConnectorBuddy  newBuddy = (IGatewayConnectorBuddy)memberToAdd.getAssociatedObject();
              buddyMembers.add ( newBuddy);
           }
           this.iGroupListTreePanel.editDistributionGroup(group, buddyMembers);
        }
    }

    @Override
    public void removeBuddyFromGroup(IGatewayConnectorBuddy buddy, IGatewayConnectorGroup group) {
        if ( this.iGroupListTreePanel != null) {
        }
    }
    
    private boolean sameBuddy ( IGatewayConnectorBuddy one,IGatewayConnectorBuddy two) {
       if ( one != null || two != null) {

          if ( equivalentStrings(one.getBuddyGroupName(), two.getBuddyGroupName())   &&
               equivalentStrings(one.getBuddyStatus().toString(), two.getBuddyStatus().toString()) &&
               equivalentStrings(one.getIMPassword(), two.getIMPassword()) &&
               equivalentStrings(one.getIMServerTypeString(), two.getIMServerTypeString()) &&
               equivalentStrings(one.getIMScreenName(), two.getIMScreenName()) &&
               equivalentStrings(one.getIMUniqueName(), two.getIMUniqueName())
             ) {
             return true;
          }
       }
       return false;
    }

    private boolean equivalentStrings ( String one, String two) {
       if (one == null ) {
          return (two == null);
       } else {
          return one.equals(two);
       }
    }

    @Override
    public void removeGroup(IGatewayConnectorGroup group) {
        IEmsCheckNode rootCheckNode = getRootCheckNode();
        IEmsCheckNode groupNode = rootCheckNode.retrieveChildrenNode(group);
        if (groupNode != null){
            groupNode.removeChildrenCheckNodes();
            rootCheckNode.removeChildCheckNode(groupNode);
            fireGroupCheckNodeDeletedEvent(groupNode);
        }
    }

    @Override
    public void removeAllGroups() {
        IEmsCheckNode rootCheckNode = getRootCheckNode();
        rootCheckNode.removeChildrenCheckNodes();
        TreeModel model = baseTree.getModel();
        if (model instanceof DefaultTreeModel){
            ((DefaultTreeModel)model).reload();
        }
    }

    @Override
    public synchronized JPanel getBasePanel() {
        return this;
    }

    /**
     * Closes the CheckTree.
     */
    public synchronized void close() {
    }

    public synchronized JTree getTree() {
        return baseTree;
    }

    /**
     * Call to expand the entire tree.
     */
    @Override
    public synchronized void expandCheckTree() {
        for (int i = 0; i <= baseTree.getRowCount(); i++) {
            baseTree.expandPath(baseTree.getPathForRow(i));
        }
    }

    Icon getImageIcon(GatewayServerType type) {
        switch (type){
            case AIM_SERVER_TYPE:
                return kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getAimBuddyIcon();
            case YIM_SERVER_TYPE:
                return kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getYahooBuddyIcon();
            case PBIM_SERVER_TYPE:
            case PBIM_DISTRIBUTION_TYPE:
            default:
                return kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getPbimBuddyIcon();
        }
    }

    @Override
    public void selectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        if ((buddy == null) || (DataGlobal.isEmptyNullString(buddy.getIMUniqueName()))){
            return;
        }
        IEmsCheckNode rootCheckNode = this.getRootCheckNode();
        Enumeration nmr = rootCheckNode.getChildrenEnumeration();
        Enumeration nmr_group;
        Object obj;
        GroupCheckNode groupNode;
        BuddyCheckNode buddyNode;
        while (nmr.hasMoreElements()){
            obj = nmr.nextElement();
            if (obj instanceof GroupCheckNode){
                groupNode = (GroupCheckNode)obj;
                nmr_group = groupNode.getChildrenEnumeration();
                while (nmr_group.hasMoreElements()){
                    obj = nmr_group.nextElement();
                    if (obj instanceof BuddyCheckNode){
                        buddyNode = (BuddyCheckNode)obj;
                        if (buddyNode.getBuddy() != null){
                            if (buddy.getIMUniqueName().equalsIgnoreCase(buddyNode.getBuddy().getIMUniqueName())){
                                buddyNode.setSelectedSimply(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unselectBuddyCheckNode(IGatewayConnectorBuddy buddy) {
        if ((buddy == null) || (DataGlobal.isEmptyNullString(buddy.getIMUniqueName()))){
            return;
        }
        IEmsCheckNode rootCheckNode = this.getRootCheckNode();
        Enumeration nmr = rootCheckNode.getChildrenEnumeration();
        Enumeration nmr_group;
        Object obj;
        GroupCheckNode groupNode;
        BuddyCheckNode buddyNode;
        while (nmr.hasMoreElements()){
            obj = nmr.nextElement();
            if (obj instanceof GroupCheckNode){
                groupNode = (GroupCheckNode)obj;
                nmr_group = groupNode.getChildrenEnumeration();
                while (nmr_group.hasMoreElements()){
                    obj = nmr_group.nextElement();
                    if (obj instanceof BuddyCheckNode){
                        buddyNode = (BuddyCheckNode)obj;
                        if (buddyNode.getBuddy() != null){
                            if (buddy.getIMUniqueName().equalsIgnoreCase(buddyNode.getBuddy().getIMUniqueName())){
                                buddyNode.setSelectedSimply(false);
                            }
                        }
                    }
                }
            }
        }
    }

    class NodeSelectionListener extends MouseAdapter {
        JTree tree;

        NodeSelectionListener(JTree tree) {
            this.tree = tree;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                EmsCheckNode node = (EmsCheckNode)path.getLastPathComponent();
                if (node instanceof GroupCheckNode){
//                    if (e.getClickCount() == 2){
//                        GroupCheckNode groupNode = (GroupCheckNode)node;
//                        if (groupNode.getAssociatedObject() instanceof IGatewayConnectorGroup){
//                            IGatewayConnectorGroup group = (IGatewayConnectorGroup)(groupNode.getAssociatedObject());
//                            ArrayList<IGatewayConnectorBuddy> members = retrieveBuddiesFromGroupNode(groupNode, false);
//                            kernel.displayFloatingMessagingBoardForDistGroup(group, members);
//                        }
//                    }
                }
                boolean isSelected = !node.isSelected();
                node.setSelected(isSelected);
                if (node.getSelectionMode() == EmsCheckNode.DIG_IN_SELECTION) {
                    if (isSelected) {
                        //tree.expandPath(path);
                        fireNodeSelected(node);
                    }else {
                        //tree.collapsePath(path);
                        fireNodeUnselected(node);
                    }
                }
                ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
                // I need revalidate if node is root.  but why?

                tree.revalidate();
                tree.repaint();

            }
        }

        private void fireNodeSelected(EmsCheckNode node) {
            for (int i = 0; i < selectionListeners.size(); i++){
                selectionListeners.get(i).checkNodeSelected(node);
            }
        }

        private void fireNodeUnselected(EmsCheckNode node) {
            for (int i = 0; i < selectionListeners.size(); i++){
                selectionListeners.get(i).checkNodeUnselected(node);
            }
        }
    }


    class ButtonActionListener implements ActionListener {
        EmsCheckNode root;
        JTextArea textArea;

        ButtonActionListener(EmsCheckNode root, JTextArea textArea) {
            this.root = root;
            this.textArea = textArea;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Enumeration nodeEnum = root.breadthFirstEnumeration();
            while (nodeEnum.hasMoreElements()) {
                EmsCheckNode node = (EmsCheckNode)nodeEnum.nextElement();
                if (node.isSelected()) {
                    TreeNode[] nodes = node.getPath();
                    textArea.append("\n" + nodes[0].toString());
                    for (int i = 1; i < nodes.length; i++) {
                        textArea.append("/" + nodes[i].toString());
                    }
                }
            }
        }
    }

    @Override
    public void setIGroupListTreePanel(IGroupListTreePanel iGroupListTreePanel) {
       this.iGroupListTreePanel = iGroupListTreePanel;
    }

}


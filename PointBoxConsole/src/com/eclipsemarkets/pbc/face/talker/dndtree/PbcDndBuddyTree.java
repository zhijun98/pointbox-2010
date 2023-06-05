/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.gateway.user.*;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.IBuddyListPanel;
import com.eclipsemarkets.pbc.face.talker.IDistributionBuddyListPanel;
import com.eclipsemarkets.pbc.face.talker.IRegularBuddyListPanel;
import com.eclipsemarkets.pbc.face.talker.PbcBuddyListName;
import com.eclipsemarkets.pbc.face.talker.PbcBuddyListType;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import com.eclipsemarkets.web.PointBoxAccountID;
import com.eclipsemarkets.web.PointBoxConnectorID;
import com.eclipsemarkets.web.pbc.talker.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * TREE RULES:<p/>
 * (1) This tree only has 3 levels: root, groups, and buddies;<br>
 * (2) One buddy may belongs to multiple groups;<br>
 * (3) No "Offline Group".<br>
 * (4) Default group is called "Friends".
 * <p/>
 * This class does not consider EDT-based implementation. It assumes that users will 
 * consider this issue. Most of methods suggest to be invoked in EDT. All the methods 
 * of this class are synchronized.
 * <p/>
 * RULE-01: For regular IM-buddy-list panel, one buddy can belongs to one group but not multiple 
 * groups. RULE-02: group's name is unique in the system-wide range and it is used as a distribution 
 * tree node ID.
 * 
 * @author Zhijun Zhang
 */
class PbcDndBuddyTree extends DnDTree implements IPbcDndBuddyTree {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(PbcDndBuddyTree.class.getName());
    }
    
    /**
     * A panel which owns this tree
     */
    private IBuddyListPanel ownerBuddyListPanel;
    
    /**
     * Root of this tree (thread-safe required)
     */
    private DnDMutableTreeNode root;
    
    /**
     * Help group-node search (thread-safe required)
     */
    private final PbcDndBuddyTreeGroupNodeMap groupNodeStorage;
    
    /**
     * Help buddy-node search (thread-safe required)
     */
    private final PbcDndBuddyTreeBuddyNodeMap buddyNodeStorage;
    
    /**
     * Keep track of offline buddy's node (thread-safe required)
     * <p/>
     * key: offlineBuddyNode's groupNode
     * value <offlineBuddy.getIMUniqueName(), offlineBuddyNodes_of_groupNode>
     */
    private final HashMap<DnDGroupTreeNode, HashMap<String, DnDBuddyTreeNode>> offlineBuddyNodeStorage;
    
    /**
     * Show offline buddies or not (thread-safe required)
     */
    private boolean offlineBuddiesRequired;
    
    /**
     * Kernel of PBC
     */
    private IPbcKernel kernel;
    
    /**
     * when a tree model is loaded, it may come from settings saved on the server-side. 
     * But, sometimes, the tree is very new whose settings was not saved on the server-side 
     * yet. In this case, this boolean will be TRUE. Also, another case for this boolean 
     * being TRUE is, after the buddy list was populated by server-side settings, new items 
     * changed on the tree structure. (thread-safe required)
     */
    private boolean realTimePersistentRequired;
    
    /**
     * Control expansion/collapse of tree (thread-safe required)
     */
    private boolean disableSecondaryNodeExpanded;
    
    private final ExecutorService eventService;

    //private final ArrayList<IPbcDndBuddyTreeListener> listeners = new ArrayList<IPbcDndBuddyTreeListener>();
    
    PbcDndBuddyTree(IBuddyListPanel ownerBuddyListPanel, 
                    TreeCellRenderer treeCellRenderer, 
                    DnDMutableTreeNode root, 
                    boolean displayOfflineBuddies,
                    IGatewayConnectorBuddy loginUser) 
    {
        super(root);
        
        eventService = Executors.newSingleThreadExecutor();
        
        disableSecondaryNodeExpanded = false;
        
        this.ownerBuddyListPanel = ownerBuddyListPanel;
        this.root = root;
        this.offlineBuddiesRequired = displayOfflineBuddies;
        this.setTransferHandler(createTransferHandler());
//////        treeStructureListeners = new ArrayList<IDndBuddyTreeStructureListener>();
        groupNodeStorage = new PbcDndBuddyTreeGroupNodeMap();
        buddyNodeStorage = new PbcDndBuddyTreeBuddyNodeMap();
        offlineBuddyNodeStorage = new HashMap<DnDGroupTreeNode, HashMap<String, DnDBuddyTreeNode>>();
        addDropTargetListener(new BuddyListDropTargetListener(this));
        if (treeCellRenderer != null){
            setCellRenderer(treeCellRenderer);
        }
        kernel = getKernel();
        if (root.isLeaf()){//nothing in tree
//            IGatewayConnectorGroup distGroup, 
            if(loginUser!=null){
//                List<IGatewayConnectorBuddy> buddies
                if(loginUser.getIMServerType().equals(GatewayServerType.YIM_SERVER_TYPE)){
                    loadDistributionGroupWithBuddyNodes(GatewayBuddyListFactory.getYahooBuddyGroupInstance(PbcReservedTerms.PbBuddyDefaultGroup.toString(), 
                                                        kernel.getPointBoxConnectorID(loginUser)),
                                                        new ArrayList<IGatewayConnectorBuddy>());
                }else if(loginUser.getIMServerType().equals(GatewayServerType.AIM_SERVER_TYPE)){
                    loadDistributionGroupWithBuddyNodes(GatewayBuddyListFactory.getAimBuddyGroupInstance(PbcReservedTerms.PbBuddyDefaultGroup.toString(), 
                                                        kernel.getPointBoxConnectorID(loginUser)),
                                                        new ArrayList<IGatewayConnectorBuddy>());
                }else{
                    ArrayList<IGatewayConnectorBuddy> buddyList = new ArrayList<IGatewayConnectorBuddy>();
                    if(loginUser.getIMServerType().equals(GatewayServerType.PBIM_SERVER_TYPE)){
                        /**
                         * Every PBIM account should add itself to be its own buddy so that it can use 
                         * of it to do pricing through messaging itself.
                         */
                        buddyList.add(loginUser);
                        loginUser.setNickname(loginUser.getIMScreenName());
                    }
                    loadDistributionGroupWithBuddyNodes(GatewayBuddyListFactory.getPbimBuddyGroupInstance(PbcReservedTerms.PbBuddyDefaultGroup.toString(), 
                                                                                                          kernel.getPointBoxConnectorID(loginUser)), 
                                                        buddyList); //only for Pricer-self
                }
            }else{
                loadDistributionGroupWithBuddyNodes(GatewayBuddyListFactory.getPbimBuddyGroupInstance(PbcReservedTerms.PbBuddyDefaultGroup.toString(), 
                                                    kernel.getPointBoxAccountID()), 
                                                    new ArrayList<IGatewayConnectorBuddy>());    
            }
            this.expandDndTreeModel();
        }
    }

//    @Override
//    public void addPbcDndBuddyTreeListener(IPbcDndBuddyTreeListener listener) {
//        synchronized(listeners){
//            if (!listeners.contains(listener)){
//                listeners.add(listener);
//            }
//        }
//    }
//
//    @Override
//    public void removePbcDndBuddyTreeListener(IPbcDndBuddyTreeListener listener) {
//        synchronized(listeners){
//            listeners.remove(listener);
//        }
//    }
//
//    private void fireGroupAddedIntoTreeEvent(final IGatewayConnectorGroup group) {
//        eventService.submit(new Runnable(){
//            @Override
//            public void run() {
//                synchronized(listeners){
//                    for (IPbcDndBuddyTreeListener listener : listeners){
//                        listener.groupAddedIntoTreeEventHappened(group);
//                    }
//                }
//            }
//        });
//    }
//
//    private void fireGroupRemovedFromTreeEvent(final IGatewayConnectorGroup group) {
//        eventService.submit(new Runnable(){
//            @Override
//            public void run() {
//                synchronized(listeners){
//                    for (IPbcDndBuddyTreeListener listener : listeners){
//                        listener.groupRemovedFromTreeEventHappened(group);
//                    }
//                }
//            }
//        });
//    }
//
//    private void fireBuddyAddedIntoGroupEvent(final IGatewayConnectorBuddy buddy, final IGatewayConnectorGroup group) {
//        eventService.submit(new Runnable(){
//            @Override
//            public void run() {
//                synchronized(listeners){
//                    for (IPbcDndBuddyTreeListener listener : listeners){
//                        listener.buddyAddedIntoGroupEventHappened(buddy, group);
//                    }
//                }
//            }
//        });
//    }
//
//    private void fireBuddyRemovedFromGroupEvent(final IGatewayConnectorBuddy buddy, final IGatewayConnectorGroup group) {
//        eventService.submit(new Runnable(){
//            @Override
//            public void run() {
//                synchronized(listeners){
//                    for (IPbcDndBuddyTreeListener listener : listeners){
//                        listener.buddyRemovedFromGroupEventHappened(buddy, group);
//                    }
//                }
//            }
//        });
//    }

    @Override
    public synchronized void enableSecondaryNodeExpanded() {
        disableSecondaryNodeExpanded = false;
    }

    @Override
    public synchronized void disableSecondaryNodeExpanded() {
        disableSecondaryNodeExpanded = true;
    }

    @Override
    public synchronized boolean isRealTimePersistentRequired() {
        return realTimePersistentRequired;
    }

    @Override
    public synchronized void setRealTimePersistentRequired(boolean realTimePersistentRequired) {
        this.realTimePersistentRequired = realTimePersistentRequired;
    }
    
    private IPbcRuntime getRuntime(){
        return kernel.getPointBoxConsoleRuntime();
    }

    @Override
    protected TransferHandler createTransferHandler() {
        return new PbcDnDBuddyTreeTransferHandler(this, (ownerBuddyListPanel instanceof IRegularBuddyListPanel));
    }

    @Override
    public IPbcKernel getKernel() {
        return ownerBuddyListPanel.getTalker().getKernel();
    }
    
    /**
     * Get this jTree
     * @return 
     */
    @Override
    public JTree getBaseTree() {
        return this;
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
//        System.out.println("treeNodesChanged() .......");
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
//////        //drag and drop happened
//////        fireDndBuddyTreeChangedEvent();
//        System.out.println("treeNodesInserted() .......");
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
//////        //drag and drop happened
//////        fireDndBuddyTreeChangedEvent();
//        System.out.println("treeNodesRemoved() .......");
    }

    /**
     * Whether display offline buddies or buffer them into a storage
     */
    private synchronized boolean isOfflineBuddiesRequired() {
        return offlineBuddiesRequired;
    }

    @Override
    public void sortFromA2Z(final boolean isPersistentRequired) {
        (new SwingWorker<Void, Void>(){
            
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    ArrayList<DnDGroupTreeNode> groupTreeNodes = retrieveAllGroupNodesForTreeModel(1);
                    sortBuddiesFromA2ZForTreeModel(groupTreeNodes, false);
                    loadTreeModelDndGroupTreeNodesInTreeAfterSorting(groupTreeNodes, isPersistentRequired);
                }
                return null;
            }

            @Override
            protected void done() {
                updateUI();
            }
        }).execute();
    }

    @Override
    public void sortFromZ2A(final boolean isPersistentRequired) {
        (new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    ArrayList<DnDGroupTreeNode> groupTreeNodes = retrieveAllGroupNodesForTreeModel(-1);
                    sortBuddiesFromZ2AForTreeModel(groupTreeNodes, false);
                    loadTreeModelDndGroupTreeNodesInTreeAfterSorting(groupTreeNodes, isPersistentRequired);
                }
                return null;
            }

            @Override
            protected void done() {
                updateUI();
            }
        }).execute();
    }

    @Override
    public void sortFromA2Z(final IDnDGroupTreeNode focusGroupNode, final boolean isPersistentRequired) {
        (new SwingWorker<Void, Void>(){
            
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    if (focusGroupNode instanceof DnDGroupTreeNode){
                        sortBuddiesFromA2ZForTreeModel((DnDGroupTreeNode)focusGroupNode, isPersistentRequired);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                updateUI();
            }
        }).execute();
    }

    @Override
    public void sortFromZ2A(final IDnDGroupTreeNode focusGroupNode, final boolean isPersistentRequired) {
        (new SwingWorker<Void, Void>(){
            
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    if (focusGroupNode instanceof DnDGroupTreeNode){
                        sortBuddiesFromZ2AForTreeModel((DnDGroupTreeNode)focusGroupNode, isPersistentRequired);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                updateUI();
            }
        }).execute();
    }

    private synchronized void sortBuddiesFromA2ZForTreeModel(ArrayList<DnDGroupTreeNode> groupTreeNodes, boolean isPersistentRequired) {
        if (groupTreeNodes == null){
            return;
        }
        for (DnDGroupTreeNode aDnDGroupTreeNode : groupTreeNodes){
            sortBuddiesFromA2ZForTreeModel(aDnDGroupTreeNode, isPersistentRequired);
        }
    }

    private synchronized void sortBuddiesFromZ2AForTreeModel(ArrayList<DnDGroupTreeNode> groupTreeNodes, boolean isPersistentRequired) {
        if (groupTreeNodes == null){
            return;
        }
        for (DnDGroupTreeNode aDnDGroupTreeNode : groupTreeNodes){
            sortBuddiesFromZ2AForTreeModel(aDnDGroupTreeNode, isPersistentRequired);
        }
    }

    private synchronized void sortBuddiesFromA2ZForTreeModel(DnDGroupTreeNode aDnDGroupTreeNode, boolean isPersistentRequired) {
        if (aDnDGroupTreeNode == null){
            return;
        }
        ArrayList<DnDBuddyTreeNode> dndBuddyTreeNodes = retrieveSortedBuddyNodesOfGroupNodeForTreeModel(aDnDGroupTreeNode, 1);
        aDnDGroupTreeNode.removeAllChildren();
        for (DnDBuddyTreeNode aDnDBuddyTreeNode : dndBuddyTreeNodes){
            aDnDGroupTreeNode.add(aDnDBuddyTreeNode);
        }
        if (isPersistentRequired && isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
    }

    private synchronized void sortBuddiesFromZ2AForTreeModel(DnDGroupTreeNode aDnDGroupTreeNode, boolean isPersistentRequired) {
        ArrayList<DnDBuddyTreeNode> dndBuddyTreeNodes = retrieveSortedBuddyNodesOfGroupNodeForTreeModel(aDnDGroupTreeNode, -1);
        aDnDGroupTreeNode.removeAllChildren();
        for (DnDBuddyTreeNode aDnDBuddyTreeNode : dndBuddyTreeNodes){
            aDnDGroupTreeNode.add(aDnDBuddyTreeNode);
        }
        if (isPersistentRequired && isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
    }
    
    private synchronized void loadTreeModelDndGroupTreeNodesInTreeAfterSorting(ArrayList<DnDGroupTreeNode> groupTreeNodes, boolean isPersistentRequired){
        if (groupTreeNodes == null){
            return;
        }
        root.removeAllChildren();
        
        for (DnDGroupTreeNode aDnDGroupTreeNode : groupTreeNodes){
            root.add(aDnDGroupTreeNode);
        }
        if (isPersistentRequired && isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
    }

    @Override
    public void displayDndTree(final boolean offlineBuddiesRequired) {
        synchronized(this){
            this.offlineBuddiesRequired = offlineBuddiesRequired;
        }
        (new SwingWorker<Void, Void>(){
            
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    if (offlineBuddiesRequired){
                        loadTreeModelWithOfflineBuddyNodes();
                    }else{
                        loadTreeModelWithoutOfflineBuddyNodes();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                updateUI();
            }
        }).execute();
    }
    
    private synchronized void loadTreeModelWithOfflineBuddyNodes() {
        Set<DnDGroupTreeNode> groupNodes = offlineBuddyNodeStorage.keySet();
        Iterator<DnDGroupTreeNode> itr = groupNodes.iterator();
        DnDGroupTreeNode groupNode;
        HashMap<String, DnDBuddyTreeNode> buddyNodeMap;
        Collection<DnDBuddyTreeNode> buddyNodes;
        //display all the offlineBuddyNodes under their grouNodes
        while(itr.hasNext()){
            groupNode = itr.next();
            buddyNodeMap = offlineBuddyNodeStorage.get(groupNode);
            if (buddyNodeMap != null){
                buddyNodes = buddyNodeMap.values();
                for (DnDBuddyTreeNode buddyNode : buddyNodes){
                    //groupNode.remove(buddyNode);
                    groupNode.add(buddyNode);
                }
            }
        }//while
        offlineBuddyNodeStorage.clear();
    }
    
    private synchronized void loadTreeModelWithoutOfflineBuddyNodes() {
        Enumeration eg = root.children();
        Object groupNodeObj;
        Object buddyNodeObj;
        DnDGroupTreeNode groupNode;
        DnDBuddyTreeNode buddyNode;
        Enumeration eb;
        //buffer all the offlineBuddyNodes into storage
        while (eg.hasMoreElements()){
            groupNodeObj = eg.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                groupNode = (DnDGroupTreeNode)groupNodeObj;
                //get buddies of the group for loginUser
                eb = groupNode.children();
                while (eb.hasMoreElements()){
                    buddyNodeObj = eb.nextElement();
                    if (buddyNodeObj instanceof IDnDBuddyTreeNode){
                        buddyNode = (DnDBuddyTreeNode)buddyNodeObj;
                        if (!(BuddyStatus.Online.equals(buddyNode.getGatewayConnectorBuddy().getBuddyStatus()))){
                            bufferOfflineBuddyNodeForTreeModel(groupNode, buddyNode);
                        }
                    }
                }
            }
        }//while
        //remove all the offlineBuddyNodes from their groupNodes
        HashMap<String, DnDBuddyTreeNode> buddyNodesMap;
        Set<DnDGroupTreeNode> groupNodes = offlineBuddyNodeStorage.keySet();
        Iterator<DnDGroupTreeNode> itr = groupNodes.iterator();
        while(itr.hasNext()){
            groupNode = itr.next();
            buddyNodesMap = offlineBuddyNodeStorage.get(groupNode);
            if (buddyNodesMap != null){
                Collection<DnDBuddyTreeNode> buddyNodes = buddyNodesMap.values();
                for (DnDBuddyTreeNode aBuddyNode : buddyNodes){
                    try{
                        groupNode.remove(aBuddyNode);
                    }catch (Exception ex){
                        //java.lang.IllegalArgumentException
                    }
                }
            }
        }//while
    }

    /**
     * Buffer it into the memory. Notice that this method does not remove buddyNode from its groupNode 
     * @param groupNode
     * @param buddyNode 
     */
    private synchronized void bufferOfflineBuddyNodeForTreeModel(DnDGroupTreeNode groupNode, DnDBuddyTreeNode buddyNode) {
        HashMap<String, DnDBuddyTreeNode> buddyNodesMap = offlineBuddyNodeStorage.get(groupNode);
        if (buddyNodesMap == null){
            buddyNodesMap = new HashMap<String, DnDBuddyTreeNode>();
            offlineBuddyNodeStorage.put(groupNode, buddyNodesMap);
        }
        if (!buddyNodesMap.containsKey(buddyNode.getGatewayConnectorBuddy().getIMUniqueName())){
            buddyNodesMap.put(buddyNode.getGatewayConnectorBuddy().getIMUniqueName(), buddyNode);
        }
    }

    /**
     * 
     * @param logoutUser - reserved for the future
     */
    @Override
    public synchronized void refreshDndBuddyTree(final IGatewayConnectorBuddy logoutUser) {
        if (logoutUser != null){
            if (!isOfflineBuddiesRequired()){
                (new SwingWorker<Void, Void>(){
                    @Override
                    protected Void doInBackground() throws Exception {
                        synchronized(PbcDndBuddyTree.this){
                            loadTreeModelWithoutOfflineBuddyNodes();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        updateUI();
                    }
                }).execute();
            }
        }
    }

    /**
     * Refresh GUI of this tree so as to display the "buddy"'s status correctly. 
     * It could be "buddy-status change event happened" or "a new buddy presented 
     * by the server-side (possibly offline or online)"
     * @param buddy 
     */
    private synchronized void refreshTreeModelForBuddy(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (!isOfflineBuddiesRequired()){
            if (BuddyStatus.Online.equals(buddy.getBuddyStatus())){
                loadTreeModelOnlineBuddyNodesFromOfflineBuddyNodeStorage(buddy);
            }else{
                DnDGroupTreeNode groupNode;
                Enumeration eg = root.children();
                Object groupNodeObj;
                DnDBuddyTreeNode buddyNode;
                while (eg.hasMoreElements()){
                    groupNodeObj = eg.nextElement();
                    if (groupNodeObj instanceof DnDGroupTreeNode){
                        groupNode = (DnDGroupTreeNode)groupNodeObj;
                        buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
                        if (buddyNode != null){
                            bufferOfflineBuddyNodeForTreeModel(groupNode, buddyNode);
                            try{
                                groupNode.remove(buddyNode);
                            } catch (Exception ex){
                                //java.lang.IllegalArgumentException
                            }
                        }
                    }
                }//while-loop
            }//if
        }//if
    }

    /**
     * if buddy is null, it means all the buddyNodes in the offlineBuddyStorage should 
     * be displayed even if its associated buddies are offline. If buddy 
     * @param buddy 
     */
    private synchronized void loadTreeModelOnlineBuddyNodesFromOfflineBuddyNodeStorage(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (BuddyStatus.Online.equals(buddy.getBuddyStatus())){
            Set<DnDGroupTreeNode> groupNodes = offlineBuddyNodeStorage.keySet();
            Iterator<DnDGroupTreeNode> itr = groupNodes.iterator();
            DnDGroupTreeNode groupNode;
            HashMap<String, DnDBuddyTreeNode> buddyNodeMap;
            //display all the offlineBuddyNodes under their grouNodes
            while(itr.hasNext()){
                groupNode = itr.next();
                buddyNodeMap = offlineBuddyNodeStorage.get(groupNode);
                if (buddyNodeMap != null){
                    DnDBuddyTreeNode buddyNode = buddyNodeMap.get(buddy.getIMUniqueName());
                    if (buddyNode != null){
                        if (buddy.equals(buddyNode.getGatewayConnectorBuddy())) {
                            //groupNode.remove(buddyNode);
                            groupNode.add(buddyNode);
                            buddyNodeMap.remove(buddy.getIMUniqueName());
                        }
                    }
                }
            }//while
        }
    }

    /**
     * Expand this tree to make every node visible
     */
    @Override
    public synchronized void expandDndTreeModel() {
        expandDndTreeForTreeModel();
    }
    
    private synchronized void expandDndTreeForTreeModel(){
        for (int i = 0; i < getRowCount(); i++) {
            expandPath(getPathForRow(i));
        }
    }

//    @Override
//    public void expandPath(final TreePath path) {
//        expandPathForTreeModel(path);
//    }
    
//    private synchronized void expandPathForTreeModel(final TreePath path) {
//        if (disableSecondaryNodeExpanded){
//            if (path != null){
//                if (path.getPathCount() >= 2){  //always expand the root
//                    return;
//                }
//            }
//        }
//        super.expandPath(path); //expand children
//    }

    @Override
    public void expandDndTreeForGroup(final IGatewayConnectorGroup group) {
        if (SwingUtilities.isEventDispatchThread()){
            expandDndTreeForGroupHelper(group);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    expandDndTreeForGroupHelper(group);
                }
            });
        }
    }
    
    private synchronized void expandDndTreeForGroupHelper(IGatewayConnectorGroup group) {
        IDnDGroupTreeNode groupNode = retrieveDnDGroupTreeNodeFromTreeModel(group);
        if (groupNode == null) {
            TreePath treePath = new TreePath(groupNode.getNodePath());
            setSelectionPath(treePath);
//            expandPathForTreeModel(treePath);
            expandPath(treePath);
            if (treePath != null){
                scrollPathToVisible(treePath);
            }
        }
    }

    @Override
    public void expandDndTreeForBuddy(final IGatewayConnectorBuddy buddy, final boolean selected) {
        if (SwingUtilities.isEventDispatchThread()){
            expandDndTreeForBuddyHelper(buddy, true);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    expandDndTreeForBuddyHelper(buddy, true);
                }
            });
        }
    }
    private synchronized void expandDndTreeForBuddyHelper(final IGatewayConnectorBuddy buddy, final boolean selected) {
        ArrayList<IDnDBuddyTreeNode> buddyNodes = retrieveDisplayedBuddyNodesForTreeModel(buddy);
        if ((buddyNodes == null) || (buddyNodes.isEmpty())){
        }else{
            TreePath treePath = null;
            for (IDnDBuddyTreeNode buddyNode : buddyNodes){
                treePath = new TreePath(buddyNode.getNodePath());
                if (selected){
                    setSelectionPath(treePath);
                }
//                expandPathForTreeModel(treePath);
                expandPath(treePath);
            }
            if (treePath != null){
                scrollPathToVisible(treePath);
            }
        }
    }

    @Override
    public void loadDistributionGroupWithBuddyNode(final IGatewayConnectorGroup distGroup, final IGatewayConnectorBuddy buddy) {
        if (distGroup == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            loadDistributionGroupWithBuddyNodeHelper(distGroup, buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    loadDistributionGroupWithBuddyNodeHelper(distGroup, buddy);
                }
            });
        }
    }
    private synchronized void loadDistributionGroupWithBuddyNodeHelper(final IGatewayConnectorGroup distGroup, final IGatewayConnectorBuddy buddy) {
        //create members node under the corresponding distGroup's node
        DnDGroupTreeNode groupNode = acquireDnDGroupTreeNodeForTreeModel(distGroup);
        if (groupNode != null){
            acquireDnDBuddyTreeNodeForTreeModel(groupNode, buddy);
        }
        updateUI();
    }

    @Override
    public void loadExistingGroupWithBuddyNode(final String existingGroupName, final IGatewayConnectorBuddy buddy) {
        if (existingGroupName == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            loadExistingGroupWithBuddyNodeHelper(existingGroupName, buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    loadExistingGroupWithBuddyNodeHelper(existingGroupName, buddy);
                }
            });
        }
    }
    private synchronized void loadExistingGroupWithBuddyNodeHelper(final String existingGroupName, final IGatewayConnectorBuddy buddy) {
        //create members node under the corresponding distGroup's node
        DnDGroupTreeNode groupNode = groupNodeStorage.get(existingGroupName);
        if (groupNode != null){
            acquireDnDBuddyTreeNodeForTreeModel(groupNode, buddy);
        }
        updateUI();
    }

    /**
     * @param distGroup
     * @param members 
     */
    @Override
    public void loadDistributionGroupWithBuddyNodes(final IGatewayConnectorGroup distGroup, 
                                                    final List<IGatewayConnectorBuddy> buddies) 
    {
        if (distGroup == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            loadDistributionGroupWithBuddyNodesHelper(distGroup, buddies) ;
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    loadDistributionGroupWithBuddyNodesHelper(distGroup, buddies) ;
                }
            });
        }
    }
    private synchronized void loadDistributionGroupWithBuddyNodesHelper(final IGatewayConnectorGroup distGroup, 
                                                           final List<IGatewayConnectorBuddy> buddies) 
    {
        //create members node under the corresponding distGroup's node
        DnDGroupTreeNode groupNode = acquireDnDGroupTreeNodeForTreeModel(distGroup);
        for (IGatewayConnectorBuddy buddy : buddies){
            acquireDnDBuddyTreeNodeForTreeModel(groupNode, buddy);
        }
        updateUI();
    }

    /**
     * 
     * @param loginConnectorID
     * @param groupItem
     * @param buddyItems - if this is NULL or empty, an empty group node will be added onto the tree
     */
    @Override
    public void loadDistributionGroupWithBuddyNodes(final PointBoxConnectorID loginConnectorID, 
                                                                 final BuddyListGroupItem groupItem, 
                                                                 final BuddyListBuddyItem[] buddyItems) 
    {
        //if ((groupItem == null) || (buddyItems == null) || (loginConnectorID == null)) {
        if ((groupItem == null) || (loginConnectorID == null)) {
            return;
        }
        final IGatewayConnectorBuddy pointBoxLoginUser = GatewayBuddyListFactory.getLoginUserInstance(
                loginConnectorID.getLoginName(), 
                GatewayServerType.convertToType(loginConnectorID.getGatewayServerType()));
        if (pointBoxLoginUser == null){
            return;
        }
        
        if (SwingUtilities.isEventDispatchThread()){
            loadDistributionGroupWithBuddyNodesHelper(loginConnectorID, groupItem, buddyItems);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    loadDistributionGroupWithBuddyNodesHelper(loginConnectorID, groupItem, buddyItems);
                }
            });
        }
    }
    private synchronized void loadDistributionGroupWithBuddyNodesHelper(final PointBoxConnectorID loginConnectorID, 
                                                                 final BuddyListGroupItem groupItem, 
                                                                 final BuddyListBuddyItem[] buddyItems) 
    {
        ConferenceItem aConferenceItem = groupItem.getConferenceItem();
        if (aConferenceItem == null){
            //non-conference
            loadTreeModelDistributionGroupWithBuddyNodesHelper(loginConnectorID, groupItem, buddyItems);
        }else{
            //conference
            loadTreeModelConferenceGroupWithBuddyNodesHelper(loginConnectorID, groupItem, buddyItems);
        }
        updateUI();
        
    }

    private synchronized void loadTreeModelConferenceGroupWithBuddyNodesHelper(PointBoxConnectorID loginConnectorID, 
                                                         BuddyListGroupItem groupItem, 
                                                         BuddyListBuddyItem[] buddyItems) 
    {
        final IGatewayConnectorBuddy pointBoxLoginUser = GatewayBuddyListFactory.getLoginUserInstance(
                loginConnectorID.getLoginName(), 
                GatewayServerType.convertToType(loginConnectorID.getGatewayServerType()));
        if (pointBoxLoginUser == null){
            return;
        }
        ConferenceItem aConferenceItem = groupItem.getConferenceItem();
        if (aConferenceItem == null){
            return;
        }
        IGatewayConnectorBuddy hoster;
        boolean saveGroup;
        if (kernel.getPointBoxLoginUser().getIMScreenName().equalsIgnoreCase(aConferenceItem.getHosterName())){
            hoster = kernel.getPointBoxLoginUser();
            saveGroup = true;
        }else{
            hoster = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(kernel.getPointBoxLoginUser(), aConferenceItem.getHosterName(), getRuntime());
            saveGroup = false;
        }
        IPointBoxConferenceGroup aConfGroup = GatewayBuddyListFactory.constructPointBoxConferenceGroup(groupItem.getGroupName(), hoster);
        aConfGroup.setSavingRequired(saveGroup);
        aConfGroup.setConferenceDetails(aConferenceItem.getDetails());
        GregorianCalendar dateTime = new GregorianCalendar();
        dateTime.setTimeInMillis(aConferenceItem.getDatetime());
        aConfGroup.setDateTime(dateTime);
        aConfGroup.setPrivacy(ConferencePrivacy.convertToType(aConferenceItem.getPrivacy()));
        aConfGroup.setPersistency(ConferencePersistency.convertToType(aConferenceItem.getPersistency()));
        aConfGroup.setLifeStatus(ConferenceLifeStatus.convertToType(aConferenceItem.getLifeStatus()));
        aConfGroup.setSubject(groupItem.getGroupName());
        ArrayList<IGatewayConnectorBuddy> attendants = new ArrayList<IGatewayConnectorBuddy>();
        aConfGroup.setAttendants(attendants);
        
        DnDGroupTreeNode groupNode = this.acquireDnDGroupTreeNodeForTreeModel(aConfGroup);
        BuddyListBuddyItem buddyItem;
        IGatewayConnectorBuddy buddy;
        IGatewayConnectorBuddy loginUser = kernel.getPointBoxLoginUser();
        IPbcRuntime runtime = getRuntime();
        if (buddyItems != null){
            for (int i = 0; i < buddyItems.length; i++){
                buddyItem = buddyItems[i];
                buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(loginUser,
                                                                                buddyItem.getBuddyName(),
                                                                                aConfGroup.getGroupName(), 
                                                                                runtime);
                buddy.setConferenceAttitude(aConfGroup, ConferenceAttitude.convertToType(buddyItem.getStatusInGroup()));
                attendants.add(buddy);
                acquireDnDBuddyTreeNodeForTreeModel(groupNode, buddy);
            }//for
        }//if
    }

    private synchronized void loadTreeModelDistributionGroupWithBuddyNodesHelper(PointBoxConnectorID loginConnectorID, 
                                                           BuddyListGroupItem groupItem, 
                                                           BuddyListBuddyItem[] buddyItems) 
    {
        final IGatewayConnectorBuddy pointBoxLoginUser = GatewayBuddyListFactory.getLoginUserInstance(
                loginConnectorID.getLoginName(), 
                GatewayServerType.convertToType(loginConnectorID.getGatewayServerType()));
        if (pointBoxLoginUser == null){
            return;
        }
        IGatewayConnectorGroup distGroup = GatewayBuddyListFactory.getDistributionGroupInstance(pointBoxLoginUser, 
                                                                                                groupItem.getGroupName());
        distGroup.setGroupDescription(groupItem.getGroupDescription());
        
        DnDGroupTreeNode groupNode = this.acquireDnDGroupTreeNodeForTreeModel(distGroup);
        BuddyListBuddyItem buddyItem;
        IPbcRuntime runtime = getRuntime();
        if (buddyItems != null){
            for (int i = 0; i < buddyItems.length; i++){
                buddyItem = buddyItems[i];
                IGatewayConnectorBuddy buddy;
                /**
                 * Distribution list is a mixed-typed list which contains buddies of different sever types. And also, 
                 * buddies may have no loginUserOwner in the initialization stage.
                 */
                if (this.ownerBuddyListPanel instanceof IDistributionBuddyListPanel){
                    buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(
                                                        GatewayBuddyListFactory.getLoginUserInstance(buddyItem.getLoginOwnerName(),
                                                                                                     GatewayServerType.convertToType(buddyItem.getServerType())),
                                                        buddyItem.getBuddyName(),
                                                        distGroup.getGroupName(), 
                                                        runtime);
                }else{
                //this case is panel which is not IDistributionBuddyListPanel
                    buddy = GatewayBuddyListFactory.getGatewayConnectorBuddyInstance(
                                                        GatewayBuddyListFactory.getLoginUserInstance(loginConnectorID.getLoginName(),
                                                                                                     GatewayServerType.convertToType(buddyItem.getServerType())),
                                                        buddyItem.getBuddyName(),
                                                        distGroup.getGroupName(), 
                                                        runtime);
                }
                acquireDnDBuddyTreeNodeForTreeModel(groupNode, buddy);
            }//for
        }//if
    }

    @Override
    public synchronized void handleBuddySubscriptionEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            logger.log(Level.SEVERE, null, new Exception("[TECH] Parameter-buddy cannot be NULL."));
            return;
        }
        GatewayServerType targetServerType = buddy.getIMServerType();
        final String remoteBuddyScreenName = buddy.getIMScreenName();
        
        if (isBuddyNodeLoaded(buddy)){
            if (targetServerType.equals(GatewayServerType.YIM_SERVER_TYPE)){
                //it has been there on the buddy list but it was not accepted on the server-side yet
                kernel.acceptNewBuddyAuthorization(buddy.getLoginOwner(), remoteBuddyScreenName);
            }else if (targetServerType.equals(GatewayServerType.PBIM_SERVER_TYPE)){
                /**
                 * There is a case that a buddy has been displayed on the buddy list but, meanwhile, 
                 * this buddy's subscription was not formally accepted by current user. For example, 
                 * PBIM server may send "item-presented" and "buddy-subscribed" events at the same 
                 * time. In this case, the first one forces PBC add buddy onto the buddy list. The 
                 * second one here will handle the subscription event.
                 * 
                 * In this case, buddy node will be removed first and then handle subscription. This 
                 * fixed ticket-479
                 */
                this.removeBuddyFromGroup(buddy, buddy.getBuddyGroupName());
                if (SwingUtilities.isEventDispatchThread()){
                    displayConfirmNewBuddyDialogHelper(buddy);
                }else{
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            displayConfirmNewBuddyDialogHelper(buddy);
                        }
                    });
                }
//                //it has been there on the buddy list but it was not accepted on the server-side yet
//                kernel.acceptNewBuddyAuthorization(buddy.getLoginOwner(), remoteBuddyScreenName);
//                String msg = buddy.getIMScreenName() + " just add " + kernel.getPointBoxLoginUser().getIMScreenName() + " as his/her buddy.";
//                kernel.updateSplashScreen(msg, Level.INFO, 1000);
            }else{
                //other IM types do nothing
            }
        }else{
            if (SwingUtilities.isEventDispatchThread()){
                displayConfirmNewBuddyDialogHelper(buddy);
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        displayConfirmNewBuddyDialogHelper(buddy);
                    }
                });
            }
        }
    }
    
    private void displayConfirmNewBuddyDialogHelper(final IGatewayConnectorBuddy buddy){
        if ((buddy.getLoginOwner() != null) 
                && (!buddy.getLoginOwner().getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName()))
                && (BuddyStatus.Online.equals(buddy.getLoginOwner().getBuddyStatus())))
        {
            ConfirmNewBuddyDialog dialog = ConfirmNewBuddyDialog.getSingleton(null, 
                        true, 
                        this, buddy.getLoginOwner(), buddy, retrieveAllGroups(true), ConfirmNewBuddyDialogType.AcceptBuddy);
            dialog.setVisible(true);
            dialog.setLocation(SwingGlobal.getCenterPointOfParentWindow(getKernel().getPointBoxMainFrame(), dialog));
        }
    }

    @Override
    public void handleBuddyStatusChangedEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            handleBuddyStatusChangedEventHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleBuddyStatusChangedEventHelper(buddy);
                }
            });
        }
    }
    private synchronized void handleBuddyStatusChangedEventHelper(final IGatewayConnectorBuddy buddy) {
        refreshTreeModelForBuddy(buddy);
        expandDndTreeForTreeModel();
        updateUI();
    }

    @Override
    public void handleBuddyStatusChangedEventInBatch(final List<IGatewayConnectorBuddy> aBuddyList) {
        if ((aBuddyList == null) || (aBuddyList.isEmpty())){
            return;
        }
        
//        System.out.println(">>>>>>>>>>>> handleBuddyStatusChangedEventInBatch = "
//                + this.ownerBuddyListPanel.getBuddyListType().toString() + ": " 
//                + this.ownerBuddyListPanel.getDistListName()
//                + this.ownerBuddyListPanel.getMasterLoginUser().getIMUniqueName()
//                + " = aBuddyList.size(): " + aBuddyList.size());
        
        (new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    for (IGatewayConnectorBuddy buddy : aBuddyList){
                        refreshTreeModelForBuddy(buddy);
                    }//for
                }//synchronized
                expandDndTreeForTreeModel();
                return null;
            }

            @Override
            protected void done() {
                try{
                    updateUI();
                }catch(Exception ex){}
            }
        }).execute();
    }

    /**
     * 
     * @param buddy 
     */
    @Override
    public void handleBuddyItemPresentedEvent(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            handleBuddyItemPresentedEventHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    handleBuddyItemPresentedEventHelper(buddy);
                }
            });
        }
    }
    private synchronized void handleBuddyItemPresentedEventHelper(final IGatewayConnectorBuddy buddy) {
        handleBuddyItemPresentedEventHelper_share(buddy);
        expandDndTreeModel();
        updateUI();
    }
    private synchronized void handleBuddyItemPresentedEventHelper_share(final IGatewayConnectorBuddy buddy) {
        ArrayList<IDnDBuddyTreeNode> buddyNodes = retrieveDisplayedBuddyNodesForTreeModel(buddy);
        if (buddyNodes.isEmpty()){
            //create a new buddy node
            String groupName = buddy.getBuddyGroupName();
            if (DataGlobal.isEmptyNullString(groupName)){
                //create buddy node under default group node
                acquireDnDBuddyTreeNodeForTreeModel(
                        acquireDnDGroupTreeNodeForTreeModel(
                            GatewayBuddyListFactory.getDefaultDistributionGroupInstance(getKernel().getPointBoxLoginUser())), 
                        buddy);
            }else{
                //Get this group's distribution group
                IGatewayConnectorGroup group = GatewayBuddyListFactory.getGatewayConnectorGroupInstance(buddy.getLoginOwner(), groupName);

                //create buddy node under the corresponding distGroup's node
                acquireDnDBuddyTreeNodeForTreeModel(acquireDnDGroupTreeNodeForTreeModel(group), 
                                             buddy);
            }
        }else{
            //update existing buddyNodes if necessary
            //cell-render will handle its image change according to buddy-status
        }
    }

    @Override
    public void handleBuddyItemPresentedEventInBatch(final List<IGatewayConnectorBuddy> aBuddyList) {
        if ((aBuddyList == null) || (aBuddyList.isEmpty())){
            return;
        }
        (new SwingWorker<Void, Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                synchronized(PbcDndBuddyTree.this){
                    for (IGatewayConnectorBuddy buddy : aBuddyList){
                        handleBuddyItemPresentedEventHelper_share(buddy);
                    }//for
                    expandDndTreeModel();
                }//synchronized
                return null;
            }

            @Override
            protected void done() {
                updateUI();
            }
        }).execute();
    }

    /**
     * If the tree does not show offline-buddy nodes, if such a buddy has offline-node 
     * in this tree, this method still returns TRUE.
     * @param buddy
     * @return 
     */
    @Override
    public synchronized boolean isBuddyNodeLoaded(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return true;
        }
        if (isBuddyNodeDisplayed(buddy)){
            return true;
        }
        /**
         * search it in offline node storage
         */
        boolean result = false;
        Set<DnDGroupTreeNode> keys = offlineBuddyNodeStorage.keySet();
        Iterator<DnDGroupTreeNode> itr = keys.iterator();
        HashMap<String, DnDBuddyTreeNode> buddyNodeMap;
        DnDBuddyTreeNode buddyNode;
        while(itr.hasNext()){
            buddyNodeMap = offlineBuddyNodeStorage.get(itr.next());
            if (buddyNodeMap != null){
                buddyNode = buddyNodeMap.get(buddy.getIMUniqueName());
                if (buddyNode != null){
                    result = true;
                    break;  //while-loop
                }
            }
        }//while
        return result;
    }

    @Override
    public synchronized IGatewayConnectorGroup retrieveGroupOfBuddy(IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return null;
        }
        IGatewayConnectorGroup result = null;
        Enumeration eg = root.children();
        Object groupNodeObj;
        IDnDBuddyTreeNode buddyNode;
        while (eg.hasMoreElements()){
            groupNodeObj = eg.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel((DnDGroupTreeNode)groupNodeObj, buddy);
                if (buddyNode != null){
                    result = ((DnDGroupTreeNode)groupNodeObj).getGatewayConnectorGroup();
                    break;
                }
            }
        }//while
        if (result != null){
            /**
             * search it in offline node storage
             */
            Set<DnDGroupTreeNode> keys = offlineBuddyNodeStorage.keySet();
            Iterator<DnDGroupTreeNode> itr = keys.iterator();
            HashMap<String, DnDBuddyTreeNode> buddyNodeMap;
            DnDGroupTreeNode groupNode; 
            while(itr.hasNext()){
                groupNode = itr.next();
                buddyNodeMap = offlineBuddyNodeStorage.get(groupNode);
                if (buddyNodeMap != null){
                    buddyNode = buddyNodeMap.get(buddy.getIMUniqueName());
                    if (buddyNode != null){
                        result = groupNode.getGatewayConnectorGroup();
                        break;  //while-loop
                    }
                }
            }//while
        }
        return result;
    }

    /**
     * Whether or not this buddy node is displayed (or visible) on the tree.
     * @param buddy
     * @return 
     */
    @Override
    public synchronized boolean isBuddyNodeDisplayed(IGatewayConnectorBuddy buddy) {
        boolean result = false;
        Enumeration eg = root.children();
        Object groupNodeObj;
        IDnDBuddyTreeNode buddyNode;
        while (eg.hasMoreElements()){
            groupNodeObj = eg.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel((DnDGroupTreeNode)groupNodeObj, buddy);
                if (buddyNode != null){
                    result = true;
                    break;
                }
            }
        }//while
        return result;
    }

    /**
     * This is used only for sorting
     * @param aDnDGroupTreeNode
     * @param sortValue
     * @return 
     */
    private ArrayList<DnDBuddyTreeNode> retrieveSortedBuddyNodesOfGroupNodeForTreeModel(DnDGroupTreeNode aDnDGroupTreeNode, int sortValue) {
        ArrayList<DnDBuddyTreeNode> buddyNodes = new ArrayList<DnDBuddyTreeNode>();
        Enumeration eg = aDnDGroupTreeNode.children();
        Object buddyNodeObj;
        while (eg.hasMoreElements()){
            buddyNodeObj = eg.nextElement();
            if (buddyNodeObj instanceof DnDBuddyTreeNode){
                buddyNodes.add((DnDBuddyTreeNode)buddyNodeObj);
            }
        }
        //sorting...
        if (sortValue != 0){
            if (sortValue > 0){
                Collections.sort(buddyNodes, new Comparator(){
                    @Override
                    public int compare(Object o1, Object o2) {
                        if ((o1 instanceof DnDBuddyTreeNode) && ((o2 instanceof DnDBuddyTreeNode))){
                            return ((DnDBuddyTreeNode)o1).getGatewayConnectorBuddy().getUniqueNickname()
                                    .compareToIgnoreCase(((DnDBuddyTreeNode)o2).getGatewayConnectorBuddy().getUniqueNickname());
                        }else{
                            return 0;
                        }
                    }
                });
            }else{
                Collections.sort(buddyNodes, new Comparator(){
                    @Override
                    public int compare(Object o1, Object o2) {
                        if ((o1 instanceof DnDBuddyTreeNode) && ((o2 instanceof DnDBuddyTreeNode))){
                            return ((DnDBuddyTreeNode)o2).getGatewayConnectorBuddy().getUniqueNickname()
                                    .compareToIgnoreCase(((DnDBuddyTreeNode)o1).getGatewayConnectorBuddy().getUniqueNickname());
                        }else{
                            return 0;
                        }
                    }
                });
            }
        }
        return buddyNodes;
    }

    /**
     * sortValue = 0, no sorting
     * sortValue = 1, sorting from A to Z
     * sortValue = -1, sorting from Z to A
     * @param sortValue
     * @return 
     */
    private synchronized ArrayList<DnDGroupTreeNode> retrieveAllGroupNodesForTreeModel(int sortValue) {
        ArrayList<DnDGroupTreeNode> groupNodes = new ArrayList<DnDGroupTreeNode>();
        Enumeration eg = root.children();
        Object groupNodeObj;
        while (eg.hasMoreElements()){
            groupNodeObj = eg.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                groupNodes.add((DnDGroupTreeNode)groupNodeObj);
            }
        }
        if (sortValue != 0){
            if (sortValue > 0){
                Collections.sort(groupNodes, new Comparator(){
                    @Override
                    public int compare(Object o1, Object o2) {
                        if ((o1 instanceof DnDGroupTreeNode) && ((o2 instanceof DnDGroupTreeNode))){
                            return ((DnDGroupTreeNode)o1).getGatewayConnectorGroup().getIMUniqueName()
                                    .compareToIgnoreCase(((DnDGroupTreeNode)o2).getGatewayConnectorGroup().getIMUniqueName());
                        }else{
                            return 0;
                        }
                    }
                });
            }else{
                Collections.sort(groupNodes, new Comparator(){
                    @Override
                    public int compare(Object o1, Object o2) {
                        if ((o1 instanceof DnDGroupTreeNode) && ((o2 instanceof DnDGroupTreeNode))){
                            return ((DnDGroupTreeNode)o2).getGatewayConnectorGroup().getIMUniqueName()
                                    .compareToIgnoreCase(((DnDGroupTreeNode)o1).getGatewayConnectorGroup().getIMUniqueName());
                        }else{
                            return 0;
                        }
                    }
                });
            }
        }
        return groupNodes;
    }

    /**
     * Get all the buddy nodes from the tree, which are associated with a specific "buddy"
     * @param buddy
     * @return 
     */
    private synchronized ArrayList<IDnDBuddyTreeNode> retrieveDisplayedBuddyNodesForTreeModel(IGatewayConnectorBuddy buddy) {
        ArrayList<IDnDBuddyTreeNode> buddyNodes = new ArrayList<IDnDBuddyTreeNode>();
        if (PbcBuddyListType.RegularBuddyList.toString().equalsIgnoreCase(ownerBuddyListPanel.getBuddyListType())){
            //for regular buddy list, it does not permit buddy staying in different group nodes. Thus, buddy's own group will be used
            DnDGroupTreeNode groupNode = groupNodeStorage.get(buddy.getBuddyGroupName());
            if (groupNode != null){
                IDnDBuddyTreeNode buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
                if (buddyNode != null){
                    buddyNodes.add(buddyNode);
                }
            }
        }else{
            //for NON-regular buddy list permits buddy belongs to different group nodes
            Enumeration eg = root.children();
            Object groupNodeObj;
            IDnDBuddyTreeNode buddyNode;
            while (eg.hasMoreElements()){
                groupNodeObj = eg.nextElement();
                if (groupNodeObj instanceof DnDGroupTreeNode){
                    buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel((DnDGroupTreeNode)groupNodeObj, buddy);
                    if (buddyNode != null){
                        buddyNodes.add(buddyNode);
                    }
                }
            }
        }//if
        return buddyNodes;
    }  
    
    /**
     * Retrieve an existing groupNode under the root.
     * @param distGroup
     * @return 
     */
    private synchronized IDnDGroupTreeNode retrieveDnDGroupTreeNodeFromTreeModel(IGatewayConnectorGroup distGroup) {
        return groupNodeStorage.get(distGroup.getGroupName());
    }
    
    @Override
    public synchronized IGatewayConnectorGroup renameDnDGroupTreeNode(IGatewayConnectorGroup groupWithOldName, String newGroupName) {
        //remove the old one from group storage
        String oldGroupNameKey = groupWithOldName.getGroupName();
        DnDGroupTreeNode groupNode = groupNodeStorage.get(oldGroupNameKey);
        if (groupNode == null){
            return groupWithOldName;
        }
        groupNodeStorage.remove(oldGroupNameKey);
        //add the new one into group storage
        groupWithOldName.setGroupName(newGroupName);
        groupNode.getGatewayConnectorGroup().setGroupName(newGroupName);
        groupNodeStorage.put(newGroupName, groupNode);
        //modify buddy storage
        HashMap<String, DnDBuddyTreeNode> buddyNodeObj = buddyNodeStorage.get(oldGroupNameKey);
        if (buddyNodeObj != null){
            buddyNodeStorage.remove(oldGroupNameKey);
            buddyNodeStorage.put(newGroupName, buddyNodeObj);
        }
        
        if (isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
        return groupNode.getGatewayConnectorGroup();
    }    

    private synchronized DnDBuddyTreeNode retrieveDndBuddyTreeNodeForTreeModel(IGatewayConnectorBuddy buddy) {
        DnDBuddyTreeNode result = null;
        Collection<HashMap<String, DnDBuddyTreeNode>> buddyNodeObjList = buddyNodeStorage.values();
        if (buddyNodeObjList != null){
            for (HashMap<String, DnDBuddyTreeNode> buddyNodeObj : buddyNodeObjList){
                result = buddyNodeObj.get(buddy.getIMUniqueName());
                if (result != null){
                    break;
                }
            }//for
        }
        return result;
    }
    
    /**
     * get the buddy node associated with buddy ubder groupNode
     * @param groupNode - (1) cannot be NULL; (2) its internal group instance cannot be NULL
     * @param buddy
     * @return - could be NULL
     */
    private synchronized DnDBuddyTreeNode retrieveDndBuddyTreeNodeForTreeModel(DnDGroupTreeNode groupNode, IGatewayConnectorBuddy buddy){
//        Enumeration e = groupNode.children();
//        DnDBuddyTreeNode buddyNode = null;
//        while(e.hasMoreElements()){
//            buddyNode = (DnDBuddyTreeNode)e.nextElement();
//            if (buddy.getIMUniqueName().equalsIgnoreCase(buddyNode.getGatewayConnectorBuddy().getIMUniqueName())){
//                break;
//            }else{
//                buddyNode = null;
//            }
//        }
//        return buddyNode;
        HashMap<String, DnDBuddyTreeNode> buddyNodeObj = buddyNodeStorage.get(groupNode.getGatewayConnectorGroup().getGroupName());
        if (buddyNodeObj == null){
            return null;
        }
        return buddyNodeObj.get(buddy.getIMUniqueName());
    }
    
    /**
     * It could return NULL if distGroup's node was not there
     * @param distGroup
     * @return 
     */
    private synchronized DnDGroupTreeNode getDnDGroupTreeNode(IGatewayConnectorGroup distGroup) {
        if (!SwingUtilities.isEventDispatchThread()){
            logger.log(Level.SEVERE, null, new Exception("This method should be run in EDT because of fireDndBuddyTreeChangedEvent()"));
        }
        return groupNodeStorage.get(distGroup.getGroupName());
    }
    
    /**
     * Create a new node under the root
     * @param distGroup
     * @return 
     */
    private synchronized DnDGroupTreeNode acquireDnDGroupTreeNodeForTreeModel(IGatewayConnectorGroup distGroup) {
        DnDGroupTreeNode groupNode;
        boolean isPersistentRequired = false;
        String groupName = distGroup.getGroupName();
        if (groupNodeStorage.containsKey(groupName)){
            groupNode = groupNodeStorage.get(groupName);
        }else{
            groupNode = new DnDGroupTreeNode(distGroup);
            groupNodeStorage.put(groupNode.getTreeNodeId(), groupNode);
            root.add(groupNode);
            
//            fireGroupAddedIntoTreeEvent(distGroup);
            
            isPersistentRequired = true;
        }
        if (!buddyNodeStorage.containsKey(groupName)){
            buddyNodeStorage.put(groupName, new HashMap<String, DnDBuddyTreeNode>());
        }
        if (isPersistentRequired && isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
        
        return groupNode;
    }
    
    /**
     * 
     * @param groupNode - (1)cannot be NULL; (2)its internal group instance cannot be NULL
     * @param buddy
     * @return 
     */
    private synchronized DnDBuddyTreeNode createDndBuddyTreeNodeForTreeModel(DnDGroupTreeNode groupNode, IGatewayConnectorBuddy buddy){
        IBuddyProfileRecord record = getKernel().retrieveBuddyProfileRecord(buddy);
        if (record != null){
            if (DataGlobal.isNonEmptyNullString(record.getNickName())){
                buddy.setNickname(record.getNickName());
            }
        }
        //save it into buddyNodeStorage in the memory...
        String groupName = groupNode.getGatewayConnectorGroup().getGroupName();
        DnDBuddyTreeNode buddyNode = new DnDBuddyTreeNode(groupNode, buddy);
        HashMap<String, DnDBuddyTreeNode> buddyNodeObj = buddyNodeStorage.get(groupName);
        if (buddyNodeObj == null){
            buddyNodeObj = new HashMap<String, DnDBuddyTreeNode>();
        }
        buddyNodeObj.put(buddy.getIMUniqueName(), buddyNode);
        buddyNodeStorage.put(groupName, buddyNodeObj);
        
//        fireBuddyAddedIntoGroupEvent(buddy, groupNode.getGatewayConnectorGroup());
        
        return buddyNode;
    }
    
    /**
     * Create a new node under groupNode
     * @param groupNode - (1)cannot be NULL; (2)its internal group instance cannot be NULL
     * @param buddy
     * @return 
     */
    private synchronized DnDBuddyTreeNode acquireDnDBuddyTreeNodeForTreeModel(DnDGroupTreeNode groupNode, IGatewayConnectorBuddy buddy) {
        boolean isPersistentRequired = false;
        //logger.log(Level.INFO, "groupNode -> {0}; buddy - > {1}", new Object[]{groupNode.getTreeNodeId(), buddy.getIMServerTypeString()});
        DnDBuddyTreeNode buddyNode;
        if (ownerBuddyListPanel instanceof IRegularBuddyListPanel){
            buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel(buddy);
        }else{
            buddyNode = retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
        }
        if (buddyNode == null){
            //logger.log(Level.INFO, "groupNode2 -> {0}; buddy2 - > {1}", new Object[]{groupNode.getTreeNodeId(), buddy.getIMUniqueName()});
            //try to find it from offlineBuddyNodeStorage first
            HashMap<String, DnDBuddyTreeNode> offlineMemberMap = offlineBuddyNodeStorage.get(groupNode);
            if (offlineMemberMap != null){
                buddyNode = offlineMemberMap.get(buddy.getIMUniqueName());
            }
            //make sure it is initiated
            if (buddyNode == null){
                buddyNode = createDndBuddyTreeNodeForTreeModel(groupNode, buddy);
                handleBuddyStatusForConferenceAttendant(buddy);
//////                fireDndBuddyTreeChangedEvent();
                isPersistentRequired = true;
            }
            if (isOfflineBuddiesRequired()){
                //groupNode.remove(buddyNode);
                groupNode.add(buddyNode);
                if (offlineMemberMap != null){
                    offlineMemberMap.remove(buddyNode.getGatewayConnectorBuddy().getIMUniqueName());
                }
            }else{
                bufferOfflineBuddyNodeForTreeModel(groupNode, buddyNode);
                try{
                    //groupNode.remove(buddyNode);
                    groupNode.add(buddyNode);
                }catch (Exception ex){
                    //java.lang.IllegalArgumentException
                }
            }
            /**
             * in case of un-checked "Show-Offline" being on, here the buddy node is refreshed.
             */
            refreshTreeModelForBuddy(buddy);
        }else{
            if (!isOfflineBuddiesRequired()){
                //make sure is not under the group node and buffered into storage
                bufferOfflineBuddyNodeForTreeModel(groupNode, buddyNode);
                try{
                    groupNode.remove(buddyNode);
                } catch (Exception ex){
                    //java.lang.IllegalArgumentException
                }
            }
        }
        if (isPersistentRequired && isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
        return buddyNode;
    }
    
    private synchronized void handleBuddyStatusForConferenceAttendant(IGatewayConnectorBuddy buddy){
        if (buddy == null){
            return;
        }
        if (getKernel().getPointBoxLoginUser() != null){
            if (getKernel().getPointBoxLoginUser().getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())){
                buddy.setBuddyStatus(BuddyStatus.Online);
            }
        }
    }
    
    @Override
    public synchronized boolean isDistGroupExisted(String distGroupName) {
        return (retrieveDnDGroupTreeNodeFromTreeModel(GatewayBuddyListFactory.getDistributionGroupInstance(getKernel().getPointBoxLoginUser(), distGroupName)) != null);
    }

    /**
     * whether or not there is no any buddy node 
     * @return 
     */
    @Override
    public synchronized boolean isEmpty() {
        return !(root.children().hasMoreElements());
    }
    
    @Override
    public synchronized void removeAllNodes() {
        this.groupNodeStorage.clear();
        this.buddyNodeStorage.clear();
        this.offlineBuddyNodeStorage.clear();
        root.removeAllChildren();
    }

    /**
     * Remove entire group node (including its member nodes) from this panel
     * @param group 
     */
    @Override
    public synchronized void removeGroupNode(IGatewayConnectorGroup group) {
        DnDGroupTreeNode groupNode = getDnDGroupTreeNode(group);
        if (groupNode != null){
            groupNodeStorage.remove(group.getGroupName());
            buddyNodeStorage.remove(group.getGroupName());
            root.remove(groupNode);
            
//            fireGroupRemovedFromTreeEvent(groupNode.getGatewayConnectorGroup());
            
            if (isRealTimePersistentRequired()){
                getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
            }
        }
    }

    @Override
    public void removeConferenceAttendantNode(final String subject, final String hosterName, final IGatewayConnectorBuddy buddy) {
        if ((buddy == null) 
                || (DataGlobal.isEmptyNullString(subject))
                || (DataGlobal.isEmptyNullString(hosterName)))
        {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            removeConferenceAttendantNodeHelper(subject, hosterName, buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    removeConferenceAttendantNodeHelper(subject, hosterName, buddy);
                }
            });
        }
    }
    private synchronized void removeConferenceAttendantNodeHelper(final String subject, final String hosterName, final IGatewayConnectorBuddy buddy) {
        Enumeration eg = root.children();
        Object groupNodeObj;
        DnDGroupTreeNode groupNode; 
        IDnDBuddyTreeNode buddyNode;
        while (eg.hasMoreElements()){
            groupNodeObj = eg.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                groupNode = (DnDGroupTreeNode)groupNodeObj;
                if (groupNode.getGatewayConnectorGroup().getIMUniqueName().equalsIgnoreCase(
                        GatewayBuddyListFactory.constructConferenceGroupIMUniqueName(subject, hosterName)))
                {
                    buddyNode = retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
                    if (buddyNode != null){
                        removeBuddyNodeFromGroupNodeFroTreeModel(buddyNode, groupNode);
                    }
                    break;
                }
            }
        }//while
        updateUI();
    }

    @Override
    public void removeBuddyFromGroup(final IGatewayConnectorBuddy buddy, final String groupName) {
        if ((buddy == null) || (groupName == null)){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            removeBuddyFromGroupHelper(buddy, groupName);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    removeBuddyFromGroupHelper(buddy, groupName);
                }
            });
        }
    }
    private synchronized void removeBuddyFromGroupHelper(final IGatewayConnectorBuddy buddy, final String groupName) {
        DnDGroupTreeNode groupNode = groupNodeStorage.get(groupName);
        if (groupNode != null){
            IDnDBuddyTreeNode buddyNode = retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
            if (buddyNode != null){
                removeBuddyNodeFromGroupNodeFroTreeModel(buddyNode, groupNode);
            }
        }
        updateUI();
    }

    /**
     * Remove all the buddy nodes associated with buddy
     * @param buddy 
     */
    @Override
    public void removeAssociatedBuddyNodes(final IGatewayConnectorBuddy buddy) {
        if (buddy == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            removeAssociatedBuddyNodesHelper(buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    removeAssociatedBuddyNodesHelper(buddy);
                }
            });
        }
    }
    private synchronized void removeAssociatedBuddyNodesHelper(final IGatewayConnectorBuddy buddy) {
        Enumeration eg = root.children();
        Object groupNodeObj;
        DnDGroupTreeNode groupNode; 
        IDnDBuddyTreeNode buddyNode;
        while (eg.hasMoreElements()){
            groupNodeObj = eg.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                groupNode = (DnDGroupTreeNode)groupNodeObj;
                buddyNode = retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
                if (buddyNode != null){
                    removeBuddyNodeFromGroupNodeFroTreeModel(buddyNode, groupNode);
                }
            }
        }//while
        updateUI();
    }

    private synchronized void removeBuddyNodeFromGroupNodeFroTreeModel(IDnDBuddyTreeNode buddyNode, DnDGroupTreeNode groupNode) {
        //find the buddy node
        if ((buddyNode != null) && (buddyNode instanceof DnDBuddyTreeNode)){
            try{
                groupNode.remove((DnDBuddyTreeNode)buddyNode);
                HashMap<String, DnDBuddyTreeNode> buddyNodeMap = buddyNodeStorage.get(groupNode.getGatewayConnectorGroup().getGroupName());
                if (buddyNodeMap != null){
                    buddyNodeMap.remove(buddyNode.getGatewayConnectorBuddy().getIMUniqueName());
                }
                
//                fireBuddyRemovedFromGroupEvent(buddyNode.getGatewayConnectorBuddy(), groupNode.getGatewayConnectorGroup());
                
                if (isRealTimePersistentRequired()){
                    getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
                }
            } catch (Exception ex){
                //java.lang.IllegalArgumentException
            }
        }
    }

    @Override
    public synchronized ArrayList<IGatewayConnectorGroup> retrieveAssociatedGroupsOfBuddy(IGatewayConnectorBuddy buddy) {
        ArrayList<IGatewayConnectorGroup> groups = new ArrayList<IGatewayConnectorGroup>();
        if (buddy != null){
            Enumeration eg = root.children();
            Object groupNodeObj;
            DnDGroupTreeNode groupNode; 
            IDnDBuddyTreeNode buddyNode;
            while (eg.hasMoreElements()){
                groupNodeObj = eg.nextElement();
                if (groupNodeObj instanceof DnDGroupTreeNode){
                    groupNode = (DnDGroupTreeNode)groupNodeObj;
                    buddyNode = this.retrieveDndBuddyTreeNodeForTreeModel(groupNode, buddy);
                    //find the buddy node
                    if ((buddyNode != null) && (buddyNode instanceof DnDBuddyTreeNode)){
                        groupNode.getGatewayConnectorGroup().setLoginUser(buddy.getLoginOwner());
                        groups.add(groupNode.getGatewayConnectorGroup());
                    }
                }
            }//while
        }//if
        return groups;
    }

    /**
     * @deprecated - plan to implement a true conference mechanism by means of PBIM smack API
     * @param subject
     * @param hosterName
     * @return 
     */
    @Override
    public synchronized IPointBoxConferenceGroup retrieveConferenceGroup(String subject, String hosterName) {
        IPointBoxConferenceGroup confGroup = null;
        Enumeration e = root.children();
        Object groupObj;
        
        while (e.hasMoreElements()){
            groupObj = e.nextElement();
            if (groupObj instanceof IDnDGroupTreeNode){
                if (((IDnDGroupTreeNode)groupObj).getGatewayConnectorGroup() instanceof IPointBoxConferenceGroup){
                    confGroup = (IPointBoxConferenceGroup)(((IDnDGroupTreeNode)groupObj).getGatewayConnectorGroup());
                    if ((confGroup.getHoster().getIMScreenName().equalsIgnoreCase(hosterName))
                            && (confGroup.getSubject().equalsIgnoreCase(subject)))
                    {
                        break;
                    }else{
                        confGroup = null;
                    }
                }
            }
        }//while
        
        return confGroup;
    }
    
    @Override
    public synchronized ArrayList<IGatewayConnectorGroup> retrieveAllGroups(boolean sortByUniqueName) {
        ArrayList<IGatewayConnectorGroup> groups = new ArrayList<IGatewayConnectorGroup>();
        Enumeration e = root.children();
        Object groupObj;
        while (e.hasMoreElements()){
            groupObj = e.nextElement();
            if (groupObj instanceof IDnDGroupTreeNode){
                groups.add(((IDnDGroupTreeNode)groupObj).getGatewayConnectorGroup());
            }
        }//while
        if (sortByUniqueName){
            return PbcGlobal.sortGroupsByUniqueNames(groups);
        }else{
            return groups;
        }
    }

    /**
     * Retrieve all the buddy instances of the buddy list (this includes offline buddies)
     * @param sort
     * @return 
     */
    @Override
    public synchronized ArrayList<IGatewayConnectorBuddy> retrieveAllBuddies(boolean sort) {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        HashMap<String, DnDBuddyTreeNode> buddyNodeMap;
        Collection<DnDBuddyTreeNode> buddyNodes;
        Enumeration e = root.children();
        Object groupObj;
        while (e.hasMoreElements()){
            groupObj = e.nextElement();
            if (groupObj instanceof IDnDGroupTreeNode){
                buddyNodeMap = buddyNodeStorage.get(((IDnDGroupTreeNode)groupObj).getGatewayConnectorGroup().getGroupName());
                if (buddyNodeMap != null){
                    buddyNodes = buddyNodeMap.values();
                    for (DnDBuddyTreeNode buddyNode : buddyNodes){
                        buddies.add(buddyNode.getGatewayConnectorBuddy());
                    }//for
                }
            }
        }//while
        if (sort){
            return PbcGlobal.sortBuddiesByUniqueNames(buddies);
        }else{
            return buddies;
        }
    }
    
    /**
     * Get all the buddies staying in the buddy-nodes of the groupNode. If "offline" 
     * buddies are hidden, they will not be included.
     * @param groupNode
     * @return 
     */
    @Override
    public synchronized ArrayList<IGatewayConnectorBuddy> retrieveBuddiesOfGroupNode(IDnDGroupTreeNode groupNode){
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        if (groupNode != null){
            Enumeration e = groupNode.children();
            Object buddyObj;
            while (e.hasMoreElements()){
                buddyObj = e.nextElement();
                if (buddyObj instanceof IDnDBuddyTreeNode){
                    buddies.add(((IDnDBuddyTreeNode)buddyObj).getGatewayConnectorBuddy());
                }
            }
        }//if
        return buddies;
    }

    @Override
    public synchronized ArrayList<IGatewayConnectorBuddy> retrieveBuddiesOfGroup(IGatewayConnectorGroup group) {
        return retrieveBuddiesOfGroupNode(retrieveDnDGroupTreeNodeFromTreeModel(group));
    }

    @Override
    public synchronized ArrayList<IGatewayConnectorBuddy> retrieveBuddiesOfGroups(ArrayList<IGatewayConnectorGroup> groups) {
        ArrayList<IGatewayConnectorBuddy> buddies = new ArrayList<IGatewayConnectorBuddy>();
        for (IGatewayConnectorGroup group : groups){
            buddies.addAll(retrieveBuddiesOfGroup(group));
        }//for
        return buddies;
    }

    /**
     * This should also consider offline buddy nodes.
     * @param distGroupNode
     * @return 
     */
    private synchronized BuddyListGroupItem constructPbcBuddyListGroupItem(IDnDGroupTreeNode distGroupNode) {
        if (distGroupNode == null){
            return null;
        }
        Enumeration eBuddies;
        Object buddyNodeObj;
        IGatewayConnectorBuddy buddy;
        ArrayList<BuddyListBuddyItem> buddyItems;
        
        PointBoxAccountID pointBoxAccountID = getKernel().getPointBoxAccountID();
        IGatewayConnectorGroup distGroup = distGroupNode.getGatewayConnectorGroup();
        
        BuddyListGroupItem groupItem = new BuddyListGroupItem();
        groupItem.setConnectorOwner(pointBoxAccountID);
        groupItem.setGroupDescription(distGroup.getGroupDescription());
        groupItem.setGroupName(distGroup.getGroupName());
        buddyItems = new ArrayList<BuddyListBuddyItem>();
        eBuddies = distGroupNode.children();
        int buddyIndex = 0;
        while (eBuddies.hasMoreElements()){
            buddyNodeObj = eBuddies.nextElement();
            if (buddyNodeObj instanceof IDnDBuddyTreeNode){
                buddy = ((IDnDBuddyTreeNode)buddyNodeObj).getGatewayConnectorBuddy();
                if ((buddy != null) && (buddy.getLoginOwner() != null)){
                    buddyItems.add(createBuddyItemInstance(distGroup, buddy, buddyIndex));
                    buddyIndex++;
                }
            }
        }//while

        HashMap<String, DnDBuddyTreeNode> offlineBuddyNodeMap = offlineBuddyNodeStorage.get((DnDGroupTreeNode)distGroupNode);
        Collection<DnDBuddyTreeNode> offlineBuddyNodes;
        if (offlineBuddyNodeMap != null){
            offlineBuddyNodes = offlineBuddyNodeMap.values();
            for (DnDBuddyTreeNode offlineBuddyNode : offlineBuddyNodes){
                buddy = offlineBuddyNode.getGatewayConnectorBuddy();
                if ((buddy != null) && (buddy.getLoginOwner() != null)){
                    buddyItems.add(createBuddyItemInstance(distGroup, buddy, buddyIndex));
                    buddyIndex++;
                }
            }
        }
        groupItem.setBuddyItems(buddyItems.toArray(new BuddyListBuddyItem[buddyItems.size()]));
        if ((distGroup instanceof IPointBoxConferenceGroup)){
            IPointBoxConferenceGroup confGroup = (IPointBoxConferenceGroup)distGroup;
            ConferenceItem confItem = new ConferenceItem();
            confItem.setConfUuid(confGroup.getConfUuid().toString());
            confItem.setDatetime(confGroup.getDateTime().getTimeInMillis());
            confItem.setHosterName(confGroup.getHoster().getIMScreenName());
            confItem.setDetails(confGroup.getConferenceDetails());
            confItem.setPrivacy(confGroup.getPrivacy().toString());
            confItem.setPersistency(confGroup.getPersistency().toString());
            confItem.setLifeStatus(confGroup.getLifeStatus().toString());
            groupItem.setConferenceItem(confItem);
        }

        return groupItem;
    }

    @Override
    public synchronized PbcBuddyListSettings constructPbcBuddyListSettings() {
        /**
         * Initialize data structure with default values
         */
        Enumeration eGroups = root.children();
        Object groupNodeObj;
        DnDGroupTreeNode distGroupNode;
        IGatewayConnectorGroup distGroup;
        PbcBuddyListSettings listSettings = new PbcBuddyListSettings();
        listSettings.setBuddyListName(PbcBuddyListName.DISTRIBUTION_LIST.toString());//default value
        kernel.getPointBoxAccountID().setPointBoxQuoteCodeValue(kernel.getDefaultPbcPricingModel().getSqCode());
        listSettings.setBuddyListType(PbcBuddyListType.BroadcastBuddyList.toString());//default value
        /**
         * Construct the visible part of the buddy list
         */
        ArrayList<BuddyListGroupItem> groupItems = new ArrayList<BuddyListGroupItem>();
        BuddyListGroupItem groupItem;
        int groupIndex = 0;
        while (eGroups.hasMoreElements()){
            groupNodeObj = eGroups.nextElement();
            if (groupNodeObj instanceof DnDGroupTreeNode){
                distGroupNode = ((DnDGroupTreeNode)groupNodeObj);
                distGroup = distGroupNode.getGatewayConnectorGroup();
                //default save groups for buddy list. BUT conference group may be not
                boolean isSavingOnServerRequired = true;
                //process conference case.
                if ((distGroup instanceof IPointBoxConferenceGroup)){//conf-group possibly deoes not require saving operation
                    isSavingOnServerRequired = ((IPointBoxConferenceGroup)distGroup).isSavingRequired();
                }
                if ((distGroup != null) && (isSavingOnServerRequired)){
                    groupItem = constructPbcBuddyListGroupItem(distGroupNode);
                    groupItem.setGroupIndex(groupIndex);
                    groupItems.add(groupItem);
                    groupIndex++;
                }//if
            }//if
        }//while
        listSettings.setGroupItems(groupItems.toArray(new BuddyListGroupItem[groupItems.size()]));
        return listSettings;
    }

    private BuddyListBuddyItem createBuddyItemInstance(IGatewayConnectorGroup group, IGatewayConnectorBuddy buddy, int buddyIndex) {
        BuddyListBuddyItem buddyItem = new BuddyListBuddyItem();
        buddyItem.setBuddyDescription(buddy.getNotes());
        buddyItem.setBuddyIndex(buddyIndex);
        buddyItem.setBuddyLegality("");
        buddyItem.setBuddyName(buddy.getIMScreenName());
        buddyItem.setBuddyNickName(buddy.getNickname());
        buddyItem.setLoginOwnerName(buddy.getLoginOwner().getIMScreenName());
        buddyItem.setServerType(buddy.getLoginOwner().getIMServerType().toString());
        if (group instanceof IPointBoxConferenceGroup){
            buddyItem.setStatusInGroup(buddy.getConferenceAttitude((IPointBoxConferenceGroup)group).toString());
        }
        return buddyItem;
    }

    synchronized void notifyBuddyListSettingsChangedAfterDndHappened() {
        if (isRealTimePersistentRequired()){
            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(ownerBuddyListPanel.constructPbcBuddyListSettings(), true);
        }
    }
}

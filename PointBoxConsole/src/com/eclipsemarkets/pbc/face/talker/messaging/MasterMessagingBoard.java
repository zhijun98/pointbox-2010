/**
 * Eclipse Market Solutions LLC
 *
 * BuddyMessagingTab.java
 *
 * @author Zhijun Zhang
 * Created on May 19, 2010, 10:25:44 PM
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.event.PointBoxEventTarget;
import com.eclipsemarkets.event.face.TalkerPublishedQuoteEvent;
import com.eclipsemarkets.gateway.data.IBroadcastedMessage;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.GatewayBuddyListFactory;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.*;
import com.eclipsemarkets.global.exceptions.OutOfEdtException;
import com.eclipsemarkets.global.util.EaioUUID;
import com.eclipsemarkets.global.util.JavaTextPaneWithBackgroundImage;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pbc.face.talker.*;
import com.eclipsemarkets.pbc.face.talker.dist.PbcFloatingFrameTerms;
import com.eclipsemarkets.pbc.face.terms.OfficePopupMenuTerms;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.pbc.runtime.PbcImageFileName;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.pbc.runtime.settings.record.IBuddyProfileRecord;
import com.eclipsemarkets.pbc.runtime.settings.record.IMessageTabRecord;
import com.eclipsemarkets.release.PointBoxExecutorConfiguration;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.*;

/**
 *
 * @author Zhijun Zhang
 */
public class MasterMessagingBoard extends JPanel implements IMasterMessagingBoard{
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    final static String POINT_BOX;
    static{
        logger = Logger.getLogger(MasterMessagingBoard.class.getName());
        POINT_BOX = "";
    }

    protected final IPbcTalker talker;
    
    private final LookupMessageTabDialog lookupDialog;
    
    private final ExecutorService messagingService;
    
    /**
     * General format of documents used by this board
     */
    private Font messageFont;
    private Color messageColor;
    
    /**
     * Keep the largest tabButton size for layout
     */
    private int largestTabButtonWidth;
    
    /**
     * all the rows in jButtonPanel, which is a data structure only used in EDT
     */
    private final LinkedList<JPanel> tabButtonRowPanels;
    
    /**
     * This is the value of MessagingBoardState.getTabButtonUniqueID(). It represents 
     * which state currently is populated onto this board. This data member is synchronized 
     * by this board instance.
     */
    protected String targetStateUniqueID;
       
    /**
     * This holds unique distribution group name for floating board. If it is NULL, it is the base master.
     */
    private String boardId; 
    
    private JButton oneClickUp;
    private JButton oneClickDown;
    private JButton oneClickRight;
    private JButton oneClickLeft;
    
    private JTextPane jBroadcastMessagingEntry;
    protected JScrollPane jBroadCastScrollPane;
    
    
    private JScrollPane jScrollPane2;
    private JTextPane jMessagingEntry;
    
    MasterMessagingBoard(final IPbcTalker talker) {
        initComponents();
        
        this.talker = talker;
        
        /**
         * The following zone was copy-pasted from initComponents();
         * PURPOSE: for customizing jMessagingEntry
         */
        jScrollPane2 = new javax.swing.JScrollPane();
        jMessagingEntry = createMessagingEntryControl();
        jScrollPane2.setName("jScrollPane2"); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(252, 50));

        jMessagingEntry.setMinimumSize(new java.awt.Dimension(150, 50));
        jMessagingEntry.setName("jMessagingEntry"); // NOI18N
        jMessagingEntry.setPreferredSize(new java.awt.Dimension(250, 20));
        jMessagingEntry.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                jMessagingEntryFocusGained(evt);
            }
        });
        jMessagingEntry.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jMessagingEntryKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(jMessagingEntry);

        jMessagingSplitPane.setRightComponent(jScrollPane2);
        
        /**
         * The followings are the implementation of constructor
         */
        initializeBroadcastMessageEntry(talker);
        
        boardId = (new EaioUUID()).toString();
        
        /**
         * Button zone should have sufficient space for tab buttons on the horizontal direction
         */
        jScrollButtonPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                
        targetStateUniqueID = null;
        
        tabButtonRowPanels = new LinkedList<JPanel>();
        
        messagingService = Executors.newFixedThreadPool(PointBoxExecutorConfiguration.MasterMessagingBoard_Service_Control);
        
        jMessagingSplitPane.setDividerLocation(0.7); //no meaning since it must have size after frame is packed.
        jMessagingSplitPane.setResizeWeight(1);
        
        jParentSplitPane.setDividerSize(0);  //don't load this section fot master board
        jBroadCastScrollPane.setVisible(false);
        jSuperParentSplitPane.setDividerSize(0);
        jMembesrPanel.setVisible(false);
        BasicSplitPaneUI ui1 = (BasicSplitPaneUI) jParentSplitPane.getUI();
        oneClickUp= (JButton) ui1.getDivider().getComponent(0);
        oneClickDown = (JButton) ui1.getDivider().getComponent(1);
        BasicSplitPaneUI ui2 = (BasicSplitPaneUI) jSuperParentSplitPane.getUI();
        oneClickRight = (JButton) ui2.getDivider().getComponent(1);
        oneClickLeft = (JButton) ui2.getDivider().getComponent(0);
    
        jParentSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                    if(jParentSplitPane.getDividerLocation()>0){
                        oneClickUp.setEnabled(false);
                        oneClickDown.setEnabled(true);
                    }
            }
        });
        
        jSuperParentSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                    if(jSuperParentSplitPane.getDividerLocation()>0){
                        oneClickLeft.setEnabled(false);
                        oneClickRight.setEnabled(true);
                    }
            }
        });
        
        oneClickUp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oneClickUp.setEnabled(false);
                oneClickDown.setEnabled(true);
            }
            
        });
        
        oneClickDown.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oneClickDown.setEnabled(false);
                oneClickUp.setEnabled(true);
            }
            
        });
        oneClickRight.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oneClickRight.setEnabled(false);
                oneClickLeft.setEnabled(true);
            }
            
        });
        oneClickLeft.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oneClickLeft.setEnabled(false);
                oneClickRight.setEnabled(true);
            }
            
        });        
        
        messageFont = SwingGlobal.getLabelFont();
        messageColor = Color.BLACK;

        IPbconsoleImageSettings imageSettings = talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
        jLookup.setText(null);
        jLookup.setIcon(imageSettings.getLookUpTabIcon());
        jLookup.setActionCommand(MessagingToolBarCommand.Lookup.toString());
        jLookup.setToolTipText(MessagingToolBarCommand.Lookup.toString());

        jSortingAZ.setText(null);
        jSortingAZ.setIcon(imageSettings.getSortingAZIcon());
        jSortingAZ.setActionCommand(MessagingToolBarCommand.SortingAZ.toString());
        jSortingAZ.setToolTipText(MessagingToolBarCommand.SortingAZ.toString());

        jSortingZA.setText(null);
        jSortingZA.setIcon(imageSettings.getSortingZAIcon());
        jSortingZA.setActionCommand(MessagingToolBarCommand.SortingZA.toString());
        jSortingZA.setToolTipText(MessagingToolBarCommand.SortingZA.toString());

        jCloseAll.setText(null);
        jCloseAll.setIcon(imageSettings.getCloseAllTabsIcon());
        jCloseAll.setActionCommand(MessagingToolBarCommand.ClosAllTabs.toString());
        jCloseAll.setToolTipText(MessagingToolBarCommand.ClosAllTabs.toString());
        
        jPersistFrameBtn.setText(null);
        jPersistFrameBtn.setIcon(imageSettings.getPersistIcon());
        jPersistFrameBtn.setActionCommand(MessagingToolBarCommand.PersistFrame.toString());
        jPersistFrameBtn.setToolTipText(MessagingToolBarCommand.PersistFrame.toString());
        
        lookupDialog = new LookupMessageTabDialog(talker, false, this);
        
        this.addFocusListener(new FocusListener(){
            @Override
            public void focusLost(java.awt.event.FocusEvent e){}

            @Override
            public void focusGained(java.awt.event.FocusEvent e){
                jMessagingEntry.setRequestFocusEnabled(true);
                jMessagingEntry.requestFocusInWindow();
                jMessagingEntry.setCaretPosition(0);
                jMessagingEntry.select(0, 0);
                jMessagingEntry.moveCaretPosition(0);
            }
        });
        jMessagingHistory.setContentType("text/plain");
        jMessagingHistory.setFont(messageFont);
        jMessagingHistory.setForeground(messageColor);
        jMessagingHistory.setEditable(false);
        jMessagingHistory.setDocument(new DefaultStyledDocument());
        jMessagingHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                showMessageTabPopupMenu(e, true, false, true);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                try{
                        Element elem = jMessagingHistory.getStyledDocument().getCharacterElement(jMessagingHistory.viewToModel(e.getPoint()));
                        AttributeSet as = elem.getAttributes();
                        Object obj = as.getAttribute(MessagingBoardState.LINK_ATTRIBUTE);
                        if (obj instanceof DataMembersGuardedByState.URLLinkAction){
                            DataMembersGuardedByState.URLLinkAction fla = (DataMembersGuardedByState.URLLinkAction)obj;
                            if(fla != null){
                                fla.execute();
                            }
                        }
                }
                catch(Exception x) {
                    //x.printStackTrace();
                }               
            }         
        });
        
        jMessagingHistory.addMouseMotionListener(new MouseInputAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                Element elem = jMessagingHistory.getStyledDocument().getCharacterElement(jMessagingHistory.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                if(StyleConstants.isUnderline(as))
                        jMessagingHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
                else
                        jMessagingHistory.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });        
        
        jPersistFrameBtn.setVisible(false);
        if(this instanceof FloatingMessagingBoard 
                && (!(this instanceof FloatingDistGroupMessagingBoard)))
        {
            jPersistFrameBtn.setVisible(true);
        }

        jMessagingEntry.setEditable(true);
        jMessagingEntry.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                showMessageTabPopupMenu(e, false, false, false);
            }
        });
       
        jButtonPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component comp = e.getComponent();
                if (comp == null){
                    return;
                }
                if (((comp.getBounds().width - comp.getPreferredSize().width) > getLargestTabButtonWidth())
                        || (comp.getBounds().width > jScrollButtonPane.getVisibleRect().width))
                {
                    ArrayList<IButtonTabComponent> tabButtons = getAllVisibleTabeButtons();
                    layoutMessagingBoardTabButtons(tabButtons);
                }
            }
        });
        jButtonPanel.setMinimumSize(new Dimension(0, 40));
        largestTabButtonWidth = 0;

        DefaultCaret caret =(DefaultCaret) jMessagingHistory.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);        
        setMessagingBoardDropTargetListener(new MessagingBoardDropTargetListener(this));
        
        jGroupMemberList.setSelectionMode(
        ListSelectionModel.SINGLE_SELECTION);
        jGroupMemberList.setCellRenderer(
                FloatingFrameCheckListItemRenderer.createNewInstance(talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings()));
        jGroupMemberList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent event)
            {
                 mouseClickedInEDT(event);
            }
        });   
        jCheckAll.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                 checkAllActionInEDT();
            }
        });
    }
    
    void hideCheclAllBox(){
        if (SwingUtilities.isEventDispatchThread()){
            jCheckAll.setVisible(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jCheckAll.setVisible(false);
                }
            });
        }
    }
    
    private void jMessagingEntryKeyReleased(java.awt.event.KeyEvent evt) {
       if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if((evt.getModifiers() & KeyEvent.SHIFT_MASK) != 0){
                try {
                    jMessagingEntry.getDocument().insertString(jMessagingEntry.getDocument().getLength(), "\n", null);
                } catch (BadLocationException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }else{
                sendCurrentMessage();
            }
        } 
    }
    
    private void jMessagingEntryFocusGained(java.awt.event.FocusEvent evt) {
        /**
         * This method is dirty! It makes "Copy-To"/"Send-To" does not work
         */
        //jBroadcastMessagingEntry.setText("");
    }
    private void showMessageTabPopupMenu(MouseEvent e, boolean disableCut, boolean disableCopy, boolean disablePaste) {
        if ((e != null) && (e.isPopupTrigger())) {
            MessageTabPopupMenu ccpPopup = new MessageTabPopupMenu(MasterMessagingBoard.this.talker.getKernel(), 
                                                                   targetStateUniqueID,getBoardId());
            if (disableCut){
                ccpPopup.disableMenuItem(OfficePopupMenuTerms.CUT);
            }else{
                ccpPopup.enableMenuItem(OfficePopupMenuTerms.CUT);
            }
            if (disableCopy){
                ccpPopup.enableMenuItem(OfficePopupMenuTerms.COPY);
            }else{
                ccpPopup.enableMenuItem(OfficePopupMenuTerms.COPY);
            }
            if (disablePaste){
                ccpPopup.disableMenuItem(OfficePopupMenuTerms.PASTE);
            }else{
                ccpPopup.enableMenuItem(OfficePopupMenuTerms.PASTE);
            }
            ccpPopup.show(e.getComponent(), e.getX(), e.getY());
            if(ccpPopup.getSetTitleMenuItem()!=null){
                ccpPopup.getSetTitleMenuItem().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        saveNewGroupWithPersistingFrameAction();
                    }
                }); 
            }
            JFrame floatingGroupFrame = getFrameOwner();
            if(MasterMessagingBoard.this instanceof FloatingMessagingBoard 
                    && (!(MasterMessagingBoard.this instanceof FloatingDistGroupMessagingBoard)))
            {
                if ((floatingGroupFrame != null) 
                        && (PbcReservedTerms.DefaultPitsLikeFrameTitle.toString().equalsIgnoreCase(floatingGroupFrame.getTitle())))
                {
                    ccpPopup.getSetTitleMenuItem().setVisible(true);
                }else{
                    ccpPopup.getSetTitleMenuItem().setVisible(false);
                }
            }
        }//if (e.isPopupTrigger()) {
    }

    void makePersistFrameButtonInvisible() {
        if (SwingUtilities.isEventDispatchThread()){
            jPersistFrameBtn.setVisible(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jPersistFrameBtn.setVisible(false);
                }
            });
        }
    }

    void modifyBoardMenuBarForPitsCast() {
        if(SwingUtilities.isEventDispatchThread()){
            modifyBoardMenuBarForPitsCastHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   modifyBoardMenuBarForPitsCastHelper();
                }
            });
        }
    }
    
    private void modifyBoardMenuBarForPitsCastHelper(){
        jCloseAll.setVisible(false);
        jPersistFrameBtn.setVisible(false);
    }
    
    private void mouseClickedInEDT(final MouseEvent event){
        if(SwingUtilities.isEventDispatchThread()){
            mouseClickedHelper(event);
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   mouseClickedHelper(event);
                }
            });
        }
    }
    
    private void mouseClickedHelper(MouseEvent event){
        JList list = (JList) event.getSource();
        // Get index of item clicked
        int index = list.locationToIndex(event.getPoint());
        if (index < 0){
            return;
        }
        if(list.getModel().getElementAt(index) instanceof  FloatingFrameCheckBuddyItem){
            FloatingFrameCheckBuddyItem item = (FloatingFrameCheckBuddyItem)list.getModel().getElementAt(index);
            // Toggle selected state
            item.setSelected(!item.isSelected());
            if (item.isSelected()){
                talker.selectBuddyCheckNode(item.getBuddy());
            }else{
                talker.unselectBuddyCheckNode(item.getBuddy());
            }
            
            // Repaint cell
            list.repaint(list.getCellBounds(index, index));
        }        
    }
    
    private void checkAllActionInEDT(){
        if(SwingUtilities.isEventDispatchThread()){
           checkAllActionHelper(jCheckAll.isSelected());
       }else{
           SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    checkAllActionHelper(jCheckAll.isSelected());
                }
            });
       }
    }
    
    /**
     * This method is to synchronize check-node state with jCheckAll.isSelected() state
     */
    private void checkAllActionHelper(final boolean checkAll){
        new SwingWorker<Void, FloatingFrameCheckBuddyItem>(){
            @Override
            protected Void doInBackground() throws Exception {
                for(int i=0;i<jGroupMemberList.getModel().getSize();i++){
                    if( jGroupMemberList.getModel().getElementAt(i) instanceof FloatingFrameCheckBuddyItem){
                        FloatingFrameCheckBuddyItem item=(FloatingFrameCheckBuddyItem) jGroupMemberList.getModel().getElementAt(i);
                        publish(item);
                    }
                }             
                return null;
            }
            @Override
            protected void process(List<FloatingFrameCheckBuddyItem> chunks) {
                   for(FloatingFrameCheckBuddyItem item:chunks){
                       if(checkAll){
                           item.setSelected(true);
                       }else{
                           item.setSelected(false);
                       }
                   }
            }

            @Override
            protected void done() {
                jGroupMemberList.repaint();
            }
        }.execute();       
    }
    
    /**
     * A JFrame which owns this board
     * @return 
     */
    private FloatingMessagingFrame getFrameOwner(){
        try{
            return MessagingPaneManager.getSingleton().retrieveFloatingMessagingBoards(getBoardId());
        }catch (Exception ex){
            return null;
        }
    
    }
    
    private void saveNewGroupWithPersistingFrameAction(){
        if(SwingUtilities.isEventDispatchThread()){
            saveNewGroupWithPersistingFrameActionHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    saveNewGroupWithPersistingFrameActionHelper();
                }
            });
        }
    }
    
    private void saveNewGroupWithPersistingFrameActionHelper(){
        FloatingMessagingFrame floatingFrame = getFrameOwner();
        if(floatingFrame != null){
            if (PbcReservedTerms.DefaultPitsLikeFrameTitle.toString().equalsIgnoreCase(floatingFrame.getTitle())){
                //setPitsLikeGroupTitleHelper();
            }else{
                
            }
//            String[] options = {PbcBuddyListPanelTabName.PITS.toString(), 
//                                PbcBuddyListPanelTabName.PITS_CAST.toString(), 
//                                PbcBuddyListPanelTabName.DISTRIBUTION.toString(),
//                                "Cancel"};
//            int groupType = JOptionPane.showOptionDialog(MasterMessagingBoard.this, 
//                                         "Please select which types of group will be saved for this persistent frame:", 
//                                         "Select group type:", 
//                                         JOptionPane.DEFAULT_OPTION, 
//                                         JOptionPane.INFORMATION_MESSAGE, 
//                                         null, 
//                                         options, 
//                                         null);
//            if ((groupType < 0) || (groupType > 3)){
//                return;
//            }else{
//                if (groupType == 3){
//                    JOptionPane.showMessageDialog(this, "This operation is canceled.");
//                }else{
                    String aNewDistGroupName = JOptionPane.showInputDialog(MasterMessagingBoard.this,
                                                                    "Please assign a new PBcast group name for the persistent frame:"
                                                                    + NIOGlobal.lineSeparator() 
                                                                    + "(Only alphabetic characters and digit numbers are legal for group name!)",
                                                                    "Save A New PBcast Group:",
                                                                    JOptionPane.INFORMATION_MESSAGE);
                    if(DataGlobal.isEmptyNullString(aNewDistGroupName))
                    {
                        JOptionPane.showMessageDialog(this, "Saving a new PBcast group for a persistent frame is canceled.");
                    }else{
                        if (PbcGlobal.isLegalInput(aNewDistGroupName)){
                            if(talker.checkGroupNameRedundancy(aNewDistGroupName)){
                                JOptionPane.showMessageDialog(this, "This group name has been used. Please give another unique group name in PBC!");
                            }else{
//                                switch (groupType){
//                                    case 0: //PITS
//                                        saveNewGroupWithPersistingFrameActionHelper_PITS(aNewDistGroupName, floatingFrame);
//                                        break;
//                                    case 1: //PITS-CAST
                                        saveNewGroupWithPersistingFrameActionHelper_PITS_CAST(aNewDistGroupName, floatingFrame);
//                                        break;
//                                    case 2: //DISTRIBUTION
//                                        saveNewGroupWithPersistingFrameActionHelper_DISTRIBUTION(aNewDistGroupName, floatingFrame);
//                                        break;
//                                }
                            }
                        }else{
                            JOptionPane.showMessageDialog(this, "Only alphabetic characters and digit numbers are legal for a group name!");
                        }
                    }
                
//                }
//            }
        }
    }
    
    private void saveNewGroupWithPersistingFrameActionHelper_PITS_CAST(final String aNewPitsCastGroupName, final FloatingMessagingFrame floatingFrame) {
        IGatewayConnectorGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(), aNewPitsCastGroupName);
        group.setLoginUser(talker.getPointBoxLoginUser());//a new group should be set a login User
        
        //(1)prepare members of the group
        ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
        
        prepareMembersOfDistGroup(members, floatingFrame, null);
        
        //(2)close floatingFrame (this should be processed before 3)
        closeDefaultFloatingFrameForNewSavedGroup(floatingFrame);
        
        //(3)populate group-members into distribution panel
        IDistributionBuddyListPanel buddyListPanel = talker.getPitsCastBuddyListTreePanel();
        if (buddyListPanel != null){
            buddyListPanel.addNewDistributionGroup(group, members);
            buddyListPanel.highlightGatewayConnectorGroup(group);
            buddyListPanel.expandListPanel();
            buddyListPanel.displayDistribributionMessageBoard("", group);
        }
    }
    
    private void saveNewGroupWithPersistingFrameActionHelper_DISTRIBUTION(final String aNewDistGroupName, final FloatingMessagingFrame floatingFrame) {

        IGatewayConnectorGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(), aNewDistGroupName);
        group.setLoginUser(talker.getPointBoxLoginUser());//a new group should be set a login User
        //prepare members of the group
        ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
        
        prepareMembersOfDistGroup(members, floatingFrame, null);
        
        //populate group-members into distribution panel
        IDistributionBuddyListPanel buddyListPanel = talker.getMasterBuddyListTreePanel();
        if (buddyListPanel != null){
            buddyListPanel.addNewDistributionGroup(group, members);
            buddyListPanel.highlightGatewayConnectorGroup(group);
            buddyListPanel.expandListPanel();
            buddyListPanel.displayDistribributionMessageBoard("", group);
        }
        //close floatingFrame
        closeDefaultFloatingFrameForNewSavedGroup(floatingFrame);
    }
    
    /**
     * A temp group floating frame can be displayed for users to assign a new group name. 
     * After this operation, this temp group floating frame should be closed.
     */
    private void closeDefaultFloatingFrameForNewSavedGroup(FloatingMessagingFrame floatingFrame){
        FloatingMessagingBoard board = floatingFrame.getFloatingMessagingBoard();
        if (board != null){
            ArrayList<IButtonTabComponent> tabs = board.getAllVisibleTabeButtons();
            if (tabs != null){
                IMessagingPaneManager aMessagingPaneManager = getTalker().getMessagingPaneManager();
                for(IButtonTabComponent tab : tabs){
                    if(tab instanceof BuddyButtonTabComponent){
                        aMessagingPaneManager.announceTabClosingEvent(tab.getTabUniqueID());
                    }
                }
            }
        }
        floatingFrame.setVisible(false);
    }
    
    /**
     * @deprecated - PITS tab panel is not created in PBC anymore.
     * @see PointBoxTalker::transferPitsGroupsIntoPitsCastBuddyListPanelHelper
     * @param aNewPitsGroupName
     * @param floatingFrame 
     */
    private void saveNewGroupWithPersistingFrameActionHelper_PITS(final String aNewPitsGroupName, final FloatingMessagingFrame floatingFrame) {
        String oldPitsGroupName = floatingFrame.getTitle();
        floatingFrame.setTitle(aNewPitsGroupName);
        if (!(PbcReservedTerms.DefaultPitsLikeFrameTitle.toString().equalsIgnoreCase(aNewPitsGroupName))){
            this.jPersistFrameBtn.setVisible(false);
        }
        //create or get a panel for PITS
        IPitsGroupListPanel floatingListPanel = talker.acquirePitsLikeGroupListTabPanelInEDT(PbcFloatingFrameTerms.PITSFrame.toString());
        if (floatingListPanel == null){
            JOptionPane.showMessageDialog(this, "Tech error: this operation failed.");
            return;
        }
        //Rename the group if necessary
        boolean isNewPitsGroup = true;
        for(IGatewayConnectorGroup group : floatingListPanel.getAllGroups(false, false)){
            if(group.getGroupName().equalsIgnoreCase(oldPitsGroupName)){ //floatingFrame.getTitle()==old title
                group.setGroupName(aNewPitsGroupName);
                floatingListPanel.refreshPanel();
                //updateBuddyListSettings();
                isNewPitsGroup = false;
                break;
            }
        }
        if (isNewPitsGroup){
            IGatewayConnectorGroup group = GatewayBuddyListFactory.getDistributionGroupInstance(talker.getKernel().getPointBoxLoginUser(),aNewPitsGroupName);
            ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
            PointBoxConsoleProperties prop = PointBoxConsoleProperties.getSingleton();
            FloatingMessagingBoard board = floatingFrame.getFloatingMessagingBoard();
            
            prepareMembersOfDistGroup(members, floatingFrame, prop);
            
            floatingListPanel.addNewDistributionGroup(group, members);
            group.setGroupDescription(board.getBoardId()); //use Description field to save unqueTabID for this frame.

            getKernel().getPointBoxConsoleRuntime().updatePbcBuddyListSettings(floatingListPanel.constructPbcBuddyListSettings(), true);

            //floatingFrame.toFront();
            
            //close floatingFrame
            closeDefaultFloatingFrameForNewSavedGroup(floatingFrame);
        }
    }

    private void prepareMembersOfDistGroup(ArrayList<IGatewayConnectorBuddy> members, 
                                           FloatingMessagingFrame floatingFrame, 
                                           PointBoxConsoleProperties prop) {
        FloatingMessagingBoard board = floatingFrame.getFloatingMessagingBoard();
        ArrayList<IButtonTabComponent> tabs;
        BuddyButtonTabComponent buddyTab;
        if (board instanceof FloatingPitsMessagingBoard){
            tabs = board.getAllVisibleTabeButtons();
            if (prop != null){
                //clean up....
                prop.removeProperties("FloatingBuddyMessagingTabsOfFrame_"+board.getTalker().getPointBoxLoginUser().getIMUniqueName()+board.getBoardId()+floatingFrame.getTitle());
            }
            for(IButtonTabComponent tab : tabs){
                if(tab instanceof BuddyButtonTabComponent){
                    buddyTab =(BuddyButtonTabComponent)tab;
                    members.add(buddyTab.getBuddy());
                }
            }
        }
    }
    
    final void setMessagingBoardDropTargetListener(MessagingBoardDropTargetListener aMessagingBoardDropTargetListener){
        jMessagingHistory.setDropTarget(new DropTarget(null,
                aMessagingBoardDropTargetListener));
        setDropTarget(new DropTarget(null,
                aMessagingBoardDropTargetListener));
    }

    @Override
    public IPbcTalker getTalker() {
        return talker;
    }
    
    @Override
    public String getBoardId() {
        return boardId;
    }

    /**
     * This is for Dnd
     * @param cursor 
     */
    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        jMessagingHistory.setCursor(cursor);
    }

    private ArrayList<IButtonTabComponent> sortVisibleTabButtons(boolean fromAtoZ) {
        ArrayList<IButtonTabComponent> result = getAllVisibleTabeButtons();
        Collections.sort(result, new ButtonTabComponentComparator(fromAtoZ));
        return result;
    }
    
    private ButtonTabComponent getVisibleTabButton(IButtonTabComponent hidingTabButton) {
        return getVisibleTabButton(hidingTabButton.getTabUniqueID());
    }

    @Override
    public boolean hasVisibleTabButton(IButtonTabComponent hidingTabButton) {
        return hasVisibleTabButton(hidingTabButton.getTabUniqueID());
    }

    @Override
    public boolean hasVisibleTabButton(String tabUniqueID) {
        return getVisibleTabButton(tabUniqueID) != null;
    }
    
    @Override
    public boolean hasVisibleTabForBuddy(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy) {
        return (getVisibleTabButton(TargetBuddyPair.generateTargetBuddyPairStorageID(loginUser, buddy)) != null);
    }

    @Override
    public boolean hasVisibleTabForGroup(IGatewayConnectorBuddy loginUser, IGatewayConnectorGroup group) {
        return (getVisibleTabButton(group.getIMUniqueName()) != null);
    }

    
    /**
     * Find a displayed ButtonTabComponent from this board
     * @param tabUniqueID
     * @return 
     */
    private ButtonTabComponent getVisibleTabButton(String tabUniqueID) {
        synchronized(tabButtonRowPanels){
            ButtonTabComponent tabButton;
            Component[] components;
            for (int j = 0; j < tabButtonRowPanels.size(); j++){
                components = tabButtonRowPanels.get(j).getComponents();
                if (components != null){
                    for (int k = 0; k < components.length; k++){
                        if (components[k] instanceof ButtonTabComponent){
                            tabButton = (ButtonTabComponent)(components[k]);
                            tabButton.setRowIndex(j);
                            if (tabButton.getTabUniqueID().equalsIgnoreCase(tabUniqueID)){
                                return tabButton;
                            }
                        }
                    }
                }//if
            }//for
        }//synchronized(tabButtonRowPanels){
        return null;
    }

    @Override
    public boolean hasVisibleTabButtons() {
        return !getAllVisibleTabeButtons().isEmpty();
    }

    List<String> getAllVisibleGroupNameList() {
        ArrayList<String> groupNameList = new ArrayList<String>();
        Component[] components;
        ButtonTabComponent tabButton;
        synchronized(tabButtonRowPanels){
            for (int j = 0; j < tabButtonRowPanels.size(); j++){
                components = tabButtonRowPanels.get(j).getComponents();
                if (components != null){
                    for (int k = 0; k < components.length; k++){
                        if (components[k] instanceof ButtonTabComponent){
                            tabButton = (ButtonTabComponent)(components[k]);
                            tabButton.setRowIndex(j);
                            groupNameList.add(tabButton.getDistGroupName());
                        }
                    }
                }//if
            }//for
        }//synchronized(tabButtonRowPanels){
        return groupNameList;
    }
    
    @Override
    public ArrayList<IButtonTabComponent> getAllVisibleTabeButtons(){
        ArrayList<IButtonTabComponent> tabButtonComponents = new ArrayList<IButtonTabComponent>();
        Component[] components;
        ButtonTabComponent tabButton;
        synchronized(tabButtonRowPanels){
            for (int j = 0; j < tabButtonRowPanels.size(); j++){
                components = tabButtonRowPanels.get(j).getComponents();
                if (components != null){
                    for (int k = 0; k < components.length; k++){
                        if (components[k] instanceof ButtonTabComponent){
                            tabButton = (ButtonTabComponent)(components[k]);
                            tabButton.setRowIndex(j);
                            tabButtonComponents.add(tabButton);
                        }
                    }
                }//if
            }//for
        }//synchronized(tabButtonRowPanels){
        return tabButtonComponents;
    }
    
    ArrayList<IGatewayConnectorBuddy> getAllBuddiesOnBoard() {
        ArrayList<IGatewayConnectorBuddy> memberItems = new ArrayList<IGatewayConnectorBuddy>();
        Component[] components;
        ButtonTabComponent tabButton;
        synchronized(tabButtonRowPanels){
            IGatewayConnectorBuddy buddy;
            for (int j = 0; j < tabButtonRowPanels.size(); j++){
                components = tabButtonRowPanels.get(j).getComponents();
                if (components != null){
                    for (int k = 0; k < components.length; k++){
                        if (components[k] instanceof ButtonTabComponent){
                            tabButton = (ButtonTabComponent)(components[k]);
                            tabButton.setRowIndex(j);
                            if (tabButton instanceof BuddyButtonTabComponent){
                                memberItems.add(((BuddyButtonTabComponent)tabButton).getBuddy());
                            }
                        }
                    }
                }//if
            }//for
        }//synchronized(tabButtonRowPanels){
        return memberItems;
    }

    /**
     * Get any visible tab button. If no one there, EMPTY tab button returned
     * @param hidingTabButton - avoid this button returned
     * @return 
     */
    private ButtonTabComponent getAnyVisibleTabButton(IButtonTabComponent hidingTabButton) {
        Component[] components;
        synchronized(tabButtonRowPanels){
            for (JPanel panel : tabButtonRowPanels){
                components = panel.getComponents();
                if (components != null){
                    ButtonTabComponent tabButton;
                    for (int k = 0; k < components.length; k++){
                        tabButton = (ButtonTabComponent)(components[k]);
                        if (components[k] instanceof ButtonTabComponent){
                            if (tabButton != hidingTabButton) {
                                return tabButton;
                            }
                        }
                    }
                }//if
            }//for
        }//synchronized(tabButtonRowPanels){
//////        return EMPTY_STATE.getButtonTabComponent();
        return null;
    }
    
    /**
     * If the state exists, it will be returned; if not, a new one created will be returned
     * @param loginUser
     * @param buddy
     * @return
     * @throws OutOfEdtException 
     */
    private IMessagingBoardState aquireStateForBuddy(final IGatewayConnectorBuddy loginUser, 
                                                     final IGatewayConnectorBuddy buddy) 
            throws OutOfEdtException
    {
        if (loginUser == null){
            throw new RuntimeException("Bad parameter - loginUser for aquireStateForBuddy");
        }
        if (buddy == null) {
            throw new RuntimeException("Bad parameter - buddy for aquireStateForBuddy");
        }
        IMessagingBoardState stateForBuddy = MessagingPaneManager.getSingleton(talker)
                                                .aquireStateForBuddy(this, loginUser, buddy);
//        if (stateForBuddy == null){
//            throw new RuntimeException("Cannot get an IMessagingBoardState instance");
//        }else{
          if(stateForBuddy!=null){
            IButtonTabComponent tabButton = MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, stateForBuddy.getTabButtonUniqueID());
            if (tabButton == null){
                PointBoxTracer.recordSevereException(logger, new Exception("tabButton - " + stateForBuddy.getTabButtonUniqueID() + "should not be NULL"));
//                return talker.get.aquireStateForBuddy(this, loginUser, buddy);
            }else{
                if (largestTabButtonWidth < ((JPanel)tabButton).getPreferredSize().width){
                    largestTabButtonWidth = ((JPanel)tabButton).getPreferredSize().width;
                }
            }
          }
        
        return stateForBuddy;
    }

    /**
     * If the state exists, it will be returned; if not, a new one created will be returned
     * @param loginUser
     * @param group
     * @param members
     * @return 
     */
    private IMessagingBoardState aquireStateForGroup(IGatewayConnectorBuddy loginUser, 
                                                     IGatewayConnectorGroup group, 
                                                     ArrayList<IGatewayConnectorBuddy> members) throws OutOfEdtException
    {
        if (!SwingUtilities.isEventDispatchThread()){
            throw new OutOfEdtException();
        }
        if (group == null){
            throw new RuntimeException("Bad parameters for aquireStateForGroup");
        }
        
        IMessagingBoardState stateForGroup = MessagingPaneManager.getSingleton(talker).aquireStateForGroup(this, loginUser, group, members);
        if (stateForGroup == null){
            throw new RuntimeException("Cannot get an IMessagingBoardState instance");
        }else{
            IButtonTabComponent tabButton = MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, stateForGroup.getTabButtonUniqueID());
            if (tabButton == null){
                PointBoxTracer.recordSevereException(logger, new Exception("tabButton should not be NULL"));
            }else{
                if (largestTabButtonWidth < ((JPanel)tabButton).getPreferredSize().width){
                    largestTabButtonWidth = ((JPanel)tabButton).getPreferredSize().width;
                }
            }
        }
        return stateForGroup;
    }

    private int getLargestTabButtonWidth() {
        return largestTabButtonWidth;
    }

    private JPanel getTabButtonRowPanel(int rowIndex){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            if ((rowIndex > -1) && (rowIndex < tabButtonRowPanels.size())){
                return tabButtonRowPanels.get(rowIndex);
            }else{
                return null;
            }
        }//synchronized(tabButtonRowPanels){
    }

    private JPanel getLastTabButtonRowPanel(){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            if (tabButtonRowPanels.isEmpty()){
                return null;
            }
            return tabButtonRowPanels.getLast();
        }//synchronized(tabButtonRowPanels){
    }

    private JPanel getFirstTabButtonRowPanel(){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            if (tabButtonRowPanels.isEmpty()){
                return null;
            }
            return tabButtonRowPanels.getFirst();
        }//synchronized(tabButtonRowPanels){
    }

    private int getTabButtonRowPanelsSize(){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            return tabButtonRowPanels.size();
        }
    }

    private void addNewTabButtonRowPanel(JPanel row){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            tabButtonRowPanels.add(row);
        }
    }

    private void removeTabButtonRowPanel(JPanel row){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            tabButtonRowPanels.remove(row);
        }
    }

    private int indexOfTabButtonRowPanel(JPanel row){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        synchronized(tabButtonRowPanels){
            return tabButtonRowPanels.indexOf(row);
        }
    }

    private Rectangle getTabButtonRectangle(IButtonTabComponent tabButton) {
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        //ButtonTabComponent tabButton = getButtonTabComponent(getTargetState().getTabButtonUniqueID());
        Rectangle targetRectangle = new Rectangle();
        if (tabButton.getRowIndex() > -1){
            
            Rectangle rowRect;
            synchronized(tabButtonRowPanels){
                rowRect = tabButtonRowPanels.get(tabButton.getRowIndex()).getBounds();
            }
            if (rowRect != null){
                Rectangle btnRect = ((JPanel)tabButton).getBounds();
                targetRectangle = new Rectangle();
                targetRectangle.setBounds(btnRect.x, rowRect.y, btnRect.width, btnRect.height);
            }
        }
        return targetRectangle;
    }
    
    private void switchToTargetState(final IGatewayConnectorBuddy loginUser, 
                                     final IGatewayConnectorGroup group, 
                                     final ArrayList<IGatewayConnectorBuddy> members) 
    {
        if (group == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            try {
                switchToTargetState(aquireStateForGroup(loginUser, group, members));
            } catch (OutOfEdtException ex) {
                PointBoxTracer.displaySevereMessage(logger, ex);
            }
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    try {
                        switchToTargetState(aquireStateForGroup(loginUser, group, members));
                    } catch (OutOfEdtException ex) {
                        PointBoxTracer.displaySevereMessage(logger, ex);
                    }
                }
            });
        }
    }
    
    private void switchToTargetState(final IGatewayConnectorBuddy loginUser, 
                                     final IGatewayConnectorGroup group, 
                                     final ArrayList<IGatewayConnectorBuddy> members, 
                                     final String message) 
    {
        if (group == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            try {
                switchToTargetStateHelper(aquireStateForGroup(loginUser, group, members));
            } catch (OutOfEdtException ex) {
                PointBoxTracer.displaySevereMessage(logger, ex);
            }
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    try {
                        switchToTargetStateHelper(aquireStateForGroup(loginUser, group, members));
                    } catch (OutOfEdtException ex) {
                        PointBoxTracer.displaySevereMessage(logger, ex);
                    }
                }
            });
        }
        
        insertDefaultMessage(message);
    }

    @Override
    public void insertBroadcastMessage(final String message) {
        if (DataGlobal.isEmptyNullString(message)){
            return;
        }
        if (DataGlobal.isNonEmptyNullString(message)){
            if (SwingUtilities.isEventDispatchThread()){
                jMessagingEntry.setText("");
                jBroadcastMessagingEntry.setText(message);
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        jMessagingEntry.setText("");
                        jBroadcastMessagingEntry.setText(message);
                    }
                });
            }
        }
    }
    
    @Override
    public void insertDefaultMessage(final String message){
        if (DataGlobal.isNonEmptyNullString(message)){
            if (SwingUtilities.isEventDispatchThread()){
                jMessagingEntry.setText(message);
            }else{
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        jMessagingEntry.setText(message);
                    }
                });
            }
        }
    }
    
    private void switchToTargetState(final IGatewayConnectorBuddy loginUser, 
                                     final IGatewayConnectorBuddy buddy)
    {
        if ((loginUser == null) || (buddy == null)) {
            return;
        }
        
        if (SwingUtilities.isEventDispatchThread()){
            try {
                switchToTargetState(aquireStateForBuddy(loginUser, buddy));
            } catch (OutOfEdtException ex) {
                PointBoxTracer.displaySevereMessage(logger, ex);
            }
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    try {
                        switchToTargetState(aquireStateForBuddy(loginUser, buddy));
                    } catch (OutOfEdtException ex) {
                        PointBoxTracer.displaySevereMessage(logger, ex);
                    }
                }
            });
        }
    }

    private void switchToEmptyState() {
        if (SwingUtilities.isEventDispatchThread()){
            switchToEmptyStateHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    switchToEmptyStateHelper();
                }
            });
        }
    }
    
    private void switchToEmptyStateHelper(){
        //update targetStateUniqueID
        targetStateUniqueID = null;
        //disable data entry so that users cannot continually type in data...
        jCommandBar.setEnabled(false);
        jFunctionBar.setEnabled(false);
        jMessagingEntry.setEditable(false);
        //populate the target state
        jMessagingHistory.setContentType("text/plain");
        jMessagingHistory.setDocument(new DefaultStyledDocument());
        jMessagingEntry.setDocument(new DefaultStyledDocument());
        jTargetName.setText("");
        jTargetName.setForeground(Color.blue);

        //enable data entry so that user can type in data
        jCommandBar.setEnabled(false);
        jFunctionBar.setEnabled(false);
    }
    
    @Override
    public void updateMemberList(){
        if (SwingUtilities.isEventDispatchThread()){
            jGroupMemberList.updateUI();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jGroupMemberList.updateUI();
                }
            });
        }       
    }
    
    void switchToTargetState(String tabUniqueID){
        switchToTargetState(MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabUniqueID));
    }
    
    /**
     * Switch the current target state to be the pass-in "state". After it becomes the target, it will 
     * be populated onto the board. If it has been the target, nothing happens.
     * @param newTargetState 
     */
    private void switchToTargetState(final IMessagingBoardState newTargetState){
        if (newTargetState == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            switchToTargetStateHelper(newTargetState);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    switchToTargetStateHelper(newTargetState);
                }
            });
        }
    }
    
    @Override
    public void refreshTextStyle(String tabId, IMessageTabRecord tabRecord) {
        IMessagingBoardState state = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabId);
        if (state != null && tabRecord != null) {
            state.customizeTextFormat(tabRecord);
            refreshPrice(tabId, true);
        }  
    }
    
    /**
     * This method has to be executed in EDT
     * @param newTargetState 
     */
    private void switchToTargetStateHelper(final IMessagingBoardState newTargetState){
        if (newTargetState == null){
            return;
        }
        synchronized(this){
            if (!newTargetState.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID)){
                //update targetStateUniqueID
                IMessagingBoardState oldTargetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(targetStateUniqueID);
                if (oldTargetState != null){
                    IButtonTabComponent tabButton = MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, oldTargetState.getTabButtonUniqueID());
                    if (tabButton != null){
                        tabButton.lostTargetInEDT();
                    }
                }
                targetStateUniqueID = newTargetState.getTabButtonUniqueID();
        //            if (!newTargetState.isGroupState()){
        //                TabButtonFlashingAgent.getSingleton(this).stopFlashingTabForDrag(targetStateUniqueID);
        //            }
                IButtonTabComponent tabButton = MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, newTargetState.getTabButtonUniqueID());
                if (tabButton != null){
                    tabButton.winTargetInEDT();
                }
                //refresh settings for the current targetState
                IMessageTabRecord messageTabRecord = talker.getKernel()
                        .getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(newTargetState.getTabButtonUniqueID());
                if (messageTabRecord == null){
                    PointBoxTracer.recordSevereTechnicalError(logger, 
                            new Exception("if messageTabID is unknown, it should offer a default record"));
                }else{
                    newTargetState.customizeTextFormat(messageTabRecord);
                }
                //disable data entry so that users cannot continually type in data...
                jCommandBar.setEnabled(false);
                jFunctionBar.setEnabled(false);
                jMessagingEntry.setEditable(false);

                //display tab button
                IButtonTabComponent newTargetButton = MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, newTargetState.getTabButtonUniqueID());
                displayButtonTabComponent(newTargetButton);

                //populate the target state
                newTargetState.loadMessagingHistoryDefaultStyledDocument(jMessagingHistory);
                newTargetState.loadMessagingEntryDefaultStyledDocument(jMessagingEntry);
                jShowPrice.setSelected(talker.getKernel().getPointBoxConsoleRuntime().getPointBoxTalkerSettings()
                        .getMessageTabRecord(newTargetState.getTabButtonUniqueID()).isDisplayPrices());
                if (jShowPrice.isSelected()){
                    jRefreshPrice.setEnabled(true);
                }else{
                    jRefreshPrice.setEnabled(false);
                }

                jTargetName.setForeground(Color.blue);

                //enable data entry so that user can type in data
                jCommandBar.setEnabled(true);
                jFunctionBar.setEnabled(true);
                if (DataGlobal.isEmptyNullString(newTargetState.getTabButtonUniqueID())){
                    jMessagingEntry.setEditable(false);
                }else{
                    jMessagingEntry.setEditable(true);
                }
                if (newTargetButton != null){
                    Rectangle rec = getTabButtonRectangle(newTargetButton);
                    if (rec != null){
                        jButtonPanel.scrollRectToVisible(rec);
                    }
                }
                refreshPrice(targetStateUniqueID, false);
                scrollToMessagingHistoryBottom(jMessagingHistory.getDocument());

            }//if (newTargetState != null){
            //always refresh this information because buddy-instance may be changed
            jTargetName.setText(newTargetState.getDescriptiveName());
        }
    }

    /**
     * Buddy instance is changed, e.g. nickname. Thus, the corresponding focus buddy 
     * tab require GUI change.
     * @param tabUniqueID
     * @param buddy 
     */
    void updateBuddyStatus(final String tabUniqueID, final IGatewayConnectorBuddy buddy) {
        if (SwingUtilities.isEventDispatchThread()){
            updateBuddyStatusHelper(tabUniqueID, buddy);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateBuddyStatusHelper(tabUniqueID, buddy);
                }
            });
        }
    }
    private void updateBuddyStatusHelper(final String tabUniqueID, final IGatewayConnectorBuddy buddy) {
        final IMessagingBoardState buddyState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabUniqueID);
        if (buddyState == null){
            return;
        }
        //buddyState is current targetStateUniqueID, which means currentGUI is displaying buddyState
        if (buddyState.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID)){
            jTargetName.setText(buddyState.getDescriptiveName());
        }
    }
    
    protected void populateCheckTreeItems(final boolean isAutoSend) {
        
    }

    /**
     * a buddy was clicked by users from the buddy list panel. 
     * If the pass-in state's ID does not match current target ID, this method switches to the state of this 
     * new target . If the state unique ID matches the target unique ID, this method does nothing because it 
     * has been populated and is the target.
     * @param loginUser
     * @param buddy 
     */
    @Override
    public void presentMessagingTab(final IGatewayConnectorBuddy loginUser, 
                                    final IGatewayConnectorBuddy buddy)
    {
        switchToTargetState(loginUser, buddy);
        grabTargetMessagingEntryFocus();
        populateCheckTreeItems(false);
    }

    @Override
    public void presentMessagingTab(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy, String message) {
        presentMessagingTab(loginUser, buddy);
        insertDefaultMessage(message);
        populateCheckTreeItems(false);
    }
    
    /**
     * a group was clicked by users from the buddy list panel
     * @param loginUser - useless. 
     * @param group 
     * @param members
     */
    @Override
    public void presentMessagingTab(final IGatewayConnectorBuddy loginUser, 
                                     final IGatewayConnectorGroup group, 
                                     final ArrayList<IGatewayConnectorBuddy> members) 
    {
        switchToTargetState(loginUser, group, members);
        populateCheckTreeItems(false);
    }
    
    @Override
    public void presentMessagingTab(final IGatewayConnectorBuddy loginUser, 
                                     final IGatewayConnectorGroup group, 
                                     final ArrayList<IGatewayConnectorBuddy> members, 
                                     final String message, final boolean isAutoSend) 
    {
         switchToTargetState(loginUser, group, members, message);
         populateCheckTreeItems(isAutoSend);
    }

    /**
     * Make sure the tabButton displayed. But it does not guarantee such a button becoming the target button
     * @param tabButton 
     */
    private void displayButtonTabComponent(final IButtonTabComponent tabButton) {
        if (tabButton == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            displayButtonTabComponentHelper(tabButton);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    displayButtonTabComponentHelper(tabButton);
                }
            });
        }
    }
    private void displayButtonTabComponentHelper(IButtonTabComponent tabButton) {
        if (tabButton == null){
            return;
        }
        //check if it has been existing
        if (getVisibleTabButton(tabButton) != null){
            return;
        }
        tabButton.setBoard(this);
        JPanel row = getLastTabButtonRowPanel();
        if (row == null){
            //add first button on the first row  (special case, the button length is longer than the row length)
            addFirstTabButtonOnNewPanelRow(tabButton);
        }else{
            int availableLastRowLength = 0;
            //calculate the avilable space in the current row
            Component[] components = row.getComponents();
            for (Component component : components){
                if (component instanceof ButtonTabComponent){
                    availableLastRowLength += ((ButtonTabComponent)component).getPreferredSize().width;
                }
            }
            availableLastRowLength = jScrollButtonPane.getVisibleRect().width - availableLastRowLength;
            //decide how to display tab button
            if ((availableLastRowLength > ((JPanel)tabButton).getPreferredSize().width) 
                    || (availableLastRowLength == jScrollButtonPane.getVisibleRect().width))//the new button too big
            {
                //it has space, simply add it in
                appendTabButtonToLastRow(tabButton);
            }else{
                //it has no space, add new row and then add its first tab button in
                addFirstTabButtonOnNewPanelRow(tabButton);
            }
        }
        //adjust the final panels
        adjustTabButtonPanelSize();
    }
    
    /**
     * Add new tab button to the end of the latest row in jTabButtonPanel. The is method assumes 
     * the caller knows the last row has space for tabButton and simply append tabButton to the 
     * end of the row
     * @param tabButton 
     */
    private void appendTabButtonToLastRow(final IButtonTabComponent tabButton) {
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        JPanel row = getLastTabButtonRowPanel();
        if (row != null){
            row.add((JPanel)tabButton);
            row.invalidate();
            tabButton.setRowIndex(indexOfTabButtonRowPanel(row));
        }
    }
    
    /**
     * Add a new row (i.e., a JPanel with FlowLayout) into jTabButtonPanel for new buttons. 
     * This method assumes the caller knows that jButtonPanel's layout manager is ready for 
     * this new row (i.e., a new JPanel with FlowLayout) added in.
     * @return 
     */
    private void addFirstTabButtonOnNewPanelRow(final IButtonTabComponent tabButton){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        //create and cache a new row instance (i.e., a JPanel with FlowLayout)
        JPanel row = new JPanel();
        if (getTabButtonRowPanelsSize() % 2 == 0){
            row.setLayout(new FlowLayout(SwingConstants.LEADING, 0, 0));
        }else{
            row.setLayout(new FlowLayout(SwingConstants.TRAILING, 0, 0));
        }
        //record that row...
        addNewTabButtonRowPanel(row);
        //add new row into jButtonPanel
        jButtonPanel.setLayout(new GridLayout(getTabButtonRowPanelsSize(), 1, 0, 0));
        jButtonPanel.add(row);
        jButtonPanel.validate();
        
        appendTabButtonToLastRow(tabButton);
    }
    
    /**
     * according to the current size of jButtonPanel, adjust the size of jTabButtonPanel
     */
    private void adjustTabButtonPanelSize(){
        if (SwingUtilities.isEventDispatchThread()){
            adjustTabButtonPanelSizeHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    adjustTabButtonPanelSizeHelper();
                }
            });
        }
    }
    private void adjustTabButtonPanelSizeHelper(){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        int height;
        JPanel row = getFirstTabButtonRowPanel();
        if (row == null){
            height = jButtonPanel.getPreferredSize().height;
        }else{
            height = row.getPreferredSize().height * getTabButtonRowPanelsSize();
        }
        height += 2;
        
        int width = jScrollButtonPane.getVisibleRect().width + 3;
        //todo: how to getMaxTabButtonPanelHeight
        int max = (int)(this.getBounds().getHeight()*0.5);
        if (height > max){
            height = max;
        }
        Dimension size = new Dimension(width, 
                                       height);
        jTabButtonPanel.setPreferredSize(size);
        jTabButtonPanel.updateUI();
    }
    
    /**
     * This method is called when talker is personalized. An internal 
     * will be initialized if it is still NULL. Otherwise, this method does nothings
     */
    @Override
    public void personalizeMessagingBoard() {
        if (SwingUtilities.isEventDispatchThread()){
            personalizeMessagingBoardHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    personalizeMessagingBoardHelper();
                }
            });
        }
    }
    private void personalizeMessagingBoardHelper(){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        switchToEmptyState();
    }
        
    void publishParsedMessage(final ArrayList<IPbsysOptionQuote> parsedQuotes) {
        messagingService.submit(new Runnable(){
            @Override
            public void run() {
                try{
                    IPbsysInstantMessage message;
                    for (IPbsysOptionQuote quote : parsedQuotes){
                        if (quote != null){
                            //get message with settings....
                            message = quote.getInstantMessage();
                            if (message instanceof IBroadcastedMessage){
                                publishParsedMessageForGroup(quote);
                            }else{
                                publishParsedMessageForBuddy(quote);
                            }
                        }
                    }
                }catch (Exception ex){
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        });
    }

    private void publishParsedMessageForGroup(final IPbsysOptionQuote quote) {
        IBroadcastedMessage data = (IBroadcastedMessage)(quote.getInstantMessage());
        ArrayList<IGatewayConnectorBuddy> members = data.getMembers();
        for (IGatewayConnectorBuddy member : members){
            if (data.isOutgoing()){
                data.setToUser(member);
            }else{
                data.setFromUser(member);
            }
            publishParsedMessageForBuddy(quote);
        }
    }
       
    private void publishParsedMessageForBuddy(final IPbsysOptionQuote quote) {
        IPbsysInstantMessage message = quote.getInstantMessage();
        IMessagingBoardState state = null;
        //IGatewayConnectorBuddy loginUser; 
        IGatewayConnectorBuddy buddy;
        if (message.isOutgoing()){
            //loginUser = message.getFromUser();
            buddy = message.getToUser();

        }else{
            //loginUser = message.getToUser();
            buddy = message.getFromUser();
        }
        if (buddy.getLoginOwner() == null){
            return;
        }
        try {
            state = MessagingPaneManager.getSingleton().getMessagingBoardState(buddy.getLoginOwner(), buddy);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if (state != null){
            IMessageTabRecord messageTabRecord = talker.getKernel()
                    .getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(state.getTabButtonUniqueID());
            if (!quote.isSufficientPricingData()){
                //insert talking messages....
                try {
                    synchronized(this){
                        if (state.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID)){
                            IMessageTabRecord record = talker.getKernel().getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(targetStateUniqueID);
                            final Document doc = state.getCopyOfMessagingHistoryDefaultStyledDocument(record);
                            //set a document copy
                            setMessagingHistoryDefaultStyledDocument(doc);
                            //insert message into the real target document
                            state.insertMessageLine(quote, messageTabRecord);
                            //set the target document
                            state.loadMessagingHistoryDefaultStyledDocument(jMessagingHistory);
                            //scroll to the bottom
                            scrollToMessagingHistoryBottom(jMessagingHistory.getDocument());
                        }else{
                            state.insertMessageLine(quote, messageTabRecord);
                        }
                    }
                } catch (BadLocationException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            } 
        }
    }
    
   
    
    /**
     * jShowPrice state
     * @param pricedQuotes 
     */
    void publishPricedMessage(final ArrayList<IPbsysOptionQuote> pricedQuotes) {
        messagingService.submit(new Runnable(){
            @Override
            public void run() {
                try{
                    for (IPbsysOptionQuote quote : pricedQuotes){
                        if (quote.isSufficientPricingData()){
                            //get message with settings....
                            IPbsysInstantMessage message = quote.getInstantMessage();
                            if (message instanceof IBroadcastedMessage){
                                publishPricedMessageForGroup(quote);
                            }else{
                                publishPricedMessageForBuddy(quote);
                            }
                        }
                    }
                }catch (Exception ex){
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        });
    }
    
//    /**
//     * Popup an existing tab into a floating frame
//     * @param existingTabUniqueID - id of the first tab on this frame
//     */
//    void popupNewFloatingFrame(ButtonTabComponent buttonTabComponent) {
//        MessagingPaneManager.getSingleton(talker).popupMessagingTabFromMaster(uuid, uuid, buttonTabComponent);
//    }
    
//    /**
//     * Popup an existing tab into a new floating frame but remove this 
//     * tab from this board
//     * @param existingTabUniqueID - id of the first tab on this frame
//     */
//    void popupNewFloatingFrame(String existingTabUniqueID) {
//        MessagingBoardState targetState = stateStorage.get(existingTabUniqueID);
//         if (targetState != null) {
//            MessagingPaneManager.getSingleton(talker).createFloatingMessagingBoard(targetState);
//        }
//    }

    private void publishPricedMessageForGroup(final IPbsysOptionQuote quote) {
        IBroadcastedMessage data = (IBroadcastedMessage)(quote.getInstantMessage());
        ArrayList<IGatewayConnectorBuddy> members = data.getMembers();
        for (IGatewayConnectorBuddy member : members){
            if (data.isOutgoing()){
                data.setToUser(member);
            }else{
                data.setFromUser(member);
            }
            publishPricedMessageForBuddy(quote);
        }
    }

    private void publishPricedMessageForBuddy(final IPbsysOptionQuote quote) {
        //get message with settings....
        IPbsysInstantMessage message = quote.getInstantMessage();
        IMessagingBoardState state;
        //IGatewayConnectorBuddy loginUser; 
        IGatewayConnectorBuddy buddy;
        if (message.isOutgoing()){
            //loginUser = message.getFromUser();
            buddy = message.getToUser();

        }else{
            //loginUser = message.getToUser();
            buddy = message.getFromUser();
        }
        if (buddy.getLoginOwner() == null){
            //this buddy has not login yet
            return;
        }
        state = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(buddy.getLoginOwner(), buddy);
        if (state != null){
            IMessageTabRecord messageTabRecord = talker.getKernel()
                    .getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(state.getTabButtonUniqueID());
//            if ((quote.isSufficientPricingData()) && (messageTabRecord.isDisplayPrices())){
            if ((quote.isSufficientPricingData()) 
                    || (quote.isPricablePowerUser()))
            {
                //show price
                try {
                    synchronized(this){
                        if (state.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID)){
                            IMessageTabRecord record = talker.getKernel().getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(targetStateUniqueID);
                            final Document doc = state.getCopyOfMessagingHistoryDefaultStyledDocument(record);
                            //set a document copy
                            setMessagingHistoryDefaultStyledDocument(doc);
                            //insert message into the real target document
                            state.insertMessageLine(quote, messageTabRecord);
                            //set the target document
                            state.loadMessagingHistoryDefaultStyledDocument(jMessagingHistory);
                            //scroll to the bottom
                            scrollToMessagingHistoryBottom(jMessagingHistory.getDocument());
                        }else{
                            state.insertMessageLine(quote, messageTabRecord);
                        }
                    }
                } catch (BadLocationException ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        }
    }
        
    void publishQuoteOnGroupMessageTab(final IPbsysOptionQuote quote){
        PointBoxTracer.displayMessage(logger, new Exception("BROADCAST - publishQuoteOnGroupMessageTab is not implemented yet."));
    }
    
    /**
     * Publish a "quote" from "loginUser" onto a message tab of "buddy"
     */
    @Override
    public void publishQuoteOnMessageTab(final IGatewayConnectorBuddy loginUser, 
                                  IGatewayConnectorBuddy buddy,
                                  final IPbsysOptionQuote quote)
    {
        if (loginUser == null){
            PointBoxTracer.recordSevereException(logger, new Exception("publishQuoteOnMessageTab::loginUser is NULL"));
            return;
        }
        if (quote == null){
            return;
        }
        IPbsysInstantMessage msg = quote.getInstantMessage();
        if (msg == null){
            return;
        }
        if (buddy == null){
            if (msg.isOutgoing()){
                buddy = msg.getToUser();
            }else{
                buddy = msg.getFromUser();
            }
        }
        if (buddy == null){
            PointBoxTracer.recordSevereException(logger, new Exception("publishQuoteOnMessageTab::buddy is NULL"));
            return;
        }
        final IGatewayConnectorBuddy buddyInstance = buddy;
        if (SwingUtilities.isEventDispatchThread()){
            publishQuoteOnMessageTabHelper(loginUser, buddyInstance, quote);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    publishQuoteOnMessageTabHelper(loginUser, buddyInstance, quote);
                }
            });
        }
    }
    private void publishQuoteOnMessageTabHelper(final IGatewayConnectorBuddy loginUser, 
                                           final IGatewayConnectorBuddy buddy,
                                           final IPbsysOptionQuote quote)
    {
        try {
            IMessagingBoardState state = aquireStateForBuddy(loginUser, buddy);
            if (state != null){
                //treat other GUI features....
                IPbsysInstantMessage message = quote.getInstantMessage();
                if ((message.isOutgoing()) && (state.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID))){
                    //in the case of outgoing message, current entry need a live caret
                    //grabTargetMessagingEntryFocus();            //no need to have it. If it grabed, it will make unexpected influnence on the Floating Borad.
                }else{
                    //display tab button in case it was not there
//                    boolean isDisplay=true;
//                    synchronized(MessagingPaneManagerImpl.floatingFrameStorage){
//                        Collection<FloatingMessagingFrame> frames =MessagingPaneManagerImpl.floatingFrameStorage.values();
//                        for (FloatingMessagingFrame frame : frames){
//                            for(ButtonTabComponent tab:frame.getFloatingMessagingBoard().getAllVisibleTabeButtons()){
//                                if(tab.getTabUniqueID().equals(state.getTabButtonUniqueID())&&!MessagingPaneManagerImpl.isDraggedTabVisble){
//                                    isDisplay=false;
//                                    break;
//                                }
//                            }
//                        }   
//                    }
//                    if(isDisplay){
                        displayButtonTabComponent(MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, state.getTabButtonUniqueID()));
//                    }
//////                    if ((targetStateUniqueID == null) 
//////                            || (targetStateUniqueID.equalsIgnoreCase(EMPTY_STATE.getTabButtonUniqueID())))
                    if (targetStateUniqueID == null) 
                    {
                        //first tab to be the target
                        switchToTargetState(state);
                    }else{
                        //not current target tab, flash the tab
                        if ((!(state.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID))) 
                                && (!message.isOutgoing())){
                            TabButtonFlashingAgent.getSingleton(MasterMessagingBoard.this)
                                    .startFlashingTab(state.getTabButtonUniqueID());
                        }
                    }
                }
            }
        } catch (OutOfEdtException ex) {
            PointBoxTracer.recordSevereException(logger, ex);
        }
        talker.getKernel().raisePointBoxEvent(
                new TalkerPublishedQuoteEvent(PointBoxEventTarget.PbcFace,
                                              quote));
    }
    
    
    /*
     * Refresh the buddy tab for solving the the bug of buddy missing
     */
     public void publishQuoteOnMessageTabHelper2(final IGatewayConnectorBuddy loginUser, 
                                           final IGatewayConnectorBuddy buddy,
                                           final IPbsysOptionQuote quote)
    {
        try {
            IMessagingBoardState state = aquireStateForBuddy(loginUser, buddy);
            if (state != null){
                displayButtonTabComponent(MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, state.getTabButtonUniqueID()));

                if (targetStateUniqueID == null) 
                {
                    //first tab to be the target
                    switchToTargetState(state);
                }else{
                    //not current target tab, flash the tab
                    if ((!(state.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID))) 
                            ){
                        TabButtonFlashingAgent.getSingleton(MasterMessagingBoard.this)
                                .startFlashingTab(state.getTabButtonUniqueID());
                    }
                }
            }
        } catch (OutOfEdtException ex) {
            PointBoxTracer.recordSevereException(logger, ex);
        }
       
    }
     
    private void scrollToMessagingHistoryBottomHelper(final Document textDoc){
        DefaultCaret caret =(DefaultCaret) jMessagingHistory.getCaret();
        if (caret.getUpdatePolicy() != DefaultCaret.ALWAYS_UPDATE){
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
        }
        jMessagingHistory.setCaretPosition(textDoc.getLength());
    }
    
    void scrollToMessagingHistoryBottom(final Document textDoc){
        if (SwingUtilities.isEventDispatchThread()){
            scrollToMessagingHistoryBottomHelper(textDoc);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    scrollToMessagingHistoryBottomHelper(textDoc);
                }
            });
        }
    }
    
    @Override
    public void setTabButtonForegroundAtInEDT(final List<IButtonTabComponent> tabButtonList, final Color currentForeground) {
        if (SwingUtilities.isEventDispatchThread()){
            setTabButtonForegroundAtInEDTHelper(tabButtonList, currentForeground);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setTabButtonForegroundAtInEDTHelper(tabButtonList, currentForeground);
                }
            });
        }
    }
    private void setTabButtonForegroundAtInEDTHelper(final List<IButtonTabComponent> tabButtonList, final Color currentForeground) {
        for (IButtonTabComponent tabButton : tabButtonList){
            if (tabButton instanceof JPanel){
                ((JPanel)tabButton).setForeground(currentForeground);
            }
        }
    }
    
    @Override
    public void setTabButtonBackgroundAtInEDT(final List<IButtonTabComponent> tabButtonList, final Color currentBackground) {
        if (SwingUtilities.isEventDispatchThread()){
            setTabButtonBackgroundAtInEDTHelper(tabButtonList, currentBackground);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setTabButtonBackgroundAtInEDTHelper(tabButtonList, currentBackground);
                }
            });
        }
    }
    private void setTabButtonBackgroundAtInEDTHelper(final List<IButtonTabComponent> tabButtonList, final Color currentBackground) {
        for (IButtonTabComponent tabButton : tabButtonList){
            if (tabButton instanceof JPanel){
                ((JPanel)tabButton).setBackground(currentBackground);
            }
        }
    }
    
    @Override
    public void setOpaqueInEDT(final List<IButtonTabComponent> tabButtonList, final boolean value) {
        if (SwingUtilities.isEventDispatchThread()){
            setOpaqueInEDTHelper(tabButtonList, value);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setOpaqueInEDTHelper(tabButtonList, value);
                }
            });
        }
    }
    private void setOpaqueInEDTHelper(final List<IButtonTabComponent> tabButtonList, final boolean value) {
        for (IButtonTabComponent tabButton : tabButtonList){
            if (tabButton instanceof JPanel){
                ((JPanel)tabButton).setOpaque(value);
            }
        }
    }
    
    void grabTargetMessagingEntryFocus() {
        if (SwingUtilities.isEventDispatchThread()){
            grabMessagingEntryFocusHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    grabMessagingEntryFocusHelper();
                }
            });
        }
    }
    private void grabMessagingEntryFocusHelper() {
        jMessagingEntry.grabFocus();
        jMessagingEntry.setCaretPosition(jMessagingEntry.getDocument().getLength());
    }
    
    /**
     * if they are the target, the relevant face should be updated
     * @param loginUser
     * @param buddy
     * @param buddyProfile - reserved for the future
     */
    @Override
    public void updateBuddyProfile(final IGatewayConnectorBuddy loginUser, 
                                  final IGatewayConnectorBuddy buddy, 
                                  final IBuddyProfileRecord buddyProfile) 
    {
        //todo: how to update GUI text for updated buddyProfile
    }
    
    /**
     * 
     * Layout all the visible tab buttons according to the current space of tab button panel. The 
     * sequence of tab buttons is the same as the current sequence of visibleButtonTabComponents in 
     * memory.
     * @param tabButtons 
     */
    @Override
    public void sortMessagingBoardTabButtonsInEDT(final boolean fromAtoZ) {
        if (SwingUtilities.isEventDispatchThread()){
            sortMessagingBoardTabButtonsInEDTHelper(fromAtoZ);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    sortMessagingBoardTabButtonsInEDTHelper(fromAtoZ);
                }
            });
        }
    }
    private void sortMessagingBoardTabButtonsInEDTHelper(final boolean fromAtoZ) {
        final ArrayList<IButtonTabComponent> tabButtons = sortVisibleTabButtons(fromAtoZ);
        IMessagingBoardState currentTargetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(targetStateUniqueID);
        if (currentTargetState == null){
            ButtonTabComponent tabButton = getAnyVisibleTabButton(null);
            if (tabButton == null){
                switchToEmptyState();
                return;
            }else{
                currentTargetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabButton.getTabUniqueID());
            }
        }
        hideAllButtonTabComponents();
        for (IButtonTabComponent tabButton : tabButtons){
            if (DataGlobal.isNonEmptyNullString(tabButton.getTabUniqueID())){
                displayButtonTabComponent(tabButton);
            }
        }
        switchToTargetState(currentTargetState);
    }
    
    /**
     * This method is executed in EDT
     */
    private void layoutMessagingBoardTabButtons(final ArrayList<IButtonTabComponent> tabButtons) {
        if (SwingUtilities.isEventDispatchThread()){
            layoutMessagingBoardTabButtonsHelper(tabButtons);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    layoutMessagingBoardTabButtonsHelper(tabButtons);
                }
            });
        }
    }
    /**
     * This method has to be in EDT
     */
    private void layoutMessagingBoardTabButtonsHelper(final ArrayList<IButtonTabComponent> tabButtons) {
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        //keep tab buttons and target state
        IMessagingBoardState currentTargetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(targetStateUniqueID);
        if (currentTargetState == null){
            ButtonTabComponent tabButton = getAnyVisibleTabButton(null);
            if (tabButton == null){
                switchToEmptyState();
                return;
            }else{
                currentTargetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabButton.getTabUniqueID());
            }
        }
        //remove all the original visible buttons...
        hideAllButtonTabComponents();
        //restore all the visible tab buttons....
        for (IButtonTabComponent tabButton : tabButtons){
            if (DataGlobal.isNonEmptyNullString(tabButton.getTabUniqueID())){
                displayButtonTabComponent(tabButton);
            }
        }
        //restore original target state....
        switchToTargetState(currentTargetState);
    }
    
    /**
     * This method is executed in EDT
     */
    private void hideAllButtonTabComponents() {
        if (SwingUtilities.isEventDispatchThread()){
            hideAllButtonTabComponentsInEDTHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    hideAllButtonTabComponentsInEDTHelper();
                }
            });
        }
    }
    
    /**
     * This method has to be in EDT
     */
    private void hideAllButtonTabComponentsInEDTHelper(){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        try{
            ArrayList<IButtonTabComponent> visibleButtons = getAllVisibleTabeButtons();
            JPanel row;
            int rowIndex;
            for (IButtonTabComponent visibleButton : visibleButtons){
                if (visibleButton instanceof ButtonTabComponent){
                    ButtonTabComponent hidingTabButton = (ButtonTabComponent)visibleButton;
                    rowIndex = hidingTabButton.getRowIndex();
                    if (rowIndex < 0){
                        PointBoxTracer.displaySevereMessage(logger, "TECH BUG: negitive index value");
                    }else{
                        //get its row...
                        row = getTabButtonRowPanel(rowIndex);
                        if (row == null){
                            PointBoxTracer.displaySevereMessage(logger, "TECH BUG: row should not be NULL");
                        }else{
                            hidingTabButton.setRowIndex(-1);
                            //remove it from its row
                            row.remove(hidingTabButton);
                            //get the last tab button on this row....
                            ButtonTabComponent newTargetButton = null;
                            Component[] components = row.getComponents();
                            for (Component component : components){
                                if (component instanceof ButtonTabComponent){
                                    newTargetButton = (ButtonTabComponent)component;
                                }
                            }//for
                            if (newTargetButton == null){  
                                //its row only has this tabButton and becomes empty row now
                                removeEmptyRowInEDT(rowIndex);
                            }
                        }
                    }
                    hidingTabButton.paintClosingButtonBorder(false);
                }
            }//for
            if (!visibleButtons.isEmpty()){
//////                switchToTargetState(EMPTY_STATE);
                switchToEmptyState();
            }
            jButtonPanel.updateUI();
        }catch (Exception ex){
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }
    /**
     * a row becomes empty. It will be removed from jButtonPanel. And all the button indices 
     * will be modified and jTabButtonPanel size will be adjusted.
     * @param rowIndex 
     */
    private void removeEmptyRowInEDT(final int rowIndex) {
        if (SwingUtilities.isEventDispatchThread()){
            removeEmptyRowInEDTHelper(rowIndex);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    removeEmptyRowInEDTHelper(rowIndex);
                }
            });
        }
    }
    private void removeEmptyRowInEDTHelper(final int rowIndex) {
        JPanel row = getTabButtonRowPanel(rowIndex);
        if (row != null){
            removeTabButtonRowPanel(row);
            jButtonPanel.remove(row);
            jButtonPanel.setLayout(new GridLayout(getTabButtonRowPanelsSize(), 1, 0, 0));
            jButtonPanel.getLayout().layoutContainer(jButtonPanel);
            updateRowIndicesInEDT(rowIndex);
            adjustTabButtonPanelSize();
        }
    }
    
    @Override
    public void hideButtonTabComponent(String tabUniqueID) {
        IMessagingBoardState state = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabUniqueID);
        if (state != null){
            hideButtonTabComponent(MessagingPaneManager.getSingleton(talker).getButtonTabComponent(this, state.getTabButtonUniqueID()));
        }
    }

    /**
     * The last tab button in the row at which the tabButton stays. If the tabButton is the only one at such 
     * a row, its previous row will be used to calculate the next target button. 
     * This method is executed in EDT
     * @param tabButton
     * @return 
     */
    @Override
    public void hideButtonTabComponent(final IButtonTabComponent tabButton) {
        if (tabButton == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            hideButtonTabComponentHelper(tabButton);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    hideButtonTabComponentHelper(tabButton);
                }
            });
        }
    }
    /**
     * This method has to be in EDT
     */
    private void hideButtonTabComponentHelper(IButtonTabComponent hidingTabButton){
//        logger.log(Level.INFO, null, new Exception("BoardID - " + getBoardId() 
//                + " = hidingTabButton.getTabUniqueID() -> " + hidingTabButton.getTabUniqueID() 
//                + "hidingTabButton.getBoard().getBoardId() -> " + hidingTabButton.getBoard().getBoardId()));
        if (hidingTabButton == null){
            PointBoxTracer.displaySevereMessage(logger, "tabButton cannot be NULL");
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereOutOfEDT(logger);
        }
        //check if the hidingTabButton has been existing..
        hidingTabButton = getVisibleTabButton(hidingTabButton);
        if (hidingTabButton != null){
            JPanel row;
            ButtonTabComponent newTargetButton = null;
            int rowIndex = hidingTabButton.getRowIndex();
            if (rowIndex < 0){
                PointBoxTracer.displaySevereMessage(logger, "TECH BUG: negitive index value");
            }else{
                //get its row...
                row = getTabButtonRowPanel(rowIndex);
                if (row == null){
                    PointBoxTracer.displaySevereMessage(logger, "TECH BUG: row should not be NULL");
                }else{
                    hidingTabButton.setRowIndex(-1);
                    //remove it from its row
                    row.remove((JPanel)hidingTabButton);
                    //get the last tab button on this row....
                    Component[] components = row.getComponents();
                    for (Component component : components){
                        if (component instanceof ButtonTabComponent){
                            newTargetButton = (ButtonTabComponent)component;
                        }
                    }//for
                    if (newTargetButton == null){  
                        //its row only has this tabButton and becomes empty row now
                        removeEmptyRowInEDT(rowIndex);
                    }
                }
            }
            hidingTabButton.paintClosingButtonBorder(false);
            if (hidingTabButton.getTabUniqueID().equalsIgnoreCase(targetStateUniqueID)){
                //switch target state...
                if (newTargetButton == null){
                    newTargetButton = getAnyVisibleTabButton(hidingTabButton);
                }
                if (newTargetButton == null){
//////                    switchToTargetState(EMPTY_STATE);
                    switchToEmptyState();
                }else{
                    IMessagingBoardState state = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(newTargetButton.getTabUniqueID());
                    if (state != null){
                        switchToTargetState(state);
                    }
                }
            }
            layoutMessagingBoardTabButtons(getAllVisibleTabeButtons());
            jButtonPanel.updateUI();
        }
    }
    
    /**
     * Synchronized by EDT. A row (at removedRowIndex) was removed from rows. The other 
     * rows are promoted and their tab buttons should update its row indices
     * @param removedRowIndex 
     */
    private void updateRowIndicesInEDT(final int removedRowIndex){
        if (SwingUtilities.isEventDispatchThread()){
            updateRowIndicesInEDTHelper(removedRowIndex);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateRowIndicesInEDTHelper(removedRowIndex);
                }
            });
        }
    }
    private void updateRowIndicesInEDTHelper(final int removedRowIndex){
        JPanel row;
        int total = getTabButtonRowPanelsSize();
        for (int k = removedRowIndex; k < total; k++){
            row = getTabButtonRowPanel(k);
            Component[] components = row.getComponents();
            for (Component component : components){
                if (component instanceof ButtonTabComponent){
                    ((ButtonTabComponent)component).setRowIndex(k);
                }
            }
        }//for
    }
    
    public void oneClickForDefaultShow(){
        if(SwingUtilities.isEventDispatchThread()){
            oneClickForDefaultShowHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    oneClickForDefaultShowHelper();
                }
            });
        }

    }
    
    public void oneClickForDefaultShowHelper(){
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                oneClickDown.doClick();
                oneClickRight.doClick();
                try {
                    Thread.sleep(10);  //take a break;
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
            @Override
            protected void done() {
                oneClickDown.setEnabled(false);
                oneClickUp.setEnabled(true);
                oneClickLeft.setEnabled(true);
                oneClickRight.setEnabled(false);  
            }
            
        }.execute();        
    }
    /**
     * A tabButtonClicked happened on a tabButton, which means such a tabButton's state 
     * should be the new target state for MasterMessagingBoard.
     * @param tabButton 
     */
    @Override
    public void tabButtonClicked(final IButtonTabComponent tabButton) {
        switchToTargetState(MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabButton.getTabUniqueID()));
        jMessagingEntry.grabFocus();
//        if(jShowPrice.isSelected()){
            refreshPrice(targetStateUniqueID, true);
//        }
        populateCheckTreeItems(false);
    }
    
    @Override
    public void tabButtonClicked(final String tabUniqueId) {
        switchToTargetState(MessagingPaneManager.getSingleton(talker).getMessagingBoardState(tabUniqueId));
        populateCheckTreeItems(false);
    }
    
    private ButtonTabComponent getLastTabButtonOfRowInEDT(JPanel row){
        if (!SwingUtilities.isEventDispatchThread()){
            PointBoxTracer.displaySevereMessage(logger, "Out of EDT");
        }
        ButtonTabComponent targetButton = null;
        Component[] components = row.getComponents();
        for (Component component : components){
            if (component instanceof ButtonTabComponent){
                targetButton = (ButtonTabComponent)component;
            }
        }
        return targetButton;
    }
    
    private boolean isEmptyRow(JPanel row){
        ButtonTabComponent targetButton = null;
        Component[] components = row.getComponents();
        for (Component component : components){
            if (component instanceof ButtonTabComponent){
                targetButton = (ButtonTabComponent)component;
                break;
            }
        }
        return (targetButton == null);
    }
    
    private ButtonTabComponent getFirstTabButtonOfRow(JPanel row){
        ButtonTabComponent targetButton = null;
        Component[] components = row.getComponents();
        for (Component component : components){
            if (component instanceof ButtonTabComponent){
                targetButton = (ButtonTabComponent)component;
                break;
            }
        }
        return targetButton;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jTabButtonPanel = new javax.swing.JPanel();
        jScrollButtonPane = new javax.swing.JScrollPane();
        jButtonPanel = new javax.swing.JPanel();
        jSuperParentSplitPane = new javax.swing.JSplitPane();
        jBasePanel = new javax.swing.JPanel();
        jFunctionBar = new javax.swing.JToolBar();
        jLookup = new javax.swing.JButton();
        jSortingAZ = new javax.swing.JButton();
        jSortingZA = new javax.swing.JButton();
        jCloseAll = new javax.swing.JButton();
        jPersistFrameBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jTargetName = new javax.swing.JLabel();
        jCommandBar = new javax.swing.JToolBar();
        jShowPrice = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jRefreshPrice = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jTemplate = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jSend = new javax.swing.JButton();
        jParentSplitPane = new javax.swing.JSplitPane();
        jMessagingSplitPane = new javax.swing.JSplitPane();
        jMessagingHistoryScrollPane = new javax.swing.JScrollPane();
        jMessagingHistory = new javax.swing.JTextPane();
        jMembesrPanel = new javax.swing.JPanel();
        jListScrollPane = new javax.swing.JScrollPane();
        jGroupMemberList = new javax.swing.JList();
        jCheckAll = new javax.swing.JCheckBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        setMinimumSize(new java.awt.Dimension(370, 330));
        setLayout(new java.awt.BorderLayout());

        jTabButtonPanel.setMinimumSize(new java.awt.Dimension(23, 50));
        jTabButtonPanel.setName("jTabButtonPanel"); // NOI18N
        jTabButtonPanel.setLayout(new java.awt.BorderLayout());

        jScrollButtonPane.setName("jScrollButtonPane"); // NOI18N

        jButtonPanel.setName("jButtonPanel"); // NOI18N
        jButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 0, 0));
        jScrollButtonPane.setViewportView(jButtonPanel);

        jTabButtonPanel.add(jScrollButtonPane, java.awt.BorderLayout.CENTER);

        add(jTabButtonPanel, java.awt.BorderLayout.NORTH);

        jSuperParentSplitPane.setResizeWeight(0.75);
        jSuperParentSplitPane.setName("jSuperParentSplitPane"); // NOI18N
        jSuperParentSplitPane.setOneTouchExpandable(true);

        jBasePanel.setName("jBasePanel"); // NOI18N
        jBasePanel.setLayout(new java.awt.BorderLayout());

        jFunctionBar.setFloatable(false);
        jFunctionBar.setRollover(true);
        jFunctionBar.setName("jFunctionBar"); // NOI18N

        jLookup.setText("LK"); // NOI18N
        jLookup.setFocusable(false);
        jLookup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLookup.setName("jLookup"); // NOI18N
        jLookup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jLookup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLookupActionPerformed(evt);
            }
        });
        jFunctionBar.add(jLookup);

        jSortingAZ.setText("AZ"); // NOI18N
        jSortingAZ.setFocusable(false);
        jSortingAZ.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSortingAZ.setName("jSortingAZ"); // NOI18N
        jSortingAZ.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSortingAZ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSortingAZActionPerformed(evt);
            }
        });
        jFunctionBar.add(jSortingAZ);

        jSortingZA.setText("ZA");
        jSortingZA.setFocusable(false);
        jSortingZA.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSortingZA.setName("jSortingZA"); // NOI18N
        jSortingZA.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSortingZA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSortingZAActionPerformed(evt);
            }
        });
        jFunctionBar.add(jSortingZA);

        jCloseAll.setText("CA"); // NOI18N
        jCloseAll.setFocusable(false);
        jCloseAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCloseAll.setName("jCloseAll"); // NOI18N
        jCloseAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCloseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCloseAllActionPerformed(evt);
            }
        });
        jFunctionBar.add(jCloseAll);

        jPersistFrameBtn.setText("PF"); // NOI18N
        jPersistFrameBtn.setFocusable(false);
        jPersistFrameBtn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPersistFrameBtn.setName("jPersistFrameBtn"); // NOI18N
        jPersistFrameBtn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPersistFrameBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPersistFrameBtnActionPerformed(evt);
            }
        });
        jFunctionBar.add(jPersistFrameBtn);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jFunctionBar.add(jSeparator1);

        jTargetName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jTargetName.setText("Buddy");
        jTargetName.setName("jTargetName"); // NOI18N
        jFunctionBar.add(jTargetName);

        jBasePanel.add(jFunctionBar, java.awt.BorderLayout.PAGE_START);

        jCommandBar.setRollover(true);
        jCommandBar.setName("jCommandBar"); // NOI18N

        jShowPrice.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jShowPrice.setText("Price");
        jShowPrice.setFocusable(false);
        jShowPrice.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jShowPrice.setName("jShowPrice"); // NOI18N
        jShowPrice.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jShowPrice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jShowPriceItemStateChanged(evt);
            }
        });
        jCommandBar.add(jShowPrice);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jCommandBar.add(jSeparator2);

        jRefreshPrice.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jRefreshPrice.setText("Refresh");
        jRefreshPrice.setFocusable(false);
        jRefreshPrice.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jRefreshPrice.setName("jRefreshPrice"); // NOI18N
        jRefreshPrice.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jRefreshPrice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRefreshPriceActionPerformed(evt);
            }
        });
        jCommandBar.add(jRefreshPrice);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jCommandBar.add(jSeparator3);

        jTemplate.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jTemplate.setText("Pricer"); // NOI18N
        jTemplate.setFocusable(false);
        jTemplate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jTemplate.setName("jTemplate"); // NOI18N
        jTemplate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jTemplate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTemplateActionPerformed(evt);
            }
        });
        jCommandBar.add(jTemplate);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jCommandBar.add(jSeparator4);

        jSend.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jSend.setText("Send"); // NOI18N
        jSend.setFocusable(false);
        jSend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jSend.setName("jSend"); // NOI18N
        jSend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSendActionPerformed(evt);
            }
        });
        jCommandBar.add(jSend);

        jBasePanel.add(jCommandBar, java.awt.BorderLayout.SOUTH);

        jParentSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jParentSplitPane.setResizeWeight(0.75);
        jParentSplitPane.setName("jParentSplitPane"); // NOI18N
        jParentSplitPane.setOneTouchExpandable(true);

        jMessagingSplitPane.setDividerLocation(75);
        jMessagingSplitPane.setDividerSize(3);
        jMessagingSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jMessagingSplitPane.setResizeWeight(0.75);
        jMessagingSplitPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jMessagingSplitPane.setMinimumSize(new java.awt.Dimension(250, 200));
        jMessagingSplitPane.setName("jMessagingSplitPane"); // NOI18N
        jMessagingSplitPane.setPreferredSize(new java.awt.Dimension(250, 200));
        jMessagingSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jMessagingSplitPanePropertyChange(evt);
            }
        });

        jMessagingHistoryScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jMessagingHistoryScrollPane.setName("jMessagingHistoryScrollPane"); // NOI18N

        jMessagingHistory.setEditable(false);
        jMessagingHistory.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jMessagingHistory.setName("jMessagingHistory"); // NOI18N
        jMessagingHistoryScrollPane.setViewportView(jMessagingHistory);

        jMessagingSplitPane.setTopComponent(jMessagingHistoryScrollPane);

        jParentSplitPane.setLeftComponent(jMessagingSplitPane);

        jBasePanel.add(jParentSplitPane, java.awt.BorderLayout.CENTER);

        jSuperParentSplitPane.setLeftComponent(jBasePanel);

        jMembesrPanel.setName("jMembesrPanel"); // NOI18N
        jMembesrPanel.setLayout(new java.awt.BorderLayout());

        jListScrollPane.setName("jListScrollPane"); // NOI18N

        jGroupMemberList.setName("jGroupMemberList"); // NOI18N
        jListScrollPane.setViewportView(jGroupMemberList);

        jMembesrPanel.add(jListScrollPane, java.awt.BorderLayout.CENTER);

        jCheckAll.setForeground(java.awt.Color.blue);
        jCheckAll.setText("Check All/Uncheck All");
        jCheckAll.setName("jCheckAll"); // NOI18N
        jMembesrPanel.add(jCheckAll, java.awt.BorderLayout.PAGE_START);

        jSuperParentSplitPane.setRightComponent(jMembesrPanel);

        add(jSuperParentSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jSortingAZActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSortingAZActionPerformed
        sortMessagingBoardTabButtonsInEDT(true);
    }//GEN-LAST:event_jSortingAZActionPerformed

    private void jSortingZAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSortingZAActionPerformed
        sortMessagingBoardTabButtonsInEDT(false);
    }//GEN-LAST:event_jSortingZAActionPerformed

    private void jCloseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCloseAllActionPerformed
        
        if (JOptionPane.showConfirmDialog(talker.getPointBoxFrame(),
                                          "Close all message tabs ?",
                                          talker.getKernel().getCompanyName(),
                                          JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            hideAllButtonTabComponents();
        }
    }//GEN-LAST:event_jCloseAllActionPerformed

    private void jSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSendActionPerformed
        String message = jMessagingEntry.getText();
        String broadCastMessage = jBroadcastMessagingEntry.getText();
        if((message != null) && (!message.isEmpty())){
            sendCurrentMessage();
        }else if ((broadCastMessage != null) && (!broadCastMessage.isEmpty())){
            broadcastCurrentMessage();
        }else{
            JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), "The instant message can not be empty!");
            return;
        }
    }//GEN-LAST:event_jSendActionPerformed
    
    @Override
    public void broadcastCurrentMessage() {
        if (SwingUtilities.isEventDispatchThread()){
            broadcastCurrentMessageHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    broadcastCurrentMessageHelper();
                }
            });
        }
    }
    
    private void broadcastCurrentMessageHelper(){
        String broadCastMessage = jBroadcastMessagingEntry.getText();
        if ((broadCastMessage != null) && (!broadCastMessage.isEmpty())){
            if (broadCastMessage.length() > PointBoxConsoleProperties.getSingleton().getMaxIntantMessageLength()){
                JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), "Your broadcasted message is too long. It cannot be more than 1000 characters.");
                return;
            }else{
                broadcastMessage(broadCastMessage); //broadcast message to current opened buddy tabs in frame, rather than group tabs.
                jBroadcastMessagingEntry.setText("");
            }
        }else{
            JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), "The instant message in Broadcast-To-All entry can not be empty!");
            return;
        }
    }
    
    public void sendCurrentMessage() {
        if (SwingUtilities.isEventDispatchThread()){
            sendCurrentMessageHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    sendCurrentMessageHelper();
                }
            });
        }
    }
    
    private void sendCurrentMessageHelper(){
        String message = jMessagingEntry.getText();
        if((message != null) && (!message.isEmpty())){
            if (message.length() > PointBoxConsoleProperties.getSingleton().getMaxIntantMessageLength()){
                JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), "Your instant message is too long. It cannot be more than 1000 characters.");
                return;
            }else{
                sendMessage(targetStateUniqueID, message);
                jMessagingEntry.setText("");
            }
            jBroadcastMessagingEntry.setText("");
        }else{
            JOptionPane.showMessageDialog(talker.getKernel().getPointBoxMainFrame(), "The instant message in data entry for the current buddy can not be empty!");
            return;
        }
    }
    
    private void jTemplateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTemplateActionPerformed
       talker.getFace().displayOptionPricerFrame();
    }//GEN-LAST:event_jTemplateActionPerformed
    
    private void jMessagingSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jMessagingSplitPanePropertyChange
        /*Disabled by zhijun. Please don't restore it. It may potatially crash the entire program
        if (evt.getPropertyName().equals("dividerLocation")) {
            if (isClosing){
                tabManager.setAllDividers((double) Math.rint(100*prop)/100);
                isClosing = false;
            }
            else
                tabManager.setAllDividers((double) Math.rint(100*(double) getDivider()/(getSplitPaneHeight()-getDividerSize()))/100);
        }*/
    }//GEN-LAST:event_jMessagingSplitPanePropertyChange

    private void jLookupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLookupActionPerformed
        lookupDialog.setVisible(true);
    }//GEN-LAST:event_jLookupActionPerformed

    void sendMessage(String stateUniqueID, String message){
        (new MessageSender(stateUniqueID, message, null)).execute();
    }
    
    protected void populateCheckTreeItemsSlowlyInEDT(){
    
    }
    
    protected void broadcastMessage(String message){
        ListModel model = jGroupMemberList.getModel();
        if (model.getSize() == 0){
            /**
             * todo-pitscast: This is a bad temp but acceptable solution.
             */
            populateCheckTreeItemsSlowlyInEDT();
            model = jGroupMemberList.getModel();
        }
        Object obj;
        for(int i=0;i<model.getSize();i++){
            obj = model.getElementAt(i);
            if (obj instanceof FloatingFrameCheckBuddyItem){
                FloatingFrameCheckBuddyItem item=(FloatingFrameCheckBuddyItem)obj;
                if(item.isSelected()&&item.getBuddyButtonTab().getLoginUser().getBuddyStatus().equals(BuddyStatus.Online)){
                    sendMessage(item.getBuddyButtonTab().getTabUniqueID(),message);
                }
            }
        }
    }
    
    /**
     * Currently, if users choose to "price it", it means price all the tabs but not only the current tab. 
     * However, it potentially can be changed for a specific tab
     * @param evt 
     */
    private void jShowPriceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jShowPriceItemStateChanged
        IMessageTabRecord record = talker.getKernel().getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(targetStateUniqueID);
        if (record != null){
            if (evt.getStateChange() == ItemEvent.SELECTED){
                record.setDisplayPrices(true);
                jRefreshPrice.setEnabled(true);
            }else if (evt.getStateChange() == ItemEvent.DESELECTED){
                record.setDisplayPrices(false);
                jRefreshPrice.setEnabled(false);
            }
            refreshPrice(targetStateUniqueID, true);
        }
    }//GEN-LAST:event_jShowPriceItemStateChanged

    private void jRefreshPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRefreshPriceActionPerformed
        refreshPrice(targetStateUniqueID, true);
    }//GEN-LAST:event_jRefreshPriceActionPerformed

    private void jPersistFrameBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPersistFrameBtnActionPerformed
        saveNewGroupWithPersistingFrameAction();
    }//GEN-LAST:event_jPersistFrameBtnActionPerformed
    
    void refreshPrice(final String targetStateUniqueID, final boolean forceRefreshing){  
        if ((!forceRefreshing) && (!jShowPrice.isSelected())){
            return;
        }      
        if (SwingUtilities.isEventDispatchThread()){
            refreshPriceHelper(targetStateUniqueID);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    refreshPriceHelper(targetStateUniqueID);
                }
            });
        }
    }
    private void refreshPriceHelper(final String targetStateUniqueID){
        IMessageTabRecord record = talker.getKernel().getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(targetStateUniqueID);
        if ((record != null) ){           
            final IMessagingBoardState state = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(targetStateUniqueID);
            if (state != null){
                List<IPbsysOptionQuote> quotes = state.getPublishedQuotesForRefresh();
                Collections.sort(quotes, new PbsysOptionQuoteComparator());
                state.cleanupHistoryDocument();
                for (IPbsysOptionQuote quote : quotes){
                    try {
                        state.insertMessageLine(quote, record);
                        if (state.getTabButtonUniqueID().equalsIgnoreCase(targetStateUniqueID)){
                            scrollToMessagingHistoryBottom(jMessagingHistory.getDocument());
                        }
                    } catch (BadLocationException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }//for
            }//if
        }//if
    
    }

    private void initializeBroadcastMessageEntry(IPbcTalker talker) {
        jBroadCastScrollPane = new javax.swing.JScrollPane();
        jBroadCastScrollPane.setName("jBroadCastScrollPane"); // NOI18N
        jBroadCastScrollPane.setPreferredSize(new java.awt.Dimension(252, 50));
        
        IPbconsoleImageSettings imageSettings = talker.getKernel().getPointBoxConsoleRuntime().getPbcImageSettings();
        try {
            jBroadcastMessagingEntry = new JavaTextPaneWithBackgroundImage(imageSettings.getImageURL(PbcImageFileName.BroadcastBackgroundPng));
        } catch (Exception ex) {
            Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
            jBroadcastMessagingEntry = new JTextPane();
        }
        if (jBroadcastMessagingEntry != null){
            jBroadcastMessagingEntry.setBackground(java.awt.Color.pink);
            jBroadcastMessagingEntry.setMinimumSize(new java.awt.Dimension(150, 50));
            jBroadcastMessagingEntry.setName("jBroadcastMessagingEntry"); // NOI18N
            jBroadcastMessagingEntry.setPreferredSize(new java.awt.Dimension(250, 20));
            jBroadcastMessagingEntry.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    jMessagingEntry.setText("");
                }
            });
            jBroadcastMessagingEntry.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        evt.consume();
                    }
                }
                @Override
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        if((evt.getModifiers() & KeyEvent.SHIFT_MASK) != 0){
                            try {
                                jBroadcastMessagingEntry.getDocument().insertString(jBroadcastMessagingEntry.getDocument().getLength(), "\n", null);
                            } catch (BadLocationException ex) {
                                Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                            }                
                        }else{
                            broadcastCurrentMessage();
                        }
                    }  
                }
            });
            jBroadcastMessagingEntry.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    showMessageTabPopupMenu(e, false, false, false);
                }
            });
            jBroadCastScrollPane.setViewportView(jBroadcastMessagingEntry);
            jParentSplitPane.setBottomComponent(jBroadCastScrollPane);
        }
    }

    /**
     * This can be customized
     * @return 
     */
    JTextPane createMessagingEntryControl() {
        return new JTextPane();
    }

    private void setMessagingHistoryDefaultStyledDocument(final Document doc) {
        if (SwingUtilities.isEventDispatchThread()){
            jMessagingHistory.setDocument(doc);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jMessagingHistory.setDocument(doc);
                }
            });
        }
    }
    
    void floatingFramePriceCheckboxEventHappened(final ItemEvent evt) {
        if (evt == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            jShowPriceItemStateChanged(evt);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    jShowPriceItemStateChanged(evt);
                }
            });
        }
    }

    IPbcKernel getKernel() {
        return talker.getKernel();
    }
      
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jBasePanel;
    private javax.swing.JPanel jButtonPanel;
    protected javax.swing.JCheckBox jCheckAll;
    private javax.swing.JButton jCloseAll;
    private javax.swing.JToolBar jCommandBar;
    private javax.swing.JToolBar jFunctionBar;
    protected javax.swing.JList jGroupMemberList;
    protected javax.swing.JScrollPane jListScrollPane;
    private javax.swing.JButton jLookup;
    protected javax.swing.JPanel jMembesrPanel;
    private javax.swing.JTextPane jMessagingHistory;
    private javax.swing.JScrollPane jMessagingHistoryScrollPane;
    private javax.swing.JSplitPane jMessagingSplitPane;
    protected javax.swing.JSplitPane jParentSplitPane;
    private javax.swing.JButton jPersistFrameBtn;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JButton jRefreshPrice;
    private javax.swing.JScrollPane jScrollButtonPane;
    private javax.swing.JButton jSend;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JCheckBox jShowPrice;
    private javax.swing.JButton jSortingAZ;
    private javax.swing.JButton jSortingZA;
    protected javax.swing.JSplitPane jSuperParentSplitPane;
    private javax.swing.JPanel jTabButtonPanel;
    private javax.swing.JLabel jTargetName;
    private javax.swing.JButton jTemplate;
    // End of variables declaration//GEN-END:variables

    /**
     * All the outgoing messages are sent through this class
     */
    class MessageSender extends SwingWorker<Boolean, Void>{
        private String stateID;
        private String message;
        private final ArrayList<IGatewayConnectorBuddy> pitsCastMembers;
        private String archiveDefaultMessage;

        MessageSender(String stateID, String message, ArrayList<IGatewayConnectorBuddy> pitsCastMembers) {
            this.stateID = stateID;
            /**
             * The following if-block is for weird "appending line separator issue for PBcast-master 
             * message entry". For some unknown reason, that message entry will append a line separator 
             * automatically at the end of message in that control.
             */
            if (DataGlobal.isNonEmptyNullString(message)){
                if (message.endsWith(NIOGlobal.lineSeparator())){
                    message = message.substring(0, (message.length() - NIOGlobal.lineSeparator().length()));
                }
            }
            this.message = message;
            if (pitsCastMembers == null){
                this.pitsCastMembers = new ArrayList<IGatewayConnectorBuddy>();
            }else{
                this.pitsCastMembers = pitsCastMembers;
            }
            archiveDefaultMessage = null;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            IMessagingBoardState targetState = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(stateID);
            if (targetState == null){
                return false;
            }else{
                if (targetState.isArchiveWarningMessageRequired()){
                    archiveDefaultMessage = talker.getArchiveWarningMessage();
                    targetState.setArchiveWarningMessageRequired(false);
                }else{
                    archiveDefaultMessage = null;
                }
                targetState.cleanupEntryDocument();
                
                Object obj = targetState.getStateObject();
                if (obj instanceof TargetBuddyPair){
                    sendMessageToBuddy((TargetBuddyPair)obj);
                }else if (obj instanceof IGatewayConnectorGroup){
                    if(MasterMessagingBoard.this instanceof FloatingDistGroupMessagingBoard){
                        //DIST
                        ArrayList<IGatewayConnectorBuddy> members = new ArrayList<IGatewayConnectorBuddy>();
                        for(int i=0;i<jGroupMemberList.getModel().getSize();i++){
                            FloatingFrameCheckBuddyItem item=(FloatingFrameCheckBuddyItem) jGroupMemberList.getModel().getElementAt(i);
                            if(item.isSelected()&&item.getBuddy().getLoginOwner().getBuddyStatus().equals(BuddyStatus.Online)){
                                members.add(item.getBuddy());
                            }                        
                        }
                        sendMessageToGroup((IGatewayConnectorGroup)obj, members, targetStateUniqueID);
                    }else if(MasterMessagingBoard.this instanceof FloatingPitsCastGroupMessagingBoard){
                        //PitsCast
                        sendMessageToGroup((IGatewayConnectorGroup)obj, pitsCastMembers, ((IGatewayConnectorGroup)obj).getIMUniqueName());
                    }else{
                        //PITS
                        sendMessageToGroup((IGatewayConnectorGroup)obj, targetState.getGroupMembers(), targetStateUniqueID);
                    }
                }else{
                    if (obj != null){
                        String errMsg = "[TECH ERROR] Messaging board target could be either IGatewayConnectorGroup or targetPair. "
                                + "It cannot be other types.";
                        PointBoxTracer.recordSevereException(logger, new Exception(errMsg));
                    }
                }
                return true;
            }
        }

        private void sendMessageToBuddy(TargetBuddyPair targetPair) {
            IGatewayConnectorBuddy loginUser = targetPair.getLoginUser();
            if (targetPair != null){
                if(loginUser.getBuddyStatus().equals(BuddyStatus.Online)){
                    if (archiveDefaultMessage != null){
                        talker.sendMessageToBuddy(loginUser, targetPair.getBuddy(), archiveDefaultMessage, null,true);
                    }
                    talker.sendMessageToBuddy(loginUser, targetPair.getBuddy(), message, null,true);
                }else{
//                     showMessageInEDT(getTalker().getPointBoxFrame(), 
//                        "You need to log into " + loginUser.getIMScreenName() + " of " + loginUser.getIMServerType() + " before this operation.");
                }
            }
        }

        //todo: zzj - how to send message for a target group
        private void sendMessageToGroup(final IGatewayConnectorGroup targetGroup, 
                                        final ArrayList<IGatewayConnectorBuddy> members, 
                                        String publishStateUniqueID) 
        {
            if ((message != null) && (!message.trim().isEmpty())){
                if (!members.isEmpty()){
//                    talker.distributeInstantMessages(message.trim(), targetGroup, members);
                    //todo: after broadcast, it should be published onto the board if it is the target state
                    //todo: it also should be piblished onto the Master Board and buddy floating board
                    for(IGatewayConnectorBuddy buddy : members){
                        if (archiveDefaultMessage != null){
                            talker.sendMessageToBuddy(buddy.getLoginOwner(), buddy, archiveDefaultMessage, null, true);
                        }
                        talker.sendMessageToBuddy(buddy.getLoginOwner(), buddy, message, null, true); //in here, don't send message and just show it.
                    }
                    //insert text on to the board
                    IGatewayConnectorBuddy member;
                    int total = members.size();
                    String offlineText = "";
                    String onlineText = "";
                    for (int index = 0; index < total; index++){
                        member = members.get(index);
                        if (BuddyStatus.Online.equals(member.getBuddyStatus())){
                            onlineText += member.getIMScreenName() + ", ";
                        }else{
                            offlineText += member.getIMScreenName() + ", ";
                        }
                    }//for
                    if (DataGlobal.isNonEmptyNullString(onlineText)){
                        onlineText = onlineText.substring(0, onlineText.length() - 2);
                    }
                    if (DataGlobal.isNonEmptyNullString(offlineText)){
                        offlineText = offlineText.substring(0, offlineText.length() - 2);
                    }
                    try {
                        IMessagingBoardState state = MessagingPaneManager.getSingleton(talker).getMessagingBoardState(publishStateUniqueID);
                        state.recordPublishedGroupMessage(talker.getPointBoxLoginUser(), targetGroup, message);
                        if (archiveDefaultMessage != null){
                            insertGroupMessage(jMessagingHistory.getDocument(), archiveDefaultMessage, targetGroup, offlineText, onlineText);
                        }
                        insertGroupMessage(jMessagingHistory.getDocument(), message, targetGroup, offlineText, onlineText);
                        
                    } catch (BadLocationException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    
        /**
         * it is always "outgoing"
         * @param textDoc
         * @param message
         * @param targetGroup
         * @param offlineText
         * @param onlineText
         * @throws BadLocationException 
         */
        private void insertGroupMessage(Document textDoc, String message, IGatewayConnectorGroup targetGroup, String offlineText, String onlineText) throws BadLocationException{

            textDoc.insertString(textDoc.getLength(),
                                getKernel().getPointBoxLoginUser().getIMScreenName() + 
                                " [" + CalendarGlobal.convertToHHmmss(new GregorianCalendar(), ":") + "] ",
                                 SwingGlobal.BOLD_BLACK_LOCAL_USER);
            textDoc.insertString(textDoc.getLength(),
                                 message,
                                 SwingGlobal.BLACK_MESSAGE);
            textDoc.insertString(textDoc.getLength(),
                                 NIOGlobal.lineSeparator(),
                                 SwingGlobal.BLACK_MESSAGE);
        }

        @Override
        protected void done() {
            //switchToTargetState(targetStateUniqueID);
            jMessagingHistory.invalidate();
            jMessagingHistory.updateUI();
            scrollToMessagingHistoryBottom(jMessagingHistory.getDocument()); //To change body of generated methods, choose Tools | Templates.
        }
    }

    /**
     * Update buddy status of relevant items
     * @param loginUser
     * @param buddy
     * @param talker 
     */
    void updateBuddyStatus(final IGatewayConnectorBuddy loginUser, final IGatewayConnectorBuddy buddy, final IPbcTalker talker) {
        (new SwingWorker<ListModel, Void>(){
            @Override
            protected ListModel doInBackground() throws Exception {
                ListModel model = jGroupMemberList.getModel();
                DefaultListModel aNewModel = new DefaultListModel();
                IGatewayConnectorBuddy aBuddy;
                FloatingFrameCheckBuddyItem aBuddyItem;
                FloatingFrameCheckBuddyItem aNewBuddyItem;
                Object obj;
                for (int i = 0; i < model.getSize(); i++){
                    obj = model.getElementAt(i);
                    if (obj instanceof FloatingFrameCheckBuddyItem){
                        aBuddyItem = (FloatingFrameCheckBuddyItem)obj;
                        aBuddy = aBuddyItem.getBuddy();
                        if (aBuddy.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())){
                            aNewBuddyItem = FloatingFrameCheckBuddyItem.createNewInstance(buddy, aBuddyItem.getBuddyButtonTab());
                            aNewBuddyItem.setSelected(aNewBuddyItem.isSelected());
                            aNewModel.addElement(aNewBuddyItem);
                        }else{
                            aNewModel.addElement(aBuddyItem);
                        }
                    }else{
                        aNewModel.addElement(obj);
                    }
                }
                return aNewModel;
            }

            @Override
            protected void done() {
                try {
                    jGroupMemberList.setModel(get());
                } catch (InterruptedException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MasterMessagingBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).execute();
    }
    
    void renameGroupButtonTabComponent(IGatewayConnectorGroup oldGroup, String newGroupName) {
        if ((oldGroup == null) || (DataGlobal.isEmptyNullString(newGroupName))){
            return;
        }
        GroupButtonTabComponent aGroupButtonTabComponent = this.getVisibleGroupButtonTabComponent(oldGroup);
        if (aGroupButtonTabComponent != null){
            aGroupButtonTabComponent.updateGroupName(newGroupName);
        }
    }
    private GroupButtonTabComponent getVisibleGroupButtonTabComponent(IGatewayConnectorGroup group) {
        if (group == null){
            return null;
        }
        synchronized(tabButtonRowPanels){
            GroupButtonTabComponent tabButton;
            Component[] components;
            for (int j = 0; j < tabButtonRowPanels.size(); j++){
                components = tabButtonRowPanels.get(j).getComponents();
                if (components != null){
                    for (int k = 0; k < components.length; k++){
                        if (components[k] instanceof GroupButtonTabComponent){
                            tabButton = (GroupButtonTabComponent)(components[k]);
                            tabButton.setRowIndex(j);
                            if (tabButton.getGroup().getGroupName().equalsIgnoreCase(group.getGroupName())){
                                return tabButton;
                            }
                        }
                    }
                }//if
            }//for
        }//synchronized(tabButtonRowPanels){
        return null;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IFloatingMessagingBoardListener;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.talker.IPointBoxTalkerComponent;
import com.eclipsemarkets.pbc.face.talker.dist.PbcFloatingFrameTerms;
import com.eclipsemarkets.pbc.kernel.GeneralFloatingFrameSettings;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang
 */
class FloatingMessagingFrame extends JFrame implements IFloatingMessagingBoardListener, IPointBoxTalkerComponent {

    private static final Logger logger;
    static {
        logger = Logger.getLogger(FloatingMessagingFrame.class.getName());
    }

    private FloatingMessagingBoard board;
//    private final String floatingFrameSizeKey;
//    private String floatingFrameLocationKey;
    
    private ComponentAdapter frameComponentListener;
    
    private final MessagingPaneManagerImpl aMessagingPaneManagerImpl;
    
    FloatingMessagingFrame(FloatingMessagingBoard board, 
                           MessagingPaneManagerImpl aMessagingPaneManagerImpl,
                           String frameName) {
        this.board = board;
        this.aMessagingPaneManagerImpl = aMessagingPaneManagerImpl;
        frameComponentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
//                Dimension d = FloatingMessagingFrame.this.getSize();
//                PointBoxConsoleProperties.getSingleton().setProperty(floatingFrameSizeKey, d.getWidth()+"@"+d.getHeight());
                storeOpenedFloatingFrameSettings(true);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
//                Point p = FloatingMessagingFrame.this.getLocation();
//                PointBoxConsoleProperties.getSingleton().setProperty(floatingFrameLocationKey, p.getX()+"@"+p.getY());
                storeOpenedFloatingFrameSettings(true);
            }
        };

        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosed(WindowEvent e) {
                FloatingMessagingFrame.this.aMessagingPaneManagerImpl.removeFloatingMessagingFrame(FloatingMessagingFrame.this.board.getBoardId());
            }
        });
        
        this.addComponentListener(frameComponentListener);
        
        getContentPane().add(board, BorderLayout.CENTER);
        if (board instanceof FloatingPitsMessagingBoard){
            if(DataGlobal.isEmptyNullString(frameName)){
                setTitle(PbcReservedTerms.DefaultPitsLikeFrameTitle.toString());
            }else{
                if (!(PbcReservedTerms.DefaultPitsLikeFrameTitle.toString().equalsIgnoreCase(frameName))){
                    board.makePersistFrameButtonInvisible();
                }
                setTitle(frameName);
            }
        }else if(board instanceof FloatingPitsCastGroupMessagingBoard){
            setTitle(PbcFloatingFrameTerms.PitsCastFrame.toString());
        }else if(board instanceof FloatingDistGroupMessagingBoard){
                setTitle(PbcFloatingFrameTerms.DistributionFrame.toString());
        }else{
            setTitle(board.getBoardId());
        }
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        this.setMinimumSize(new Dimension(450,360));
        
        pack();
        
        GeneralFloatingFrameSettings frameSettings = null;
        if (board instanceof FloatingDistGroupMessagingBoard){
            frameSettings = PointBoxConsoleProperties.getSingleton().retrieveOpenedDistributionFloatingFrameSettings(
                    board.getKernel().getPointBoxLoginUser().getIMUniqueName());
        }else if (board instanceof FloatingPitsCastGroupMessagingBoard){
            frameSettings = PointBoxConsoleProperties.getSingleton().retrieveOpenedPitsCastFloatingFrameSettings(
                    board.getKernel().getPointBoxLoginUser().getIMUniqueName());
        }else if (board instanceof FloatingPitsMessagingBoard){
            frameSettings = PointBoxConsoleProperties.getSingleton().retrieveOpenedPitsFloatingFrameSettings(
                    board.getKernel().getPointBoxLoginUser().getIMUniqueName(), this.getTitle());
        }else{
            
        }
        Dimension defaultDimension = new Dimension(800, 600);
        if (frameSettings == null) {
            setLocation(SwingGlobal.getCenterPointOfParentWindow(board.getTalker().getPointBoxFrame(), this));
            setSize(defaultDimension);
        }else{
            if ((frameSettings.getLocation() != null) && (frameSettings.getSize() != null)){
                setLocation(frameSettings.getLocation());
                setSize(frameSettings.getSize());
            }else{
                setLocation(SwingGlobal.getCenterPointOfParentWindow(board.getTalker().getPointBoxFrame(), this));
                setSize(defaultDimension);
            }
        }
        //board.oneClickForDefaultShow();
    }

    @Override
    public void release() {
        if (SwingUtilities.isEventDispatchThread()){
            dispose();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    dispose();
                }
            });
        }
    }
    
    private void storeOpenedFloatingFrameSettings(boolean visible){
        if (board instanceof FloatingDistGroupMessagingBoard){
            FloatingDistGroupMessagingBoard aFloatingDistGroupMessagingBoard = (FloatingDistGroupMessagingBoard)board;
            PointBoxConsoleProperties.getSingleton().storeOpenedDistributionFloatingFrame(board.getKernel().getPointBoxLoginUser().getIMUniqueName(),
                                                                                        aFloatingDistGroupMessagingBoard.getAllVisibleGroupNameList(),
                                                                                        getLocation(), getSize(), visible);
        }else if (board instanceof FloatingPitsCastGroupMessagingBoard){
            FloatingPitsCastGroupMessagingBoard aFloatingDistGroupMessagingBoard = (FloatingPitsCastGroupMessagingBoard)board;
            PointBoxConsoleProperties.getSingleton().storeOpenedPitsCastFloatingFrame(board.getKernel().getPointBoxLoginUser().getIMUniqueName(),
                                                                                        aFloatingDistGroupMessagingBoard.getAllVisibleGroupNameList(),
                                                                                        getLocation(), getSize(), visible);
        }else if (board instanceof FloatingPitsMessagingBoard){
            PointBoxConsoleProperties.getSingleton().storeOpenedPitsFloatingFrame(board.getKernel().getPointBoxLoginUser().getIMUniqueName(),
                                                                                this.getTitle(),
                                                                                getLocation(), getSize(), visible);
        }
    }

    @Override
    public void setVisible(boolean visibleValue) {
        board.removeFloatingMessagingBoardListener(this);
        board.addFloatingMessagingBoardListener(this);
        super.setVisible(visibleValue);
        
        storeOpenedFloatingFrameSettings(visibleValue);
        
        if(!visibleValue){
            resumeTabsonMasterboard();
        }
    }
    
    private void resumeTabsonMasterboard(){
        if(SwingUtilities.isEventDispatchThread()){
            resumeTabsonMasterboardHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    resumeTabsonMasterboardHelper();
                }
            });
        }
    }
    
    private void resumeTabsonMasterboardHelper(){
        IMasterMessagingBoard masterboard = null;
        try {
            masterboard = MessagingPaneManager.getSingleton().getMasterMessagingBoard();
        } catch (Exception ex) {
            Logger.getLogger(FloatingMessagingFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (masterboard == null){
            return;
        }
        ArrayList<IButtonTabComponent> tabs = getFloatingMessagingBoard().getAllVisibleTabeButtons();
        for(IButtonTabComponent tab : tabs){
                for(IButtonTabComponent mTabs:masterboard.getAllVisibleTabeButtons()){
                    if(mTabs.getTabUniqueID().equals(tab.getTabUniqueID())){
                        mTabs.lostTargetInEDT();
                    }
                }     
        }        
    }

    FloatingMessagingBoard getFloatingMessagingBoard() {
        return board;
    }

//    @Override
//    public void newGroupSaved(final IGatewayConnectorGroup group, 
//                              final ArrayList<IGatewayConnectorBuddy> members) 
//    {
//        if ((group == null) || (members == null) || (members.isEmpty())){
//            return;
//        }
//        if (SwingUtilities.isEventDispatchThread()){
//            setTitle(group.getGroupName());
//        }else{
//            SwingUtilities.invokeLater(new Runnable(){
//                @Override
//                public void run() {
//                    setTitle(group.getGroupName());
//                }
//            });
//        }
//    }

    /**
     * Update buddy status of relevant items
     * @param loginUser
     * @param buddy
     * @param talker 
     */
    void updateBuddyStatus(IGatewayConnectorBuddy loginUser, IGatewayConnectorBuddy buddy, IPbcTalker talker) {
        board.updateBuddyStatus(loginUser, buddy, talker);
    }

    /**
     * 
     * @param group
     * @param members 
     */
    void updateDistributionGroupMembers(IGatewayConnectorGroup group, ArrayList<IGatewayConnectorBuddy> members) {
        board.updateDistributionGroupMembers(group, members);
    }

    void copyBoradcastMessageToBoard(String message, boolean autoSend) {
        board.insertBroadcastMessage(message);
        if (autoSend){
            board.broadcastCurrentMessage();
        }
    }

    /**
     * @param oldGroup
     * @param newGroupName 
     */
    void renameGroupInFloatingFrame(IGatewayConnectorGroup oldGroup, String newGroupName) {
        board.renameGroupInFloatingFrame(oldGroup, newGroupName);
    }
}

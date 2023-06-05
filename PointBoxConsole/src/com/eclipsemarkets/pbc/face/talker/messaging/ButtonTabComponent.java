/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import com.eclipsemarkets.pbc.face.talker.IFloatingPitsMessagingBoard;
import com.eclipsemarkets.pbc.face.talker.IMasterMessagingBoard;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author Zhijun Zhang
 */
public abstract class ButtonTabComponent extends JPanel implements IButtonTabComponent{

    private static final Logger logger;
    public static final String DistributionGroup;
    static {
        logger = Logger.getLogger(ButtonTabComponent.class.getName());
        DistributionGroup="[Distribution Group] ";
    }

    private IMasterMessagingBoard board;
    //Read-Only
    //private String boardUuid;
    private final String tabUniqueID;
    private int rowIndex;   //in jButtonPanel
    private final JButton closingButton;
    private final MasterMessagingBoardTabTextLabel tabTextLabel;
    private final GatewayServerType serverType;
    private Color originalTabButtonBgColor;
    private Color originalTabButtonFgColor;
    private ImageIcon icon;
    
    private IMasterMessagingBoard masterboard;

    /**
     * @param messagingBorad
     * @param buttonText
     * @param icon
     * @param tabUniqueID - if this is NULL, it is used as SAMPLE. It could be group's unique id or TargetBuddyPair's uniqueID
     */
    ButtonTabComponent(final IMasterMessagingBoard board,
                       final String buttonText, 
                       final ImageIcon icon, 
                       final String tabUniqueID,
                       final GatewayServerType serverType,
                       final IMasterMessagingBoard masterboard) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.board = board;
        this.masterboard=masterboard;
        this.tabUniqueID = tabUniqueID;
        this.icon = icon;
        rowIndex = -1;
        this.serverType = serverType;
        setOpaque(false);

        //make JLabel read titles from JTabbedPane
        tabTextLabel = new MasterMessagingBoardTabTextLabel(tabUniqueID, buttonText);
        tabTextLabel.setForeground(Color.black);
        tabTextLabel.setIcon(icon);
        tabTextLabel.setIconTextGap(2);
        tabTextLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        tabTextLabel.setHorizontalAlignment(SwingConstants.LEADING);
        tabTextLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        tabTextLabel.addMouseListener(new ButtonTabMouseListener(this));
        tabTextLabel.addMouseMotionListener(new DndButtonTabMouseMotionListener());

        add(tabTextLabel);
        //add more space between the label and the button
        if (DataGlobal.isEmptyNullString(tabUniqueID) && (buttonText.equalsIgnoreCase(MasterMessagingBoard.POINT_BOX))){
            tabTextLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
            closingButton = null;
        }else{
            tabTextLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            //tab button
            closingButton = new TabClosingButton(this);
            if((!(board.getBoardId().equals(tabUniqueID)||(DistributionGroup+board.getBoardId()).equals(tabUniqueID))) || !(board instanceof FloatingMessagingBoard)){
                add(closingButton);
            }else{
                tabTextLabel.setText(buttonText+"     ");
            }
        }
        //add more space to the top of the component
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        originalTabButtonBgColor = getBackground();
        originalTabButtonFgColor = getForeground();

        tabTextLabel.setTransferHandler(new BuddyMessagingBoardDragHandler());
    }

    public void setIcon(ImageIcon icon){
        this.icon=icon;
        tabTextLabel.setIcon(icon);
    }
    
    public ImageIcon getIcon() {
        return icon;
    }

    @Override
    public IMasterMessagingBoard getBoard() {
        return board;
    }

    @Override
    public void setBoard(IMasterMessagingBoard board) {
        this.board = board;
    }

    /**
     * Buddy owned this tab
     * @return 
     */
    public abstract IGatewayConnectorBuddy getBuddy();
    
    /**
     * Group owned this tab
     * @return 
     */
    public abstract IGatewayConnectorGroup getGroup();

    @Override
    public String toString() {
        if (serverType == null){
            return tabTextLabel.getText();
        }else{
            return "[" + serverType + "] " + tabTextLabel.getText();
        }
    }

    @Override
    public GatewayServerType getServerType() {
        return serverType;
    }

    @Override
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (tabTextLabel != null){
            tabTextLabel.setForeground(fg);
        }
    }

    /**
     * 
     * @return - never NULL
     */
    @Override
    public String getTabUniqueID() {
        if (tabUniqueID == null){
            return " ";
        }
        return tabUniqueID;
    }

    @Override
    public void paintClosingButtonBorder(final boolean value) {
        if (SwingUtilities.isEventDispatchThread()){
            if (closingButton != null){
                closingButton.setBorderPainted(value);
            }
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    if (closingButton != null){
                        closingButton.setBorderPainted(value);
                    }
                }
            });
        }
    }

    /**
     * After the state lost its target position, look-and-feel will be changed correspondingly
     */
    @Override
    public void lostTargetInEDT() {
        if (SwingUtilities.isEventDispatchThread()){
            lostTargetHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    lostTargetHelper();
                }
            });
        }
    }
    private void lostTargetHelper(){
        setOpaque(false);
        setBackground(originalTabButtonBgColor);
        setForeground(originalTabButtonFgColor);
    }

    /**
     * if the state becomes target, look-and-feel will be changed correspondingly
     */
    @Override
    public void winTargetInEDT(){
        if (SwingUtilities.isEventDispatchThread()){
            winTargetHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    winTargetHelper();
                }
            });
        }
    }
    private void winTargetHelper(){
        if (DataGlobal.isNonEmptyNullString(getTabUniqueID())){
            setOpaque(true);
            setBackground(Color.yellow);
            setForeground(Color.red);
        }
    }

    String getDistGroupName() {
        String result = getTabUniqueID();
        if (DataGlobal.isNonEmptyNullString(result)){
            result = result.replace(DistributionGroup, "").trim();
        }
        return result;
    }

    void makeClosable(final boolean value) {
        if (SwingUtilities.isEventDispatchThread()){
            makeClosableHelp(value);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    makeClosableHelp(value);
                }
            });
        }
    }

    private void makeClosableHelp(boolean value) {
        if (closingButton != null){
            closingButton.setVisible(value);
        }
    }

    private class TabClosingButton extends JButton {

        public TabClosingButton(final ButtonTabComponent tabComponent) {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(new ButtonTabMouseListener(tabComponent));
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (board.hasVisibleTabButton(tabComponent)){
                            board.hideButtonTabComponent(tabComponent);
                            if ((board instanceof FloatingMessagingBoard)){
                                if (!board.hasVisibleTabButtons()){
                                    board.getTalker().getMessagingPaneManager().removeFloatingMessagingFrame(board.getBoardId());
                                }
                                resumeTabsOnMasterboardHelper();
                                if (board instanceof IFloatingPitsMessagingBoard){
                                    ((IFloatingPitsMessagingBoard)board).handlePitsBuddyTabClosed(getBuddy());
                                }
                            }else{
                                board.getTalker().getMessagingPaneManager().announceTabClosingEvent(tabUniqueID);
                            }
                        }
                    } catch (Exception ex) {
                        PointBoxTracer.recordSevereException(logger, ex);
                    }
                }
                private void resumeTabsOnMasterboardHelper(){
                    if (masterboard instanceof MasterMessagingBoard){
                        ArrayList<IButtonTabComponent> aButtonTabComponentList = ((MasterMessagingBoard)masterboard).getAllVisibleTabeButtons();
                        if (aButtonTabComponentList != null){
                            for(IButtonTabComponent mTabs : aButtonTabComponentList){
                                if(mTabs.getTabUniqueID().equals(tabComponent.getTabUniqueID())){
                                    mTabs.lostTargetInEDT();
                                    break;
                                }
                            }//for
                        }
                    }
                }
            });
        }
        
        //we don't want to update UI for this button
        @Override
        public void updateUI() {
        }

        //paint the cross
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }//TabClosingButton

    /**
     * Response to clicking on button-tab
     */
    private class ButtonTabMouseListener implements MouseListener {

        ButtonTabComponent tabComponent;

        public ButtonTabMouseListener(ButtonTabComponent tabComponent) {
            this.tabComponent = tabComponent;
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                ((AbstractButton)component).setBorderPainted(true);
            }else if (component instanceof JLabel) {
                component.setForeground(Color.red);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof JLabel) {
                try {
                    if (board.hasVisibleTabButton(tabComponent)){
                        TabButtonFlashingAgent.getSingleton(board).stopFlashingTab(tabUniqueID);
                        board.tabButtonClicked(tabComponent);
                    }
                } catch (Exception ex) {
                    PointBoxTracer.recordSevereException(logger, ex);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                ((AbstractButton)component).setBorderPainted(false);
            }else if (component instanceof JLabel) {
                component.setForeground(Color.black);
            }
        }
    }//ButtonTabMouseListener

    private class DndButtonTabMouseMotionListener implements MouseMotionListener {

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Object obj = e.getSource();
            if (obj instanceof MasterMessagingBoardTabTextLabel){
                TransferHandler handler = ((MasterMessagingBoardTabTextLabel)obj).getTransferHandler();
                if (handler != null){
                    handler.exportAsDrag((MasterMessagingBoardTabTextLabel)obj, e, TransferHandler.MOVE);
                }
            }
        }

    }//DndButtonTabMouseListener

    void updateTabTextLabelValue(final String tabText) {
        if (DataGlobal.isEmptyNullString(tabText)){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            tabTextLabel.setText(tabText);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    tabTextLabel.setText(tabText);
                }
            });
        }
    }

}//ButtonTabComponent

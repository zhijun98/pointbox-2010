/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.CopyCutPastePopupMenu;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pbc.runtime.settings.record.IMessageTabRecord;
import com.l2fprod.common.swing.JFontChooser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * MessageTabPopupMenu.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Aug 28, 2010, 9:32:45 PM
 */
class MessageTabPopupMenu extends CopyCutPastePopupMenu {
    private static final long serialVersionUID = 1L;
    private JCheckBoxMenuItem timestampMenuItem;
    //private JCheckBoxMenuItem pricingMenuItem;
    private JMenuItem myFontMenuItem;
    private JMenuItem myColorMenuItem;
    private JMenuItem buddyFontMenuItem;
    private JMenuItem buddyColorMenuItem;
    private JMenuItem setTitleMenuItem;
    private JMenu copyToMenu;
    private JMenu sendToMenu;
    private String boardId;

    private IPbcKernel kernel;
    private String messageTabID;

    /**
     * 
     * @param kernel
     * @param messageTabID - identify who it is working for
     * @param boardId  - reserved for the future usage
     */
    MessageTabPopupMenu(IPbcKernel kernel, String messageTabID, String boardId) {
        super();
        if (messageTabID == null){
            kernel = null;
        }
        this.kernel = kernel;
        this.messageTabID = messageTabID;
        this.boardId=boardId;

        initializeLocalPopupMenu();
    }

    public JMenuItem getCopyToPitsCast() {
        JMenuItem copyToPitsCast = new JMenuItem();
        copyToPitsCast.setText("Copy to PBcast");
        copyToPitsCast.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                doClickCopyMenuItem();
                try {
                    Object obj = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    if (obj instanceof String){
                        selectedText = DataGlobal.nullize(obj.toString());
                    }
                } catch (UnsupportedFlavorException ex) {
                    selectedText = null;
                } catch (IOException ex) {
                    selectedText = null;
                }

                String message = selectedText;
                if (DataGlobal.isNonEmptyNullString(message)){
                    kernel.getPointBoxFace().getPointBoxTalker().displayPitsCastMessageFrameForCopyPasteQuoteMessage(message);
                }else{
                    JOptionPane.showMessageDialog(kernel.getPointBoxMainFrame(), "Please click on a valid option quote which contains instant messages.");
                }
            }
        });
        //add(copyToPitsCast);
        return copyToPitsCast;
    }

    public JMenuItem getSendByPitsCast() {
        JMenuItem sendByPitsCast = new JMenuItem();
        sendByPitsCast.setText("Send by PBcast");
        sendByPitsCast.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                doClickCopyMenuItem();
                try {
                    Object obj = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    if (obj instanceof String){
                        selectedText = DataGlobal.nullize(obj.toString());
                    }
                } catch (UnsupportedFlavorException ex) {
                    selectedText = null;
                } catch (IOException ex) {
                    selectedText = null;
                }

                String message = selectedText;
                if (DataGlobal.isNonEmptyNullString(message)){
                    kernel.getPointBoxFace().getPointBoxTalker().displayPitsCastMessageFrameForSendQuoteMessage(message);
                }else{
                    JOptionPane.showMessageDialog(kernel.getPointBoxMainFrame(), "Please click on a valid option quote which contains instant messages.");
                }
            }
        });
        //add(sendByPitsCast);
        return sendByPitsCast;
    }
    
    private IMessageTabRecord getMessageTabRecord(){
        if(kernel!=null){
            return kernel.getPointBoxConsoleRuntime().getPointBoxTalkerSettings().getMessageTabRecord(messageTabID);
        }else{
            return null;
        }
    }
    
    private String selectedText = null;
    private void loadCopyAndSendToMenu(final JMenu toMenu, final boolean isCopyTo) {
        final IPbcTalker talker = kernel.getPointBoxFace().getPointBoxTalker();
        ArrayList<IGatewayConnectorGroup> groupList = talker.getPitsCastGroups();
        JMenuItem aGroupMenuItem;
        if ((groupList == null) || (groupList.isEmpty())){
            aGroupMenuItem = new JMenuItem();
            aGroupMenuItem.setText("No PBcast Group yet...");
            toMenu.add(aGroupMenuItem);
        }else{
            for (IGatewayConnectorGroup group : groupList){
                aGroupMenuItem = new JMenuItem();
                aGroupMenuItem.setText(group.getGroupName());
                final IGatewayConnectorGroup sendToGroup = group;
                aGroupMenuItem.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doClickCopyMenuItem();
                        try {
                            Object obj = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                            if (obj instanceof String){
                                selectedText = DataGlobal.nullize(obj.toString());
                            }
                        } catch (UnsupportedFlavorException ex) {
                            selectedText = null;
                        } catch (IOException ex) {
                            selectedText = null;
                        }
                        
                        String message = selectedText;
                        if (isCopyTo){
                            talker.displayPitsCastMessageBoardForCopyTo(message, sendToGroup);
                        }else{
                            talker.displayPitsCastMessageBoardForSendTo(message, sendToGroup);
                        }
                    }
                });
                toMenu.add(aGroupMenuItem);
            }
        }
    }

    final void initializeLocalPopupMenu() {
        if (getMessageTabRecord() != null){
            timestampMenuItem = new JCheckBoxMenuItem();
            timestampMenuItem.setText("Display Timestamp");
            if (getMessageTabRecord().isDisplayTimestamp()){
                timestampMenuItem.setState(true);
            }else{
                timestampMenuItem.setState(false);
            }
            timestampMenuItem.addItemListener(new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED){
                        if (getMessageTabRecord().isDisplayTimestamp()){
                            getMessageTabRecord().setDisplayTimestamp(false);
//                            talkerSettings.fireSettingsUpdated(null, new SettingsEvent(getMessageTabRecord()));
                        }
                    }else if (e.getStateChange() == ItemEvent.SELECTED){
                        if (!getMessageTabRecord().isDisplayTimestamp()){
                            getMessageTabRecord().setDisplayTimestamp(true);
//                            talkerSettings.fireSettingsUpdated(null, new SettingsEvent(getMessageTabRecord()));
                        }
                    }
                }
            });
            add(timestampMenuItem);
            
            copyToMenu = new JMenu();
            copyToMenu.setText("Copy to");
            loadCopyAndSendToMenu(copyToMenu, true);
            copyToMenu.add(this.getCopyToPitsCast(), 0);
            add(copyToMenu);
            
            sendToMenu = new JMenu();
            sendToMenu.setText("Send to");
            loadCopyAndSendToMenu(sendToMenu, false);
            sendToMenu.add(this.getSendByPitsCast(), 0);
            add(sendToMenu);

            myFontMenuItem = new JMenuItem();
            myFontMenuItem.setText("My Font");
            myFontMenuItem.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    Font font = JFontChooser.showDialog(myFontMenuItem,
                                                        "Font Chooser",
                                                        getMessageTabRecord().getMyFont());
                    if (font == null){
                        return;
                    }
                    if (!getMessageTabRecord().getMyFont().equals(font)){
                        IMessageTabRecord tab = getMessageTabRecord();
                        tab.setMyFont(font);
                        IPbcTalker talker = kernel.getPointBoxFace().getPointBoxTalker();
                        talker.getMessagingPaneManager().getMasterMessagingBoard().refreshTextStyle(messageTabID, tab);
//                        talkerSettings.fireSettingsUpdated(null, new SettingsEvent(getMessageTabRecord()));
                    }
                }
            });
            add(myFontMenuItem);

            myColorMenuItem = new JMenuItem();
            myColorMenuItem.setText("My Color");
            myColorMenuItem.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color color = JColorChooser.showDialog(myColorMenuItem,
                                    "Color chooser", getMessageTabRecord().getMyForeground());
                    if (color == null){
                        return;
                    }
                    if (!getMessageTabRecord().getMyForeground().equals(color)){
                        IMessageTabRecord tab = getMessageTabRecord();
                        tab.setMyForeground(color);
                        IPbcTalker talker = kernel.getPointBoxFace().getPointBoxTalker();
                        talker.getMessagingPaneManager().getMasterMessagingBoard().refreshTextStyle(messageTabID, tab);
//                        talkerSettings.fireSettingsUpdated(null, new SettingsEvent(getMessageTabRecord()));
                    }
                }
            });
            add(myColorMenuItem);

            String menuItemText = getMessageTabRecord().getMessageTabID();
            menuItemText = menuItemText.substring(menuItemText.indexOf(']')+ 1, menuItemText.length());
            menuItemText = menuItemText.trim();
            buddyFontMenuItem = new JMenuItem();
            buddyFontMenuItem.setText(menuItemText + "'s Font");
            buddyFontMenuItem.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    Font font = JFontChooser.showDialog(buddyFontMenuItem,
                                                        "Font Chooser",
                                                        getMessageTabRecord().getBuddyFont());
                    if (font == null){
                        return;
                    }
                    if (!getMessageTabRecord().getBuddyFont().equals(font)){
                        IMessageTabRecord tab = getMessageTabRecord();
                        tab.setBuddyFont(font);
                        IPbcTalker talker = kernel.getPointBoxFace().getPointBoxTalker();
                        talker.getMessagingPaneManager().getMasterMessagingBoard().refreshTextStyle(messageTabID, tab);
//                        talkerSettings.fireSettingsUpdated(null, new SettingsEvent(getMessageTabRecord()));
                    }
                }
            });
            add(buddyFontMenuItem);

            buddyColorMenuItem = new JMenuItem();
            buddyColorMenuItem.setText(menuItemText + "'s Color");
            buddyColorMenuItem.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color color = JColorChooser.showDialog(buddyColorMenuItem,
                                    "Color chooser", getMessageTabRecord().getBuddyForeground());
                    if (color == null){
                        return;
                    }
                    if (!getMessageTabRecord().getBuddyForeground().equals(color)){
                        IMessageTabRecord tab = getMessageTabRecord();
                        tab.setBuddyForeground(color);
                        IPbcTalker talker = kernel.getPointBoxFace().getPointBoxTalker();
                        talker.getMessagingPaneManager().getMasterMessagingBoard().refreshTextStyle(messageTabID, tab);
//                        talkerSettings.fireSettingsUpdated(null, new SettingsEvent(getMessageTabRecord()));
                    }
                }
            });
            add(buddyColorMenuItem);
            
            setTitleMenuItem = new JMenuItem();
            setTitleMenuItem.setText("Save new group and persist its frame");
            setTitleMenuItem.setVisible(false);
            add(setTitleMenuItem);
        }
    }

    /**
     * @return the setTitleMenuItem
     */
    public JMenuItem getSetTitleMenuItem() {
        return setTitleMenuItem;
    }

}
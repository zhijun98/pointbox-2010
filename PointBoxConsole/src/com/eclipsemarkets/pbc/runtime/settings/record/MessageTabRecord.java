/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.runtime.settings.record;

import com.eclipsemarkets.runtime.PointBoxSettings;
import com.eclipsemarkets.global.SwingGlobal;
import java.awt.Color;
import java.awt.Font;

/**
 * MessageTabRecord.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Aug 27, 2010, 8:14:54 PM
 */
class MessageTabRecord extends PointBoxSettings implements IMessageTabRecord{
    private String messageTabID;
    private Font myFont;
    private Color myForeground;
    private Font buddyFont;
    private Color buddyForeground;
    
    /**
     * todo: Currently, if users choose to "display timestamp", it means display timestamp for all the tabs 
     * but not only the current tab. However, it potentially can be changed for a specific tab
     */
    private static boolean displayTimestamp;
    
    /**
     * todo: Currently, if users choose to "price it", it means price all the tabs but not only the current tab. 
     * However, it potentially can be changed for a specific tab
     */
    private static boolean displayPrices;

    MessageTabRecord(String ownerUniqueName, String messageTabId) {
        super(ownerUniqueName);

        if (messageTabId == null){
            messageTabID = "";
        }else{
            messageTabID = messageTabId;
        }
        myFont = SwingGlobal.getLabelFont();
        myForeground = Color.BLACK;
        buddyFont = SwingGlobal.getLabelFont();
        buddyForeground = Color.BLUE;
        displayTimestamp = true;
        displayPrices = false;
    }

    public synchronized Font getBuddyFont() {
        return buddyFont;
    }

    public synchronized void setBuddyFont(Font buddyFont) {
        this.buddyFont = buddyFont;
    }

    public synchronized Color getBuddyForeground() {
        return buddyForeground;
    }

    public synchronized void setBuddyForeground(Color buddyForeground) {
        this.buddyForeground = buddyForeground;
    }

    public synchronized Font getMyFont() {
        return myFont;
    }

    public synchronized void setMyFont(Font myFont) {
        this.myFont = myFont;
    }

    public synchronized Color getMyForeground() {
        return myForeground;
    }

    public synchronized void setMyForeground(Color myForeground) {
        this.myForeground = myForeground;
    }

    public synchronized boolean isDisplayPrices() {
        return displayPrices;
    }

    public synchronized void setDisplayPrices(boolean displayPrices) {
        this.displayPrices = displayPrices;
    }

    public synchronized boolean isDisplayTimestamp() {
        return displayTimestamp;
    }

    public synchronized void setDisplayTimestamp(boolean displayTimestamp) {
        this.displayTimestamp = displayTimestamp;
    }

    public synchronized String getMessageTabID() {
        return messageTabID;
    }

    public synchronized void setMessageTabID(String messageTabID) {
        this.messageTabID = messageTabID;
    }
}

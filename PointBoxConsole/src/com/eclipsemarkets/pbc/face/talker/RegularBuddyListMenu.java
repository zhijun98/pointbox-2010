/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker;

import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * BuddyListPopupMenu1.java
 * <p>
 * @author Zhijun Zhang
 * which is a JXPanel instance
 * Created on May 16, 2010, 7:43:38 AM
 */
class RegularBuddyListMenu extends BasicBuddyListMenu {
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    private boolean isDisplayOfflineBuddies;
    static{
        logger = Logger.getLogger(RegularBuddyListMenu.class.getName());
    }

    RegularBuddyListMenu(final IPbcTalker talker,
                            final IBuddyListPanel buddyListPanel,
                            final DefaultMutableTreeNode focusNode,final boolean isDisplayOfflineBuddies)
    { 
        super(talker, buddyListPanel, focusNode);
        this.isDisplayOfflineBuddies=isDisplayOfflineBuddies;
    }

    @Override
    void constructBuddyListMenu() {
        add(addNewBuddyMenuItem);
        add(deleteBuddyMenuItem);
        add(moveBuddyMenuItem);
        add(renameGroupMenuItem);
        if(isDisplayOfflineBuddies){
            add(addGroupMenuItem);
            add(editGroupMenuItem);
            add(removeGroupMenuItem);
        }
        add(sortGroupFromA2ZMenuItem);
        add(sortGroupFromZ2AMenuItem);
        add(profileMenuItem);
        add(blockBuddyMenuItem);
    }
}

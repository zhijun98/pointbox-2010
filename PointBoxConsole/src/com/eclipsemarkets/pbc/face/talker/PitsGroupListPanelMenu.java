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
class PitsGroupListPanelMenu extends BasicBuddyListMenu {
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(PitsGroupListPanelMenu.class.getName());
    }

    PitsGroupListPanelMenu(final IPbcTalker talker,
                            final IBuddyListPanel buddyListPanel,
                            final DefaultMutableTreeNode focusNode)
    { 
        super(talker, buddyListPanel, focusNode);
    }

    @Override
    void constructBuddyListMenu() {
        add(renameFrameNodeItem);
        add(removeFrameNodeItem);
        add(modifyPitsGroupNodeItem);
        add(sortGroupFromA2ZMenuItem);
        add(sortGroupFromZ2AMenuItem);
    }
}

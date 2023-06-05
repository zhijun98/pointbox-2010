/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Zhijun Zhang, date & time: Dec 21, 2013 - 1:53:55 PM
 */
public class PitsCastListPanelMenu extends BasicBuddyListMenu{

    public PitsCastListPanelMenu(IPbcTalker talker, IBuddyListPanel buddyListPanel, DefaultMutableTreeNode focusNode) {
        super(talker, buddyListPanel, focusNode);
    }

    @Override
    void constructBuddyListMenu() {
        super.constructBuddyListMenu();
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.talker.dndtree;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

/**
 *
 * @author Zhijun Zhang
 */
class RegularBuddyListRenderer extends JPanel implements TreeCellRenderer {
    private static final Logger logger;

    static {
        logger = Logger.getLogger(RegularBuddyListRenderer.class.getName());
    }
    
    IPbcKernel kernel;
    JLabel label;

    Color originalBackground;
    Color originalLabelForeground;

    RegularBuddyListRenderer(IPbcKernel kernel) {
        this.kernel = kernel;
        
        setLayout(null);
        
        label = new JLabel();
        label.setForeground(UIManager.getColor("Tree.textForeground"));
        add(label);
        
        originalBackground = this.getBackground();
        originalLabelForeground = label.getForeground();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        /**
         * DEBUG PURPOSE
         */
//        if (tree instanceof PbcDndBuddyTree){
//            PbcDndBuddyTree dndTree = (PbcDndBuddyTree)tree;
//            String treeName = dndTree.getRootTreeName();
//            if (("AIM".equalsIgnoreCase(treeName))
//                    && ("[Distribution Group] test_group".equalsIgnoreCase(value.toString())))
//            {
//                System.out.println(treeName);
//                System.out.println(value);
//            }
//        }
        
        if (selected){
            label.setForeground(Color.red);
            this.setBackground(Color.yellow);
        }else{
            label.setForeground(originalLabelForeground);
            this.setBackground(originalBackground);
        }
        if (value instanceof IDnDBuddyTreeNode){
            IGatewayConnectorBuddy buddy = ((IDnDBuddyTreeNode)value).getGatewayConnectorBuddy();
            label.setIcon(kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getBuddyImageIcon(buddy));
            if (buddy.getIMUniqueName().equalsIgnoreCase(kernel.getPointBoxLoginUser().getIMUniqueName())){
                label.setText(PbcReservedTerms.PricerBuddy.toString());
            }else{
                if (PbcReservedTerms.PricerBuddy.toString().equalsIgnoreCase(buddy.getNickname())){
                    buddy.setNickname(buddy.getIMScreenName());
                }
                label.setText(buddy.getNickname());
            }
            label.setFont(SwingGlobal.getLabelFont());
            coloringLabel(label, buddy.getBuddyStatus());
        }else if (value instanceof IDnDGroupTreeNode){
            IGatewayConnectorGroup distGroup = ((IDnDGroupTreeNode)value).getGatewayConnectorGroup();
            ImageIcon icon;
            if (label.getIcon() == null){
                icon = kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getPointBoxIcon16();
                label.setIcon(icon);
            }else{
                if (distGroup.getLoginUser() != null){
                    icon = kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getBuddyImageIcon(distGroup.getLoginUser());
                    label.setIcon(icon);
                }
            }
            label.setText(distGroup.getGroupName());
            label.setForeground(Color.BLACK);
            label.setFont(SwingGlobal.getBoldedLabelFont());
        }else if (value instanceof DnDMutableHybridTreeRoot) {
            label.setText(((DnDMutableTreeNode)value).getUserObject().toString());
            label.setForeground(Color.BLACK);
            label.setFont(SwingGlobal.getLabelFont());
            label.setIcon(kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getPbimBuddyIcon());
        }else if (value instanceof DnDMutableTreeNode) {//DnDMutableHybridTreeRoot
            label.setText(((DnDMutableTreeNode)value).getUserObject().toString());
            label.setForeground(Color.BLACK);
            label.setFont(SwingGlobal.getLabelFont());
        }else if (value instanceof String){
            label.setIcon(kernel.getPointBoxConsoleRuntime().getPbcImageSettings().getOpenedGroupIcon());
            label.setText(value.toString());
            label.setFont(SwingGlobal.getLabelFont());
            label.setForeground(Color.BLACK);
        }else {
            label.setForeground(Color.BLACK);
        }
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return label.getPreferredSize();
    }

    @Override
    public void doLayout() {
        Dimension d_label = label.getPreferredSize();
        label.setLocation(0, 0);
        label.setBounds(0, 0, d_label.width, d_label.height);
    }

    @Override
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }

    void coloringLabel(JLabel label, BuddyStatus buddyStatus) {
        switch(buddyStatus){
            case Online:
                label.setForeground(Color.BLACK);
                break;
            default:
                label.setForeground(Color.LIGHT_GRAY);
        }
    }
}

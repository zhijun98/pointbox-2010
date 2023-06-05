/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 *
 * @author Zhijun Zhang, date & time: Dec 22, 2013 - 11:04:42 PM
 */
public class PitsCastCheckTreeCellRenderer extends JPanel implements TreeCellRenderer
{
    private IPbconsoleImageSettings settings;
    private JCheckBox check;
    private JLabel label;

    public PitsCastCheckTreeCellRenderer(JTree tree, IPbconsoleImageSettings settings) {
        this.settings = settings;
        check = new JCheckBox();
        label = new JLabel();
        label.setFont(SwingGlobal.getLabelFont());
        add(check, BorderLayout.WEST);
        add(label,BorderLayout.CENTER);
        setBackground(tree.getBackground());
        setForeground(tree.getForeground());
        setLayout(new BorderLayout());
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if(value instanceof PitsCastCheckGroupTreeNode){
            check.setSelected(((PitsCastCheckGroupTreeNode)value).isSelected());IGatewayConnectorGroup group=((PitsCastCheckGroupTreeNode)value).getGroup();
            label.setIcon(settings.getConnectorBuddyIcon(GatewayServerType.PBIM_SERVER_TYPE));
            label.setText(group.getGroupName());
        }
        return this;
    }
    
}

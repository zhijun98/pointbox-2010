/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.TreeCellRenderer;

/**
 *
 * @author Zhijun Zhang
 */
class EmsCheckRenderer extends JPanel implements TreeCellRenderer {
    private static final Logger logger;
    static{
        logger = Logger.getLogger(EmsCheckRenderer.class.getName());
    }
    private static final long serialVersionUID = 1L;
    private JCheckBox check;
    private EmsTreeLabel label;

    private Dimension checkDefaultSize;
    
    /**
     * Create new CheckRenderer.
     */
    EmsCheckRenderer() {
        setLayout(null);
        add(check = new JCheckBox());
        add(label = new EmsTreeLabel());
        check.setBackground(UIManager.getColor("Tree.textBackground"));
        checkDefaultSize = check.getPreferredSize();
        label.setForeground(UIManager.getColor("Tree.textForeground"));
        label.setOpaque(true);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean isSelected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected,
            expanded, leaf, row, hasFocus);
        
//        if (value != null){
//            logger.log(Level.INFO, "EmsCheckRenderer >>> {0}", value.getClass().getCanonicalName());
//        }
//        
//        if (value instanceof GroupCheckNode){
//            check.setVisible(true);
//        }else{
//            check.setVisible(false);
//        }
        
        setEnabled(tree.isEnabled());
        check.setSelected(((EmsCheckNode)value).isSelected());
        if (value instanceof GroupCheckNode){
            check.setVisible(true);
            check.setPreferredSize(checkDefaultSize);
            GroupCheckNode groupNode = (GroupCheckNode)value;
            Enumeration nmr_group = groupNode.getChildrenEnumeration();
            Object obj;
            boolean findCheck = false;
            boolean findUncheck = false;
            while (nmr_group.hasMoreElements()){
                obj = nmr_group.nextElement();
                if (obj instanceof BuddyCheckNode){
                    if (((BuddyCheckNode)obj).isSelected()){
                        findCheck = true;
                    }
                    if (!((BuddyCheckNode)obj).isSelected()){
                        findUncheck = true;
                    }
                }
            }//while
            if (findCheck && findUncheck){
                label.setFont(SwingGlobal.getTahomaBoldedItalicsFont11());  
            }else{
                label.setFont(SwingGlobal.getBoldedLabelFont());  
            }
            if ((!findCheck) && (findUncheck)){
                groupNode.setSelectedSimply(false);
            }else{
                groupNode.setSelectedSimply(true);
            }
            label.setForeground(Color.BLACK);
        }else if (value instanceof BuddyCheckNode) {
            check.setVisible(true);
            check.setPreferredSize(checkDefaultSize);
            label.setFont(tree.getFont());
            IGatewayConnectorBuddy buddy = ((BuddyCheckNode)value).getBuddy();
            if (BuddyStatus.Online.equals(buddy.getBuddyStatus())){
                label.setForeground(Color.BLACK);
            }else{
                label.setForeground(Color.LIGHT_GRAY);
            }
            stringValue = getPricerBuddyDescriptiveName(buddy);
        }else{
            check.setVisible(false);
            check.setPreferredSize(new Dimension(1, 1));
            label.setFont(tree.getFont());
            label.setForeground(Color.BLACK);
        }
        
        label.setText(stringValue);
        label.setSelected(isSelected);
        label.setFocus(hasFocus);
        Icon icon = ((EmsCheckNode)value).getIcon();
        label.setIcon(icon);
        return this;
    }
    
    private String getPricerBuddyDescriptiveName(IGatewayConnectorBuddy buddy){
        if (buddy == null){
            return PbcReservedTerms.UNKNOWN.toString();
        }
        IGatewayConnectorBuddy loginUser = buddy.getLoginOwner();
        if ((loginUser != null)
                && (GatewayServerType.PBIM_SERVER_TYPE.equals(loginUser.getIMServerType()))
                && (loginUser.getIMUniqueName().equalsIgnoreCase(buddy.getIMUniqueName())))
        {
            return PbcReservedTerms.PricerBuddy.toString();
        } else {
            if (PbcReservedTerms.PricerBuddy.toString().equalsIgnoreCase(buddy.getNickname())){
                buddy.setNickname(buddy.getIMScreenName());
            }
            return buddy.getNickname();
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d_check = new Dimension(30, 30);
        Dimension d_label = label.getPreferredSize();
        return new Dimension(d_check.width + d_label.width,
            d_check.height < d_label.height ? d_label.height : d_check.height);
    }

    @Override
    public void doLayout() {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        if (d_check.height < d_label.height) {
            y_check = (d_label.height - d_check.height) / 2;
        }
        else {
            y_label = (d_check.height - d_label.height) / 2;
        }
        check.setLocation(0, y_check);
        check.setBounds(0, y_check, d_check.width, d_check.height);
        label.setLocation(d_check.width, y_label);
        label.setBounds(d_check.width, y_label, d_label.width, d_label.height);
    }

    @Override
    public void setBackground(Color color) {
        if (color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }
}

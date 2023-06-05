/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.gateway.user.PbcReservedTerms;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleImageSettings;
import com.eclipsemarkets.storage.constant.GatewayServerType;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

/**
 *
 * @author Zhijun Zhang, date & time: Jul 27, 2013 - 10:02:22 PM
 */
class FloatingFrameCheckListItemRenderer extends JPanel implements ListCellRenderer
{
    private IPbconsoleImageSettings settings;
    private JCheckBox check;
    private JLabel label;

    private FloatingFrameCheckListItemRenderer(IPbconsoleImageSettings settings) {
        this.settings = settings;
        init();
    }
    
    static FloatingFrameCheckListItemRenderer createNewInstance(IPbconsoleImageSettings settings){
        return new FloatingFrameCheckListItemRenderer(settings);
    }

    private void init(){
        if(SwingUtilities.isEventDispatchThread()){
            initHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    initHelper();
                }
            });
        }
    }

    private void initHelper(){
        check=new JCheckBox();
        label=new JLabel();
    }

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean hasFocus)
    {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setLayout(new BorderLayout());
        if(value instanceof FloatingFrameCheckBuddyItem){
            check.setSelected(((FloatingFrameCheckBuddyItem) value).isSelected());
            IGatewayConnectorBuddy member=((FloatingFrameCheckBuddyItem)value).getBuddy();
            if(member.getLoginOwner().getBuddyStatus().equals(BuddyStatus.Online)&&BuddyStatus.Online.equals(member.getBuddyStatus())){
                label.setIcon(settings.getConnectorBuddyIcon(member.getIMServerType()));
            }else{
                label.setIcon(settings.getConnectorLogo21(member.getIMServerType()));
            }
            label.setText(getPricerBuddyDescriptiveName(member));
            label.setFont(SwingGlobal.getLabelFont());
            add(check, BorderLayout.WEST);
            add(label,BorderLayout.CENTER);
        }else if(value instanceof FloatingFrameCheckGroupItem){
            check.setSelected(((FloatingFrameCheckGroupItem)value).isSelected());
            IGatewayConnectorGroup group=((FloatingFrameCheckGroupItem)value).getGroup();
            label.setIcon(settings.getConnectorBuddyIcon(GatewayServerType.PBIM_SERVER_TYPE));
            label.setText(group.getGroupName());
            label.setFont(SwingGlobal.getLabelFont());
            add(check, BorderLayout.WEST);
            add(label,BorderLayout.CENTER);
        }

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
}

/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.gateway.user.IGatewayConnectorBuddy;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.tree.MutableTreeNode;
/**
 *
 * @author Zhijun Zhang
 */
abstract class EmsCheckNode extends EmsTreeNode implements IEmsCheckNode{

    /**
     * Mode to use if the node should not expand when selected.
     */
    public static final int SINGLE_SELECTION = 0;
    /**
     * Mode to use if the node should be expaned if selected and if possible.
     */
    public static final int DIG_IN_SELECTION = 4;
    private static final long serialVersionUID = 1L;
    private int selectionMode;
    private String fullName;

    /**
     * Construct an empty node.
     */
    public EmsCheckNode() {
        this(null);
    }

    /**
     * Creates a new CheckNode with the specified name.
     *
     * @param userObject the name to use.
     */
    public EmsCheckNode(Object userObject) {
        this(userObject, true, false);

    }

    public EmsCheckNode(String userObject) {
        this(userObject, true, false);
        this.fullName = userObject;
    }

    /**
     * Constructs a new CheckNode.
     *
     * @param userObject     the name to use.
     * @param allowsChildren true if it allows children.
     * @param isSelected     true if it is to be selected.
     */
    public EmsCheckNode(Object userObject, boolean allowsChildren, boolean isSelected) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
        if (userObject != null) {
            this.fullName = userObject.toString();
        }

        setSelectionMode(DIG_IN_SELECTION);
    }

    /**
     * Constructs a new CheckNode.
     *
     * @param userObject     the name to use.
     * @param allowsChildren true if it allows children.
     * @param icon the icon to use.
     */
    public EmsCheckNode(String userObject, boolean allowsChildren, Icon icon) {
        super(userObject, allowsChildren, icon);
        setSelectionMode(DIG_IN_SELECTION);
        this.fullName = userObject;
    }

    public Vector getChilds() {
        return this.children;
    }

    @Override
    public void addChildCheckNode(IEmsCheckNode memberNode) {
        if ((memberNode != null) && (memberNode instanceof MutableTreeNode)){
            super.add((MutableTreeNode)memberNode);
        }
    }

    @Override
    public void removeChildCheckNode(IEmsCheckNode memberNode) {
        if ((memberNode != null) && (memberNode instanceof MutableTreeNode)){
            super.remove((MutableTreeNode)memberNode);
        }
    }

    @Override
    public void removeChildrenCheckNodes() {
        super.removeAllChildren();
    }

    @Override
    public void setAssociatedObject(Object aMember) {
        super.setAssociatedObject(aMember);
        if ((aMember != null) && (aMember instanceof IGatewayConnectorBuddy)){
            IGatewayConnectorBuddy aBuddy = (IGatewayConnectorBuddy)aMember;
            if (BuddyStatus.Online.equals(aBuddy.getBuddyStatus())){
                setSelected(true);
            }else{
                setSelected(false);
            }
        }
    }

    @Override
    public Enumeration getChildrenEnumeration() {
        return children();
    }

    /**
     * Constructs a new CheckNode.
     *
     * @param userObject     the name to use.
     * @param allowsChildren true if it allows children.
     * @param isSelected     true if it is selected.
     * @param name           the identifier name.
     */
    public EmsCheckNode(Object userObject, boolean allowsChildren, boolean isSelected, String name) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
        setSelectionMode(DIG_IN_SELECTION);
        fullName = name;
    }

    /**
     * Returns the full name of the node.
     *
     * @return the full name of the node.
     */
    @Override
    public String getFullName() {
        return fullName;
    }

    public void SetFullName(String name) {
        this.fullName = name;
    }

    /**
     * Sets the selection mode.
     *
     * @param mode the selection mode to use.
     */
    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    /**
     * Returns the selection mode.
     *
     * @return the selection mode.
     */
    public int getSelectionMode() {
        return selectionMode;
    }

    /**
     * Selects or deselects node.
     *
     * @param isSelected true if the node should be selected, false otherwise.
     */
    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        //the following moved to GroupCheckNode
//        if (selectionMode == DIG_IN_SELECTION && children != null) {
//            Enumeration nodeEnum = children.elements();
//            while (nodeEnum.hasMoreElements()) {
//                EmsCheckNode node = (EmsCheckNode) nodeEnum.nextElement();
//                node.setSelected(isSelected);
//            }
//        }
    }

    /**
     * Returns true if the node is selected.
     *
     * @return true if the node is selected.
     */
    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public IEmsCheckNode retrieveChildrenNode(IGatewayConnectorGroup group) {
        if (children == null){
            return null;
        }
        String groupUniqueName = group.getIMUniqueName();
        IEmsCheckNode groupNode;
        Object obj;
        EmsCheckNode node;
        Enumeration nodeEnum = children.elements();
        while (nodeEnum.hasMoreElements()) {
            node = (EmsCheckNode) nodeEnum.nextElement();
            obj = node.getAssociatedObject();
            if (obj instanceof IGatewayConnectorGroup){
                if (((IGatewayConnectorGroup)obj).getIMUniqueName().equalsIgnoreCase(groupUniqueName)){
                    return node;
                }
            }
            groupNode = node.retrieveChildrenNode(group);
            if (groupNode != null){
                return groupNode;
            }
        }
        return null;
    }

    @Override
    public IEmsCheckNode retrieveChildrenNode(IGatewayConnectorBuddy buddy) {
        if (children == null){
            return null;
        }
        String buddyUniqueName = buddy.getIMUniqueName();
        IEmsCheckNode buddyNode;
        Object obj;
        IEmsCheckNode node;
        Enumeration nodeEnum = children.elements();
        while (nodeEnum.hasMoreElements()) {
            node = (EmsCheckNode) nodeEnum.nextElement();
            obj = node.getAssociatedObject();
            if (obj instanceof IGatewayConnectorBuddy){
                if (((IGatewayConnectorBuddy)obj).getIMUniqueName().equalsIgnoreCase(buddyUniqueName)){
                    return node;
                }
            }
            buddyNode = node.retrieveChildrenNode(buddy);
            if (buddyNode != null){
                return buddyNode;
            }
        }
        return null;
    }


    /**
     * Checks if the memberNode is a current member node of this EmsCheckNode
     * @param nodeToCheck
     * @return true if nodeToCheck is a current member node
     */
    @Override
    public boolean isMember (IEmsCheckNode nodeToCheck) {
       if ( nodeToCheck == null) {
          return false;
       }

       String memberFullName = nodeToCheck.getFullName();
       int childrenCount = this.children.size();
       for ( int i = 0; i< childrenCount; i++ ) {
          EmsCheckNode curMember = (EmsCheckNode)this.children.get(i);
          String curFullName = curMember.getFullName();

          if ( memberFullName != null && memberFullName.equals(curFullName)) {
             return true;
          }
       }
       return false;
    }

    void setSelectedSimply(boolean isSelected) {
        this.isSelected = isSelected;
    }
}

/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.talker;

import java.util.logging.Logger;

/**
 * TalkerNewBuddyGroupDialogCustomizer
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Nov 10, 2010 at 9:39:20 AM
 */
class TalkerNewBuddyGroupDialogCustomizer implements ITalkerBuddyGroupDialogCustomizer{

    private static final Logger logger = Logger.getLogger(TalkerNewBuddyGroupDialogCustomizer.class.getName());


    private String baseDialogTitle = "Add New Buddy/Group";
    private String buddyNameFieldValue = null;
    private String message = "*choose or type in a new group name";
    private NewBuddyGroupDialogCommand addButtonCommand = NewBuddyGroupDialogCommand.Add_New_Buddy;
    private String cancelButtonText = "Cancel";
    private String loginUserNameFieldValue;

    private NewBuddyGroupDialogTerms purpose;

    public NewBuddyGroupDialogTerms getPurpose() {
        return purpose;
    }

    public void setPurpose(NewBuddyGroupDialogTerms purpose) {
        this.purpose = purpose;
    }

    public String getBaseDialogTitle() {
        return baseDialogTitle;
    }

    public void setBaseDialogTitle(String baseDialogTitle) {
        this.baseDialogTitle = baseDialogTitle;
    }

    public NewBuddyGroupDialogCommand getAddButtonCommand() {
        return addButtonCommand;
    }

    public void setAddButtonCommand(NewBuddyGroupDialogCommand addButtonCommand) {
        this.addButtonCommand = addButtonCommand;
    }

    public String getCancelButtonText() {
        return cancelButtonText;
    }

    public void setCancelButtonText(String cancelButtonText) {
        this.cancelButtonText = cancelButtonText;
    }

    public String getLoginUserNameFieldValue() {
        return loginUserNameFieldValue;
    }

    public void setLoginUserNameFieldValue(String loginUserNameFieldValue) {
        this.loginUserNameFieldValue = loginUserNameFieldValue;
    }

    /**
     *
     * @return if the value is NOT null, the jBuddyName text field will be disabled.
     */
    public String getBuddyNameFieldValue() {
        return buddyNameFieldValue;
    }

    public void setBuddyNameFieldValue(String buddyNameFieldValue) {
        if (buddyNameFieldValue.trim().isEmpty()){
            buddyNameFieldValue = null;
        }
        this.buddyNameFieldValue = buddyNameFieldValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message.trim().isEmpty()){
            message = "*choose or type in a new group name";
        }
        this.message = message;
    }

}//TalkerNewBuddyGroupDialogCustomizer


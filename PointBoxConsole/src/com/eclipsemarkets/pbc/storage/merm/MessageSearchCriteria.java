/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.storage.merm;

/**
 *
 * @author xmly
 */
abstract class MessageSearchCriteria implements IMessageSearchCriteria {

    protected MessageSearchCriteriaTerms searchCriteria;

    public MessageSearchCriteriaTerms getSearchCriteria() {
        return searchCriteria;
    }

    public void setSearchCriteria(MessageSearchCriteriaTerms searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    public static enum MessageSearchCriteriaTerms {

        pb_to_user("pb_to_user"),
        pb_from_user("pb_from_user"),
        pb_timestamp("pb_timestamp"),
        pb_message("pb_message"),
        pb_outgoing("pb_outgoing"),
        pb_servertype("pb_servertype");
        private String term;

        MessageSearchCriteriaTerms(String term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return term;
        }

        public static MessageSearchCriteriaTerms convertStringToViewerSearchCriteriaTerms(String string) {
            if (string.equalsIgnoreCase(pb_to_user.toString())) {
                return pb_to_user;
            }

            if (string.equalsIgnoreCase(pb_from_user.toString())) {
                return pb_from_user;
            }


            if (string.equalsIgnoreCase(pb_timestamp.toString())) {
                return pb_timestamp;
            }

            if (string.equalsIgnoreCase(pb_message.toString())) {
                return pb_message;
            }

            if (string.equalsIgnoreCase(pb_outgoing.toString())) {
                return pb_outgoing;
            }

            if (string.equalsIgnoreCase(pb_servertype.toString())) {
                return pb_servertype;
            }

            return null;
        }
    }
}

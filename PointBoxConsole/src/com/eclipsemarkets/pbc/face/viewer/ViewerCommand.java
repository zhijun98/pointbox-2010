/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer;

/**
 * ViewerCommand.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 10:44:17 AM
 */
public enum ViewerCommand {
    CloseAllViewerTabs("Close All Tabs"),
    CloseViewerTab("Close Tab"),
    RenameViewerTab("Rename Tab"),
    populateForResending("populateForResending"),
    populateForFixing("populateForFixing"),
    populateRowIntoTEFItem("Update/Resend Quote"),
    fixQuoteByTEFItem("Fix Quote"),
    exportSingleRowIntoTextItem("Export This Quote"),
    exportAllRowsIntoTextItem("Export All Quotes"),
    FloatingFrame("Floating Windows"),
    removeSelectedQuotes("Remove Selected Quotes"),
    logProblematicQuote("Log Problematic Quote");

    private String term;
    ViewerCommand(String term){
        this.term = term;
    }

    @Override
    public String toString() {
        return term;
    }
}

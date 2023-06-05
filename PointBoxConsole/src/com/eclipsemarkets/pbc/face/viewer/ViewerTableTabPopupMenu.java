/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.data.PointBoxQuoteType;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuoteWrapper;
import com.eclipsemarkets.gateway.user.IGatewayConnectorGroup;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.face.talker.IPbcTalker;
import com.eclipsemarkets.pbc.face.viewer.search.ViewerSearchFactory;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

/**
 * ViewerTableTabPopupMenu.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Aug 8, 2010, 2:55:09 PM
 */
class ViewerTableTabPopupMenu extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    private JMenuItem optionPricerMenuItem;
    private JMenuItem copyToClipboardMenuItem;
    private JMenu copyToMenu;
    private JMenu sendToMenu;
    private JMenuItem clearTradeMenuItem;
    private JMenuItem exportSingleRowIntoText;
    private JMenuItem exportAllRowsIntoText;
    private JMenuItem hideSimilarRowItem;
    private JMenuItem editFilterQuotesItem;
    private JMenuItem enableScrollingViewerItem;
    private JMenuItem disableScrollingViewerItem;
    private JMenuItem newFilterQuotesItem;
    private JMenuItem clearViewerItemMenuItem;
    private JMenu displaySimilarRowSub;
    private IViewerTablePanel viewerTableTab;

    /**
     * this menu's target quote wrapper
     */
    private IPbsysOptionQuoteWrapper targetQuoteWrapper;

    private IPbcViewer viewer;

    ViewerTableTabPopupMenu(final IPbcViewer viewer, final IViewerTablePanel viewerTableTab) {
        this.viewer = viewer;
        this.viewerTableTab = viewerTableTab;
        targetQuoteWrapper = null;

        optionPricerMenuItem = new JMenuItem(ViewerTableTabPopupMenuTerms.optionPricerItem.toString());
        optionPricerMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner() != null) && (targetQuoteWrapper.getQuoteOwner().isSufficientPricingData())){
                    //viewer.displayStripPricerWithQuote(targetQuoteWrapper);
                    viewer.displaySimPricerWithQuote(targetQuoteWrapper, PointBoxQuoteType.OPTION);
                }else{
                    JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote for this operation.");
                }
            }
        });
        optionPricerMenuItem.setFont(SwingGlobal.getLabelFont());
        //add(stripPricerMenuItem);
        
        clearTradeMenuItem = new JMenuItem(ViewerTableTabPopupMenuTerms.clearTradeinClearPort.toString());
        clearTradeMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner() != null) && (targetQuoteWrapper.getQuoteOwner().isSufficientPricingData())){
                     viewer.displayClearPortMainFrameWithQuote(targetQuoteWrapper);
                }else{
                    JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote for this operation.");
                }
            }
        });
        clearTradeMenuItem.setFont(SwingGlobal.getLabelFont());
        //add(clearTradeMenuItem);     
        
        loadSendAndCopyItemsHelper();       

        copyToClipboardMenuItem = new JMenuItem(ViewerTableTabPopupMenuTerms.copyToClipboardItem.toString());
                copyToClipboardMenuItem.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner() != null) ){
                            if (targetQuoteWrapper.getQuoteOwner().getInstantMessage() != null){
                                StringSelection stringSelection = new StringSelection( targetQuoteWrapper.getQuoteOwner().getInstantMessage().getMessageContent());
                                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                clipboard.setContents( stringSelection, stringSelection );
                            }else{
                                JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote which contains instant messages.");
                            }
                        }else{
                            //JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote for this operation.");
                        }
                    }
                });
        copyToClipboardMenuItem.setFont(SwingGlobal.getLabelFont());
        //add(copyToClipboardMenuItem);
             
        /*
        fixQuoteByTEF = new JMenuItem(ViewerTableTabPopupMenuTerms.fixQuote.toString());
        fixQuoteByTEF.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner().isSufficientPricingData())){
                    pointBoxFrame.getTradeEntryForm().displayTradeEntryWithQuote(targetQuoteWrapper, TradeEntryPurpose.forFix);
                }else{
                    JOptionPane.showMessageDialog(pointBoxFrame.getBaseFrame(), "Please click on a valid option quote for this operation.");
                }
            }
        });
        fixQuoteByTEF.setFont(SwingGlobal.getLabelFont());
        add(fixQuoteByTEF);
        */
        exportSingleRowIntoText = new JMenuItem(ViewerTableTabPopupMenuTerms.exportSingleRowIntoTextItem.toString());
        exportSingleRowIntoText.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                exportQuote(e);
            }
        });
        exportSingleRowIntoText.setFont(SwingGlobal.getLabelFont());
        //add(exportSingleRowIntoText);

        exportAllRowsIntoText = new JMenuItem(ViewerTableTabPopupMenuTerms.exportAllRowsIntoTextItem.toString());
        exportAllRowsIntoText.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                exportAllQuotes(e);
            }
        });
        exportAllRowsIntoText.setFont(SwingGlobal.getLabelFont());
        //add(exportAllRowsIntoText);

        editFilterQuotesItem = new JMenuItem(ViewerTableTabPopupMenuTerms.editFilterQuoteShortcutItem.toString());
        editFilterQuotesItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewerSearchFactory.getViewerFilterDialogInstance(viewer, true, false).displayDialog();
            }
        });
        editFilterQuotesItem.setFont(SwingGlobal.getLabelFont());
        //add(editFilterQuoteShortcutItem);
        editFilterQuotesItem.setEnabled(false);

        hideSimilarRowItem = new JMenuItem(ViewerTableTabPopupMenuTerms.hideSimilarRow.toString());
        hideSimilarRowItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                hideSimilarRow(e);
            }
        });
        hideSimilarRowItem.setFont(SwingGlobal.getLabelFont());
        //add(hideSimilarRowItem);
        
        enableScrollingViewerItem = new JMenuItem(ViewerTableTabPopupMenuTerms.enableScrollingViewerItem.toString());
        enableScrollingViewerItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                enableScrollingViewerItem.setEnabled(false);
                disableScrollingViewerItem.setEnabled(true);
                viewerTableTab.enableAutomaticallyScrollingViewer();
            }
        });
        enableScrollingViewerItem.setEnabled(false);
        //add(enableScrollingViewerItem);
        
        disableScrollingViewerItem = new JMenuItem(ViewerTableTabPopupMenuTerms.disableScrollingViewerItem.toString());
        disableScrollingViewerItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                disableScrollingViewerItem.setEnabled(false);
                enableScrollingViewerItem.setEnabled(true);
                viewerTableTab.disableAutomaticallyScrollingViewer();
            }
        });
        disableScrollingViewerItem.setEnabled(true);
        //add(disableScrollingViewerItem);
        
        hideSimilarRowItem.setFont(SwingGlobal.getLabelFont());
        //add(hideSimilarRowItem);
        

        displaySimilarRowSub = new JMenu(ViewerTableTabPopupMenuTerms.displaySimilarRow.toString());
        initializeDisplaySimilarRow();

        /*
        floatingFrame = new JMenuItem(ViewerTableTabPopupMenuTerms.FloatingFrame.toString());
        floatingFrame.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                displayFloatingFrame(e);
            }
        });
        floatingFrame.setFont(SwingGlobal.getLabelFont());
        add(floatingFrame);
        */

        /*
        removeSelectedQuotes = new JMenuItem(ViewerTableTabPopupMenuTerms.removeSelectedQuotes.toString());
        removeSelectedQuotes.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                removeSelectedQuotes();
            }
        });
        removeSelectedQuotes.setFont(SwingGlobal.getLabelFont());
        add(removeSelectedQuotes);
        */

        /*
        logProblematicQuotes = new JMenuItem(ViewerTableTabPopupMenuTerms.logProblematicQuote.toString());
        logProblematicQuotes.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               logProblemQuotes(e);
           }
        });
        logProblematicQuotes.setFont(SwingGlobal.getLabelFont());
        add(logProblematicQuotes);*/
        newFilterQuotesItem=new JMenuItem(ViewerTableTabPopupMenuTerms.newFilterQuoteShortcutItems.toString());
        newFilterQuotesItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ViewerSearchFactory.getViewerFilterDialogInstance(viewer, true, true).displayDialog();
            }
        });
        newFilterQuotesItem.setFont(SwingGlobal.getLabelFont());
        //add(searchQuotesItem);
        newFilterQuotesItem.setEnabled(true);     

        clearViewerItemMenuItem = new JMenuItem(ViewerTableTabPopupMenuTerms.clearViewerItem.toString());
                clearViewerItemMenuItem.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        clearViewerItemMenuItem.setEnabled(false);
//                        (new SwingWorker<Void, Void>(){
//                            @Override
//                            protected Void doInBackground() throws Exception {
//                                viewer.clearViewer();
//                                return null;
//                            }
//
//                            @Override
//                            protected void done() {
//                                clearViewerItemMenuItem.setEnabled(true);
//                            }
//                        }).execute();
                        viewer.getKernel().getPointBoxFace().clearViewerOptions();
                        clearViewerItemMenuItem.setEnabled(true);
                    }
                });
        clearViewerItemMenuItem.setFont(SwingGlobal.getLabelFont());
        //add(clearViewerItemMenuItem);
        
        add(copyToMenu);
        
        add(sendToMenu);
        
        add(copyToClipboardMenuItem);
        add(new JToolBar.Separator());
        add(optionPricerMenuItem);
        //add(clearTradeMenuItem); 
        add(new JToolBar.Separator());
        add(newFilterQuotesItem);
        add(editFilterQuotesItem);
        add(clearViewerItemMenuItem);
    }

    private JMenuItem getCopyToPitsCast() {
        JMenuItem copyToPitsCast = new JMenuItem();
        copyToPitsCast.setText("Paste to PBcast");
        copyToPitsCast.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner() != null) ){
                    if (targetQuoteWrapper.getQuoteOwner().getInstantMessage() != null){
                        viewer.getKernel().getPointBoxFace().getPointBoxTalker().displayPitsCastMessageFrameForCopyPasteQuoteMessage(targetQuoteWrapper.getQuoteOwner().getInstantMessage().getMessageContent());
                    }else{
                        JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote which contains instant messages.");
                    }
                }
            }
        });
        return copyToPitsCast;
    }

    private JMenuItem getSendByPitsCast() {
        JMenuItem sendByPitsCast = new JMenuItem();
        sendByPitsCast.setText("Send by PBcast");
        sendByPitsCast.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner() != null) ){
                    if (targetQuoteWrapper.getQuoteOwner().getInstantMessage() != null){
                        viewer.getKernel().getPointBoxFace().getPointBoxTalker().displayPitsCastMessageFrameForSendQuoteMessage(targetQuoteWrapper.getQuoteOwner().getInstantMessage().getMessageContent());
                    }else{
                        JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote which contains instant messages.");
                    }
                }
            }
        });
        return sendByPitsCast;
    }
    
    public void loadSendAndCopyItemInEDT(){
        if(SwingUtilities.isEventDispatchThread()){
            loadSendAndCopyItemsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    loadSendAndCopyItemsHelper();
                }
            });
        }
    }
    
    private void loadSendAndCopyItemsHelper(){
        if(sendToMenu==null){
            sendToMenu = new JMenu(ViewerTableTabPopupMenuTerms.sendToDistributionItem.toString());
            sendToMenu.setFont(SwingGlobal.getLabelFont());
            //add(sendToMenu); 
        }
        if(copyToMenu==null){
            copyToMenu = new JMenu(ViewerTableTabPopupMenuTerms.pasteToDistributionItem.toString());
            copyToMenu.setFont(SwingGlobal.getLabelFont());
            //add(copyToMenu);
        }
        sendToMenu.removeAll();
        copyToMenu.removeAll();
        
        copyToMenu.add(getCopyToPitsCast(), 0);
        sendToMenu.add(getSendByPitsCast(), 0);
        
        loadCopyAndSendToMenu(sendToMenu, false);
        loadCopyAndSendToMenu(copyToMenu, true);
    }
    
    private void loadCopyAndSendToMenu(final JMenu toMenu, final boolean isCopyTo) {
        final IPbcTalker talker = viewer.getKernel().getPointBoxFace().getPointBoxTalker();
        ArrayList<IGatewayConnectorGroup> groupList = talker.getPitsCastGroups();
        JMenuItem aGroupMenuItem;
        if ((groupList == null) || (groupList.isEmpty())){
            aGroupMenuItem = new JMenuItem();
            aGroupMenuItem.setText("No PBcast Group yet...");
            toMenu.add(aGroupMenuItem);
        }else{
            for (IGatewayConnectorGroup group : groupList){
                aGroupMenuItem = new JMenuItem();
                aGroupMenuItem.setText(group.getGroupName());
                final IGatewayConnectorGroup sendToGroup = group;
                aGroupMenuItem.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if ((targetQuoteWrapper != null) && (targetQuoteWrapper.getQuoteOwner() != null) ){
                            if (targetQuoteWrapper.getQuoteOwner().getInstantMessage() != null){
                                if (isCopyTo){
                                    talker.displayPitsCastMessageBoardForCopyTo(targetQuoteWrapper.getQuoteOwner().getInstantMessage().getMessageContent(), sendToGroup);
                                }else{
                                    talker.displayPitsCastMessageBoardForSendTo(targetQuoteWrapper.getQuoteOwner().getInstantMessage().getMessageContent(), sendToGroup);
                                }
                            }else{
                                JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote which contains instant messages.");
                            }
                        }else{
                            //JOptionPane.showMessageDialog(viewer.getPointBoxFrame(), "Please click on a valid option quote for this operation.");
                        }
                    }
                });
                toMenu.add(aGroupMenuItem);
            }
        }
    }
    
    public void enableScrollingViewer(){
        if (SwingUtilities.isEventDispatchThread()){
            enableScrollingViewerItem.setEnabled(false);
            disableScrollingViewerItem.setEnabled(true);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    enableScrollingViewerItem.setEnabled(false);
                    disableScrollingViewerItem.setEnabled(true);
                }
            });
        }
    }
    
    public void disableScrollingViewer(){
        if (SwingUtilities.isEventDispatchThread()){
            enableScrollingViewerItem.setEnabled(true);
            disableScrollingViewerItem.setEnabled(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    enableScrollingViewerItem.setEnabled(true);
                    disableScrollingViewerItem.setEnabled(false);
                }
            });
        }
    }
    
    public void enableFilterRename(){
        if (SwingUtilities.isEventDispatchThread()){
            editFilterQuotesItem.setEnabled(true);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    editFilterQuotesItem.setEnabled(true);
                }
            });
        }
    }
    
    public void disableFilterRename(){
        if (SwingUtilities.isEventDispatchThread()){
            editFilterQuotesItem.setEnabled(false);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    editFilterQuotesItem.setEnabled(false);
                }
            });
        }
    }

    public final void initializeDisplaySimilarRow()
    {
       displaySimilarRowSub.removeAll();
        ArrayList messages = PointBoxHiddenMessagesProperties.getSingleton(viewer.getKernel()).getHiddenMessageMenu();
        JMenuItem tempItem;
        if(messages != null && !(messages.isEmpty()))
        {
            for(int index=0; index < messages.size(); index++)
            {
                String shortMessage;
                int messageLength = 20;
                if(messages.get(index).toString().length() < messageLength) {
                    shortMessage = messages.get(index).toString();
                }
                else {
                    shortMessage = messages.get(index).toString().substring(0, messageLength) + "...";
                }
                tempItem = new JMenuItem(shortMessage);
                tempItem.setName(messages.get(index).toString());
                tempItem.addActionListener(new DispSimRowActionListener(this));
                displaySimilarRowSub.add(tempItem);
            }
            //add(displaySimilarRowSub);
            displaySimilarRowSub.setEnabled(true);
        }
        else
        {
            //add(displaySimilarRowSub);
            displaySimilarRowSub.setEnabled(false);
        }
    }
    private void hideSimilarRow(ActionEvent e) {
        String output = targetQuoteWrapper.getQuoteMessageFaceValue();
        long timestamp = targetQuoteWrapper.getTimestampFaceValue().getTime().getTime();
        if (output != null){
            PointBoxHiddenMessagesProperties.getSingleton(viewer.getKernel()).addProperties(output, timestamp);
            viewer.refreshViewer();
            initializeDisplaySimilarRow();
            this.updateUI();
        }
    }

    private void exportQuote(ActionEvent e) {
        if (targetQuoteWrapper == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            exportQuoteHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    exportQuoteHelper();
                }
            });
        }
    }

    private void exportQuoteHelper(){
        final String outputFilePath = getFileFromFileChooser();
        (new Thread(new Runnable(){
            @Override
            public void run() {
                ArrayList<String> outputList = new ArrayList<String>();
                if (targetQuoteWrapper.getQuoteOwner() != null){
                    outputList.add(targetQuoteWrapper.getQuoteOwner().createExportQuoteFieldString());
                    if (!outputFilePath.isEmpty()){
                        ExportFileWriter exportFileWriter = new ExportFileWriter(outputList, outputFilePath);
                        exportFileWriter.write(false);
                    }
                }
            }
        })).start();
    }

    private String getFileFromFileChooser() {
        String result = "";
        PbsysFileFilter filter = new PbsysFileFilter("txt");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(viewer.getPointBoxFrame()) == JFileChooser.APPROVE_OPTION) {
            result = fileChooser.getSelectedFile().getAbsolutePath();
        }

        if (!result.isEmpty() && !result.substring(result.lastIndexOf("."),result.length()).equalsIgnoreCase(filter.getDescription())){
            return result + filter.getDescription();
        }
        return result;
    }

    private void exportAllQuotes(ActionEvent e) {
        if (SwingUtilities.isEventDispatchThread()){
            exportAllQuotesHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    exportAllQuotesHelper();
                }
            });
        }
    }

    private void exportAllQuotesHelper(){
        final String outputFilePath = getFileFromFileChooser();
        (new Thread(new Runnable(){
            @Override
            public void run() {
                ArrayList<String> outputList = new ArrayList<String>();
                if (!outputFilePath.isEmpty()){
                    ArrayList<IPbsysOptionQuote> quotes = viewer.retrieveAllQuotes();
                    for (IPbsysOptionQuote quote : quotes){
                        if (quote.isSufficientPricingData()){
                            outputList.add(quote.createExportQuoteFieldString());
                        }
                    }
                    ExportFileWriter exportFileWriter = new ExportFileWriter(outputList, outputFilePath);
                    exportFileWriter.write(false);
                }
            }
        })).start();
    }

    void setTargetQuoteWrapper(IPbsysOptionQuoteWrapper quoteWrapper) {
        targetQuoteWrapper = quoteWrapper;
    }

    enum ViewerTableTabPopupMenuTerms{
        clearViewerItem("Clear"),
        copyToClipboardItem("Copy"),
        pasteToDistributionItem("Paste To"),
        sendToDistributionItem("Send To"),
        optionPricerItem("Option Pricer"),
        newFilterQuoteShortcutItems("New Filter"),
        enableScrollingViewerItem("Enable Automatic Scrolling"),
        disableScrollingViewerItem("Disable Automatic Scrolling"),
        clearTradeinClearPort("Clearport"),
        editFilterQuoteShortcutItem("Edit Filter"),
        hideSimilarRow("Hide Similar Row"),
        displaySimilarRow("Display Similar Row"),
        fixQuote("Fix Quote"),
        exportSingleRowIntoTextItem("Export This Quote"),
        exportAllRowsIntoTextItem("Export All Quotes"),
        FloatingFrame("Floating Windows"),
        removeSelectedQuotes("Remove Selected Quotes"),
        logProblematicQuote("Log Problematic Quote");

        private String term;
        ViewerTableTabPopupMenuTerms(String term){
            this.term = term;
        }
        @Override
        public String toString() {
            return term;
        }
    }

    private class ExportFileWriter{
        private String exportedString;
        private FileWriter outputFileWriter;
        private PrintWriter outputStream;
        private String filePath;
        private ArrayList<String> exportStringList;

        public ExportFileWriter(ArrayList<String> exportStringList, String filePath){
            this.exportStringList = exportStringList;
            this.filePath = filePath;
        }

        public synchronized void write(boolean append){
            try{
                //File selectedPathFile = new File(filePath);
                //outputFileWriter = new FileWriter(filePath, selectedPathFile.exists());
                outputFileWriter = new FileWriter(filePath, append);
                BufferedWriter bufferedWriter = new BufferedWriter(outputFileWriter);

                for (int i = 0; i < exportStringList.size(); i++){
                    bufferedWriter.write(exportStringList.get(i));
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
                /*
                outputStream = new PrintWriter(new FileOutputStream(selectedPathFile), selectedPathFile.exists());
                outputStream.println(exportedString);

                outputStream.close();*/
            }
            catch(IOException e){
                //NIOGlobal.printMessage("trouble in exporting quotes from row to file");
            }
        }
    }
    class PbsysFileFilter extends FileFilter {
        private String ext;

        public PbsysFileFilter(String ext) {
            ext = ext.toLowerCase().replaceAll("\\*", "").trim();
            ext = ext.replaceAll("\\.", "").trim();
            this.ext = "." + ext;

        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()){
                return true;
            }
            if (f.isFile() && f.getName().toLowerCase().endsWith(ext)){
                return true;
            }else{
                return false;
            }
        }

        @Override
        public String getDescription() {
            return ext;
        }
    }

    private class DispSimRowActionListener implements ActionListener {
        private ViewerTableTabPopupMenu menu;

        DispSimRowActionListener(ViewerTableTabPopupMenu aMenu)
        {
            menu = aMenu;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String fullMessage = ((JMenuItem)e.getSource()).getName();
            if (fullMessage != null){
                PointBoxHiddenMessagesProperties.getSingleton(viewer.getKernel()).removeProperties(fullMessage);
                viewer.refreshViewer();
                menu.initializeDisplaySimilarRow();
                menu.updateUI();
            //menu.
            }
        }
    }
}

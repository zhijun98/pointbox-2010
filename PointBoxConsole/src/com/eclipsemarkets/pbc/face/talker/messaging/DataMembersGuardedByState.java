/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.gateway.data.IBroadcastedMessage;
import com.eclipsemarkets.gateway.data.IPbsysInstantMessage;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.user.BuddyStatus;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pbc.face.talker.IMessagingBoardState;
import static com.eclipsemarkets.pbc.face.talker.messaging.MessagingBoardState.LINK_ATTRIBUTE;
import com.eclipsemarkets.pbc.runtime.settings.record.IMessageTabRecord;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Zhijun Zhang, date & time: Jan 14, 2014 - 3:34:11 PM
 */
class DataMembersGuardedByState {
    
    private static final Logger logger = Logger.getLogger(DataMembersGuardedByState.class.getName());
    
    /**
     * key: pbcMessageUuid; value: quote-messages
     */
    private final HashMap<String, IPbsysOptionQuote> publishedQuoteMessages;
    /**
     * Record the sequence of messages published on the document
     */
    private final LinkedList<String> pbcMessageUuidSequence;
    /**
     * Record each inserted message line has how many characters
     */
    private final HashMap<String, Integer> msgLengths;

    /**
     * This data structure cannot be exposed to the outside
     */
    private final DefaultStyledDocument historyDocument;

    /**
     * Data entry of the messenger
     */
    private final DefaultStyledDocument entryDocument;

    private boolean archiveWarningMessageRequired;
    
    private final IMessagingBoardState ownerState;
    
    private SimpleAttributeSet lineFeedFormat = SwingGlobal.BLACK_MESSAGE;
    private SimpleAttributeSet priceFontFormat = SwingGlobal.HIGHLIGHTED_PRICE;

    DataMembersGuardedByState(IMessagingBoardState ownerState) {
        this.ownerState = ownerState;
        historyDocument = new DefaultStyledDocument();
        entryDocument = new DefaultStyledDocument();
        publishedQuoteMessages = new HashMap<String, IPbsysOptionQuote>();
        pbcMessageUuidSequence = new LinkedList<String>();
        msgLengths = new HashMap<String, Integer>();
        archiveWarningMessageRequired = true;
    }
    private synchronized int getHistoryDocumentLength(DefaultStyledDocument doc){
        int len = doc.getLength();
        if ((len < 0) || (len > Integer.MAX_VALUE)){
            len = 0;
        }
        return len;
    }

    synchronized void insertMessageLine(final IPbsysOptionQuote currentQuoteMessage, 
                                        final IMessageTabRecord messageTabRecord) 
            throws BadLocationException
    {
        //record quote for refreshing price
        if (!recordPublishedQuote(currentQuoteMessage)){
            return;
        }
        IPbsysInstantMessage msg = currentQuoteMessage.getInstantMessage();
        //find the location for the quote
        Iterator<String> itr = pbcMessageUuidSequence.descendingIterator();
        IPbsysOptionQuote preQuoteMessage;
        IPbsysInstantMessage preMsg;
        ArrayList<String> preUuids = new ArrayList<String>();
        while (itr.hasNext()){
            preQuoteMessage = publishedQuoteMessages.get(itr.next());
            if (preQuoteMessage == null){
                PointBoxTracer.recordSevereException(logger, new Exception("[Tech Bug] preQuoteMessage should be found in publishedQuoteMessages"));
                //break;
            }else{
                preMsg = preQuoteMessage.getInstantMessage();
                if (preMsg == null){
                    PointBoxTracer.recordSevereException(logger, new Exception("[Tech Bug] preQuoteMessage.getInstantMessage() cannot be NULL"));
                    //break;
                }else{
                    if (msg.getMessageTimestamp().before(preMsg.getMessageTimestamp())){
                        preUuids.add(preMsg.getPbcMessageUuid());
                    }else{
                        break;
                    }
                }
            }
        }//while
        //remove preQuoteMessages
        int msgLen;
        int docLen;
        for (String preUuid : preUuids){
            msgLen = msgLengths.get(preUuid);
            docLen = getHistoryDocumentLength(historyDocument);
            if (docLen-msgLen-1 >= 0){
                if (msgLen > 0){
                    historyDocument.remove(docLen-msgLen-1, msgLen);
                }
            }else{
                if (msgLen > 0){
                    historyDocument.remove(0, msgLen);
                }
            }
            pbcMessageUuidSequence.removeLast();
            msgLengths.remove(preUuid);
        }
        //insert the current message
        try{
            insertMessageLineHelper(currentQuoteMessage, messageTabRecord);
        }catch (Throwable ex){
//            PointBoxTracer.recordSevereThrowable(logger, ex, true);
        }
        //re-insert preQuoteMessages
        for (String preUuid : preUuids){
            try{
                insertMessageLineHelper(publishedQuoteMessages.get(preUuid), messageTabRecord);
            }catch(Exception ex){
                //PointBoxTracer.recordSevereException(logger, ex, true);
            }
        }
    }

    private synchronized void changetoLinkFormat(DefaultStyledDocument paramHistoryDocument, String messageContent){
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        //create the style for the hyperlink
        Style regularBlue = paramHistoryDocument.addStyle("regularBlue", def);
        StyleConstants.setForeground(regularBlue, Color.BLUE);
        StyleConstants.setUnderline(regularBlue,true);

        Map<Integer,String> map= extractUrls(messageContent);

        for(int index:map.keySet()){
            String url=map.get(index);
            try {
                regularBlue.addAttribute(LINK_ATTRIBUTE,new URLLinkAction(url));
                paramHistoryDocument.replace(paramHistoryDocument.getLength()-messageContent.length()+index, url.length(), url, regularBlue);
            } catch (BadLocationException ex) {
                Logger.getLogger(MessagingBoardState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private synchronized Map<Integer,String> extractUrls(String input) {
        Map<Integer,String> result = new TreeMap<Integer,String>();

        String regex=new StringBuilder() 
            .append("((?:(http|https|Http|Https|rtsp|Rtsp):") 
            .append("\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)") 
                    .append("\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_") 
                    .append("\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?") 
                    .append("((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+")   // named host 
                    .append("(?:")   // plus top level domain 
                    .append("(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])") 
                    .append("|(?:biz|b[abdefghijmnorstvwyz])") 
                    .append("|(?:cat|com|coop|c[acdfghiklmnoruvxyz])") 
                    .append("|d[ejkmoz]") 
                    .append("|(?:edu|e[cegrstu])") 
                    .append("|f[ijkmor]") 
                    .append("|(?:gov|g[abdefghilmnpqrstuwy])") 
                    .append("|h[kmnrtu]") 
                    .append("|(?:info|int|i[delmnoqrst])") 
                    .append("|(?:jobs|j[emop])") 
                    .append("|k[eghimnrwyz]") 
                    .append("|l[abcikrstuvy]") 
                    .append("|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])") 
                    .append("|(?:name|net|n[acefgilopruz])") 
                    .append("|(?:org|om)") 
                    .append("|(?:pro|p[aefghklmnrstwy])") 
                    .append("|qa") 
                    .append("|r[eouw]") 
                    .append("|s[abcdeghijklmnortuvyz]") 
                    .append("|(?:tel|travel|t[cdfghjklmnoprtvwz])") 
                    .append("|u[agkmsyz]") 
                    .append("|v[aceginu]") 
                    .append("|w[fs]") 
                    .append("|y[etu]") 
                    .append("|z[amw]))") 
                    .append("|(?:(?:25[0-5]|2[0-4]") // or ip address 
                    .append("[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]") 
                    .append("|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]") 
                    .append("[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}") 
                    .append("|[1-9][0-9]|[0-9])))") 
                    .append("(?:\\:\\d{1,5})?)") // plus option port number 
                    .append("(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~")  // plus option query params 
                    .append("\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?") 
                    .append("(?:\\b|$)").toString();

        //Pattern pattern = Pattern.compile("((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)");  
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.put(matcher.start(),matcher.group());
        }
        return result;
    }        


    private synchronized void insertMessageLineHelper(final IPbsysOptionQuote currentQuoteMessage, 
                                                      final IMessageTabRecord messageTabRecord) 
            throws BadLocationException
    {
        IPbsysInstantMessage message = currentQuoteMessage.getInstantMessage();
        int originalLength = getHistoryDocumentLength(historyDocument);
        
        executeMessageLineInsertion(historyDocument, 
                                    currentQuoteMessage, 
                                    messageTabRecord);
        /**
         * Record this just-published message's length and sequence 
         */
        msgLengths.put(message.getPbcMessageUuid(), getHistoryDocumentLength(historyDocument) - originalLength);
        pbcMessageUuidSequence.addLast(message.getPbcMessageUuid());
    }
    
    private synchronized void executeMessageLineInsertion(final DefaultStyledDocument doc, 
                                                          final IPbsysOptionQuote currentQuoteMessage, 
                                                          final IMessageTabRecord messageTabRecord)
            throws BadLocationException
    {
    
        if (currentQuoteMessage == null){
            /**
             * This case is possible when archive-warning-message is NULL
             */
            return;
        }
        IPbsysInstantMessage message = currentQuoteMessage.getInstantMessage();
        //insert text....
        if (message.isOutgoing()){
            if (messageTabRecord.isDisplayTimestamp()){
                doc.insertString(getHistoryDocumentLength(doc),
                                    message.getFromUser().getIMScreenName() + " [" + CalendarGlobal.convertToHHmmss(message.getMessageTimestamp(), ":") + "] ",
                                    ownerState.getOutgoingUserFontFormat());

            }else{
                doc.insertString(getHistoryDocumentLength(doc),
                                    message.getFromUser().getIMScreenName() + " - ",
                                    ownerState.getOutgoingUserFontFormat());
            }
            if (BuddyStatus.Online.equals(message.getToUser().getBuddyStatus())){
                doc.insertString(getHistoryDocumentLength(doc),
                                    message.getMessageContent(),
                                    ownerState.getOutgoingMessageFontFormat());
            }else{
                //todo: there is timing issue here because when this method is called, the buddy may became online
                if (message instanceof IBroadcastedMessage){
                    doc.insertString(getHistoryDocumentLength(doc),
                                        "[OFFLINE, NOT SENT] - " + message.getMessageContent(),
                                        ownerState.getOutgoingMessageFontFormat());
                }else{
                    doc.insertString(getHistoryDocumentLength(doc),
                                        message.getMessageContent(),
                                        ownerState.getOutgoingMessageFontFormat());
                }
            }
        }else{
            if (messageTabRecord.isDisplayTimestamp()){
                doc.insertString(getHistoryDocumentLength(doc),
                                    message.getFromUser().getIMScreenName() + " <" + CalendarGlobal.convertToHHmmss(message.getMessageTimestamp(), ":") +"> ",
                                    ownerState.getIncomingUserFontFormat());
            }else{
                doc.insertString(getHistoryDocumentLength(doc),
                                    message.getFromUser().getIMScreenName() + " - ",
                                    ownerState.getIncomingUserFontFormat());
            }
            if (BuddyStatus.Online.equals(message.getFromUser().getBuddyStatus())){
                doc.insertString(getHistoryDocumentLength(doc),
                                    message.getMessageContent(),
                                    ownerState.getIncomingMessageFontFormat());
            }else{
                //todo: there is timing issue here because when this method is called, the buddy may became online
                if (message instanceof IBroadcastedMessage){
                    doc.insertString(getHistoryDocumentLength(doc),
                                        "[OFFLINE, NOT SENT] - " + message.getMessageContent(),
                                        ownerState.getOutgoingMessageFontFormat());
                }else{
                    doc.insertString(getHistoryDocumentLength(doc),
                                        message.getMessageContent() + " ",
                                        ownerState.getOutgoingMessageFontFormat());
                }
            }
        }
        changetoLinkFormat(doc,message.getMessageContent());
        //display prices of the quote
        if (messageTabRecord.isDisplayPrices()){
            if (currentQuoteMessage.isSufficientPricingData()){
                doc.insertString(
                        getHistoryDocumentLength(doc)," ",null);  //add a white space after the quote
                doc.insertString(
                        getHistoryDocumentLength(doc),
                        " " + PbcGlobal.localFormatStringByDoublePrecision(Math.abs(currentQuoteMessage.getPrice()), 4, "0") + " ",
                        priceFontFormat);
            }
        }
        //insert line-separator
        doc.insertString(getHistoryDocumentLength(doc),
                            NIOGlobal.lineSeparator(),
                            lineFeedFormat);
    }

    /**
     * simply record the quote which will be inserted into the document
     * @param quote
     * @return 
     */
    synchronized boolean recordPublishedQuote(IPbsysOptionQuote quote) {
        if ((quote == null) || ((quote.getInstantMessage() == null))){
            return false;
        }
        if (publishedQuoteMessages.containsKey(quote.getInstantMessage().getPbcMessageUuid())){
            return false;
        }
        publishedQuoteMessages.put(quote.getInstantMessage().getPbcMessageUuid(), quote);
        return true;
    }

    synchronized ArrayList<IPbsysOptionQuote> getPublishedQuotesForPriceRefresh() {
        Collection<IPbsysOptionQuote> quotes = publishedQuoteMessages.values();
        ArrayList<IPbsysOptionQuote> quoteList = new ArrayList<IPbsysOptionQuote>();
        for (IPbsysOptionQuote aQuote : quotes){
            quoteList.add(aQuote);
        }
        publishedQuoteMessages.clear();
        pbcMessageUuidSequence.clear();
        msgLengths.clear();
        return quoteList;
    }

    synchronized void cleanupHistoryDocument() {
        try {
            historyDocument.remove(0, getHistoryDocumentLength(historyDocument));
        } catch (BadLocationException ex) {
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }

    synchronized void cleanupEntryDocument() {
        try {
            entryDocument.remove(0, entryDocument.getLength());
        } catch (BadLocationException ex) {
            PointBoxTracer.recordSevereException(logger, ex);
        }
    }

    synchronized boolean isArchiveWarningMessageRequired() {
        return archiveWarningMessageRequired;
    }

    synchronized void setArchiveWarningMessageRequired(boolean archiveWarningMessageRequired) {
        this.archiveWarningMessageRequired = archiveWarningMessageRequired;
    }

    synchronized void loadMessagingEntryDocument(JTextPane messagingEntry) {
        if (messagingEntry != null){
            messagingEntry.setDocument(entryDocument);
        }
    }

    synchronized void loadMessagingHistoryDocument(JTextPane messagingHistory) {
        if (messagingHistory != null){
            messagingHistory.setDocument(historyDocument);
        }
    }

//    synchronized void scrollToMessagingHistoryBottom(JTextPane messagingHistory) {
//        int len  = getHistoryDocumentLength(historyDocument);
//        if(len == 0){
//            return;
//        }
//        messagingHistory.setCaretPosition(len);
//    }

    synchronized Document getCopyOfMessagingHistoryDefaultStyledDocument(IMessageTabRecord record) {
        if (record == null){
            return new DefaultStyledDocument();
        }
        Collection<IPbsysOptionQuote> quotes = publishedQuoteMessages.values();
        //get all the published quote messages...
        ArrayList<IPbsysOptionQuote> quoteList = new ArrayList<IPbsysOptionQuote>();
        for (IPbsysOptionQuote aQuote : quotes){
            quoteList.add(aQuote);
        }
        //sort it in a sequence...
        Collections.sort(quoteList, new PbsysOptionQuoteComparator());
        DefaultStyledDocument aDefaultStyledDocument = new DefaultStyledDocument();
        for (IPbsysOptionQuote quote : quoteList){
            try {
                executeMessageLineInsertion(aDefaultStyledDocument, quote, record);
            } catch (BadLocationException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }//for
        return aDefaultStyledDocument;
    }
    
    static class URLLinkAction extends AbstractAction{
        private String url;

        URLLinkAction(String bac)
        {
            url=bac;
        }

        protected void execute() {
            try {
                String osName = System.getProperty("os.name").toLowerCase();
                Runtime rt = Runtime.getRuntime();
                if (osName.indexOf( "win" ) >= 0) {
                        rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
                } else if (osName.indexOf("mac") >= 0) {
                        rt.exec( "open " + url);
                } else if (osName.indexOf("ix") >=0 || osName.indexOf("ux") >=0 || osName.indexOf("sun") >=0) {
                    String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
                            "netscape","opera","links","lynx"};

                    // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                    StringBuilder cmd = new StringBuilder();
                    for (int i = 0 ; i < browsers.length ; i++){
                        cmd.append(i == 0  ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
                    }

                    rt.exec(new String[] { "sh", "-c", cmd.toString() });
                }
            }
            catch (Exception ex)
            {
            }
        }

        @Override
        public void actionPerformed(ActionEvent e){
            execute();
        }
    } 
}

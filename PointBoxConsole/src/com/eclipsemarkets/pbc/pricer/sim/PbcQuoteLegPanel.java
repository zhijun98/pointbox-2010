/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.IPointBoxQuoteMonthFirstDate;
import com.eclipsemarkets.data.PointBoxCalendarFactory;
import com.eclipsemarkets.data.PointBoxOption;
import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.data.PointBoxQuoteStrategyTerm;
import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import com.eclipsemarkets.gateway.data.IPbsysQuoteLeg;
import com.eclipsemarkets.gateway.data.PbsysQuoteLegCrossStatus;
import com.eclipsemarkets.gateway.data.PointBoxOptionPosition;
import com.eclipsemarkets.gateway.data.PointBoxQuotePrices;
import com.eclipsemarkets.gateway.web.QuoteLegState;
import com.eclipsemarkets.gateway.web.QuoteLegValueState;
import com.eclipsemarkets.global.CalendarGlobal;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.RegexGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.parser.PbcSimGuiParser;
import com.eclipsemarkets.pbc.PbcGlobal;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Zhijun Zhang
 */
class PbcQuoteLegPanel extends javax.swing.JPanel implements PropertyChangeListener{

    private static final String LEG_ID_PREFIX;
    
    private static final ArrayList<Color> legTabNameBkgColorList;
    static{
        LEG_ID_PREFIX = "Leg ";
        legTabNameBkgColorList = new ArrayList<Color>();
        legTabNameBkgColorList.add(Color.YELLOW);
        legTabNameBkgColorList.add(Color.GREEN);
    }
    
    
    private final PbcQuoteFrame owner;
    
    private final EnumSet<PointBoxQuoteStrategyTerm> structureSet;
    
    private ArrayList<JFormattedTextField> jStrikeFieldList = new ArrayList<JFormattedTextField>();
    private ArrayList<JFormattedTextField> jRatioFieldList = new ArrayList<JFormattedTextField>();
    private ArrayList<JLabel> jPriceFieldList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jVoltList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jDeltaList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jDdeltaList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jVegaList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jThetaList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jGammaList = new ArrayList<JLabel>();
    private ArrayList<JLabel> jDgammaList = new ArrayList<JLabel>();
    private ArrayList<JComboBox> jCallPutSelectorList = new ArrayList<JComboBox>();
    
    private double previousUserInputCross;
    
    private double oldCrossField;
    private double oldSwapField;

    private double oldStrikeField1;
    private double oldStrikeField2;
    private double oldStrikeField3;
    private double oldStrikeField4;
    private double oldStrikeField5;

    private double oldRatioField1;
    private double oldRatioField2;
    private double oldRatioField3;
    private double oldRatioField4;
    private double oldRatioField5;
    
    private String[] callPutOptions = {"", "c", "p"};
    
    /**
     * start from 1 not 0
     */
    private int legId;
    
    private final JFormattedTextField jCrossTextField = new JFormattedTextField(new DefaultFormatter());
    
    /**
     * Creates new form PbcQuoteLegPanel
     * 
     * @param legId - starting from 1
     * @param owner 
     */
    PbcQuoteLegPanel(int legId, PbcQuoteFrame owner) {
        this.structureSet = EnumSet.allOf(PointBoxQuoteStrategyTerm.class);
        //structureSet.remove(PointBoxQuoteStrategyTerm.CUSTOM);
        initComponents();
        
        jAdjField.setVisible(false);

        
        this.owner = owner;
        this.legId = legId;
        
        replaceCrossSpinner();
        
        setupLegTabNameLabel(legId);
        setupStartAndEndSelectors(owner.getSelectedPointBoxQuoteCode());
        setupStructureSelector();
        setupCrossSwapAgjFieldsWithListeners();
        setupStrikeRatioCallPutFieldsWithListeners();
        setupOptionPriceTable();
        
    }
    
    private DefaultFormatterFactory createDefaultFormatterFactoryForSelectedCode(){
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
        numberFormat.setMaximumFractionDigits(owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
        return new DefaultFormatterFactory(new NumberFormatter(numberFormat));
    }
    
    private void setupCrossSwapAgjFieldsWithListeners(){
//        JFormattedTextField jCrossFormattedTextField = ((JSpinner.NumberEditor)jCrossSpinner.getEditor()).getTextField();
//        jCrossFormattedTextField.setInputVerifier(new SimFormattedValueInputVerifier());
//        jCrossFormattedTextField.setEditable(false);
//        jCrossFormattedTextField.addPropertyChangeListener("value", this);
//        jCrossFormattedTextField.setValue(0.0000);
//        try{
//            jSwapField.setFormatterFactory(createDefaultFormatterFactoryForSelectedCode());
//        }catch(Exception ex){}
        jSwapField.addPropertyChangeListener("value", this);
        
        jLiveCheck.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                resetCrossTextField();          
                owner.populateQuoteMessageTextField();
            }
        });    
    
    }
    
//    private void resetCrossSpinner(){
//        JFormattedTextField jCrossFormattedTextField = ((JSpinner.NumberEditor)jCrossSpinner.getEditor()).getTextField();
//        if (jLiveCheck.isSelected()){
//            previousUserInputCross = jCrossFormattedTextField.getValue().toString();
//            jCrossFormattedTextField.setEditable(false);
//            jCrossSpinner.setEnabled(false);
//            jCrossFormattedTextField.setValue(0.0000);
//        }else{
//            jCrossFormattedTextField.setEditable(true);
//            jCrossSpinner.setEnabled(true);
//            jCrossFormattedTextField.setValue(Double.parseDouble(previousUserInputCross));
//        }
//    }
    
    private void setupStrikeRatioCallPutFieldsWithListeners(){

        jStrikeField1.setInputVerifier(new SimFormattedValueInputVerifier());
        jStrikeField2.setInputVerifier(new SimFormattedValueInputVerifier());
        jStrikeField3.setInputVerifier(new SimFormattedValueInputVerifier());
        jStrikeField4.setInputVerifier(new SimFormattedValueInputVerifier());
        jStrikeField5.setInputVerifier(new SimFormattedValueInputVerifier());

        jRatioField1.setInputVerifier(new SimFormattedValueInputVerifier());
        jRatioField2.setInputVerifier(new SimFormattedValueInputVerifier());
        jRatioField3.setInputVerifier(new SimFormattedValueInputVerifier());
        jRatioField4.setInputVerifier(new SimFormattedValueInputVerifier());
        jRatioField5.setInputVerifier(new SimFormattedValueInputVerifier());

        jStrikeField1.addPropertyChangeListener("value", this);
        jStrikeField2.addPropertyChangeListener("value", this);
        jStrikeField3.addPropertyChangeListener("value", this);
        jStrikeField4.addPropertyChangeListener("value", this);
        jStrikeField5.addPropertyChangeListener("value", this);

        jRatioField1.addPropertyChangeListener("value", this);
        jRatioField2.addPropertyChangeListener("value", this);
        jRatioField3.addPropertyChangeListener("value", this);
        jRatioField4.addPropertyChangeListener("value", this);
        jRatioField5.addPropertyChangeListener("value", this);

        jStrikeField1.setValue("");
        jStrikeField2.setValue("");
        jStrikeField3.setValue("");
        jStrikeField4.setValue("");
        jStrikeField5.setValue("");

        jRatioField1.setValue("");
        jRatioField2.setValue("");
        jRatioField3.setValue("");
        jRatioField4.setValue("");
        jRatioField5.setValue("");

        jStrikeFieldList = new ArrayList<JFormattedTextField>();
        jStrikeFieldList.add(jStrikeField1);
        jStrikeFieldList.add(jStrikeField2);
        jStrikeFieldList.add(jStrikeField3);
        jStrikeFieldList.add(jStrikeField4);
        jStrikeFieldList.add(jStrikeField5);
        
        jRatioFieldList = new ArrayList<JFormattedTextField>();
        jRatioFieldList.add(jRatioField1);
        jRatioFieldList.add(jRatioField2);
        jRatioFieldList.add(jRatioField3);
        jRatioFieldList.add(jRatioField4);
        jRatioFieldList.add(jRatioField5);
        
        initStrikeRatioListenersHelper(jStrikeField1, jRatioField1);
        initStrikeRatioListenersHelper(jStrikeField2, jRatioField2);
        initStrikeRatioListenersHelper(jStrikeField3, jRatioField3);
        initStrikeRatioListenersHelper(jStrikeField4, jRatioField4);
        initStrikeRatioListenersHelper(jStrikeField5, jRatioField5);
        
        jCallPutSelectorList = new ArrayList<JComboBox>();
        jCallPutSelectorList.add(jCallPutSelector1);
        jCallPutSelectorList.add(jCallPutSelector2);
        jCallPutSelectorList.add(jCallPutSelector3);
        jCallPutSelectorList.add(jCallPutSelector4);
        jCallPutSelectorList.add(jCallPutSelector5);
    }
    
    private void initStrikeRatioListenersHelper(final JTextField S, final JTextField R) {
        S.addFocusListener(new FocusListener(){
           @Override
           public void focusGained(FocusEvent e) {
               S.selectAll();
           }

           @Override
            public void focusLost(FocusEvent e) {
            }
        });
        S.getDocument().addDocumentListener(new DocumentListener(){
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateRatioField();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateRatioField();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateRatioField();
            }

            private void updateRatioField(){
                if (R.getText().isEmpty()){
                    R.setText("1.0");
                }
            }
        });
    }
    
    private void setupStartAndEndSelectors(final PointBoxQuoteCode selectedCode){
        if (SwingUtilities.isEventDispatchThread()){
            setupStartAndEndSelectorHelper(selectedCode);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    setupStartAndEndSelectorHelper(selectedCode);
                }
            });
        }
    }
    
    private void setupStartAndEndSelectorHelper(final PointBoxQuoteCode selectedCode){
        setupDateSelectorHelper(jStartSelector, selectedCode);
        setupDateSelectorHelper(jEndSelector, selectedCode);
    }

    String generateLegMessageToken() {
        String structure = getStructureValue(false);
        int num = getNumberOfStrikeRatios(structure);
        String result = "";
        if (this.isCustomStructure(structure)){
            result += generateContractPeriodToken() 
                    + PbcSimGuiParser.WhiteSpace + generateStrikeMessageToken(num)
                    + PbcSimGuiParser.WhiteSpace + generateOptionCallPutToken(num)
                    + PbcSimGuiParser.WhiteSpace + generateStructureToken(structure) 
                    + PbcSimGuiParser.WhiteSpace 
                    + (generateRatioMessageToken(num)+ PbcSimGuiParser.WhiteSpace + generateCrossMessage()).trim();
        }else{
            result += generateContractPeriodToken() 
                    + PbcSimGuiParser.WhiteSpace + generateStrikeMessageToken(num)
                    + PbcSimGuiParser.WhiteSpace + generateStructureToken(structure) 
                    + PbcSimGuiParser.WhiteSpace 
                    + (generateRatioMessageToken(num) + PbcSimGuiParser.WhiteSpace + generateCrossMessage()).trim();
        }
        result = result.trim();
        return result;
    }
    
    private String generateStructureToken(String structure){
        if (DataGlobal.isEmptyNullString(structure)){
            return "[Structure?]";
        }else{
            return structure.toUpperCase();
        }
    }
    
    private String generateCrossMessage(){
        String crossMessage = "";
        
        if(!jLiveCheck.isSelected()){
            if (isValidCross(jCrossTextField.getText())){
                crossMessage = PbcSimGuiParser.XCross + (DataGlobal.formatDoubleWithMinMax(Double.parseDouble(jCrossTextField.getText().trim()),
                                                         owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                         owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            }
        }else{
            if (!PointBoxQuoteStrategyTerm.isLiveHiddenRequired(getStructureValue(false))){
                crossMessage = PbcSimGuiParser.LiveCross;
            }
        }
      
        return crossMessage;
    }

    private boolean isValidCross(String txt){
         boolean validCross = false;

         if (!txt.isEmpty()){
             validCross = RegexGlobal.isNumberString(txt.trim());
         }

         return validCross;
    }    
    
    private String generateRatioMessageToken(int num){
        String ratioXratioMessage = null;
        HashSet<String> tokenSet = new HashSet<String>();
        String token;
        for (int i = 0; i < num; i++){
            token = jRatioFieldList.get(i).getText();
            if (token != null){
                token = token.trim();
            }
            tokenSet.add(token);
            if (ratioXratioMessage == null){
                ratioXratioMessage = token;
            }else{
                if (ratioXratioMessage.startsWith("-")){
                    ratioXratioMessage = ratioXratioMessage.substring(1);
                }
                ratioXratioMessage = ratioXratioMessage + PbcSimGuiParser.RatioDelimiter + token;
            }
        }//for
        if ((tokenSet.isEmpty()) || (tokenSet.size() == 1)){
            return PbcSimGuiParser.WhiteSpace;
        }else{
            ratioXratioMessage = ratioXratioMessage.replaceAll("-", "");
            boolean hideText = false;
            String structure = getStructureValue(false);
            String stdRatioText = "";
            if ((PointBoxQuoteStrategyTerm.STRDL.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.STRNGL.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.FENCE.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.CSPRD.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.PSPRD.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.CONV.toString().equalsIgnoreCase(structure)))
            {
                stdRatioText = "1.0" + PbcSimGuiParser.RatioDelimiter + "1.0";
            }else if ((PointBoxQuoteStrategyTerm.CFLY.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.PFLY.toString().equalsIgnoreCase(structure)))
            {
                stdRatioText = "1.0" + PbcSimGuiParser.RatioDelimiter + "2.0" + PbcSimGuiParser.RatioDelimiter + "1.0";
            }else if ((PointBoxQuoteStrategyTerm.CTREE.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.PTREE.toString().equalsIgnoreCase(structure)))
            {
                stdRatioText = "1.0" + PbcSimGuiParser.RatioDelimiter + "1.0" + PbcSimGuiParser.RatioDelimiter + "1.0";
            }else if ((PointBoxQuoteStrategyTerm.IFLY.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.CCNDR.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.PCNDR.toString().equalsIgnoreCase(structure))
                    || (PointBoxQuoteStrategyTerm.ICNDR.toString().equalsIgnoreCase(structure)))
            {
                stdRatioText = "1.0" + PbcSimGuiParser.RatioDelimiter + "1.0" + PbcSimGuiParser.RatioDelimiter + "1.0" + PbcSimGuiParser.RatioDelimiter + "1.0";
            }
            if (stdRatioText.equalsIgnoreCase(ratioXratioMessage)){
                hideText = true;
            }
            if (hideText){
                return "";
            }else{
                return ratioXratioMessage;
            }
        }
    }   
    
    /**
     * Knowledge of how many "strike/ratio pairs" of a specific strategy. If it is 
     * unknown, 5, the total number will be returned.
     * 
     * @param strat
     * @return 
     */
    private int getNumberOfStrikeRatios(String strat){
        String strategy = strat;
        if (this.isCustomStructure(strategy)){
            /**
             * Check the last line which has data
             */
            int num = -1;
            for (int i = 0; i < 5; i++){
                if ((DataGlobal.isNonEmptyNullString(jStrikeFieldList.get(i).getText())) 
                        || (((jCallPutSelectorList.get(i).getSelectedItem()) != null 
                            && (DataGlobal.isNonEmptyNullString(jCallPutSelectorList.get(i).getSelectedItem().toString()))))
                        || (DataGlobal.isNonEmptyNullString(jRatioFieldList.get(i).getText())))
                {
                    num = i;
                }
            }
            if (num == -1){
                return 1;
            }else{
                return num +1;
            }
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CALL.toString())){
            return 1;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.FENCE.toString())){
            return 2;
//        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.FENCEC.toString())){
//            return 2;
//        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.FENCEP.toString())){
//            return 2;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CCNDR.toString())){
            return 4;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CFLY.toString())){
            return 3;
//        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CRATIO.toString())){
//            return 2;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CSPRD.toString())){
            return 2;
//        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CSWPN.toString())){
//            return 1;
//        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PSWPN.toString())){
//            return 1;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CTREE.toString())){
            return 3;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PUT.toString())){
            return 1;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PCNDR.toString())){
            return 4;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PFLY.toString())){
            return 3;
//        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PRATIO.toString())){
//            return 2;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PSPRD.toString())){
            return 2;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PTREE.toString())){
            return 3;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.STRDL.toString())){
            return 2;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.STRNGL.toString())){
            return 2;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.IFLY.toString())){
            return 4;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.ICNDR.toString())){
            return 4;
        } else if (strategy.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CONV.toString())){
            return 2;
        }else{
            return 5;
        }
    }

    private String generateOptionCallPutToken(int num) {
        String srMessage = "";
        
        try{
            for (int i = 0; i < num; i++){
                if (srMessage.isEmpty()){
                    srMessage += getCallPutSelectorValue(i);
                }else{
                    srMessage += PbcSimGuiParser.DataDelimiter + getCallPutSelectorValue(i);
                }
            }
        }catch(Exception e){
            //logger.log(Level.SEVERE, e.getMessage(), e);
        }
       
        return srMessage;
    }
    

    private String generateStrikeMessageToken(int num) {
        boolean duplicateStrike;
        HashSet<String> duplicateStrikeStrategies = new HashSet<String>();
        duplicateStrikeStrategies.clear();
        duplicateStrikeStrategies.add(PointBoxQuoteStrategyTerm.IFLY.toString());
        duplicateStrikeStrategies.add(PointBoxQuoteStrategyTerm.CONV.toString());
       // duplicateStrikeStrategies.add(PointBoxQuoteStrategyTerm.STRDL.toString());

        String srMessage = "";
        try{
            for (int i = 0; i < num; i++){
                if (duplicateStrikeStrategies.contains(getStructureValue(false))){
                    duplicateStrike = false;
                    for (int j = i + 1; j < jStrikeFieldList.size(); j++){
                        if ((DataGlobal.convertToDouble(jStrikeFieldList.get(j).getText()) == DataGlobal.convertToDouble(jStrikeFieldList.get(i).getText())) && !jStrikeFieldList.get(j).getText().isEmpty()){
                            duplicateStrike = true;
                            break;
                        }
                    }

                    if (!duplicateStrike){
                        if (srMessage.isEmpty()){
                            srMessage += getStrikeValueToken(jStrikeFieldList.get(i));
                        }
                        else{
                            srMessage += PbcSimGuiParser.DataDelimiter + getStrikeValueToken(jStrikeFieldList.get(i));
                        }
                    }
                }else if (srMessage.isEmpty()){
                    srMessage += getStrikeValueToken(jStrikeFieldList.get(i));
                }
                else{
                    srMessage += PbcSimGuiParser.DataDelimiter + getStrikeValueToken(jStrikeFieldList.get(i));
                }
            }
            //spacial case: straddle
            if (PointBoxQuoteStrategyTerm.STRDL.toString().equalsIgnoreCase(jStructureSelector.getSelectedItem().toString())){
                String[] strikes = srMessage.split(PbcSimGuiParser.DataDelimiter);
                if (strikes[0].equalsIgnoreCase(strikes[1])){
                    srMessage = strikes[0];
                }
            }
        }catch(Exception e){
            //logger.log(Level.SEVERE, e.getMessage(), e);
        }
       
        return srMessage;
    }
    
    private String getStrikeValueToken(JTextField strike) {
        if (strike.getText().trim().isEmpty()){
            return "[Strike?]";
        }else{
            return DataGlobal.formatDoubleWithMinMax(DataGlobal.convertToDouble(strike.getText()),
                                                     owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                     owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()));
        }
    }
    
    private String generateContractPeriodToken(){
        String startDate = generateContractPeriodTokenHelper("[Start?]", jStartSelector);
        String endDate = generateContractPeriodTokenHelper("[End?]", jEndSelector);
        if (startDate.equalsIgnoreCase(endDate)){
            return startDate;
        }else{
            return startDate + PbcSimGuiParser.CalendarDelimiter + endDate;
        }
    }

    private String generateContractPeriodTokenHelper(String dateText, JComboBox jDateSelector){
        Object dateObject = jDateSelector.getSelectedItem();
        if (dateObject instanceof DateSelectorObject){
            return ((DateSelectorObject)dateObject).getDescriptiveDate();
        }else{
            return dateText;
        }
    }
    
    private long getContractSimMarkFieldValue(JComboBox jDateSelector){
        Object dateObject = jDateSelector.getSelectedItem();
        if (dateObject instanceof DateSelectorObject){
            return ((DateSelectorObject)dateObject).getDateTime().getTimeInMillis();
        }else{
            return (new GregorianCalendar()).getTimeInMillis();
        }
    }
    
    private void populateCrossSwapAdjFieldValuesForStripSelector(IPbsysQuoteLeg quoteLeg) {
        double tempCross = quoteLeg.getOptionCross();
        double tempSwap = quoteLeg.getOptionSwap();
        jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(tempCross,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        jSwapField.setValue(DataGlobal.formatDoubleWithMinMax(tempSwap,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        jAdjField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
    
    }
    
    private void updateCrossSwapAdjFieldValuesForStripSelector() {
        int startIndex = jStartSelector.getSelectedIndex();
        int endIndex = jEndSelector.getSelectedIndex();
        if ((startIndex < 0) || (endIndex < 0)) {
            jSwapField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            jAdjField.setValue("");
        } else {
            int months = endIndex - startIndex + 1;
            double tempPrice = 0.0;
            Object obj;
            if (startIndex >= 0 && endIndex >= 0 && months > 0){
                for (int i = startIndex; i <= endIndex; i++){
                    obj = jStartSelector.getItemAt(i);
                    if (obj instanceof DateSelectorObject){
                        tempPrice += owner.getKernel().queryUnderlierByMonthIndex(((DateSelectorObject)obj).toMMddyyyy(), owner.getSelectedPointBoxQuoteCode());
                    }
                }
                tempPrice /= months;
            }else{
                //one of index is not 0
                obj = jStartSelector.getItemAt(endIndex + startIndex);
                if (obj instanceof DateSelectorObject){
                    tempPrice = owner.getKernel().queryUnderlierByMonthIndex(((DateSelectorObject)obj).toMMddyyyy(), owner.getSelectedPointBoxQuoteCode());
                }
            }
            jSwapField.setValue(DataGlobal.formatDoubleWithMinMax(tempPrice, 
                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(tempPrice,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            jAdjField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        }
        owner.populateQuoteMessageTextField();
    }
    
    private void setupDateSelectorHelper(JComboBox jDateSelector, PointBoxQuoteCode selectedCode){
        jDateSelector.removeAllItems();
        LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData = owner.getKernel().retrieveAllDescriptiveExpirationData(selectedCode);
        Set<GregorianCalendar> keys = allDescriptiveExpData.keySet();
        Iterator<GregorianCalendar> itr = keys.iterator();
        GregorianCalendar date;
        while(itr.hasNext()){
            date = itr.next();
            jDateSelector.addItem(new DateSelectorObject(date, allDescriptiveExpData.get(date)));
        }//while
    }

    void refreshGuiForSelectedCode(final PointBoxQuoteCode selectedCode) {
        setupStartAndEndSelectors(selectedCode);
        if (SwingUtilities.isEventDispatchThread()){
            owner.setupSpinner(jCrossSpinner, true, selectedCode);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    owner.setupSpinner(jCrossSpinner, true, selectedCode);
                }
            });
        }
    }

    private void setupLegTabNameLabel(int legId){
        jLegTabName.setText(getLegTabName());
        try{
            jLegTabName.setBackground(legTabNameBkgColorList.get(legId-1));
        }catch (Exception ex){
            jLegTabName.setBackground(legTabNameBkgColorList.get(0));
        }
    }
    
    private void setupStructureSelector() {
        jStructureSelector.removeAllItems();
        jStructureSelector.addItem("");
        
        Iterator<PointBoxQuoteStrategyTerm> itr = structureSet.iterator();
        while (itr.hasNext()){
            jStructureSelector.addItem(itr.next());
        }
        
        jStructureSelector.setEditable(true);
        JTextField aJTextField = ((JTextField)jStructureSelector.getEditor().getEditorComponent());
        aJTextField.setColumns(3);
    }
    
    public String getLegTabName(){
        return LEG_ID_PREFIX + getLegId();
    }
    
    public int getLegId() {
        return legId;
    }

    public void setLegId(int legId) {
        this.legId = legId;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLegDataPanel = new javax.swing.JPanel();
        jStrikePanel = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jStartSelector = new javax.swing.JComboBox();
        jLabel2l = new javax.swing.JLabel();
        DefaultFormatter format = new DefaultFormatter();
        format.setOverwriteMode(false);
        jStrikeField1 = new JFormattedTextField(format);
        jStrikeField2 = new JFormattedTextField(format);
        jStrikeField3 = new JFormattedTextField(format);
        jStrikeField4 = new JFormattedTextField(format);
        jStrikeField5 = new JFormattedTextField(format);
        jCalculateButton = new javax.swing.JButton();
        jCallPutPanel = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jEndSelector = new javax.swing.JComboBox();
        jLabel33 = new javax.swing.JLabel();
        jCallPutSelector1 = new javax.swing.JComboBox(callPutOptions);
        jCallPutSelector2 = new javax.swing.JComboBox(callPutOptions);
        jCallPutSelector3 = new javax.swing.JComboBox(callPutOptions);
        jCallPutSelector4 = new javax.swing.JComboBox(callPutOptions);
        jCallPutSelector5 = new javax.swing.JComboBox(callPutOptions);
        jVolt6 = new javax.swing.JLabel();
        jRatioPanel = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jStructureSelector = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jRatioField1 = new javax.swing.JFormattedTextField();
        jRatioField2 = new javax.swing.JFormattedTextField();
        jRatioField3 = new javax.swing.JFormattedTextField();
        jRatioField4 = new javax.swing.JFormattedTextField();
        jRatioField5 = new javax.swing.JFormattedTextField();
        jReverseRatios = new javax.swing.JButton();
        jVolPanel = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jCrossSpinner = new javax.swing.JSpinner();
        jLabel17 = new javax.swing.JLabel();
        jVolt1 = new javax.swing.JLabel();
        jVolt2 = new javax.swing.JLabel();
        jVolt3 = new javax.swing.JLabel();
        jVolt4 = new javax.swing.JLabel();
        jVolt5 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPricePanel = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLiveCheck = new javax.swing.JCheckBox();
        jLabel31 = new javax.swing.JLabel();
        jPriceField1 = new javax.swing.JLabel();
        jPriceField2 = new javax.swing.JLabel();
        jPriceField3 = new javax.swing.JLabel();
        jPriceField4 = new javax.swing.JLabel();
        jPriceField5 = new javax.swing.JLabel();
        jPriceField6 = new javax.swing.JLabel();
        jDeltaPanel = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jSwapField = new javax.swing.JFormattedTextField();
        jLabel29 = new javax.swing.JLabel();
        jDelta1 = new javax.swing.JLabel();
        jDelta2 = new javax.swing.JLabel();
        jDelta3 = new javax.swing.JLabel();
        jDelta4 = new javax.swing.JLabel();
        jDelta5 = new javax.swing.JLabel();
        jDelta6 = new javax.swing.JLabel();
        jDdeltaPanel = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jDdelta1 = new javax.swing.JLabel();
        jDdelta2 = new javax.swing.JLabel();
        jDdelta3 = new javax.swing.JLabel();
        jDdelta4 = new javax.swing.JLabel();
        jDdelta5 = new javax.swing.JLabel();
        jDdelta6 = new javax.swing.JLabel();
        jVegaPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jAdjField = new javax.swing.JFormattedTextField();
        jLabel27 = new javax.swing.JLabel();
        jVega1 = new javax.swing.JLabel();
        jVega2 = new javax.swing.JLabel();
        jVega3 = new javax.swing.JLabel();
        jVega4 = new javax.swing.JLabel();
        jVega5 = new javax.swing.JLabel();
        jVega6 = new javax.swing.JLabel();
        jThetaPanel = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jTheta1 = new javax.swing.JLabel();
        jTheta2 = new javax.swing.JLabel();
        jTheta3 = new javax.swing.JLabel();
        jTheta4 = new javax.swing.JLabel();
        jTheta5 = new javax.swing.JLabel();
        jTheta6 = new javax.swing.JLabel();
        jGammaPanel = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jLegTabName = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jGamma1 = new javax.swing.JLabel();
        jGamma2 = new javax.swing.JLabel();
        jGamma3 = new javax.swing.JLabel();
        jGamma4 = new javax.swing.JLabel();
        jGamma5 = new javax.swing.JLabel();
        jGamma6 = new javax.swing.JLabel();
        jDgammaPanel = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jDgamma1 = new javax.swing.JLabel();
        jDgamma2 = new javax.swing.JLabel();
        jDgamma3 = new javax.swing.JLabel();
        jDgamma4 = new javax.swing.JLabel();
        jDgamma5 = new javax.swing.JLabel();
        jDgamma6 = new javax.swing.JLabel();
        jRowPanel = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(780, 400));

        jLegDataPanel.setLayout(new java.awt.GridLayout(1, 11, 5, 0));

        jStrikePanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel20.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel20.setText("From:");
        jLabel20.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jStrikePanel.add(jLabel20);

        jStartSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jStartSelectorItemStateChanged(evt);
            }
        });
        jStrikePanel.add(jStartSelector);

        jLabel2l.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2l.setText("Strike");
        jLabel2l.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jStrikePanel.add(jLabel2l);
        jStrikePanel.add(jStrikeField1);
        jStrikePanel.add(jStrikeField2);
        jStrikePanel.add(jStrikeField3);
        jStrikePanel.add(jStrikeField4);
        jStrikePanel.add(jStrikeField5);

        jCalculateButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCalculateButton.setForeground(new java.awt.Color(255, 0, 0));
        jCalculateButton.setText("Calculate");
        jCalculateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCalculateButtonActionPerformed(evt);
            }
        });
        jStrikePanel.add(jCalculateButton);

        jLegDataPanel.add(jStrikePanel);

        jCallPutPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel18.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel18.setText("To:");
        jLabel18.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jCallPutPanel.add(jLabel18);

        jEndSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jEndSelectorItemStateChanged(evt);
            }
        });
        jCallPutPanel.add(jEndSelector);

        jLabel33.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("Call/Put");
        jLabel33.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jCallPutPanel.add(jLabel33);

        jCallPutSelector1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCallPutSelector1ItemStateChanged(evt);
            }
        });
        jCallPutPanel.add(jCallPutSelector1);

        jCallPutSelector2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCallPutSelector2ItemStateChanged(evt);
            }
        });
        jCallPutPanel.add(jCallPutSelector2);

        jCallPutSelector3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCallPutSelector3ItemStateChanged(evt);
            }
        });
        jCallPutPanel.add(jCallPutSelector3);

        jCallPutSelector4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCallPutSelector4ItemStateChanged(evt);
            }
        });
        jCallPutPanel.add(jCallPutSelector4);

        jCallPutSelector5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCallPutSelector5ItemStateChanged(evt);
            }
        });
        jCallPutPanel.add(jCallPutSelector5);

        jVolt6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVolt6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jCallPutPanel.add(jVolt6);

        jLegDataPanel.add(jCallPutPanel);

        jRatioPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel34.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel34.setText("Structure:");
        jLabel34.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jRatioPanel.add(jLabel34);

        jStructureSelector.setMaximumSize(new java.awt.Dimension(28, 20));
        jStructureSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jStructureSelectorItemStateChanged(evt);
            }
        });
        jRatioPanel.add(jStructureSelector);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Ratio");
        jLabel9.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jRatioPanel.add(jLabel9);
        jRatioPanel.add(jRatioField1);
        jRatioPanel.add(jRatioField2);
        jRatioPanel.add(jRatioField3);
        jRatioPanel.add(jRatioField4);
        jRatioPanel.add(jRatioField5);

        jReverseRatios.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jReverseRatios.setText("Reverse");
        jReverseRatios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jReverseRatiosActionPerformed(evt);
            }
        });
        jRatioPanel.add(jReverseRatios);

        jLegDataPanel.add(jRatioPanel);

        jVolPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel32.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel32.setText("Cross");
        jLabel32.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jVolPanel.add(jLabel32);

        jCrossSpinner.setEnabled(false);
        jVolPanel.add(jCrossSpinner);

        jLabel17.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Volatility");
        jLabel17.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jVolPanel.add(jLabel17);

        jVolt1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVolt1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVolPanel.add(jVolt1);

        jVolt2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVolt2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVolPanel.add(jVolt2);

        jVolt3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVolt3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVolPanel.add(jVolt3);

        jVolt4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVolt4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVolPanel.add(jVolt4);

        jVolt5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVolt5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVolPanel.add(jVolt5);

        jLabel19.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel19.setText("TOTAL");
        jVolPanel.add(jLabel19);

        jLegDataPanel.add(jVolPanel);

        jPricePanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));
        jPricePanel.add(jLabel15);

        jLiveCheck.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLiveCheck.setSelected(true);
        jLiveCheck.setText("live");
        jLiveCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jLiveCheckItemStateChanged(evt);
            }
        });
        jPricePanel.add(jLiveCheck);

        jLabel31.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("Price");
        jLabel31.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jPricePanel.add(jLabel31);

        jPriceField1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPriceField1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPricePanel.add(jPriceField1);

        jPriceField2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPriceField2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPricePanel.add(jPriceField2);

        jPriceField3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPriceField3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPricePanel.add(jPriceField3);

        jPriceField4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPriceField4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPricePanel.add(jPriceField4);

        jPriceField5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPriceField5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPricePanel.add(jPriceField5);

        jPriceField6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPriceField6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPricePanel.add(jPriceField6);

        jLegDataPanel.add(jPricePanel);

        jDeltaPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel30.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel30.setText("Swap:");
        jLabel30.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jDeltaPanel.add(jLabel30);

        jSwapField.setEditable(false);
        jDeltaPanel.add(jSwapField);

        jLabel29.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Delta");
        jLabel29.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jDeltaPanel.add(jLabel29);

        jDelta1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDelta1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDeltaPanel.add(jDelta1);

        jDelta2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDelta2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDeltaPanel.add(jDelta2);

        jDelta3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDelta3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        jDeltaPanel.add(jDelta3);

        jDelta4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDelta4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDeltaPanel.add(jDelta4);

        jDelta5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDelta5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDeltaPanel.add(jDelta5);

        jDelta6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDelta6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDeltaPanel.add(jDelta6);

        jLegDataPanel.add(jDeltaPanel);

        jDdeltaPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel35.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel35.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jDdeltaPanel.add(jLabel35);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 85, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 29, Short.MAX_VALUE)
        );

        jDdeltaPanel.add(jPanel1);

        jLabel36.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("True Delta");
        jLabel36.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jDdeltaPanel.add(jLabel36);

        jDdelta1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDdelta1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDdeltaPanel.add(jDdelta1);

        jDdelta2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDdelta2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDdeltaPanel.add(jDdelta2);

        jDdelta3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDdelta3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        jDdeltaPanel.add(jDdelta3);

        jDdelta4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDdelta4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDdeltaPanel.add(jDdelta4);

        jDdelta5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDdelta5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDdeltaPanel.add(jDdelta5);

        jDdelta6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDdelta6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDdeltaPanel.add(jDdelta6);

        jLegDataPanel.add(jDdeltaPanel);

        jVegaPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Vol Adjust:");
        jLabel10.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jVegaPanel.add(jLabel10);

        jAdjField.setEditable(false);
        jVegaPanel.add(jAdjField);

        jLabel27.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("Vega");
        jLabel27.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jVegaPanel.add(jLabel27);

        jVega1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVega1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVegaPanel.add(jVega1);

        jVega2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVega2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVegaPanel.add(jVega2);

        jVega3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVega3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVegaPanel.add(jVega3);

        jVega4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVega4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVegaPanel.add(jVega4);

        jVega5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVega5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVegaPanel.add(jVega5);

        jVega6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jVega6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jVegaPanel.add(jVega6);

        jLegDataPanel.add(jVegaPanel);

        jThetaPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));
        jThetaPanel.add(jLabel21);
        jThetaPanel.add(jLabel22);

        jLabel25.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("Theta");
        jLabel25.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jThetaPanel.add(jLabel25);

        jTheta1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTheta1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jThetaPanel.add(jTheta1);

        jTheta2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTheta2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jThetaPanel.add(jTheta2);

        jTheta3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTheta3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jThetaPanel.add(jTheta3);

        jTheta4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTheta4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jThetaPanel.add(jTheta4);

        jTheta5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTheta5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jThetaPanel.add(jTheta5);

        jTheta6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jTheta6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jThetaPanel.add(jTheta6);

        jLegDataPanel.add(jThetaPanel);

        jGammaPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));
        jGammaPanel.add(jLabel14);

        jLegTabName.setBackground(new java.awt.Color(255, 255, 0));
        jLegTabName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLegTabName.setForeground(new java.awt.Color(255, 0, 0));
        jLegTabName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLegTabName.setText("Leg #");
        jLegTabName.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 0, 0), 1, true));
        jLegTabName.setOpaque(true);
        jGammaPanel.add(jLegTabName);

        jLabel26.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("Gamma");
        jLabel26.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jGammaPanel.add(jLabel26);

        jGamma1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jGamma1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jGammaPanel.add(jGamma1);

        jGamma2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jGamma2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jGammaPanel.add(jGamma2);

        jGamma3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jGamma3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jGammaPanel.add(jGamma3);

        jGamma4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jGamma4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jGammaPanel.add(jGamma4);

        jGamma5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jGamma5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jGammaPanel.add(jGamma5);

        jGamma6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jGamma6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jGammaPanel.add(jGamma6);

        jLegDataPanel.add(jGammaPanel);

        jDgammaPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));
        jDgammaPanel.add(jLabel16);
        jDgammaPanel.add(jLabel23);

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("True Gamma");
        jLabel28.setToolTipText("");
        jLabel28.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jDgammaPanel.add(jLabel28);

        jDgamma1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDgamma1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDgammaPanel.add(jDgamma1);

        jDgamma2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDgamma2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDgammaPanel.add(jDgamma2);

        jDgamma3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDgamma3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDgammaPanel.add(jDgamma3);

        jDgamma4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDgamma4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDgammaPanel.add(jDgamma4);

        jDgamma5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDgamma5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDgammaPanel.add(jDgamma5);

        jDgamma6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jDgamma6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jDgammaPanel.add(jDgamma6);

        jLegDataPanel.add(jDgammaPanel);

        jRowPanel.setLayout(new java.awt.GridLayout(9, 1, 0, 5));
        jRowPanel.add(jLabel12);
        jRowPanel.add(jLabel11);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jRowPanel.add(jLabel1);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("(1)");
        jRowPanel.add(jLabel2);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("(2)");
        jRowPanel.add(jLabel3);

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("(3)");
        jRowPanel.add(jLabel4);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("(4)");
        jRowPanel.add(jLabel5);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("(5)");
        jRowPanel.add(jLabel6);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jRowPanel.add(jLabel7);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jRowPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLegDataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRowPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLegDataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(74, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLiveCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jLiveCheckItemStateChanged
        if (owner != null){
            owner.populateQuoteMessageTextField();
        }
    }//GEN-LAST:event_jLiveCheckItemStateChanged

    private void jCallPutSelector1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCallPutSelector1ItemStateChanged
        calculateOptionPrice(jCallPutSelector1, 0);
        owner.populateQuoteMessageTextField();
    }//GEN-LAST:event_jCallPutSelector1ItemStateChanged

    private void jCallPutSelector2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCallPutSelector2ItemStateChanged
        calculateOptionPrice(jCallPutSelector2, 1);
        owner.populateQuoteMessageTextField();
    }//GEN-LAST:event_jCallPutSelector2ItemStateChanged

    private void jCallPutSelector3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCallPutSelector3ItemStateChanged
        calculateOptionPrice(jCallPutSelector3, 2);
        owner.populateQuoteMessageTextField();
    }//GEN-LAST:event_jCallPutSelector3ItemStateChanged

    private void jCallPutSelector4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCallPutSelector4ItemStateChanged
        calculateOptionPrice(jCallPutSelector4, 3);
        owner.populateQuoteMessageTextField();
    }//GEN-LAST:event_jCallPutSelector4ItemStateChanged

    private void jCallPutSelector5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCallPutSelector5ItemStateChanged
        calculateOptionPrice(jCallPutSelector5, 4);
        owner.populateQuoteMessageTextField();
    }//GEN-LAST:event_jCallPutSelector5ItemStateChanged

    private void jReverseRatiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jReverseRatiosActionPerformed
        for (JFormattedTextField ratioField : jRatioFieldList){
            flipRatioValue(ratioField);
        }
    }//GEN-LAST:event_jReverseRatiosActionPerformed

    private void jStructureSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jStructureSelectorItemStateChanged
        String structure = getStructureValue(true);
        if (DataGlobal.isEmptyNullString(structure)){
            return;
        }
        structureSelectedEventHappened(structure);
        if (owner != null){
            owner.populateQuoteMessageTextField();
        }
    }//GEN-LAST:event_jStructureSelectorItemStateChanged

    private void jEndSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jEndSelectorItemStateChanged
        int startIndex = jStartSelector.getSelectedIndex();
        int endIndex = jEndSelector.getSelectedIndex();

        if ((startIndex < 0) || (endIndex < 0)){
            return;
        }
        if (startIndex > endIndex || (startIndex < 0 && endIndex > 0)) {
            jStartSelector.setSelectedIndex(endIndex);
        }
        updateCrossSwapAdjFieldValuesForStripSelector();
    }//GEN-LAST:event_jEndSelectorItemStateChanged

    private void jStartSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jStartSelectorItemStateChanged
        int startIndex = jStartSelector.getSelectedIndex();
        int endIndex = jEndSelector.getSelectedIndex();

        if ((startIndex < 0) || (endIndex < 0)){
            return;
        }
        if ((endIndex < 0) || startIndex > endIndex) {
            jEndSelector.setSelectedIndex(startIndex);
        }
        updateCrossSwapAdjFieldValuesForStripSelector();
    }//GEN-LAST:event_jStartSelectorItemStateChanged

    private void jCalculateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCalculateButtonActionPerformed
        owner.handleCalculateBtnClicked(true);
    }//GEN-LAST:event_jCalculateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField jAdjField;
    private javax.swing.JButton jCalculateButton;
    private javax.swing.JPanel jCallPutPanel;
    private javax.swing.JComboBox jCallPutSelector1;
    private javax.swing.JComboBox jCallPutSelector2;
    private javax.swing.JComboBox jCallPutSelector3;
    private javax.swing.JComboBox jCallPutSelector4;
    private javax.swing.JComboBox jCallPutSelector5;
    private javax.swing.JSpinner jCrossSpinner;
    private javax.swing.JLabel jDdelta1;
    private javax.swing.JLabel jDdelta2;
    private javax.swing.JLabel jDdelta3;
    private javax.swing.JLabel jDdelta4;
    private javax.swing.JLabel jDdelta5;
    private javax.swing.JLabel jDdelta6;
    private javax.swing.JPanel jDdeltaPanel;
    private javax.swing.JLabel jDelta1;
    private javax.swing.JLabel jDelta2;
    private javax.swing.JLabel jDelta3;
    private javax.swing.JLabel jDelta4;
    private javax.swing.JLabel jDelta5;
    private javax.swing.JLabel jDelta6;
    private javax.swing.JPanel jDeltaPanel;
    private javax.swing.JLabel jDgamma1;
    private javax.swing.JLabel jDgamma2;
    private javax.swing.JLabel jDgamma3;
    private javax.swing.JLabel jDgamma4;
    private javax.swing.JLabel jDgamma5;
    private javax.swing.JLabel jDgamma6;
    private javax.swing.JPanel jDgammaPanel;
    private javax.swing.JComboBox jEndSelector;
    private javax.swing.JLabel jGamma1;
    private javax.swing.JLabel jGamma2;
    private javax.swing.JLabel jGamma3;
    private javax.swing.JLabel jGamma4;
    private javax.swing.JLabel jGamma5;
    private javax.swing.JLabel jGamma6;
    private javax.swing.JPanel jGammaPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel2l;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jLegDataPanel;
    private javax.swing.JLabel jLegTabName;
    private javax.swing.JCheckBox jLiveCheck;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jPriceField1;
    private javax.swing.JLabel jPriceField2;
    private javax.swing.JLabel jPriceField3;
    private javax.swing.JLabel jPriceField4;
    private javax.swing.JLabel jPriceField5;
    private javax.swing.JLabel jPriceField6;
    private javax.swing.JPanel jPricePanel;
    private javax.swing.JFormattedTextField jRatioField1;
    private javax.swing.JFormattedTextField jRatioField2;
    private javax.swing.JFormattedTextField jRatioField3;
    private javax.swing.JFormattedTextField jRatioField4;
    private javax.swing.JFormattedTextField jRatioField5;
    private javax.swing.JPanel jRatioPanel;
    private javax.swing.JButton jReverseRatios;
    private javax.swing.JPanel jRowPanel;
    private javax.swing.JComboBox jStartSelector;
    private javax.swing.JFormattedTextField jStrikeField1;
    public javax.swing.JFormattedTextField jStrikeField2;
    private javax.swing.JFormattedTextField jStrikeField3;
    private javax.swing.JFormattedTextField jStrikeField4;
    private javax.swing.JFormattedTextField jStrikeField5;
    private javax.swing.JPanel jStrikePanel;
    private javax.swing.JComboBox jStructureSelector;
    private javax.swing.JFormattedTextField jSwapField;
    private javax.swing.JLabel jTheta1;
    private javax.swing.JLabel jTheta2;
    private javax.swing.JLabel jTheta3;
    private javax.swing.JLabel jTheta4;
    private javax.swing.JLabel jTheta5;
    private javax.swing.JLabel jTheta6;
    private javax.swing.JPanel jThetaPanel;
    private javax.swing.JLabel jVega1;
    private javax.swing.JLabel jVega2;
    private javax.swing.JLabel jVega3;
    private javax.swing.JLabel jVega4;
    private javax.swing.JLabel jVega5;
    private javax.swing.JLabel jVega6;
    private javax.swing.JPanel jVegaPanel;
    private javax.swing.JPanel jVolPanel;
    private javax.swing.JLabel jVolt1;
    private javax.swing.JLabel jVolt2;
    private javax.swing.JLabel jVolt3;
    private javax.swing.JLabel jVolt4;
    private javax.swing.JLabel jVolt5;
    private javax.swing.JLabel jVolt6;
    // End of variables declaration//GEN-END:variables


    private void enableComponentSelections(boolean choice1, boolean choice2, boolean choice3, boolean choice4, boolean choice5) {
        jStrikeField1.setEnabled(choice1);
        jRatioField1.setEnabled(choice1);
        if(choice1==false){
            jRatioField1.setText("");
        }
        jCallPutSelector1.setEnabled(choice1);

        jStrikeField2.setEnabled(choice2);
        jRatioField2.setEnabled(choice2);
        if(choice2==false){
            jRatioField2.setText("");
        }
        jCallPutSelector2.setEnabled(choice2);

        jStrikeField3.setEnabled(choice3);
        jRatioField3.setEnabled(choice3);
        if(choice3==false){
            jRatioField3.setText("");
        }
        jCallPutSelector3.setEnabled(choice3);

        jStrikeField4.setEnabled(choice4);
        jRatioField4.setEnabled(choice4);
        if(choice4==false){
            jRatioField4.setText("");
        }        
        jCallPutSelector4.setEnabled(choice4);

        jStrikeField5.setEnabled(choice5);
        jRatioField5.setEnabled(choice5);
        if(choice5==false){
            jRatioField5.setText("");
        }        
        jCallPutSelector5.setEnabled(choice5);
    }
    
    private void clearCallPutsAndRatios(){
        jRatioField1.setValue("");
        jRatioField2.setValue("");
        jRatioField3.setValue("");
        jRatioField4.setValue("");
        jRatioField5.setValue("");

        jCallPutSelector1.setSelectedIndex(0);
        jCallPutSelector2.setSelectedIndex(0);
        jCallPutSelector3.setSelectedIndex(0);
        jCallPutSelector4.setSelectedIndex(0);
        jCallPutSelector5.setSelectedIndex(0);
    }
    
    private boolean isCustomStructure(String structure){
        return PointBoxQuoteStrategyTerm.CUSTOM.equals(PointBoxQuoteStrategyTerm.convertEnumValueToType(structure));
    }
    
    private void structureSelectedEventHappened(String structure){
        this.clearPbcQuoteLegValueTable();
        if (isCustomStructure(structure)){
            enableComponentSelections(true, true, true, true, true);
        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.FENCE.toString())) {
            clearCallPutsAndRatios();
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);       

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.STRDL.toString())) {
            clearCallPutsAndRatios();
            jStrikeField2.setValue("");
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(1.0);   

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.STRNGL.toString())) {
            clearCallPutsAndRatios();
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CALL.toString())) {
            clearCallPutsAndRatios();
            jStrikeField2.setValue("");
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, false, false, false, false);

            jCallPutSelector1.setSelectedIndex(1);
            jRatioField1.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CSPRD.toString())) {
            clearCallPutsAndRatios();
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            jCallPutSelector1.setSelectedIndex(1);
            jCallPutSelector2.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CFLY.toString())) {
            clearCallPutsAndRatios();
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            jCallPutSelector1.setSelectedIndex(1);
            jCallPutSelector2.setSelectedIndex(1);
            jCallPutSelector3.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-2.0);
            jRatioField3.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CCNDR.toString())) {
            clearCallPutsAndRatios();
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            jCallPutSelector1.setSelectedIndex(1);
            jCallPutSelector2.setSelectedIndex(1);
            jCallPutSelector3.setSelectedIndex(1);
            jCallPutSelector4.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);
            jRatioField3.setValue(-1.0);
            jRatioField4.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CTREE.toString())) {
            clearCallPutsAndRatios();
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            //buy low, sell mid, sell high
            jCallPutSelector1.setSelectedIndex(1);
            jCallPutSelector2.setSelectedIndex(1);
            jCallPutSelector3.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);
            jRatioField3.setValue(-1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField3);
            if (strike2 < strike1 && strike2 < strike3) {
                jRatioField1.setValue(-1.0);
                jRatioField2.setValue(1.0);
                jRatioField3.setValue(-1.0);
            } else if (strike3 < strike1 && strike3 < strike2) {
                jRatioField1.setValue(-1.0);
                jRatioField2.setValue(-1.0);
                jRatioField3.setValue(1.0);
            }

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PUT.toString())) {
            clearCallPutsAndRatios();
            jStrikeField2.setValue("");
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, false, false, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jRatioField1.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PSPRD.toString())) {
            clearCallPutsAndRatios();
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(2);
            jRatioField1.setValue(-1.0);
            jRatioField2.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PFLY.toString())) {
            clearCallPutsAndRatios();
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(2);
            jCallPutSelector3.setSelectedIndex(2);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-2.0);
            jRatioField3.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PCNDR.toString())) {
            clearCallPutsAndRatios();
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(2);
            jCallPutSelector3.setSelectedIndex(2);
            jCallPutSelector4.setSelectedIndex(2);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);
            jRatioField3.setValue(-1.0);
            jRatioField4.setValue(1.0);

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.PTREE.toString())) {
            clearCallPutsAndRatios();
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, false, false);

            //buy high, sell mid, sell low
            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(2);
            jCallPutSelector3.setSelectedIndex(2);
            jRatioField1.setValue(-1.0);
            jRatioField2.setValue(-1.0);
            jRatioField3.setValue(1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField3);
            if (strike2 > strike1 && strike2 > strike3) {
                jRatioField1.setValue(-1.0);
                jRatioField2.setValue(1.0);
                jRatioField3.setValue(-1.0);
            } else if (strike3 > strike1 && strike3 > strike2) {
                jRatioField1.setValue(-1.0);
                jRatioField2.setValue(-1.0);
                jRatioField3.setValue(1.0);
            }

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.IFLY.toString())) {
            clearCallPutsAndRatios();
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            //low is +put
            //mid is -put
            //mid is -call
            //high is +call
            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(2);
            jCallPutSelector3.setSelectedIndex(1);
            jCallPutSelector4.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);
            jRatioField3.setValue(-1.0);
            jRatioField4.setValue(1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField3);
            double strike4 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField4);
            if (strike1 <= strike2 && strike2 == strike3 && strike3 <= strike4) {
            } else if (strike1 >= strike2 && strike2 == strike3 && strike3 >= strike4) {
                jCallPutSelector1.setSelectedIndex(1);
                jCallPutSelector2.setSelectedIndex(1);
                jCallPutSelector3.setSelectedIndex(2);
                jCallPutSelector4.setSelectedIndex(2);
                jRatioField1.setValue(1.0);
                jRatioField2.setValue(-1.0);
                jRatioField3.setValue(-1.0);
                jRatioField4.setValue(1.0);
            } else if (strike1 == strike2) {
            } else if (strike1 == strike3) {
            } else if (strike1 == strike4) {
            } else if (strike2 == strike4) {
            } else if (strike3 == strike4) {
            }

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.ICNDR.toString())) {
            clearCallPutsAndRatios();
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, true, true, false);

            //low is +call
            //2nd low is -call
            //2nd high is -put
            //high is +put
            jCallPutSelector1.setSelectedIndex(1);
            jCallPutSelector2.setSelectedIndex(1);
            jCallPutSelector3.setSelectedIndex(2);
            jCallPutSelector4.setSelectedIndex(2);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);
            jRatioField3.setValue(-1.0);
            jRatioField4.setValue(1.0);

            double strike1 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField1);
            double strike2 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField2);
            double strike3 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField3);
            double strike4 = SwingGlobal.retrieveDoubleFromTextField(jStrikeField4);

            //KEEP WORKING ON THIS
            if (strike1 <= strike2 && strike2 <= strike3 && strike3 <= strike4) {
            } else if (strike1 >= strike2 && strike2 >= strike3 && strike3 >= strike4) {
                jCallPutSelector1.setSelectedIndex(2);
                jCallPutSelector2.setSelectedIndex(2);
                jCallPutSelector3.setSelectedIndex(1);
                jCallPutSelector4.setSelectedIndex(1);
                jRatioField1.setValue(1.0);
                jRatioField2.setValue(-1.0);
                jRatioField3.setValue(-1.0);
                jRatioField4.setValue(1.0);
            } else if (strike2 <= strike1 && strike1 <= strike3 && strike3 <= strike4) {
                jCallPutSelector1.setSelectedIndex(1);
                jCallPutSelector2.setSelectedIndex(1);
                jCallPutSelector3.setSelectedIndex(2);
                jCallPutSelector4.setSelectedIndex(2);
                jRatioField1.setValue(-1.0);
                jRatioField2.setValue(1.0);
                jRatioField3.setValue(-1.0);
                jRatioField4.setValue(1.0);
            } else if (strike2 >= strike1 && strike1 >= strike3 && strike3 >= strike4) {
                jCallPutSelector1.setSelectedIndex(2);
                jCallPutSelector2.setSelectedIndex(2);
                jCallPutSelector3.setSelectedIndex(1);
                jCallPutSelector4.setSelectedIndex(1);
                jRatioField1.setValue(-1.0);
                jRatioField2.setValue(1.0);
                jRatioField3.setValue(-1.0);
                jRatioField4.setValue(1.0);
            } else if (strike1 <= strike2 && strike2 <= strike4 && strike4 <= strike3) {
                jCallPutSelector1.setSelectedIndex(1);
                jCallPutSelector2.setSelectedIndex(1);
                jCallPutSelector3.setSelectedIndex(2);
                jCallPutSelector4.setSelectedIndex(2);
                jRatioField1.setValue(1.0);
                jRatioField2.setValue(-1.0);
                jRatioField3.setValue(1.0);
                jRatioField4.setValue(-1.0);
            } else if (strike1 <= strike3 && strike3 <= strike2 && strike2 <= strike4) {
                jCallPutSelector1.setSelectedIndex(1);
                jCallPutSelector2.setSelectedIndex(2);
                jCallPutSelector3.setSelectedIndex(1);
                jCallPutSelector4.setSelectedIndex(2);
                jRatioField1.setValue(1.0);
                jRatioField2.setValue(-1.0);
                jRatioField3.setValue(-1.0);
                jRatioField4.setValue(1.0);
            }

        } else if (structure.equalsIgnoreCase(PointBoxQuoteStrategyTerm.CONV.toString())) {
            clearCallPutsAndRatios();
            jStrikeField3.setValue("");
            jStrikeField4.setValue("");
            jStrikeField5.setValue("");

            enableComponentSelections(true, true, false, false, false);

            jCallPutSelector1.setSelectedIndex(2);
            jCallPutSelector2.setSelectedIndex(1);
            jRatioField1.setValue(1.0);
            jRatioField2.setValue(-1.0);

        }else{
            enableComponentSelections(true, true, true, true, true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        double value;
        if (source == jCrossTextField) {           
            value = SwingGlobal.retrieveDoubleFromTextField(jCrossTextField);
            if (oldCrossField != value) {
                oldCrossField = value;
                if (oldCrossField == 0.0) {
                    oldCrossField = SwingGlobal.retrieveDoubleFromTextField(jSwapField);
                }                 
                jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(oldCrossField,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
//                tableModel.crossValue = oldCrossField;
                jAdjField.setValue(DataGlobal.formatDoubleWithMinMax(oldCrossField - oldSwapField,
                        owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                        owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            }
        } else if (source == jSwapField) {
            value = SwingGlobal.retrieveDoubleFromTextField(jSwapField);
            if (oldSwapField != value) {
                oldSwapField = value;
//                tableModel.swapValue = value;
            }
        } else if (source == jStrikeField1) {
            value = SwingGlobal.retrieveDoubleFromTextField(jStrikeField1);
            if (oldStrikeField1 != value) {
                oldStrikeField1 = value;
                if (value == 0.0) {
//                    tableModel.inputStrikes.put(0, "");
                    jStrikeField1.setValue("");
                } else {
//                    tableModel.inputStrikes.put(0, value);
                    jStrikeField1.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField1,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    calculateOptionPrice(0);
                }                
            } else {
                if (value != 0.0) {
                    jStrikeField1.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField1,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                }
            }
        } else if (source == jStrikeField2) {
            value = SwingGlobal.retrieveDoubleFromTextField(jStrikeField2);
            if (oldStrikeField2 != value) {
                oldStrikeField2 = value;
                if (value == 0.0) {
//                    tableModel.inputStrikes.put(1, "");
                    jStrikeField2.setValue("");
                } else {
//                    tableModel.inputStrikes.put(1, value);
                    jStrikeField2.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField2,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    calculateOptionPrice(1);
                }                
            } else {
                if (value != 0.0) {
                    jStrikeField2.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField2,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                }
            }
        } else if (source == jStrikeField3) {
            value = SwingGlobal.retrieveDoubleFromTextField(jStrikeField3);
            if (oldStrikeField3 != value) {
                oldStrikeField3 = value;
                if (value == 0.0) {
//                    tableModel.inputStrikes.put(2, "");
                    jStrikeField3.setValue("");
                } else {
//                    tableModel.inputStrikes.put(2, value);
                    jStrikeField3.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField3,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    calculateOptionPrice(2);
                }                
            } else {
                if (value != 0.0) {
                    jStrikeField3.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField3,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                }
            }
        } else if (source == jStrikeField4) {
            value = SwingGlobal.retrieveDoubleFromTextField(jStrikeField4);
            if (oldStrikeField4 != value) {
                oldStrikeField4 = value;
                if (value == 0.0) {
//                    tableModel.inputStrikes.put(3, "");
                    jStrikeField4.setValue("");
                } else {
//                    tableModel.inputStrikes.put(3, value);
                    jStrikeField4.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField4,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    calculateOptionPrice(3);
                }                
            } else {
                if (value != 0.0) {
                    jStrikeField4.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField4,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                }
            }
        } else if (source == jStrikeField5) {
            value = SwingGlobal.retrieveDoubleFromTextField(jStrikeField5);
            if (oldStrikeField5 != value) {
                oldStrikeField5 = value;
                if (value == 0.0) {
//                    tableModel.inputStrikes.put(4, "");
                    jStrikeField5.setValue("");
                } else {
//                    tableModel.inputStrikes.put(4, value);
                    jStrikeField5.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField5,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    calculateOptionPrice(4);
                }                
            } else {
                if (value != 0.0) {
                    jStrikeField5.setValue(DataGlobal.formatDoubleWithMinMax(oldStrikeField5,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                }
            }
        } else if (source == jRatioField1) {
            value = SwingGlobal.retrieveDoubleFromTextField(jRatioField1);
            if (oldRatioField1 != value) {
                oldRatioField1 = value;
                if (value == 0.0) {
//                    tableModel.inputRatios.put(0, "");
                    jRatioField1.setValue("");
                } else {
//                    tableModel.inputRatios.put(0, value);
                    jRatioField1.setValue(FormatterCommons.format1Dec(oldRatioField1));
                    calculateOptionPrice(0);
                }                
            } else {
                if (value != 0.0) {
                    jRatioField1.setValue(FormatterCommons.format1Dec(oldRatioField1));
                }
            }
        } else if (source == jRatioField2) {
            value = SwingGlobal.retrieveDoubleFromTextField(jRatioField2);
            if (oldRatioField2 != value) {
                oldRatioField2 = value;
                if (value == 0.0) {
//                    tableModel.inputRatios.put(1, "");
                    jRatioField2.setValue("");
                } else {
//                    tableModel.inputRatios.put(1, value);
                    jRatioField2.setValue(FormatterCommons.format1Dec(oldRatioField2));
                    calculateOptionPrice(1);
                }                
            } else {
                if (value != 0.0) {
                    jRatioField2.setValue(FormatterCommons.format1Dec(oldRatioField2));
                }
            }
        } else if (source == jRatioField3) {
            value = SwingGlobal.retrieveDoubleFromTextField(jRatioField3);
            if (oldRatioField3 != value) {
                oldRatioField3 = value;
                if (value == 0.0) {
//                    tableModel.inputRatios.put(2, "");
                    jRatioField3.setValue("");
                } else {
//                    tableModel.inputRatios.put(2, value);
                    jRatioField3.setValue(FormatterCommons.format1Dec(oldRatioField3));
                    calculateOptionPrice(2);
                }                
            } else {
                if (value != 0.0) {
                    jRatioField3.setValue(FormatterCommons.format1Dec(oldRatioField3));
                }
            }
        } else if (source == jRatioField4) {
            value = SwingGlobal.retrieveDoubleFromTextField(jRatioField4);
            if (oldRatioField4 != value) {
                oldRatioField4 = value;
                if (value == 0.0) {
//                    tableModel.inputRatios.put(3, "");
                    jRatioField4.setValue("");
                } else {
//                    tableModel.inputRatios.put(3, value);
                    jRatioField4.setValue(FormatterCommons.format1Dec(oldRatioField4));
                    calculateOptionPrice(3);
                }                
            } else {
                if (value != 0.0) {
                    jRatioField4.setValue(FormatterCommons.format1Dec(oldRatioField4));
                }
            }
        } else if (source == jRatioField5) {
            value = SwingGlobal.retrieveDoubleFromTextField(jRatioField5);
            if (oldRatioField5 != value) {
                oldRatioField5 = value;
                if (value == 0.0) {
//                    tableModel.inputRatios.put(4, "");
                    jRatioField5.setValue("");
                } else {
//                    tableModel.inputRatios.put(4, value);
                    jRatioField5.setValue(FormatterCommons.format1Dec(oldRatioField5));
                    calculateOptionPrice(4);
                }                
            } else {
                if (value != 0.0) {
                    jRatioField5.setValue(FormatterCommons.format1Dec(oldRatioField5));
                }
            }
        }
        owner.populateQuoteMessageTextField();
    }

    private void flipRatioValue(JFormattedTextField ratioField) {
        if (!ratioField.isEnabled()){
            return;
        }
        double value = SwingGlobal.retrieveDoubleFromTextField(ratioField);
        if (value != 0){
            value = value *(-1);
            ratioField.setValue(value);
        }
    }

    void clearPbcQuoteLegPanel() {
        try{
            jStartSelector.setSelectedIndex(0);
            jEndSelector.setSelectedIndex(0);
            jStructureSelector.setSelectedIndex(0);
            jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
//            jCrossSpinner.setValue(0.0);
            jSwapField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
            jAdjField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        }catch(Exception ex){
        }

        clearPbcQuoteLegValueTable();
    }
    
    private void clearPbcQuoteLegValueTable(){
        clearFormattedTextFieldList(jStrikeFieldList);
        clearJComboBoxList(jCallPutSelectorList);
        clearFormattedTextFieldList(jRatioFieldList);
        clearLabelValues(jPriceFieldList);
        clearLabelValues(jVoltList);
        clearLabelValues(jDeltaList);
        clearLabelValues(jDdeltaList);
        clearLabelValues(jVegaList);
        clearLabelValues(jThetaList);
        clearLabelValues(jGammaList);
        clearLabelValues(jDgammaList);
    }
    
    private void clearLabelValues(ArrayList<JLabel> aJLabelList){
        for (JLabel aJLabel : aJLabelList){
            aJLabel.setText("");
        }
    }
    
    private void clearFormattedTextFieldList(ArrayList<JFormattedTextField> aJFormattedTextFieldList) {
        for (JFormattedTextField aJFormattedTextField : aJFormattedTextFieldList){
            aJFormattedTextField.setValue("");
        }
    }

    private void clearJComboBoxList(ArrayList<JComboBox> aJComboBoxList) {
        for (JComboBox aJComboBox : aJComboBoxList){
            aJComboBox.setSelectedIndex(0);
        }
    }

    private GregorianCalendar getContractStart(){
        return ((DateSelectorObject)(jStartSelector.getSelectedItem())).getDate().getDateTime();
    }

    private GregorianCalendar getContractEnd(){
        return ((DateSelectorObject)(jEndSelector.getSelectedItem())).getDate().getDateTime();
    }
    
    private double getCrossValue(){
        String value = (jCrossTextField.getText()).trim();
        if (RegexGlobal.isNumberString(value)){
            return Double.parseDouble(value);
        }else{
            return 0;
        }
    }
    
    private String getStructureValue(boolean displayMessage){                                              
        String structure = jStructureSelector.getSelectedItem().toString();
        if (structure.contains(" ")){
            if (displayMessage){
                if (SwingUtilities.isEventDispatchThread()){
                    JOptionPane.showMessageDialog(owner, "Custom strategy cannot have any white space.");
                }else{
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(owner, "Custom strategy cannot have any white space.");
                        }
                    });
                }
            }
            return "";
        }else{
            return structure;
        }
    }

    void loadPbsysQuoteLegForOption(IPbsysQuoteLeg quoteLeg, int index) throws Exception{
        quoteLeg.setOptionStrategy(getCallPutSelectorValue(index));
        quoteLeg.setOptionStrikes(0, SwingGlobal.retrieveDoubleFromTextField(jStrikeFieldList.get(index)));
        if ((int)(quoteLeg.getOptionStrikes(0)*1) == 0){
            throw new Exception("Strike value is not input yet.");
        }
        quoteLeg.setOptionRatios(0, SwingGlobal.retrieveDoubleFromTextField(jRatioFieldList.get(index)));
        if ((int)(quoteLeg.getOptionRatios(0)*1) == 0){
            throw new Exception("Ratio value is not input yet.");
        }
        
        quoteLeg.setOptionContractStartDate(getContractStart());
        quoteLeg.setOptionContractEndDate(getContractEnd());
        quoteLeg.setOptionCross(getCrossValue());
        if (quoteLeg.getOptionCross() > 0){
            quoteLeg.setCrossEmbedded(PbsysQuoteLegCrossStatus.Yes);
        }else{
            quoteLeg.setCrossEmbedded(PbsysQuoteLegCrossStatus.No);
        }
    }
    
    /**
     * This method assumes quotePanel.isQuoteMessageReady() is true
     * @param quote
     * @param legIndex
     * @throws Exception 
     */
    void loadPbsysQuoteLeg(IPbsysOptionQuote quote, int legIndex) throws Exception{
        if (legIndex > 1){
            return;
        }
        IPbsysQuoteLeg quoteLeg = quote.getOptionStrategyLegs().get(legIndex);
        quoteLeg.setOptionContractStartDate(getContractStart());
        quoteLeg.setOptionContractEndDate(getContractEnd());
        quoteLeg.setOptionCross(getCrossValue());
        if (jLiveCheck.isSelected()){
            quoteLeg.setupCrossEmbeddedStatus(PbsysQuoteLegCrossStatus.No);
        }else{
            quoteLeg.setupCrossEmbeddedStatus(PbsysQuoteLegCrossStatus.Yes);
        }
        //quoteLeg.setOptionProduct(quoteLegState.getLocation());
        quoteLeg.setOptionStrategy(getStructureValue(false));
        //quoteLeg.setOptionExercise(quoteLegState.getExercise());
        //quoteLeg.setOptionSecurity(quoteLegState.getSecurity());
        //quoteLeg.setOptionUnderlier(quoteLegState.getUnderlier());
        String[] optionFieldArray;
        double strike;
        double ratio;
        for (int optionIndex = 0; optionIndex < 5; optionIndex++){
            strike = SwingGlobal.retrieveDoubleFromTextField(jStrikeFieldList.get(optionIndex));
            ratio = SwingGlobal.retrieveDoubleFromTextField(jRatioFieldList.get(optionIndex));
            quoteLeg.setOptionStrikes(optionIndex, strike);
            quoteLeg.setOptionRatios(optionIndex, ratio);
            if (strike != 0){
                optionFieldArray = new String[3];
                optionFieldArray[0] = String.valueOf(strike);
                optionFieldArray[1] = getCallPutSelectorValue(optionIndex);
                optionFieldArray[2] = String.valueOf(ratio);
                quoteLeg.addPointBoxOption(PbcStructuredQuoteBuilder.createPointBoxOption(quote, legIndex, optionFieldArray));
            }
        }//for
    }

    QuoteLegState constructQuoteLegState() {
        QuoteLegState aQuoteLegState = new QuoteLegState();
        aQuoteLegState.setContractEnd(this.getContractEnd().getTimeInMillis());
        aQuoteLegState.setContractStart(this.getContractStart().getTimeInMillis());
        if (!jLiveCheck.isSelected()){
            aQuoteLegState.setCross(getCrossValue());
        }
        aQuoteLegState.setStructure(getStructureValue(false));
        
        ArrayList<QuoteLegValueState> aQuoteLegValueStateList = new ArrayList<QuoteLegValueState>();
        QuoteLegValueState aQuoteLegValueState;
        for (int i = 0; i < 5; i++){
            aQuoteLegValueState = new QuoteLegValueState();
            aQuoteLegValueState.setValueIndex(i);
            aQuoteLegValueState.setRatio(SwingGlobal.retrieveDoubleFromTextField(jRatioFieldList.get(i)));
            aQuoteLegValueState.setStrike(SwingGlobal.retrieveDoubleFromTextField(jStrikeFieldList.get(i)));
            aQuoteLegValueStateList.add(aQuoteLegValueState);
        }
        aQuoteLegState.setQuoteLegValueStates(aQuoteLegValueStateList.toArray(new QuoteLegValueState[0]));
        return aQuoteLegState;
    }

    private void setupOptionPriceTable() {
        jPriceFieldList = new ArrayList<JLabel>();
        jPriceFieldList.add(jPriceField1);
        jPriceFieldList.add(jPriceField2);
        jPriceFieldList.add(jPriceField3);
        jPriceFieldList.add(jPriceField4);
        jPriceFieldList.add(jPriceField5);
        jPriceFieldList.add(jPriceField6);
        
        jVoltList = new ArrayList<JLabel>();
        jVoltList.add(jVolt1);
        jVoltList.add(jVolt2);
        jVoltList.add(jVolt3);
        jVoltList.add(jVolt4);
        jVoltList.add(jVolt5);
        jVoltList.add(jVolt6);
        jVolt6.setVisible(false);
        
        jDeltaList = new ArrayList<JLabel>();
        jDeltaList.add(jDelta1);
        jDeltaList.add(jDelta2);
        jDeltaList.add(jDelta3);
        jDeltaList.add(jDelta4);
        jDeltaList.add(jDelta5);
        jDeltaList.add(jDelta6);
        
        jDdeltaList = new ArrayList<JLabel>();
        jDdeltaList.add(jDdelta1);
        jDdeltaList.add(jDdelta2);
        jDdeltaList.add(jDdelta3);
        jDdeltaList.add(jDdelta4);
        jDdeltaList.add(jDdelta5);
        jDdeltaList.add(jDdelta6);
        
        jVegaList = new ArrayList<JLabel>();
        jVegaList.add(jVega1);
        jVegaList.add(jVega2);
        jVegaList.add(jVega3);
        jVegaList.add(jVega4);
        jVegaList.add(jVega5);
        jVegaList.add(jVega6);
        
        jThetaList = new ArrayList<JLabel>();
        jThetaList.add(jTheta1);
        jThetaList.add(jTheta2);
        jThetaList.add(jTheta3);
        jThetaList.add(jTheta4);
        jThetaList.add(jTheta5);
        jThetaList.add(jTheta6);
        
        jGammaList = new ArrayList<JLabel>();
        jGammaList.add(jGamma1);
        jGammaList.add(jGamma2);
        jGammaList.add(jGamma3);
        jGammaList.add(jGamma4);
        jGammaList.add(jGamma5);
        jGammaList.add(jGamma6);
        
        jDgammaList = new ArrayList<JLabel>();
        jDgammaList.add(jDgamma1);
        jDgammaList.add(jDgamma2);
        jDgammaList.add(jDgamma3);
        jDgammaList.add(jDgamma4);
        jDgammaList.add(jDgamma5);
        jDgammaList.add(jDgamma6);
    }

    /**
     * Calculate each PUT/CALL option's prices
     * @param optionIndex 
     */
    private IPbsysOptionQuote calculateOptionPrice(int optionIndex) {
        IPbsysOptionQuote pricedQuote = owner.calculateOptionPrice(getLegId() - 1, optionIndex);
        if (pricedQuote != null){
            updateOptionPricesRowFields(pricedQuote.getPointBoxQuotePrices(), optionIndex);
        }
        return pricedQuote;
    }

    private String getCallPutSelectorValue(int index) {
        String value = jCallPutSelectorList.get(index).getSelectedItem().toString();
        if ("c".equalsIgnoreCase(value)){
            value = PointBoxQuoteStrategyTerm.CALL.toString();
        }else if ("p".equalsIgnoreCase(value)){
            value = PointBoxQuoteStrategyTerm.PUT.toString();
        }else{
            value = "[C/P?]";
        }
        return value;
    }

    private void calculateOptionPrice(JComboBox jCallPutSelector, int index) {
        String value = jCallPutSelector.getSelectedItem().toString();
        if (DataGlobal.isEmptyNullString(value)){
            return;
        }
        if ((value.equalsIgnoreCase("c")) || (value.equalsIgnoreCase("p"))){
            calculateOptionPrice(index);
        }
    }

//    /**
//     * Calculate totals of all the option prices in the table and return the total 
//     * as a PointBoxQuotePrices object back to the caller
//     * @return 
//     */
//    PointBoxQuotePrices calculateLegOptionPriceTotal() {
//        PointBoxQuotePrices result = new PointBoxQuotePrices();
//        IPbsysOptionQuote optionQuote;
//        int count = 0;
//        for (int optionIndex = 0; optionIndex < 5; optionIndex++){
//            optionQuote = calculateOptionPrice(optionIndex);
//            if (optionQuote != null){
//                result.setDdelta(result.getDdelta() + optionQuote.getDDelta());
//                result.setDelta(result.getDelta() + optionQuote.getDelta());
//                result.setDgamma(result.getDgamma() + optionQuote.getDGamma());
//                result.setGamma(result.getGamma() + optionQuote.getGamma());
//                result.setPrice(result.getPrice() + optionQuote.getPrice());
//                result.setTheta(result.getTheta()+ optionQuote.getTheta());
//                result.setVega(result.getVega()+ optionQuote.getVega());
//                result.setVolatility(result.getVolatility() + Math.abs(optionQuote.getVol()));
//                count++;
//            }
//        }//for
//        if (count > 0){
//            result.setVolatility(result.getVolatility()/count);
//        }
//        
//        updateTotalPricesFields(result);
//        
//        return result;
//    }

    private void updateOptionPricesRowFieldsHelper(final PointBoxQuotePrices pricedQuote, final int optionIndex) {
        if (optionIndex >= 6){
            return;
        }
        jPriceFieldList.get(optionIndex).setText(DataGlobal.formatDoubleWithMinMax(pricedQuote.getPrice(), 
                    owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()), 
                    owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        
        if (pricedQuote.getVolatility()> 0){
            jVoltList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getVolatility(), 4, "0"));
        }else{
            jVoltList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getVolatility(), 4, "0.0000"));
        }
        if (pricedQuote.getVega() > 0){
            jVegaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getVega(), 4, "0"));
        }else{
            jVegaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getVega(), 4, "0.0000"));
        }
        if (pricedQuote.getGamma() > 0){
            jGammaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getGamma(), 4, "0"));
        }else{
            jGammaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getGamma(), 4, "0.0000"));
        }
        if (pricedQuote.getDgamma() > 0){
            jDgammaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getDgamma(), 4, "0"));
        }else{
            jDgammaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getDgamma(), 4, "0.0000"));
        }
        if (pricedQuote.getTheta() > 0){
            jThetaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getTheta(), 4, "0"));
        }else{
            jThetaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getTheta(), 4, "0.0000"));
        }
        if (pricedQuote.getDelta() > 0){
            jDeltaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getDelta(), 4, "0"));
        }else{
            jDeltaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getDelta(), 4, "0.0000"));
        }
        if (pricedQuote.getDdelta() > 0){
            jDdeltaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getDdelta(), 4, "0"));
        }else{
            jDdeltaList.get(optionIndex).setText(PbcGlobal.localFormatStringByDoublePrecision(pricedQuote.getDdelta(), 4, "0.0000"));
        }
    }

    void updatePanelForPricedQuote(IPbsysOptionQuote quote) {
        ArrayList<IPbsysQuoteLeg> legs = quote.getOptionStrategyLegs();
        IPbsysQuoteLeg leg = legs.get(getLegId() - 1);
        ArrayList<PointBoxOption> aPointBoxOptionList = leg.getPointBoxOptionList();
        int optionIndex = 0;
        for (PointBoxOption aPointBoxOption : aPointBoxOptionList){
            updateOptionPricesRowFields(aPointBoxOption.getPrices(), optionIndex);
            optionIndex++;
        }
        updateOptionPricesRowFields(leg.getLegOptionPriceTotal(), 5);//todo-sim: 5 is hardcoded
        
        jSwapField.setValue(DataGlobal.formatDoubleWithMinMax(leg.getOptionSwap(),
                owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
    }

    private void updateOptionPricesRowFields(final PointBoxQuotePrices pricedQuotePrices, final int optionIndex) {
        if (pricedQuotePrices == null){
            return;
        }
        if (SwingUtilities.isEventDispatchThread()){
            updateOptionPricesRowFieldsHelper(pricedQuotePrices, optionIndex);
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    updateOptionPricesRowFieldsHelper(pricedQuotePrices, optionIndex);
                }
            });
        }
    }
    
//    private void updateTotalPricesFields(final PointBoxQuotePrices prices) {
//        updateOptionPricesRowFields(prices, 5); //index for TOTAL row is 5 (todo: hardcoded so far).
//    }
    
    void populateQuoteFromViewer(PointBoxQuoteCode code, IPbsysQuoteLeg quoteLeg) {
        populateCalendarForQuteFromViewer(code, quoteLeg);
        populateStructureCrossLiveForQuteFromViewer(quoteLeg);
        populateCrossSwapAdjFieldValuesForStripSelector(quoteLeg);
        populateStrikeRatioFieldsForQuteFromViewer(quoteLeg);
    }

    private void populateCalendarForQuteFromViewer(PointBoxQuoteCode code, IPbsysQuoteLeg quoteLeg) {
        if (PointBoxQuoteCode.IA.equals(code)){
            populateIaStyledCalendarForQuteFromViewerHelper(code, quoteLeg.getOptionContractStartDate(), jStartSelector);
            populateIaStyledCalendarForQuteFromViewerHelper(code, quoteLeg.getOptionContractEndDate(), jEndSelector);
        }else{
            populateCalendarForQuteFromViewerHelper(code, quoteLeg.getOptionContractStartDate(), jStartSelector);
            populateCalendarForQuteFromViewerHelper(code, quoteLeg.getOptionContractEndDate(), jEndSelector);
        }
    }

    private void populateStructureCrossLiveForQuteFromViewer(IPbsysQuoteLeg quoteLeg) {
        PointBoxQuoteStrategyTerm aPointBoxQuoteStrategyTerm = PointBoxQuoteStrategyTerm.convertEnumValueToType(quoteLeg.getOptionStrategy());
        String structure = quoteLeg.getOptionStrategy();
        if (PointBoxQuoteStrategyTerm.CUSTOM.equals(aPointBoxQuoteStrategyTerm)){
            jStructureSelector.removeItem(structure);
            jStructureSelector.addItem(structure);
            jStructureSelector.setSelectedItem(structure);
            return;
        }else{
            jStructureSelector.setSelectedItem(aPointBoxQuoteStrategyTerm.toString());
        }
        if (quoteLeg.isCrossEmbedded()){
            jLiveCheck.setSelected(false);
        }else{
            jLiveCheck.setSelected(true);
        }
        resetCrossTextField();
        jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(quoteLeg.getOptionCross(),
                                                              owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                                                              owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
    }

    /*
     * this method has to be invoked after populateStructureCrossLiveForQuteFromViewer
     * @param quoteLeg 
     */
    private void populateStrikeRatioFieldsForQuteFromViewer(IPbsysQuoteLeg quoteLeg) {
        ArrayList<PointBoxOption> aPointBoxOptionList = quoteLeg.getPointBoxOptionList();
        try{
            for (int i = 0; i < 5; i++){
                if (aPointBoxOptionList != null){
                    populateCallPutSelector(jCallPutSelectorList.get(i), aPointBoxOptionList.get(i).getCallPutTypeValue());
                }
                populateStrikeFormattedTextField(jStrikeFieldList.get(i), quoteLeg.getOptionStrikes()[i]);
                double factor = 1;
                if (aPointBoxOptionList != null){
                    if (PointBoxOptionPosition.Short.equals(aPointBoxOptionList.get(i).getOptionPosition())){
                        factor = -1;
                    }
                }
                //quoteLeg.getOptionRatios()[i] could be negative or positive for a number whose original is negative
                populateRatioFormattedTextField(jRatioFieldList.get(i), Math.abs(quoteLeg.getOptionRatios()[i])*factor);
                this.calculateOptionPrice(i);
            }//for
        }catch(Exception ex){}
    }

    private void populateStrikeFormattedTextField(JFormattedTextField textField, double strike) {
        if (textField.isEnabled()){
            textField.setText(DataGlobal.formatDoubleWithMinMax(strike,
                owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        }
    }

    private void populateCallPutSelector(JComboBox cpComboBox, PointBoxQuoteStrategyTerm cp) {
        switch (cp){
            case CALL:
                cpComboBox.setSelectedItem("c");
                break;
            case PUT:
                cpComboBox.setSelectedItem("p");
                break;
        }
    }

    private void populateCalendarForQuteFromViewerHelper(PointBoxQuoteCode code, GregorianCalendar optionContractDate, JComboBox jDateSelector) {
        LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData = owner.getKernel().retrieveAllDescriptiveExpirationData(code);
        Set<GregorianCalendar> keys = allDescriptiveExpData.keySet();
        Iterator<GregorianCalendar> itr = keys.iterator();
        GregorianCalendar date;
        String optionContractDateMMYYYY = CalendarGlobal.convertToContractMMMYY(optionContractDate);
        if (optionContractDateMMYYYY == null){
            return;
        }
        int index = 0;
        while(itr.hasNext()){
            date = itr.next();
            if (optionContractDateMMYYYY.equalsIgnoreCase(CalendarGlobal.convertToContractMMMYY(date))){
                jDateSelector.setSelectedIndex(index);
                break;
            }
            index++;
        }
    }

    private void populateIaStyledCalendarForQuteFromViewerHelper(PointBoxQuoteCode code, GregorianCalendar optionContractDate, JComboBox jDateSelector) {
        LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData = owner.getKernel().retrieveAllDescriptiveExpirationData(code);
        Set<GregorianCalendar> keys = allDescriptiveExpData.keySet();
        Iterator<GregorianCalendar> itr = keys.iterator();
        GregorianCalendar date;
        String optionContractMMMDD = CalendarGlobal.convertToIaStyledMMMDD(optionContractDate);
        if (optionContractMMMDD == null){
            return;
        }
        int index = 0;
        while(itr.hasNext()){
            date = itr.next();
            if (optionContractMMMDD.equalsIgnoreCase(CalendarGlobal.convertToIaStyledMMMDD(date))){
                jDateSelector.setSelectedIndex(index);
                break;
            }
            index++;
        }
    }

    private void populateRatioFormattedTextField(JFormattedTextField textField, double strike) {
        if (textField.isEnabled()){
            textField.setText(Double.toString(strike));
        }
    }

    /**
     * This method assumes that the quote-message is ready. Refer to isQuoteMessageReady().
     * @return 
     */
    String generateSimMarkFieldValuesToken() {
        //FROM#TO#STRUCTURE#CROSS#LIVE#SWAP#STRIKE#CP#RATIO(#STRIKE#CP#RATIO...)
        String token = getContractSimMarkFieldValue(jStartSelector) + PbcSimGuiParser.SimMarkValueDelimiter;
        token += getContractSimMarkFieldValue(jEndSelector) + PbcSimGuiParser.SimMarkValueDelimiter;
        token += getStructureValue(false) + PbcSimGuiParser.SimMarkValueDelimiter;
        token += getCrossValue() + PbcSimGuiParser.SimMarkValueDelimiter;
        token += getLiveSimMarkFieldValue() + PbcSimGuiParser.SimMarkValueDelimiter;
        token += getSwapSimMarkFieldValue() + PbcSimGuiParser.SimMarkValueDelimiter;
        String optionToken;
        for (int i = 0; i < 5; i++){
            optionToken = getOptionUnitSimMarkFieldValue(i);
            if (DataGlobal.isNonEmptyNullString(optionToken)){
                token += optionToken + PbcSimGuiParser.SimMarkValueDelimiter;
            }
        }
        return token.substring(0, token.lastIndexOf(PbcSimGuiParser.SimMarkValueDelimiter));
    }

    private String getLiveSimMarkFieldValue() {
        if (jLiveCheck.isSelected()){
            return PbcSimGuiParser.SimMarkYesValue;
        }else{
            return PbcSimGuiParser.SimMarkNoValue;
        }
    }
    
    private String getSwapSimMarkFieldValue() {
        if (DataGlobal.isEmptyNullString(jSwapField.getText())){
            return PbcSimGuiParser.SimMarkNoValue;
        }else{
            return jSwapField.getText();
        }
    }


    /**
     * #STRIKE#CP#RATIO
     * 
     * @param optionIndex
     * @return - if it is NULL, it means no value for this optionIndexed option unit
     */
    private String getOptionUnitSimMarkFieldValue(int optionIndex) {
        //strike
        String token = getStrikeValueToken(jStrikeFieldList.get(optionIndex)) + PbcSimGuiParser.SimMarkValueDelimiter;
        if (token.contains("?")){
            return null;
        }
        //C or P
        String value = getCallPutSelectorValue(optionIndex);
        if (DataGlobal.isEmptyNullString(value)){
            return null;
        }else{
            token += value + PbcSimGuiParser.SimMarkValueDelimiter;
        }
        //Ratio
        value = jRatioFieldList.get(optionIndex).getText();
        if (DataGlobal.isEmptyNullString(value)){
            value = "1.0";
        }
        token += value;
        
        return token;
    }

    boolean isCustomSimLeg() {
        String s = getStructureValue(false);
        boolean result = DataGlobal.isEmptyNullString(s);
        if (!result){
            result = PointBoxQuoteStrategyTerm.CUSTOM.equals(PointBoxQuoteStrategyTerm.convertEnumValueToType(s));
        }
        return result;
    }

    private void replaceCrossSpinner() {
        if (SwingUtilities.isEventDispatchThread()){
            replaceCrossSpinnerHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    replaceCrossSpinnerHelper();
                }
            });
        }
    }
    
    private void replaceCrossSpinnerHelper(){
        int index = jVolPanel.getComponentZOrder(jCrossSpinner);
        jVolPanel.remove(index);
        jVolPanel.add(jCrossTextField, index);
        jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        jCrossTextField.setEnabled(false);
        jCrossTextField.setInputVerifier(new SimFormattedValueInputVerifier());
        jCrossTextField.addPropertyChangeListener("value", new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double value = SwingGlobal.retrieveDoubleFromTextField(jCrossTextField);
                if (value == 0.0) {
    //                    tableModel.inputStrikes.put(0, "");
                    jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(0.0,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                } else {
    //                    tableModel.inputStrikes.put(0, value);
                    jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(value,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
                    owner.populateQuoteMessageTextField();
                }   
            }
        });
    
    }
    
    private void resetCrossTextField(){
        previousUserInputCross = DataGlobal.convertToDouble(jCrossTextField.getText());
        if (jLiveCheck.isSelected()){
            jCrossTextField.setEnabled(false);
//            jCrossSpinner.setEnabled(false);
            jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(previousUserInputCross,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        }else{
            jCrossTextField.setEnabled(true);
//            jCrossSpinner.setEnabled(true);
            jCrossTextField.setValue(DataGlobal.formatDoubleWithMinMax(previousUserInputCross,
                            owner.getFormatValueMinForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode()),
                            owner.getFormatValueMaxForSelectedPointBoxQuoteCode(owner.getSelectedPointBoxQuoteCode())));
        }
    }
    
    class DateSelectorObject{
        
        private IPointBoxQuoteMonthFirstDate aPointBoxQuoteMonthFirstDate;
        private String descriptiveDate;

//        public DateSelectorObject(IPointBoxQuoteMonthFirstDate date) {
//            this.date = date;
//        }

        private DateSelectorObject(GregorianCalendar contractDate, String descriptiveDate) {
            aPointBoxQuoteMonthFirstDate = PointBoxCalendarFactory.createPointBoxQuoteMonthFirstDateInstance();
            aPointBoxQuoteMonthFirstDate.setDateTime(contractDate);
            this.descriptiveDate = descriptiveDate;
        }

        public IPointBoxQuoteMonthFirstDate getDate() {
            return aPointBoxQuoteMonthFirstDate;
        }
        
        public GregorianCalendar getDateTime(){
            return aPointBoxQuoteMonthFirstDate.getDateTime();
        }

        @Override
        public String toString() {
//            String year = Integer.toString(aPointBoxQuoteMonthFirstDate.getCalendarYear());
//            return CalendarGlobal.convertToAbbrStringMonth(aPointBoxQuoteMonthFirstDate.getCalendarMonth()+1) + PbcSimGuiParser.CalendarDelimiter + year.substring(2);
            return descriptiveDate;
        }

        private String toMMddyyyy() {
            return CalendarGlobal.convertToMMDDYYYY(aPointBoxQuoteMonthFirstDate.getDateTime());
        }

        private String getDescriptiveDate() {
            return descriptiveDate;
        }

        private String toFinancialMonthYear() {
            return CalendarGlobal.convertToFinancialMonth(aPointBoxQuoteMonthFirstDate.getCalendarMonth()+1).toUpperCase() + Integer.toString(aPointBoxQuoteMonthFirstDate.getCalendarYear()).substring(2);
        }
        
    }  
    
}

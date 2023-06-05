package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pricer.host.BlackScholes;
import java.awt.Color;
import java.awt.Component;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import com.eclipsemarkets.pricer.data.AtmVolCurveData;
import com.eclipsemarkets.pricer.data.ExpirationsData;
import com.eclipsemarkets.pricer.data.IPriceCurveData;
import com.eclipsemarkets.pricer.data.VolSkewSurfaceData;
import com.eclipsemarkets.pricer.data.PbcInterestRateData;
import com.eclipsemarkets.pricer.data.PbsupportReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christine.Kim
 */
public class PricingCompTableModel extends AbstractPbcCurveTableModel {

    private static final int CONTRACT_INDEX = 0;
    private static final int PRICE_INDEX = 1;
    private static final int SPACER_INDEX = 2;
    protected static final int OPTION_1_INDEX = 3;
    protected static final int OPTION_2_INDEX = 4;
    protected static final int OPTION_3_INDEX = 5;
    protected static final int OPTION_4_INDEX = 6;
    protected static final int OPTION_5_INDEX = 7;
    private static final int TOTAL_INDEX = 8;

    private static final String[] columnNames = {"Contract", "Price", "", "Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Total"};

    private PricingCompFrame targetFrame;

    private ExpirationsData expirationsData;
    private Vector<GregorianCalendar> allContractsData;
    private Vector<Integer> allDteData;
    private LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData;

    private IPriceCurveData priceCurveData;
    //private LinkedHashMap<String, PriceCurveDataPoint> allPriceCurveData;

    private Vector<GregorianCalendar> allHolidaysData;
    //private LiborCurveData liborCurveData;
    private PbcInterestRateData aPbcInterestRateData;

    //private ContractIRCurvesData contractIRCurveData;
    //private LinkedHashMap<GregorianCalendar, Double> contractIRDataByProduct;

    private AtmVolCurveData atmVolCurveData;
    private LinkedHashMap<GregorianCalendar, Double> allAtmVolCurveData;

    private VolSkewSurfaceData volSkewSurfaceData;

    protected HashMap<Integer, Double> inputStrikes;
    protected HashMap<Integer, String> inputCallPuts;
    protected HashMap<Integer, Double> inputRatios;

    private HashMap<Integer, Double> option1Values;
    private HashMap<Integer, Double> option2Values;
    private HashMap<Integer, Double> option3Values;
    private HashMap<Integer, Double> option4Values;
    private HashMap<Integer, Double> option5Values;

    public PricingCompTableModel(PricingCompFrame frame) {
        this.targetFrame = frame;
        
        readData();

        inputStrikes = new HashMap<Integer, Double>();
        inputStrikes.put(OPTION_1_INDEX, 0.0);
        inputStrikes.put(OPTION_2_INDEX, 0.0);
        inputStrikes.put(OPTION_3_INDEX, 0.0);
        inputStrikes.put(OPTION_4_INDEX, 0.0);
        inputStrikes.put(OPTION_5_INDEX, 0.0);

        inputCallPuts = new HashMap<Integer, String>();
        inputCallPuts.put(OPTION_1_INDEX, "");
        inputCallPuts.put(OPTION_2_INDEX, "");
        inputCallPuts.put(OPTION_3_INDEX, "");
        inputCallPuts.put(OPTION_4_INDEX, "");
        inputCallPuts.put(OPTION_5_INDEX, "");

        inputRatios = new HashMap<Integer, Double>();
        inputRatios.put(OPTION_1_INDEX, 0.0);
        inputRatios.put(OPTION_2_INDEX, 0.0);
        inputRatios.put(OPTION_3_INDEX, 0.0);
        inputRatios.put(OPTION_4_INDEX, 0.0);
        inputRatios.put(OPTION_5_INDEX, 0.0);

        option1Values = new HashMap<Integer, Double>();
        option2Values = new HashMap<Integer, Double>();
        option3Values = new HashMap<Integer, Double>();
        option4Values = new HashMap<Integer, Double>();
        option5Values = new HashMap<Integer, Double>();
    }

    public void setUpColumns(JTable baseTable) {
        setUpSingleColumn(baseTable, CONTRACT_INDEX, 60);
        setUpSingleColumn(baseTable, PRICE_INDEX, 80);
        setUpSingleColumn(baseTable, SPACER_INDEX, 20);
        setUpSingleColumn(baseTable, OPTION_1_INDEX, 76);
        setUpSingleColumn(baseTable, OPTION_2_INDEX, 76);
        setUpSingleColumn(baseTable, OPTION_3_INDEX, 76);
        setUpSingleColumn(baseTable, OPTION_4_INDEX, 76);
        setUpSingleColumn(baseTable, OPTION_5_INDEX, 76);
        setUpSingleColumn(baseTable, TOTAL_INDEX, 100);
    }

    private void setUpSingleColumn(JTable baseTable, int columnIndex, int preferredWidth) {
        TableColumn column = baseTable.getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(preferredWidth);
        column.setResizable(false);
        column.setCellRenderer(new CompRenderer());
    }

    public void reloadData() {
        //allPriceCurveData = null;
        priceCurveData = null;
        allHolidaysData = null;
        aPbcInterestRateData = null;
        //liborCurveData = null;
        //contractIRDataByProduct = null;
        //contractIRCurveData = null;
        allAtmVolCurveData = null;
        atmVolCurveData = null;
        volSkewSurfaceData = null;
        readData();
        
        if (SwingUtilities.isEventDispatchThread()) {
            fireTableDataChanged();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireTableDataChanged();
                }
            });
        }
    }

    private void readData() {
        try{
            expirationsData = PbsupportReader.readExpirationsFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.Expirations, true));
            allContractsData = expirationsData.getAllContractsData();
            allDescriptiveExpData = expirationsData.getAllDescriptiveExpData();
            allDteData = expirationsData.getAllDteData();

            priceCurveData = PbsupportReader.readPriceCurveFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.Underlier, true));
            priceCurveData.checkToAddContracts(allContractsData);

            allHolidaysData = PbsupportReader.readHolidaysFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.Holidays, true)).getAllHolidaysData();

            aPbcInterestRateData = PbsupportReader.retrievePbcInterestRateData(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.InterestRate, true), 
                                                                                     allHolidaysData, 
                                                                                     allContractsData);

            atmVolCurveData = PbsupportReader.readAtmVolCurveFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.AtmVolCurve, true));
            atmVolCurveData.checkToAddContracts(allContractsData);
            allAtmVolCurveData = atmVolCurveData.getAllAtmVolCurveData();

            volSkewSurfaceData = PbsupportReader.readVolSkewSurfaceFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.VolSkewSurface, true));
            volSkewSurfaceData.checkToAddContracts(allContractsData);
        }catch (Exception ex){
            Logger.getLogger(PricingCompTableModel.class.getName()).log(Level.SEVERE, null, ex);
            displayCurveWarningMessage();
        }
    }

    @Override
    public int getRowCount() {
        return allContractsData.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {

        switch (column) {
            case CONTRACT_INDEX:
                return allDescriptiveExpData.get(allContractsData.get(row));
            case PRICE_INDEX:
                return FormatterCommons.format4Dec(priceCurveData.getDataAt(row).getPrice());
                //return FormatterCommons.format4Dec(allPriceCurveData.get("@" + (row + 1) + "ng").getPrice());
            case SPACER_INDEX:
                return "";
            case OPTION_1_INDEX:
            case OPTION_2_INDEX:
            case OPTION_3_INDEX:
            case OPTION_4_INDEX:
            case OPTION_5_INDEX:

                double value = 0.0;

                if (inputStrikes.get(column) != 0.0 && !inputCallPuts.get(column).isEmpty() && inputRatios.get(column) != 0.0) {
                    double price = priceCurveData.getDataAt(row).getPrice();
                    //double price = allPriceCurveData.get("@" + (row + 1) + "ng").getPrice();
                    double strike = inputStrikes.get(column);
                    double vol = volSkewSurfaceData.getInterpolatedVol(allContractsData.get(row),
                                                                       allAtmVolCurveData.get(allContractsData.get(row)),
                                                                       strike - price);
                    double dte = allDteData.get(row);
                    double t = dte / 365.0;
                    double ir = aPbcInterestRateData.retrieveIR(dte, allContractsData.get(row));
                    String opt = inputCallPuts.get(column);
                    double ratio = inputRatios.get(column);
                    value = ratio * BlackScholes.BlackScholesPricer(price, strike, vol, t, ir, opt);
                }

                switch (column) {
                    case OPTION_1_INDEX:
                        option1Values.put(row, value);
                        break;
                    case OPTION_2_INDEX:
                        option2Values.put(row, value);
                        break;
                    case OPTION_3_INDEX:
                        option3Values.put(row, value);
                        break;
                    case OPTION_4_INDEX:
                        option4Values.put(row, value);
                        break;
                    case OPTION_5_INDEX:
                        option5Values.put(row, value);
                        break;
                }

                return FormatterCommons.format4Dec(value);

            case TOTAL_INDEX:
                return FormatterCommons.format4Dec(option1Values.get(row) + option2Values.get(row) + option3Values.get(row) + option4Values.get(row) + option5Values.get(row));

            default:
                return new Object();
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case CONTRACT_INDEX:
            case SPACER_INDEX:
                return String.class;
            default:
                return Double.class;
        }
    }

    public void loadWhenTechData() {
        // Do not implement
    }

////////////////////////////////////////////////////////////////////////////////
    class CompRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            //System.out.println("row: " + row + ", col: " + column);
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            switch (column) {
                case CONTRACT_INDEX:
                    setHorizontalAlignment(JLabel.LEFT);
                    setFont(SwingGlobal.getTahomaBoldedFont11());
                    break;
                case PRICE_INDEX:
                case SPACER_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    break;
                case OPTION_1_INDEX:
                case OPTION_2_INDEX:
                case OPTION_3_INDEX:
                case OPTION_4_INDEX:
                case OPTION_5_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    if (inputStrikes.get(column) != 0.0 && !inputCallPuts.get(column).isEmpty() && inputRatios.get(column) != 0.0) {
                        setForeground(Color.BLACK);
                    } else {
                        setForeground(Color.WHITE);
                    }
                    break;
                case TOTAL_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    setFont(SwingGlobal.getTahomaBoldedFont12());
                    setBackground(new Color(121, 210, 255));
                    break;
            }

            return c;
        }
    }
}

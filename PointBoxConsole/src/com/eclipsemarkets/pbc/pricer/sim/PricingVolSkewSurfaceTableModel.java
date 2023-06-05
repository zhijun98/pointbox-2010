package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.global.SwingGlobal;
import com.eclipsemarkets.pricer.host.BlackScholes;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import com.eclipsemarkets.pricer.data.AtmVolCurveData;
import com.eclipsemarkets.pricer.data.ExpirationsData;
import com.eclipsemarkets.pricer.data.IPriceCurveData;
import com.eclipsemarkets.pricer.data.PbcInterestRateData;
import com.eclipsemarkets.pricer.data.PbsupportReader;
import com.eclipsemarkets.pricer.data.VolSkewSurfaceData;
import com.eclipsemarkets.pricer.data.VolSkewSurfaceDataPoints;
import java.awt.Color;
import java.awt.Component;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author Christine.Kim
 */
public class PricingVolSkewSurfaceTableModel extends AbstractPbcCurveTableModel {

    public static final int CONTRACT_INDEX = 0;
    public static final int PRICE_INDEX = 1;
    public static final int ATM_INDEX = 2;
    public static final int ATM_VOL_INDEX = 3;
    public static final int ATM_STRADDLE_INDEX = 4;
    public static final int SPACER_INDEX = 5;
    public static final int SKEW_START_INDEX = 6;

    private static final String[] columnNames = {"Contract", "Price", "ATM", "ATM Vol(%)", "ATM Straddle", ""};

    private PricingVolSkewSurfaceFrame targetFrame;

    private ExpirationsData expirationsData;
    private Vector<GregorianCalendar> allContractsData;
    private Vector<Integer> allDteData;
    private LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData;
    
    private IPriceCurveData priceCurveData;
    private Vector<Double> allAtmPricesData;

    private Vector<GregorianCalendar> allHolidaysData;
    //private LiborCurveData liborCurveData;
    private PbcInterestRateData aPbcInterestRateData;
    
    private AtmVolCurveData atmVolCurveData;
    private LinkedHashMap<GregorianCalendar, Double> allAtmVolCurveData;

    private VolSkewSurfaceData volSkewSurfaceData;
    private Vector<Double> allVolStrikesData;
    private LinkedHashMap<GregorianCalendar, VolSkewSurfaceDataPoints> allVolSkewPointsData;

    public PricingVolSkewSurfaceTableModel(PricingVolSkewSurfaceFrame frame) {
        this.targetFrame = frame;
        readData();
    }

    public Vector<Double> getAllVolStrikesData() {
        return allVolStrikesData;
    }

    public void setUpColumns(JTable baseTable) {
        for (int i = 0; i < baseTable.getColumnCount(); i++) {
            switch (i) {
                case CONTRACT_INDEX:
                    setUpSingleColumn(baseTable, i, 60);
                    break;
                case PRICE_INDEX:
                    setUpSingleColumn(baseTable, i, 80);
                    break;
                case ATM_INDEX:
                    setUpSingleColumn(baseTable, i, 40);
                    break;
                case ATM_VOL_INDEX:
                    setUpSingleColumn(baseTable, i, 70);
                    break;
                case ATM_STRADDLE_INDEX:
                    setUpSingleColumn(baseTable, i, 78);
                    break;
                case SPACER_INDEX:
                    setUpSingleColumn(baseTable, i, 20);
                    break;
                default:
                    setUpSingleColumn(baseTable, i, 47);
                    break;
            }
        }
    }

    private void setUpSingleColumn(JTable baseTable, int columnIndex, int preferredWidth) {
        TableColumn column = baseTable.getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(preferredWidth);
        column.setResizable(false);
        column.setCellRenderer(new VolSkewSurfaceRenderer());
    }

    public void reloadData() {
        allAtmPricesData = null;
        priceCurveData = null;
        allAtmVolCurveData = null;
        atmVolCurveData = null;
        allVolStrikesData = null;
        allVolSkewPointsData = null;
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
            allAtmPricesData = priceCurveData.getAllAtmPricesData(allContractsData, targetFrame.getTargetCode());

            allHolidaysData = PbsupportReader.readHolidaysFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.Holidays, true)).getAllHolidaysData();
            aPbcInterestRateData = PbsupportReader.retrievePbcInterestRateData(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.InterestRate, true), 
                                                                                allHolidaysData, 
                                                                                allContractsData);

            atmVolCurveData = PbsupportReader.readAtmVolCurveFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.AtmVolCurve, true));
            atmVolCurveData.checkToAddContracts(allContractsData);
            allAtmVolCurveData = atmVolCurveData.getAllAtmVolCurveData();

            volSkewSurfaceData = PbsupportReader.readVolSkewSurfaceFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.VolSkewSurface, true));
            volSkewSurfaceData.checkToAddContracts(allContractsData);
            allVolStrikesData = volSkewSurfaceData.getAllVolStrikesData();
            allVolSkewPointsData = volSkewSurfaceData.getAllVolSkewPointsData();
        }catch (Exception ex){
            Logger.getLogger(PricingVolSkewSurfaceTableModel.class.getName()).log(Level.SEVERE, null, ex);
            displayCurveWarningMessage();
        }
    }
    
    @Override
    public int getRowCount() {
        return allContractsData.size();
    }

    @Override
    public int getColumnCount() {
        return allVolStrikesData.size() + columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {

        switch (column) {
            case CONTRACT_INDEX:
                return allDescriptiveExpData.get(allContractsData.get(row));
            case PRICE_INDEX:
                return FormatterCommons.format4Dec(priceCurveData.getDataAt(row).getPrice());
           case ATM_INDEX:
                return FormatterCommons.format2Dec(allAtmPricesData.get(row));
            case ATM_VOL_INDEX:
                return FormatterCommons.format3Dec(allAtmVolCurveData.get(allContractsData.get(row)) * 100.0);  //vol in %
            case ATM_STRADDLE_INDEX:
                double value;
                double price = priceCurveData.getDataAt(row).getPrice();
                double strike = allAtmPricesData.get(row);                
                double vol = volSkewSurfaceData.getInterpolatedVol(allContractsData.get(row), 
                                                                   allAtmVolCurveData.get(allContractsData.get(row)),
                                                                   strike - price);
                double dte = allDteData.get(row);
                double t = dte / 365.0;
                double ir = aPbcInterestRateData.retrieveIR(dte, allContractsData.get(row));
                value = BlackScholes.BlackScholesPricer(price, strike, vol, t, ir, "c") +
                        BlackScholes.BlackScholesPricer(price, strike, vol, t, ir, "p");
                return FormatterCommons.format4Dec(value);
            case SPACER_INDEX:
                return "";
            default:
                return FormatterCommons.format3Dec(allVolSkewPointsData.get(allContractsData.get(row)).getAllSkewDataPoints().get(allVolStrikesData.get(column - SKEW_START_INDEX)));
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        try {
            switch (column) {
                case ATM_INDEX:
                    allAtmPricesData.setElementAt(Math.round(DataGlobal.convertToDouble(value.toString()) * 20.0) / 20.0, row);
                    break;
                case ATM_VOL_INDEX:
                    allAtmVolCurveData.put(allContractsData.get(row), new Double(value.toString()) / 100.0);    //vol in decimal
                    break;
                default:
                    allVolSkewPointsData.get(allContractsData.get(row)).getAllSkewDataPoints().put(allVolStrikesData.get(column - SKEW_START_INDEX), new Double(value.toString()));
                    break;
            }
            fireTableRowsUpdated(row, row);
            
        } catch (Exception ex) {
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        switch (column) {
            case CONTRACT_INDEX:
            case PRICE_INDEX:
            case ATM_STRADDLE_INDEX:
            case SPACER_INDEX:
                return false;
            default:
                return true;
        }
    }

    @Override
    public String getColumnName(int column) {

        switch (column) {
            case CONTRACT_INDEX:
            case PRICE_INDEX:
            case ATM_INDEX:
            case ATM_VOL_INDEX:
            case ATM_STRADDLE_INDEX:
            case SPACER_INDEX:
                return columnNames[column];
            default:
                return FormatterCommons.format2Dec(allVolStrikesData.get(column - SKEW_START_INDEX));
        }
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

    public Vector<GregorianCalendar> getAllContractsData() {
        return allContractsData;
    }

    public AtmVolCurveData getAtmVolCurveData() {
        return atmVolCurveData;
    }

    public VolSkewSurfaceData getVolSkewSurfaceData() {
        return volSkewSurfaceData;
    }   

////////////////////////////////////////////////////////////////////////////////
    class VolSkewSurfaceRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            TableColumn col;
            switch (column) {
                case CONTRACT_INDEX:
                    setHorizontalAlignment(JLabel.LEFT);
                    setForeground(Color.BLACK);
                    break;
                case PRICE_INDEX:
                case ATM_STRADDLE_INDEX:
                case SPACER_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    setFont(SwingGlobal.getTahomaPlainFont11());
                    setForeground(Color.BLACK);
                    break;
                case ATM_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    setFont(SwingGlobal.getTahomaPlainFont11());
                    setForeground(Color.BLACK);
                    setBackground(new Color(217,215,215));
                                       
//                    col = table.getColumnModel().getColumn(column);
//                    col.setCellEditor(new MySpinnerEditor(value,3)); 
                    break;
                case ATM_VOL_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    setFont(SwingGlobal.getTahomaPlainFont11());
                    setForeground(Color.BLACK);
                    setBackground(new Color(217,215,215));
                                       
                    col = table.getColumnModel().getColumn(column);
                    col.setCellEditor(new MySpinnerEditor(value,3)); 
                    break;
                default:
                    setHorizontalAlignment(JLabel.RIGHT);
                    double temp = DataGlobal.convertToDouble(value.toString());
                    if (temp == 0.0) {
                        setFont(SwingGlobal.getTahomaBoldedFont11());
                        setForeground(Color.BLUE);
                    } else if (temp < 0.0) {
                        setFont(SwingGlobal.getTahomaPlainFont11());
                        setForeground(Color.RED);
                    } else {
                        setFont(SwingGlobal.getTahomaPlainFont11());
                        setForeground(Color.BLACK);
                    }
                    if (getColumnName(column).equals("0.00")) {
                        setBackground(new Color(217,215,215));
                    }
                    break;
            }

            return c;
        }
    }
}

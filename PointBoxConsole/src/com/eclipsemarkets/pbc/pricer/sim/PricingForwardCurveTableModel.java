package com.eclipsemarkets.pbc.pricer.sim;

import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.global.DataGlobal;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleAccessorySettings;
import java.awt.Color;
import java.awt.Component;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import com.eclipsemarkets.pricer.commons.FormatterCommons;
import com.eclipsemarkets.pricer.data.ExpirationsData;
import com.eclipsemarkets.pricer.data.IPriceCurveData;
import com.eclipsemarkets.pricer.data.PbsupportReader;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christine.Kim
 */
public class PricingForwardCurveTableModel extends AbstractPbcCurveTableModel {

    public static final int CONTRACT_INDEX = 0;
//    public static final int NYMEX_SETTLE_INDEX = 1;
    public static final int PRICE_INDEX = 1;
    public static final int SPREAD_INDEX = 2;

    //private static final String[] columnNames = {"Contract", "NYMEX Settle", "Price", "Spread"};
    private static final String[] columnNames = {"Contract", "Price", "Spread"};

    private PricingForwardCurveFrame targetFrame;

    private Vector<GregorianCalendar> allContractsData;
    private LinkedHashMap<GregorianCalendar, String> allDescriptiveExpData;

    private IPriceCurveData priceCurveData;
    private HashMap<String, Double> allNymexSettleData;

    public PricingForwardCurveTableModel(PricingForwardCurveFrame frame) {
        this.targetFrame = frame;
        readData();
    }

    public void setUpColumns(JTable baseTable) {
        setUpSingleColumn(baseTable, CONTRACT_INDEX, 60);
//        setUpSingleColumn(baseTable, NYMEX_SETTLE_INDEX, 80);
        setUpSingleColumn(baseTable, PRICE_INDEX, 80);
        setUpSingleColumn(baseTable, SPREAD_INDEX, 70);
    }

    private void setUpSingleColumn(JTable baseTable, int columnIndex, int preferredWidth) {
        TableColumn column = baseTable.getColumnModel().getColumn(columnIndex);
        column.setPreferredWidth(preferredWidth);
        column.setResizable(false);
        column.setCellRenderer(new PriceCurveRenderer());
    }

    public void reloadData() {
        priceCurveData = null;
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
            ExpirationsData expirationsData = PbsupportReader.readExpirationsFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.Expirations, true));
            allContractsData = expirationsData.getAllContractsData();
            allDescriptiveExpData = expirationsData.getAllDescriptiveExpData();
            priceCurveData = PbsupportReader.readPriceCurveFile(targetFrame.getKernel().getLocalCurveFileFullPath(targetFrame.getTargetCode(), PointBoxCurveType.Underlier, true));
            priceCurveData.checkToAddContracts(allContractsData);
        }catch (Exception ex){
            Logger.getLogger(PricingForwardCurveTableModel.class.getName()).log(Level.SEVERE, null, ex);
            displayCurveWarningMessage();
        }
    }

    protected void loadNymexSettles() {
        try {
            if (allNymexSettleData == null) {

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        IPbconsoleAccessorySettings accessoryFileSettings = targetFrame.getKernel().getPointBoxConsoleRuntime().getPbconsoleAccessorySettings();
                        allNymexSettleData = PbsupportReader.readNymexSettleFtp(accessoryFileSettings.getNymexFtpAddressRecord().getFilePath(), priceCurveData.getNymexHeader());
                        return null;
                    }

                    @Override
                    protected void done() {
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
                };
                worker.execute();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(targetFrame,
                                          "Failed to read in NYMEX Settlements. Please check your NYMEX FTP address.");
        }
    }

    protected void hideNymexSettles() {
        //redo this later so instead of null
        allNymexSettleData.clear();
        allNymexSettleData = null;

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
//                return CalendarGlobal.convertToContractMMMYY(allContractsData.get(row));
                return allDescriptiveExpData.get(allContractsData.get(row));
//            case NYMEX_SETTLE_INDEX:
//                if (allNymexSettleData != null) {
//                    return FormatterCommons.format4Dec(allNymexSettleData.get(CalendarGlobal.convertToContractMMMYY(allContractsData.get(row))));
//                } else {
//                    return "";
//                }
            case PRICE_INDEX:
                return FormatterCommons.format4Dec(priceCurveData.getDataAt(row).getPrice());
            case SPREAD_INDEX:
                return FormatterCommons.format4Dec(priceCurveData.getDataAt(row).getSpread());
            default:
                return new Object();
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {

        try {
            switch (column) {
                case PRICE_INDEX:
                    priceCurveData.getDataAt(row).setPrice(DataGlobal.convertToDouble(value.toString()));
                    break;
                case SPREAD_INDEX:
                    priceCurveData.getDataAt(row).setSpread(DataGlobal.convertToDouble(value.toString()));
                    break;
            }
            fireTableCellUpdated(row, column);
        } catch (Exception ex) {
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {

        switch (column) {
            case PRICE_INDEX:
               //return row == 0;
                return true;
            case SPREAD_INDEX:
                return row != 0;
            default:
                return false;
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
                return String.class;
//            case NYMEX_SETTLE_INDEX:
            case PRICE_INDEX:
            case SPREAD_INDEX:
                return Double.class;
            default:
                return Object.class;
        }
    }

    public IPriceCurveData getPriceCurveData() {
        return priceCurveData;
    }

    public Vector<GregorianCalendar> getAllContractsData() {
        return allContractsData;
    }

    class PriceCurveRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            //System.out.println("row: " + row + ", col: " + column);
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            hideFirstSpreadRow(row, column);
            setColorEditable(row, column);

            switch (column) {
                case CONTRACT_INDEX:
                    setHorizontalAlignment(JLabel.LEFT);
                    break;
//                case NYMEX_SETTLE_INDEX:
                case PRICE_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    if(!value.toString().isEmpty()){
                        Double v=Double.parseDouble(value.toString());
                        TableColumn col = table.getColumnModel().getColumn(column);
                        col.setCellEditor(new MySpinnerEditor(v,4)); 
                        break;
                    }
                case SPREAD_INDEX:
                    setHorizontalAlignment(JLabel.RIGHT);
                    break;
            }

            return c;
        }

        private void hideFirstSpreadRow(int row, int column) {
            if (row == 0 && column == SPREAD_INDEX) {
                setForeground(Color.WHITE);
            } else {
                setForeground(Color.BLACK);
            }
        }

        private void setColorEditable(int row, int column) {
            if (row != 0 && column == SPREAD_INDEX) {
                setBackground(new Color(217, 215, 215));
            } else if (row == 0 && column == PRICE_INDEX) {
                setBackground(new Color(217, 215, 215));
            } else {
                setBackground(Color.WHITE);
            }
        }
    }
}

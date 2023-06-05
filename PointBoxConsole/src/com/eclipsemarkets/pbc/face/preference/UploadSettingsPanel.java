/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.kernel.PointBoxConsoleProperties;
import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.runtime.IPointBoxPricingSettings;
import com.eclipsemarkets.web.PbcAccountBasedSettings;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This class has bad implementation. If it is for NG, it is OK. For other types 
 * such as CO, it need redesign. ZZJ
 * 
 * @author chen.yuan
 */
public class UploadSettingsPanel extends JPanel implements IPreferenceComponentPanel {
        
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(FileSettingsPanel.class.getName());
    }
    
    //Fake servlet in local server
    private static final String SERVER_URL = "http://localhost:8080/ServletTest/UploadServlet";
    
    public static final int SUCCESSFUL = 0;
    public static final int NO_FILE_SELECTED = 1;
    public static final int FAILED = 2;
    
    IPbcFace face;
    private PointBoxConsoleProperties prop;
    /**
     * This is possibly wrong although CO may have multiple symbols
     */
    private ArrayList<UploadControlPanel> symbolCheckBoxes;
    
    /**
     * Creates new form UploadSettingsPanel
     */
    public UploadSettingsPanel(IPbcFace face) {
        this.face = face;
        prop = PointBoxConsoleProperties.getSingleton();
        symbolCheckBoxes = new ArrayList<UploadControlPanel>();
        initComponents();
        
        addLNPanel(face.getKernel().getPointBoxAccountID().getPbcAccountBasedSettings());
        //addAOPanel();
    }
        
    private void addLNPanel(PbcAccountBasedSettings aPbcPricingAdminSettings) {
        UploadControlPanel lnPanel = new UploadControlPanel(PointBoxQuoteCode.LN, aPbcPricingAdminSettings);
        lnPanel.add(new JCheckBox("Underlier"), PointBoxCurveType.Underlier);
        lnPanel.add(new JCheckBox("ATM Vol Curve"), PointBoxCurveType.AtmVolCurve);
        lnPanel.add(new JCheckBox("Vol Skew Surface"), PointBoxCurveType.VolSkewSurface);
        lnPanel.add(new JCheckBox("Interest Rate"), PointBoxCurveType.InterestRate);
        lnPanel.add(new JCheckBox("Expiration"), PointBoxCurveType.Expirations);
        lnPanel.add(new JCheckBox("Holidays"), PointBoxCurveType.Holidays);
        symbolCheckBoxes.add(lnPanel);
        this.add(lnPanel, BorderLayout.LINE_START);  
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder("Select Products"));
        setLayout(new java.awt.GridLayout(0, 1));
        getAccessibleContext().setAccessibleParent(this);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    //Read settings from memory
    @Override
    public void populateSettings() {
        if (SwingUtilities.isEventDispatchThread()){
            populateSettingsHelper();
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    populateSettingsHelper();
                }
            });
        }    
    }

    private void populateSettingsHelper(){   
        setCheckBox();
    }
    
    private void setCheckBox() {
        if (prop == null) {
            return;
        } 
        
        String status = prop.retrieveUploadStatus();
        if (status != null) {
            int index = 0;
            for (UploadControlPanel panel : symbolCheckBoxes) {
                if (index >= status.length()) {
                    return;
                }
                index += panel.setStatus(status.substring(index, index+panel.getCheckBoxCount()));
            }
        }    
    }

    //Save checkbox
    @Override
    public void updateSettings() {
        if (SwingUtilities.isEventDispatchThread()) {
            updateSettingsHelper();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSettingsHelper();
                }
            });
        }
    }
    
    private void updateSettingsHelper() {
        StringBuilder status = new StringBuilder();
        for (UploadControlPanel panel : symbolCheckBoxes) {
            panel.getStatus(status);
        }
        prop.storeUploadStatus(status.toString());
    }

    ArrayList<PointBoxCurveType> getSelectedFileTypesForPricingSettingsUploadAdmin() {
        setCheckBox();
        ArrayList<PointBoxCurveType> result = new ArrayList<PointBoxCurveType>();
        for (UploadControlPanel panel : symbolCheckBoxes) {
            for (PointBoxCurveType type : panel.getSelectedFileType()) {
                result.add(type);
            }
        }//for
        return result;
    }
        
//    /**
//     * @deprecated 
//     * @return 
//     */
//    public int uploadPricingRuntimeToServer() {
//        setCheckBox();
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        int fileCount = 0;
//        for (UploadControlPanel panel : symbolCheckBoxes) {
//            if (panel.isSelected()) {
//                PointBoxQuoteCode symbol = panel.getSymbol();
//                for (PointBoxCurveType type : panel.getSelectedFileType()) {
//                    File file = new File(getPricingSettings().retrievePricingSettingFilePath(symbol, type));
//                    if (NIOGlobal.isValidFile(file)) {
//                        HttpPost post = new HttpPost(SERVER_URL);
//                        MultipartEntity multiPartEntity = new MultipartEntity () ;
//                        FileBody fileBody = new FileBody(file, "text/plain", "UTF-8");
//                        multiPartEntity.addPart("attachment", fileBody);
//                        post.setEntity(multiPartEntity);
//                        //Set file name as header
//                        post.addHeader("FILETYPE", file.getName());
//                        try {
//                            HttpResponse response = httpClient.execute(post);
//                            EntityUtils.consume(response.getEntity());
//                            
//                            if (response.getStatusLine().getStatusCode() != StatusCode.OK) {
//                                return FAILED;
//                            }
//                            fileCount++;
//                        } catch (IOException e) {
//                            return FAILED;
//                        }
//                    }
//                }
//            }
//        }
//        if (fileCount == 0) {
//            return NO_FILE_SELECTED;
//        }
//        return SUCCESSFUL;
//    }
    
    final IPointBoxPricingSettings getPricingSettings() {
        return face.getKernel().getPointBoxConsoleRuntime().getPointBoxPricingSettings();
    }
}
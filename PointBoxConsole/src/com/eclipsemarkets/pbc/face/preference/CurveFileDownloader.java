/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.release.PointBoxConfig;
import com.eclipsemarkets.runtime.PointBoxPricingCurveSettings;
import com.eclipsemarkets.web.pbc.PricingCurveFileSettings;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Zhijun Zhang, date & time: Jun 27, 2014 - 10:41:50 AM
 */
public class CurveFileDownloader extends SwingWorker<String, Void>{
    private boolean displayMessage;
    private List<PricingCurveFileSettings> aPricingCurveFileSettingsList;
    private String codeValue;
    private IPbcKernel kernel;

    public CurveFileDownloader(IPbcKernel kernel, String codeValue, List<PricingCurveFileSettings> aPricingCurveFileSettingsList, boolean displayMessage) {
        this.displayMessage = displayMessage;
        this.aPricingCurveFileSettingsList = aPricingCurveFileSettingsList;
        this.codeValue = codeValue;
        this.kernel = kernel;
    }

    @Override
    protected String doInBackground() throws Exception {
        String errMsg = null;
        String filePath;
        String tempFilePathExt = ".tmp";
        String url;
        ArrayList<String> aFilePathList = new ArrayList<String>();
        for (PricingCurveFileSettings aPricingCurveFileSettings : aPricingCurveFileSettingsList){
            filePath = kernel.getLocalCurveFileFullPath(PointBoxQuoteCode.convertEnumNameToType(codeValue), aPricingCurveFileSettings, true);

            aFilePathList.add(filePath);
            
            
            //back the file at "filePath"
            PointBoxPricingCurveSettings.backupPricingCurveFile(filePath);

            url = PointBoxConfig.generateCurveFileDownloadURL(kernel.getSelectedControllerIPwithPort(), 
                                                                     aPricingCurveFileSettings.getServerFileName());
            if (!NIOGlobal.downloadFileFromWeb(new URL(url), new File(filePath + tempFilePathExt), true)){
                PointBoxPricingCurveSettings.rollbackPricingCurveFile(filePath);
                errMsg = "Failed to download curve files.";
            }
        }//for
        if (errMsg == null){
            for (String aFilePath : aFilePathList){
                PointBoxPricingCurveSettings.copyPricingRuntimeFile(aFilePath + tempFilePathExt, aFilePath, true);
            }
        }
        return errMsg;
    }//doInBackground

    @Override
    protected void done() {
        try {
            String status = get();
            if (displayMessage){
                if (status == null){
                    JOptionPane.showMessageDialog(null, "Successfully download the file(s).");
                }else{
                    JOptionPane.showMessageDialog(null, "Failed to download the file(s). " + status);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CurveFileDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(CurveFileDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.preference;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.PbsysFileFilter;
import com.eclipsemarkets.pbc.runtime.settings.IPbconsoleAccessorySettings;
import com.eclipsemarkets.runtime.IPointBoxPricingSettings;
import com.eclipsemarkets.web.pbc.PbcPricingModel;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * FileSettingsPanel.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jul 14, 2010, 8:17:47 PM
 */
abstract class FileSettingsPanel extends javax.swing.JPanel implements IPreferenceComponentPanel
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger;
    static{
        logger = Logger.getLogger(FileSettingsPanel.class.getName());
    }

    IPbcFace face;
    
    private boolean settingsChanged = false;
    JFileChooser fileChooser;

    FileSettingsPanel(IPbcFace face) {
        this.face = face;
        fileChooser = new JFileChooser();
    }

//    void hanldeFTPBtnActionPerformed(final JTextField jNymexFtpText, final JButton jFTPBtn){
//        if (SwingUtilities.isEventDispatchThread()){
//            hanldeFTPBtnActionPerformedHelper(jNymexFtpText, jFTPBtn);
//        }else{
//            SwingUtilities.invokeLater(new Runnable(){
//                @Override
//                public void run() {
//                    hanldeFTPBtnActionPerformedHelper(jNymexFtpText, jFTPBtn);
//                }
//            });
//        }
//    }
//    private void hanldeFTPBtnActionPerformedHelper(final JTextField jNymexFtpText, final JButton jFTPBtn){
//        if(jNymexFtpText.isEditable()){
//            savingPath(jNymexFtpText,jNymexFtpText.getText().trim() , PointBoxLegacyFileType.nymexFtpAddress.toString());
//            jFTPBtn.setText("Edit");
//            jNymexFtpText.setEditable(false);
//        }else{
//            jFTPBtn.setText("Save");
//            jNymexFtpText.setEditable(true);            
//        }
//    }
    
    /**
     * 
     * @return - this method should guarantee non-NULL returned
     */
    abstract List<PointBoxQuoteCode> getStandardPricingSymbolListForDownloadPricingRuntime();
    
    /**
     * download all the curve files of every possible code from the server-side
     * @param displayCompleteMessage 
     */
    public abstract void downloadPricingRuntimeCurveFiles(boolean displayCompleteMessage);
    public abstract void downloadPricingRuntimeCurveFiles(PbcPricingModel aPbcPricingModel, boolean displayCompleteMessage);
    /**
     * upload all the curve files of every possible code from the server-side
     */
    public abstract void uploadPricingRuntimeCurveFiles();
    
    IPbconsoleAccessorySettings getAccessorySettings(){
        return face.getKernel().getPointBoxConsoleRuntime().getPbconsoleAccessorySettings();
    }

    public boolean isSettingsChanged() {
        synchronized(this){
            return settingsChanged;
        }
    }

    public void setSettingsChanged(boolean settingsChanged) {
        synchronized(this){
            this.settingsChanged = settingsChanged;
        }
    }
    
//    void savingPath(final JTextField jTextField,final String newPath,final String codeBasedFileName){
//        face.getKernel().getPointBoxConsoleRuntime().getPointBoxPricingSettings().getFileInfoRecord(codeBasedFileName).setFilePath(newPath);
////        face.getKernel().getPointBoxConsoleRuntime().getPointBoxPricingSettings().getLocalPropertiesInstance().setProperty(settingsFileType.toString(), newPath);
//        settingsChanged = true;
//        jTextField.setText(newPath);        
//    }

    abstract IPointBoxPricingSettings getPricingSettings();

    public static String getFileFromFileChooserWithLastDirectory(JFileChooser fileChooser, PbsysFileFilter filter, String lastSelectedPath){
        String result;
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        if (filter != null){
            fileChooser.setFileFilter(filter);
        }

        if (!lastSelectedPath.isEmpty()){
            File existingDirectoryOrFile = new File(lastSelectedPath);
            if (existingDirectoryOrFile.isDirectory()){
                fileChooser.setCurrentDirectory(existingDirectoryOrFile);
            }
            else if (existingDirectoryOrFile.isFile()){
                fileChooser.setSelectedFile(existingDirectoryOrFile);
            }
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            result = fileChooser.getSelectedFile().getAbsolutePath();
        }
        else{
            return "";
        }

        if (filter != null && !result.isEmpty() && !result.substring(result.lastIndexOf("."),result.length()).equalsIgnoreCase(filter.getDescription())){
            return result + filter.getDescription();
        }

        return result;
    }

}

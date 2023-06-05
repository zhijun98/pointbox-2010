/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.pricer;

import com.eclipsemarkets.data.PointBoxQuoteCode;
import com.eclipsemarkets.debug.PointBoxTracer;
import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import com.eclipsemarkets.pricer.IPricingRuntimeManager;
import com.eclipsemarkets.data.PointBoxCurveType;
import com.eclipsemarkets.runtime.IFileInfoRecord;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * PricingRuntimeManager.java
 * <p>
 * Manage the pricing settings files for pricer. It is thread-safe
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 22, 2010, 7:06:31 AM
 */
class PricingRuntimeManager implements IPricingRuntimeManager{

    private static final Logger logger;
    //private static PricingRuntimeManager self;
    static{
        //self = null;
        logger = Logger.getLogger(PricingRuntimeManager.class.getName());
    }
    
    private final HashMap<String, Long> pricingSettingFilesTimestamp;
    private final HashMap<String, Long> pricingCurveFilesTimestamp;
//    private File filesFolder;

    private IPbcKernel kernel;

    PricingRuntimeManager(IPbcKernel kernel, File filesFolder) throws IOException{
        /* TODO: The pricingSettings will always be NaturalGas
         * because it is set as the default before user gets to choose a commodity in welcome panel.
         * Need to change this so it updates with SettingsUpdate.
         */
        this.kernel = kernel;
//        setPricingRuntimeManager(filesFolder);
        pricingSettingFilesTimestamp = new HashMap<String, Long>();
        pricingCurveFilesTimestamp = new HashMap<String, Long>();
    }

    public IPbcKernel getKernel() {
        return kernel;
    }

    private synchronized boolean hasUpdatedFile(File file, HashMap<String, Long> filesTimestamp) {
        if (!NIOGlobal.isValidFile(file)){  //does not exist
            return false;
        }
        if (filesTimestamp.containsKey(file.getName())) {
            boolean result = filesTimestamp.get(file.getName()).longValue() != file.lastModified();
            filesTimestamp.put(file.getName(), file.lastModified());
            return result;
        } else {
            filesTimestamp.put(file.getName(), file.lastModified());
            return true;
        }
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public synchronized boolean isPricingSettingsChanged(){
        if (kernel.getPointBoxConsoleRuntime() == null){
            PointBoxTracer.recordSevereException(logger, new Exception("PointBoxConsoleRuntime is NULL"));
            return false;
        }
        boolean result = false;
        List<PointBoxQuoteCode> aPointBoxQuoteCodeList = PointBoxQuoteCode.getPointBoxQuoteCodeList(false);
        ArrayList<IFileInfoRecord> records;
        for (PointBoxQuoteCode aPointBoxQuoteCode : aPointBoxQuoteCodeList){
            records = kernel.getPointBoxConsoleRuntime().getPointBoxPricingSettings()
                    .getPbconsoleFileInfoRecordList(aPointBoxQuoteCode);
            if ((records != null)&&(!records.isEmpty())){
                for (IFileInfoRecord record : records){
                    if (hasUpdatedFile(new File(record.getFilePath()), pricingSettingFilesTimestamp)){
                        result = true;
                        //break;
                    }
                }
            }
//            if (result){
//                break;
//            }
        }//for
        return result;
    }

    /**
     * 
     * This is used for auto-uploading price curves
     * @return 
     */
    private synchronized ArrayList<ArrayList<Object>> checkPricingCurveFileChanged(){
        final ArrayList<ArrayList<Object>> filePathObjects = new ArrayList<ArrayList<Object>>();
        if (kernel.getPointBoxConsoleRuntime() == null){
            PointBoxTracer.recordSevereException(logger, new Exception("PointBoxConsoleRuntime is NULL"));
            return filePathObjects;
        }
        final PointBoxQuoteCode[] aPointBoxQuoteCodeList = PointBoxQuoteCode.values();
        if ((aPointBoxQuoteCodeList == null) || (aPointBoxQuoteCodeList.length == 0)){
            return filePathObjects;
        }
        //validate file paths....
        List<PointBoxCurveType> types = PointBoxCurveType.getStandardPricingSettingsTypes(false);
        ArrayList<Object> filePathObject;
        String statusMsg = null;
        String filePath;
        for (PointBoxQuoteCode aPointBoxQuoteCode : aPointBoxQuoteCodeList){
            for (PointBoxCurveType type : types){
                filePath = kernel.getLocalCurveFileFullPath(aPointBoxQuoteCode, type, true);
                filePathObject = new ArrayList<Object>();
                filePathObject.add(aPointBoxQuoteCode);
                filePathObject.add(type);
                filePathObject.add(filePath);
                if (NIOGlobal.isValidFile(filePath)){
                    if (hasUpdatedFile(new File(filePath), pricingCurveFilesTimestamp)){
                        filePathObjects.add(filePathObject);
                    }
                }else{
                    filePathObject.remove(aPointBoxQuoteCode);
                    statusMsg = "Cannot find a valid " + type + " file for " + aPointBoxQuoteCode;
                    break;
                }
            }//for
            if (statusMsg != null){
                filePathObjects.clear();
                break;
            }
        }//for
        return filePathObjects;
    }
}

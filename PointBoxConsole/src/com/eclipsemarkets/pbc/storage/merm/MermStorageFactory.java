/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.storage.merm;

import com.eclipsemarkets.global.NIOGlobal;
import com.eclipsemarkets.pbc.kernel.IPbcKernel;

/**
 *
 * @author Zhijun.Zhang
 */
public class MermStorageFactory {

    private static IMermQuoteRecorder mermQuoteRecorder;
    static{
        mermQuoteRecorder = null;
    }

    public static IMermQuoteRecorder createMermQuoteRecorderSingleton(IPbcKernel kernel, String mdbFullPath){
        if (mermQuoteRecorder == null){
            if ((mdbFullPath == null) || (!NIOGlobal.isValidFile(mdbFullPath)) || (!mdbFullPath.endsWith(".mdb"))){
                return null;
            }
            mermQuoteRecorder = new MermQuoteRecorder(kernel, mdbFullPath);
        }
        return mermQuoteRecorder;
    }

    public static IMermQuoteRetriever createMermQuoteRetrieverInstance(String mdbFullPath){
        if ((mdbFullPath == null) || (!NIOGlobal.isValidFile(mdbFullPath)) || (!mdbFullPath.endsWith(".mdb"))){
            return null;
        }
        return new MermQuoteRetriever(mdbFullPath);
    }

    private MermStorageFactory() {
    }
}

/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer.model;

import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;
import com.eclipsemarkets.pbc.runtime.IPbcRuntime;

/**
 * ViewerModelFactory.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 1:27:34 PM
 */
public class ViewerModelFactory {

    public static IViewerDataModel createViewerDataModelInstance(IViewerDataModelKernel dataModelKernel,
                                                                 IViewerColumnModel columnModel)
    {
        return new ViewerDataModel(dataModelKernel, columnModel);
    }
    
    public static IViewerColumnModel createViewerColumnModelInstance(IPbcRuntime runtime, String viewerUniqueTabName)
    {
        return new ViewerColumnModel(runtime, viewerUniqueTabName);
    }

    public static IViewerDataModelKernel createViewerDataModelKernelInstance(IPbcViewer viewer, int rowThreshold){
        return new ViewerDataModelKernel(viewer, rowThreshold);
    }

    private ViewerModelFactory() {
    }
}

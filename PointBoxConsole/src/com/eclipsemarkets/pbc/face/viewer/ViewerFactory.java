/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.viewer;

import com.eclipsemarkets.pbc.face.IPbcFace;
import com.eclipsemarkets.pbc.face.viewer.IPbcViewer;

/**
 * ViewerFactory.java
 * <p>
 * @author Zhijun Zhang
 * Created on May 21, 2010, 6:33:04 AM
 */
public class ViewerFactory {

    private static IPbcViewer pointBoxViewer;
    static{
        pointBoxViewer = null;
    }

    public static IPbcViewer getPointBoxViewerSingleton(IPbcFace face){
        if (pointBoxViewer == null){
            pointBoxViewer = new PointBoxViewer(face);
        }
        return pointBoxViewer;
    }

    private ViewerFactory() {
    }
}

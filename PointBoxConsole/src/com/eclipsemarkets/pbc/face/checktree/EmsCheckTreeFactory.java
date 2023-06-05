/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import com.eclipsemarkets.pbc.kernel.IPbcKernel;
import javax.swing.Icon;

/**
 * EmsCheckTreeFactory.java
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Jun 25, 2010, 11:35:24 AM
 */
public class EmsCheckTreeFactory {

    public static IEmsCheckTreePanel createEmsCheckTreePanelComponentInstance(String rootName, boolean sorting,
                                                                              IPbcKernel kernel) {
        if (sorting){
            return new EmsSortedCheckTreePanel(rootName, kernel);
        }else{
            return new EmsCheckTreePanel(rootName, kernel);
        }
    }

    public static IEmsCheckTreePanel createEmsCheckTreePanelComponentInstance(String rootName, Icon rootIcon, boolean sorting,
                                                                              IPbcKernel kernel) {
        if (sorting){
            return new EmsSortedCheckTreePanel(rootName, rootIcon, kernel);
        }else{
            return new EmsCheckTreePanel(rootName, rootIcon, kernel);
        }
    }

    private EmsCheckTreeFactory() {
    }

}

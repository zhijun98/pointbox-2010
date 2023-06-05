/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc.face.checktree;

import javax.swing.Icon;

/**
 * RootCheckNode
 * <p>
 * <p>
 * @author Zhijun Zhang
 * Created on Oct 15, 2010, 11:11:22 AM
 */
class RootCheckNode extends EmsCheckNode{

    RootCheckNode(String rootName) {
        super(rootName);
    }

    RootCheckNode(String rootName, Icon rootIcon) {
        super(rootName, true, rootIcon);
    }

}

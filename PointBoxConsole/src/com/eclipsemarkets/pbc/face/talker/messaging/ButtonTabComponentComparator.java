/**
 * Eclipse Market Solutions LLC
 */
package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.pbc.face.talker.IButtonTabComponent;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * ButtonTabComponentComparator
 * <P>
 * {Insert class description here}
 * <P>
 * @author Zhijun Zhang
 * Created on Apr 26, 2011 at 10:24:35 AM
 */
class ButtonTabComponentComparator implements Comparator<IButtonTabComponent> {
    
    private boolean fromAtoZ;

    public ButtonTabComponentComparator(boolean fromAtoZ) {
        this.fromAtoZ = fromAtoZ;
    }
    
    private static final Logger logger = Logger.getLogger(ButtonTabComponentComparator.class.getName());

    public int compare(IButtonTabComponent o1, IButtonTabComponent o2) {
        String s1 = o1.getTabUniqueID().toLowerCase();
        String s2 = o2.getTabUniqueID().toLowerCase();
        if (fromAtoZ){
            return s1.compareTo(s2);
        }else{
            return ((-1)*s1.compareTo(s2));
        }
    }

}//ButtonTabComponentComparator

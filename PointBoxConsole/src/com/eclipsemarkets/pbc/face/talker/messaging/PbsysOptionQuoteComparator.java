/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eclipsemarkets.pbc.face.talker.messaging;

import com.eclipsemarkets.gateway.data.IPbsysOptionQuote;
import java.util.Comparator;

/**
 *
 * @author Zhijun Zhang, date & time: Jan 16, 2014 - 9:17:56 PM
 */
class PbsysOptionQuoteComparator implements Comparator<IPbsysOptionQuote>{

    @Override
    public int compare(IPbsysOptionQuote o1, IPbsysOptionQuote o2) {
        if ((o1 == null) || (o2 == null)){
            return 0;
        }
        if (o1.getInstantMessage().getMessageTimestamp().before(o2.getInstantMessage().getMessageTimestamp())){
            return -1;
        }else if (o1.getInstantMessage().getMessageTimestamp().after(o2.getInstantMessage().getMessageTimestamp())){
            return 1;
        }else{
            return 0;
        }
    }

}

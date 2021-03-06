/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: DoubleValues.java 604 2006-03-07 09:52:50Z jalateras $
 */


package org.openvpms.component.system.service.jxpath;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate: 2006-03-07 09:52:50 +0000 (Tue, 07 Mar 2006) $
 */
public class DoubleValues {
    private double low;
    private double high;
    
    /**
     * @param high
     * @param low
     */
    public DoubleValues(double high, double low) {
        // TODO Auto-generated constructor stub
        this.high = high;
        this.low = low;
    }

    /**
     * @return Returns the high.
     */
    public double getHigh() {
        return high;
    }

    /**
     * @param high The high to set.
     */
    public void setHigh(double high) {
        this.high = high;
    }

    /**
     * @return Returns the low.
     */
    public double getLow() {
        return low;
    }

    /**
     * @param low The low to set.
     */
    public void setLow(double low) {
        this.low = low;
    }
}

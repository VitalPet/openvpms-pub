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
 *  $Id: BigDecimalOperationMod.java 671 2006-03-22 20:34:01Z jalateras $
 */


package org.openvpms.component.system.common.jxpath;

import java.math.BigDecimal;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.CoreOperationMod;
import org.apache.commons.jxpath.ri.compiler.Expression;

/**
 * BigDecimal support
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate: 2006-03-22 20:34:01 +0000 (Wed, 22 Mar 2006) $
 */
public class BigDecimalOperationMod extends CoreOperationMod {

    /**
     * Support for base class construction
     * 
     * @param arg1
     * @param arg2
     */
    public BigDecimalOperationMod(Expression arg1, Expression arg2) {
        super(arg1, arg2);

    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.CoreOperationMod#computeValue(org.apache.commons.jxpath.ri.EvalContext)
     */
    @Override
    public Object computeValue(EvalContext context) {
        BigDecimal l = TypeConversionUtil.bigDecimalValue(args[0].computeValue(context));
        BigDecimal r = TypeConversionUtil.bigDecimalValue(args[1].computeValue(context));

        return l.divideAndRemainder(r)[1];
    }

}

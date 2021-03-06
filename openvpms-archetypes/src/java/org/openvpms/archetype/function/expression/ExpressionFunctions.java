/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.expression;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.system.common.jxpath.AbstractObjectFunctions;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

/**
 * Functions to support conditional evaluation in JXPath expressions.
 * <p/>
 * This extends {@link ObjectFunctions} in order to translate the "if" function name the corresponding {@link #when}
 * implementation.
 *
 * @author Tim Anderson
 */
public class ExpressionFunctions extends AbstractObjectFunctions {

    /**
     * Constructs an {@link ExpressionFunctions}.
     *
     * @param namespace the function namespace
     */
    public ExpressionFunctions(String namespace) {
        super(namespace);
        setObject(this);
    }

    /**
     * Returns a Function, if any, for the specified namespace,
     * name and parameter types.
     *
     * @param namespace if it is not the namespace specified in the constructor, the method returns null
     * @param name      is a function name.
     * @return a MethodFunction, or {@code null} if there is no such function.
     */
    @Override
    public Function getFunction(String namespace, String name, Object[] parameters) {
        if ("if".equals(name)) {
            name = "when";
        }
        return super.getFunction(namespace, name, parameters);
    }

    /**
     * Returns the {@code thenValue} if {@code condition} is {@code true}.
     *
     * @param condition the condition
     * @param thenValue the value to return if the condition is true
     * @return {@code thenValue} if {@code condition} is {@code true}; otherwise {@code null}
     */
    public Object when(boolean condition, Object thenValue) {
        return when(condition, thenValue, null);
    }

    /**
     * Returns the {@code trueValue} if {@code condition} is {@code true}, else returns {@code falseValue}.
     *
     * @param condition  the condition
     * @param trueValue  the value to return if the condition is true
     * @param falseValue the value to return if the condition is false
     * @return {@code trueValue} if {@code condition} is {@code true}; otherwise {@code falseValue}
     */
    public Object when(boolean condition, Object trueValue, Object falseValue) {
        return condition ? trueValue : falseValue;
    }

    /**
     * Evaluates a variable, if it exists.
     *
     * @param context the expression context
     * @param name    the variable name
     * @return the value of the variable, or {@code null} if it doesn't exist
     */
    public Object var(ExpressionContext context, String name) {
        return var(context, name, null);
    }

    /**
     * Evaluates a variable, if it exists.
     *
     * @param context the expression context
     * @param name    the variable name
     * @return the value of the variable, or {@code elseValue} if it doesn't exist
     */
    public Object var(ExpressionContext context, String name, Object elseValue) {
        Variables variables = context.getJXPathContext().getVariables();
        if (variables.isDeclaredVariable(name)) {
            return variables.getVariable(name);
        }
        return elseValue;
    }

    /**
     * Concatenates a list of values if all of them are non-null/empty.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return the concatenated values, or an empty string if a value is null or empty
     */
    public String concatIf(String value1, String value2) {
        return concatIf(new String[]{value1, value2});
    }

    /**
     * Concatenates a list of values if all of them are non-null/empty.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @return the concatenated values, or an empty string if a value is null or empty
     */
    public String concatIf(String value1, String value2, String value3) {
        return concatIf(new String[]{value1, value2, value3});
    }

    /**
     * Concatenates a list of values if all of them are non-null/empty.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     * @return the concatenated values, or an empty string if a value is null or empty
     */
    public String concatIf(String value1, String value2, String value3, String value4) {
        return concatIf(new String[]{value1, value2, value3, value4});
    }

    /**
     * Concatenates a list of values if all of them are non-null/empty.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     * @param value5 the fifth value
     * @return the concatenated values, or an empty string if a value is null or empty
     */
    public String concatIf(String value1, String value2, String value3, String value4, String value5) {
        return concatIf(new String[]{value1, value2, value3, value4, value5});
    }

    /**
     * Returns the first argument if not null or empty, otherwise returns the second.
     *
     * @param value     the value to check
     * @param elseValue the value to return if {@code value} is null or empty
     * @return {@code value} or {@code elseValue} if {@code value} is {@code null} or empty
     */
    public Object ifempty(String value, String elseValue) {
        return !StringUtils.isEmpty(value) ? value : elseValue;
    }

    /**
     * Truncates a string if it exceeds a maximum length.
     *
     * @param value     the string
     * @param maxLength the maximum length
     * @return the string, truncated to maxLength characters if it exceeds maxLength, otherwise the string unchanged
     */
    public String trim(String value, int maxLength) {
        return value != null && value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    /**
     * Replaces a value with another if it exceeds a maximum length.
     *
     * @param value     the value
     * @param maxLength the maximum length
     * @param elseValue the value to return if {@code value} is too long
     * @return {@code value} or {@code elseValue} if {@code value} is too long
     */
    public String replaceIf(String value, int maxLength, String elseValue) {
        return (value != null && value.length() > maxLength) ? elseValue : value;
    }

    /**
     * Concatenates a list of values if all of them are non-null/empty.
     *
     * @param values the values to concatenate
     * @return the concatenated values, or an empty string if a value is null or empty
     */
    private String concatIf(String[] values) {
        StringBuilder result = new StringBuilder();

        for (String value : values) {
            if (StringUtils.isEmpty(value)) {
                return "";
            }
            result.append(value);
        }
        return result.toString();
    }

}

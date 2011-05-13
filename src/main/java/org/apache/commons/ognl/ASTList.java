/*
 * $Id$
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.ognl;

import org.apache.commons.ognl.enhance.ExpressionCompiler;
import org.apache.commons.ognl.enhance.UnsupportedCompilationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ASTList extends SimpleNode implements NodeType
{
    public ASTList(int id)
    {
        super(id);
    }

    public ASTList(OgnlParser p, int id)
    {
        super(p, id);
    }

    protected Object getValueBody(OgnlContext context, Object source)
            throws OgnlException
    {
        List answer = new ArrayList(jjtGetNumChildren());
        for(int i = 0; i < jjtGetNumChildren(); ++i)
            answer.add(_children[i].getValue(context, source));
        return answer;
    }

    public Class getGetterClass()
    {
        return null;
    }

    public Class getSetterClass()
    {
        return null;
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder("{ ");

        for(int i = 0; i < jjtGetNumChildren(); ++i) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(_children[i].toString());
        }
        return result.append(" }").toString();
    }

    public String toGetSourceString(OgnlContext context, Object target)
    {
        String result = "";
        boolean array = false;

        if (_parent != null && ASTCtor.class.isInstance(_parent)
            && ((ASTCtor)_parent).isArray()) {

            array = true;
        }

        context.setCurrentType(List.class);
        context.setCurrentAccessor(List.class);

        if (!array)
        {
            if (jjtGetNumChildren() < 1)
                return "java.util.Arrays.asList( new Object[0])";

            result += "java.util.Arrays.asList( new Object[] ";
        }

        result += "{ ";

        try {

            for(int i = 0; i < jjtGetNumChildren(); ++i) {
                if (i > 0) {
                    result = result + ", ";
                }

                Class prevType = context.getCurrentType();

                Object objValue = _children[i].getValue(context, context.getRoot());
                String value = _children[i].toGetSourceString(context, target);

                // to undo type setting of constants when used as method parameters
                if (ASTConst.class.isInstance(_children[i])) {

                    context.setCurrentType(prevType);
                }

                value = ExpressionCompiler.getRootExpression(_children[i], target, context) + value;

                String cast = "";
                if (ExpressionCompiler.shouldCast(_children[i])) {

                    cast = (String)context.remove(ExpressionCompiler.PRE_CAST);
                }
                if (cast == null)
                    cast = "";

                if (!ASTConst.class.isInstance(_children[i]))
                    value = cast + value;

                Class ctorClass = (Class)context.get("_ctorClass");
                if (array && ctorClass != null && !ctorClass.isPrimitive()) {

                    Class valueClass = value != null ? value.getClass() : null;
                    if (NodeType.class.isAssignableFrom(_children[i].getClass()))
                        valueClass = ((NodeType)_children[i]).getGetterClass();

                    if (valueClass != null && ctorClass.isArray()) {

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                               "(" + ExpressionCompiler.getCastString(ctorClass)
                                                                               + ")org.apache.commons.ognl.OgnlOps.toArray(" + value + ", " + ctorClass.getComponentType().getName()
                                                                               + ".class, true)",
                                                                               ctorClass
                        );

                    } else  if (ctorClass.isPrimitive()) {

                        Class wrapClass = OgnlRuntime.getPrimitiveWrapperClass(ctorClass);

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                               "((" + wrapClass.getName()
                                                                               + ")org.apache.commons.ognl.OgnlOps.convertValue(" + value + ","
                                                                               + wrapClass.getName() + ".class, true))."
                                                                               + OgnlRuntime.getNumericValueGetter(wrapClass),
                                                                               ctorClass
                        );

                    } else if (ctorClass != Object.class) {

                        value = OgnlRuntime.getCompiler().createLocalReference(context,
                                                                               "(" + ctorClass.getName() + ")org.apache.commons.ognl.OgnlOps.convertValue(" + value + "," + ctorClass.getName() + ".class)",
                                                                               ctorClass
                        );

                    } else if ((NodeType.class.isInstance(_children[i])
                                && ((NodeType)_children[i]).getGetterClass() != null
                                && Number.class.isAssignableFrom(((NodeType)_children[i]).getGetterClass()))
                               || valueClass.isPrimitive()) {

                        value = " ($w) (" + value + ")";
                    } else if (valueClass.isPrimitive()) {
                        value = "($w) (" + value + ")";
                    }

                } else if (ctorClass == null || !ctorClass.isPrimitive()) {

                    value = " ($w) (" + value + ")";
                }

                if (objValue == null || value.length() <= 0)
                    value = "null";

                result += value;
            }

        } catch (Throwable t)
        {
            throw OgnlOps.castToRuntime(t);
        }

        context.setCurrentType(List.class);
        context.setCurrentAccessor(List.class);

        result += "}";

        if (!array)
            result += ")";

        return result;
    }

    public String toSetSourceString(OgnlContext context, Object target)
    {
        throw new UnsupportedCompilationException("Can't generate setter for ASTList.");
    }
}
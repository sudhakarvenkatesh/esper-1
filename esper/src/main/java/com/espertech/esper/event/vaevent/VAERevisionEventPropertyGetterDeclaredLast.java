/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.event.vaevent;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.codegen.core.CodegenContext;
import com.espertech.esper.codegen.model.expression.CodegenExpression;
import com.espertech.esper.event.EventPropertyGetterSPI;

import static com.espertech.esper.codegen.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.event.vaevent.VAERevisionEventPropertyGetterDeclaredGetVersioned.revisionImplementationNotProvided;

public class VAERevisionEventPropertyGetterDeclaredLast implements EventPropertyGetterSPI {
    private final EventPropertyGetterSPI fullGetter;

    public VAERevisionEventPropertyGetterDeclaredLast(EventPropertyGetterSPI fullGetter) {
        this.fullGetter = fullGetter;
    }

    public Object get(EventBean eventBean) throws PropertyAccessException {
        RevisionEventBeanDeclared riv = (RevisionEventBeanDeclared) eventBean;
        EventBean bean = riv.getLastBaseEvent();
        if (bean == null) {
            return null;
        }
        return fullGetter.get(bean);
    }

    private String getCodegen(CodegenContext context) {
        return context.addMethod(Object.class, EventBean.class, "eventBean", this.getClass())
                .declareVar(RevisionEventBeanDeclared.class, "riv", cast(RevisionEventBeanDeclared.class, ref("eventBean")))
                .declareVar(EventBean.class, "bean", exprDotMethod(ref("riv"), "getLastBaseEvent"))
                .ifRefNullReturnNull("bean")
                .methodReturn(fullGetter.codegenEventBeanGet(ref("bean"), context));
    }

    public boolean isExistsProperty(EventBean eventBean) {
        return true;
    }

    public Object getFragment(EventBean eventBean) {
        return null; // fragments no provided by revision events
    }

    public CodegenExpression codegenEventBeanGet(CodegenExpression beanExpression, CodegenContext context) {
        return localMethod(getCodegen(context), beanExpression);
    }

    public CodegenExpression codegenEventBeanExists(CodegenExpression beanExpression, CodegenContext context) {
        return constantTrue();
    }

    public CodegenExpression codegenEventBeanFragment(CodegenExpression beanExpression, CodegenContext context) {
        return constantNull();
    }

    public CodegenExpression codegenUnderlyingGet(CodegenExpression underlyingExpression, CodegenContext context) {
        throw revisionImplementationNotProvided();
    }

    public CodegenExpression codegenUnderlyingExists(CodegenExpression underlyingExpression, CodegenContext context) {
        throw revisionImplementationNotProvided();
    }

    public CodegenExpression codegenUnderlyingFragment(CodegenExpression underlyingExpression, CodegenContext context) {
        throw revisionImplementationNotProvided();
    }
}

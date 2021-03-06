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
package com.espertech.esper.codegen.model.statement;

import com.espertech.esper.codegen.core.CodegenBlock;
import com.espertech.esper.codegen.model.expression.CodegenExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodegenStatementIf extends CodegenStatementWBlockBase {

    private List<CodegenStatementIfConditionBlock> blocks = new ArrayList<>(2);
    private CodegenBlock optionalElse;

    public CodegenStatementIf(CodegenBlock parent) {
        super(parent);
    }

    public CodegenBlock getOptionalElse() {
        return optionalElse;
    }

    public void setOptionalElse(CodegenBlock optionalElse) {
        this.optionalElse = optionalElse;
    }

    public void render(StringBuilder builder, Map<Class, String> imports) {
        String delimiter = "";
        for (CodegenStatementIfConditionBlock pair : blocks) {
            builder.append(delimiter);
            builder.append("if (");
            pair.getCondition().render(builder, imports);
            builder.append(") {\n");
            pair.getBlock().render(builder, imports);
            builder.append("}");
            delimiter = "\n";
        }
        if (optionalElse != null) {
            builder.append("else {\n");
            optionalElse.render(builder, imports);
            builder.append("}");
        }
    }

    public void mergeClasses(Set<Class> classes) {
        for (CodegenStatementIfConditionBlock pair : blocks) {
            pair.mergeClasses(classes);
        }
        if (optionalElse != null) {
            optionalElse.mergeClasses(classes);
        }
    }

    public void add(CodegenExpression condition, CodegenBlock block) {
        blocks.add(new CodegenStatementIfConditionBlock(condition, block));
    }
}

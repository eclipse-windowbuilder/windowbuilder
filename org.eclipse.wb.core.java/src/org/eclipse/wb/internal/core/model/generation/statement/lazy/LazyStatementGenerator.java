/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.generation.statement.lazy;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Implementation of {@link StatementGenerator} that works with {@link LazyVariableSupport}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class LazyStatementGenerator extends StatementGenerator {
  public static final LazyStatementGenerator INSTANCE = new LazyStatementGenerator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private LazyStatementGenerator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StatementGenerator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo child, StatementTarget target, Association association) throws Exception {
    // add method
    {
      VariableSupport variableSupport = child.getVariableSupport();
      Assert.isTrue(variableSupport instanceof LazyVariableSupport);
      variableSupport.add_getVariableStatementSource(target);
    }
    // add association
    association.add(child, target, null);
    // This type of association is used when child associated with parent at creation.
    // But we still need to call method to perform creation/association.
    if (association instanceof InvocationVoidAssociation) {
      LazyVariableSupport variableSupport = (LazyVariableSupport) child.getVariableSupport();
      String source = variableSupport.m_accessor.getName() + "();";
      Statement statement = child.getEditor().addStatement(source, target);
      Expression expression = DomGenerics.getExpression(statement);
      child.addRelatedNode(expression);
    }
  }
}

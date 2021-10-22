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
package org.eclipse.wb.internal.rcp.databinding.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ViewerInputParser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * JFace bindings API parser.
 *
 * @author lobas_av
 * @coverage bindings.rcp.parser
 */
public final class DatabindingParser extends AbstractParser {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void parse(DatabindingsProvider provider) throws Exception {
    new DatabindingParser(provider.getJavaInfoRoot(), provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DatabindingParser(JavaInfo root, DatabindingsProvider provider) throws Exception {
    super(provider.getAstEditor(), provider);
    // prepare root node
    TypeDeclaration rootNode = provider.getRootNode();
    // prepare parsers
    for (ObserveTypeContainer container : provider.getContainers()) {
      container.createObservables(root, this, m_editor, rootNode);
      m_subParsers.add(container);
    }
    //
    final DataBindingsRootInfo rootInfo = provider.getRootInfo();
    m_subParsers.add(rootInfo);
    m_subParsers.add(new ViewerInputParser(rootInfo.getContextInfo(), provider));
    //
    if (rootNode != null) {
      // find method initDataBindings()
      MethodDeclaration initDataBindings =
          AstNodeUtils.getMethodBySignature(rootNode, "initDataBindings()");
      if (initDataBindings != null) {
        rootInfo.setInitDataBindings(initDataBindings);
        // parse method initDataBindings()
        parseMethod(initDataBindings);
        initDataBindings.accept(new ASTVisitor() {
          @Override
          public void endVisit(TryStatement statement) {
            StringBuffer userTryCatchBlock = new StringBuffer();
            userTryCatchBlock.append("}");
            //
            List<CatchClause> catchClauses = CoreUtils.cast(statement.catchClauses());
            for (CatchClause catchClause : catchClauses) {
              userTryCatchBlock.append(" ");
              userTryCatchBlock.append(m_editor.getSource(catchClause));
            }
            //
            Block finallyBlock = statement.getFinally();
            if (finallyBlock != null) {
              userTryCatchBlock.append(" finally ");
              userTryCatchBlock.append(m_editor.getSource(finallyBlock));
            }
            //
            rootInfo.getContextInfo().setUserTryCatchBlock(userTryCatchBlock.toString());
          }
        });
        // after parsing post processing for all bindings
        for (AbstractBindingInfo binding : provider.getBindings0()) {
          binding.postParse();
        }
      }
    }
  }
}
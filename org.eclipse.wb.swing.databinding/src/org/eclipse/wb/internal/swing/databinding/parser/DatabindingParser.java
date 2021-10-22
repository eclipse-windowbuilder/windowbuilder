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
package org.eclipse.wb.internal.swing.databinding.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.VirtualParser;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * Swing beans bindings API parser.
 *
 * @author lobas_av
 * @coverage bindings.swing.parser
 */
public final class DatabindingParser extends AbstractParser {
  public static boolean useGenerics;

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
  @SuppressWarnings("unchecked")
  private DatabindingParser(JavaInfo root, DatabindingsProvider provider) throws Exception {
    super(root.getEditor(), provider);
    useGenerics = CoreUtils.useGenerics(m_editor.getJavaProject());
    // prepare root node
    TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(root);
    // prepare parsers
    for (ObserveTypeContainer container : provider.getContainers()) {
      container.createObservables(root, this, m_editor, rootNode);
      m_subParsers.add(container);
    }
    //
    DataBindingsRootInfo rootInfo = provider.getRootInfo();
    m_subParsers.add(rootInfo);
    if (rootNode != null) {
      // find method initDataBindings()
      MethodDeclaration initDataBindings =
          AstNodeUtils.getMethodBySignature(
              rootNode,
              DataBindingsRootInfo.INIT_DATA_BINDINGS_METHOD_NAME + "()");
      if (initDataBindings != null) {
        rootInfo.setInitDataBindings(initDataBindings);
        // parse virtual bindings
        Object[] virtualBindingsData =
            VirtualParser.getBindings(m_editor, provider, initDataBindings);
        // parse method initDataBindings()
        parseMethod(initDataBindings);
        //
        List<BindingInfo> bindings = rootInfo.getBindings();
        // after parsing post processing for all bindings
        for (int i = 0; i < bindings.size(); i++) {
          bindings.get(i).create(bindings);
        }
        // add virtual bindings
        List<Integer> indexes = (List<Integer>) virtualBindingsData[0];
        List<BindingInfo> virtualBindings = (List<BindingInfo>) virtualBindingsData[1];
        int virtualSize = virtualBindings.size();
        //
        for (int i = 0; i < virtualSize; i++) {
          int index = indexes.get(i);
          if (index < 0 || index >= bindings.size()) {
            index = 0;
          }
          bindings.add(index, virtualBindings.get(i));
        }
      }
    }
  }
}
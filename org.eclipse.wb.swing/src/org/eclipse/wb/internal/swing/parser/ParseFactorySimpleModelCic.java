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
package org.eclipse.wb.internal.swing.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.parser.IParseFactorySimpleModelCic;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * {@link IParseFactorySimpleModelCic} for Swing.
 * 
 * @author scheglov_ke
 * @coverage swing.parser
 */
public class ParseFactorySimpleModelCic implements IParseFactorySimpleModelCic {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory_simpleModel_CIC
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean accept(AstEditor editor, ClassInstanceCreation creation, ITypeBinding typeBinding)
      throws Exception {
    EditorState state = EditorState.get(editor);
    if (state.getEditorLoader() == null) {
      return false;
    }
    // Swing components
    String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
    if (typeName.startsWith("java.awt.") || typeName.startsWith("javax.swing.")) {
      return true;
    }
    // something different
    return false;
  }

  public JavaInfo create(AstEditor editor, ClassInstanceCreation creation, ITypeBinding typeBinding)
      throws Exception {
    // only Component
    boolean isSwingModel =
        AstNodeUtils.isSuccessorOf(typeBinding, "java.awt.Component")
            || AstNodeUtils.isSuccessorOf(typeBinding, "java.awt.LayoutManager")
            || AstNodeUtils.isSuccessorOf(typeBinding, "java.awt.GridBagConstraints")
            || AstNodeUtils.isSuccessorOf(typeBinding, "javax.swing.ButtonGroup")
            || AstNodeUtils.isSuccessorOf(typeBinding, "javax.swing.AbstractAction");
    if (!isSwingModel) {
      return null;
    }
    // standard Swing component
    String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
    return JavaInfoUtils.createJavaInfo(editor, typeName, new ConstructorCreationSupport(creation));
  }
}

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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.util.List;

/**
 * {@link VariableSupport} implementation for field, reused variable.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class FieldReuseVariableSupport extends FieldVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldReuseVariableSupport(JavaInfo javaInfo, Expression variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "field-reused: " + getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setName(String newName) throws Exception {
    splitUniqueField();
    m_javaInfo.getVariableSupport().setName(newName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    assertJavaInfoCreatedAt(target);
    if (isVisibleAtTarget(target)) {
      return getName();
    } else {
      splitUniqueField();
      return m_javaInfo.getVariableSupport().getReferenceExpression(target);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canConvertFieldToLocal() {
    return false;
  }

  @Override
  public void convertFieldToLocal() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean prefixThis() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setType(String newTypeName) throws Exception {
    splitUniqueField();
    ((FieldUniqueVariableSupport) m_javaInfo.getVariableSupport()).setType(newTypeName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Split
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates unique field for this component.
   */
  private void splitUniqueField() throws Exception {
    // add new field and use its name
    {
      AstEditor editor = m_javaInfo.getEditor();
      VariableDeclaration oldDeclaration = m_declaration;
      FieldDeclaration oldField = (FieldDeclaration) oldDeclaration.getParent();
      String oldFieldName = getName();
      String newFieldName = editor.getUniqueVariableName(-1, oldFieldName, null);
      // prepare modifiers
      String modifiers;
      {
        modifiers = "private ";
        if (AstNodeUtils.isStatic(oldField)) {
          modifiers += "static ";
        }
      }
      // check when field has assignment of our component
      if (oldDeclaration.getName() == m_variable) {
        // use temporary name for all references
        modifyName("__tmpField");
        // add new field with oldFieldName
        {
          String fieldSource =
              modifiers + editor.getSource(oldField.getType()) + " " + oldFieldName + ";";
          editor.addFieldDeclaration(fieldSource, new BodyDeclarationTarget(oldField, true));
        }
        // change references: this component -> newFieldName, other -> oldFieldName
        {
          // prepare list of references for this JavaInfo (not just on same variable)
          // NB: we need this list because during replacing references it will become temporary invalid
          List<Expression> componentReferences = getComponentReferences();
          // replace references
          for (Expression reference : getReferences()) {
            if (componentReferences.contains(reference)) {
              modifyVariableName(reference, newFieldName);
            } else {
              modifyVariableName(reference, oldFieldName);
            }
          }
        }
      } else {
        String fieldSource =
            modifiers + editor.getSource(oldField.getType()) + " " + newFieldName + ";";
        editor.addFieldDeclaration(fieldSource, new BodyDeclarationTarget(oldField, false));
        replaceComponentReferences(newFieldName);
      }
    }
    // use field variable support
    m_javaInfo.setVariableSupport(new FieldUniqueVariableSupport(m_javaInfo, m_variable));
  }
}

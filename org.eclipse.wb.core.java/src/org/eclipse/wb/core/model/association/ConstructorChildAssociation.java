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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.binding.DesignerMethodBinding;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Implementation of {@link Association} for {@link ClassInstanceCreation} when <em>child</em>
 * passed as argument.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class ConstructorChildAssociation extends ConstructorAssociation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ClassInstanceCreation getCreation() {
    return getConstructorCreationSupport().getCreation();
  }

  /**
   * @return the {@link ConstructorCreationSupport} for <em>parent</em> this {@link JavaInfo}.
   */
  private ConstructorCreationSupport getConstructorCreationSupport() {
    return (ConstructorCreationSupport) m_javaInfo.getParentJava().getCreationSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    // no need for check if we going to delete parent
    if (m_javaInfo.getParentJava().isDeleting()) {
      return true;
    }
    // prepare new binding - without this JavaInfo arguments
    DesignerMethodBinding newBinding;
    {
      ConstructorCreationSupport creationSupport = getConstructorCreationSupport();
      newBinding = m_editor.getBindingContext().get(creationSupport.getBinding());
      List<Expression> arguments = DomGenerics.arguments(getCreation());
      for (int i = arguments.size() - 1; i >= 0; i--) {
        Expression argument = arguments.get(i);
        if (m_javaInfo.isRepresentedBy(argument)) {
          newBinding.removeParameterType(i);
        }
      }
    }
    // we can delete association only if there is alternative constructor, without child
    return m_javaInfo.getParentJava().getDescription().getConstructor(newBinding) != null;
  }

  @Override
  public boolean remove() throws Exception {
    if (!m_javaInfo.getParentJava().isDeleting()) {
      // update ClassInstanceCreation
      ClassInstanceCreation creation;
      {
        ConstructorCreationSupport creationSupport = getConstructorCreationSupport();
        creation = creationSupport.getCreation();
        List<Expression> arguments = DomGenerics.arguments(creation);
        for (int i = arguments.size() - 1; i >= 0; i--) {
          Expression argument = arguments.get(i);
          if (m_javaInfo.isRepresentedBy(argument)) {
            m_javaInfo.getEditor().removeCreationArgument(creation, i);
          }
        }
      }
      // set new ConstructorCreationSupport for parent JavaInfo
      m_javaInfo.getParentJava().setCreationSupport(new ConstructorCreationSupport(creation));
    }
    // remove association
    return super.remove();
  }
}

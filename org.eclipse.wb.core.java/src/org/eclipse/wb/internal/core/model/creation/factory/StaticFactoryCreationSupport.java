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
package org.eclipse.wb.internal.core.model.creation.factory;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link CreationSupport} for creating objects using static methods.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class StaticFactoryCreationSupport extends AbstractExplicitFactoryCreationSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StaticFactoryCreationSupport(FactoryMethodDescription description) {
    super(description);
  }

  public StaticFactoryCreationSupport(FactoryMethodDescription description,
      MethodInvocation invocation) {
    super(description, invocation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "static factory: "
        + m_description.getDeclaringClass().getName()
        + " "
        + m_description.getSignature();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public CreationSupport getLiveComponentCreation() {
    FactoryMethodDescription factoryMethodDescription = getDescription();
    return new StaticFactoryCreationSupport(factoryMethodDescription);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String add_getSource_invocationExpression(NodeTarget target) throws Exception {
    if (isLocalFactoryMethod()) {
      return "";
    }
    return m_description.getDeclaringClass().getName() + ".";
  }

  private boolean isLocalFactoryMethod() {
    String editorTypeName =
        m_javaInfo.getEditor().getModelUnit().findPrimaryType().getFullyQualifiedName();
    String factoryTypeName = m_description.getDeclaringClass().getName();
    return editorTypeName.equals(factoryTypeName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IClipboardCreationSupport getClipboard() throws Exception {
    final String factoryClassName = m_description.getDeclaringClass().getName();
    final String methodSignature = m_description.getSignature();
    final String argumentsSource = getClipboardArguments();
    return new IClipboardCreationSupport() {
      private static final long serialVersionUID = 0L;

      @Override
      public CreationSupport create(JavaInfo rootObject) throws Exception {
        AstEditor editor = rootObject.getEditor();
        Class<?> factoryClass =
            EditorState.get(editor).getEditorLoader().loadClass(factoryClassName);
        FactoryMethodDescription description =
            FactoryDescriptionHelper.getDescription(editor, factoryClass, methodSignature, true);
        //
        StaticFactoryCreationSupport creationSupport =
            new StaticFactoryCreationSupport(description);
        creationSupport.m_addArguments = argumentsSource;
        return creationSupport;
      }
    };
  }
}

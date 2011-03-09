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
package org.eclipse.wb.internal.swing.model.bean;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectReferenceInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

/**
 * Model for {@link ButtonGroup}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ButtonGroupInfo extends JavaInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ButtonGroupInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the list of {@link ObjectReferenceInfo}'s on {@link ComponentInfo}'s with
   *         {@link AbstractButton} model, bounded to this {@link ButtonGroupInfo}.
   */
  public List<ObjectReferenceInfo> getButtons() throws Exception {
    getChildren().clear();
    List<ObjectReferenceInfo> buttons = Lists.newArrayList();
    for (MethodInvocation addInvocation : getMethodInvocations("add(javax.swing.AbstractButton)")) {
      Expression buttonReference = DomGenerics.arguments(addInvocation).get(0);
      ComponentInfo button = (ComponentInfo) getRootJava().getChildRepresentedBy(buttonReference);
      if (button != null) {
        ObjectReferenceInfo reference = new ObjectReferenceInfo(button);
        addChild(reference);
        buttons.add(reference);
      }
    }
    return buttons;
  }

  /**
   * @return <code>true</code> if given {@link AbstractButton} is contained in this
   *         {@link ButtonGroupInfo}.
   */
  public boolean hasButton(ComponentInfo button) {
    for (MethodInvocation addInvocation : getMethodInvocations("add(javax.swing.AbstractButton)")) {
      Expression buttonExpression = DomGenerics.arguments(addInvocation).get(0);
      if (button.isRepresentedBy(buttonExpression)) {
        return true;
      }
    }
    // no such button
    return false;
  }

  /**
   * Removes given {@link AbstractButton} from any {@link ButtonGroupInfo}.
   */
  public static void clearButton(ComponentInfo button) throws Exception {
    assertIsButton(button);
    for (ASTNode relatedNode : button.getRelatedNodes()) {
      if (relatedNode.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) relatedNode.getParent();
        if (AstNodeUtils.getMethodSignature(invocation).equals("add(javax.swing.AbstractButton)")) {
          button.getEditor().removeEnclosingStatement(invocation);
        }
      }
    }
  }

  /**
   * Adds new {@link AbstractButton} to this {@link ButtonGroupInfo}.
   */
  public void addButton(ComponentInfo button) throws Exception {
    assertIsButton(button);
    clearButton(button);
    // add to this ButtonGroupInfo
    {
      String addSource = TemplateUtils.format("{0}.add({1})", this, button);
      MethodInvocation addInvocation = (MethodInvocation) button.addExpressionStatement(addSource);
      addRelatedNode(addInvocation.getExpression());
    }
  }

  /**
   * Asserts that given {@link ComponentInfo} is {@link AbstractButton}.
   */
  private static void assertIsButton(ComponentInfo button) {
    Assert.isLegal(AbstractButton.class.isAssignableFrom(button.getDescription().getComponentClass()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public List<ObjectInfo> getChildrenTree() throws Exception {
      return new ArrayList<ObjectInfo>(getButtons());
    }
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }
}

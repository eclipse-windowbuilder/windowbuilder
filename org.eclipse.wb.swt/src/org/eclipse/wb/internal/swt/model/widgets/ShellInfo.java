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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Model for {@link Shell}.
 *
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public final class ShellInfo extends CompositeInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ShellInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setObject(Object object) throws Exception {
    super.setObject(object);
    // In RCP the Section performs setRedraw(false/true) for all parents, including Shell.
    // So, even through we don't open Shell, setRedraw(true) causes repaint for area under Shell.
    // So, we have to move Shell outside the view area ASAP.
    if (object != null) {
      ReflectionUtils.invokeMethod(object, "setLocation(int,int)", 10000, 10000);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IThisMethodParameterEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object evaluateParameter(EvaluationContext context,
      MethodDeclaration methodDeclaration,
      String methodSignature,
      SingleVariableDeclaration parameter,
      int index) throws Exception {
    if (index == 1) {
      return SWT.SHELL_TRIM;
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void fillContextMenu(IMenuManager manager) throws Exception {
    super.fillContextMenu(manager);
    contextMenu_removeSize(manager);
  }

  /**
   * Adds "Remove setSize()" item.
   */
  private void contextMenu_removeSize(IMenuManager manager) throws Exception {
    if (isRoot() || JavaInfoUtils.hasTrueParameter(this, "SWT.isRoot")) {
      ObjectInfoAction action = new ObjectInfoAction(this) {
        @Override
        protected void runEx() throws Exception {
          removeMethodInvocations("setSize(int,int)");
        }
      };
      action.setText(ModelMessages.ShellInfo_removeSetSize);
      manager.appendToGroup(IContextMenuConstants.GROUP_LAYOUT, action);
    }
  }
}
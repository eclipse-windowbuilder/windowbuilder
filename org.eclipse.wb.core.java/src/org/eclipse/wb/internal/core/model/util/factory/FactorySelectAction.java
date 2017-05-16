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
package org.eclipse.wb.internal.core.model.util.factory;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

import java.util.Map;

/**
 * {@link Action} for selecting factory type in other package (not in current).
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class FactorySelectAction extends Action {
  private final JavaInfo m_component;
  private final AstEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactorySelectAction(JavaInfo component) {
    m_component = component;
    m_editor = m_component.getEditor();
    setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/factory/open_factory.gif"));
    setText(ModelMessages.FactorySelectAction_text);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    try {
      Class<?> factoryType = selectFactoryType();
      if (factoryType == null) {
        return;
      }
      // prepare factory methods
      Map<String, FactoryMethodDescription> descriptionsMap =
          FactoryDescriptionHelper.getDescriptionsMap(m_editor, factoryType, true);
      // prepare factory method selection dialog
      ListDialog selectionDialog;
      {
        selectionDialog = new ListDialog(DesignerPlugin.getShell());
        selectionDialog.setTitle(ModelMessages.FactorySelectAction_dialogTitle);
        selectionDialog.setMessage(ModelMessages.FactorySelectAction_dialogMessage);
        selectionDialog.setContentProvider(new ArrayContentProvider());
        selectionDialog.setLabelProvider(new LabelProvider() {
          @Override
          public String getText(Object element) {
            return (String) element;
          }
        });
        selectionDialog.setInput(descriptionsMap.keySet());
        if (!descriptionsMap.isEmpty()) {
          selectionDialog.setInitialSelections(new Object[]{descriptionsMap.keySet().iterator().next()});
        }
      }
      // open dialog
      if (selectionDialog.open() == Window.OK && selectionDialog.getResult().length != 0) {
        FactoryActionsSupport.addPreviousTypeName(m_component, factoryType.getName());
        // do apply
        String signature = (String) selectionDialog.getResult()[0];
        FactoryMethodDescription description = descriptionsMap.get(signature);
        new FactoryApplyAction(m_component, description).run();
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Asks user about factory type.
   *
   * @return the loaded factory type, or <code>null</code> is selection was cancelled.
   */
  private Class<?> selectFactoryType() throws Exception {
    Class<?> factoryType;
    // prepare dialog
    SelectionDialog dialog;
    {
      Shell shell = DesignerPlugin.getShell();
      ProgressMonitorDialog context = new ProgressMonitorDialog(shell);
      IJavaSearchScope scope =
          SearchEngine.createJavaSearchScope(new IJavaElement[]{m_editor.getJavaProject()});
      dialog =
          JavaUI.createTypeDialog(
              shell,
              context,
              scope,
              IJavaElementSearchConstants.CONSIDER_CLASSES,
              false);
      dialog.setTitle(ModelMessages.FactorySelectAction_chooseTitle);
      dialog.setMessage(ModelMessages.FactorySelectAction_chooseMessage);
    }
    // open dialog
    if (dialog.open() != Window.OK) {
      return null;
    }
    // prepare type
    IType type = (IType) dialog.getResult()[0];
    String factoryTypeName = type.getFullyQualifiedName().replace('$', '.');
    factoryType = EditorState.get(m_editor).getEditorLoader().loadClass(factoryTypeName);
    return factoryType;
  }
}

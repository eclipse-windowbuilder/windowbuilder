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
package org.eclipse.wb.internal.core.databinding.ui.editor;

import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog field based {@link IUiContentProvider}. Subclasses need implement method
 * {@link #getDialogField()} for access to {@link DialogField}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class DialogFieldUiContentProvider implements IUiContentProvider {
  private Shell m_shell;
  private ICompleteListener m_listener;
  private String m_errorMessage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setCompleteListener(ICompleteListener listener) {
    m_listener = listener;
  }

  public final String getErrorMessage() {
    return m_errorMessage;
  }

  protected final ICompleteListener getListener() {
    return m_listener;
  }

  /**
   * Sets or clears the error message for this provider.
   */
  protected final void setErrorMessage(String message) {
    m_errorMessage = message;
    if (m_listener != null) {
      m_listener.calculateFinish();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public final int getNumberOfControls() {
    return getDialogField().getNumberOfControls();
  }

  public void createContent(Composite parent, int columns) {
    getDialogField().doFillIntoGrid(parent, columns);
    m_shell = parent.getShell();
  }

  /**
   * Helper method for access to {@link Shell}.
   */
  protected final Shell getShell() {
    return m_shell;
  }

  /**
   * @return {@link DialogField} that represented GUI object for this provider.
   */
  public abstract DialogField getDialogField();

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method for load classes over editor class loader.
   */
  protected Class<?> loadClass(String className) throws ClassNotFoundException {
    return CoreUtils.load(JavaInfoUtils.getClassLoader(EditorState.getActiveJavaInfo()), className);
  }

  /**
   * Helper method for access to current (enclosing project for editing compilation unit)
   * {@link IJavaProject}.
   */
  protected IJavaProject getJavaProject() {
    AstEditor editor = EditorState.getActiveJavaInfo().getEditor();
    return editor.getJavaProject();
  }
}
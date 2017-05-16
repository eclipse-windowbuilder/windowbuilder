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
package org.eclipse.wb.internal.core.utils.jdt.ui;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

/**
 * {@link FieldEditor} for {@link IJavaProject} selection.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt.ui
 */
public final class ProjectSelectionDialogField extends StringButtonDialogField
    implements
      IDialogFieldListener {
  private IJavaProject m_project;
  private IDialogFieldListener m_updateListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ProjectSelectionDialogField}.
   */
  public static ProjectSelectionDialogField create() {
    ButtonAdapter adapter = new ButtonAdapter();
    ProjectSelectionDialogField field = new ProjectSelectionDialogField(adapter);
    adapter.setReceiver(field);
    return field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ProjectSelectionDialogField(IStringButtonAdapter adapter) {
    super(adapter);
    setDialogFieldListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link IJavaProject}.
   */
  public IJavaProject getProject() {
    return m_project;
  }

  /**
   * Sets the selected {@link IJavaProject}.
   */
  public void setProject(IJavaProject project) {
    m_project = project;
    if (m_project != null) {
      String newText = getProjectString(m_project);
      if (!getText().equals(newText)) {
        setText(newText);
      }
    }
    // notify listener
    if (m_updateListener != null) {
      m_updateListener.dialogFieldChanged(this);
    }
  }

  /**
   * Sets the {@link IDialogFieldListener} to listen for {@link IJavaProject} updates in this field.
   */
  public void setUpdateListener(IDialogFieldListener updateListener) {
    m_updateListener = updateListener;
    setDialogFieldListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDialogFieldListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dialogFieldChanged(DialogField field) {
    setProject(getProjectFromString(getText()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return string presentation of {@link IJavaProject}..
   */
  private static String getProjectString(IJavaProject project) {
    return project == null ? "" : project.getPath().makeRelative().toString();
  }

  /**
   * @return the {@link IJavaProject} for a string.
   */
  private static IJavaProject getProjectFromString(String projectString) {
    if (projectString.length() == 0) {
      return null;
    }
    // check project
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspaceRoot.getProject(projectString);
    IJavaProject javaProject = JavaCore.create(project);
    return javaProject.exists() ? javaProject : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Button adapter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ButtonAdapter implements IStringButtonAdapter {
    private ProjectSelectionDialogField m_receiver;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void setReceiver(ProjectSelectionDialogField receiver) {
      m_receiver = receiver;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IStringButtonAdapter
    //
    ////////////////////////////////////////////////////////////////////////////
    public void changeControlPressed(DialogField field) {
      IJavaProject project = selectProject(m_receiver.m_project);
      if (project != null) {
        m_receiver.setProject(project);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Selection
    //
    ////////////////////////////////////////////////////////////////////////////
    private IJavaProject selectProject(IJavaProject initialSelection) {
      Shell shell = Display.getCurrent().getActiveShell();
      ILabelProvider labelProvider =
          new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
      ITreeContentProvider contentProvider = new StandardJavaElementContentProvider();
      // prepare dialog
      ListDialog dialog = new ListDialog(shell);
      dialog.setContentProvider(contentProvider);
      dialog.setLabelProvider(labelProvider);
      dialog.setTitle(Messages.ProjectSelectionDialogField_dialogTitle);
      dialog.setMessage(Messages.ProjectSelectionDialogField_dialogMessage);
      // show projects
      dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()));
      dialog.setInitialSelections(new Object[]{initialSelection});
      // select project
      if (dialog.open() == Window.OK) {
        Object[] objects = dialog.getResult();
        if (objects != null && objects.length == 1) {
          return (IJavaProject) objects[0];
        }
      }
      // no project selected
      return null;
    }
  }
}

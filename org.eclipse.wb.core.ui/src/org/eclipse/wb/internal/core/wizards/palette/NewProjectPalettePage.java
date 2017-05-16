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
package org.eclipse.wb.internal.core.wizards.palette;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.UiMessages;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.ui.ProjectSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * {@link WizardPage} for configuring {@link NewProjectPaletteWizard}.
 *
 * @author scheglov_ke
 * @coverage core.wizards.ui
 */
public final class NewProjectPalettePage extends WizardPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NewProjectPalettePage() {
    super("main");
    setTitle(UiMessages.NewProjectPalettePage_title);
    setDescription(UiMessages.NewProjectPalettePage_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initializing
  //
  ////////////////////////////////////////////////////////////////////////////
  private IJavaProject m_initialProject;

  /**
   * Initializes this page with current selection.
   */
  public void init(IStructuredSelection selection) {
    // initial IJavaProject
    {
      IJavaElement javaElement = null;
      Object selectedElement = selection.getFirstElement();
      if (selectedElement instanceof IAdaptable) {
        IAdaptable adaptable = (IAdaptable) selectedElement;
        javaElement = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
        if (javaElement == null) {
          IResource resource = (IResource) adaptable.getAdapter(IResource.class);
          if (resource != null && resource.getType() != IResource.ROOT) {
            while (javaElement == null && resource.getType() != IResource.PROJECT) {
              resource = resource.getParent();
              javaElement = (IJavaElement) resource.getAdapter(IJavaElement.class);
            }
            if (javaElement == null) {
              javaElement = JavaCore.create(resource);
            }
          }
        }
      }
      // get IJavaProject
      if (javaElement != null) {
        m_initialProject = (IJavaProject) javaElement.getAncestor(IJavaElement.JAVA_PROJECT);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link IJavaProject}.
   */
  IJavaProject getJavaProject() {
    return m_projectField.getProject();
  }

  /**
   * @return the selected {@link ToolkitDescription}.
   */
  ToolkitDescription getToolkit() {
    return m_toolkits.get(m_toolkitField.getSelectionIndex());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private ProjectSelectionDialogField m_projectField;
  private ComboDialogField m_toolkitField;
  private final List<ToolkitDescription> m_toolkits = Lists.newArrayList();

  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    setControl(container);
    GridLayoutFactory.create(container).columns(3);
    // create fields
    {
      m_projectField = ProjectSelectionDialogField.create();
      m_projectField.setButtonLabel(UiMessages.NewProjectPalettePage_projectBrowse);
      m_projectField.setProject(m_initialProject);
      doCreateField(m_projectField, UiMessages.NewProjectPalettePage_projectLabel);
      m_projectField.setUpdateListener(m_validateListener);
    }
    {
      m_toolkitField = new ComboDialogField(SWT.READ_ONLY);
      m_toolkitField.setVisibleItemCount(10);
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          Collections.addAll(m_toolkits, DescriptionHelper.getToolkits());
          // sort by name
          Collections.sort(m_toolkits, new Comparator<ToolkitDescription>() {
            public int compare(ToolkitDescription o1, ToolkitDescription o2) {
              return o1.getName().compareTo(o2.getName());
            }
          });
          // add items
          for (ToolkitDescription toolkit : m_toolkits) {
            m_toolkitField.addItem(toolkit.getName());
          }
        }
      });
      m_toolkitField.selectItem(1); // select second, because first is "Core Java"
      doCreateField(m_toolkitField, UiMessages.NewProjectPalettePage_toolkitLabel);
    }
    m_projectField.setFocus();
    // initial validation
    m_validationEnabled = true;
    validateAll();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_validationEnabled;
  /**
   * Implementation of {@link IDialogFieldListener} for {@link DialogField}'s validation.
   */
  protected final IDialogFieldListener m_validateListener = new IDialogFieldListener() {
    public void dialogFieldChanged(DialogField field) {
      validateAll();
    }
  };

  /**
   * Validate all and disable/enable page.
   */
  private void validateAll() {
    if (m_validationEnabled) {
      String errorMessage = validate();
      setErrorMessage(errorMessage);
      setPageComplete(errorMessage == null);
    }
  }

  /**
   * Validate {@link DialogField}'s and returns error message or <code>null</code>.
   */
  private String validate() {
    // validate project
    IJavaProject javaProject;
    {
      javaProject = m_projectField.getProject();
      if (javaProject == null) {
        return UiMessages.NewProjectPalettePage_validateNoProject;
      }
    }
    // validate toolkit
    {
      int toolkitIndex = m_toolkitField.getSelectionIndex();
      ToolkitDescription toolkit = m_toolkits.get(toolkitIndex);
      IFile paletteFile =
          javaProject.getProject().getFile(
              new Path("wbp-meta/" + toolkit.getId() + ".wbp-palette.xml"));
      if (paletteFile.exists()) {
        return MessageFormat.format(
            UiMessages.NewProjectPalettePage_validateHasToolkit,
            toolkit.getName(),
            paletteFile.getFullPath());
      }
    }
    // no error
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link DialogField}.
   */
  private void doCreateField(DialogField dialogField, String labelText) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    Composite parent = (Composite) getControl();
    DialogFieldUtils.fillControls(parent, dialogField, 3, 40);
  }
}

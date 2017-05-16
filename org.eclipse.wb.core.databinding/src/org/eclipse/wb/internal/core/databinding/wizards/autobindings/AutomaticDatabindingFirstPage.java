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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Standard "New Java Class" wizard page.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public abstract class AutomaticDatabindingFirstPage extends TemplateDesignWizardPage {
  private final IAutomaticDatabindingProvider m_databindingProvider;
  private final String m_initialBeanClassName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticDatabindingFirstPage(IAutomaticDatabindingProvider databindingProvider,
      String initialBeanClassName) {
    m_databindingProvider = databindingProvider;
    m_initialBeanClassName =
        initialBeanClassName == null ? null : ClassUtils.getShortClassName(initialBeanClassName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperClass(m_databindingProvider.getInitialSuperClass(), false);
    // handle initial wizard selection
    if (m_initialBeanClassName != null) {
      setTypeName(getInitialTypeNameWithSuperClass(), true);
    }
  }

  private String getInitialTypeNameWithSuperClass() {
    return m_initialBeanClassName + ClassUtils.getShortClassName(getSuperClass());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int columns) {
    // prepare super classes
    final String[] superClasses = m_databindingProvider.getSuperClasses();
    int current = ArrayUtils.indexOf(superClasses, m_databindingProvider.getInitialSuperClass());
    // create dialog field
    final SelectionButtonDialogFieldGroup fieldsGroup =
        new SelectionButtonDialogFieldGroup(SWT.RADIO, superClasses, 1);
    fieldsGroup.setLabelText(Messages.AutomaticDatabindingFirstPage_superClassLabel);
    fieldsGroup.setSelection(current, true);
    fieldsGroup.doFillIntoGrid(parent, columns);
    // handle change super class
    fieldsGroup.setDialogFieldListener(new IDialogFieldListener() {
      public void dialogFieldChanged(DialogField field) {
        int[] selection = fieldsGroup.getSelection();
        if (selection.length == 1) {
          String superClass = superClasses[selection[0]];
          // handle initial wizard selection
          if (m_initialBeanClassName != null) {
            if (getTypeName().equals(getInitialTypeNameWithSuperClass())) {
              setTypeName(m_initialBeanClassName + ClassUtils.getShortClassName(superClass), true);
            }
          }
          //
          setSuperClass(superClass, false);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    // prepare template content
    InputStream stream = m_databindingProvider.getTemplateFile(newType.getSuperclassName());
    try {
      // generate auto binding code
      String code = IOUtils.toString(stream);
      code = m_databindingProvider.performSubstitutions(code, imports);
      // create type
      fillTypeFromTemplate(newType, imports, monitor, new ByteArrayInputStream(code.getBytes()));
    } catch (Throwable e) {
      throw new CoreException(DesignerPlugin.createStatus("Error load template file", e));
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }
}
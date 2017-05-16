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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.NewContainerWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;

/**
 * Utils for {@link Wizard}s
 *
 * @author scheglov_ke
 * @coverage core.wizards.ui
 */
public class WizardUtils {
  /**
   * @return the {@link IJavaProject} of selection, may be <code>null</code>.
   */
  public static IJavaProject getJavaProject(IStructuredSelection selection) {
    IJavaElement javaElement = getJavaElement(selection);
    return javaElement == null ? null : javaElement.getJavaProject();
  }

  /**
   * @return the {@link IJavaElement} of selection, may be <code>null</code>.
   */
  public static IJavaElement getJavaElement(IStructuredSelection selection) {
    NewContainerWizardPage tmp = new NewContainerWizardPage("__tmp") {
      public void createControl(Composite parent) {
      }
    };
    try {
      return (IJavaElement) ReflectionUtils.invokeMethodEx(
          tmp,
          "getInitialJavaElement(org.eclipse.jface.viewers.IStructuredSelection)",
          selection);
    } finally {
      tmp.dispose();
    }
  }
}
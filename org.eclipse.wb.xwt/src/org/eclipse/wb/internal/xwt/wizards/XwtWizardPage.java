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
package org.eclipse.wb.internal.xwt.wizards;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.xwt.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;

/**
 * Abstract {@link WizardPage} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.wizards
 */
public abstract class XwtWizardPage extends RcpWizardPage {
  private IFile m_javaFile;
  private IFile m_xwtFile;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XwtWizardPage() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
      throws CoreException {
    String templatePath = getTemplatePath_Java();
    InputStream template = Activator.getFile(templatePath);
    fillTypeFromTemplate(newType, imports, monitor, template);
    m_javaFile = (IFile) newType.getUnderlyingResource();
  }

  protected void createXWT() throws Exception {
    IType newType = getCreatedType();
    // prepare template
    String template;
    {
      String templatePath = getTemplatePath_XWT();
      InputStream templateStream = Activator.getFile(templatePath);
      template = IOUtils2.readString(templateStream);
    }
    // prepare content
    String content;
    {
      String qualifiedTypeName = newType.getFullyQualifiedName();
      content = StringUtils.replace(template, "%TypeName%", qualifiedTypeName);
    }
    // create XWT file
    IFolder folder = (IFolder) getPackageFragment().getUnderlyingResource();
    m_xwtFile = folder.getFile(newType.getElementName() + ".xwt");
    IOUtils2.setFileContents(m_xwtFile, content);
  }

  /**
   * @return the path to the Java file template in {@link Activator}.
   */
  protected abstract String getTemplatePath_Java();

  /**
   * @return the path to the XWT file template in {@link Activator}.
   */
  protected abstract String getTemplatePath_XWT();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the created Java {@link IFile}.
   */
  public IFile getFileJava() {
    return m_javaFile;
  }

  /**
   * @return the create XWT {@link IFile}.
   */
  public IFile getFileXWT() {
    return m_xwtFile;
  }
}
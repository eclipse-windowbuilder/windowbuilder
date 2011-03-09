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
package org.eclipse.wb.internal.core.xml.editor;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.editor.errors.ExceptionComposite;
import org.eclipse.wb.internal.core.editor.errors.report.ErrorReport;
import org.eclipse.wb.internal.core.editor.errors.report.ErrorReport.SourceInfo;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.xml.editor.actions.RefreshAction;
import org.eclipse.wb.internal.core.xml.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation for XML.
 * 
 * @author mitin_aa
 * @coverage XML.editor
 */
public final class XmlExceptionComposite extends ExceptionComposite {
  private IFile m_file;
  private IDocument m_document;
  private XmlObjectInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public XmlExceptionComposite(Composite parent, int style) {
    super(parent, style);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExceptionComposite
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ErrorReport getErrorReport() {
    IProject project = m_file.getProject();
    SourceInfo sourceInfo = getSourceInfo(m_file, m_document);
    return new ErrorReport(getScreenshotImage(), getProductCode(m_rootObject), project, sourceInfo);
  }

  @Override
  protected void doShowSource(int sourcePosition) {
    SwitchAction.showSource(sourcePosition);
  }

  @Override
  protected void doRefresh() {
    new RefreshAction().run();
  }

  /**
   * @return the product code from the {@link ToolkitDescription} if available otherwise returns
   *         "UNKNOWN".
   */
  private static String getProductCode(XmlObjectInfo objectInfo) {
    String proId;
    // proId in Product lookup was replaced with the branding name lookup,
    // when we removed Shared from the WB build.
    proId = BrandingUtils.getBranding().getProductName();
    //if (objectInfo != null) {
    //	proId = objectInfo.getDescription().getToolkit().getProduct().getProId();
    //} else {
    //	proId = Products.DESIGNER_WBPRO.getProId();
    //}
    if (StringUtils.isEmpty(proId)) {
      proId = "UNKNOWN";
    }
    return proId;
  }

  /**
   * @return the contents of the IDocument with name of the IFile or <code>null</code> if any error.
   */
  private static SourceInfo getSourceInfo(IFile file, IDocument document) {
    try {
      return new ErrorReport.SourceInfo(file.getName(), document.get());
    } catch (Throwable e) {
      // ignore, just send nothing
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link Throwable} to display with additional information which may be included into
   * problem report.
   * 
   * @param e
   *          the {@link Throwable} to display.
   * @param screenshot
   *          the {@link Image} of entire shell just before error. Can be <code>null</code> in case
   *          of parse error when no screenshot needed.
   * @param file
   *          the IFile instance of editing document.
   * @param document
   *          the currently editing document, may be modified.
   * @param rootObject
   *          the root object of the hierarchy, can be <code>null</code>.
   */
  public void setException(Throwable e,
      Image screenshot,
      IFile file,
      IDocument document,
      XmlObjectInfo rootObject) {
    m_file = file;
    m_document = document;
    m_rootObject = rootObject;
    setException0(e, screenshot);
  }
}

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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.actions.RefreshAction;
import org.eclipse.wb.internal.core.editor.actions.SwitchAction;
import org.eclipse.wb.internal.core.editor.errors.report2.IReportEntry;
import org.eclipse.wb.internal.core.editor.errors.report2.StringFileReportEntry;
import org.eclipse.wb.internal.core.editor.errors.report2.ZipFileErrorReport;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation for Java-related UI.
 *
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public final class JavaExceptionComposite extends ExceptionComposite {
  private ICompilationUnit m_compilationUnit;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaExceptionComposite(Composite parent, int style) {
    super(parent, style);
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
   * @param compilationUnit
   *          the compilation unit in which the problem occurred. Cannot be <code>null</code>.
   * @param javaInfo
   *          the root {@link JavaInfo}. Can be <code>null</code>.
   */
  public void setException(Throwable e,
      Image screenshot,
      ICompilationUnit compilationUnit,
      JavaInfo javaInfo) {
    setException0(e, screenshot);
    Assert.isNotNull(compilationUnit);
    m_compilationUnit = compilationUnit;
  }

  @Override
  protected ZipFileErrorReport getZipFileErrorReport() {
    IProject project = m_compilationUnit.getJavaProject().getProject();
    return new ZipFileErrorReport(getScreenshotImage(),
        project,
        getSourceFileReport(m_compilationUnit));
  }

  @Override
  protected void doShowSource(int sourcePosition) {
    SwitchAction.showSource(sourcePosition);
  }

  @Override
  protected void doRefresh() {
    new RefreshAction().run();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Error Report related.
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the report info containing actual source of editing CU.
   */
  public static IReportEntry getSourceFileReport(ICompilationUnit compilationUnit) {
    try {
      return new StringFileReportEntry(compilationUnit.getElementName(),
          compilationUnit.getSource());
    } catch (Throwable e) {
      // ignore, just send nothing
    }
    return null;
  }
}

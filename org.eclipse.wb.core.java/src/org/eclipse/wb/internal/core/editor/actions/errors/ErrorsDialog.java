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
package org.eclipse.wb.internal.core.editor.actions.errors;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.errors.JavaExceptionComposite;
import org.eclipse.wb.internal.core.editor.errors.report2.CreateReportDialog;
import org.eclipse.wb.internal.core.editor.errors.report2.ZipFileErrorReport;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.ImageDisposer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

/**
 * Dialog for displaying errors using {@link IErrorPage}'s.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public final class ErrorsDialog extends ResizableTitleAreaDialog {
  private static final int CONTACT_SUPPORT_ID = 999;
  private final ObjectInfo m_rootObject;
  private final List<IErrorPage> m_pages;
  private final Dialog m_dialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ErrorsDialog(Shell parentShell, ObjectInfo rootObject, List<IErrorPage> pages) {
    super(parentShell, DesignerPlugin.getDefault());
    m_rootObject = rootObject;
    m_pages = pages;
    //
    Image screenshot = DesignerExceptionUtils.makeScreenshot();
    JavaInfo rootObjectJava = m_rootObject instanceof JavaInfo ? (JavaInfo) m_rootObject : null;
    ICompilationUnit unit = rootObject != null ? rootObjectJava.getEditor().getModelUnit() : null;
    IProject project = unit != null ? unit.getJavaProject().getProject() : null;
    ZipFileErrorReport errorReport =
        new ZipFileErrorReport(screenshot,
            project,
            JavaExceptionComposite.getSourceFileReport(unit));
    m_dialog = new CreateReportDialog(DesignerPlugin.getShell(), screenshot, errorReport);
    ImageDisposer.add(m_dialog, "ContactSupportDialog_screenshot", screenshot);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Messages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    configureMessages();
    return control;
  }

  /**
   * Subclasses override this methods to set title and message for this {@link TitleAreaDialog}.
   */
  protected void configureMessages() {
    getShell().setText(Messages.ErrorsDialog_shellTitle);
    setTitle(Messages.ErrorsDialog_title);
    setMessage(Messages.ErrorsDialog_message);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, CONTACT_SUPPORT_ID, Messages.ErrorsDialog_supportButton, false);
    createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    //
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    GridLayoutFactory.create(container);
    // create TabFolder for pages
    TabFolder tabFolder = new TabFolder(container, SWT.NONE);
    GridDataFactory.create(tabFolder).grab().fill();
    // create pages
    for (IErrorPage page : m_pages) {
      page.setRoot(m_rootObject);
      // create tab
      TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
      tabItem.setText(page.getTitle());
      // create control for page
      Control pageControl = page.create(tabFolder);
      tabItem.setControl(pageControl);
    }
    return area;
  }

  @Override
  protected void buttonPressed(int buttonId) {
    close();
    if (CONTACT_SUPPORT_ID == buttonId) {
      ExecutionUtils.runAsync(new RunnableEx() {
        public void run() throws Exception {
          handleContactSupport();
        }
      });
    }
  }

  /**
   * Engages error report engine.
   */
  private void handleContactSupport() {
    m_dialog.open();
  }
}

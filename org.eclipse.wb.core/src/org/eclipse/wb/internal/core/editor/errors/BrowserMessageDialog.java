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

import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

/**
 * A simple {@link Dialog} using {@link Browser} to render read-only info.
 *
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public class BrowserMessageDialog extends Dialog {
  // default html
  private static final String HTML_HEADER = "<html><head><style type=\"text/css\"> "
      + "body { background-color: %bg_color%; font-size: 8pt; font-family: Verdana;Helvetica;} "
      + "table { font-size: 8pt; font-family: Verdana;Helvetica;} "
      + "h3 {font-size: 10pt;}</style></head><body>";
  private static final String HTML_FOOTER = "</body></html>";
  // fields
  private final String m_title;
  private String m_htmlToShow;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected BrowserMessageDialog(Shell parentShell, String title) {
    super(parentShell);
    m_title = title;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite composite = new Composite(area, SWT.NONE);
    GridDataFactory.modify(composite).grab().fill();
    composite.setLayout(new FillLayout());
    BrowserComposite browser = new BrowserComposite(composite, SWT.NONE);
    browser.setText(m_htmlToShow);
    return area;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

  @Override
  protected Point getInitialSize() {
    return getParentShell().getSize();
  }

  @Override
  protected Point getInitialLocation(Point initialSize) {
    return getParentShell().getLocation();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the content part of HTML. This means that <code>html</code> parameter should contain only
   * the contents needed to display in this dialog (i.e. without html, head, title, body tags).
   */
  public void setHTML(String html) {
    m_htmlToShow =
        StringUtils.replace(
            HTML_HEADER,
            "%bg_color%",
            DesignerExceptionUtils.getColorWebString(IColorConstants.button))
            + (m_title != null ? "<h3>" + m_title + "</h3>" : "")
            + html
            + HTML_FOOTER;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Usage
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void openMessage(Shell shell, String title, String html) {
    BrowserMessageDialog dialog = new BrowserMessageDialog(shell, title);
    dialog.setHTML(html);
    dialog.open();
  }
}

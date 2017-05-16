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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ReusableDialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Abstract {@link Dialog} for color choosing.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractColorDialog extends ReusableDialog {
  private ColorInfo m_colorInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractColorDialog(Shell parentShell) {
    super(parentShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Color access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link ColorInfo}.
   */
  public final ColorInfo getColorInfo() {
    return m_colorInfo;
  }

  /**
   * Sets initial {@link ColorInfo}.
   */
  public void setColorInfo(ColorInfo colorInfo) {
    m_colorInfo = colorInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Active page access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the title of currently selected page.
   */
  protected final String getSelectedPageTitle() {
    int selectionIndex = m_tabFolder.getSelectionIndex();
    return m_tabFolder.getItem(selectionIndex).getText();
  }

  /**
   * Selects the page with given title.
   */
  protected final void selectPageByTitle(String title) {
    TabItem[] tabItems = m_tabFolder.getItems();
    for (int i = 0; i < tabItems.length; i++) {
      TabItem tabItem = tabItems[i];
      if (tabItem.getText().equals(title)) {
        m_tabFolder.setSelection(new TabItem[]{tabItem});
        break;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private ColorPreviewCanvas m_previewCanvas;
  private TabFolder m_tabFolder;

  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    area.setLayout(new GridLayout());
    // create preview group
    {
      Group group = new Group(area, SWT.NONE);
      GridDataFactory.create(group).fillH();
      group.setLayout(new FillLayout());
      group.setText(Messages.AbstractColorDialog_previewGroup);
      //
      m_previewCanvas = new ColorPreviewCanvas(group, SWT.NONE, showShortTextInColorPreview());
    }
    // create folder for tabs
    {
      m_tabFolder = new TabFolder(area, SWT.NONE);
      m_tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
      addPages(m_tabFolder);
    }
    //
    return area;
  }

  /**
   * Overridden to return <code>true</code> if the created {@link ColorPreviewCanvas} should have
   * the color shown with the short-text version.
   */
  protected boolean showShortTextInColorPreview() {
    return false;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.AbstractColorDialog_title);
  }

  @Override
  protected void onBeforeOpen() {
    m_previewCanvas.setColor(m_colorInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new page with given title and {@link AbstractColorsComposite}.
   */
  protected final void addPage(String title, AbstractColorsComposite composite) {
    TabItem tabItem = new TabItem(m_tabFolder, SWT.NONE);
    tabItem.setText(title);
    tabItem.setControl(composite);
  }

  /**
   * Adds pages with {@link AbstractColorsComposite}'s.
   */
  protected abstract void addPages(Composite parent);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link ColorInfo} that should be used as result.
   */
  public final void setResultColor(ColorInfo colorInfo) {
    m_colorInfo = colorInfo;
    m_previewCanvas.setColor(m_colorInfo);
  }

  /**
   * Closes dialog with "OK" result.
   */
  public final void closeOk() {
    if (m_colorInfo != null) {
      buttonPressed(IDialogConstants.OK_ID);
    }
  }
}
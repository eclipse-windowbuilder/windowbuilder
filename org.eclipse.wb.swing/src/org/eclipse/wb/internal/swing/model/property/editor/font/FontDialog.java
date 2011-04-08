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
package org.eclipse.wb.internal.swing.model.property.editor.font;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.awt.Font;
import java.util.List;

/**
 * Dialog for {@link Font} choosing.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class FontDialog extends ResizableDialog {
  private FontInfo m_fontInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected FontDialog(Shell parentShell) {
    super(parentShell, Activator.getDefault());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link FontInfo}.
   */
  public FontInfo getFontInfo() {
    return m_fontInfo;
  }

  /**
   * Sets the selected {@link FontInfo}
   */
  public void setFontInfo(FontInfo fontInfo) {
    m_fontInfo = fontInfo;
    updateGUI();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private FontPreviewCanvas m_previewCanvas;
  private TabFolder m_tabFolder;
  private final List<AbstractFontPage> m_pages = Lists.newArrayList();

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    // create preview
    {
      Group previewGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(previewGroup).grabH().fillH();
      GridLayoutFactory.create(previewGroup);
      previewGroup.setText(ModelMessages.FontDialog_previewText);
      //
      m_previewCanvas = new FontPreviewCanvas(previewGroup, SWT.NONE);
      GridDataFactory.create(m_previewCanvas).grab().fill();
    }
    // create folder for pages
    {
      m_tabFolder = new TabFolder(area, SWT.NONE);
      GridDataFactory.create(m_tabFolder).grab().fill();
      addPages(m_tabFolder);
    }
    //
    return area;
  }

  @Override
  public void create() {
    super.create();
    updateGUI();
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.FontDialog_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates GUI using current {@link FontInfo}.
   */
  private void updateGUI() {
    if (getShell() != null) {
      // update preview
      m_previewCanvas.setFontInfo(m_fontInfo);
      // notify pages
      for (AbstractFontPage page : m_pages) {
        boolean suits = page.setFont(m_fontInfo);
        if (suits) {
          m_tabFolder.setSelection(m_pages.indexOf(page));
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new page with given title and {@link AbstractFontPage}.
   */
  protected final void addPage(String title, AbstractFontPage page) {
    m_pages.add(page);
    TabItem tabItem = new TabItem(m_tabFolder, SWT.NONE);
    tabItem.setText(title);
    tabItem.setControl(page);
  }

  /**
   * Adds pages with {@link AbstractFontPage}'s.
   */
  protected void addPages(Composite parent) {
    addPage(ModelMessages.FontDialog_pageConstruction, new ExplicitFontPage(parent, SWT.NONE, this));
    addPage(ModelMessages.FontDialog_pageDerived, new DerivedFontPage(parent, SWT.NONE, this));
    addPage(ModelMessages.FontDialog_pageSwing, new UiManagerFontPage(parent, SWT.NONE, this));
  }

  /**
   * @return the {@link AbstractFontPage} of this dialog, to configure them externally.
   */
  List<AbstractFontPage> getPages() {
    return m_pages;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Closes dialog with "OK" result.
   */
  public final void closeOk() {
    setReturnCode(IDialogConstants.OK_ID);
    close();
  }
}

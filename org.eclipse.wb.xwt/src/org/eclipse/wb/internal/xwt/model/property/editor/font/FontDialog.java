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
package org.eclipse.wb.internal.xwt.model.property.editor.font;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ReusableDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.util.List;

/**
 * Dialog for {@link Font} choosing.
 * <p>
 * Note, this this dialog is reduced, no support for multiple pages.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class FontDialog extends ReusableDialog {
  private FontInfo m_fontInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontDialog(Shell parentShell) {
    super(parentShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void disposeFont() {
    if (m_fontInfo != null) {
      m_fontInfo.dispose();
      m_fontInfo = null;
    }
  }

  /**
   * @return the selected {@link FontInfo}.
   */
  public FontInfo getFontInfo() {
    return m_fontInfo;
  }

  /**
   * Sets the selected {@link FontInfo}.
   */
  public void setFontInfo(FontInfo fontInfo) {
    disposeFont();
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

  //
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    // create preview
    {
      Group previewGroup = new Group(area, SWT.NONE);
      GridDataFactory.create(previewGroup).grabH().fillH();
      GridLayoutFactory.create(previewGroup);
      previewGroup.setText("Selected Font");
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
  protected void onBeforeOpen() {
    updateGUI();
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Font chooser");
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
        page.setFont(m_fontInfo);
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
    addPage("Construction", new ConstructionFontPage(parent, SWT.NONE, this));
  }
}
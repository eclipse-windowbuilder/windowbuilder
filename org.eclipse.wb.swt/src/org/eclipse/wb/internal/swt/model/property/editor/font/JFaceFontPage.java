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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.support.FontSupport;
import org.eclipse.wb.internal.swt.support.JFaceSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link AbstractFontPage} for constructing {@link Font} using JFace constants.
 * 
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class JFaceFontPage extends AbstractFontPage {
  public static final String NAME = "JFace";
  //
  private final Table m_fontTable;
  private final List<FontInfo> m_fonts;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JFaceFontPage(Composite parent, int style, FontDialog fontDialog, JavaInfo javaInfo) {
    super(parent, style, fontDialog);
    GridLayoutFactory.create(this);
    //
    {
      new Label(this, SWT.NONE).setText("Select font from the list:");
    }
    {
      m_fontTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
      GridDataFactory.create(m_fontTable).hintVC(15).grab().fill();
      m_fontTable.setHeaderVisible(true);
      m_fontTable.setLinesVisible(true);
      // create columns
      {
        new TableColumn(m_fontTable, SWT.NONE).setText("Name");
        new TableColumn(m_fontTable, SWT.NONE).setText("Value");
      }
      // add items
      List<FontInfo> fonts;
      try {
        fonts = JFaceSupport.getJFaceFonts();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        fonts = Collections.emptyList();
      }
      m_fonts = fonts;
      //
      for (FontInfo fontInfo : m_fonts) {
        TableItem tableItem = new TableItem(m_fontTable, SWT.NONE);
        tableItem.setText(0, fontInfo.getName());
        try {
          Object fontData = FontSupport.getFontData(fontInfo.getFont());
          tableItem.setText(
              1,
              "" + FontSupport.getFontName(fontData) + " " + FontSupport.getFontSize(fontData));
        } catch (Throwable e) {
          tableItem.setText(1, "???");
        }
      }
      // pack columns
      for (int i = 0; i < m_fontTable.getColumnCount(); i++) {
        m_fontTable.getColumn(i).pack();
      }
      // add listeners
      m_fontTable.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          FontInfo fontInfo = m_fonts.get(m_fontTable.getSelectionIndex());
          m_fontDialog.setFontInfo(fontInfo);
        }
      });
      m_fontTable.addListener(SWT.MouseDoubleClick, new Listener() {
        public void handleEvent(Event event) {
          m_fontDialog.closeOk();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setFont(FontInfo fontInfo) {
  }
}
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
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Implementation of {@link AbstractFontPage} for selecting {@link Font} from {@link UIManager}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class UiManagerFontPage extends AbstractFontPage {
  private final Table m_fontTable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiManagerFontPage(Composite parent, int style, FontDialog fontDialog) {
    super(parent, style, fontDialog);
    GridLayoutFactory.create(this);
    //
    {
      new Label(this, SWT.NONE).setText(ModelMessages.UiManagerFontPage_listLabel);
    }
    //
    {
      m_fontTable = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
      GridDataFactory.create(m_fontTable).hintVC(15).grab().fill();
      m_fontTable.setHeaderVisible(true);
      m_fontTable.setLinesVisible(true);
      // create columns
      {
        new TableColumn(m_fontTable, SWT.NONE).setText(ModelMessages.UiManagerFontPage_nameColumn);
        new TableColumn(m_fontTable, SWT.NONE).setText(ModelMessages.UiManagerFontPage_valueColumn);
      }
      // add items
      prepareFonts();
      for (UiManagerFontInfo fontInfo : m_fonts) {
        TableItem tableItem = new TableItem(m_fontTable, SWT.NONE);
        tableItem.setText(0, fontInfo.getKey());
        tableItem.setText(1, fontInfo.getValueText());
      }
      // pack columns
      for (TableColumn column : m_fontTable.getColumns()) {
        column.pack();
      }
      // add listeners
      m_fontTable.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          int selectionIndex = m_fontTable.getSelectionIndex();
          FontInfo fontInfo = m_fonts.get(selectionIndex);
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
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFont(FontInfo fontInfo) {
    if (fontInfo instanceof UiManagerFontInfo) {
      String key = ((UiManagerFontInfo) fontInfo).getKey();
      for (TableItem item : m_fontTable.getItems()) {
        if (item.getText(0).equals(key)) {
          m_fontTable.setSelection(item);
        }
      }
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  private static List<UiManagerFontInfo> m_fonts;

  /**
   * Prepares {@link FontInfo}'s for {@link Font}'s from {@link UIManager}.
   */
  private static void prepareFonts() {
    if (m_fonts == null) {
      m_fonts = Lists.newArrayList();
      UIDefaults defaults = UIManager.getLookAndFeelDefaults();
      // prepare set of all String keys in UIManager
      Set<String> allKeys = Sets.newTreeSet();
      for (Iterator<?> I = defaults.keySet().iterator(); I.hasNext();) {
        Object key = I.next();
        if (key instanceof String) {
          allKeys.add((String) key);
        }
      }
      // add FontInfo for each Font key
      for (String key : allKeys) {
        Font font = defaults.getFont(key);
        if (font != null) {
          UiManagerFontInfo fontInfo = new UiManagerFontInfo(key, font);
          m_fonts.add(fontInfo);
        }
      }
    }
  }
}

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

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Implementation of {@link AbstractFontPage} for constructing {@link Font} using family, style and
 * size.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ExplicitFontPage extends AbstractFontPage {
  private final String m_families[];
  private final Text m_familyText;
  private final List m_familyList;
  //
  private static final String[] m_styleTitles = new String[]{
      ModelMessages.ExplicitFontPage_stylePlain,
      ModelMessages.ExplicitFontPage_styleBold,
      ModelMessages.ExplicitFontPage_styleItalic,
      ModelMessages.ExplicitFontPage_styleBoldItalic};
  private static final int[] m_styleValues = new int[]{
      Font.PLAIN,
      Font.BOLD,
      Font.ITALIC,
      Font.BOLD | Font.ITALIC};
  private final Text m_styleText;
  private final List m_styleList;
  //
  private final Text m_sizeText;
  private final List m_sizeList;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExplicitFontPage(Composite parent, int style, FontDialog fontDialog) {
    super(parent, style, fontDialog);
    GridLayoutFactory.create(this).columns(3);
    // labels
    {
      new Label(this, SWT.NONE).setText(ModelMessages.ExplicitFontPage_family);
      new Label(this, SWT.NONE).setText(ModelMessages.ExplicitFontPage_atyle);
      new Label(this, SWT.NONE).setText(ModelMessages.ExplicitFontPage_size);
    }
    // text's
    {
      {
        m_familyText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.create(m_familyText).fill();
      }
      {
        m_styleText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.create(m_styleText).fill();
      }
      {
        m_sizeText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.create(m_sizeText).fill();
      }
    }
    // list's
    {
      {
        m_familyList = new List(this, SWT.BORDER | SWT.V_SCROLL);
        GridDataFactory.create(m_familyList).hintVC(12).grab().fill();
        // add items
        m_families =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String family : m_families) {
          m_familyList.add(family);
        }
        // add listener
        m_familyList.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            int index = m_familyList.getSelectionIndex();
            String family = m_families[index];
            m_familyText.setText(family);
            updateFont();
          }
        });
      }
      {
        m_styleList = new List(this, SWT.BORDER);
        GridDataFactory.create(m_styleList).hintHC(20).fill();
        // add items
        for (int i = 0; i < m_styleTitles.length; i++) {
          String styleTitle = m_styleTitles[i];
          m_styleList.add(styleTitle);
        }
        // add listener
        m_styleList.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            int index = m_styleList.getSelectionIndex();
            String fontStyle = m_styleTitles[index];
            m_styleText.setText(fontStyle);
            updateFont();
          }
        });
      }
      {
        m_sizeList = new List(this, SWT.BORDER | SWT.V_SCROLL);
        GridDataFactory.create(m_sizeList).hintC(10, 12).fill();
        // add items
        for (int i = 5; i < 100; i++) {
          m_sizeList.add("" + i);
        }
        // add listener
        m_sizeList.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            String size = m_sizeList.getSelection()[0];
            m_sizeText.setText(size);
            updateFont();
          }
        });
      }
    }
  }

  /**
   * Updates current font in {@link FontDialog} based on selection in controls.
   */
  private void updateFont() {
    String family = m_familyText.getText();
    // prepare style
    int style = Font.PLAIN;
    {
      int index = m_styleList.getSelectionIndex();
      if (index != -1) {
        style = m_styleValues[index];
      }
    }
    // prepare size
    int size = 12;
    try {
      size = Integer.parseInt(m_sizeText.getText());
    } catch (Throwable e) {
    }
    //
    Font font = new Font(family, style, size);
    m_fontDialog.setFontInfo(new ExplicitFontInfo(font));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFont(FontInfo fontInfo) {
    if (fontInfo != null) {
      Font font = fontInfo.getFont();
      if (font == null) {
        return false;
      }
      // family
      {
        String family = font.getFamily();
        m_familyList.setSelection(new String[]{family});
        m_familyText.setText(family);
      }
      // style
      for (int i = 0; i < m_styleValues.length; i++) {
        int style = m_styleValues[i];
        if (style == font.getStyle()) {
          m_styleList.select(i);
          m_styleText.setText(m_styleTitles[i]);
        }
      }
      // size
      {
        String text = "" + font.getSize();
        m_sizeList.setSelection(new String[]{text});
        m_sizeText.setText(text);
      }
    }
    return fontInfo instanceof ExplicitFontInfo;
  }
}

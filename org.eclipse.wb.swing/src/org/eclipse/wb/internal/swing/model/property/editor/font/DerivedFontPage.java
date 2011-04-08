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

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.core.controls.Separator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Implementation of {@link AbstractFontPage} for deriving {@link Font} by changing its family,
 * style or size.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class DerivedFontPage extends AbstractFontPage {
  private final Combo m_familyCombo;
  private final Button m_boldSame;
  private final Button m_boldSet;
  private final Button m_boldClear;
  private final Button m_italicSame;
  private final Button m_italicSet;
  private final Button m_italicClear;
  private final Button m_relativeButton;
  private final Button m_absoluteButton;
  private final CSpinner m_relativeSpinner;
  private final CSpinner m_absoluteSpinner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DerivedFontPage(Composite parent, int style, FontDialog fontDialog) {
    super(parent, style, fontDialog);
    GridLayoutFactory.create(this);
    // update listener
    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        updateFont();
      }
    };
    // family
    {
      createSeparator(this, ModelMessages.DerivedFontPage_familySeparator);
      Composite composite = new Composite(this, SWT.NONE);
      GridDataFactory.create(composite).grabH().fill();
      GridLayoutFactory.create(composite).columns(2).noMargins();
      {
        Label label = new Label(composite, SWT.NONE);
        GridDataFactory.create(label).indentHC(2).hintHC(15);
        label.setText(ModelMessages.DerivedFontPage_familyLabel);
      }
      {
        m_familyCombo = new Combo(composite, SWT.READ_ONLY);
        GridDataFactory.create(m_familyCombo).grabH().fillH();
        m_familyCombo.addListener(SWT.Selection, listener);
        m_familyCombo.setVisibleItemCount(15);
        // add items
        {
          m_familyCombo.add(ModelMessages.DerivedFontPage_unchanged);
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          for (String family : ge.getAvailableFontFamilyNames()) {
            m_familyCombo.add(family);
          }
        }
      }
    }
    // style
    {
      createSeparator(this, ModelMessages.DerivedFontPage_styleSeparator);
      Composite composite = new Composite(this, SWT.NONE);
      GridDataFactory.create(composite).grabH().fill();
      GridLayoutFactory.create(composite).columns(2).noMargins();
      // BOLD
      {
        {
          Label label = new Label(composite, SWT.NONE);
          GridDataFactory.create(label).indentHC(2).hintHC(15);
          label.setText(ModelMessages.DerivedFontPage_styleBold);
        }
        {
          Composite composite2 = new Composite(composite, SWT.NONE);
          GridLayoutFactory.create(composite2).columns(3).noMargins();
          m_boldSame =
              createRadioButton(
                  composite2,
                  ModelMessages.DerivedFontPage_styleBoldUnchanged,
                  listener);
          m_boldSet =
              createRadioButton(composite2, ModelMessages.DerivedFontPage_styleBoldSet, listener);
          m_boldClear =
              createRadioButton(composite2, ModelMessages.DerivedFontPage_styleBoldClear, listener);
        }
      }
      // ITALIC
      {
        {
          Label label = new Label(composite, SWT.NONE);
          GridDataFactory.create(label).indentHC(2).hintHC(15);
          label.setText(ModelMessages.DerivedFontPage_styleItalic);
        }
        {
          Composite composite2 = new Composite(composite, SWT.NONE);
          GridLayoutFactory.create(composite2).columns(3).noMargins();
          m_italicSame =
              createRadioButton(
                  composite2,
                  ModelMessages.DerivedFontPage_styleItalicUnchanged,
                  listener);
          m_italicSet =
              createRadioButton(composite2, ModelMessages.DerivedFontPage_styleItalicSet, listener);
          m_italicClear =
              createRadioButton(
                  composite2,
                  ModelMessages.DerivedFontPage_styleItalicClear,
                  listener);
        }
      }
    }
    // size
    {
      createSeparator(this, ModelMessages.DerivedFontPage_size);
      Composite composite = new Composite(this, SWT.NONE);
      GridDataFactory.create(composite).grabH().fill();
      GridLayoutFactory.create(composite).columns(2).noMargins();
      // relative
      {
        {
          m_relativeButton =
              createRadioButton(composite, ModelMessages.DerivedFontPage_sizeRelative, listener);
          GridDataFactory.create(m_relativeButton).indentHC(2).hintHC(15);
        }
        {
          m_relativeSpinner = new CSpinner(composite, SWT.BORDER);
          GridDataFactory.create(m_relativeSpinner).hintHC(15);
          m_relativeSpinner.addListener(SWT.Selection, listener);
          m_relativeSpinner.setRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
      }
      // absolute
      {
        {
          m_absoluteButton =
              createRadioButton(composite, ModelMessages.DerivedFontPage_sizeAbsolute, listener);
          GridDataFactory.create(m_absoluteButton).indentHC(2).hintHC(15);
        }
        {
          m_absoluteSpinner = new CSpinner(composite, SWT.BORDER);
          GridDataFactory.create(m_absoluteSpinner).hintHC(15);
          m_absoluteSpinner.addListener(SWT.Selection, listener);
          m_absoluteSpinner.setRange(0, Integer.MAX_VALUE);
        }
      }
    }
  }

  /**
   * Creates separator with given text.
   */
  private static void createSeparator(Composite parent, String text) {
    Separator separator = new Separator(parent, SWT.NONE);
    GridDataFactory.create(separator).grabH().fillH();
    separator.setText(text);
    separator.setForeground(separator.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
  }

  /**
   * Creates radio {@link Button} with given text and selection {@link Listener}.
   */
  private static Button createRadioButton(Composite parent, String text, Listener listener) {
    Button button = new Button(parent, SWT.RADIO);
    button.setText(text);
    button.addListener(SWT.Selection, listener);
    return button;
  }

  /**
   * Updates current font in {@link FontDialog} based on selection in controls.
   */
  private void updateFont() {
    m_relativeSpinner.setEnabled(m_relativeButton.getSelection());
    m_absoluteSpinner.setEnabled(m_absoluteButton.getSelection());
    // family
    String newFamily = null;
    {
      int index = m_familyCombo.getSelectionIndex();
      if (index != 0) {
        newFamily = m_familyCombo.getItem(index);
      }
    }
    // BOLD
    Boolean newBold = null;
    if (m_boldSet.getSelection()) {
      newBold = true;
    } else if (m_boldClear.getSelection()) {
      newBold = false;
    }
    // ITALIC
    Boolean newItalic = null;
    if (m_italicSet.getSelection()) {
      newItalic = true;
    } else if (m_italicClear.getSelection()) {
      newItalic = false;
    }
    // size
    Integer deltaSize = null;
    Integer newSize = null;
    if (m_relativeButton.getSelection()) {
      int delta = m_relativeSpinner.getSelection();
      if (delta != 0) {
        deltaSize = delta;
      }
    }
    if (m_absoluteButton.getSelection()) {
      newSize = m_absoluteSpinner.getSelection();
    }
    // set new Font
    m_fontDialog.setFontInfo(new DerivedFontInfo(m_baseFont,
        m_baseFontSource,
        null,
        newFamily,
        newBold,
        newItalic,
        deltaSize,
        newSize));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  private Font m_baseFont;
  private String m_baseFontSource;

  /**
   * Configures with {@link GenericProperty}, we should get "base" attributes required to create
   * {@link DerivedFontInfo}.
   * 
   * @return <code>true</code> if configuration was successful.
   */
  boolean configure(GenericProperty property) throws Exception {
    JavaInfo javaInfo = property.getJavaInfo();
    if (javaInfo.getObject() instanceof Component) {
      m_baseFont = (Font) property.getDefaultValue();
      m_baseFontSource = TemplateUtils.format("{0}.getFont()", javaInfo);
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFont(FontInfo _font) {
    if (_font instanceof DerivedFontInfo) {
      DerivedFontInfo font = (DerivedFontInfo) _font;
      // family
      if (font.m_newFamily != null) {
        m_familyCombo.setText(font.m_newFamily);
      } else {
        m_familyCombo.select(0);
      }
      // BOLD
      if (font.m_newBold == null) {
        m_boldSame.setSelection(true);
        m_boldSet.setSelection(false);
        m_boldClear.setSelection(false);
      } else if (font.m_newBold.booleanValue()) {
        m_boldSame.setSelection(false);
        m_boldSet.setSelection(true);
        m_boldClear.setSelection(false);
      } else {
        m_boldSame.setSelection(false);
        m_boldSet.setSelection(false);
        m_boldClear.setSelection(true);
      }
      // ITALIC
      if (font.m_newItalic == null) {
        m_italicSame.setSelection(true);
        m_italicSet.setSelection(false);
        m_italicClear.setSelection(false);
      } else if (font.m_newItalic.booleanValue()) {
        m_italicSame.setSelection(false);
        m_italicSet.setSelection(true);
        m_italicClear.setSelection(false);
      } else {
        m_italicSame.setSelection(false);
        m_italicSet.setSelection(false);
        m_italicClear.setSelection(true);
      }
      // size
      if (font.m_deltaSize != null) {
        m_relativeButton.setSelection(true);
        m_absoluteButton.setSelection(false);
        m_relativeSpinner.setEnabled(true);
        m_absoluteSpinner.setEnabled(false);
        m_relativeSpinner.setSelection(font.m_deltaSize.intValue());
        m_absoluteSpinner.setSelection(font.getFont().getSize());
      } else if (font.m_newSize != null) {
        m_relativeButton.setSelection(false);
        m_absoluteButton.setSelection(true);
        m_relativeSpinner.setEnabled(false);
        m_absoluteSpinner.setEnabled(true);
        m_relativeSpinner.setSelection(font.getFont().getSize() - m_baseFont.getSize());
        m_absoluteSpinner.setSelection(font.getFont().getSize());
      } else {
        m_relativeButton.setSelection(true);
        m_absoluteButton.setSelection(false);
        m_relativeSpinner.setEnabled(true);
        m_absoluteSpinner.setEnabled(false);
        m_relativeSpinner.setSelection(0);
        m_absoluteSpinner.setSelection(font.getFont().getSize());
      }
      // yes, we know this FontInfo
      return true;
    } else {
      if (_font != null) {
        m_familyCombo.select(0);
        //
        m_boldSame.setSelection(true);
        m_italicSame.setSelection(true);
        //
        m_relativeButton.setSelection(true);
        m_relativeSpinner.setEnabled(true);
        m_absoluteSpinner.setEnabled(false);
        //
        m_relativeSpinner.setSelection(0);
        if (_font.getFont() != null) {
          m_absoluteSpinner.setSelection(_font.getFont().getSize());
        }
      }
      // no, we don't know this FontInfo
      return false;
    }
  }
}

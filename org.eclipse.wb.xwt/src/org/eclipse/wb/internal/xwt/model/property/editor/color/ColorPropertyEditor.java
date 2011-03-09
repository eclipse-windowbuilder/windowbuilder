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
package org.eclipse.wb.internal.xwt.model.property.editor.color;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;
import org.eclipse.wb.internal.core.xml.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link PropertyEditor} for {@link Color}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.property.editor
 */
public final class ColorPropertyEditor extends PropertyEditor implements IClipboardSourceProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new ColorPropertyEditor();

  private ColorPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int SAMPLE_SIZE = 10;
  private static final int SAMPLE_MARGIN = 3;
  private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {
    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      openDialog(property);
    }
  };

  @Override
  public PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  @Override
  public void paint(Property property, GC gc, int x, int y, int width, int height) throws Exception {
    Object value = property.getValue();
    if (value instanceof Color) {
      Color color = (Color) value;
      // draw color sample
      {
        Color oldBackground = gc.getBackground();
        Color oldForeground = gc.getForeground();
        try {
          int width_c = SAMPLE_SIZE;
          int height_c = SAMPLE_SIZE;
          int x_c = x;
          int y_c = y + (height - height_c) / 2;
          // update rest bounds
          {
            int delta = SAMPLE_SIZE + SAMPLE_MARGIN;
            x += delta;
            width -= delta;
          }
          // fill
          {
            gc.setBackground(color);
            gc.fillRectangle(x_c, y_c, width_c, height_c);
          }
          // draw line
          gc.setForeground(IColorConstants.gray);
          gc.drawRectangle(x_c, y_c, width_c, height_c);
        } finally {
          gc.setBackground(oldBackground);
          gc.setForeground(oldForeground);
        }
      }
      // draw color text
      {
        String text = getText(property);
        if (text != null) {
          DrawUtils.drawStringCV(gc, text, x, y, width, height);
        }
      }
    }
  }

  /**
   * @return the text for current {@link Color} value.
   */
  private String getText(Property property) throws Exception {
    // use expression
    if (property instanceof GenericProperty) {
      if (property.isModified()) {
        return ((GenericProperty) property).getExpression();
      }
    }
    // use value
    Object value = property.getValue();
    if (value instanceof Color) {
      Color color = (Color) value;
      return ColorSupport.toString(color);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClipboardSourceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClipboardSource(GenericProperty property) throws Exception {
    return getText(property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ColorDialog m_colorDialog = new ColorDialog();

  @Override
  public boolean activate(PropertyTable propertyTable, Property property, Point location)
      throws Exception {
    // activate using keyboard
    if (location == null) {
      openDialog(property);
    }
    // don't activate
    return false;
  }

  /**
   * Opens editing dialog.
   */
  private void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    // set initial color
    {
      Object value = property.getValue();
      if (value instanceof Color) {
        Color color = (Color) value;
        m_colorDialog.setColorInfo(ColorSupport.createInfo(color));
      }
    }
    // open dialog
    if (m_colorDialog.open() == Window.OK) {
      ColorInfo colorInfo = m_colorDialog.getColorInfo();
      String expression = (String) colorInfo.getData();
      genericProperty.setExpression(expression, Property.UNKNOWN_VALUE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ColorDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class ColorDialog extends AbstractColorDialog {
    private static String m_lastPageTitle;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ColorDialog() {
      super(DesignerPlugin.getShell());
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Pages
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void addPages(Composite parent) {
      addPage("System colors", new SystemColorsPage(parent, SWT.NONE, this));
      addPage("Named colors", new NamedColorsPage(parent, SWT.NONE, this));
      selectPageByTitle(m_lastPageTitle);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Dialog
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void buttonPressed(int buttonId) {
      // save page selection
      if (buttonId == IDialogConstants.OK_ID) {
        m_lastPageTitle = getSelectedPageTitle();
      }
      // super handle
      super.buttonPressed(buttonId);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // System Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class SystemColorsPage extends AbstractColorsGridComposite {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SystemColorsPage(Composite parent, int style, AbstractColorDialog colorDialog) {
      super(parent, style, colorDialog);
      {
        ColorsGridComposite colorsGrid =
            createColorsGroup(this, null, ColorSupport.getSystemColors());
        colorsGrid.showNames(50);
        colorsGrid.setCellHeight(25);
        colorsGrid.setColumns(2);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Named Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class NamedColorsPage extends AbstractColorsGridComposite {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public NamedColorsPage(Composite parent, int style, AbstractColorDialog colorDialog) {
      super(parent, style, colorDialog);
      {
        ColorsGridComposite colorsGrid =
            createColorsGroup(this, null, ColorSupport.getNamedColors());
        colorsGrid.showNames(50);
        colorsGrid.setCellHeight(25);
        colorsGrid.setColumns(6);
        colorsGrid.setMaxNameWidth(100);
      }
    }
  }
}
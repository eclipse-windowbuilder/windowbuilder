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
package org.eclipse.wb.internal.swing.model.property.editor.beans;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.customize.AwtComponentDialog;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

/**
 * Wrapper for common operations on {@link java.beans.PropertyEditor}.
 * 
 * @author lobas_av
 * @coverage swing.property.beans
 */
public final class PropertyEditorWrapper {
  private final java.beans.PropertyEditor m_propertyEditor;
  private DialogPresentation m_presentation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyEditorWrapper(java.beans.PropertyEditor propertyEditor) throws Exception {
    m_propertyEditor = propertyEditor;
    if (m_propertyEditor.supportsCustomEditor()) {
      m_presentation = new DialogPresentation(m_propertyEditor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void updatePropertyValue(Property property,
      java.beans.PropertyEditor propertyEditor) throws Exception {
    Object value = propertyEditor.getValue();
    String source = propertyEditor.getJavaInitializationString();
    GenericProperty genericProperty = (GenericProperty) property;
    genericProperty.setExpression(source, value);
  }

  /**
   * Configures {@link java.beans.PropertyEditor} to be used with given {@link Property}.
   */
  private void configure(Property property) throws Exception {
    if (property != null) {
      Method method_setSource =
          ReflectionUtils.getMethodBySignature(
              m_propertyEditor.getClass(),
              "setSource(java.lang.Object)");
      if (method_setSource != null) {
        Object source = ((GenericProperty) property).getJavaInfo().getObject();
        method_setSource.invoke(m_propertyEditor, source);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public java.beans.PropertyEditor getPropertyEditor() {
    return m_propertyEditor;
  }

  public String[] getTags(Property property) throws Exception {
    configure(property);
    return m_propertyEditor.getTags();
  }

  public String getText(Property property) throws Exception {
    configure(property);
    m_propertyEditor.setValue(property.getValue());
    return m_propertyEditor.getAsText();
  }

  public void setText(Property property, String text) throws Exception {
    configure(property);
    m_propertyEditor.setAsText(text);
    updatePropertyValue(property, m_propertyEditor);
  }

  public String getSource(Object value) throws Exception {
    m_propertyEditor.setValue(value);
    return m_propertyEditor.getJavaInitializationString();
  }

  public void openDialogEditor(PropertyTable propertyTable, Property property) throws Exception {
    configure(property);
    m_presentation.onClick(propertyTable, property);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertyEditorPresentation getPresentation() {
    return m_presentation;
  }

  public void paint(Property property, GC gc, int x, int y, int width, int height) throws Exception {
    configure(property);
    m_propertyEditor.setValue(property.getValue());
    // paint
    if (m_propertyEditor.isPaintable()) {
      Image image = paintValue(gc, width, height);
      gc.drawImage(image, x, y);
      image.dispose();
      return;
    }
    // text
    {
      String text = m_propertyEditor.getAsText();
      if (!StringUtils.isEmpty(text)) {
        DrawUtils.drawStringCV(gc, text, x, y, width, height);
      }
    }
  }

  private Image paintValue(GC gc, int width, int height) throws Exception {
    // create AWT graphics
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = (Graphics2D) image.getGraphics();
    // prepare color's
    Color background = gc.getBackground();
    Color foreground = gc.getForeground();
    // fill graphics
    graphics2D.setColor(SwingUtils.getAWTColor(background));
    graphics2D.fillRect(0, 0, width, height);
    // set color
    graphics2D.setBackground(SwingUtils.getAWTColor(background));
    graphics2D.setColor(SwingUtils.getAWTColor(foreground));
    // set font
    FontData[] fontData = gc.getFont().getFontData();
    String name = fontData.length > 0 ? fontData[0].getName() : "Arial";
    graphics2D.setFont(new java.awt.Font(name,
        java.awt.Font.PLAIN,
        graphics2D.getFont().getSize() - 1));
    // paint image
    m_propertyEditor.paintValue(graphics2D, new java.awt.Rectangle(0, 0, width, height));
    // conversion
    try {
      return SwingImageUtils.convertImage_AWT_to_SWT(image);
    } finally {
      image.flush();
      graphics2D.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class DialogPresentation extends ButtonPropertyEditorPresentation {
    private final java.beans.PropertyEditor m_dialogPropertyEditor;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DialogPresentation(java.beans.PropertyEditor propertyEditor) throws Exception {
      m_dialogPropertyEditor = propertyEditor.getClass().newInstance();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ButtonPropertyEditorPresentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
      m_dialogPropertyEditor.setValue(property.getValue());
      AwtComponentDialog dialog =
          new AwtComponentDialog(Activator.getDefault(),
              m_dialogPropertyEditor.getCustomEditor(),
              ModelMessages.PropertyEditorWrapper_awtComponentDialogTitle,
              "DialogPropertyEditor: " + m_dialogPropertyEditor.getClass().getName());
      if (dialog.open() == Window.OK) {
        updatePropertyValue(property, m_dialogPropertyEditor);
      }
    }
  }
}
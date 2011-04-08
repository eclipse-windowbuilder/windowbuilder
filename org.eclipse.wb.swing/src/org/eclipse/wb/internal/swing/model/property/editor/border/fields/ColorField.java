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
package org.eclipse.wb.internal.swing.model.property.editor.border.fields;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.property.editor.color.ColorPropertyEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.awt.Color;

/**
 * {@link AbstractBorderField} that allows to enter integer value.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ColorField extends AbstractBorderField {
  private final Canvas m_colorCanvas;
  private final Label m_colorLabel;
  private ColorInfo m_colorInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorField(Composite parent, String labelText) {
    super(parent, 3, labelText);
    {
      m_colorCanvas = new Canvas(this, SWT.BORDER);
      GridDataFactory.create(m_colorCanvas).hint(12, 12).alignVM();
    }
    {
      m_colorLabel = new Label(this, SWT.NONE);
      GridDataFactory.create(m_colorLabel).hintHC(18);
    }
    {
      ToolBar toolBar = new ToolBar(this, SWT.FLAT);
      {
        ToolItem selectItem = new ToolItem(toolBar, SWT.NONE);
        selectItem.setImage(Activator.getImage("borderEditor/selectColor.gif"));
        selectItem.setToolTipText(ModelMessages.ColorField_select);
        selectItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            ColorInfo newColorInfo = ColorPropertyEditor.external_editColor(m_colorInfo);
            getShell().setActive();
            if (newColorInfo != null) {
              onColorSelected(newColorInfo);
            }
          }
        });
      }
      {
        ToolItem defaultItem = new ToolItem(toolBar, SWT.NONE);
        defaultItem.setImage(Activator.getImage("borderEditor/clear.gif"));
        defaultItem.setToolTipText(ModelMessages.ColorField_reset);
        defaultItem.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            onColorSelected(null);
          }
        });
      }
    }
    showColor();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the value, that should correspond to the one of the field values.
   */
  public void setValue(Color color) throws Exception {
    if (color != null) {
      m_colorInfo = new ColorInfo(color.getRed(), color.getGreen(), color.getBlue());
    } else {
      m_colorInfo = null;
    }
    showColor();
  }

  @Override
  public String getSource() throws Exception {
    return m_colorInfo != null ? ColorPropertyEditor.external_getSource(m_colorInfo) : "null";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * User selected new {@link ColorInfo}.
   */
  private void onColorSelected(ColorInfo colorInfo) {
    m_colorInfo = colorInfo;
    showColor();
    notifyListeners(SWT.Selection, new Event());
  }

  /**
   * Shows current {@link #m_colorInfo} in {@link #m_colorCanvas} and {@link #m_colorLabel}.
   */
  private void showColor() {
    if (m_colorInfo != null) {
      m_colorCanvas.setBackground(new org.eclipse.swt.graphics.Color(null, m_colorInfo.getRGB()));
      m_colorLabel.setText("(" + m_colorInfo.getCommaRGB() + ")");
    } else {
      m_colorCanvas.setBackground(null);
      m_colorLabel.setText("(default)");
    }
  }
}

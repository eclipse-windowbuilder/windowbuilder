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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BooleanField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;

import org.eclipse.swt.widgets.Composite;

import java.awt.Color;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link LineBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class LineBorderComposite extends AbstractBorderComposite {
  private final ColorField m_colorField;
  private final IntegerField m_thicknessField;
  private final BooleanField m_typeField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LineBorderComposite(Composite parent) {
    super(parent, "LineBorder");
    GridLayoutFactory.create(this);
    m_colorField = createColorField(ModelMessages.LineBorderComposite_color);
    m_thicknessField = createIntegerField(ModelMessages.LineBorderComposite_thinkness);
    m_typeField =
        createBooleanField(ModelMessages.LineBorderComposite_corners, new String[]{
            ModelMessages.LineBorderComposite_cornersSquare,
            ModelMessages.LineBorderComposite_cornersRounded});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof LineBorder) {
      LineBorder ourBorder = (LineBorder) border;
      m_colorField.setValue(ourBorder.getLineColor());
      m_thicknessField.setValue(ourBorder.getThickness());
      m_typeField.setValue(ourBorder.getRoundedCorners());
      // OK, this is our Border
      return true;
    } else {
      m_colorField.setValue(Color.BLACK);
      m_thicknessField.setValue(1);
      m_typeField.setValue(false);
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
    String colorSource = m_colorField.getSource();
    String thinknessSource = m_thicknessField.getSource();
    String cornersSource = m_typeField.getSource();
    if ("false".equals(cornersSource)) {
      if ("1".equals(thinknessSource)) {
        return "new javax.swing.border.LineBorder(" + colorSource + ")";
      }
      return "new javax.swing.border.LineBorder(" + colorSource + ", " + thinknessSource + ")";
    }
    return "new javax.swing.border.LineBorder(" + colorSource + ", " + thinknessSource + ", true)";
  }
}

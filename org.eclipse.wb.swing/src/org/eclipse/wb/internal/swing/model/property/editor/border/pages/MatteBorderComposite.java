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
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.IntegerField;

import org.eclipse.swt.widgets.Composite;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link MatteBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class MatteBorderComposite extends AbstractBorderComposite {
  private final ColorField m_colorField;
  private final IntegerField m_topField;
  private final IntegerField m_leftField;
  private final IntegerField m_bottomField;
  private final IntegerField m_rightField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MatteBorderComposite(Composite parent) {
    super(parent, "MatteBorder");
    GridLayoutFactory.create(this);
    m_colorField = createColorField(ModelMessages.MatteBorderComposite_color);
    m_topField = createIntegerField(ModelMessages.MatteBorderComposite_top);
    m_leftField = createIntegerField(ModelMessages.MatteBorderComposite_left);
    m_bottomField = createIntegerField(ModelMessages.MatteBorderComposite_bottom);
    m_rightField = createIntegerField(ModelMessages.MatteBorderComposite_right);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof MatteBorder) {
      MatteBorder ourBorder = (MatteBorder) border;
      Insets borderInsets = ourBorder.getBorderInsets();
      m_colorField.setValue(ourBorder.getMatteColor());
      m_topField.setValue(borderInsets.top);
      m_leftField.setValue(borderInsets.left);
      m_bottomField.setValue(borderInsets.bottom);
      m_rightField.setValue(borderInsets.right);
      // OK, this is our Border
      return true;
    } else {
      m_colorField.setValue(Color.BLACK);
      m_topField.setValue(1);
      m_leftField.setValue(1);
      m_bottomField.setValue(1);
      m_rightField.setValue(1);
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
    return "new javax.swing.border.MatteBorder("
        + m_topField.getSource()
        + ", "
        + m_leftField.getSource()
        + ", "
        + m_bottomField.getSource()
        + ", "
        + m_rightField.getSource()
        + ", (java.awt.Color) "
        + m_colorField.getSource()
        + ")";
  }
}

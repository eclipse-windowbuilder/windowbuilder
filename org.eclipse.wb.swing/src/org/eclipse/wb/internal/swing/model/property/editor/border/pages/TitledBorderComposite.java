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

import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderDialog;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.BorderField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ComboField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.TextField;

import org.eclipse.swt.widgets.Composite;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link TitledBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TitledBorderComposite extends AbstractBorderComposite {
  private final TextField m_titleField;
  private final ComboField m_titleJustificationField;
  private final ComboField m_titlePositionField;
  private final ColorField m_titleColorField;
  private final BorderField m_borderField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TitledBorderComposite(Composite parent) {
    super(parent, "TitledBorder");
    GridLayoutFactory.create(this);
    m_titleField = createTextField("&Title:");
    m_titleJustificationField =
        createComboField("Title &justification:", TitledBorder.class, new String[]{
            "LEFT",
            "CENTER",
            "RIGHT",
            "LEADING",
            "TRAILING"}, new String[]{"Left", "Center", "Right", "Leading", "Trailing"});
    m_titlePositionField =
        createComboField("Title &position:", TitledBorder.class, new String[]{
            "ABOVE_TOP",
            "TOP",
            "BELOW_TOP",
            "ABOVE_BOTTOM",
            "BOTTOM",
            "BELOW_BOTTOM"}, new String[]{
            "Above Top",
            "Top",
            "Below Top",
            "Above Bottom",
            "Bottom",
            "Below Bottom"});
    m_titleColorField = createColorField("Title &color:");
    m_borderField = createBorderField("&Border:", "&Edit...");
    // configure layout
    GridDataFactory.create(m_titleField).fillH();
    GridDataFactory.create(m_titleJustificationField).fillH();
    GridDataFactory.create(m_titlePositionField).fillH();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize(BorderDialog borderDialog, AstEditor editor) {
    super.initialize(borderDialog, editor);
    m_borderField.setEditor(editor);
  }

  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof TitledBorder) {
      TitledBorder ourBorder = (TitledBorder) border;
      m_titleField.setValue(ourBorder.getTitle());
      m_titleJustificationField.setValue(ourBorder.getTitleJustification());
      m_titlePositionField.setValue(ourBorder.getTitlePosition());
      m_titleColorField.setValue(ourBorder.getTitleColor());
      m_borderField.setBorder(ourBorder.getBorder());
      /*m_colorField.setValue(ourBorder.getLineColor());
      m_thicknessField.setValue(ourBorder.getThickness());
      m_typeField.setValue(ourBorder.getRoundedCorners());*/
      // OK, this is our Border
      return true;
    } else {
      m_titleField.setValue("");
      m_titleJustificationField.setValue(TitledBorder.LEADING);
      m_titlePositionField.setValue(TitledBorder.TOP);
      m_titleColorField.setValue(null);
      m_borderField.setBorder(null);
      /*m_colorField.setValue(Color.BLACK);
      m_thicknessField.setValue(1);
      m_typeField.setValue(false);*/
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
    /*String colorSource = m_colorField.getSource();
    String thinknessSource = m_thicknessField.getSource();
    String cornersSource = m_typeField.getSource();
    if ("false".equals(cornersSource)) {
    	if ("1".equals(thinknessSource)) {
    		return "new javax.swing.border.LineBorder(" + colorSource + ")";
    	}
    	return "new javax.swing.border.LineBorder(" + colorSource + ", " + thinknessSource + ")";
    }
    return "new javax.swing.border.LineBorder(" + colorSource + ", " + thinknessSource + ", true)";*/
    String borderSource = m_borderField.getSource();
    String titleSource = m_titleField.getSource();
    String titleJustificationSource = m_titleJustificationField.getSource();
    String titlePositionSource = m_titlePositionField.getSource();
    String titleFontSource = "null";
    String titleColorSource = m_titleColorField.getSource();
    return "new javax.swing.border.TitledBorder("
        + borderSource
        + ", "
        + titleSource
        + ", "
        + titleJustificationSource
        + ", "
        + titlePositionSource
        + ", "
        + titleFontSource
        + ", "
        + titleColorSource
        + ")";
  }
}

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
import org.eclipse.wb.internal.swing.model.ModelMessages;
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
    m_titleField = createTextField(ModelMessages.TitledBorderComposite_title);
    m_titleJustificationField =
        createComboField(
            ModelMessages.TitledBorderComposite_titleJustification,
            TitledBorder.class,
            new String[]{"LEFT", "CENTER", "RIGHT", "LEADING", "TRAILING"},
            new String[]{
                ModelMessages.TitledBorderComposite_justLeft,
                ModelMessages.TitledBorderComposite_justCenter,
                ModelMessages.TitledBorderComposite_justRight,
                ModelMessages.TitledBorderComposite_justLeading,
                ModelMessages.TitledBorderComposite_justTrailing});
    m_titlePositionField =
        createComboField(
            ModelMessages.TitledBorderComposite_titlePosition,
            TitledBorder.class,
            new String[]{"ABOVE_TOP", "TOP", "BELOW_TOP", "ABOVE_BOTTOM", "BOTTOM", "BELOW_BOTTOM"},
            new String[]{
                ModelMessages.TitledBorderComposite_posAboveTop,
                ModelMessages.TitledBorderComposite_posTop,
                ModelMessages.TitledBorderComposite_posBelowTop,
                ModelMessages.TitledBorderComposite_posAboveBottom,
                ModelMessages.TitledBorderComposite_posBottom,
                ModelMessages.TitledBorderComposite_posBelowBottom});
    m_titleColorField = createColorField(ModelMessages.TitledBorderComposite_titleColor);
    m_borderField =
        createBorderField(
            ModelMessages.TitledBorderComposite_border,
            ModelMessages.TitledBorderComposite_borderEdit);
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
      // OK, this is our Border
      return true;
    } else {
      m_titleField.setValue("");
      m_titleJustificationField.setValue(TitledBorder.LEADING);
      m_titlePositionField.setValue(TitledBorder.TOP);
      m_titleColorField.setValue(null);
      m_borderField.setBorder(null);
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
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

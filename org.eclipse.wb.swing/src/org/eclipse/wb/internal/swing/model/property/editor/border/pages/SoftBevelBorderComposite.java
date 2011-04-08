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
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;

import org.eclipse.swt.widgets.Composite;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link SoftBevelBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class SoftBevelBorderComposite extends AbstractBorderComposite {
  private final RadioField m_typeField;
  private final ColorField m_highlightOuterField;
  private final ColorField m_highlightInnerField;
  private final ColorField m_shadowOuterField;
  private final ColorField m_shadowInnerField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SoftBevelBorderComposite(Composite parent) {
    super(parent, "SoftBevelBorder");
    GridLayoutFactory.create(this);
    m_typeField =
        createRadioField(
            ModelMessages.SoftBevelBorderComposite_bevelType,
            BevelBorder.class,
            new String[]{"LOWERED", "RAISED"},
            new String[]{
                ModelMessages.SoftBevelBorderComposite_bevelLowered,
                ModelMessages.SoftBevelBorderComposite_bevelRaised});
    m_highlightOuterField =
        createColorField(ModelMessages.SoftBevelBorderComposite_highlightOuterColor);
    m_highlightInnerField =
        createColorField(ModelMessages.SoftBevelBorderComposite_highlightInnerColor);
    m_shadowOuterField = createColorField(ModelMessages.SoftBevelBorderComposite_shadowOuterColor);
    m_shadowInnerField = createColorField(ModelMessages.SoftBevelBorderComposite_shadowInnerColor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof SoftBevelBorder) {
      SoftBevelBorder ourBorder = (SoftBevelBorder) border;
      m_typeField.setValue(ourBorder.getBevelType());
      m_highlightOuterField.setValue(ourBorder.getHighlightOuterColor());
      m_highlightInnerField.setValue(ourBorder.getHighlightInnerColor());
      m_shadowOuterField.setValue(ourBorder.getShadowOuterColor());
      m_shadowInnerField.setValue(ourBorder.getShadowInnerColor());
      // OK, this is our Border
      return true;
    } else {
      m_typeField.setValue(BevelBorder.LOWERED);
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
    String typeSource = m_typeField.getSource();
    String highlightOuterSource = m_highlightOuterField.getSource();
    String highlightInnerSource = m_highlightInnerField.getSource();
    String shadowOuterSource = m_shadowOuterField.getSource();
    String shadowInnerSource = m_shadowInnerField.getSource();
    if (highlightOuterSource == null
        && highlightInnerSource == null
        && shadowOuterSource == null
        && shadowInnerSource == null) {
      return "new javax.swing.border.SoftBevelBorder(" + typeSource + ")";
    }
    if (highlightOuterSource == null
        && highlightInnerSource != null
        && shadowOuterSource != null
        && shadowInnerSource == null) {
      return "new javax.swing.border.SoftBevelBorder("
          + typeSource
          + ", "
          + highlightInnerSource
          + ", "
          + shadowOuterSource
          + ")";
    }
    return "new javax.swing.border.SoftBevelBorder("
        + typeSource
        + ", "
        + highlightOuterSource
        + ", "
        + highlightInnerSource
        + ", "
        + shadowOuterSource
        + ", "
        + shadowInnerSource
        + ")";
  }
}

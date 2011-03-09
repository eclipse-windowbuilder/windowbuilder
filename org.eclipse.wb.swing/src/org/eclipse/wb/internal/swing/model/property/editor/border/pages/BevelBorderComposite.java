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
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.ColorField;
import org.eclipse.wb.internal.swing.model.property.editor.border.fields.RadioField;

import org.eclipse.swt.widgets.Composite;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link BevelBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class BevelBorderComposite extends AbstractBorderComposite {
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
  public BevelBorderComposite(Composite parent) {
    super(parent, "BevelBorder");
    GridLayoutFactory.create(this);
    m_typeField =
        createRadioField(
            "&Bevel type:",
            BevelBorder.class,
            new String[]{"LOWERED", "RAISED"},
            new String[]{"&lowered", "&raised"});
    m_highlightOuterField = createColorField("&Highlight outer color:");
    m_highlightInnerField = createColorField("&Highlight inner color:");
    m_shadowOuterField = createColorField("&Shadow outer color:");
    m_shadowInnerField = createColorField("&Shadow inner color:");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof BevelBorder) {
      BevelBorder ourBorder = (BevelBorder) border;
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
      return "new javax.swing.border.BevelBorder(" + typeSource + ")";
    }
    if (highlightOuterSource == null
        && highlightInnerSource != null
        && shadowOuterSource != null
        && shadowInnerSource == null) {
      return "new javax.swing.border.BevelBorder("
          + typeSource
          + ", "
          + highlightInnerSource
          + ", "
          + shadowOuterSource
          + ")";
    }
    return "new javax.swing.border.BevelBorder("
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

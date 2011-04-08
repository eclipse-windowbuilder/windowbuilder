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

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link EtchedBorder}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class EtchedBorderComposite extends AbstractBorderComposite {
  private final RadioField m_typeField;
  private final ColorField m_highlightField;
  private final ColorField m_shadowField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EtchedBorderComposite(Composite parent) {
    super(parent, "EtchedBorder");
    GridLayoutFactory.create(this);
    m_typeField =
        createRadioField(
            ModelMessages.EtchedBorderComposite_etchType,
            EtchedBorder.class,
            new String[]{"LOWERED", "RAISED"},
            new String[]{
                ModelMessages.EtchedBorderComposite_etchLowered,
                ModelMessages.EtchedBorderComposite_etchRaised});
    m_highlightField = createColorField(ModelMessages.EtchedBorderComposite_highlightColor);
    m_shadowField = createColorField(ModelMessages.EtchedBorderComposite_shadowColor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setBorder(Border border) throws Exception {
    if (border instanceof EtchedBorder) {
      EtchedBorder ourBorder = (EtchedBorder) border;
      m_typeField.setValue(ourBorder.getEtchType());
      m_highlightField.setValue(ourBorder.getHighlightColor());
      m_shadowField.setValue(ourBorder.getShadowColor());
      // OK, this is our Border
      return true;
    } else {
      m_typeField.setValue(EtchedBorder.LOWERED);
      // no, we don't know this Border
      return false;
    }
  }

  @Override
  public String getSource() throws Exception {
    String typeSource = m_typeField.getSource();
    String highlightSource = m_highlightField.getSource();
    String shadowSource = m_shadowField.getSource();
    if (highlightSource == null && shadowSource == null) {
      return "new javax.swing.border.EtchedBorder(" + typeSource + ")";
    }
    return "new javax.swing.border.EtchedBorder("
        + typeSource
        + ", "
        + highlightSource
        + ", "
        + shadowSource
        + ")";
  }
}

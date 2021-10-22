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
package org.eclipse.wb.internal.swing.FormLayout.model;

import com.jgoodies.forms.layout.RowSpec;

/**
 * Description for {@link RowSpec}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormRowInfo extends FormDimensionInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormRowInfo(RowSpec spec) throws Exception {
    super(spec);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the copy of this {@link FormRowInfo}.
   */
  @Override
  public FormRowInfo copy() throws Exception {
    return new FormRowInfo((RowSpec) getFormSpec());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Templates
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final FormDimensionTemplate[] TEMPLATES = new FormDimensionTemplate[]{
      new FormDimensionTemplate("DEFAULT_ROWSPEC", true, "default", "default.gif"),
      new FormDimensionTemplate("PREF_ROWSPEC", true, "preferred", "preferred.gif"),
      new FormDimensionTemplate("MIN_ROWSPEC", true, "minimum", "minimum.gif"),
      new FormDimensionTemplate("GLUE_ROWSPEC", true, "glue", "glue.gif"),
      new FormDimensionTemplate("LABEL_COMPONENT_GAP_ROWSPEC",
          false,
          "label component",
          "related_row.gif"),
      new FormDimensionTemplate("RELATED_GAP_ROWSPEC", false, "related gap", "related_row.gif"),
      new FormDimensionTemplate("UNRELATED_GAP_ROWSPEC",
          false,
          "unrelated gap",
          "unrelated_row.gif"),
      new FormDimensionTemplate("NARROW_LINE_GAP_ROWSPEC",
          false,
          "narrow line gap",
          "narrow_line.gif"),
      new FormDimensionTemplate("LINE_GAP_ROWSPEC", false, "line gap", "line.gif"),
      new FormDimensionTemplate("PARAGRAPH_GAP_ROWSPEC", false, "paragraph gap", "paragraph.gif")};

  @Override
  public FormDimensionTemplate[] getTemplates() {
    return TEMPLATES;
  }
}

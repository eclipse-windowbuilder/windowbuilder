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

import com.jgoodies.forms.layout.ColumnSpec;

/**
 * Description for {@link ColumnSpec}.
 * 
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public final class FormColumnInfo extends FormDimensionInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormColumnInfo(ColumnSpec spec) throws Exception {
    super(spec);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public FormColumnInfo copy() throws Exception {
    return new FormColumnInfo((ColumnSpec) getFormSpec());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Templates
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final FormDimensionTemplate[] TEMPLATES = new FormDimensionTemplate[]{
      new FormDimensionTemplate("DEFAULT_COLSPEC", true, "default", "default.gif"),
      new FormDimensionTemplate("PREF_COLSPEC", true, "preferred", "preferred.gif"),
      new FormDimensionTemplate("MIN_COLSPEC", true, "minimum", "minimum.gif"),
      new FormDimensionTemplate("BUTTON_COLSPEC", true, "button", "button.gif"),
      new FormDimensionTemplate("GROWING_BUTTON_COLSPEC",
          true,
          "growing button",
          "growing_button.gif"),
      new FormDimensionTemplate("GLUE_COLSPEC", false, "glue", "glue.gif"),
      new FormDimensionTemplate("RELATED_GAP_COLSPEC", false, "related gap", "related.gif"),
      new FormDimensionTemplate("UNRELATED_GAP_COLSPEC", false, "unrelated gap", "unrelated.gif"),
      new FormDimensionTemplate("LABEL_COMPONENT_GAP_COLSPEC",
          false,
          "label component gap",
          "label.gif"),};

  @Override
  public FormDimensionTemplate[] getTemplates() {
    return TEMPLATES;
  }
}

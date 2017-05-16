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
package org.eclipse.wb.internal.core.model.generation.preview;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link LazyVariableSupport} and
 * {@link LazyStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation.ui
 */
public final class GenerationPreviewLazy extends GenerationPreview {
  public static final GenerationPreview INSTANCE = new GenerationPreviewLazy();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenerationPreviewLazy() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenerationPreview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite) {
    int v_modifierIndex = variableComposite.getInteger(LazyVariableSupport.P_METHOD_MODIFIER);
    String v_modifierSource = LazyVariableSupport.V_MODIFIER_CODE[v_modifierIndex];
    String source = "";
    // declare fields
    source += "\tprivate JPanel panel;\n";
    source += "\tprivate JButton button;\n";
    // begin
    source += "\t...\n";
    // declare getPanel(), use getButton()
    {
      // open method
      source += "\t" + v_modifierSource + "JPanel getPanel() {\n";
      // code for panel
      source += "\t\tif (panel == null) {\n";
      source += "\t\t\tpanel = new JPanel();\n";
      source += "\t\t\tpanel.setBorder(new TitledBorder(\"Management\"));\n";
      // use getButton()
      source += "\t\t\tpanel.add(getButton());\n";
      // close method
      source += "\t\t}\n";
      source += "\t\treturn panel;\n";
      source += "\t}\n";
    }
    // separator
    source += "\t...\n";
    // declare getButton()
    {
      // open method
      source += "\t" + v_modifierSource + "JButton getButton() {\n";
      // code
      source += "\t\tif (button == null) {\n";
      source += "\t\t\tbutton = new JButton();\n";
      source += "\t\t\tbutton.setText(\"New button\");\n";
      source += "\t\t}\n";
      // close method
      source += "\t\treturn button;\n";
      source += "\t}\n";
    }
    // final result
    return source;
  }
}

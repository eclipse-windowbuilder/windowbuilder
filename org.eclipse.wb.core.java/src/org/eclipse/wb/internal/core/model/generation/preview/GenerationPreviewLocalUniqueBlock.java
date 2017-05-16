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
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link LocalUniqueVariableSupport} and
 * {@link BlockStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation.ui
 */
public final class GenerationPreviewLocalUniqueBlock extends GenerationPreview {
  public static final GenerationPreview INSTANCE = new GenerationPreviewLocalUniqueBlock();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenerationPreviewLocalUniqueBlock() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenerationPreview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite) {
    boolean v_useFinal = variableComposite.getBoolean(LocalUniqueVariableSupport.P_DECLARE_FINAL);
    String source = "";
    // begin
    source += "\t...\n";
    // parent
    {
      // open block
      source += "\t{\n";
      // declare parent
      source += "\t\t";
      if (v_useFinal) {
        source += "final ";
      }
      source += "JPanel panel = new JPanel();\n";
      // properties
      source += "\t\tpanel.setBorder(new TitledBorder(\"Management\"));\n";
      // child
      {
        // open block
        source += "\t\t{\n";
        // variable
        source += "\t\t\t";
        if (v_useFinal) {
          source += "final ";
        }
        source += "JButton button = new JButton();\n";
        // properties
        source += "\t\t\tbutton.setText(\"Add customer...\");\n";
        source += "\t\t\tpanel.add(button);\n";
        // close block
        source += "\t\t}\n";
        source += "\t\t...\n";
      }
      // close block
      source += "\t}\n";
    }
    // end
    source += "\t...\n";
    // final result
    return source;
  }
}

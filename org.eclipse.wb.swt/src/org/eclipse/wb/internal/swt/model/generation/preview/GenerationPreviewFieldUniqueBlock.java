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
package org.eclipse.wb.internal.swt.model.generation.preview;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGenerator;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;

/**
 * Implementation of {@link GenerationPreview} for {@link FieldUniqueVariableSupport} and
 * {@link FlatStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation.ui
 */
public final class GenerationPreviewFieldUniqueBlock extends GenerationPreview {
  public static final GenerationPreview INSTANCE = new GenerationPreviewFieldUniqueBlock();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GenerationPreviewFieldUniqueBlock() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GenerationPreview
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite) {
    boolean v_useThis = variableComposite.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS);
    int v_modifierIndex = variableComposite.getInteger(FieldUniqueVariableSupport.P_FIELD_MODIFIER);
    String v_modifierSource = FieldVariableSupport.V_MODIFIER_CODE[v_modifierIndex];
    //
    String source = "";
    String contentsRef = v_useThis ? "this.contents" : "contents";
    String buttonRef = v_useThis ? "this.button" : "button";
    // declare fields
    source += "\t" + v_modifierSource + "Composite contents;\n";
    source += "\t" + v_modifierSource + "Button button;\n";
    // begin
    source += "\t...\n";
    // parent
    {
      // open block
      source += "\t{\n";
      // assign field
      source += "\t\t" + contentsRef + " = new Composite(parent, SWT.NONE);\n";
      // properties
      source += "\t\t" + contentsRef + ".setLayout(new GridLayout(1, false));\n";
      // child
      {
        // open block
        source += "\t\t{\n";
        // assign field
        source += "\t\t\t" + buttonRef + " = new Button(" + contentsRef + ", SWT.NONE);\n";
        // properties
        source += "\t\t\t" + buttonRef + ".setText(\"New button\");\n";
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

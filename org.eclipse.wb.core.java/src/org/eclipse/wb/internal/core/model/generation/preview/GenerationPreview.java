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
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang.StringUtils;

/**
 * Implementations of this class provide preview for code generation using combination of
 * {@link VariableSupportDescription} and {@link StatementGeneratorDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation.ui
 */
public abstract class GenerationPreview {
  /**
   * @return the preview source using properties from given {@link GenerationPropertiesComposite}'s
   *         for variable and statement.
   */
  public abstract String getPreview(GenerationPropertiesComposite variableComposite,
      GenerationPropertiesComposite statementComposite);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link String} for joined given {@link String}'s using "\n".
   */
  protected static String getSource(String[] lines) {
    return getSource(new String[][]{lines});
  }

  /**
   * @return the {@link String} for joined given {@link String}'s using "\n".
   */
  protected static String getSource(String[][] lines2) {
    StringBuffer buffer = new StringBuffer();
    for (String[] lines : lines2) {
      if (lines == null) {
        continue;
      }
      //
      for (String line : lines) {
        // prepare count of leading spaces
        int spaceCount = 0;
        for (char c : line.toCharArray()) {
          if (c != ' ') {
            break;
          }
          spaceCount++;
        }
        // replace each two leading spaces with one \t
        Assert.isTrue(spaceCount % 2 == 0);
        line = StringUtils.repeat("\t", spaceCount / 2) + line.substring(spaceCount);
        // append line
        buffer.append(line);
        buffer.append("\n");
      }
    }
    return buffer.toString();
  }
}

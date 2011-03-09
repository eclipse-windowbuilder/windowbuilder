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
package org.eclipse.wb.tests.designer.ercp.model;

import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Tests for {@link GenerationSettings}.
 * 
 * @author scheglov_ke
 */
public class GenerationSettingsTest extends SwingModelTest {
  private static final GenerationSettings ERCP_SETTINGS =
      org.eclipse.wb.internal.ercp.ToolkitProvider.DESCRIPTION.getGenerationSettings();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for
   * {@link GenerationSettings#getPreview(VariableSupportDescription, StatementGeneratorDescription)}
   * for Swing and SWT.
   */
  public void test_getPreview() throws Exception {
    // test several eRCP preview's
    assertInstanceOf(
        org.eclipse.wb.internal.swt.model.generation.preview.GenerationPreviewLocalUniqueFlat.class,
        ERCP_SETTINGS.getPreview(
            LocalUniqueVariableDescription.INSTANCE,
            FlatStatementGeneratorDescription.INSTANCE));
    assertInstanceOf(
        org.eclipse.wb.internal.swt.model.generation.preview.GenerationPreviewLocalUniqueBlock.class,
        ERCP_SETTINGS.getPreview(
            LocalUniqueVariableDescription.INSTANCE,
            BlockStatementGeneratorDescription.INSTANCE));
  }
}

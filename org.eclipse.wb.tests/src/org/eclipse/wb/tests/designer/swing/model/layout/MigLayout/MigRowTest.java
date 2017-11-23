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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link MigRowInfo}.
 * 
 * @author scheglov_ke
 */
public class MigRowTest extends AbstractMigLayoutTest {
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
  public void test_alignments() throws Exception {
    checkAlignment(MigRowInfo.Alignment.DEFAULT, "Default");
    checkAlignment(MigRowInfo.Alignment.TOP, "Top");
    checkAlignment(MigRowInfo.Alignment.CENTER, "Center");
    checkAlignment(MigRowInfo.Alignment.BOTTOM, "Bottom");
    checkAlignment(MigRowInfo.Alignment.FILL, "Fill");
    checkAlignment(MigRowInfo.Alignment.BASELINE, "Baseline");
  }

  private static void checkAlignment(MigRowInfo.Alignment alignment, String expectedText) {
    assertNotNull(alignment.getSmallImage());
    assertNotNull(alignment.getMenuImage());
    assertEquals(expectedText, alignment.getText());
  }

  /**
   * Check for content of {@link MigRowInfo#ALIGNMENTS_TO_SET}.
   */
  public void test_alignmentsToSet() throws Exception {
    assertThat(MigRowInfo.ALIGNMENTS_TO_SET).hasSize(MigRowInfo.Alignment.values().length - 1);
    assertFalse(ArrayUtils.contains(MigRowInfo.ALIGNMENTS_TO_SET, MigRowInfo.Alignment.UNKNOWN));
  }
}

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

import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link MigColumnInfo}.
 * 
 * @author scheglov_ke
 */
public class MigColumnTest extends AbstractMigLayoutTest {
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
    checkAlignment(MigColumnInfo.Alignment.DEFAULT, "Default");
    checkAlignment(MigColumnInfo.Alignment.LEFT, "Left");
    checkAlignment(MigColumnInfo.Alignment.CENTER, "Center");
    checkAlignment(MigColumnInfo.Alignment.RIGHT, "Right");
    checkAlignment(MigColumnInfo.Alignment.FILL, "Fill");
    checkAlignment(MigColumnInfo.Alignment.LEADING, "Leading");
    checkAlignment(MigColumnInfo.Alignment.TRAILING, "Trailing");
  }

  private static void checkAlignment(MigColumnInfo.Alignment alignment, String expectedText) {
    assertNotNull(alignment.getSmallImage());
    assertNotNull(alignment.getMenuImage());
    assertEquals(expectedText, alignment.getText());
  }

  /**
   * Check for content of {@link MigColumnInfo#ALIGNMENTS_TO_SET}.
   */
  public void test_alignmentsToSet() throws Exception {
    assertThat(MigColumnInfo.ALIGNMENTS_TO_SET).hasSize(MigColumnInfo.Alignment.values().length - 1);
    assertFalse(ArrayUtils.contains(
        MigColumnInfo.ALIGNMENTS_TO_SET,
        MigColumnInfo.Alignment.UNKNOWN));
  }
}

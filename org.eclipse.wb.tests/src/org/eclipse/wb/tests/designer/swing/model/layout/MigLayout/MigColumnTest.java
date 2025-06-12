/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
	@Test
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
		assertNotNull(alignment.getSmallImageDescriptor());
		assertNotNull(alignment.getMenuImageDescriptor());
		assertEquals(expectedText, alignment.getText());
	}

	/**
	 * Check for content of {@link MigColumnInfo#ALIGNMENTS_TO_SET}.
	 */
	@Test
	public void test_alignmentsToSet() throws Exception {
		Assertions.assertThat(MigColumnInfo.ALIGNMENTS_TO_SET).hasSize(MigColumnInfo.Alignment.values().length - 1);
		assertFalse(ArrayUtils.contains(
				MigColumnInfo.ALIGNMENTS_TO_SET,
				MigColumnInfo.Alignment.UNKNOWN));
	}
}

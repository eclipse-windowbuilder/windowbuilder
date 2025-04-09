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

import org.eclipse.wb.internal.swing.MigLayout.model.MigRowInfo;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

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
	@Test
	public void test_alignments() throws Exception {
		checkAlignment(MigRowInfo.Alignment.DEFAULT, "Default");
		checkAlignment(MigRowInfo.Alignment.TOP, "Top");
		checkAlignment(MigRowInfo.Alignment.CENTER, "Center");
		checkAlignment(MigRowInfo.Alignment.BOTTOM, "Bottom");
		checkAlignment(MigRowInfo.Alignment.FILL, "Fill");
		checkAlignment(MigRowInfo.Alignment.BASELINE, "Baseline");
	}

	private static void checkAlignment(MigRowInfo.Alignment alignment, String expectedText) {
		assertNotNull(alignment.getSmallImageDescriptor());
		assertNotNull(alignment.getMenuImageDescriptor());
		assertEquals(expectedText, alignment.getText());
	}

	/**
	 * Check for content of {@link MigRowInfo#ALIGNMENTS_TO_SET}.
	 */
	@Test
	public void test_alignmentsToSet() throws Exception {
		Assertions.assertThat(MigRowInfo.ALIGNMENTS_TO_SET).hasSize(MigRowInfo.Alignment.values().length - 1);
		assertFalse(ArrayUtils.contains(MigRowInfo.ALIGNMENTS_TO_SET, MigRowInfo.Alignment.UNKNOWN));
	}
}

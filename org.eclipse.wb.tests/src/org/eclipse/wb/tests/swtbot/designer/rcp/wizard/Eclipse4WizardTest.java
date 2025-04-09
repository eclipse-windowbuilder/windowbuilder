/*******************************************************************************
 * Copyright (c) 2023, 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer.rcp.wizard;

import org.eclipse.wb.tests.swtbot.designer.AbstractWizardTest;

import org.junit.jupiter.api.Test;

public class Eclipse4WizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewViewPart() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "Eclipse 4", "ViewPart");
	}

	@Test
	public void testCreateNewViewPartNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "Eclipse 4", "ViewPart");
	}
}

/*******************************************************************************
 * Copyright (c) 2023, 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer.rcp.wizard;

import org.eclipse.wb.tests.swtbot.designer.AbstractWizardTest;

import org.junit.Test;

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

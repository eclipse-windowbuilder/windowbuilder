/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.wizard;

import org.eclipse.wb.internal.rcp.wizards.rcp.view.ViewPartWizard;

import org.junit.Test;

public class Eclipse4WizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewViewPart() throws Exception {
		openDesign(new ViewPartWizard(), m_packageFragment, "MyViewPart");
	}
}

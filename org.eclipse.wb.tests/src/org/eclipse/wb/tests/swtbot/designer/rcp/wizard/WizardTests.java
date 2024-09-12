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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * A collection of all SWTBot-based RCP Wizard tests.
 */
@RunWith(Suite.class)
@SuiteClasses({
		Eclipse4WizardTest.class,
		JFaceWizardTest.class,
		RcpWizardTest.class,
		SwtWizardTest.class
})
public class WizardTests {
}

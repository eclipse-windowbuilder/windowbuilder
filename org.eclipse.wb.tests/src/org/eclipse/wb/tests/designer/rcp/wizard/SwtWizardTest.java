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

import org.eclipse.wb.internal.rcp.wizards.swt.application.SwtApplicationWizard;
import org.eclipse.wb.internal.rcp.wizards.swt.composite.CompositeWizard;
import org.eclipse.wb.internal.rcp.wizards.swt.dialog.DialogWizard;
import org.eclipse.wb.internal.rcp.wizards.swt.shell.ShellWizard;

import org.junit.Test;

public class SwtWizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewApplicationWindow() throws Exception {
		openDesign(new SwtApplicationWizard(), m_packageFragment, "MyApplicationWindow");
	}

	@Test
	public void testCreateNewComposite() throws Exception {
		openDesign(new CompositeWizard(), m_packageFragment, "MyComposite");
	}

	@Test
	public void testCreateNewDialog() throws Exception {
		openDesign(new DialogWizard(), m_packageFragment, "MyDialog");
	}

	@Test
	public void testCreateNewShell() throws Exception {
		openDesign(new ShellWizard(), m_packageFragment, "MyShell");
	}
}

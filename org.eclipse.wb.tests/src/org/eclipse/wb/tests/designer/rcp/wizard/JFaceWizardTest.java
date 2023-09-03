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

import org.eclipse.wb.internal.rcp.wizards.jface.application.JFaceApplicationWizard;
import org.eclipse.wb.internal.rcp.wizards.jface.dialog.DialogWizard;
import org.eclipse.wb.internal.rcp.wizards.jface.dialog.TitleAreaDialogWizard;
import org.eclipse.wb.internal.rcp.wizards.jface.wizard.WizardPageWizard;
import org.eclipse.wb.internal.rcp.wizards.jface.wizard.WizardWizard;

import org.junit.Test;

public class JFaceWizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewApplicationWindow() throws Exception {
		openDesign(new JFaceApplicationWizard(), m_packageFragment, "MyApplicationWizard");
	}

	@Test
	public void testCreateNewDialog() throws Exception {
		openDesign(new DialogWizard(), m_packageFragment, "MyDialog");
	}

	@Test
	public void testCreateNewTitleAreaDialog() throws Exception {
		openDesign(new TitleAreaDialogWizard(), m_packageFragment, "MyTitleAreaDialog");
	}

	@Test
	public void testCreateNewWizard() throws Exception {
		// Graphical editing is not provided for Wizard classes
		assertThrows(Exception.class, () -> openDesign(new WizardWizard(), m_packageFragment, "MyWizard"));
	}

	@Test
	public void testCreateNewWizardPage() throws Exception {
		openDesign(new WizardPageWizard(), m_packageFragment, "MyWizardPage");
	}
}

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

import org.eclipse.wb.internal.rcp.wizards.rcp.advisor.ActionBarAdvisorWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.editor.EditorPartWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.editor.MultiPageEditorPartWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.pagebook.PageBookWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.perspective.PerspectiveWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.preference.PreferencePageWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.property.PropertyPageWizard;
import org.eclipse.wb.internal.rcp.wizards.rcp.view.ViewPartWizard;

import org.junit.Test;

public class RcpWizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewActionBarAdvisor() throws Exception {
		openDesign(new ActionBarAdvisorWizard(), m_packageFragment, "MyActionBarAdvisor");
	}

	@Test
	public void testCreateNewEditorPart() throws Exception {
		openDesign(new EditorPartWizard(), m_packageFragment, "MyEditorPart");
	}

	@Test
	public void testCreateNewMultiPageEditorPart() throws Exception {
		// Graphical editing is not provided for MultiPageEditorPart
		assertThrows(Exception.class,
				() -> openDesign(new MultiPageEditorPartWizard(), m_packageFragment, "MyMultiPageEditorPart"));
	}

	@Test
	public void testCreateNewPageBookViewPage() throws Exception {
		openDesign(new PageBookWizard(), m_packageFragment, "MyPageBookViewPage");
	}

	@Test
	public void testCreateNewPerspective() throws Exception {
		openDesign(new PerspectiveWizard(), m_packageFragment, "MyPerspective");
	}

	@Test
	public void testCreateNewPreferencePage() throws Exception {
		openDesign(new PreferencePageWizard(), m_packageFragment, "MyPreferencePage");
	}

	@Test
	public void testCreateNewPropertyPage() throws Exception {
		openDesign(new PropertyPageWizard(), m_packageFragment, "MyPropertyPage");
	}

	@Test
	public void testCreateNewViewPart() throws Exception {
		openDesign(new ViewPartWizard(), m_packageFragment, "MyViewPart");
	}
}

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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for RCP JFace models.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		TableViewerTest.class,
		TableViewerColumnTest.class,
		TreeViewerColumnTest.class,
		ComboViewerTest.class,
		AbstractColumnLayoutTest.class,
		WindowTopBoundsSupportTest.class,
		DialogTest.class,
		TitleAreaDialogTest.class,
		PopupDialogTest.class,
		DialogPageTest.class,
		ApplicationWindowTest.class,
		ApplicationWindowGefTest.class,
		ActionTest.class,
		MenuManagerTest.class,
		MenuManagerGefTest.class,
		CoolBarManagerTest.class,
		WizardPageTest.class,
		WizardTest.class,
		PreferencePageTest.class,
		FieldEditorPreferencePageTest.class,
		FieldEditorLabelsConstantsPropertyEditorTest.class,
		DoubleFieldEditorEntryInfoTest.class,
		FieldLayoutPreferencePageTest.class,
		ControlDecorationTest.class,
		FieldEditorPreferencePageGefTest.class,
		CellEditorTest.class,
		NoJFaceInClasspathTest.class,
		GridLayoutFactoryTest.class
})
public class JFaceTests {
}
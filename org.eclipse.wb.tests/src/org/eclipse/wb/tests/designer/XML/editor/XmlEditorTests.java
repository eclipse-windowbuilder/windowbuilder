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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * {@link AbstractXmlEditor} and related tests.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		// editor
		AbstractXmlEditorTest.class,
		EditorLayoutTest.class,
		UndoManagerTest.class,
		XmlDesignPageTest.class,
		DesignContextMenuProviderTest.class,
		XmlPropertiesToolBarContributorTest.class,
		SelectSupportTest.class,
		// actions
		EditorRelatedActionTest.class,
		SwitchActionTest.class,
		SwitchPairEditorActionTest.class,
		RefreshActionTest.class,
		TestActionTest.class,
		DeleteActionTest.class,
		CopyActionTest.class,
		CutActionTest.class,
		// policies
		TopSelectionEditPolicyTest.class,
		DirectTextPropertyEditPolicyTest.class
})
public class XmlEditorTests {
}
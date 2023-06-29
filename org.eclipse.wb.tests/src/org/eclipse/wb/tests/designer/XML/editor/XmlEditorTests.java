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
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * {@link AbstractXmlEditor} and related tests.
 *
 * @author scheglov_ke
 */
public class XmlEditorTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.xml.editor");
		// editor
		suite.addTest(createSingleSuite(AbstractXmlEditorTest.class));
		suite.addTest(createSingleSuite(EditorLayoutTest.class));
		suite.addTest(createSingleSuite(UndoManagerTest.class));
		suite.addTest(createSingleSuite(XmlDesignPageTest.class));
		suite.addTest(createSingleSuite(DesignContextMenuProviderTest.class));
		suite.addTest(createSingleSuite(XmlPropertiesToolBarContributorTest.class));
		suite.addTest(createSingleSuite(SelectSupportTest.class));
		// actions
		suite.addTest(createSingleSuite(EditorRelatedActionTest.class));
		suite.addTest(createSingleSuite(SwitchActionTest.class));
		suite.addTest(createSingleSuite(SwitchPairEditorActionTest.class));
		suite.addTest(createSingleSuite(RefreshActionTest.class));
		suite.addTest(createSingleSuite(TestActionTest.class));
		suite.addTest(createSingleSuite(DeleteActionTest.class));
		suite.addTest(createSingleSuite(CopyActionTest.class));
		suite.addTest(createSingleSuite(CutActionTest.class));
		// policies
		suite.addTest(createSingleSuite(TopSelectionEditPolicyTest.class));
		suite.addTest(createSingleSuite(DirectTextPropertyEditPolicyTest.class));
		return suite;
	}
}
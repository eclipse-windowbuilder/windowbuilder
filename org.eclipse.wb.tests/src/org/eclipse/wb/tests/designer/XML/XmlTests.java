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
package org.eclipse.wb.tests.designer.XML;

import org.eclipse.wb.tests.designer.XML.editor.XmlEditorTests;
import org.eclipse.wb.tests.designer.XML.gef.GefTests;
import org.eclipse.wb.tests.designer.XML.model.ModelTests;
import org.eclipse.wb.tests.designer.XML.palette.PaletteTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All XML tests.
 *
 * @author scheglov_ke
 */
public class XmlTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.xml");
		suite.addTest(org.eclipse.wb.tests.designer.core.util.xml.XmlTests.suite());
		suite.addTest(createSingleSuite(ActivatorTest.class));
		suite.addTest(createSingleSuite(ClassLoadingTest.class));
		suite.addTest(ModelTests.suite());
		suite.addTest(XmlEditorTests.suite());
		suite.addTest(GefTests.suite());
		suite.addTest(PaletteTests.suite());
		return suite;
	}
}

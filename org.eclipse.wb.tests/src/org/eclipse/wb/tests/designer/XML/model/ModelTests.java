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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.tests.designer.XML.model.association.AssociationTests;
import org.eclipse.wb.tests.designer.XML.model.description.DescriptionTests;
import org.eclipse.wb.tests.designer.XML.model.generic.GenericTests;
import org.eclipse.wb.tests.designer.XML.model.property.PropertyTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * XML model tests.
 *
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.xml.model");
		suite.addTest(DescriptionTests.suite());
		suite.addTest(createSingleSuite(EditorWarningTest.class));
		suite.addTest(createSingleSuite(XmlObjectTest.class));
		suite.addTest(createSingleSuite(NamespacesHelperTest.class));
		suite.addTest(createSingleSuite(XmlObjectUtilsTest.class));
		suite.addTest(createSingleSuite(ElementCreationSupportTest.class));
		suite.addTest(createSingleSuite(TagCreationSupportTest.class));
		suite.addTest(AssociationTests.suite());
		suite.addTest(PropertyTests.suite());
		suite.addTest(createSingleSuite(XmlObjectRootProcessorTest.class));
		suite.addTest(createSingleSuite(TopBoundsSupportTest.class));
		suite.addTest(createSingleSuite(ClipboardTest.class));
		suite.addTest(createSingleSuite(AbstractComponentTest.class));
		suite.addTest(GenericTests.suite());
		suite.addTest(createSingleSuite(MorphingSupportTest.class));
		return suite;
	}
}
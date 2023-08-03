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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * XML model tests.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		DescriptionTests.class,
		EditorWarningTest.class,
		XmlObjectTest.class,
		NamespacesHelperTest.class,
		XmlObjectUtilsTest.class,
		ElementCreationSupportTest.class,
		TagCreationSupportTest.class,
		AssociationTests.class,
		PropertyTests.class,
		XmlObjectRootProcessorTest.class,
		TopBoundsSupportTest.class,
		ClipboardTest.class,
		AbstractComponentTest.class,
		GenericTests.class,
		MorphingSupportTest.class
})
public class ModelTests {
}
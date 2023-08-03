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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.tests.designer.core.model.association.AssociationTests;
import org.eclipse.wb.tests.designer.core.model.description.DescriptionTests;
import org.eclipse.wb.tests.designer.core.model.generic.GenericTests;
import org.eclipse.wb.tests.designer.core.model.nonvisual.NonVisualBeansGefTest;
import org.eclipse.wb.tests.designer.core.model.nonvisual.NonVisualBeansTest;
import org.eclipse.wb.tests.designer.core.model.operations.AddTest;
import org.eclipse.wb.tests.designer.core.model.operations.DeleteTest;
import org.eclipse.wb.tests.designer.core.model.parser.ParserTests;
import org.eclipse.wb.tests.designer.core.model.property.PropertiesTests;
import org.eclipse.wb.tests.designer.core.model.util.UtilTests;
import org.eclipse.wb.tests.designer.core.model.variables.VariablesTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ObjectInfoTest.class,
		DefaultObjectPresentationTest.class,
		ObjectReferenceInfoTest.class,
		ClassLoadingTest.class,
		DescriptionTests.class,
		JavaInfoTest.class,
		DefaultJavaInfoPresentationTest.class,
		AbstractComponentTest.class,
		UtilTests.class,
		AssociationTests.class,
		AddTest.class,
		DeleteTest.class,
		PropertiesTests.class,
		ParserTests.class,
		VariablesTests.class,
		GenericTests.class,
		NonVisualBeansTest.class,
		NonVisualBeansGefTest.class,
		ArrayObjectTest.class,
		WrapperInfoTest.class,
		EllipsisObjectInfoTest.class
})
public class ModelTests {
}

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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.tests.designer.core.model.util.generic.GenericTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ExposeComponentSupportTest.class,
		FactoryActionsTests.class,
		JavaInfoUtilsTest.class,
		ObjectInfoUtilsTest.class,
		GenericTypeResolverJavaInfoTest.class,
		TemplateUtilsTest.class,
		ScriptUtilsTest.class,
		MethodOrderTest.class,
		ComponentOrderTest.class,
		MorphingSupportTest.class,
		ObjectsLabelProviderTest.class,
		ObjectsTreeContentProviderTest.class,
		RenameConvertSupportTest.class,
		PredicatesTest.class,
		StackContainerSupportTest.class,
		GenericTests.class,
		PropertyUtilsTest.class
})
public class UtilTests {
}

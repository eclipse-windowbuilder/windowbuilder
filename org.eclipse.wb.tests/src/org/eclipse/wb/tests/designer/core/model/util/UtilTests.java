/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.tests.designer.core.model.util.generic.GenericTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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

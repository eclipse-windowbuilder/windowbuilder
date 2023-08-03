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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link ComponentDescription}, {@link ComponentDescriptionHelper}, etc.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ToolkitDescriptionTest.class,
		LayoutDescriptionTest.class,
		DescriptionProcessorTest.class,
		ComponentDescriptionKeyTest.class,
		ComponentDescriptionTest.class,
		ComponentDescriptionIbmTest.class,
		CreationDescriptionTest.class,
		CreationDescriptionLoadingTest.class,
		MorphingTargetDescriptionTest.class,
		DescriptionVersionsProvidersTest.class,
		ComponentDescriptionHelperTest.class,
		GenericPropertyDescriptionTest.class,
		BeanPropertyTagsTest.class,
		MethodSinglePropertyRuleTest.class,
		MethodPropertyRuleTest.class
})
public class DescriptionTests {
}

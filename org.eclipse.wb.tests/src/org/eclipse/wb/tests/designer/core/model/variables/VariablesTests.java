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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.internal.core.model.variable.VariableSupport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link VariableSupport}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		AbstractVariableSupportTest.class,
		ThisTest.class,
		ThisForcedMethodTest.class,
		AbstractNamedTest.class,
		AbstractSimpleTest.class,
		LocalUniqueTest.class,
		LocalReuseTest.class,
		FieldUniqueTest.class,
		FieldInitializerTest.class,
		FieldReuseTest.class,
		EmptyTest.class,
		EmptyPureTest.class,
		EmptyInvocationTest.class,
		VoidInvocationTest.class,
		ExposedPropertyTest.class,
		ExposedFieldTest.class,
		LazyTest.class,
		MethodParameterTest.class,
		NamesManagerTest.class,
		TextPropertyRenameTest.class
})
public class VariablesTests {
}

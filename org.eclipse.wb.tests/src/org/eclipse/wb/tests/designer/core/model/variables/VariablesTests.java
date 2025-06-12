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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.internal.core.model.variable.VariableSupport;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link VariableSupport}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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

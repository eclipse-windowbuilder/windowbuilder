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
package org.eclipse.wb.tests.designer.core.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for core {@link ExpressionAccessor}'s.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		AccessorUtilsTest.class,
		FieldAccessorTest.class,
		SetterAccessorTest.class,
		ConstructorAccessorTest.class,
		SuperConstructorAccessorTest.class,
		FactoryAccessorTest.class,
		InvocationChildAssociationAccessorTest.class,
		MethodInvocationAccessorTest.class,
		MethodInvocationArgumentAccessorTest.class
})
public class AccessorsTests {
}

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
package org.eclipse.wb.tests.designer.core.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for core {@link ExpressionAccessor}'s.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
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

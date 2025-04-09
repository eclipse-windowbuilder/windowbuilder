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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.Association;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link Association}'s.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		// constructor
		ConstructorParentAssociationTest.class,
		ConstructorChildAssociationTest.class,
		// invocation
		InvocationChildAssociationTest.class,
		InvocationVoidAssociationTest.class,
		InvocationSecondaryAssociationTest.class,
		FactoryParentAssociationTest.class,
		// other
		RootAssociationTest.class,
		EmptyAssociationTest.class,
		UnknownAssociationTest.class,
		SuperConstructorArgumentAssociationTest.class,
		ImplicitObjectAssociationTest.class,
		ImplicitFactoryArgumentAssociationTest.class,
		CompoundAssociationTest.class,
		// object/factory
		AssociationObjectsTest.class,
		AssociationObjectFactoriesTest.class
})
public class AssociationTests {
}

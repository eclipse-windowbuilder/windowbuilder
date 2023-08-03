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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link FactoryDescriptionHelper} and static/instance factories.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		// creations
		ThisCreationSupportTest.class, ConstructorCreationSupportTest.class, ExposedPropertyCreationSupportTest.class,
		ExposedFieldCreationSupportTest.class, SuperInvocationCreationSupportTest.class,
		// factories
		FactoriesTests.class,
		// other
		InvocationChainCreationSupportTest.class, ICreationSupportPermissionsTest.class,
		OpaqueCreationSupportTest.class })
public class CreationsTests {
}

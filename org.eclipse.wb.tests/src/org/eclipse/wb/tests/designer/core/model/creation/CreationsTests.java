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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link FactoryDescriptionHelper} and static/instance factories.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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

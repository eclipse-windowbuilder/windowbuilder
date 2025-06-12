/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link ILayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		ModelClassLayoutRequestValidatorTest.class,
		ComponentClassLayoutRequestValidatorTest.class,
		CompatibleLayoutRequestValidatorTest.class,
		BorderOfChildLayoutRequestValidatorTest.class,
		LayoutRequestValidatorsTest.class
})
public class LayoutRequestValidatorTests {
}

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
package org.eclipse.wb.tests.designer.editor.validator;

import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link ILayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ModelClassLayoutRequestValidatorTest.class,
		ComponentClassLayoutRequestValidatorTest.class,
		CompatibleLayoutRequestValidatorTest.class,
		BorderOfChildLayoutRequestValidatorTest.class,
		BorderTransparentLayoutRequestValidatorTest.class,
		LayoutRequestValidatorsTest.class
})
public class LayoutRequestValidatorTests {
}

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
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link ILayoutRequestValidator}.
 *
 * @author scheglov_ke
 */
public class LayoutRequestValidatorTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.editor.validator");
    suite.addTest(createSingleSuite(ModelClassLayoutRequestValidatorTest.class));
    suite.addTest(createSingleSuite(ComponentClassLayoutRequestValidatorTest.class));
    suite.addTest(createSingleSuite(CompatibleLayoutRequestValidatorTest.class));
    suite.addTest(createSingleSuite(BorderOfChildLayoutRequestValidatorTest.class));
    suite.addTest(createSingleSuite(BorderTransparentLayoutRequestValidatorTest.class));
    suite.addTest(createSingleSuite(LayoutRequestValidatorsTest.class));
    return suite;
  }
}

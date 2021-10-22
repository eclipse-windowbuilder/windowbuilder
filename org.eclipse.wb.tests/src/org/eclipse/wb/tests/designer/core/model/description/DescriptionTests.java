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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link ComponentDescription}, {@link ComponentDescriptionHelper}, etc.
 *
 * @author scheglov_ke
 */
public class DescriptionTests extends DesignerSuiteTests {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Suite
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.model.description");
    suite.addTest(createSingleSuite(ToolkitDescriptionTest.class));
    suite.addTest(createSingleSuite(LayoutDescriptionTest.class));
    suite.addTest(createSingleSuite(DescriptionProcessorTest.class));
    suite.addTest(createSingleSuite(ComponentDescriptionKeyTest.class));
    suite.addTest(createSingleSuite(ComponentDescriptionTest.class));
    suite.addTest(createSingleSuite(ComponentDescriptionIbmTest.class));
    suite.addTest(createSingleSuite(CreationDescriptionTest.class));
    suite.addTest(createSingleSuite(CreationDescriptionLoadingTest.class));
    suite.addTest(createSingleSuite(MorphingTargetDescriptionTest.class));
    suite.addTest(createSingleSuite(DescriptionVersionsProvidersTest.class));
    suite.addTest(createSingleSuite(ComponentDescriptionHelperTest.class));
    suite.addTest(createSingleSuite(GenericPropertyDescriptionTest.class));
    suite.addTest(createSingleSuite(BeanPropertyTagsTest.class));
    suite.addTest(createSingleSuite(MethodSinglePropertyRuleTest.class));
    suite.addTest(createSingleSuite(MethodPropertyRuleTest.class));
    return suite;
  }
}

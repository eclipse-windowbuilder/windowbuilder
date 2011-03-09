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
package org.eclipse.wb.tests.designer.XML.model.description;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * XML model tests.
 * 
 * @author scheglov_ke
 */
public class DescriptionTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xml.model.description");
    suite.addTest(createSingleSuite(ExpressionConverterTest.class));
    suite.addTest(createSingleSuite(EmptyExpressionAccessorTest.class));
    suite.addTest(createSingleSuite(MethodExpressionAccessorTest.class));
    suite.addTest(createSingleSuite(FieldExpressionAccessorTest.class));
    suite.addTest(createSingleSuite(ComponentDescriptionHelperTest.class));
    suite.addTest(createSingleSuite(GenericPropertyDescriptionTest.class));
    return suite;
  }
}
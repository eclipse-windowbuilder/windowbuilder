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
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for core {@link ExpressionAccessor}'s.
 *
 * @author scheglov_ke
 */
public class AccessorsTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.model.property.accessor");
    suite.addTest(createSingleSuite(AccessorUtilsTest.class));
    suite.addTest(createSingleSuite(FieldAccessorTest.class));
    suite.addTest(createSingleSuite(SetterAccessorTest.class));
    suite.addTest(createSingleSuite(ConstructorAccessorTest.class));
    suite.addTest(createSingleSuite(SuperConstructorAccessorTest.class));
    suite.addTest(createSingleSuite(FactoryAccessorTest.class));
    suite.addTest(createSingleSuite(InvocationChildAssociationAccessorTest.class));
    suite.addTest(createSingleSuite(MethodInvocationAccessorTest.class));
    suite.addTest(createSingleSuite(MethodInvocationArgumentAccessorTest.class));
    return suite;
  }
}

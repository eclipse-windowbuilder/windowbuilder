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
package org.eclipse.wb.tests.designer.core.model.association;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link Association}'s.
 *
 * @author scheglov_ke
 */
public class AssociationTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.model.association");
    // constructor
    suite.addTest(createSingleSuite(ConstructorParentAssociationTest.class));
    suite.addTest(createSingleSuite(ConstructorChildAssociationTest.class));
    // invocation
    suite.addTest(createSingleSuite(InvocationChildAssociationTest.class));
    suite.addTest(createSingleSuite(InvocationVoidAssociationTest.class));
    suite.addTest(createSingleSuite(InvocationSecondaryAssociationTest.class));
    suite.addTest(createSingleSuite(FactoryParentAssociationTest.class));
    // other
    suite.addTest(createSingleSuite(RootAssociationTest.class));
    suite.addTest(createSingleSuite(EmptyAssociationTest.class));
    suite.addTest(createSingleSuite(UnknownAssociationTest.class));
    suite.addTest(createSingleSuite(SuperConstructorArgumentAssociationTest.class));
    suite.addTest(createSingleSuite(ImplicitObjectAssociationTest.class));
    suite.addTest(createSingleSuite(ImplicitFactoryArgumentAssociationTest.class));
    suite.addTest(createSingleSuite(CompoundAssociationTest.class));
    // object/factory
    suite.addTest(createSingleSuite(AssociationObjectsTest.class));
    suite.addTest(createSingleSuite(AssociationObjectFactoriesTest.class));
    return suite;
  }
}

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
package org.eclipse.wb.tests.designer.XML.model.association;

import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * XML {@link Association} tests.
 *
 * @author scheglov_ke
 */
public class AssociationTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xml.model.association");
    suite.addTest(createSingleSuite(DirectAssociationTest.class));
    suite.addTest(createSingleSuite(OrderAssociationTest.class));
    suite.addTest(createSingleSuite(PropertyAssociationTest.class));
    suite.addTest(createSingleSuite(IntermediateAssociationTest.class));
    return suite;
  }
}
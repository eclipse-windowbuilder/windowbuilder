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
package org.eclipse.wb.tests.designer.rcp.model.layout.form;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import org.eclipse.swt.layout.FormLayout;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link FormLayout}.
 *
 * @author scheglov_ke
 * @author mitin_aa
 */
public class FormLayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.layout.FormLayout");
    suite.addTest(createSingleSuite(FormLayoutMoveSingleResizableTest.class));
    suite.addTest(createSingleSuite(FormLayoutMoveSingleWithSingleSideTest.class));
    suite.addTest(createSingleSuite(FormLayoutMoveSingleWithBothSidesTest.class));
    suite.addTest(createSingleSuite(FormLayoutAlignmentDetectionTest.class));
    suite.addTest(createSingleSuite(FormLayoutModelsTest.class));
    /*suite.addTest(createSingleSuite(FormLayoutMoveTest.class));
    suite.addTest(createSingleSuite(FormLayout_Alignment_Test.class));*/
    return suite;
  }
}
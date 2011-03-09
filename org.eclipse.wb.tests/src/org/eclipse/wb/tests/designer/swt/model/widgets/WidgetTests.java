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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for SWT widgets models.
 * 
 * @author lobas_av
 */
public class WidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swt.model.widgets");
    suite.addTest(createSingleSuite(WidgetTest.class));
    suite.addTest(createSingleSuite(LiveComponentsManagerTest.class));
    suite.addTest(createSingleSuite(DescriptionProcessorTest.class));
    suite.addTest(createSingleSuite(ControlTest.class));
    suite.addTest(createSingleSuite(LiveImagesManagerTest.class));
    suite.addTest(createSingleSuite(ScrollableTest.class));
    suite.addTest(createSingleSuite(CompositeTopBoundsSupportTest.class));
    suite.addTest(createSingleSuite(CompositeTopBoundsTest.class));
    suite.addTest(createSingleSuite(CompositeTest.class));
    suite.addTest(createSingleSuite(TableTest.class));
    suite.addTest(createSingleSuite(TreeTest.class));
    suite.addTest(createSingleSuite(ThisCompositeTest.class));
    suite.addTest(createSingleSuite(ButtonsTest.class));
    suite.addTest(createSingleSuite(StaticFactoryTest.class));
    suite.addTest(createSingleSuite(InstanceFactoryTest.class));
    return suite;
  }
}
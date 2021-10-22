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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Swing properties.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public class PropertiesTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swing.model.property");
    suite.addTest(createSingleSuite(FontPropertyEditorTest.class));
    suite.addTest(createSingleSuite(IconPropertyEditorTest.class));
    suite.addTest(createSingleSuite(ImagePropertyEditorTest.class));
    suite.addTest(createSingleSuite(BorderPropertyEditorTest.class));
    suite.addTest(createSingleSuite(TabOrderPropertyTest.class));
    suite.addTest(createSingleSuite(TabOrderPropertyValueTest.class));
    suite.addTest(createSingleSuite(BeanPropertyEditorTest.class));
    return suite;
  }
}
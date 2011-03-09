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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for SWT properties.
 * 
 * @author lobas_av
 * @author scheglov_ke
 */
public class PropertiesTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swt.model.property");
    suite.addTest(createSingleSuite(ConvertersTest.class));
    suite.addTest(createSingleSuite(ColorPropertyEditorTestNoManager.class));
    suite.addTest(createSingleSuite(ColorPropertyEditorTestWithManager.class));
    suite.addTest(createSingleSuite(ColorPropertyEditorTestRegistry.class));
    suite.addTest(createSingleSuite(ImagePropertyEditorTestNoManager.class));
    suite.addTest(createSingleSuite(ImagePropertyEditorTestWithManager.class));
    suite.addTest(createSingleSuite(ImagePropertyEditorTestPlugin.class));
    suite.addTest(createSingleSuite(ImageDescriptorPropertyEditorTestNoManager.class));
    suite.addTest(createSingleSuite(ImageDescriptorPropertyEditorTestWithManager.class));
    suite.addTest(createSingleSuite(ImageDescriptorPropertyEditorTestPlugin.class));
    suite.addTest(createSingleSuite(FontPropertyEditorTestNoManager.class));
    suite.addTest(createSingleSuite(FontPropertyEditorTestWithManager.class));
    suite.addTest(createSingleSuite(FontPropertyEditorTestRegistry.class));
    suite.addTest(createSingleSuite(ResourceRegistryTest.class));
    suite.addTest(createSingleSuite(SWTResourceManagerTest.class));
    suite.addTest(createSingleSuite(ResourceManagerTest.class));
    suite.addTest(createSingleSuite(TabOrderPropertyTest.class));
    return suite;
  }
}
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
package org.eclipse.wb.tests.designer.XWT.model.property;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for XWT properties and {@link PropertyEditor}s.
 *
 * @author scheglov_ke
 */
public class PropertyTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model.property");
    suite.addTest(createSingleSuite(XwtStyleClassResolverTest.class));
    suite.addTest(createSingleSuite(StylePropertyEditorTest.class));
    suite.addTest(createSingleSuite(ColorPropertyEditorTest.class));
    suite.addTest(createSingleSuite(FontPropertyEditorTest.class));
    suite.addTest(createSingleSuite(ImagePropertyEditorTest.class));
    suite.addTest(createSingleSuite(InnerClassPropertyEditorTest.class));
    suite.addTest(createSingleSuite(ObjectPropertyEditorTest.class));
    return suite;
  }
}
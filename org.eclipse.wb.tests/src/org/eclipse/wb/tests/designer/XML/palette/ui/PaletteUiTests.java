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
package org.eclipse.wb.tests.designer.XML.palette.ui;

import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link PaletteInfo}.
 *
 * @author scheglov_ke
 */
public class PaletteUiTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xml.palette.ui");
    suite.addTest(createSingleSuite(PaletteUiTest.class));
    return suite;
  }
}

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
package org.eclipse.wb.tests.designer.swing.model.layout.spring;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

import javax.swing.SpringLayout;

/**
 * Tests for {@link SpringLayout}.
 *
 * @author scheglov_ke
 */
public class SpringLayoutTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swing.model.layout.spring");
		suite.addTest(createSingleSuite(SpringAttachmentTest.class));
		suite.addTest(createSingleSuite(SpringLayoutTest.class));
		suite.addTest(createSingleSuite(SpringLayoutGefTest.class));
		return suite;
	}
}

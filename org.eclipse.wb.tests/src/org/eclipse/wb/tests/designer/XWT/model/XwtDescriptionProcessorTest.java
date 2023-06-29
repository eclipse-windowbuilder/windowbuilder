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
package org.eclipse.wb.tests.designer.XWT.model;

import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.xwt.parser.XwtDescriptionProcessor;

/**
 * Test for {@link XwtDescriptionProcessor}.
 *
 * @author scheglov_ke
 */
public class XwtDescriptionProcessorTest extends XwtModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that each XWT component has default creation, with <code>null</code> ID.
	 */
	public void test_ensureDefaultCreation() throws Exception {
		setFileContentSrc(
				"test/MyComponent.java",
				getSource(
						"package test;",
						"import org.eclipse.swt.widgets.Composite;",
						"public class MyComponent extends Composite {",
						"  public MyComponent(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parse("<Shell/>");
		// analyze description
		ComponentDescription description =
				ComponentDescriptionHelper.getDescription(m_lastContext, "test.MyComponent");
		assertNotNull(description.getCreation(null));
	}
}
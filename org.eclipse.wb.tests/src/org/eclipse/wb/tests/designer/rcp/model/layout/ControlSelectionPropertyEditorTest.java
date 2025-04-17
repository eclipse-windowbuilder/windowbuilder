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
package org.eclipse.wb.tests.designer.rcp.model.layout;

import org.eclipse.wb.internal.swt.model.layout.form.ControlSelectionPropertyEditor;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ControlSelectionPropertyEditor}.
 *
 * @author mitin_aa
 */
public class ControlSelectionPropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// prepare testing object
		setFileContentSrc(
				"test/MyTestObject.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyTestObject {",
						"  public Control control;",
						"}"));
		setFileContentSrc(
				"test/MyTestObject.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <property id='control'>",
						"    <editor id='controlSelection'/>",
						"  </property>",
						"</component>"));
		waitForAutoBuild();
	}

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
	// getText()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for <code>getText()</code>.
	 */
	@Test
	public void test_getText_0() throws Exception {
		// TODO
		/*CompositeInfo shell =
    		parseComposite(
    				"public class Test extends Shell {",
    				"  public Test() {",
    				"  }",
    				"}");
    JavaInfo newInfo = createJavaInfo(m_lastEditor, "test.MyTestObject");*/
	}
}
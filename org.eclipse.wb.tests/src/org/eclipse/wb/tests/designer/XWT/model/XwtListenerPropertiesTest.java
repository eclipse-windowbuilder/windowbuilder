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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.util.XwtListenerProperties;

/**
 * Test for {@link XwtListenerProperties}.
 *
 * @author scheglov_ke
 */
public class XwtListenerPropertiesTest extends XwtModelTest {
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
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void tearDown() throws Exception {
		DesignerPlugin.getActivePage().closeAllEditors(false);
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If widget has no name, we generate handler method name without widget name.
	 */
	public void test_openListener_noName() throws Exception {
		createTestClass(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public class Test {",
				"}");
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell x:Class='test.Test'>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		Property property = PropertyUtils.getByPath(button, "Events/KeyDown");
		// method was added
		openListener(property);
		assertEquals(
				getJavaSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class Test {",
						"  public void onKeyDown(Event event) {",
						"  }",
						"}"),
				getJavaFile());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell x:Class='test.Test'>",
				"  <Button wbp:name='button' KeyDownEvent='onKeyDown'/>",
				"</Shell>");
	}

	/**
	 * When widget has name, we should include it into event listener method name.
	 */
	public void test_openListener_hasName() throws Exception {
		createTestClass(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public class Test {",
				"}");
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell x:Class='test.Test'>",
				"  <Button wbp:name='button' x:Name='myButton'/>",
				"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		Property property = PropertyUtils.getByPath(button, "Events/KeyDown");
		// method was added
		openListener(property);
		assertEquals(
				getJavaSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class Test {",
						"  public void onMyButtonKeyDown(Event event) {",
						"  }",
						"}"),
				getJavaFile());
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell x:Class='test.Test'>",
				"  <Button wbp:name='button' x:Name='myButton' KeyDownEvent='onMyButtonKeyDown'/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void createTestClass(String... lines) throws Exception {
		setFileContentSrc("test/Test.java", getJavaSource(lines));
		waitForAutoBuild();
	}

	private static void openListener(Property property) throws Exception {
		ReflectionUtils.invokeMethod(property, "openListener()");
	}

	private static String getJavaFile() throws Exception {
		return getFileContentSrc("test/Test.java");
	}
}
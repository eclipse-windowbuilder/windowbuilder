/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.XWT.model;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.xwt.model.util.NameSupport;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

/**
 * Test for {@link NameSupport}.
 *
 * @author scheglov_ke
 */
public class NameSupportTest extends XwtModelTest {
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
	// getName()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NameSupport#getName(XmlObjectInfo)}.
	 * <p>
	 * No namespace, and no name.
	 */
	@Test
	public void test_getName_noNamespace() throws Exception {
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		// remove namespace declaration
		shell.setAttribute("xmlns:x", null);
		// validate
		Assertions.assertThat(NameSupport.getName(button)).isNull();
	}

	/**
	 * Test for {@link NameSupport#getName(XmlObjectInfo)}.
	 * <p>
	 * Name exists.
	 */
	@Test
	public void test_getName_hasName() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button' x:Name='button' text='Save'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		// has name
		{
			String name = NameSupport.getName(button);
			Assertions.assertThat(name).isEqualTo("button");
		}
		// ...and it is included into presentation text
		{
			String text = ObjectInfo.getText(button);
			assertEquals("Button - button - \"Save\"", text);
		}
	}

	/**
	 * Test for {@link NameSupport#getName(XmlObjectInfo)}.
	 * <p>
	 * Name exists, but not "x:" namespace name is used.
	 */
	@Test
	public void test_getName_hasName_nonStandardNamespace() throws Exception {
		XmlObjectInfo root =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button' x:Name='btn'/>",
						"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		// use "y" name of namespace
		{
			root.setAttribute("xmlns:x", null);
			root.setAttribute("xmlns:y", "http://www.eclipse.org/xwt");
			button.setAttribute("x:Name", null);
			button.setAttribute("y:Name", "btn");
		}
		// validate
		String name = NameSupport.getName(button);
		Assertions.assertThat(name).isEqualTo("btn");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setName()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NameSupport#setName(XmlObjectInfo, String)}.
	 */
	@Test
	public void test_setName_hasName() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		//
		NameSupport.setName(button, "myButton");
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button' x:Name='myButton'/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ensureName()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for single component with no name set.
	 */
	@Test
	public void test_ensureName_generateName() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		//
		{
			String name = NameSupport.ensureName(button);
			Assertions.assertThat(name).isEqualTo("button");
		}
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button' x:Name='button'/>",
				"</Shell>");
	}

	/**
	 * Test for single component with no name set.
	 */
	@Test
	public void test_ensureName_generateName_nonStandardNamespace() throws Exception {
		XmlObjectInfo root =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		// use "y" name of namespace
		{
			root.setAttribute("xmlns:x", null);
			root.setAttribute("xmlns:y", "http://www.eclipse.org/xwt");
		}
		// generate and validate
		String name = NameSupport.ensureName(button);
		Assertions.assertThat(name).isEqualTo("button");
		Assertions.assertThat(m_lastContext.getContent()).contains("y:Name=\"button\"");
	}

	/**
	 * Test for multiple components one of which already has default name set.
	 */
	@Test
	public void test_ensureName_generateUnique() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button x:Name='button'/>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		//
		String name = NameSupport.ensureName(button);
		Assertions.assertThat(name).isEqualTo("button_1");
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button x:Name='button'/>",
				"  <Button wbp:name='button' x:Name='button_1'/>",
				"</Shell>");
	}

	/**
	 * Test for component name to be specifically defined in component description.
	 */
	@Test
	public void test_ensureName_defaultNameInXML() throws Exception {
		prepareMyComponent(new String[]{}, new String[]{
				"// filler filler filler filler filler",
				"<parameters>",
				"  <parameter name='variable.name'>testName</parameter>",
		"</parameters>"});
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <t:MyComponent wbp:name='myComponent'/>",
				"</Shell>");
		refresh();
		ControlInfo component = getObjectByName("myComponent");
		//
		String name = NameSupport.ensureName(component);
		Assertions.assertThat(name).isEqualTo("testName");
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <t:MyComponent wbp:name='myComponent' x:Name='testName'/>",
				"</Shell>");
	}

	/**
	 * Generate new name, using {@link ComponentNameDescription}.
	 */
	@Test
	public void test_ensureName_defaultNameInPreferences() throws Exception {
		// set descriptions
		{
			List<ComponentNameDescription> descriptions = Lists.newArrayList();
			descriptions.add(new ComponentNameDescription("org.eclipse.swt.widgets.Button",
					"myButton",
					"mbtn"));
			NamesManager.setNameDescriptions(RcpToolkitDescription.INSTANCE, descriptions);
		}
		// parse
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		//
		String name = NameSupport.ensureName(button);
		Assertions.assertThat(name).isEqualTo("myButton");
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button' x:Name='myButton'/>",
				"</Shell>");
	}

	/**
	 * When we check for existing "x:Name" attributes, we should not try to ask "element" of virtual
	 * {@link XmlObjectInfo}, to prevent its materialization.
	 */
	@Test
	public void test_ensureName_ignoreVirtualElements() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		refresh();
		ControlInfo button = getObjectByName("button");
		//
		String name = NameSupport.ensureName(button);
		Assertions.assertThat(name).isEqualTo("button");
		assertXML(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <GridLayout/>",
				"  </Shell.layout>",
				"  <Button wbp:name='button' x:Name='button'/>",
				"</Shell>");
	}
}
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
package org.eclipse.wb.tests.designer.core.model.variables;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

/**
 * Test for {@link NamesManager}.
 *
 * @author scheglov_ke
 */
public class NamesManagerTest extends AbstractVariableTest {
	private static final ToolkitDescriptionJava TOOLKIT = ToolkitProvider.DESCRIPTION;

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
	// NamesManager
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getDefaultVariableName() throws Exception {
		assertEquals("button", invoke_getDefaultName("a.b.Button"));
		assertEquals("button", invoke_getDefaultName("a.b.JButton"));
		assertEquals("bUtton_", invoke_getDefaultName("a.b.bUtton"));
		assertEquals("button", invoke_getDefaultName("a.b.BUTTON"));
	}

	private static String invoke_getDefaultName(String qualifiedClassName) throws Exception {
		return (String) ReflectionUtils.invokeMethod2(
				NamesManager.class,
				"getDefaultName",
				String.class,
				qualifiedClassName);
	}

	public void test_getDefaultAcronym() throws Exception {
		assertEquals("btn", invoke_getDefaultAcronym("org.eclipse.swt.widgets.Button"));
		assertEquals("btn", invoke_getDefaultAcronym("javax.swing.JButton"));
		assertEquals("stldtxt", invoke_getDefaultAcronym("org.eclipse.swt.custom.StyledText"));
		// only vowels
		assertEquals("IAo", invoke_getDefaultAcronym("com.mycompany.IAo"));
	}

	private static String invoke_getDefaultAcronym(String qualifiedClassName) throws Exception {
		return (String) ReflectionUtils.invokeMethod2(
				NamesManager.class,
				"getDefaultAcronym",
				String.class,
				qualifiedClassName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Name/Acronym
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getNameAcronym_default() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// for javax.swing.JPanel default name/acronym are used
		assertEquals("panel", NamesManager.getName(panel));
		assertEquals("pnl", NamesManager.getAcronym(panel));
	}

	public void test_getNameAcronym_explicit() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"// filler filler filler",
						"public class MyPanel extends JPanel {",
						"}"));
		setFileContent(
				"wbp-meta/test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='variable.name'>pName</parameter>",
						"    <parameter name='variable.acronym'>pAcr</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		// get name/acronym from description
		assertEquals("pName", NamesManager.getName(panel));
		assertEquals("pAcr", NamesManager.getAcronym(panel));
	}

	/**
	 * If explicit name was set in superclass, we still want to be able to use default name in
	 * subclass.
	 */
	public void test_getNameAcronym_resetExplicit() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"// filler filler filler",
						"public class MyPanel extends JPanel {",
						"}"));
		setFileContent(
				"wbp-meta/test/MyPanel.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='variable.name'>pName</parameter>",
						"    <parameter name='variable.acronym'>pAcr</parameter>",
						"  </parameters>",
						"</component>"));
		setFileContentSrc(
				"test/MyPanel2.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"// filler filler filler",
						"public class MyPanel2 extends MyPanel {",
						"}"));
		setFileContent(
				"wbp-meta/test/MyPanel2.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='variable.name'></parameter>",
						"    <parameter name='variable.acronym'></parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel2 {",
						"  public Test() {",
						"  }",
						"}");
		// default names
		assertEquals("myPanel2", NamesManager.getName(panel));
		assertEquals("mpnl2", NamesManager.getAcronym(panel));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ComponentNameDescription
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_getNameDescriptions_setNameDescriptions() throws Exception {
		// set descriptions
		{
			List<ComponentNameDescription> descriptions = Lists.newArrayList();
			descriptions.add(new ComponentNameDescription("javax.swing.JButton", "button", "btn"));
			descriptions.add(new ComponentNameDescription("javax.swing.JComboBox", "combo", "cmb"));
			NamesManager.setNameDescriptions(TOOLKIT, descriptions);
		}
		// check descriptions
		{
			List<ComponentNameDescription> descriptions =
					NamesManager.getNameDescriptions(TOOLKIT, false);
			assertEquals(2, descriptions.size());
			{
				ComponentNameDescription description = descriptions.get(0);
				assertEquals("javax.swing.JButton", description.getClassName());
				assertEquals("button", description.getName());
				assertEquals("btn", description.getAcronym());
				// check accessors
				{
					description.setName("buttn");
					assertEquals("buttn", description.getName());
					//
					description.setAcronym("bt");
					assertEquals("bt", description.getAcronym());
					//
					description.setAsField(true);
					assertTrue(description.isAsField());
				}
			}
			{
				ComponentNameDescription description = descriptions.get(1);
				assertEquals("javax.swing.JComboBox", description.getClassName());
				assertEquals("combo", description.getName());
				assertEquals("cmb", description.getAcronym());
			}
		}
	}

	public void test_getNameDescription() throws Exception {
		// parse to have context for loading ComponentDescription
		parseContainer(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"}",
				"}");
		// set description
		{
			List<ComponentNameDescription> descriptions = Lists.newArrayList();
			descriptions.add(new ComponentNameDescription("javax.swing.JButton", "b_name", "b_acronym"));
			NamesManager.setNameDescriptions(TOOLKIT, descriptions);
		}
		// get description
		{
			JavaInfo button =
					JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							JButton.class,
							new ConstructorCreationSupport());
			// getNameDescription()
			{
				ComponentNameDescription nameDescription =
						invoke_getNameDescription(button.getDescription());
				assertEquals("javax.swing.JButton", nameDescription.getClassName());
				assertEquals("b_name", nameDescription.getName());
				assertEquals("b_acronym", nameDescription.getAcronym());
			}
			// getName/getAcronym
			assertEquals("b_name", NamesManager.getName(button));
			assertEquals("b_acronym", NamesManager.getAcronym(button));
		}
		// no name description
		{
			JavaInfo textField =
					JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							JTextField.class,
							new ConstructorCreationSupport());
			assertNull(invoke_getNameDescription(textField.getDescription()));
		}
	}

	public void test_getNameDescription_innerClass() throws Exception {
		class MyPanel {
		}
		// parse to have context for loading ComponentDescription
		parseContainer(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"}",
				"}");
		// getName
		JavaInfo button =
				JavaInfoUtils.createJavaInfo(m_lastEditor, MyPanel.class, new ConstructorCreationSupport());
		assertEquals("myPanel", NamesManager.getName(button));
		assertEquals("mpnl", NamesManager.getAcronym(button));
	}

	/**
	 * Test for {@link NamesManager#getNameDescription(ComponentDescription)}.
	 * <p>
	 * No component class in {@link ComponentDescription}, for example for "absolute" layout.
	 */
	public void test_getNameDescription_nullClass() throws Exception {
		ComponentDescription description = new ComponentDescription(null);
		ComponentNameDescription nameDescription = invoke_getNameDescription(description);
		assertNull(nameDescription);
	}

	/**
	 * Test that {@link GenerationSettings#getVariable(JavaInfo)} uses
	 * {@link ComponentNameDescription} for returning {@link VariableSupportDescription}.
	 */
	public void test_VariableSupportDescription_from_ComponentNameDescription() throws Exception {
		// parse to have context for loading ComponentDescription
		parseContainer(
				"// filler filler filler",
				"public final class Test extends JPanel {",
				"  public Test() {",
				"}",
				"}");
		// set description for javax.swing.JTextField
		{
			List<ComponentNameDescription> descriptions = Lists.newArrayList();
			descriptions.add(new ComponentNameDescription("javax.swing.JTextField",
					"textField",
					"txt",
					true));
			NamesManager.setNameDescriptions(TOOLKIT, descriptions);
		}
		// check variable for javax.swing.JTextField
		{
			JavaInfo component =
					JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							m_lastLoader.loadClass("javax.swing.JTextField"),
							new ConstructorCreationSupport());
			assertSame(
					FieldUniqueVariableDescription.INSTANCE,
					TOOLKIT.getGenerationSettings().getVariable(component));
		}
		// check variable for javax.swing.JButton, default one expected
		{
			JavaInfo component =
					JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							m_lastLoader.loadClass("javax.swing.JButton"),
							new ConstructorCreationSupport());
			assertSame(
					LocalUniqueVariableDescription.INSTANCE,
					TOOLKIT.getGenerationSettings().getVariable(component));
		}
	}

	private static ComponentNameDescription invoke_getNameDescription(ComponentDescription componentDescription)
			throws Exception {
		return (ComponentNameDescription) ReflectionUtils.invokeMethod2(
				NamesManager.class,
				"getNameDescription",
				ComponentDescription.class,
				componentDescription);
	}

	public void test_ComponentNameDescription_empty() throws Exception {
		IPreferenceStore preferences = TOOLKIT.getPreferences();
		preferences.setValue(IPreferenceConstants.P_VARIABLE_TYPE_SPECIFIC, "");
		assertEquals(0, NamesManager.getNameDescriptions(TOOLKIT, false).size());
	}

	public void test_ComponentNameDescription_default() throws Exception {
		IPreferenceStore preferences = TOOLKIT.getPreferences();
		preferences.setDefault(IPreferenceConstants.P_VARIABLE_TYPE_SPECIFIC, "");
		assertEquals(0, NamesManager.getNameDescriptions(TOOLKIT, true).size());
	}

	/**
	 * Test for {@link NamesManager#getDefaultNameDescription(String)}.
	 */
	public void test_getDefaultNameDescription() throws Exception {
		ComponentNameDescription description =
				NamesManager.getDefaultNameDescription("javax.swing.JButton");
		assertEquals("javax.swing.JButton", description.getClassName());
		assertEquals("button", description.getName());
		assertEquals("btn", description.getAcronym());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NamesManager#validate(String)}.
	 */
	public void test_validate() throws Exception {
		assertNull(NamesManager.validate("${text}"));
		assertNull(NamesManager.validate("${default_name}"));
		assertNull(NamesManager.validate("${class_name}"));
		assertNull(NamesManager.validate("${class_acronym}"));
		assertNotNull(NamesManager.validate("${noSuchVariable}"));
	}
}

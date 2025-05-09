/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.InstanceListPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.InstanceObjectPropertyEditor;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test for {@link InstanceListPropertyEditor}.
 *
 * @author sablin_aa
 */
public class InstanceObjectPropertyEditorTest extends SwingModelTest {
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
	 * Test editor parameters validation.
	 */
	@Test
	public void test_configure() throws Exception {
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
		Map<String, Object> parameters = getEditorParameters();
		{
			// full configuration
			/*InstanceObjectPropertyEditor editor = */createEditor(parameters);
		}
		{
			// valid (no source) configuration
			parameters.remove("source");
			/*InstanceObjectPropertyEditor editor = */createEditor(parameters);
		}
		{
			// wrong (no class) configuration
			parameters.remove("class");
			parameters.put("source", "new javax.swing.AbstractButton() {}");
			try {
				/*InstanceObjectPropertyEditor editor = */createEditor(parameters);
				fail();
			} catch (AssertionFailedException e) {
				if (!e.getMessage().contains("'class'")) {
					throw e;
				}
			}
		}
	}

	/**
	 * Test creating property & editor with empty value.
	 */
	@Test
	public void test_parse_noValue() throws Exception {
		configureContents();
		ContainerInfo container =
				parseContainer(
						"// filler filler filler",
						"public class Test extends TestPanel {",
						"  public Test() {",
						"  }",
						"}");
		// property
		Property property = container.getPropertyByTitle("property");
		assertNotNull(property);
		assertNull(getPropertyText(property));
		assertInstanceOf(InstanceObjectPropertyEditor.class, property.getEditor());
		//editor
		InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
		assertNull(editor.getInstanceExpression(property));
		Assertions.assertThat(editor.getProperties(property)).isEmpty();
	}

	/**
	 * Test creating property & editor with not empty value.
	 */
	@Test
	public void test_parse_withValue() throws Exception {
		configureContents();
		ContainerInfo container =
				parseContainer(
						"public class Test extends TestPanel {",
						"  public Test() {",
						"    setProperty(new JButton());",
						"  }",
						"}");
		// property instance info
		Assertions.assertThat(container.getChildrenComponents()).hasSize(1);
		ComponentInfo childInfo = container.getChildrenComponents().get(0);
		// property
		Property property = container.getPropertyByTitle("property");
		assertNotNull(property);
		assertEquals(getPropertyText(property), "javax.swing.JButton");
		assertInstanceOf(InstanceObjectPropertyEditor.class, property.getEditor());
		//editor
		InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
		assertNotNull(editor.getInstanceExpression(property));
		Assertions.assertThat(editor.getProperties(property)).isNotEmpty();
		// property info
		JavaInfo propertyInfo = editor.getInstanceInfo(property);
		assertNotNull(propertyInfo);
		assertSame(propertyInfo, childInfo);
	}

	/**
	 * Test setting property value using dialog.
	 */
	@Test
	public void test_dialog() throws Exception {
		configureContents();
		ContainerInfo container =
				parseContainer(
						"// filler filler filler",
						"public class Test extends TestPanel {",
						"  public Test() {",
						"  }",
						"}");
		// property instance info
		Assertions.assertThat(container.getChildrenComponents()).hasSize(0);
		// property
		final Property property = container.getPropertyByTitle("property");
		final InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
		// use GUI to set "ExternalLabelProvider"
		{
			// open dialog and animate it
			new UiContext().executeAndCheck(new FailableRunnable<>() {
				@Override
				public void run() throws Exception {
					openPropertyDialog(property);
				}
			}, new FailableConsumer<>() {
				@Override
				public void accept(SWTBot bot) {
					animateOpenTypeSelection(bot, "JButton", "OK");
				}
			});
			// check source
			assertEditor(
					"// filler filler filler",
					"public class Test extends TestPanel {",
					"  public Test() {",
					"    setProperty(new JButton('New button'));",
					"  }",
					"}");
			assertEquals("javax.swing.JButton", getPropertyText(property));
			Assertions.assertThat(container.getChildrenComponents()).hasSize(1);
			assertSame(container.getChildrenComponents().get(0), editor.getInstanceInfo(property));
		}
	}

	/**
	 * Test setting property value using double click and 'source' as template for anonymous class
	 * instance.
	 */
	@Test
	public void test_doubleClick() throws Exception {
		configureContents();
		ContainerInfo container =
				parseContainer(
						"// filler filler filler",
						"public class Test extends TestPanel {",
						"  public Test() {",
						"  }",
						"}");
		// property instance info
		Assertions.assertThat(container.getChildrenComponents()).hasSize(0);
		// property
		Property property = container.getPropertyByTitle("property");
		//editor
		InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
		// kick "doubleClick"
		editor.doubleClick(property, null);
		assertEditor(
				"// filler filler filler",
				"public class Test extends TestPanel {",
				"  public Test() {",
				"    setProperty(",
				"            new AbstractButton() {",
				"            }",
				"            );",
				"  }",
				"}");
		assertEquals("<anonymous>", getPropertyText(property));
		Assertions.assertThat(container.getChildrenComponents()).isEmpty();
	}

	/**
	 * Test property "Restore default value".
	 */
	@Test
	public void test_restore_default() throws Exception {
		configureContents();
		ContainerInfo container =
				parseContainer(
						"// filler filler filler",
						"public class Test extends TestPanel {",
						"  public Test() {",
						"    setProperty(new JButton());",
						"  }",
						"}");
		// property instance info
		Assertions.assertThat(container.getChildrenComponents()).hasSize(1);
		// property
		Property property = container.getPropertyByTitle("property");
		//editor
		InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
		JavaInfo instanceInfo = editor.getInstanceInfo(property);
		assertSame(instanceInfo, container.getChildrenComponents().get(0));
		// manual set listener for property
		InstanceObjectPropertyEditor.installListenerForProperty(instanceInfo);
		// set to default
		property.setValue(Property.UNKNOWN_VALUE);
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends TestPanel {",
				"  public Test() {",
				"  }",
				"}");
		Assertions.assertThat(container.getChildrenComponents()).isEmpty();
	}

	/**
	 * Test for sub properties
	 */
	@Test
	public void test_sub_properties() throws Exception {
		configureContents();
		ContainerInfo container =
				parseContainer(
						"public class Test extends TestPanel {",
						"  public Test() {",
						"    setProperty(new JButton());",
						"  }",
						"}");
		// property
		Property property = container.getPropertyByTitle("property");
		//editor
		InstanceObjectPropertyEditor editor = (InstanceObjectPropertyEditor) property.getEditor();
		JavaInfo instanceInfo = editor.getInstanceInfo(property);
		// sub property
		Property subProperty = getPropertyByTitle(editor.getProperties(property), "text");
		assertSame(subProperty, instanceInfo.getPropertyByTitle("text"));
		subProperty.setValue("value");
		// check source
		assertEditor(
				"public class Test extends TestPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    button.setText('value');",
				"    setProperty(button);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<String, Object> getEditorParameters() {
		//<editor id="instanceObject">
		//	<parameter name="class"></parameter-list>
		//	<parameter name="source">
		//    new javax.swing.AbstractButton() {
		//    }
		//  </parameter>
		//</editor>
		HashMap<String, Object> params = new HashMap<>();
		params.put("class", "");
		params.put("source", getSourceDQ("new javax.swing.AbstractButton() {", "}"));
		return params;
	}

	protected InstanceObjectPropertyEditor createEditor(Map<String, Object> parameters)
			throws Exception {
		InstanceObjectPropertyEditor editor = new InstanceObjectPropertyEditor();
		editor.configure(m_lastState, parameters);
		return editor;
	}

	private void configureContents() throws Exception {
		setJavaContentSrc("test", "TestPanel", new String[]{
				"public class TestPanel extends JPanel {",
				"  public TestPanel(){",
				"  }",
				"  public void setProperty(AbstractButton value){",
				"  }",
		"}"}, new String[]{
				"<?xml version='1.0' encoding='UTF-8'?>",
				"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
				"  <methods>",
				"    <method name='setProperty'>",
				"      <parameter type='javax.swing.AbstractButton' child='true'/>",
				"    </method>",
				"  </methods>",
				"  <property id='setProperty(javax.swing.AbstractButton)'>",
				"    <editor id='instanceObject'>",
				"      <parameter name='class'>javax.swing.AbstractButton</parameter>",
				"      <parameter name='source'><![CDATA[",
				"        new javax.swing.AbstractButton() {",
				"        }",
				"        ]]></parameter>",
				"    </editor>",
				"  </property>",
		"</component>"});
		waitForAutoBuild();
	}
}

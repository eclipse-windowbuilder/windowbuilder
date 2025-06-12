/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryContainerInfo;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.FactoryAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.editor.icon.IconPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;
import org.eclipse.wb.tests.designer.tests.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.resource.ImageDescriptor;

import org.junit.jupiter.api.Test;

import java.util.List;

import javax.swing.JButton;

/**
 * Test for {@link InstanceFactoryCreationSupport}.
 *
 * @author scheglov_ke
 */
public class InstanceFactoryCreationSupportTest extends SwingModelTest {
	private static final ImageDescriptor DEFAULT_FACTORY_ICON = DesignerPlugin
			.getImageDescriptor("components/factory.gif");

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
	// Parsing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse, check for parameters binding to properties.
	 */
	@Test
	public void test_parse() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  /**",
						"  * @wbp.factory.parameter.source text 'Factory button'",
						"  */",
						"  public JButton createButton(String text, Icon icon) {",
						"    return new JButton(text, icon);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse source
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  private final InstanceFactory m_factory = new InstanceFactory();",
						"  Test() {",
						"    add(m_factory.createButton('button', null));",
						"  }",
						"}");
		// check that we have instance factory in container
		{
			List<InstanceFactoryContainerInfo> containers =
					panel.getChildren(InstanceFactoryContainerInfo.class);
			InstanceFactoryContainerInfo container = containers.get(0);
			assertEquals("{instance factory container}", container.toString());
			// prepare factories
			List<InstanceFactoryInfo> factories = container.getChildren(InstanceFactoryInfo.class);
			assertEquals(1, factories.size());
		}
		// button child expected
		ComponentInfo button;
		{
			assertEquals(1, panel.getChildrenComponents().size());
			button = panel.getChildrenComponents().get(0);
		}
		// check association
		{
			Association association = button.getAssociation();
			assertInstanceOf(InvocationChildAssociation.class, association);
			assertEquals("add(m_factory.createButton(\"button\", null))", association.getSource());
		}
		// check creation support
		{
			InstanceFactoryCreationSupport creationSupport =
					(InstanceFactoryCreationSupport) button.getCreationSupport();
			assertEquals(
					"instance factory: {field-initializer: m_factory} createButton(java.lang.String,javax.swing.Icon)",
					creationSupport.toString());
			assertTrue(creationSupport.canDelete());
			assertTrue(creationSupport.getNode() != null);
			assertInstanceOf(CreationSupport.class, creationSupport.getLiveComponentCreation());
			// check description
			FactoryMethodDescription description = creationSupport.getDescription();
			assertTrue(description.isFactory());
			assertEquals("test.InstanceFactory", description.getDeclaringClass().getName());
			assertEquals("javax.swing.JButton", description.getReturnClass().getName());
			assertEquals(2, description.getParameters().size());
			// check parameter "0"
			{
				ParameterDescription parameter = description.getParameter(0);
				assertEquals("text", parameter.getName());
				assertEquals("\"Factory button\"", parameter.getDefaultSource());
				assertEquals("setText(java.lang.String)", parameter.getProperty());
				assertNotNull(parameter.toString());
				// check converter/editor
				assertInstanceOf(StringConverter.class, parameter.getConverter());
				assertInstanceOf(StringPropertyEditor.class, parameter.getEditor());
			}
			// check parameter "1"
			{
				ParameterDescription parameter = description.getParameter(1);
				assertEquals("icon", parameter.getName());
				assertEquals("setIcon(javax.swing.Icon)", parameter.getProperty());
				// check converter/editor
				assertSame(null, parameter.getConverter());
				assertInstanceOf(IconPropertyEditor.class, parameter.getEditor());
			}
		}
		// test accessors
		{
			GenericProperty textProperty = (GenericProperty) button.getPropertyByTitle("text");
			List<ExpressionAccessor> accessors = getGenericPropertyAccessors(textProperty);
			assertEquals(2, accessors.size());
			assertInstanceOf(SetterAccessor.class, accessors.get(0));
			assertInstanceOf(FactoryAccessor.class, accessors.get(1));
			// static factory accessor makes property modified
			assertTrue(textProperty.isModified());
			assertEquals("button", textProperty.getValue());
			// check modification
			{
				// pre-check
				assertRelatedNodes(button, new String[]{"add(m_factory.createButton(\"button\", null))"});
				// modify property and check source
				textProperty.setValue("12345");
				assertRelatedNodes(button, new String[]{"add(m_factory.createButton(\"12345\", null))"});
				// set to default
				textProperty.setValue(Property.UNKNOWN_VALUE);
				assertRelatedNodes(
						button,
						new String[]{"add(m_factory.createButton(\"Factory button\", null))"});
			}
		}
		// check delete
		assertTrue(button.canDelete());
		button.delete();
		assertEditor(
				"public final class Test extends JPanel {",
				"  private final InstanceFactory m_factory = new InstanceFactory();",
				"  Test() {",
				"  }",
				"}");
	}

	/**
	 * Two instances of {@link InstanceFactoryInfo}.
	 */
	@Test
	public void test_parse2() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public JButton createButton(String text) {",
						"    return new JButton(text);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse source
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  private final InstanceFactory m_factory = new InstanceFactory();",
						"  private final InstanceFactory m_factory2 = new InstanceFactory();",
						"  Test() {",
						"    add(m_factory.createButton('111'));",
						"    add(m_factory2.createButton('222'));",
						"  }",
						"}");
		// check that we have two instance factories in container
		{
			List<InstanceFactoryContainerInfo> containers =
					panel.getChildren(InstanceFactoryContainerInfo.class);
			InstanceFactoryContainerInfo container = containers.get(0);
			// check container
			{
				// delete
				{
					assertFalse(container.canDelete());
					container.delete(); // no result expected
				}
				// presentation
				{
					IObjectPresentation presentation = container.getPresentation();
					assertEquals("(instance factories)", presentation.getText());
					assertNotNull(presentation.getIcon());
				}
			}
			// check factories children
			{
				List<InstanceFactoryInfo> factories = container.getChildren(InstanceFactoryInfo.class);
				assertEquals(2, factories.size());
				{
					InstanceFactoryInfo factory = factories.get(0);
					assertEquals("m_factory", factory.getVariableSupport().getName());
				}
				{
					InstanceFactoryInfo factory = factories.get(1);
					assertEquals("m_factory2", factory.getVariableSupport().getName());
				}
			}
			// get factories
			{
				Class<?> factoryClass = m_lastLoader.loadClass("test.InstanceFactory");
				List<InstanceFactoryInfo> factories = InstanceFactoryInfo.getFactories(panel, factoryClass);
				assertEquals(2, factories.size());
			}
		}
		// two buttons expected
		assertEquals(2, panel.getChildrenComponents().size());
		// button: 0
		{
			ComponentInfo button = panel.getChildrenComponents().get(0);
			InstanceFactoryCreationSupport creationSupport =
					(InstanceFactoryCreationSupport) button.getCreationSupport();
			assertEquals("m_factory", creationSupport.getFactory().getVariableSupport().getName());
		}
		// button: 1
		{
			ComponentInfo button = panel.getChildrenComponents().get(1);
			InstanceFactoryCreationSupport creationSupport =
					(InstanceFactoryCreationSupport) button.getCreationSupport();
			assertEquals("m_factory2", creationSupport.getFactory().getVariableSupport().getName());
		}
	}

	/**
	 * We should support creating of {@link InstanceFactoryInfo} using static factory, for example to
	 * allow users configure it.
	 */
	@Test
	public void test_parse_createInstanceFactory_usingStaticFactory() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public JButton createButton() {",
						"    return new JButton();",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		m_waitForAutoBuild = true;
		parseContainer(
				"public final class Test extends JPanel {",
				"  private final InstanceFactory m_factory = createMyFactory();",
				"  public Test() {",
				"  }",
				"  /**",
				"  * @wbp.factory",
				"  */",
				"  private static InstanceFactory createMyFactory() {",
				"    return new InstanceFactory();",
				"  }",
				"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {instance factory container}",
				"    {static factory: test.Test createMyFactory()} {field-initializer: m_factory} {/createMyFactory()/}");
	}

	/**
	 * Factory method with parent, can be used as {@link MethodInvocation} in
	 * {@link ExpressionStatement}.
	 */
	@Test
	public void test_factoryMethodWithParent() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public JButton createButton(Container parent) {",
						"    JButton button = new JButton();",
						"    parent.add(button);",
						"    return button;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  private final InstanceFactory m_factory = new InstanceFactory();",
						"  Test() {",
						"    m_factory.createButton(this);",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/m_factory.createButton(this)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {instance factory: {field-initializer: m_factory} createButton(java.awt.Container)} {empty} {/m_factory.createButton(this)/}",
				"  {instance factory container}",
				"    {new: test.InstanceFactory} {field-initializer: m_factory} {/new InstanceFactory()/ /m_factory.createButton(this)/}");
		// refresh()
		panel.refresh();
		assertNoErrors(panel);
	}

	/**
	 * We can not create model for interface, at least in general case.
	 */
	@Test
	public void test_factoryMethod_interface() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public Comparable createObject() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public final class Test extends JPanel {",
						"  private final InstanceFactory m_factory = new InstanceFactory();",
						"  Test() {",
						"    m_factory.createObject();",
						"  }",
						"}");
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {instance factory container}",
				"    {new: test.InstanceFactory} {field-initializer: m_factory} {/new InstanceFactory()/ /m_factory.createObject()/}");
		// refresh()
		panel.refresh();
		assertNoErrors(panel);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Add
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for adding.
	 */
	@Test
	public void test_add() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  /**",
						"  * @wbp.factory.parameter.source text '000'",
						"  */",
						"  public JButton createButton(String text) {",
						"    return new JButton(text);",
						"  }",
						"  public void setEnabled(boolean enabled) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse source
		ContainerInfo frame =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JFrame {",
						"  public Test() {",
						"  }",
						"}");
		ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
		BorderLayoutInfo borderLayoutInfo = (BorderLayoutInfo) contentPane.getLayout();
		// prepare instance factory class
		Class<?> factoryClass = m_lastLoader.loadClass("test.InstanceFactory");
		// prepare instance factory model
		InstanceFactoryInfo factoryInfo;
		{
			factoryInfo = InstanceFactoryInfo.add(frame, factoryClass);
			assertNotNull(factoryInfo.getAssociation());
			// check that factory was added to container
			List<InstanceFactoryInfo> factories = InstanceFactoryInfo.getFactories(frame, factoryClass);
			assertEquals(1, factories.size());
			assertTrue(factories.contains(factoryInfo));
		}
		// prepare new component
		ComponentInfo newComponent;
		{
			FactoryMethodDescription description =
					FactoryDescriptionHelper.getDescription(
							m_lastEditor,
							factoryClass,
							"createButton(java.lang.String)",
							false);
			assertEquals("\"000\"", description.getParameter(0).getDefaultSource());
			//
			newComponent =
					(ComponentInfo) JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							JButton.class,
							new InstanceFactoryCreationSupport(factoryInfo, description));
		}
		// add component
		{
			SwingTestUtils.setGenerations(
					LocalUniqueVariableDescription.INSTANCE,
					BlockStatementGeneratorDescription.INSTANCE);
			try {
				borderLayoutInfo.command_CREATE(newComponent, java.awt.BorderLayout.NORTH);
			} finally {
				SwingTestUtils.setGenerationDefaults();
			}
		}
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends JFrame {",
				"  private final InstanceFactory instanceFactory = new InstanceFactory();",
				"  public Test() {",
				"    {",
				"      JButton button = instanceFactory.createButton('000');",
				"      getContentPane().add(button, BorderLayout.NORTH);",
				"    }",
				"  }",
				"}");
		// check association
		{
			Association association = newComponent.getAssociation();
			assertInstanceOf(InvocationChildAssociation.class, association);
			assertEquals("getContentPane().add(button, BorderLayout.NORTH)", association.getSource());
		}
		// set factory property
		factoryInfo.getPropertyByTitle("enabled").setValue(Boolean.FALSE);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JFrame {",
				"  private final InstanceFactory instanceFactory = new InstanceFactory();",
				"  public Test() {",
				"    instanceFactory.setEnabled(false);",
				"    {",
				"      JButton button = instanceFactory.createButton('000');",
				"      getContentPane().add(button, BorderLayout.NORTH);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for adding factory itself.
	 */
	@Test
	public void test_addFactory() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  /**",
						"  * @wbp.factory.parameter.source text '000'",
						"  */",
						"  public JButton createButton(String text) {",
						"    return new JButton(text);",
						"  }",
						"  public void setEnabled(boolean enabled) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse source
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		FlowLayoutInfo flowLayoutInfo = (FlowLayoutInfo) panel.getLayout();
		// prepare instance factory class
		Class<?> factoryClass = m_lastLoader.loadClass("test.InstanceFactory");
		// prepare instance factory model
		InstanceFactoryInfo factoryInfo = InstanceFactoryInfo.add(panel, factoryClass);
		// prepare new component
		ComponentInfo newComponent;
		{
			FactoryMethodDescription description =
					FactoryDescriptionHelper.getDescription(
							m_lastEditor,
							factoryClass,
							"createButton(java.lang.String)",
							false);
			newComponent =
					(ComponentInfo) JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							JButton.class,
							new InstanceFactoryCreationSupport(factoryInfo, description));
		}
		// add component
		{
			SwingTestUtils.setGenerations(
					LocalUniqueVariableDescription.INSTANCE,
					BlockStatementGeneratorDescription.INSTANCE);
			try {
				flowLayoutInfo.add(newComponent, null);
			} finally {
				SwingTestUtils.setGenerationDefaults();
			}
		}
		// check source
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  private final InstanceFactory instanceFactory = new InstanceFactory();",
				"  public Test() {",
				"    {",
				"      JButton button = instanceFactory.createButton('000');",
				"      add(button);",
				"    }",
				"  }",
				"}");
		// set factory property
		factoryInfo.getPropertyByTitle("enabled").setValue(Boolean.FALSE);
		assertEditor(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  private final InstanceFactory instanceFactory = new InstanceFactory();",
				"  public Test() {",
				"    instanceFactory.setEnabled(false);",
				"    {",
				"      JButton button = instanceFactory.createButton('000');",
				"      add(button);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Icon
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If there is custom factory-specific icon, it should be used.
	 */
	@Test
	public void test_icon_withCustom() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class InstanceFactory {",
						"}"));
		// create custom icon
		{
			IFile iconFile = getFileSrc("test", "InstanceFactory.png");
			iconFile.create(Activator.getFile("icons/test.png"), true, null);
		}
		waitForAutoBuild();
		// parse source
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// prepare instance factory class
		Class<?> factoryClass = m_lastLoader.loadClass("test.InstanceFactory");
		// prepare instance factory model
		InstanceFactoryInfo factoryInfo = InstanceFactoryInfo.add(panel, factoryClass);
		// icon should be loaded custom icon
		ImageDescriptor factoryIcon = factoryInfo.getDescription().getIcon();
		assertTrue(UiUtils.equals(Activator.getImageDescriptor("test.png"), factoryIcon));
		// dispose project with any created resource
		do_projectDispose();
	}

	/**
	 * If there is no custom factory-specific icon, then default icon for any instance factory should
	 * be used. Note, that this icon is <em>not</em> just {@link Object} icon.
	 */
	@Test
	public void test_icon_noCustom() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class InstanceFactory {",
						"}"));
		waitForAutoBuild();
		// parse source
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		// prepare instance factory class
		Class<?> factoryClass = m_lastLoader.loadClass("test.InstanceFactory");
		// prepare instance factory model
		InstanceFactoryInfo factoryInfo = InstanceFactoryInfo.add(panel, factoryClass);
		// default factory icon should be used
		assertSame(DEFAULT_FACTORY_ICON, factoryInfo.getDescription().getIcon());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link InstanceFactoryInfo} after parsing should have {@link Association}.
	 */
	@Test
	public void test_nullAssociationProblem() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public JButton createButton() {",
						"    return new JButton();",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse source
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final InstanceFactory instanceFactory = new InstanceFactory();",
						"  public Test() {",
						"  }",
						"}");
		InstanceFactoryInfo factoryInfo =
				InstanceFactoryContainerInfo.get(panel).getChildrenFactory().get(0);
		assertNotNull(factoryInfo.getAssociation());
	}
}

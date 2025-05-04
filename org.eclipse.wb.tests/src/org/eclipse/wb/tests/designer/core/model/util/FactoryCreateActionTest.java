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
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.IPaletteSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.palette.PaletteManager;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryAddCommand;
import org.eclipse.wb.internal.core.model.util.factory.FactoryCreateAction;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

/**
 * Tests for {@link FactoryCreateAction}.
 *
 * @author scheglov_ke
 */
public class FactoryCreateActionTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		action = null;
		super.tearDown();
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
	// findFactoryUnit()
	//
	////////////////////////////////////////////////////////////////////////////
	private ICompilationUnit findFactoryUnit(JavaInfo component) throws Exception {
		action = new FactoryCreateAction(component);
		return (ICompilationUnit) ReflectionUtils.invokeMethod(action, "findFactoryUnit()");
	}

	/**
	 * No factory units.
	 */
	@Test
	public void test_findFactoryUnit_noUnits() throws Exception {
		m_waitForAutoBuild = true;
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertNull(findFactoryUnit(panel));
	}

	/**
	 * We have factory class, however it is not marked with tag or <code>*.wbp-factory.xml</code>.
	 */
	@Test
	public void test_findFactoryUnit_noTagOrDescription() throws Exception {
		setFileContentSrc(
				"test/StaticFactory_.java",
				getTestSource(
						"public final class StaticFactory_ {",
						"  public static JButton createButton(String text) {",
						"    return new JButton(text);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse, just for context
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertNull(findFactoryUnit(panel));
	}

	/**
	 * Test for factory with <code>@wbp.factory</code> in source.
	 */
	@Test
	public void test_findFactoryUnit_tag() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory_.java",
						getTestSource(
								"public final class StaticFactory_ {",
								"  /**",
								"  * @wbp.factory",
								"  */",
								"  public static JButton createButton(String text) {",
								"    return new JButton(text);",
								"  }",
								"}"));
		waitForAutoBuild();
		// parse, just for context
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertEquals(factoryUnit, findFactoryUnit(panel));
	}

	/**
	 * Test for factory with <code>@wbp.factory</code> in source, but not active.
	 */
	@Test
	public void test_findFactoryUnit_tagInComment() throws Exception {
		setFileContentSrc(
				"test/StaticFactory_.java",
				getTestSource(
						"public final class StaticFactory_ {",
						"  public static JButton createButton(String text) {",
						"    // @wbp.factory  -  here this tag means nothing",
						"    return new JButton(text);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse, just for context
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertNull(findFactoryUnit(panel));
	}

	/**
	 * We have factory class and <code>*.wbp-factory.xml</code>.
	 */
	@Test
	public void test_findFactoryUnit_description() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory_.java",
						getTestSource(
								"public final class StaticFactory_ {",
								"  public static JButton createButton(String text) {",
								"    return new JButton(text);",
								"  }",
								"}"));
		setFileContentSrc(
				"test/StaticFactory_.wbp-factory.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<factory>",
						"  <method name='createButton'>",
						"    <parameter type='java.lang.String'/>",
						"  </method>",
						"</factory>"));
		waitForAutoBuild();
		// parse, just for context
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertEquals(factoryUnit, findFactoryUnit(panel));
	}

	/**
	 * We have factory class and <code>*.wbp-factory.xml</code>, but not factory methods.
	 */
	@Test
	public void test_findFactoryUnit_descriptionNoMethods() throws Exception {
		setFileContentSrc(
				"test/StaticFactory_.java",
				getTestSource(
						"public final class StaticFactory_ {",
						"  public static JButton createButton(String text) {",
						"    return new JButton(text);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/StaticFactory_.wbp-factory.xml",
				getSourceDQ("<?xml version='1.0' encoding='UTF-8'?>", "<factory>", "</factory>"));
		waitForAutoBuild();
		// parse, just for context
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		assertNull(findFactoryUnit(panel));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// validate()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_validate() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare Java elements
		IJavaProject javaProject = m_testProject.getJavaProject();
		IProject project = m_testProject.getProject();
		IPackageFragmentRoot validRoot = javaProject.getPackageFragmentRoot(project.getFolder("src"));
		IPackageFragment validPackage = validRoot.getPackageFragment("test");
		String validClass = "MyFactory";
		String validMethod = "createComponent";
		// source folder
		{
			// "null" as source folder
			{
				String message = callValidate(button, null, null, validClass, validMethod);
				assertTrue(message.contains("source folder"));
				assertTrue(message.contains("invalid"));
			}
			// not existing source folder
			{
				IPackageFragmentRoot invalidRoot =
						javaProject.getPackageFragmentRoot(project.getFolder("src2"));
				String message = callValidate(button, invalidRoot, null, validClass, validMethod);
				assertTrue(message.contains("source folder"));
				assertTrue(message.contains("invalid"));
			}
		}
		// package
		{
			// "null" as package
			{
				String message = callValidate(button, validRoot, null, validClass, validMethod);
				assertTrue(message.contains("package"));
				assertTrue(message.contains("invalid"));
			}
			// not existing package
			{
				IPackageFragment invalidPackage = validRoot.getPackageFragment("test2");
				String message = callValidate(button, validRoot, invalidPackage, validClass, validMethod);
				assertTrue(message.contains("package"));
				assertTrue(message.contains("invalid"));
			}
			// default package
			{
				IPackageFragment defaultPackage = validRoot.getPackageFragment("");
				String message = callValidate(button, validRoot, defaultPackage, validClass, validMethod);
				assertTrue(message.contains("package"));
				assertTrue(message.contains("default"));
			}
		}
		// class
		{
			// empty class name
			{
				String message = callValidate(button, validRoot, validPackage, "", validMethod);
				assertTrue(message.contains("class name"));
				assertTrue(message.contains("empty"));
			}
			// "." in class name
			{
				String message = callValidate(button, validRoot, validPackage, "bad.name", validMethod);
				assertTrue(message.contains("class name"));
				assertTrue(message.contains("dot"));
			}
			// bad in class name
			{
				String message = callValidate(button, validRoot, validPackage, "bad name", validMethod);
				assertTrue(message.contains("identifier"));
			}
		}
		// method
		{
			// empty method name
			{
				String message = callValidate(button, validRoot, validPackage, validClass, "");
				assertTrue(message.contains("method name"));
				assertTrue(message.contains("empty"));
			}
			// bad method name
			{
				String message =
						callValidate(button, validRoot, validPackage, validClass, "bad method name");
				assertTrue(message.contains("identifier"));
			}
		}
		// OK
		{
			String message = callValidate(button, validRoot, validPackage, validClass, validMethod);
			assertNull(message);
		}
	}

	@Test
	public void test_validate_existingMethod() throws Exception {
		setFileContentSrc(
				"test/StaticFactory.java",
				getTestSource(
						"public final class StaticFactory {",
						"  public static JButton createComponent() {",
						"    return null;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    add(new JButton());",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare action
		action = new FactoryCreateAction(button);
		generate_configureDefaultTarget();
		// validate
		String message =
				generate_configureInvocations(button, new int[]{}, new String[]{}, new int[][]{});
		assertNotNull(message);
		assertTrue(message.contains("already exists"));
	}

	private String callValidate(JavaInfo component,
			IPackageFragmentRoot root,
			IPackageFragment pkg,
			String className,
			String methodName) throws Exception {
		action = new FactoryCreateAction(component);
		// configure with default package/class
		{
			ReflectionUtils.setField(action, "m_sourceFolder", root);
			ReflectionUtils.setField(action, "m_package", pkg);
			ReflectionUtils.setField(action, "m_className", className);
			ReflectionUtils.setField(action, "m_methodName", methodName);
		}
		// do validate
		return generate_configureInvocations(component, new int[]{}, new String[]{}, new int[][]{});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// generate()
	//
	////////////////////////////////////////////////////////////////////////////
	private FactoryCreateAction action;

	/**
	 * Simplest test for preview.
	 */
	@Test
	public void test_preview() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    add(button);",
						"  }",
						"}");
		String unitSource = m_lastEditor.getSource();
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare action
		action = new FactoryCreateAction(button);
		generate_configureDefaultTarget();
		// check preview
		generate_configureInvocations(button, new int[]{}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		String expectedSource =
				getSourceDQ(
						"  /**",
						"   * @wbp.factory",
						"   */",
						"  public static JButton createComponent() {",
						"    JButton button = new JButton('text');",
						"    return button;",
						"  }");
		String previewSource =
				(String) ReflectionUtils.invokeMethod2(action, "getFactoryPreviewSource");
		assertEquals(StringUtils.chomp(expectedSource), previewSource);
		// no changes in parsed unit expected
		assertEditor(unitSource, m_lastEditor);
	}

	/**
	 * No parameters, use same class.
	 */
	@Test
	public void test_generate_sameFactoryClass() throws Exception {
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		{
			action = new FactoryCreateAction(button);
			IJavaProject javaProject = m_testProject.getJavaProject();
			IProject project = m_testProject.getProject();
			IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(project.getFolder("src"));
			IPackageFragment testPackage = srcFolder.getPackageFragment("test");
			ReflectionUtils.setField(action, "m_sourceFolder", srcFolder);
			ReflectionUtils.setField(action, "m_package", testPackage);
			ReflectionUtils.setField(action, "m_className", "Test");
			ReflectionUtils.setField(action, "m_methodName", "createButton");
			// configure creation/invocations
			{
				String errorMessage;
				errorMessage =
						generate_configureInvocations(button, new int[]{}, new String[]{}, new int[][]{});
				assertNull(errorMessage);
			}
			// generate factory
			ReflectionUtils.invokeMethod2(action, "generate", boolean.class, true);
		}
		m_getSource_ignoreSpacesCheck = true;
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = createButton();",
				"    button.setSelected(true);",
				"    button.setAutoscrolls(true);",
				"    add(button);",
				"  }",
				"  /**",
				"   * @wbp.factory",
				"   */",
				"  public static JButton createButton() {",
				"    JButton button = new JButton('text');",
				"    return button;",
				"  }",
				"}");
	}

	/**
	 * No parameters, use just existing creation source.
	 */
	@Test
	public void test_generate_newFactoryClass() throws Exception {
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{}, new String[]{}, new int[][]{});
		ICompilationUnit factoryUnit =
				m_testProject.getJavaProject().findType("test.StaticFactory").getCompilationUnit();
		assertNotNull("New factory class test.StaticFactory should be created.", factoryUnit);
		try {
			m_getSource_ignoreSpacesCheck = true;
			assertEquals(
					getSourceDQ(
							"package test;",
							"import javax.swing.JButton;",
							"",
							"public final class StaticFactory {",
							"  /**",
							"   * @wbp.factory",
							"   */",
							"  public static JButton createComponent() {",
							"    JButton button = new JButton('text');",
							"    return button;",
							"  }",
							"}").trim(),
					factoryUnit.getSource());
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    JButton button = StaticFactory.createComponent();",
					"    button.setSelected(true);",
					"    button.setAutoscrolls(true);",
					"    add(button);",
					"  }",
					"}");
		} finally {
			factoryUnit.delete(true, null);
		}
	}

	/**
	 * Test that we add {@link FactoryAddCommand} to the palette.
	 */
	@Test
	public void test_generate_addFactoryOnPalette() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// prepare palette manager
		PaletteManager manager;
		{
			manager = new PaletteManager(panel, panel.getDescription().getToolkit().getId());
			manager.reloadPalette();
		}
		// set palette site
		IPaletteSite paletteSite;
		ArgumentCaptor<FactoryAddCommand> factoryCommand = ArgumentCaptor.forClass(FactoryAddCommand.class);
		{
			paletteSite = mock(IPaletteSite.class);
			IPaletteSite.Helper.setSite(panel, paletteSite);
		}
		// prepare target category
		CategoryInfo categoryInfo = manager.getPalette().getCategories().get(0);
		// configure FactoryCreateAction for adding factory on palette
		action = new FactoryCreateAction(button);
		ReflectionUtils.setField(action, "m_paletteCategory", categoryInfo);
		// do generate
		callGenerate2(button, new int[]{}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   */",
						"  public static JButton createComponent() {",
						"    JButton button = new JButton();",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent();",
				"    add(button);",
				"  }",
				"}");
		// verify palette
		verify(paletteSite).addCommand(factoryCommand.capture());
		verifyNoMoreInteractions(paletteSite);
		//
		assertNotNull(factoryCommand.getValue());
		// unsafe command checks
		{
			assertEquals(
					"createComponent()",
					ReflectionUtils.getFieldObject(factoryCommand.getValue(), "m_methodSignature"));
			assertEquals(
					categoryInfo.getId(),
					ReflectionUtils.getFieldObject(factoryCommand.getValue(), "m_categoryId"));
		}
	}

	/**
	 * No parameters, use just existing creation source.
	 */
	@Test
	public void test_generate_creationParameters_0() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   */",
						"  public static JButton createComponent() {",
						"    JButton button = new JButton('text');",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent();",
				"    button.setSelected(true);",
				"    button.setAutoscrolls(true);",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Single creation argument as parameter.
	 */
	@Test
	public void test_generate_creationParameters_1() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text', null);",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{0}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source text 'text'",
						"   */",
						"  public static JButton createComponent(String text) {",
						"    JButton button = new JButton(text, null);",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent('text');",
				"    button.setSelected(true);",
				"    button.setAutoscrolls(true);",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Two creation arguments as parameters.
	 */
	@Test
	public void test_generate_creationParameters_2() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text', null);",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{0, 1}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source text 'text'",
						"   * @wbp.factory.parameter.source icon null",
						"   */",
						"  public static JButton createComponent(String text, Icon icon) {",
						"    JButton button = new JButton(text, icon);",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent('text', null);",
				"    button.setSelected(true);",
				"    button.setAutoscrolls(true);",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Check that we generate non-conflicting parameter names.
	 */
	@Test
	public void test_generate_uniqueParameterNames() throws Exception {
		setFileContentSrc(
				"test/Text.java",
				getTestSource(
						"public class Text extends JButton {",
						"  public Text(String text) {",
						"    setText(text);",
						"  }",
						"}"));
		// empty factory
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    Text button = new Text('text');",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{0}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source text_1 'text'",
						"   */",
						"  public static Text createComponent(String text_1) {",
						"    Text text = new Text(text_1);",
						"    return text;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    Text button = StaticFactory.createComponent('text');",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Single invocation argument as parameter.
	 */
	@Test
	public void test_generate_invocationParameters_1() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{}, new String[]{"setSelected(boolean)"}, new int[][]{{0}});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source selected true",
						"   */",
						"  public static JButton createComponent(boolean selected) {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(selected);",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent(true);",
				"    button.setAutoscrolls(true);",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Two invocation arguments as parameters.
	 */
	@Test
	public void test_generate_invocationParameters_2() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{}, new String[]{
				"setSelected(boolean)",
		"setAutoscrolls(boolean)"}, new int[][]{{0}, {0}});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source selected true",
						"   * @wbp.factory.parameter.source autoscrolls true",
						"   */",
						"  public static JButton createComponent(boolean selected, boolean autoscrolls) {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(selected);",
						"    button.setAutoscrolls(autoscrolls);",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent(true, true);",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Creation and invocation parameters.
	 */
	@Test
	public void test_generate_creation_invocation_parameters() throws Exception {
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton('text');",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{0}, new String[]{
				"setSelected(boolean)",
		"setAutoscrolls(boolean)"}, new int[][]{{0}, {0}});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source text 'text'",
						"   * @wbp.factory.parameter.source selected true",
						"   * @wbp.factory.parameter.source autoscrolls true",
						"   */",
						"  public static JButton createComponent(String text, boolean selected, boolean autoscrolls) {",
						"    JButton button = new JButton(text);",
						"    button.setSelected(selected);",
						"    button.setAutoscrolls(autoscrolls);",
						"    return button;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = StaticFactory.createComponent('text', true, true);",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * Test that we skip invocations with variables.
	 */
	@Test
	public void test_generate_invocations() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    boolean selected = true;",
						"    button.setSelected(selected);",
						"    button.setAutoscrolls(true);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check invocations
		action = new FactoryCreateAction(button);
		{
			Object invocation = generate_findInvocation("setAutoscrolls(boolean)");
			assertTrue((Boolean) ReflectionUtils.getFieldObject(invocation, "m_canExtract"));
		}
		{
			Object invocation = generate_findInvocation("setSelected(boolean)");
			assertFalse((Boolean) ReflectionUtils.getFieldObject(invocation, "m_canExtract"));
		}
	}

	/**
	 * {@link Object#getClass()} is used, extracted as parameter.
	 */
	@Test
	public void test_generate_getClass_parameter() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public void setClazz(Class clazz) {",
						"  }",
						"}"));
		// empty factory
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    button.setClazz(getClass());",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{}, new String[]{"setClazz(java.lang.Class)"}, new int[][]{{0}});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source clazz {wbp_class}",
						"   */",
						"  public static MyButton createComponent(Class clazz) {",
						"    MyButton myButton = new MyButton();",
						"    myButton.setClazz(clazz);",
						"    return myButton;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = StaticFactory.createComponent(getClass());",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * {@link Object#getClass()} is used and extracted as value in factory method.
	 */
	@Test
	public void test_generate_getClass_asValue() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public void setClazz(Class clazz) {",
						"  }",
						"}"));
		// empty factory
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    button.setClazz(getClass());",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{}, new String[]{"setClazz(java.lang.Class)"}, new int[][]{{}});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   */",
						"  public static MyButton createComponent() {",
						"    MyButton myButton = new MyButton();",
						"    myButton.setClazz(Test.class);",
						"    return myButton;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = StaticFactory.createComponent();",
				"    add(button);",
				"  }",
				"}");
	}

	/**
	 * We should skip {@link AnnotationTypeDeclaration}'s, such as event listeners.
	 */
	@Test
	public void test_generate_skipAnonymousCreations() throws Exception {
		setFileContentSrc(
				"test/StaticFactory.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    button.addKeyListener(new KeyAdapter() {});",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check invocations
		action = new FactoryCreateAction(button);
		{
			Object invocation = generate_findInvocation("addKeyListener(java.awt.event.KeyListener)");
			assertNull(invocation);
		}
	}

	/**
	 * By default we don't mark invocations like <code>setBounds()/setLocation()/setSize()</code> as
	 * extracted.
	 */
	@Test
	public void test_generate_dontExtractSetBounds() throws Exception {
		setFileContentSrc(
				"test/StaticFactory.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    button.setSelected(true);",
						"    button.setBounds(0, 0, 100, 100);",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check invocations
		action = new FactoryCreateAction(button);
		// setBounds() is marked as not extractable
		{
			Object invocation = generate_findInvocation("setBounds(int,int,int,int)");
			assertNull(invocation);
		}
		// setSelected() can be extracted
		{
			Object invocation = generate_findInvocation("setSelected(boolean)");
			assertTrue((Boolean) ReflectionUtils.getFieldObject(invocation, "m_canExtract"));
			assertTrue((Boolean) ReflectionUtils.getFieldObject(invocation, "m_extract"));
		}
	}

	/**
	 * Generate factory method and applies it to component.
	 */
	private void callGenerate(ComponentInfo component,
			int[] creationParameters,
			String[] invocationSignatures,
			int[][] invocationParameters) throws Exception {
		action = new FactoryCreateAction(component);
		callGenerate2(component, creationParameters, invocationSignatures, invocationParameters);
	}

	/**
	 * Same as {@link #callGenerate(ComponentInfo, int[], String[], int[][])}, but uses already
	 * created {@link FactoryCreateAction}.
	 */
	private void callGenerate2(ComponentInfo component,
			int[] creationParameters,
			String[] invocationSignatures,
			int[][] invocationParameters) throws Exception {
		generate_configureDefaultTarget();
		// configure creation/invocations
		{
			String errorMessage;
			errorMessage =
					generate_configureInvocations(
							component,
							creationParameters,
							invocationSignatures,
							invocationParameters);
			assertNull(errorMessage);
		}
		// generate factory
		ReflectionUtils.invokeMethod2(action, "generate", boolean.class, true);
	}

	/**
	 * Configures {@link #action} with default package/class/method.
	 */
	private void generate_configureDefaultTarget() throws Exception {
		IJavaProject javaProject = m_testProject.getJavaProject();
		IProject project = m_testProject.getProject();
		IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(project.getFolder("src"));
		IPackageFragment testPackage = srcFolder.getPackageFragment("test");
		ReflectionUtils.setField(action, "m_sourceFolder", srcFolder);
		ReflectionUtils.setField(action, "m_package", testPackage);
		ReflectionUtils.setField(action, "m_className", "StaticFactory");
		ReflectionUtils.setField(action, "m_methodName", "createComponent");
	}

	/**
	 * Configures {@link FactoryCreateAction} creation/invocations.
	 *
	 * @return the result of validation - error message or <code>null</code>.
	 */
	private String generate_configureInvocations(JavaInfo component,
			int[] creationParameters,
			String[] invocationSignatures,
			int[][] invocationParameters) throws Exception {
		assertEquals(invocationSignatures.length, invocationParameters.length);
		// fill creation parameters
		{
			Object creationInfo = ReflectionUtils.getFieldObject(action, "m_creationInfo");
			generate_fillParameters(creationInfo, creationParameters);
		}
		// clear "extract" flag for all invocations (by default we set it to "true" for all invocations)
		{
			List<?> invocations = (List<?>) ReflectionUtils.getFieldObject(action, "m_invocations");
			for (Object invocation : invocations) {
				ReflectionUtils.setField(invocation, "m_extract", Boolean.FALSE);
			}
		}
		// fill invocation parameters
		for (int i = 0; i < invocationSignatures.length; i++) {
			String signature = invocationSignatures[i];
			Object invocation = generate_findInvocation(signature);
			assertNotNull("Can not find invocation with signature: " + signature, invocation);
			ReflectionUtils.setField(invocation, "m_extract", Boolean.TRUE);
			generate_fillParameters(invocation, invocationParameters[i]);
		}
		// validate
		return (String) ReflectionUtils.invokeMethod2(action, "validate");
	}

	/**
	 * @return the <code>InvocationInfo</code> with given signature.
	 */
	private Object generate_findInvocation(String signature) throws Exception {
		List<?> invocations = (List<?>) ReflectionUtils.getFieldObject(action, "m_invocations");
		for (Object invocation : invocations) {
			if (signature.equals(ReflectionUtils.getFieldObject(invocation, "m_signature"))) {
				return invocation;
			}
		}
		// no such invocation
		return null;
	}

	/**
	 * Marks arguments of <code>InvocationInfo</code> as parameters.
	 */
	private void generate_fillParameters(Object invocation, int[] parameters) throws Exception {
		List<?> arguments = (List<?>) ReflectionUtils.getFieldObject(invocation, "m_arguments");
		for (int index : parameters) {
			Object argument = arguments.get(index);
			ReflectionUtils.setField(argument, "m_parameter", Boolean.TRUE);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Generate: parent
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creation arguments with "parent" parameter.
	 */
	@Test
	public void test_generate_creationParameters_parent() throws Exception {
		ICompilationUnit factoryUnit = prepare_generate_creationParameters_parent();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton(this, true);",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// do generate
		callGenerate(button, new int[]{0, 1}, new String[]{}, new int[][]{});
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   * @wbp.factory.parameter.source enabled true",
						"   */",
						"  public static MyButton createComponent(Container parent, boolean enabled) {",
						"    MyButton myButton = new MyButton(parent, enabled);",
						"    return myButton;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = StaticFactory.createComponent(this, true);",
				"    button.setSelected(true);",
				"    button.setAutoscrolls(true);",
				"  }",
				"}");
	}

	/**
	 * Creation arguments with "parent" parameter.
	 */
	@Test
	public void test_generate_creationParameters_parent_usingDialog() throws Exception {
		ICompilationUnit factoryUnit = prepare_generate_creationParameters_parent();
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton(this, true);",
						"    button.setSelected(true);",
						"    button.setAutoscrolls(true);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// animate UI
		final IAction createAction = getCreateFactoryAction(button);
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				createAction.run();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Create factory").bot();
				shell.textWithLabel("&Class:").setText("StaticFactory");
				{
					SWTBotTreeItem treeItem = shell.tree().expandNode("Invocations", "setSelected(boolean)");
					treeItem.uncheck();
				}
				shell.button("OK").click();
			}
		});
		// verify
		m_getSource_ignoreSpacesCheck = true;
		assertEquals(
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public final class StaticFactory {",
						"  /**",
						"   * @wbp.factory",
						"   */",
						"  public static MyButton createMyButton(Container parent) {",
						"    MyButton myButton = new MyButton(parent, true);",
						"    myButton.setAutoscrolls(true);",
						"    return myButton;",
						"  }",
						"}"),
				factoryUnit.getSource());
		assertEditor(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = StaticFactory.createMyButton(this);",
				"    button.setSelected(true);",
				"  }",
				"}");
	}

	private static IAction getCreateFactoryAction(JavaInfo javaInfo) throws Exception {
		IMenuManager contextMenu = getContextMenu(javaInfo);
		IMenuManager factoryMenu = findChildMenuManager(contextMenu, "Factory");
		return findChildAction(factoryMenu, "Create factory...");
	}

	private ICompilationUnit prepare_generate_creationParameters_parent() throws Exception {
		// component with "parent" in constructor
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public MyButton(Container container, boolean enabled) {",
						"    container.add(this);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <constructors>",
						"    <constructor>",
						"      <parameter type='java.awt.Container' parent='true'/>",
						"      <parameter type='boolean'/>",
						"    </constructor>",
						"  </constructors>",
						"</component>"));
		// empty factory
		ICompilationUnit factoryUnit =
				createModelCompilationUnit(
						"test",
						"StaticFactory.java",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"public final class StaticFactory {",
								"}"));
		waitForAutoBuild();
		return factoryUnit;
	}
}

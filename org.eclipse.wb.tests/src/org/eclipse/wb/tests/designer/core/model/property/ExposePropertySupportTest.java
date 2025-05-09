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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.structure.property.IPropertiesMenuContributor;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.ExposePropertySupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for {@link ExposePropertySupport}.
 *
 * @author scheglov_ke
 */
public class ExposePropertySupportTest extends SwingModelTest {
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
	@Test
	public void test_validOrInvalidProperty() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		//
		{
			IAction action = getExposeAction(panel, "Class");
			assertNull(action);
		}
		{
			IAction action = getExposeAction(panel, "enabled");
			assertNotNull(action);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_validate() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"  private int getFoo() {return 0;}",
						"  private void setBar(boolean bar) {}",
						"}");
		IAction action = getExposeAction(panel, "enabled");
		// invalid identifier
		{
			String message = call_validate(action, "bad-name");
			assertTrue(message.contains("identifier"));
		}
		// getter already exists
		{
			String message = call_validate(action, "foo");
			assertTrue(message.contains("getFoo()"));
		}
		// setter already exists
		{
			String message = call_validate(action, "bar");
			assertTrue(message.contains("setBar(boolean)"));
		}
		// OK
		assertNull(call_validate(action, "someUniqueProperty"));
	}

	private static String call_validate(IAction action, String exposedName) throws Exception {
		return (String) ReflectionUtils.invokeMethod(action, "validate(java.lang.String)", exposedName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preview
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Property with primitive type.
	 */
	@Test
	public void test_getPreviewSource_primitive() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		assertEquals(
				getSourceDQ(
						"...",
						"  public float getButtonAlignmentX() {",
						"    return button.getAlignmentX();",
						"  }",
						"  public void setButtonAlignmentX(float alignmentX) {",
						"    button.setAlignmentX(alignmentX);",
						"  }",
						"..."),
				call_getPreview(button, "alignmentX", "buttonAlignmentX", true));
	}

	/**
	 * Test case when parameter of setter conflicts with existing {@link VariableDeclaration}.
	 */
	@Test
	public void test_getPreviewSource_parameter() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  private int alignmentX;",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		assertEquals(
				getSourceDQ(
						"...",
						"  public float getButtonAlignmentX() {",
						"    return button.getAlignmentX();",
						"  }",
						"  public void setButtonAlignmentX(float alignmentX_1) {",
						"    button.setAlignmentX(alignmentX_1);",
						"  }",
						"..."),
				call_getPreview(button, "alignmentX", "buttonAlignmentX", true));
	}

	/**
	 * Property with qualified type name.
	 */
	@Test
	public void test_getPreviewSource_qualified() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		assertEquals(
				getSourceDQ(
						"...",
						"  public String getButtonName() {",
						"    return button.getName();",
						"  }",
						"  public void setButtonName(String name) {",
						"    button.setName(name);",
						"  }",
						"..."),
				call_getPreview(button, "name", "buttonName", true));
	}

	/**
	 * Property with array of objects type name.
	 */
	@Test
	public void test_getPreviewSource_qualifiedArray() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public String[] getItems() {",
						"    return null;",
						"  }",
						"  public void setItems(String[] items) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = new MyButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		// check preview
		assertEquals(
				getSourceDQ(
						"...",
						"  public String[] getButtonItems() {",
						"    return button.getItems();",
						"  }",
						"  public void setButtonItems(String[] items) {",
						"    button.setItems(items);",
						"  }",
						"..."),
				call_getPreview(button, "items", "buttonItems", true));
	}

	/**
	 * <code>protected</code> modifier for exposed.
	 */
	@Test
	public void test_getPreviewSource_protected() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		assertEquals(
				getSourceDQ(
						"...",
						"  protected String getButtonName() {",
						"    return button.getName();",
						"  }",
						"  protected void setButtonName(String name) {",
						"    button.setName(name);",
						"  }",
						"..."),
				call_getPreview(button, "name", "buttonName", false));
	}

	private static String call_getPreview(JavaInfo component,
			String propertyName,
			String exposedName,
			boolean isPublic) throws Exception {
		String initialSource = component.getEditor().getSource();
		// prepare action
		IAction action;
		{
			action = getExposeAction(component, propertyName);
			assertNotNull(action);
			assertTrue(action.isEnabled());
		}
		// get preview
		String previewSource;
		{
			assertNull(call_validate(action, exposedName));
			previewSource =
					(String) ReflectionUtils.invokeMethod2(
							action,
							"getPreviewSource",
							boolean.class,
							isPublic);
		}
		// assert that source is not changed
		assertEquals(initialSource, component.getEditor().getSource());
		// OK
		return previewSource;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// expose()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Expose <code>String</code> property.
	 */
	@Test
	public void test_expose_String() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		call_expose(button, "name", "buttonName", true);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"  public String getButtonName() {",
				"    return button.getName();",
				"  }",
				"  public void setButtonName(String name) {",
				"    button.setName(name);",
				"  }",
				"}");
	}

	/**
	 * Expose <code>String[]</code> property.
	 */
	@Test
	public void test_expose_StringArray() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public String[] getItems() {",
						"    return null;",
						"  }",
						"  public void setItems(String[] items) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = new MyButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		//
		call_expose(button, "items", "buttonItems", true);
		assertEditor(
				"public class Test extends JPanel {",
				"  private MyButton button;",
				"  public Test() {",
				"    button = new MyButton();",
				"    add(button);",
				"  }",
				"  public String[] getButtonItems() {",
				"    return button.getItems();",
				"  }",
				"  public void setButtonItems(String[] items) {",
				"    button.setItems(items);",
				"  }",
				"}");
	}

	private static void call_expose(JavaInfo component,
			String propertyName,
			String exposedName,
			boolean isPublic) throws Exception {
		IAction action = getExposeAction(component, propertyName);
		// do expose
		assertNull(call_validate(action, exposedName));
		ReflectionUtils.invokeMethod2(action, "expose", boolean.class, isPublic);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dialog UI
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_animateDialog() throws Exception {
		parseContainer(
				"public class Test extends JPanel {",
				"  public Test() {",
				"    JButton button = new JButton();",
				"    add(button);",
				"  }",
				"}");
		ComponentInfo button = getJavaInfoByName("button");
		assertNotNull(button);
		// prepare action
		final IAction action = getExposeAction(button, "text");
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() {
				action.run();
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("Expose property").bot();
				// prepare widgets
				SWTBotText textWidget = shell.textWithLabel("Property name:");
				SWTBotStyledText previewWidget = bot.styledTextWithLabel("Preview:");
				SWTBotButton okButton = shell.button("OK");
				// initial state
				{
					assertEquals("buttonText", textWidget.getText());
					Assertions.assertThat(previewWidget.getText()).contains("getButtonText()");
					assertTrue(okButton.isEnabled());
				}
				// set wrong property name
				{
					textWidget.setText("wrong name");
					assertEquals(previewWidget.getText(), "No preview");
					assertFalse(okButton.isEnabled());
				}
				// set good name again
				{
					textWidget.setText("myText");
					Assertions.assertThat(previewWidget.getText()).contains("getMyText()");
					assertTrue(okButton.isEnabled());
				}
				// OK
				okButton.click();
			}
		});
		assertEditor(
				"public class Test extends JPanel {",
				"  private JButton button;",
				"  public Test() {",
				"    button = new JButton();",
				"    add(button);",
				"  }",
				"  public String getMyText() {",
				"    return button.getText();",
				"  }",
				"  public void setMyText(String text) {",
				"    button.setText(text);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "Expose property..." {@link IAction}, which is contributed for given
	 *         {@link Property}, may be <code>null</code>.
	 */
	private static IAction getExposeAction(Property property) throws Exception {
		IMenuManager manager = new MenuManager();
		manager.add(new Separator(IPropertiesMenuContributor.GROUP_EDIT));
		// ask for contributions
		ExposePropertySupport.INSTANCE.contributeMenu(manager, property);
		return findChildAction(manager, "Expose property...");
	}

	/**
	 * @return the "Expose property..." {@link IAction}, which is contributed for given
	 *         {@link JavaInfo}'s property, may be <code>null</code>.
	 */
	private static IAction getExposeAction(JavaInfo javaInfo, String propertyName) throws Exception {
		Property property = javaInfo.getPropertyByTitle(propertyName);
		return getExposeAction(property);
	}
}

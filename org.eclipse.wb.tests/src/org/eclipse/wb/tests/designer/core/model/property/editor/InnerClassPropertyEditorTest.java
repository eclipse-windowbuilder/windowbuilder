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

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.InnerClassPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.Test;

/**
 * Test for {@link InnerClassPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class InnerClassPropertyEditorTest extends SwingModelTest {
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
	 * Generate new inner class.
	 */
	@Test
	public void test_generateInner() throws Exception {
		declareButtonAndProvider();
		declareProviderProperty_inner();
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		final Property property = button.getPropertyByTitle("labelProvider");
		final InnerClassPropertyEditor propertyEditor = (InnerClassPropertyEditor) property.getEditor();
		// no provider
		assertEquals("<double click>", getPropertyText(property));
		// add new provider
		{
			propertyEditor.activate(null, property, null);
			assertEditor(
					"public class Test extends JPanel {",
					"  private class LabelProvider implements ILabelProvider {",
					"    public String getText(Object o) {",
					"      return null;",
					"    }",
					"  }",
					"  public Test() {",
					"    MyButton button = new MyButton();",
					"    button.setLabelProvider(new LabelProvider());",
					"    add(button);",
					"  }",
					"}");
			assertEquals("test.Test.LabelProvider", getPropertyText(property));
		}
		// kick "doubleClick", assert that expected position opened
		{
			int expectedPosition = ((GenericProperty) property).getExpression().getStartPosition();
			// prepare scenario
			IDesignPageSite designerPageSite = mock(IDesignPageSite.class);
			// use DesignPageSite, open position
			DesignPageSite.Helper.setSite(button, designerPageSite);
			propertyEditor.doubleClick(property, null);
			//
			verify(designerPageSite).openSourcePosition(expectedPosition);
			verifyNoMoreInteractions(designerPageSite);
		}
	}

	@Test
	public void test_openExternalClass() throws Exception {
		declareButtonAndProvider();
		declareProviderProperty_inner();
		setFileContentSrc(
				"test/ExternalLabelProvider.java",
				getTestSource(
						"public class ExternalLabelProvider implements ILabelProvider {",
						"  public ExternalLabelProvider(long level) {",
						"  }",
						"  public String getText(Object o) {",
						"    return null;",
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
		final Property property = button.getPropertyByTitle("labelProvider");
		// no provider
		assertEquals("<double click>", getPropertyText(property));
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
					animateOpenTypeSelection(bot, "ExternalLabelPro", "OK");
				}
			});
			// check source
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    MyButton button = new MyButton();",
					"    button.setLabelProvider(new ExternalLabelProvider(0L));",
					"    add(button);",
					"  }",
					"}");
			assertEquals("test.ExternalLabelProvider", getPropertyText(property));
		}
	}

	/**
	 * We can not generate "new AbstractClass()" instance creation, so should not allow user to choose
	 * abstract type.
	 */
	@Test
	public void test_openExternalClass_abstract() throws Exception {
		declareButtonAndProvider();
		declareProviderProperty_inner();
		setFileContentSrc(
				"test/AbstractLabelProvider.java",
				getTestSource(
						"public abstract class AbstractLabelProvider implements ILabelProvider {",
						"  public AbstractLabelProvider(long level) {",
						"  }",
						"  public String getText(Object o) {",
						"    return null;",
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
		final Property property = button.getPropertyByTitle("labelProvider");
		// no provider
		assertEquals("<double click>", getPropertyText(property));
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
					animateOpenTypeSelection(bot, "AbstractLabelPro", "OK");
				}
			});
		}
	}

	/**
	 * Test for generating class as anonymous instead of inner.
	 */
	@Test
	public void test_generateAnonymous() throws Exception {
		declareButtonAndProvider();
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <property id='setLabelProvider(test.ILabelProvider)'>",
						"    <editor id='innerClass'>",
						"      <parameter name='mode'>anonymous</parameter>",
						"      <parameter name='class'>test.ILabelProvider</parameter>",
						"      <parameter name='source'><![CDATA[",
						"new test.ILabelProvider() {",
						"  public String getText(Object o) {",
						"    return null;",
						"  }",
						"}",
						"      ]]></parameter>",
						"    </editor>",
						"  </property>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		final Property property = button.getPropertyByTitle("labelProvider");
		final InnerClassPropertyEditor propertyEditor = (InnerClassPropertyEditor) property.getEditor();
		// no provider
		assertEquals("<double click>", getPropertyText(property));
		// add new provider
		{
			propertyEditor.activate(null, property, null);
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    MyButton button = new MyButton();",
					"    button.setLabelProvider(new ILabelProvider() {",
					"      public String getText(Object o) {",
					"        return null;",
					"      }",
					"    });",
					"    add(button);",
					"  }",
					"}");
			assertEquals("<anonymous>", getPropertyText(property));
		}
	}

	/**
	 * Anonymous class with <code>${parent.firstChild[javax.swing.JLabel].expression}</code> template.
	 */
	@Test
	public void test_templateExpression() throws Exception {
		declareButtonAndProvider();
		setFileContentSrc(
				"test/AbstractLabelProvider.java",
				getTestSource(
						"public abstract class AbstractLabelProvider implements ILabelProvider {",
						"  public AbstractLabelProvider(JLabel label) {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <property id='setLabelProvider(test.ILabelProvider)'>",
						"    <editor id='innerClass'>",
						"      <parameter name='mode'>anonymous</parameter>",
						"      <parameter name='class'>test.ILabelProvider</parameter>",
						"      <parameter name='source'><![CDATA[",
						"new test.AbstractLabelProvider(${parent.firstChild[javax.swing.JLabel].expression}) {",
						"  public String getText(Object o) {",
						"    return null;",
						"  }",
						"}",
						"      ]]></parameter>",
						"    </editor>",
						"  </property>",
						"</component>"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JLabel myLabel = new JLabel();",
						"    add(myLabel);",
						"    //",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(1);
		// prepare property
		final Property property = button.getPropertyByTitle("labelProvider");
		final InnerClassPropertyEditor propertyEditor = (InnerClassPropertyEditor) property.getEditor();
		// no provider
		assertEquals("<double click>", getPropertyText(property));
		// add new provider
		{
			propertyEditor.activate(null, property, null);
			assertEditor(
					"public class Test extends JPanel {",
					"  public Test() {",
					"    JLabel myLabel = new JLabel();",
					"    add(myLabel);",
					"    //",
					"    MyButton button = new MyButton();",
					"    button.setLabelProvider(new AbstractLabelProvider(myLabel) {",
					"      public String getText(Object o) {",
					"        return null;",
					"      }",
					"    });",
					"    add(button);",
					"  }",
					"}");
			assertEquals("<anonymous>", getPropertyText(property));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void declareButtonAndProvider() throws Exception {
		setFileContentSrc(
				"test/ILabelProvider.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public interface ILabelProvider {",
						"  String getText(Object o);",
						"}"));
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"public class MyButton extends JButton {",
						"  public void setLabelProvider(ILabelProvider provider) {",
						"  }",
						"}"));
	}

	private void declareProviderProperty_inner() throws Exception {
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <property id='setLabelProvider(test.ILabelProvider)'>",
						"    <editor id='innerClass'>",
						"      <parameter name='mode'>inner</parameter>",
						"      <parameter name='name'>LabelProvider</parameter>",
						"      <parameter name='class'>test.ILabelProvider</parameter>",
						"      <parameter name='source'><![CDATA[",
						"private class ${name} implements test.ILabelProvider {",
						"  public String getText(Object o) {",
						"    return null;",
						"  }",
						"}",
						"      ]]></parameter>",
						"    </editor>",
						"  </property>",
						"</component>"));
	}
}
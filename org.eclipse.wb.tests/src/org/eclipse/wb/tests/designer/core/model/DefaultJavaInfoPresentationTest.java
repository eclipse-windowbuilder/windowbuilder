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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.tests.Activator;

import org.eclipse.core.resources.IFile;

import org.junit.Test;

/**
 * Test for {@link DefaultJavaInfoPresentation}.
 *
 * @author scheglov_ke
 */
public class DefaultJavaInfoPresentationTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for "normal" component.
	 */
	@Test
	public void test_normalComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check "button" presentation, icon is from ComponentDescription
		IObjectPresentation presentation = button.getPresentation();
		assertInstanceOf(DefaultJavaInfoPresentation.class, presentation);
		assertSame(button.getDescription().getIcon(), presentation.getIcon());
		assertEquals("button", presentation.getText());
	}

	/**
	 * Test for component, created using static factory, with icon.
	 */
	@Test
	public void test_factoryComponent() throws Exception {
		// prepare factory
		{
			setFileContentSrc(
					"test/StaticFactory.java",
					getTestSource(
							"public final class StaticFactory {",
							"  public static JButton createButton() {",
							"    return new JButton();",
							"  }",
							"}"));
			// create icon for "createButton()"
			{
				IFile iconFile = getFileSrc("test", "StaticFactory.createButton__.png");
				iconFile.create(Activator.getFile("icons/test.png"), true, null);
			}
			// build
			waitForAutoBuild();
		}
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = StaticFactory.createButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// check "button" presentation, icon is from FactoryMethodDescription
		StaticFactoryCreationSupport creationSupport =
				(StaticFactoryCreationSupport) button.getCreationSupport();
		IObjectPresentation presentation = button.getPresentation();
		assertSame(creationSupport.getDescription().getIcon(), presentation.getIcon());
		assertEquals("button", presentation.getText());
	}

	/**
	 * Test for component, created using static factory, without icon.
	 */
	@Test
	public void test_factoryComponent_noIcon() throws Exception {
		do_projectDispose();
		do_projectCreate();
		// prepare factory
		{
			setFileContentSrc(
					"test/StaticFactory.java",
					getTestSource(
							"public final class StaticFactory {",
							"  public static JButton createButton() {",
							"    return new JButton();",
							"  }",
							"}"));
			// build
			waitForAutoBuild();
		}
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = StaticFactory.createButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// no icon in FactoryMethodDescription...
		{
			StaticFactoryCreationSupport creationSupport =
					(StaticFactoryCreationSupport) button.getCreationSupport();
			assertNull(creationSupport.getDescription().getIcon());
		}
		// ...so use from ComponentDescription
		IObjectPresentation presentation = button.getPresentation();
		assertSame(button.getDescription().getIcon(), presentation.getIcon());
		assertEquals("button", presentation.getText());
	}
}

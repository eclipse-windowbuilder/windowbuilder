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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for {@link LiveImagesManager}.
 *
 * @author scheglov_ke
 */
public class LiveImagesManagerTest extends RcpModelTest {
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
	// "Live" image
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Check that after "live image" source is not changed, and even {@link ICompilationUnit} is not
	 * touched by {@link AstEditor#commitChanges()}.
	 */
	public void test_noSourceChange() throws Exception {
		parseSource(
				"test",
				"Test.java",
				getSourceDQ(
						"package test;",
						"import org.eclipse.swt.widgets.Shell;",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}"));
		String originalSource = m_lastEditor.getSource();
		// prepare button
		ControlInfo button = BTestUtils.createButton();
		// check image
		{
			Image image = button.getImage();
			assertNotNull(image);
			// no source modification expected
			assertEquals(originalSource, m_lastEditor.getSource());
			assertEquals(originalSource, m_lastEditor.getModelUnit().getSource());
			// compilation unit also not touched
			assertTrue(m_lastEditor.getModelUnit().isConsistent());
		}
		// check preferred size
		{
			Dimension preferredSize = button.getPreferredSize();
			assertNotNull(preferredSize);
		}
	}

	/**
	 * Test for "live" image when top-level component is {@link Shell}.
	 */
	public void test_onShell() throws Exception {
		parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// do checks
		ControlInfo button = BTestUtils.createButton();
		assertNotNull(button.getImage());
		assertNotNull(button.getPreferredSize());
	}

	/**
	 * Test for "live" image when top-level component is {@link Composite}.
	 */
	public void test_onComposite() throws Exception {
		parseComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"  }",
				"}");
		// do checks
		ControlInfo label = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
		assertNotNull(label.getImage());
		assertNotNull(label.getPreferredSize());
	}

	/**
	 * Test that live image, returned by {@link AbstractComponentInfo#getImage()} is not disposed
	 * during refresh. Right now this means that if we cache live images, we should use keep in
	 * {@link AbstractComponentInfo} copy of cached image.
	 */
	public void test_noDispose() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"  }",
						"}");
		RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
		shell.refresh();
		// add button
		{
			ControlInfo button = BTestUtils.createButton();
			// we have live image
			Image image = button.getImage();
			assertNotNull(image);
			assertFalse(image.isDisposed());
			// do add
			rowLayout.command_CREATE(button, null);
		}
		// check live image for new button
		{
			ControlInfo button = BTestUtils.createButton();
			// we still have valid live image
			Image image = button.getImage();
			assertNotNull(image);
			assertFalse(image.isDisposed());
		}
	}

	/**
	 * Test that live images work when there is visible variable with name "shell", because there was
	 * problem in {@link LiveImagesManager} that it used also name "shell".
	 */
	public void test_withShell() throws Exception {
		parseComposite(
				"public class Test {",
				"  private static Shell shell;",
				"  public static void main(String[] args) {",
				"    shell = new Shell();",
				"  }",
				"}");
		// do checks
		ControlInfo text = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
		assertNotNull(text.getImage());
		assertNotNull(text.getPreferredSize());
	}

	/**
	 * Test for custom component.
	 */
	public void test_customComponent() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// do checks
		ControlInfo myComposite = BTestUtils.createControl("test.MyComposite");
		assertNotNull(myComposite.getImage());
		assertNotNull(myComposite.getPreferredSize());
	}

	/**
	 * Test for "live" image during paste.
	 */
	public void test_copyPaste() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		// prepare memento
		JavaInfoMemento memento;
		{
			ControlInfo button = shell.getChildrenControls().get(0);
			memento = JavaInfoMemento.createMemento(button);
		}
		// do checks
		AbstractComponentInfo pasteButton = (AbstractComponentInfo) memento.create(shell);
		assertNotNull(pasteButton.getImage());
		assertNotNull(pasteButton.getPreferredSize());
	}

	/**
	 * Test that we can use "live" and {@link ImplicitFactoryCreationSupport}.
	 */
	public void test_instanceFactory() throws Exception {
		setFileContentSrc(
				"test/InstanceFactory.java",
				getTestSource(
						"public final class InstanceFactory {",
						"  public Button createButton(Composite parent) {",
						"    return new Button(parent, SWT.NONE);",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse source
		CompositeInfo shell =
				parseComposite(
						"public final class Test extends Shell {",
						"  private final InstanceFactory m_factory = new InstanceFactory();",
						"  public Test() {",
						"  }",
						"}");
		shell.refresh();
		// prepare factory
		Class<?> factoryClass = m_lastLoader.loadClass("test.InstanceFactory");
		InstanceFactoryInfo factoryInfo = InstanceFactoryInfo.getFactories(shell, factoryClass).get(0);
		// prepare new Button
		ControlInfo newButton;
		{
			FactoryMethodDescription description =
					FactoryDescriptionHelper.getDescription(
							m_lastEditor,
							factoryClass,
							"createButton(org.eclipse.swt.widgets.Composite)",
							false);
			newButton =
					(ControlInfo) JavaInfoUtils.createJavaInfo(
							m_lastEditor,
							m_lastLoader.loadClass("org.eclipse.swt.widgets.Button"),
							new InstanceFactoryCreationSupport(factoryInfo, description));
		}
		// check for "live" image
		assertNotNull(newButton.getImage());
	}
}
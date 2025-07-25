/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.laf.IBaselineSupport;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.swt.model.widgets.live.SwtLiveManager;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SwtLiveManager}, style access.
 *
 * @author scheglov_ke
 */
public class LiveComponentsManagerTest extends RcpModelTest {
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
	@Test
	public void test_liveImage_noSourceChange() throws Exception {
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

	@Test
	public void test_liveImage_onShell() throws Exception {
		parseComposite(
				"// filler filler filler",
				"class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// prepare buttons
		ControlInfo button_1 = BTestUtils.createButton();
		ControlInfo button_2 = BTestUtils.createButton();
		// check images
		{
			// check that button_1 has "create" image
			Image image_1 = button_1.getImage();
			assertNotNull(image_1);
		}
		// check preferred size
		{
			// check that button_1 has "create" preferred size
			Dimension preferredSize_1 = button_1.getPreferredSize();
			assertNotNull(preferredSize_1);
			// check that button_2 has same preferred size
			assertEquals(preferredSize_1, button_2.getPreferredSize());
		}
	}

	/**
	 * Test that live image, returned by {@link AbstractComponentInfo#getImage()} is not disposed
	 * during refresh. Right now this means that if we cache live images, we should use keep in
	 * {@link AbstractComponentInfo} copy of cached image.
	 */
	@Test
	public void test_liveImage_noDispose() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
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

	@Test
	public void test_liveImage_onComposite() throws Exception {
		parseComposite(
				"public class Test extends Composite {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"  }",
				"}");
		//
		ControlInfo label = BTestUtils.createControl("org.eclipse.swt.widgets.Label");
		assertNotNull(label.getImage());
	}

	/**
	 * Test that live images work when there is visible variable with name "shell", because there was
	 * problem in {@link LiveImagesManager} that it used also name "shell".
	 */
	@Test
	public void test_liveImage_withShell() throws Exception {
		parseComposite(
				"class Test {",
				"  private static Shell shell;",
				"  public static void main(String[] args) {",
				"    shell = new Shell();",
				"  }",
				"}");
		//
		ControlInfo text = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
		assertNotNull(text.getImage());
	}

	/**
	 * Sometimes component has zero or just too small size by default, so we need some way to force
	 * its "live" size.
	 */
	@Test
	public void test_liveImage_forcedSize() throws Exception {
		setFileContentSrc(
				"test/MyCanvas.java",
				getTestSource(
						"public class MyCanvas extends Canvas {",
						"  public MyCanvas(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyCanvas.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='liveComponent.forcedSize.width'>150</parameter>",
						"    <parameter name='liveComponent.forcedSize.height'>30</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// parse
		parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// we should have forced "preferred" size
		ControlInfo myCanvas = BTestUtils.createControl("test.MyCanvas");
		assertEquals(new Dimension(150, 30), myCanvas.getPreferredSize());
	}

	/**
	 * If exception happens during live image creation, we still should return some image (not
	 * <code>null</code>) to prevent problems on other levels.
	 */
	@Test
	public void test_liveImage_whenException() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  public Rectangle getClientArea() {",
						"    throw new IllegalStateException('Problem in getClientArea()');",
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
		// add log listener for exception validation
		ILog log = DesignerPlugin.getDefault().getLog();
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				assertEquals(IStatus.ERROR, status.getSeverity());
				Throwable exception = status.getException();
				Assertions.assertThat(exception).isExactlyInstanceOf(IllegalStateException.class);
				assertEquals("Problem in getClientArea()", exception.getMessage());
			}
		};
		// temporary intercept logging
		try {
			log.addLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			// prepare new component
			ControlInfo newComponent = createJavaInfo("test.MyComposite");
			// ask image
			{
				Image image = newComponent.getImage();
				assertNotNull(image);
				assertEquals(image.getBounds().width, 200);
				assertEquals(image.getBounds().height, 50);
			}
		} finally {
			log.removeLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(true);
		}
	}

	@Test
	public void test_liveImage_copyPaste() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setBounds(10, 10, 200, 100);",
						"  }",
						"}");
		shell.refresh();
		// prepare memento
		JavaInfoMemento memento;
		{
			ControlInfo button = shell.getChildrenControls().get(0);
			memento = JavaInfoMemento.createMemento(button);
		}
		// do paste
		ControlInfo pasteButton = (ControlInfo) memento.create(shell);
		// get "live" image, from memento
		{
			Image image = pasteButton.getImage();
			assertEquals(image.getBounds().width, 200);
			assertEquals(image.getBounds().height, 100);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Style
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_liveStyle_standardControl() throws Exception {
		parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// PUSH
		{
			ControlInfo button = createJavaInfo("org.eclipse.swt.widgets.Button", null);
			int actualStyle = button.getStyle();
			assertTrue(
					(actualStyle & SWT.PUSH) == SWT.PUSH,
					"SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// CHECK
		{
			ControlInfo button = createJavaInfo("org.eclipse.swt.widgets.Button", "check");
			int actualStyle = button.getStyle();
			assertTrue(
					(actualStyle & SWT.CHECK) == SWT.CHECK,
					"SWT.CHECK bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// RADIO
		{
			ControlInfo button = createJavaInfo("org.eclipse.swt.widgets.Button", "radio");
			int actualStyle = button.getStyle();
			assertTrue(
					(actualStyle & SWT.RADIO) == SWT.RADIO,
					"SWT.RADIO bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// Text: BORDER
		{
			ControlInfo text = BTestUtils.createControl("org.eclipse.swt.widgets.Text");
			int actualStyle = text.getStyle();
			assertTrue(
					(actualStyle & SWT.BORDER) == SWT.BORDER,
					"SWT.BORDER bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
	}

	@Test
	public void test_liveStyle_customControl() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, SWT.BORDER | SWT.NO_RADIO_GROUP);",
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
		int actualStyle = myComposite.getStyle();
		assertTrue(
				(actualStyle & SWT.BORDER) == SWT.BORDER,
				"SWT.BORDER bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		assertTrue((actualStyle & SWT.NO_RADIO_GROUP) == SWT.NO_RADIO_GROUP,
				"SWT.NO_RADIO_GROUP bit expected, but "
				+ Integer.toHexString(actualStyle)
				+ " found.");
	}

	@Test
	public void test_liveStyle_forMenu() throws Exception {
		parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// BAR
		{
			WidgetInfo menu = createJavaInfo("org.eclipse.swt.widgets.Menu", "bar");
			int actualStyle = menu.getStyle();
			assertTrue(
					(actualStyle & SWT.BAR) == SWT.BAR,
					"SWT.BAR bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// POPUP
		{
			WidgetInfo menu = createJavaInfo("org.eclipse.swt.widgets.Menu", null);
			int actualStyle = menu.getStyle();
			assertTrue(
					(actualStyle & SWT.POP_UP) == SWT.POP_UP,
					"SWT.POP_UP bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
	}

	@Test
	public void test_liveStyle_forMenuItem() throws Exception {
		parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		// NONE
		{
			WidgetInfo menuItem = createJavaInfo("org.eclipse.swt.widgets.MenuItem");
			int actualStyle = menuItem.getStyle();
			assertTrue(
					(actualStyle & SWT.PUSH) == SWT.PUSH,
					"SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.");
			assertFalse(
					(actualStyle & SWT.CASCADE) == SWT.CASCADE,
					"Not SWT.CASCADE bit expected, but "
					+ Integer.toHexString(actualStyle)
					+ " found.");
		}
		// CHECK
		{
			WidgetInfo menuItem = createJavaInfo("org.eclipse.swt.widgets.MenuItem", "check");
			int actualStyle = menuItem.getStyle();
			assertTrue(
					(actualStyle & SWT.CHECK) == SWT.CHECK,
					"SWT.CHECK bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// RADIO
		{
			WidgetInfo menuItem = createJavaInfo("org.eclipse.swt.widgets.MenuItem", "radio");
			int actualStyle = menuItem.getStyle();
			assertTrue(
					(actualStyle & SWT.RADIO) == SWT.RADIO,
					"SWT.RADIO bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// CASCADE
		{
			WidgetInfo menuItem = createJavaInfo("org.eclipse.swt.widgets.MenuItem", "cascade");
			int actualStyle = menuItem.getStyle();
			assertTrue(
					(actualStyle & SWT.CASCADE) == SWT.CASCADE,
					"SWT.CASCADE bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
	}

	@Test
	public void test_liveStyle_forStaticFactory() throws Exception {
		setFileContentSrc(
				"test/MenuStaticFactory.java",
				getTestSource(
						"public final class MenuStaticFactory {",
						"  public static Button createPushButton(Composite parent) {",
						"    return new Button(parent, SWT.PUSH);",
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
		// get factory description
		FactoryMethodDescription description =
				FactoryDescriptionHelper.getDescription(
						m_lastEditor,
						m_lastState.getEditorLoader().loadClass("test.MenuStaticFactory"),
						"createPushButton(org.eclipse.swt.widgets.Composite)",
						true);
		// create new widget
		ControlInfo button =
				(ControlInfo) JavaInfoUtils.createJavaInfo(
						m_lastEditor,
						description.getReturnClass(),
						new StaticFactoryCreationSupport(description));
		// check style
		int actualStyle = button.getStyle();
		assertTrue(
				(actualStyle & SWT.PUSH) == SWT.PUSH,
				"SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.");
	}

	@Test
	public void test_liveStyle_copyPaste() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setText('my text');",
						"  }",
						"}");
		shell.refresh();
		// prepare memento
		JavaInfoMemento memento;
		{
			ControlInfo button = shell.getChildrenControls().get(0);
			memento = JavaInfoMemento.createMemento(button);
		}
		// do paste
		ControlInfo pasteButton = (ControlInfo) memento.create(shell);
		// check style
		{
			int actualStyle = pasteButton.getStyle();
			assertTrue(
					(actualStyle & SWT.PUSH) == SWT.PUSH,
					"SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.");
		}
		// we still can paste
		RowLayoutInfo rowLayout = (RowLayoutInfo) shell.getLayout();
		rowLayout.command_CREATE(pasteButton, null);
		memento.apply();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    Button button = new Button(this, SWT.NONE);",
				"    button.setText('my text');",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('my text');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Baseline
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_liveBaseline() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"  }",
						"}");
		// prepare button
		ControlInfo newButton = BTestUtils.createButton();
		// get baseline
		int liveBaseline = newButton.getBaseline();
		assertNotEquals(liveBaseline, IBaselineSupport.NO_BASELINE);
		assertTrue(liveBaseline > 0);
		// drop Button
		shell.getLayout().command_CREATE(newButton, null);
		assertEditor(
				"class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		// same baseline as "live"
		int baseline = newButton.getBaseline();
		assertEquals(baseline, liveBaseline);
	}
}
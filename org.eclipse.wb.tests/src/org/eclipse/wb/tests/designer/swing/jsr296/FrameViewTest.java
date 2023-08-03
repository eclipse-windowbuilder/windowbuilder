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
package org.eclipse.wb.tests.designer.swing.jsr296;

import org.eclipse.wb.internal.swing.jsr296.model.FrameViewInfo;
import org.eclipse.wb.internal.swing.jsr296.model.FrameViewTopBoundsSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.draw2d.geometry.Rectangle;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Test for {@link FrameViewInfo}.
 *
 * @author scheglov_ke
 */
public class FrameViewTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		m_testProject.addBundleJars("org.eclipse.wb.tests.support", "/resources/Swing/jsr296");
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Tests that <code>FrameView</code> can be parsed and children are bound.
	 */
	@Ignore
	@Test
	public void test_parse() throws Exception {
		FrameViewInfo view =
				parseJavaInfo(
						"import org.jdesktop.application.*;",
						"public class Test extends FrameView {",
						"  public Test(Application application) {",
						"    super(application);",
						"    {",
						"      JMenuBar menuBar = new JMenuBar();",
						"      setMenuBar(menuBar);",
						"    }",
						"    {",
						"      JToolBar toolBar = new JToolBar();",
						"      setToolBar(toolBar);",
						"    }",
						"    {",
						"      JPanel component = new JPanel();",
						"      setComponent(component);",
						"    }",
						"    {",
						"      JPanel statusBar = new JPanel();",
						"      setStatusBar(statusBar);",
						"    }",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.jdesktop.application.FrameView} {this} {/setMenuBar(menuBar)/ /setToolBar(toolBar)/ /setComponent(component)/ /setStatusBar(statusBar)/}",
				"  {new: javax.swing.JMenuBar} {local-unique: menuBar} {/new JMenuBar()/ /setMenuBar(menuBar)/}",
				"  {new: javax.swing.JToolBar} {local-unique: toolBar} {/new JToolBar()/ /setToolBar(toolBar)/}",
				"  {new: javax.swing.JPanel} {local-unique: component} {/new JPanel()/ /setComponent(component)/}",
				"    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: javax.swing.JPanel} {local-unique: statusBar} {/new JPanel()/ /setStatusBar(statusBar)/}",
				"    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
		refresh();
		assertNoErrors(view);
	}

	/**
	 * Test that {@link FrameViewInfo} handles correctly not only <code>Application</code> parameter,
	 * but also other parameters.
	 */
	@Ignore
	@Test
	public void test_constructorWithOtherArgument() throws Exception {
		useStrictEvaluationMode(false);
		parseJavaInfo(
				"import org.jdesktop.application.*;",
				"public class Test extends FrameView {",
				"  public Test(Application application, boolean enabled) {",
				"    super(application);",
				"    {",
				"      JPanel component = new JPanel();",
				"      setComponent(component);",
				"      component.setEnabled(enabled);",
				"    }",
				"  }",
				"}");
		refresh();
		assertNoErrors(m_lastParseInfo);
	}

	/**
	 * Test for <code>FrameView</code> bounds.
	 */
	@Ignore
	@Test
	public void test_bounds() throws Exception {
		FrameViewInfo view =
				parseJavaInfo(
						"import org.jdesktop.application.*;",
						"public class Test extends FrameView {",
						"  public Test(Application application) {",
						"    super(application);",
						"    {",
						"      JPanel component = new JPanel();",
						"      setComponent(component);",
						"    }",
						"  }",
						"}");
		refresh();
		// FrameView has image and bounds
		{
			assertEquals(new Rectangle(0, 0, 450, 300), view.getModelBounds());
			assertEquals(new Rectangle(0, 0, 450, 300), view.getBounds());
			assertEquals(450, view.getImage().getBounds().width);
			assertEquals(300, view.getImage().getBounds().height);
		}
		// "component"
		{
			ComponentInfo component = getJavaInfoByName("component");
			assertEquals(new Rectangle(8, 30, 434, 262), component.getBounds());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FrameView_TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link FrameViewTopBoundsSupport#setSize(int, int)}.
	 */
	@Ignore
	@Test
	public void test_TopBoundsSupport_setSize() throws Exception {
		FrameViewInfo view =
				parseJavaInfo(
						"import org.jdesktop.application.*;",
						"public class Test extends FrameView {",
						"  public Test(Application application) {",
						"    super(application);",
						"    {",
						"      JPanel component = new JPanel();",
						"      setComponent(component);",
						"    }",
						"  }",
						"}");
		refresh();
		// initial size
		assertEquals(new Rectangle(0, 0, 450, 300), view.getBounds());
		// set new size
		view.getTopBoundsSupport().setSize(500, 400);
		refresh();
		assertEquals(new Rectangle(0, 0, 500, 400), view.getBounds());
	}

	/**
	 * Test for {@link FrameViewTopBoundsSupport#show()}.
	 */
	@Ignore
	@Test
	public void test_TopBoundsSupport_show() throws Exception {
		final FrameViewInfo view =
				parseJavaInfo(
						"import org.jdesktop.application.*;",
						"public class Test extends FrameView {",
						"  public Test(Application application) {",
						"    super(application);",
						"    {",
						"      JPanel component = new JPanel();",
						"      setComponent(component);",
						"    }",
						"  }",
						"}");
		refresh();
		final JFrame frame = view.getFrame();
		// not visible initially
		assertFalse(frame.isVisible());
		// animate show()
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				view.getTopBoundsSupport().show();
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				// now JFrame is visible
				assertTrue(frame.isVisible());
				// ...close it by sending event
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
	}
}
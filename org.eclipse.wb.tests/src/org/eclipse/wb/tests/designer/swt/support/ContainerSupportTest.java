/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link ContainerSupport}.
 *
 * @author scheglov_ke
 */
public class ContainerSupportTest extends AbstractSupportTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String[] getTestSource() {
		return new String[]{
				"public class Test extends Shell {",
				"  public Test() {",
				"    Button button = new Button(this, SWT.CHECK);",
				"    button.setBounds(10, 20, 50, 30);",
				"  }",
		"}"};
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
	// Style
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerSupport#isRTL(Object)}.
	 */
	@Test
	public void test_isRTL_null() throws Exception {
		assertFalse(ContainerSupport.isRTL(null));
	}

	/**
	 * Test for {@link ContainerSupport#isRTL(Object)}.
	 */
	@Test
	public void test_isRTL_false() throws Exception {
		Object composite = ContainerSupport.createComposite(m_shell.getObject(), SWT.NONE);
		assertFalse(ContainerSupport.isRTL(composite));
	}

	/**
	 * Test for {@link ContainerSupport#isRTL(Object)}.
	 */
	@Test
	public void test_isRTL_true() throws Exception {
		Object composite = ContainerSupport.createComposite(m_shell.getObject(), SWT.RIGHT_TO_LEFT);
		assertTrue(ContainerSupport.isRTL(composite));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shell
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerSupport#getShellClass()}.
	 */
	@Test
	public void test_getShellClass() throws Exception {
		Class<?> classShell = m_lastLoader.loadClass("org.eclipse.swt.widgets.Shell");
		assertSame(classShell, ContainerSupport.getShellClass());
	}

	/**
	 * Test for {@link ContainerSupport#isShell(Object)}.
	 */
	@Test
	public void test_isShell_byObject() throws Exception {
		assertTrue(ContainerSupport.isShell(m_shell.getObject()));
		assertFalse(ContainerSupport.isShell(this));
	}

	/**
	 * Test for {@link ContainerSupport#isShell(Class)}.
	 */
	@Test
	public void test_isShell_byClass() throws Exception {
		assertTrue(ContainerSupport.isShell(ContainerSupport.getShellClass()));
		assertTrue(ContainerSupport.isShell(m_shell.getObject().getClass()));
		assertFalse(ContainerSupport.isShell(Object.class));
	}

	/**
	 * Test for {@link ContainerSupport#createShell()}.
	 */
	@Test
	public void test_createShell() throws Exception {
		Object shell = ContainerSupport.createShell();
		try {
			assertTrue(ContainerSupport.isShell(shell));
		} finally {
			ControlSupport.dispose(shell);
		}
	}

	/**
	 * Test for {@link ContainerSupport#setShellText(Object, String)}.
	 */
	@Test
	public void test_setShellText() throws Exception {
		Object shell = ContainerSupport.createShell();
		try {
			assertFalse("My text".equals(ReflectionUtils.invokeMethod(shell, "getText()")));
			ContainerSupport.setShellText(shell, "My text");
			assertTrue("My text".equals(ReflectionUtils.invokeMethod(shell, "getText()")));
		} finally {
			ControlSupport.dispose(shell);
		}
	}

	/**
	 * Test for {@link ContainerSupport#setShellImage(Object, Image)}.
	 */
	@Test
	public void test_setShellImage() throws Exception {
		Object shell = ContainerSupport.createShell();
		Image rcpImage = ImageDescriptor.createFromFile(Object.class, "/javax/swing/plaf/basic/icons/JavaCup16.png")
				.createImage();
		try {
			assertNull(ReflectionUtils.invokeMethod(shell, "getImage()"));
			//
			ContainerSupport.setShellImage(shell, rcpImage);
			// check newly set image
			{
				Object image = ReflectionUtils.invokeMethod(shell, "getImage()");
				// exists...
				assertNotNull(image);
				// ..and has same size
				{
					Object bounds = ReflectionUtils.invokeMethod(image, "getBounds()");
					assertEquals(rcpImage.getBounds().width, ReflectionUtils.getFieldInt(bounds, "width"));
					assertEquals(rcpImage.getBounds().height, ReflectionUtils.getFieldInt(bounds, "height"));
				}
			}
		} finally {
			rcpImage.dispose();
			ControlSupport.dispose(shell);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Composite
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerSupport#getCompositeClass()}.
	 */
	@Test
	public void test_getCompositeClass() throws Exception {
		Class<?> classComposite = m_lastLoader.loadClass("org.eclipse.swt.widgets.Composite");
		assertSame(classComposite, ContainerSupport.getCompositeClass());
	}

	/**
	 * Test for {@link ContainerSupport#isCompositeClass(Class)}.
	 */
	@Test
	public void test_isCompositeClass() throws Exception {
		assertTrue(ContainerSupport.isCompositeClass(m_shell.getObject().getClass()));
		{
			Class<?> classComposite = m_lastLoader.loadClass("org.eclipse.swt.widgets.Composite");
			assertTrue(ContainerSupport.isCompositeClass(classComposite));
		}
		{
			Class<?> classComposite = ContainerSupport.getCompositeClass();
			assertTrue(ContainerSupport.isCompositeClass(classComposite));
		}
		{
			Class<?> classShell = m_lastLoader.loadClass("org.eclipse.swt.widgets.Shell");
			assertTrue(ContainerSupport.isCompositeClass(classShell));
		}
		assertFalse(ContainerSupport.isCompositeClass(Object.class));
	}

	/**
	 * Test for {@link ContainerSupport#isComposite(Object)}.
	 */
	@Test
	public void test_isComposite() throws Exception {
		assertTrue(ContainerSupport.isComposite(m_shell.getObject()));
		assertFalse(ContainerSupport.isComposite(this));
	}

	/**
	 * Test for {@link ContainerSupport#createComposite(Object, int)}.
	 */
	@Test
	public void test_createComposite() throws Exception {
		Object composite = ContainerSupport.createComposite(m_shell.getObject(), 0);
		assertTrue(ContainerSupport.isComposite(composite));
	}

	/**
	 * Test for {@link ContainerSupport#getChildren(Object)}.
	 */
	@Test
	public void test_getChildren() throws Exception {
		Object shellObject = m_shell.getObject();
		// initially single Button as child
		Object[] children = ContainerSupport.getChildren(shellObject);
		Assertions.assertThat(children).hasSize(1);
		assertEquals("org.eclipse.swt.widgets.Button", children[0].getClass().getName());
		// dispose Button, no more children
		ControlSupport.dispose(children[0]);
		Assertions.assertThat(ContainerSupport.getChildren(shellObject)).isEmpty();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerSupport#getLayoutClass()}.
	 */
	@Test
	public void test_getLayoutClass() throws Exception {
		Class<?> classLayout = m_lastLoader.loadClass("org.eclipse.swt.widgets.Layout");
		assertSame(classLayout, ContainerSupport.getLayoutClass());
	}

	/**
	 * Test for {@link ContainerSupport#getLayout(Object)},
	 * {@link ContainerSupport#setLayout(Object, Object)} and {@link ContainerSupport#layout(Object)}.
	 */
	@Test
	public void test_getLayout() throws Exception {
		Object shellObject = m_shell.getObject();
		// initially "null" layout
		assertNull(ContainerSupport.getLayout(shellObject));
		// set RowLayout
		{
			Object rowLayout = m_lastLoader.loadClass("org.eclipse.swt.layout.RowLayout").newInstance();
			ContainerSupport.setLayout(shellObject, rowLayout);
			assertSame(rowLayout, ContainerSupport.getLayout(shellObject));
		}
		// RowLayout was set, but Button is not layout yet
		{
			Object button = ContainerSupport.getChildren(shellObject)[0];
			assertEquals(new Rectangle(10, 20, 50, 30), ControlSupport.getBounds(button));
			// perform layout()
			ContainerSupport.layout(shellObject);
			assertEquals(new Point(3, 3), ControlSupport.getBounds(button).getLocation());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Client area
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ContainerSupport#computeTrim(Object, int, int, int, int)}.
	 */
	@Ignore
	@Test
	public void test_computeTrim() throws Exception {
		Object shellObject = m_shell.getObject();
		Rectangle trim = ContainerSupport.computeTrim(shellObject, 0, 0, 500, 500);
		assertEquals(trim.x, (int) Expectations.get(
						-8,
						new IntValue[]{new IntValue("flanker-windows", -8), new IntValue("scheglov-win", -8)}));
		assertEquals(trim.y, (int) Expectations.get(
						-30,
						new IntValue[]{
								new IntValue("flanker-windows", -30),
								new IntValue("scheglov-win", -30)}));
		assertEquals(trim.width, (int) Expectations.get(
						500 + 8 + 8,
						new IntValue[]{
								new IntValue("flanker-windows", 500 + 8 + 8),
								new IntValue("scheglov-win", 500 + 8 + 8)}));
		assertEquals(trim.height, (int) Expectations.get(
						500 + 30 + 8,
						new IntValue[]{
								new IntValue("flanker-windows", 500 + 30 + 8),
								new IntValue("scheglov-win", 500 + 30 + 8)}));
	}

	/**
	 * Test for {@link ContainerSupport#getClientArea(Object)}.
	 */
	@Ignore
	@Test
	public void test_getClientArea() throws Exception {
		Object shellObject = m_shell.getObject();
		ControlSupport.setSize(shellObject, 500, 500);
		Rectangle clientArea = ContainerSupport.getClientArea(shellObject);
		assertEquals(clientArea.x, 0);
		assertEquals(clientArea.y, 0);
		assertEquals(clientArea.width, (int) Expectations.get(500 - 8 - 8, new IntValue[] {
						new IntValue("flanker-windows", 500 - 8 - 8),
						new IntValue("scheglov-win", 500 - 8 - 8)}));
		assertEquals(clientArea.height, (int) Expectations.get(500 - 30 - 8, new IntValue[] {
						new IntValue("flanker-windows", 500 - 30 - 8),
						new IntValue("scheglov-win", 500 - 30 - 8)}));
	}
}
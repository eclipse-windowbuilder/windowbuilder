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
package org.eclipse.wb.tests.designer.swing.model.top;

import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.top.WindowTopBoundsSupport;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.DimValue;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ICompilationUnit;

import org.junit.Test;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * Basic tests for {@link WindowTopBoundsSupport}.
 *
 * @author scheglov_ke
 */
public class JFrameTopBoundsTest extends SwingGefTest {
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
	 * Size in properties of {@link IResource}.
	 */
	@Test
	public void test_defaultSize() throws Exception {
		Dimension oldSize = new Dimension(450, 300);
		Dimension resizeSize = new Dimension(350, 200);
		ICompilationUnit unit =
				check_resize("// no size", "// none", oldSize, resizeSize, resizeSize, "// no size");
		assert_sameSizeAfterReparse(unit, resizeSize);
	}

	/**
	 * If {@link JFrame} has big size, which was set in superclass, then use this size.
	 */
	@Test
	public void test_JFrame_bigSize_setInSuperclass() throws Exception {
		setFileContentSrc(
				"test/MyBigFrame.java",
				getTestSource("""
						public class MyBigFrame extends JFrame {
							public MyBigFrame() {
								setSize(500, 400);
							}
						}"""));
		waitForAutoBuild();
		//
		Dimension oldSize = new Dimension(500, 400);
		Dimension resizeSize = new Dimension(350, 200);
		ICompilationUnit unit =
				check_resize(
						"MyBigFrame",
						"// no size",
						"// none",
						oldSize,
						resizeSize,
						resizeSize,
						"// no size");
		assert_sameSizeAfterReparse(unit, resizeSize);
	}

	/**
	 * The size of the JFrame should be able to exceed the display resolution.
	 */
	@Test
	public void test_JFrame_veryBig() throws Exception {
		setFileContentSrc("test/MyVeryBigFrame.java",
				getTestSource("""
						public class MyVeryBigFrame extends JFrame {
							public MyVeryBigFrame() {
									setSize(500, 400);
							}
						}"""));
		waitForAutoBuild();
		// Expand horizontally
		Dimension oldSize = new Dimension(500, 400);
		Dimension resizeSize = new Dimension(5000, 400);
		ICompilationUnit unit = check_resize("MyVeryBigFrame",
				"// no size",
				"// none",
				oldSize,
				resizeSize,
				resizeSize,
				"// no size");
		assert_sameSizeAfterReparse(unit, resizeSize);
		// Expand vertically
		oldSize = new Dimension(5000, 400);
		resizeSize = new Dimension(500, 4000);
		unit = check_resize("MyVeryBigFrame",
				"// no size",
				"// none",
				oldSize,
				resizeSize,
				resizeSize,
				"// no size");
		assert_sameSizeAfterReparse(unit, resizeSize);
	}

	/**
	 * If {@link JInternalFrame} has big size, which was set in superclass, then use this size.
	 */
	@Test
	public void test_JInternalFrame_bigSize_setInSuperclass() throws Exception {
		setFileContentSrc(
				"test/MyBigFrame.java",
				getTestSource("""
						public class MyBigFrame extends JInternalFrame {
							public MyBigFrame() {
								setSize(500, 400);
							}
						}"""));
		waitForAutoBuild();
		//
		Dimension oldSize = new Dimension(500, 400);
		Dimension resizeSize = new Dimension(350, 200);
		ICompilationUnit unit =
				check_resize(
						"MyBigFrame",
						"// no size",
						"// none",
						oldSize,
						resizeSize,
						resizeSize,
						"// no size");
		assert_sameSizeAfterReparse(unit, resizeSize);
	}

	/**
	 * Using {@link JFrame#pack()}.
	 */
	@Test
	public void test_resize_pack() throws Exception {
		Dimension packSize =
				Expectations.get(new Dimension(132, 89), new DimValue[]{
						new DimValue("flanker-windows", new Dimension(132, 83)),
						new DimValue("scheglov-win", new Dimension(132, 83)),});
		Dimension resizeSize = new Dimension(450, 300);
		ICompilationUnit unit =
				check_resize("// no size", "pack();", packSize, resizeSize, packSize, "// no size");
		assert_sameSizeAfterReparse(unit, packSize);
	}

	/**
	 * There was request to handle {@link Window} as if there was "pack()" invocation, for example
	 * when superclass has its invocation.
	 */
	@Test
	public void test_packAlways() throws Exception {
		setFileContentSrc(
				"test/MyFrame.java",
				getTestSource("""
						public class MyFrame extends JFrame {
							protected void finishInit() {
								pack();
							}
						}"""));
		setFileContentSrc(
				"test/MyFrame.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <methods>",
						"    <method name='finishInit'/>",
						"  </methods>",
						"  <parameters>",
						"    <parameter name='topBounds.pack'>true</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// open
		ContainerInfo frame = openContainer("""
				// filler filler filler filler filler
				public class Test extends MyFrame {
					public Test() {
						finishInit();
					}
				}""");
		// assert that pack() was invoked and not overridden
		Dimension size = frame.getBounds().getSize();
		assertNotEquals(size.width, 450);
		assertNotEquals(size.height, 300);
	}

	/**
	 * Size in setSize(int,int)
	 */
	@Test
	public void test_resize_setSize_ints() throws Exception {
		Dimension oldSize = new Dimension(300, 200);
		Dimension newSize = new Dimension(400, 300);
		check_resize("setSize(300, 200);", "", oldSize, newSize, newSize, "setSize(400, 300);");
	}

	/**
	 * Size in setBounds(int,int,int,int)
	 */
	@Test
	public void test_resize_setBounds_ints() throws Exception {
		Dimension oldSize = new Dimension(300, 200);
		Dimension newSize = new Dimension(400, 300);
		check_resize(
				"setBounds(0, 0, 300, 200);",
				"",
				oldSize,
				newSize,
				newSize,
				"setBounds(0, 0, 400, 300);");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test resize of {@link JFrame}.
	 */
	private ICompilationUnit check_resize(String oldSizeLine,
			String addSizeString,
			Dimension oldSize,
			Dimension resizeSize,
			Dimension newSize,
			String newSizeLine) throws Exception {
		return check_resize(
				"JFrame",
				oldSizeLine,
				addSizeString,
				oldSize,
				resizeSize,
				newSize,
				newSizeLine);
	}

	/**
	 * Test resize of {@link JFrame} subclass.
	 */
	private ICompilationUnit check_resize(String superClassName,
			String oldSizeLine,
			String addSizeString,
			Dimension oldSize,
			Dimension resizeSize,
			Dimension newSize,
			String newSizeLine) throws Exception {
		ContainerInfo frame = openContainer("""
				public class Test extends %s {
					public Test() {
						%s
						getContentPane().add(new JButton("Swing JButton"), BorderLayout.NORTH);
						getContentPane().add(new Button("AWT Button"), BorderLayout.WEST);
						%s
					}
				}""".formatted(superClassName, oldSizeLine, addSizeString));
		// check size
		assertEquals(oldSize, canvas.getSize(frame));
		waitEventLoop(50);
		// change size
		canvas.beginResize(frame, PositionConstants.EAST);
		canvas.dragTo(frame, resizeSize.width, 0).endDrag();
		canvas.beginResize(frame, PositionConstants.SOUTH);
		canvas.dragTo(frame, 0, resizeSize.height).endDrag();
		// check new size
		assertEquals(newSize, canvas.getSize(frame));
		assertEditor("""
				public class Test extends %s {
					public Test() {
						%s
						getContentPane().add(new JButton("Swing JButton"), BorderLayout.NORTH);
						getContentPane().add(new Button("AWT Button"), BorderLayout.WEST);
						%s
					}
				}""".formatted(superClassName, newSizeLine, addSizeString));
		//
		return m_lastEditor.getModelUnit();
	}

	/**
	 * Close editor, reopen and check for size - it should be same as we set.
	 */
	private void assert_sameSizeAfterReparse(ICompilationUnit unit, Dimension resizeSize)
			throws Exception {
		TestUtils.closeAllEditors();
		openDesign(unit);
		assertEquals(resizeSize, canvas.getSize(m_contentEditPart));
	}
}

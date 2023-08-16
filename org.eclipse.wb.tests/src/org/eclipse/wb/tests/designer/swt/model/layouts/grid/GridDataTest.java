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
package org.eclipse.wb.tests.designer.swt.model.layouts.grid;

import static org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo.getGridData;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridImages;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.junit.Test;

/**
 * Test for {@link GridDataInfo}.
 *
 * @author scheglov_ke
 */
public class GridDataTest extends RcpModelTest {
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
	// Modern alignment constants
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_modernHorizontalAlignment() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData(-1, -1, false, false);",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		shell.startEdit();
		//
		check_modernHorizontalAlignment(shell, SWT.LEFT, SWT.LEFT, "SWT.LEFT");
		check_modernHorizontalAlignment(shell, SWT.BEGINNING, SWT.LEFT, "SWT.LEFT");
		check_modernHorizontalAlignment(shell, GridData.BEGINNING, SWT.LEFT, "SWT.LEFT");
		//
		check_modernHorizontalAlignment(shell, SWT.CENTER, SWT.CENTER, "SWT.CENTER");
		check_modernHorizontalAlignment(shell, GridData.CENTER, SWT.CENTER, "SWT.CENTER");
		//
		check_modernHorizontalAlignment(shell, SWT.RIGHT, SWT.RIGHT, "SWT.RIGHT");
		check_modernHorizontalAlignment(shell, SWT.END, SWT.RIGHT, "SWT.RIGHT");
		check_modernHorizontalAlignment(shell, GridData.END, SWT.RIGHT, "SWT.RIGHT");
		//
		check_modernHorizontalAlignment(shell, SWT.FILL, SWT.FILL, "SWT.FILL");
		check_modernHorizontalAlignment(shell, GridData.FILL, SWT.FILL, "SWT.FILL");
	}

	@Test
	public void test_modernVerticalAlignment() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData(-1, -1, false, false);",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		shell.startEdit();
		//
		check_modernVerticalAlignment(shell, SWT.TOP, SWT.TOP, "SWT.TOP");
		check_modernVerticalAlignment(shell, SWT.BEGINNING, SWT.TOP, "SWT.TOP");
		check_modernVerticalAlignment(shell, GridData.BEGINNING, SWT.TOP, "SWT.TOP");
		//
		check_modernVerticalAlignment(shell, SWT.CENTER, SWT.CENTER, "SWT.CENTER");
		check_modernVerticalAlignment(shell, GridData.CENTER, SWT.CENTER, "SWT.CENTER");
		//
		check_modernVerticalAlignment(shell, SWT.BOTTOM, SWT.BOTTOM, "SWT.BOTTOM");
		check_modernVerticalAlignment(shell, SWT.END, SWT.BOTTOM, "SWT.BOTTOM");
		check_modernVerticalAlignment(shell, GridData.END, SWT.BOTTOM, "SWT.BOTTOM");
		//
		check_modernVerticalAlignment(shell, SWT.FILL, SWT.FILL, "SWT.FILL");
		check_modernVerticalAlignment(shell, GridData.FILL, SWT.FILL, "SWT.FILL");
	}

	private void check_modernHorizontalAlignment(CompositeInfo shell,
			int horizontalAlignment,
			int horizontalAlignmentEx,
			String horizontalSourceEx) throws Exception {
		check_modernAlignments(
				shell,
				horizontalAlignment,
				horizontalAlignmentEx,
				horizontalSourceEx,
				SWT.TOP,
				SWT.TOP,
				"SWT.TOP");
	}

	private void check_modernVerticalAlignment(CompositeInfo shell,
			int verticalAlignment,
			int verticalAlignmentEx,
			String verticalSourceEx) throws Exception {
		check_modernAlignments(
				shell,
				SWT.LEFT,
				SWT.LEFT,
				"SWT.LEFT",
				verticalAlignment,
				verticalAlignmentEx,
				verticalSourceEx);
	}

	private void check_modernAlignments(CompositeInfo shell,
			int horizontalAlignment,
			int horizontalAlignmentEx,
			String horizontalSourceEx,
			int verticalAlignment,
			int verticalAlignmentEx,
			String verticalSourceEx) throws Exception {
		// prepare GridData
		GridDataInfo gridData;
		{
			ControlInfo button = shell.getChildrenControls().get(0);
			gridData = getGridData(button);
		}
		// set/check alignments
		gridData.setHorizontalAlignment(horizontalAlignment);
		gridData.setVerticalAlignment(verticalAlignment);
		assertEquals(horizontalAlignmentEx, gridData.getHorizontalAlignment());
		assertEquals(verticalAlignmentEx, gridData.getVerticalAlignment());
		// check source for GridData
		String gridDataSource = m_lastEditor.getSource(gridData.getCreationSupport().getNode());
		assertEquals(
				String.format("new GridData(%s, %s, false, false)", horizontalSourceEx, verticalSourceEx),
				gridDataSource);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Set alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GridDataInfo#setHorizontalAlignment(int)}.
	 */
	@Test
	public void test_setHorizontalAlignment() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData = getGridData(button);
		// SWT.LEFT is default alignment, so nothing should be changed
		gridData.setHorizontalAlignment(SWT.LEFT);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridDataInfo#setVerticalAlignment(int)}.
	 */
	@Test
	public void test_setVerticalAlignment() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData = getGridData(button);
		// SWT.CENTER is default alignment, so nothing should be changed
		gridData.setVerticalAlignment(SWT.CENTER);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getSmallAlignmentImage() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		shell.refresh();
		try {
			GridDataInfo gridData = getGridData(button);
			check_getSmallAlignmentImage(gridData, true, new int[]{
					SWT.LEFT,
					SWT.CENTER,
					SWT.RIGHT,
					SWT.FILL}, new String[]{"left.gif", "center.gif", "right.gif", "fill.gif"});
			check_getSmallAlignmentImage(gridData, false, new int[]{
					SWT.TOP,
					SWT.CENTER,
					SWT.BOTTOM,
					SWT.FILL}, new String[]{"top.gif", "center.gif", "bottom.gif", "fill.gif"});
		} finally {
			shell.refresh_dispose();
		}
	}

	private static void check_getSmallAlignmentImage(GridDataInfo gridData,
			boolean horizontal,
			int[] alignments,
			String[] paths) throws Exception {
		for (int i = 0; i < alignments.length; i++) {
			int alignment = alignments[i];
			ImageDescriptor expectedImage = GridImages.getImageDescriptor((horizontal ? "h/" : "v/") + paths[i]);
			if (horizontal) {
				gridData.setHorizontalAlignment(alignment);
			} else {
				gridData.setVerticalAlignment(alignment);
			}
			assertSame(expectedImage, gridData.getSmallAlignmentImage(horizontal));
		}
	}

	/**
	 * Set invalid alignment and ask image.
	 */
	@Test
	public void test_getSmallAlignmentImage_invalid() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(-1, -1, false, false));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		GridDataInfo gridData = getGridData(button);
		assertSame(null, gridData.getSmallAlignmentImage(true));
		assertSame(null, gridData.getSmallAlignmentImage(false));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Size hint
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GridDataInfo#getWidthHint()} and {@link GridDataInfo#setWidthHint(int)}.
	 */
	@Test
	public void test_sizeHint_width() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData = GridLayoutInfo.getGridData(button);
		// no hint initially
		assertEquals(-1, gridData.getWidthHint());
		// set hint
		gridData.setWidthHint(200);
		assertEquals(200, gridData.getWidthHint());
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
				"        gridData.widthHint = 200;",
				"        button.setLayoutData(gridData);",
				"      }",
				"    }",
				"  }",
				"}");
		// remove hint
		gridData.setWidthHint(-1);
		assertEquals(-1, gridData.getWidthHint());
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridDataInfo#getHeightHint()} and {@link GridDataInfo#setHeightHint(int)}.
	 */
	@Test
	public void test_sizeHint_height() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData = GridLayoutInfo.getGridData(button);
		// no hint initially
		assertEquals(-1, gridData.getHeightHint());
		// set hint
		gridData.setHeightHint(200);
		assertEquals(200, gridData.getHeightHint());
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);",
				"        gridData.heightHint = 200;",
				"        button.setLayoutData(gridData);",
				"      }",
				"    }",
				"  }",
				"}");
		// remove hint
		gridData.setHeightHint(-1);
		assertEquals(-1, gridData.getHeightHint());
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grab
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_grabHorizontal() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData();",
						"        gridData.grabExcessHorizontalSpace = true;",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// initial state, set new
		{
			GridDataInfo gridData = getGridData(button);
			assertTrue(gridData.getHorizontalGrab());
			gridData.setHorizontalGrab(false);
		}
		// check new state
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		{
			GridDataInfo gridData = getGridData(button);
			assertFalse(gridData.getHorizontalGrab());
		}
	}

	@Test
	public void test_grabVertical() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData();",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// initial state, set new
		{
			GridDataInfo gridData = getGridData(button);
			assertFalse(gridData.getVerticalGrab());
			gridData.setVerticalGrab(true);
		}
		// check new state
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        GridData gridData = new GridData();",
				"        gridData.grabExcessVerticalSpace = true;",
				"        button.setLayoutData(gridData);",
				"      }",
				"    }",
				"  }",
				"}");
		{
			GridDataInfo gridData = getGridData(button);
			assertTrue(gridData.getVerticalGrab());
		}
	}

	@Test
	public void test_grab_usingProperty() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData();",
						"        gridData.grabExcessHorizontalSpace = true;",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// initial state, set new
		{
			GridDataInfo gridData = getGridData(button);
			assertTrue(gridData.getHorizontalGrab());
			gridData.getPropertyByTitle("grabExcessHorizontalSpace").setValue(Boolean.FALSE);
		}
		// check new state
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		{
			GridDataInfo gridData = getGridData(button);
			assertFalse(gridData.getHorizontalGrab());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Span
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link GridDataInfo#setHorizontalSpan(int)}.
	 */
	@Test
	public void test_setHorizontalSpan() throws Exception {
		parseComposite(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		refresh();
		ControlInfo button = getJavaInfoByName("button_1");
		//
		GridDataInfo gridData = getGridData(button);
		gridData.setHorizontalSpan(2);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"    }",
				"    new Label(this, SWT.NONE);",
				"  }",
				"}");
	}

	/**
	 * Test for using "horizontalSpan" property.
	 */
	@Test
	public void test_setProperty_horizontalSpan() throws Exception {
		parseComposite(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		refresh();
		ControlInfo button = getJavaInfoByName("button_1");
		Property property = getGridData(button).getPropertyByTitle("horizontalSpan");
		// ignore <= 0
		{
			String source = m_lastEditor.getSource();
			property.setValue(0);
			assertEditor(source, m_lastEditor);
		}
		// ignore if "x + span > numColumns"
		{
			String source = m_lastEditor.getSource();
			property.setValue(3);
			assertEditor(source, m_lastEditor);
		}
		// set "2"
		property.setValue(2);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"    }",
				"    new Label(this, SWT.NONE);",
				"  }",
				"}");
	}

	/**
	 * Test for {@link GridDataInfo#setVerticalSpan(int)}.
	 */
	@Test
	public void test_setVerticalSpan() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_3 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_4 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button_2");
		//
		GridDataInfo gridData = getGridData(button);
		gridData.setVerticalSpan(2);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));",
				"    }",
				"    {",
				"      Button button_3 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_4 = new Button(this, SWT.NONE);",
				"    }",
				"    new Label(this, SWT.NONE);",
				"  }",
				"}");
	}

	/**
	 * Test for using "verticalSpan" property.
	 */
	@Test
	public void test_setProperty_verticalSpan() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_3 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_4 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button_2");
		Property property = getGridData(button).getPropertyByTitle("verticalSpan");
		// ignore <= 0
		{
			String source = m_lastEditor.getSource();
			property.setValue(0);
			assertEditor(source, m_lastEditor);
		}
		// ignore if "y + span > numRows"
		{
			String source = m_lastEditor.getSource();
			property.setValue(3);
			assertEditor(source, m_lastEditor);
		}
		// set "2"
		property.setValue(2);
		assertEditor(
				"class Test extends Shell {",
				"  Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));",
				"    }",
				"    {",
				"      Button button_3 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_4 = new Button(this, SWT.NONE);",
				"    }",
				"    new Label(this, SWT.NONE);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_contextMenu_horizontal() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		GridDataInfo gridData = (GridDataInfo) button.getChildrenJava().get(0);
		assertNotNull(gridData);
		//
		shell.refresh();
		try {
			// prepare context menu
			IMenuManager manager;
			{
				manager = getDesignerMenuManager();
				shell.getBroadcastObject().addContextMenu(null, button, manager);
			}
			// check actions
			IMenuManager manager2 = findChildMenuManager(manager, "Horizontal alignment");
			assertNotNull(manager2);
			assertNotNull(findChildAction(manager2, "&Grab excess space"));
			assertNotNull(findChildAction(manager2, "&Left"));
			assertNotNull(findChildAction(manager2, "&Center"));
			assertNotNull(findChildAction(manager2, "&Right"));
			assertNotNull(findChildAction(manager2, "&Fill"));
			// check "check" state
			assertTrue(findChildAction(manager2, "&Left").isChecked());
			assertFalse(findChildAction(manager2, "&Right").isChecked());
			// use "Right" action
			{
				IAction action = findChildAction(manager2, "&Right");
				action.setChecked(true);
				action.run();
				assertEditor(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));",
						"    }",
						"  }",
						"}");
			}
			// use "Grab action"
			{
				IAction action = findChildAction(manager2, "&Grab excess space");
				action.run();
				assertEditor(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));",
						"    }",
						"  }",
						"}");
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_contextMenu_vertical() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		GridDataInfo gridData = (GridDataInfo) button.getChildrenJava().get(0);
		assertNotNull(gridData);
		//
		shell.refresh();
		try {
			// prepare context menu
			IMenuManager manager;
			{
				manager = getDesignerMenuManager();
				shell.getBroadcastObject().addContextMenu(null, button, manager);
			}
			// check actions
			IMenuManager manager2 = findChildMenuManager(manager, "Vertical alignment");
			assertNotNull(manager2);
			assertNotNull(findChildAction(manager2, "&Grab excess space"));
			assertNotNull(findChildAction(manager2, "&Top"));
			assertNotNull(findChildAction(manager2, "&Center"));
			assertNotNull(findChildAction(manager2, "&Bottom"));
			assertNotNull(findChildAction(manager2, "&Fill"));
			// use "Bottom" action
			{
				IAction action = findChildAction(manager2, "&Bottom");
				action.setChecked(true);
				action.run();
				assertEditor(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));",
						"    }",
						"  }",
						"}");
			}
			// use "Grab action"
			{
				IAction action = findChildAction(manager2, "&Grab excess space");
				action.run();
				assertEditor(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, true, 1, 1));",
						"    }",
						"  }",
						"}");
			}
		} finally {
			shell.refresh_dispose();
		}
	}

	@Test
	public void test_contextMenu_horizontalHint() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData();",
						"        gridData.widthHint = 200;",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// clear "widthHint"
		{
			// prepare action
			IAction clearHintAction = getClearHintAction(button, true);
			assertNotNull(clearHintAction);
			// use action
			clearHintAction.run();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    setLayout(new GridLayout(1, false));",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"    }",
					"  }",
					"}");
		}
		// no "widthHint" value, so no action
		{
			IAction clearHintAction = getClearHintAction(button, true);
			assertNull(clearHintAction);
		}
	}

	@Test
	public void test_contextMenu_verticalHint() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        GridData gridData = new GridData();",
						"        gridData.heightHint = 200;",
						"        button.setLayoutData(gridData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// clear "heightHint"
		{
			// prepare action
			IAction clearHintAction = getClearHintAction(button, false);
			assertNotNull(clearHintAction);
			// use action
			clearHintAction.run();
			assertEditor(
					"class Test extends Shell {",
					"  Test() {",
					"    setLayout(new GridLayout(1, false));",
					"    {",
					"      Button button = new Button(this, SWT.NONE);",
					"    }",
					"  }",
					"}");
		}
		// no "widthHint" value, so no action
		{
			IAction clearHintAction = getClearHintAction(button, false);
			assertNull(clearHintAction);
		}
	}

	private IAction getClearHintAction(ControlInfo button, boolean horizontal) throws Exception {
		IMenuManager manager = getContextMenu(button);
		IMenuManager alignmentManager =
				findChildMenuManager(manager, horizontal ? "Horizontal alignment" : "Vertical alignment");
		return findChildAction(alignmentManager, "Clear hint");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Default values
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link GridData} does not use "modern" values as default values for fields, for example it uses
	 * {@link GridData#BEGINNING}, not {@link SWT#LEFT} as we would like. So, we need to check and fix
	 * this.
	 */
	@Test
	public void test_defaultValues() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData = getGridData(button);
		// ask using methods
		assertEquals(SWT.LEFT, gridData.getHorizontalAlignment());
		assertEquals(SWT.CENTER, gridData.getVerticalAlignment());
		assertEquals(false, gridData.getHorizontalGrab());
		assertEquals(false, gridData.getVerticalGrab());
		// ask using properties
		{
			assertEquals(SWT.LEFT, gridData.getPropertyByTitle("horizontalAlignment").getValue());
			assertEquals(SWT.CENTER, gridData.getPropertyByTitle("verticalAlignment").getValue());
		}
	}

	/**
	 * Formally there are no assignment for "horizontalAlignment" property, so we don't have
	 * {@link Expression} for it. So, we should get default value, i.e. value assigned to field in
	 * constructor.
	 */
	@Test
	public void test_separateValuesFor_GridData_FILL_BOTH() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(GridData.FILL_BOTH));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		GridDataInfo gridData = getGridData(button);
		// ask using methods
		assertEquals(SWT.FILL, gridData.getHorizontalAlignment());
		assertEquals(SWT.FILL, gridData.getVerticalAlignment());
		assertEquals(true, gridData.getHorizontalGrab());
		assertEquals(true, gridData.getVerticalGrab());
		// ask using properties
		{
			assertEquals(SWT.FILL, gridData.getPropertyByTitle("horizontalAlignment").getValue());
			assertEquals(SWT.FILL, gridData.getPropertyByTitle("verticalAlignment").getValue());
		}
	}

	@Test
	public void test_deleteIfDefault_emptyConstructor() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData());",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// refresh(), force check
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_constructor4_yes() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// refresh(), force check
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_constructor4_no1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// refresh(), force check
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_constructor4_no2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// refresh(), force check
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_constructor6_yes() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// refresh(), force check
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteIfDefault_constructor6_no1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// refresh(), force check
		ExecutionUtils.refresh(shell);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(2, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dangling
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link GridData} can be used only if parent {@link Composite} has {@link GridLayout}.
	 */
	@Test
	public void test_hasParentLayout_notCompatible() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"class Test extends Shell {",
						"  public Test() {",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setLayoutData(new GridData());",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new Button(this, SWT.NONE)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(new GridData())/}");
		//
		shell.refresh();
		assertNoErrors(shell);
	}
}
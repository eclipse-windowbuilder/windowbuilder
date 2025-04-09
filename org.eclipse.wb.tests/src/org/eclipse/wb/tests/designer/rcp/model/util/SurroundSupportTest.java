/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.rcp.model.util;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.MenuIntersector;
import org.eclipse.wb.internal.rcp.model.util.surround.CTabFolderSurroundTarget;
import org.eclipse.wb.internal.rcp.model.util.surround.SashFormSurroundTarget;
import org.eclipse.wb.internal.rcp.model.util.surround.ScrolledCompositeSurroundTarget;
import org.eclipse.wb.internal.rcp.model.util.surround.TabFolderSurroundTarget;
import org.eclipse.wb.internal.rcp.model.widgets.ScrolledCompositeInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.util.surround.SwtSurroundSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link SwtSurroundSupport}.
 *
 * @author scheglov_ke
 */
public class SurroundSupportTest extends RcpModelTest {
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
	// Invalid selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Empty selection, so no "surround" menu.
	 */
	@Test
	public void test_emptySelection() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		shell.refresh();
		//
		assertNoSurroundManager(shell, Collections.emptyList());
	}

	/**
	 * Try to give {@link LayoutInfo} instead of {@link ControlInfo}, so no "surround" menu.
	 */
	@Test
	public void test_notControl() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		shell.refresh();
		LayoutInfo layout = shell.getLayout();
		//
		assertNoSurroundManager(shell, List.of(layout));
	}

	/**
	 * Components that have different parents, so none of these parents contribute "surround" menu.
	 */
	@Test
	public void test_notSameParent() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(10, 20, 100, 50);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		//
		assertNoSurroundManager(shell, List.of(shell, button));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Flow layouts
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Single {@link ControlInfo} on {@link FillLayoutInfo}.
	 */
	@Test
	public void test_flow_singleControl() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// run action
		runSurround_Composite(button);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new RowLayout(SWT.HORIZONTAL));",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Two adjacent controls, good case.
	 */
	@Test
	public void test_flow_twoControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_3 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		// run action
		runSurround_Composite(button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayout(new RowLayout(SWT.HORIZONTAL));",
				"      {",
				"        Button button_1 = new Button(composite, SWT.NONE);",
				"      }",
				"      {",
				"        Button button_2 = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"    {",
				"      Button button_3 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Not an adjacent controls, so can not surround.
	 */
	@Test
	public void test_flow_notAdjacentControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_3 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_3 = shell.getChildrenControls().get(2);
		// can not surround
		assertNoSurroundManager(button_3, List.of(button_1, button_3));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Absolute layout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Single {@link ControlInfo} on {@link AbsoluteLayoutInfo}.
	 */
	@Test
	public void test_absolute_singleControl() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(10, 20, 100, 50);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// run action
		runSurround_Composite(button);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(10, 20, 100, 50);",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(0, 0, 100, 50);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Single {@link ControlInfo} on {@link Group} with {@link AbsoluteLayoutInfo}.
	 */
	@Ignore
	@Test
	public void test_absolute_singleControl_onGroup() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(10, 20, 100, 50);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = shell.getChildrenControls().get(0);
		// run action
		runSurround("org.eclipse.swt.widgets.Group", button);
		String expected1 =
				Expectations.get("(7, 5, 106, 68)", new StrValue[]{
						new StrValue("flanker-win", "(7, 7, 106, 66)"),
						new StrValue("scheglov-win", "(7, 5, 106, 68)")});
		String expected2 =
				Expectations.get("(3, 15, 100, 50)", new StrValue[]{
						new StrValue("flanker-win", "(3, 13, 100, 50)"),
						new StrValue("scheglov-kwin", "(3, 15, 100, 50)")});
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Group group = new Group(this, SWT.NONE);",
				"      group.setBounds" + expected1 + ";",
				"      {",
				"        Button button = new Button(group, SWT.NONE);",
				"        button.setBounds" + expected2 + ";",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Two {@link ControlInfo}'s on {@link AbsoluteLayoutInfo}.
	 */
	@Test
	public void test_absolute_twoControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"      button_1.setBounds(150, 50, 100, 20);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"      button_2.setBounds(10, 10, 100, 20);",
						"    }",
						"    {",
						"      Button button_3 = new Button(this, SWT.NONE);",
						"      button_3.setBounds(160, 100, 110, 50);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_3 = shell.getChildrenControls().get(2);
		// run action
		runSurround_Composite(button_1, button_3);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(150, 50, 120, 100);",
				"      {",
				"        Button button_1 = new Button(composite, SWT.NONE);",
				"        button_1.setBounds(0, 0, 100, 20);",
				"      }",
				"      {",
				"        Button button_3 = new Button(composite, SWT.NONE);",
				"        button_3.setBounds(10, 50, 110, 50);",
				"      }",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setBounds(10, 10, 100, 20);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SashForm
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SashFormSurroundTarget}.
	 */
	@Test
	public void test_SashForm_twoControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		// run action
		runSurround("org.eclipse.swt.custom.SashForm", button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      SashForm sashForm = new SashForm(this, SWT.NONE);",
				"      {",
				"        Button button_1 = new Button(sashForm, SWT.NONE);",
				"      }",
				"      {",
				"        Button button_2 = new Button(sashForm, SWT.NONE);",
				"      }",
				"      sashForm.setWeights(new int[] {1, 1});",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// [C]TabFolder
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TabFolderSurroundTarget}.
	 */
	@Test
	public void test_TabFolder_twoControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		// run action
		runSurround("org.eclipse.swt.widgets.TabFolder", button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      TabFolder tabFolder = new TabFolder(this, SWT.NONE);",
				"      {",
				"        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);",
				"        tabItem.setText('New Item');",
				"        {",
				"          Button button_1 = new Button(tabFolder, SWT.NONE);",
				"          tabItem.setControl(button_1);",
				"        }",
				"      }",
				"      {",
				"        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);",
				"        tabItem.setText('New Item');",
				"        {",
				"          Button button_2 = new Button(tabFolder, SWT.NONE);",
				"          tabItem.setControl(button_2);",
				"        }",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link CTabFolderSurroundTarget}.
	 */
	@Test
	public void test_CTabFolder_twoControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		// run action
		runSurround("org.eclipse.swt.custom.CTabFolder", button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      CTabFolder tabFolder = new CTabFolder(this, SWT.BORDER);",
				"      tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));",
				"      {",
				"        CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);",
				"        tabItem.setText('New Item');",
				"        {",
				"          Button button_1 = new Button(tabFolder, SWT.NONE);",
				"          tabItem.setControl(button_1);",
				"        }",
				"      }",
				"      {",
				"        CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);",
				"        tabItem.setText('New Item');",
				"        {",
				"          Button button_2 = new Button(tabFolder, SWT.NONE);",
				"          tabItem.setControl(button_2);",
				"        }",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ScrolledComposite
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ScrolledCompositeSurroundTarget}.
	 */
	@Test
	public void test_ScrolledComposite_oneComposite() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Composite composite = new Composite(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(0);
		// run action
		runSurround("org.eclipse.swt.custom.ScrolledComposite", composite);
		assertEditor(
				"public class Test extends Shell {",
				"  private Composite composite;",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);",
				"      scrolledComposite.setExpandHorizontal(true);",
				"      scrolledComposite.setExpandVertical(true);",
				"      {",
				"        composite = new Composite(scrolledComposite, SWT.NONE);",
				"      }",
				"      scrolledComposite.setContent(composite);",
				"      scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link ScrolledCompositeSurroundTarget}.
	 */
	@Test
	public void test_ScrolledComposite_twoControls() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		// run action
		runSurround("org.eclipse.swt.custom.ScrolledComposite", button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  private Composite composite;",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);",
				"      scrolledComposite.setExpandHorizontal(true);",
				"      scrolledComposite.setExpandVertical(true);",
				"      {",
				"        composite = new Composite(scrolledComposite, SWT.NONE);",
				"        composite.setLayout(new RowLayout(SWT.HORIZONTAL));",
				"        {",
				"          Button button_1 = new Button(composite, SWT.NONE);",
				"        }",
				"        {",
				"          Button button_2 = new Button(composite, SWT.NONE);",
				"        }",
				"      }",
				"      scrolledComposite.setContent(composite);",
				"      scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
				"    }",
				"  }",
				"}");
		// set "absolute" layout, because there was problem with implicit layout
		{
			ScrolledCompositeInfo scrolledComposite =
					(ScrolledCompositeInfo) shell.getChildrenControls().get(0);
			CompositeInfo composite = (CompositeInfo) scrolledComposite.getContent();
			AbsoluteLayoutInfo absoluteLayout = AbsoluteLayoutInfo.createExplicit(composite);
			composite.setLayout(absoluteLayout);
			assertNoErrors(shell);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GridLayout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We can not surround exposed control.
	 */
	@Test
	public void test_GridLayout_disableWhenExposed() throws Exception {
		setFileContentSrc(
				"test/MyComposite.java",
				getTestSource(
						"public class MyComposite extends Composite {",
						"  private Button button;",
						"  public MyComposite(Composite parent, int style) {",
						"    super(parent, style);",
						"    setLayout(new GridLayout());",
						"    button = new Button(this, SWT.NONE);",
						"  }",
						"  public Button getButton() {",
						"    return button;",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      MyComposite myComposite = new MyComposite(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		refresh();
		ControlInfo button = getJavaInfoByName("getButton()");
		assertNotNull(button);
		// no surround
		assertNoSurroundManager(button, List.of(button));
	}

	/**
	 * Bad: two controls on diagonal, and other control in same rectangle.
	 */
	@Test
	public void test_GridLayout_0() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_BAD = new Button(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		List<ControlInfo> buttons = getButtons(shell);
		ControlInfo button_1 = buttons.get(0);
		ControlInfo button_2 = buttons.get(2);
		// no surround
		assertNoSurroundManager(shell, List.of(button_1, button_2));
	}

	/**
	 * Good: two controls in single row, no other controls.
	 */
	@Test
	public void test_GridLayout_1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		List<ControlInfo> buttons = getButtons(shell);
		ControlInfo button_1 = buttons.get(0);
		ControlInfo button_2 = buttons.get(1);
		// run action
		runSurround_Composite(button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));",
				"      composite.setLayout(new GridLayout(2, false));",
				"      {",
				"        Button button_1 = new Button(composite, SWT.NONE);",
				"      }",
				"      {",
				"        Button button_2 = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Good: two controls on diagonal, no other controls.
	 */
	@Test
	public void test_GridLayout_2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		List<ControlInfo> buttons = getButtons(shell);
		ControlInfo button_1 = buttons.get(0);
		ControlInfo button_2 = buttons.get(1);
		// run action
		runSurround_Composite(button_1, button_2);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));",
				"      composite.setLayout(new GridLayout(2, false));",
				"      {",
				"        Button button_1 = new Button(composite, SWT.NONE);",
				"      }",
				"      new Label(composite, SWT.NONE);",
				"      new Label(composite, SWT.NONE);",
				"      {",
				"        Button button_2 = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Good: three controls, one spanned horizontally.
	 */
	@Test
	public void test_GridLayout_3() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(2, false));",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"      button_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_3 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		List<ControlInfo> buttons = getButtons(shell);
		ControlInfo button_1 = buttons.get(0);
		ControlInfo button_2 = buttons.get(1);
		ControlInfo button_3 = buttons.get(2);
		// run action
		runSurround_Composite(button_1, button_2, button_3);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));",
				"      composite.setLayout(new GridLayout(2, false));",
				"      {",
				"        Button button_1 = new Button(composite, SWT.NONE);",
				"        button_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));",
				"      }",
				"      {",
				"        Button button_2 = new Button(composite, SWT.NONE);",
				"      }",
				"      {",
				"        Button button_3 = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Good: two controls on diagonal, other controls on sides.
	 */
	@Test
	public void test_GridLayout_4() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(3, false));",
						"    {",
						"      Button button_00 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_10 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_20 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_01 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_11 = new Button(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button_02 = new Button(this, SWT.NONE);",
						"    }",
						"    new Label(this, SWT.NONE);",
						"    {",
						"      Button button_22 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		List<ControlInfo> buttons = getButtons(shell);
		ControlInfo button_11 = buttons.get(4);
		ControlInfo button_22 = buttons.get(6);
		// check locations
		{
			{
				GridDataInfo gridData = GridLayoutInfo.getGridData(button_11);
				assertEquals(1, ReflectionUtils.getFieldInt(gridData, "x"));
				assertEquals(1, ReflectionUtils.getFieldInt(gridData, "y"));
			}
			{
				GridDataInfo gridData = GridLayoutInfo.getGridData(button_22);
				assertEquals(2, ReflectionUtils.getFieldInt(gridData, "x"));
				assertEquals(2, ReflectionUtils.getFieldInt(gridData, "y"));
			}
		}
		// run action
		runSurround_Composite(button_11, button_22);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(3, false));",
				"    {",
				"      Button button_00 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_10 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_20 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_01 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 2));",
				"      composite.setLayout(new GridLayout(2, false));",
				"      {",
				"        Button button_11 = new Button(composite, SWT.NONE);",
				"      }",
				"      new Label(composite, SWT.NONE);",
				"      new Label(composite, SWT.NONE);",
				"      {",
				"        Button button_22 = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"    {",
				"      Button button_02 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Initially with {@link GridLayout}, but then switch to absolute layout. So, no
	 * {@link GridLayout} surround expected.
	 */
	@Test
	public void test_GridLayout_5() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(150, 50));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getButtons(shell).get(0);
		// set absolute layout
		{
			AbsoluteLayoutInfo absoluteLayout = AbsoluteLayoutInfo.createExplicit(shell);
			shell.setLayout(absoluteLayout);
			shell.refresh();
		}
		Rectangle bounds = button.getBounds();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setBounds(5, 5, " + bounds.width + ", " + bounds.height + ");",
				"    }",
				"  }",
				"}");
		// surround with Composite, should be done using absolute layout
		runSurround_Composite(button);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(null);",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setBounds(5, 5, " + bounds.width + ", " + bounds.height + ");",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"        button.setBounds(0, 0, " + bounds.width + ", " + bounds.height + ");",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for case when super class has already controls.
	 */
	@Test
	public void test_GridLayout_leadingImplicitControls() throws Exception {
		setFileContentSrc(
				"test/MyShell.java",
				getTestSource(
						"public class MyShell extends Shell {",
						"  public MyShell() {",
						"    setLayout(new GridLayout());",
						"    new Text(this, SWT.NONE);",
						"  }",
						"  protected void checkSubclass() {",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				parseComposite(
						"public class Test extends MyShell {",
						"  public Test() {",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		GridLayoutInfo layout = (GridLayoutInfo) shell.getLayout();
		List<ControlInfo> buttons = getButtons(shell);
		ControlInfo button = buttons.get(0);
		// initial checks
		{
			IGridInfo gridInfo = layout.getGridInfo();
			assertEquals(1, gridInfo.getColumnCount());
			assertEquals(2, gridInfo.getRowCount());
			assertEquals(new Rectangle(0, 1, 1, 1), gridInfo.getComponentCells(button));
		}
		// run action
		runSurround_Composite(button);
		assertEditor(
				"public class Test extends MyShell {",
				"  public Test() {",
				"    {",
				"      Composite composite = new Composite(this, SWT.NONE);",
				"      composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));",
				"      composite.setLayout(new GridLayout(1, false));",
				"      {",
				"        Button button = new Button(composite, SWT.NONE);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * @return the {@link ControlInfo} models for {@link Button} controls.
	 */
	private static List<ControlInfo> getButtons(CompositeInfo parent) {
		List<ControlInfo> buttons = new ArrayList<>();
		for (ControlInfo control : parent.getChildrenControls()) {
			if (control.getDescription().getComponentClass().getName().equals(
					"org.eclipse.swt.widgets.Button")) {
				buttons.add(control);
			}
		}
		return buttons;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the "Surround with" {@link IMenuManager} for given {@link ControlInfo}'s.
	 */
	private static IMenuManager createSurroundManager(ObjectInfo object,
			List<? extends ObjectInfo> objects) throws Exception {
		IMenuManager resultMenuManager;
		if (objects.size() < 2) {
			resultMenuManager = getDesignerMenuManager();
			object.getBroadcastObject().addContextMenu(objects, object, resultMenuManager);
		} else {
			resultMenuManager = new MenuManager();
			// handle multi selection
			List<IMenuManager> managers = new ArrayList<>();
			for (ObjectInfo object_ : objects) {
				IMenuManager manager = getDesignerMenuManager();
				object.getBroadcastObject().addContextMenu(objects, object_, manager);
				managers.add(manager);
			}
			// select common parts
			MenuIntersector.merge(resultMenuManager, managers);
		}
		// select "Surround with" sub-menu
		return findChildMenuManager(resultMenuManager, "Surround with");
	}

	/**
	 * Asserts that there are no "Surround with" {@link IMenuManager} for given input.
	 */
	private static void assertNoSurroundManager(ObjectInfo object, List<? extends ObjectInfo> objects)
			throws Exception {
		IMenuManager surroundManager = createSurroundManager(object, objects);
		assertNull(surroundManager);
	}

	/**
	 * Runs action from "Surround with" {@link IMenuManager}.
	 */
	private static void runSurround(String actionText, ObjectInfo... objects) throws Exception {
		assertFalse(objects.length == 0);
		IMenuManager surroundManager = createSurroundManager(objects[0], List.of(objects));
		assertNotNull(surroundManager);
		IAction surroundAction = findChildAction(surroundManager, actionText);
		assertNotNull(surroundAction);
		// run action
		surroundAction.run();
	}

	/**
	 * Runs action from "Surround with" {@link IMenuManager} for
	 * <code>"org.eclipse.swt.widgets.Composite"</code>.
	 */
	private static void runSurround_Composite(ObjectInfo... objects) throws Exception {
		runSurround("org.eclipse.swt.widgets.Composite", objects);
	}
}

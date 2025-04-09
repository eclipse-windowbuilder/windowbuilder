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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractPositionInfo;
import org.eclipse.wb.internal.rcp.model.widgets.ViewFormInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Control;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

/**
 * Test for {@link ViewFormInfo}.
 *
 * @author scheglov_ke
 */
public class ViewFormTest extends RcpModelTest {
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
	 * No any children {@link ControlInfo}'s, so all "getXXX()" methods return <code>null</code>.
	 */
	@Test
	public void test_defaultProperties() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		// check default property values
		assertSame(Boolean.FALSE, viewForm.getPropertyByTitle("borderVisible").getValue());
		assertSame(Boolean.FALSE, viewForm.getPropertyByTitle("topCenterSeparate").getValue());
	}

	/**
	 * No any children {@link ControlInfo}'s, so all "getXXX()" methods return <code>null</code>.
	 */
	@Test
	public void test_childrenNo() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		// no "real" Control's
		assertNull(viewForm.getControl("setContent"));
		assertNull(viewForm.getControl("setTopLeft"));
		assertNull(viewForm.getControl("setTopCenter"));
		assertNull(viewForm.getControl("setTopRight"));
	}

	/**
	 * Test for {@link ViewFormInfo#getContent()}.
	 */
	@Test
	public void test_children() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(viewForm, SWT.NONE);",
						"      viewForm.setContent(button);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = viewForm.getChildrenControls().get(0);
		assertSame(button, viewForm.getControl("setContent"));
		// check association
		{
			CompoundAssociation compoundAssociation = (CompoundAssociation) button.getAssociation();
			List<Association> associations = compoundAssociation.getAssociations();
			Assertions.assertThat(associations).hasSize(2);
			{
				ConstructorParentAssociation constructorAssociation =
						(ConstructorParentAssociation) associations.get(0);
				assertEquals("new Button(viewForm, SWT.NONE)", constructorAssociation.getSource());
			}
			{
				InvocationChildAssociation invocationAssociation =
						(InvocationChildAssociation) associations.get(1);
				assertEquals("viewForm.setContent(button)", invocationAssociation.getSource());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Each {@link ControlInfo} text is decorated with its position method.
	 */
	@Test
	public void test_presentation_decorateText() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(viewForm, SWT.NONE);",
						"      viewForm.setContent(button);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = viewForm.getChildrenControls().get(0);
		assertEquals("setContent - button", ObjectInfo.getText(button));
	}

	/**
	 * Even when no "real" {@link ControlInfo} children, tree still has {@link AbstractPositionInfo}
	 * placeholders.
	 */
	@Test
	public void test_AbstractPositionInfo_getChildrenTree_placeholders() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		// add new Button on "topLeft"
		{
			// no "real" Control's, but in "tree" we have position placeholder children
			List<ObjectInfo> children = viewForm.getPresentation().getChildrenTree();
			assertEquals(4, children.size());
			assertEquals(4, GenericsUtils.select(children, AbstractPositionInfo.class).size());
			// prepare "topLeft" position
			AbstractPositionInfo positionTopLeft = (AbstractPositionInfo) children.get(0);
			assertSame(viewForm, positionTopLeft.getComposite());
			assertSame(null, positionTopLeft.getControl());
			// check AbstractPosition_Info presentation
			{
				IObjectPresentation presentation = positionTopLeft.getPresentation();
				assertNotNull(presentation.getIcon());
				assertEquals("setTopLeft", presentation.getText());
			}
			// create
			ControlInfo button = BTestUtils.createButton();
			positionTopLeft.command_CREATE(button);
			assertSame(button, positionTopLeft.getControl());
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new FillLayout());",
					"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(viewForm, SWT.NONE);",
					"      viewForm.setTopLeft(button);",
					"    }",
					"  }",
					"}");
		}
		// move Button from "topLeft" to "content"
		{
			// no "real" Control's, but in "tree" we have position placeholder children
			List<ObjectInfo> children = viewForm.getPresentation().getChildrenTree();
			assertEquals(4, children.size());
			assertInstanceOf(ControlInfo.class, children.get(0));
			assertEquals(3, GenericsUtils.select(children, AbstractPositionInfo.class).size());
			// prepare "content" position
			AbstractPositionInfo positionContent = (AbstractPositionInfo) children.get(3);
			assertEquals("setContent", ObjectInfo.getText(positionContent));
			//
			ControlInfo button = viewForm.getControl("setTopLeft");
			positionContent.command_MOVE(button);
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new FillLayout());",
					"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
					"    {",
					"      Button button = new Button(viewForm, SWT.NONE);",
					"      viewForm.setContent(button);",
					"    }",
					"  }",
					"}");
		}
	}

	/**
	 * "Tree" children of {@link ViewFormInfo} should be sorted in same order as "set" methods array
	 * passed to constructor.
	 */
	@Test
	public void test_AbstractPositionInfo_getChildrenTree_sortChildren() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(viewForm, SWT.NONE);",
						"      viewForm.setTopCenter(button);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = viewForm.getChildrenControls().get(0);
		// check "tree" children
		List<ObjectInfo> children = viewForm.getPresentation().getChildrenTree();
		assertEquals(4, children.size());
		// index: 0
		{
			AbstractPositionInfo position = (AbstractPositionInfo) children.get(0);
			assertEquals("setTopLeft", ObjectInfo.getText(position));
		}
		// index: 1
		assertSame(button, children.get(1));
		// index: 2
		{
			AbstractPositionInfo position = (AbstractPositionInfo) children.get(2);
			assertEquals("setTopRight", ObjectInfo.getText(position));
		}
		// index: 3
		{
			AbstractPositionInfo position = (AbstractPositionInfo) children.get(3);
			assertEquals("setContent", ObjectInfo.getText(position));
		}
	}

	/**
	 * Single {@link Control} used in two positions - show it only on first one.
	 */
	@Test
	public void test_AbstractPositionInfo_getChildrenTree_sortChildren_dups() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    Button button = new Button(viewForm, SWT.NONE);",
						"    viewForm.setTopLeft(button);",
						"    viewForm.setTopCenter(button);",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = viewForm.getChildrenControls().get(0);
		// check "tree" children
		List<ObjectInfo> children = viewForm.getPresentation().getChildrenTree();
		assertEquals(4, children.size());
		assertSame(children.get(0), button);
		assertInstanceOf(AbstractPositionInfo.class, children.get(1));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ViewFormInfo#command_CREATE(ControlInfo, String)}.
	 */
	@Test
	public void test_CREATE() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		//
		ControlInfo button = BTestUtils.createButton();
		viewForm.command_CREATE(button, "setContent");
		assertSame(button, viewForm.getControl("setContent"));
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(viewForm, SWT.NONE);",
				"      viewForm.setContent(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.<br>
	 * "Move" into different position.
	 */
	@Test
	public void test_MOVE_1() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(viewForm, SWT.NONE);",
						"      viewForm.setContent(button);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = viewForm.getChildrenControls().get(0);
		// initially "button" has position "setContent"
		assertSame(button, viewForm.getControl("setContent"));
		// do move
		viewForm.command_MOVE(button, "setTopLeft");
		// now "button" is in "topLeft"
		assertNull(viewForm.getControl("setContent"));
		assertSame(button, viewForm.getControl("setTopLeft"));
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(viewForm, SWT.NONE);",
				"      viewForm.setTopLeft(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.<br>
	 * Move into {@link ViewFormInfo}.
	 */
	@Test
	public void test_MOVE_2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = shell.getChildrenControls().get(1);
		//
		viewForm.command_MOVE(button, "setTopLeft");
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(viewForm, SWT.NONE);",
				"      viewForm.setTopLeft(button);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.<br>
	 * Move from {@link ViewFormInfo}.
	 */
	@Test
	public void test_MOVE_3() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button = new Button(viewForm, SWT.NONE);",
						"      viewForm.setTopLeft(button);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button = viewForm.getChildrenControls().get(0);
		//
		shell.getLayout().command_MOVE(button, null);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link ViewFormInfo#command_MOVE(ControlInfo, String)}.<br>
	 * After moving of {@link ControlInfo}'s into new position it should be places in same order, as
	 * "set" methods.
	 */
	@Test
	public void test_MOVE_4() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(viewForm, SWT.NONE);",
						"      viewForm.setTopCenter(button_1);",
						"    }",
						"    {",
						"      Button button_2 = new Button(viewForm, SWT.NONE);",
						"      viewForm.setTopRight(button_2);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ViewFormInfo viewForm = (ViewFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button_2 = viewForm.getChildrenControls().get(1);
		//
		viewForm.command_MOVE(button_2, "setTopLeft");
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    ViewForm viewForm = new ViewForm(this, SWT.NONE);",
				"    {",
				"      Button button_2 = new Button(viewForm, SWT.NONE);",
				"      viewForm.setTopLeft(button_2);",
				"    }",
				"    {",
				"      Button button_1 = new Button(viewForm, SWT.NONE);",
				"      viewForm.setTopCenter(button_1);",
				"    }",
				"  }",
				"}");
	}
}
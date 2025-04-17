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
package org.eclipse.wb.tests.designer.rcp.model.layout.form;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormAttachmentInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormDataInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link FormLayoutInfo}.
 *
 * @author mitin_aa
 */
public class FormLayoutModelsTest extends RcpModelTest {
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
	 * There was problem that {@link FormLayoutInfo} tries to get preferences from {@link GlobalState}
	 * which was empty at that time. We need alternative implementation.
	 */
	@Test
	public void test_emptyGlobalState() throws Exception {
		parseComposite(
				"public class Test {",
				"  public Test(Composite parent) {",
				"    FormLayout formLayout = new FormLayout();",
				"    Composite composite = new Composite(parent, SWT.NONE);",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_attachWidgetSequientially() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    {",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 50);",
						"      button.setLayoutData(data);",
						"    }",
						"    {",
						"      Button button2 = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.right = new FormAttachment(100, -10);",
						"      button2.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
		ControlInfo button = shell.getChildrenControls().get(0);
		ControlInfo button2 = shell.getChildrenControls().get(1);
		//
		((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).attachWidgetSequientially(
				button2,
				button,
				PositionConstants.LEFT,
				6);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    Button button = new Button(this, SWT.NONE);",
				"    {",
				"      FormData data = new FormData();",
				"      data.left = new FormAttachment(0, 50);",
				"      button.setLayoutData(data);",
				"    }",
				"    {",
				"      Button button2 = new Button(this, SWT.NONE);",
				"      FormData data = new FormData();",
				"      data.left = new FormAttachment(button, 6);",
				"      data.right = new FormAttachment(100, -10);",
				"      button2.setLayoutData(data);",
				"    }",
				"  }",
				"}");
		assertTrue(((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).getAttachedToWidget(
				button2,
				PositionConstants.LEFT) == button);
		{
			FormDataInfo formData = FormLayoutInfo.getFormData(button2);
			FormAttachmentInfo attachment = formData.getAttachment(PositionConstants.LEFT);
			assertSame(button, attachment.getControl());
			assertEquals(6, attachment.getOffset());
			assertEquals(SWT.RIGHT, attachment.getAlignment());
		}
	}

	@Test
	public void test_attachmentPropertyExists() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 100);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get layout and button
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		LayoutDataInfo layoutData = LayoutInfo.getLayoutData(buttonInfo);
		assertInstanceOf(FormDataInfo.class, layoutData);
		FormDataInfo formDataInfo = (FormDataInfo) layoutData;
		assertNotNull(formDataInfo.getAttachment(PositionConstants.LEFT));
		assertNotNull(formDataInfo.getAttachment(PositionConstants.RIGHT));
		assertNotNull(formDataInfo.getAttachment(PositionConstants.TOP));
		assertNotNull(formDataInfo.getAttachment(PositionConstants.BOTTOM));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FormAttachment tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_attachmentToParent() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 50);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get button and layout data
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		FormDataInfo formDataInfo = (FormDataInfo) LayoutInfo.getLayoutData(buttonInfo);
		// get left attachment
		FormAttachmentInfo leftAttachment = formDataInfo.getAttachment(PositionConstants.LEFT);
		// test
		assertEquals(50, leftAttachment.getOffset());
		assertEquals(0, leftAttachment.getNumerator());
		assertEquals(100, leftAttachment.getDenominator());
		assertNull(leftAttachment.getControl());
	}

	@Test
	public void test_attachmentToComponent() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    {",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 50);",
						"      button.setLayoutData(data);",
						"    }",
						"    {",
						"      Button button2 = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(button, 6);",
						"      button2.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get buttons and attachments
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		ControlInfo button2Info = shell.getChildrenControls().get(1);
		FormDataInfo formData2Info = (FormDataInfo) LayoutInfo.getLayoutData(button2Info);
		FormAttachmentInfo leftAttachment2 = formData2Info.getAttachment(PositionConstants.LEFT);
		assertEquals(6, leftAttachment2.getOffset());
		assertSame(buttonInfo, leftAttachment2.getControl());
	}

	@Test
	public void test_isAttached() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    {",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 50);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get buttons and attachments
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
		// tests
		assertFalse(((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).isAttached(
				buttonInfo,
				PositionConstants.RIGHT));
		assertTrue(((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).isAttached(
				buttonInfo,
				PositionConstants.LEFT));
	}

	@Test
	public void test_isAttachedToComponent() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    Button button = new Button(this, SWT.NONE);",
						"    {",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 50);",
						"      button.setLayoutData(data);",
						"    }",
						"    {",
						"      Button button2 = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(button, 6);",
						"      button2.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get buttons and attachments
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		ControlInfo button2Info = shell.getChildrenControls().get(1);
		FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
		// tests
		assertFalse(((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).getAttachedToWidget(
				buttonInfo,
				PositionConstants.LEFT) == button2Info);
		assertTrue(((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).getAttachedToWidget(
				button2Info,
				PositionConstants.LEFT) == buttonInfo);
		assertFalse(((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).getAttachedToWidget(
				button2Info,
				PositionConstants.RIGHT) == buttonInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout managing tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_delete() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 100);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new FormLayout())/ /new Button(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FormLayout} {empty} {/setLayout(new FormLayout())/}",
				"  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/ /button.setLayoutData(data)/}",
				"    {new: org.eclipse.swt.layout.FormData} {local-unique: data} {/new FormData()/ /data.left = new FormAttachment(0, 100)/ /button.setLayoutData(data)/}",
				"      (0, 100)",
				"      (none)",
				"      (none)",
				"      (none)");
		// get layout and button
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		// delete
		buttonInfo.delete();
		// test
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"  }",
				"}");
	}

	@Test
	public void test_deleteAttachment() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 100);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get layout and button
		FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		// detach
		((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).detach(
				buttonInfo,
				PositionConstants.LEFT);
		// test
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      button.setLayoutData(new FormData());",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_deleteAttachmentAndAttach() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 100);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// get layout and button
		FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
		ControlInfo buttonInfo = shell.getChildrenControls().get(0);
		// detach
		((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).detach(
				buttonInfo,
				PositionConstants.LEFT);
		((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).attachAbsolute(
				buttonInfo,
				PositionConstants.LEFT,
				10);
		// test
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.left = new FormAttachment(0, 10);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_empty() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		setFormLayout(shell, new String[]{
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"  }",
		"}"});
	}

	@Test
	public void test_changeFromGridEmpty() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"  }",
						"}");
		setFormLayout(shell, new String[]{
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"  }",
		"}"});
	}

	@Test
	public void test_changeFromGridWithData() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      GridData data = new GridData();",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		setFormLayout(shell, new String[]{
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.top = new FormAttachment(0, 5);",
				"        formData.left = new FormAttachment(0, 5);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
		"}"});
	}

	@Test
	public void test_changeFromAbsolute() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(null);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setBounds(15, 20, 50, button.computeSize(-1, -1).y);",
						"    }",
						"  }",
						"}");
		setFormLayout(shell, new String[]{
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.right = new FormAttachment(0, 65);",
				"        formData.top = new FormAttachment(0, 20);",
				"        formData.left = new FormAttachment(0, 15);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
		"}"});
	}

	@Test
	public void test_changeToGridWithData() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 0);",
						"      button.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		setGridLayout(shell, new String[]{
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout(1, false));",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
		"}"});
	}

	/**
	 * Test for {@link FormAttachmentInfo#getAlignment()}.
	 * <p>
	 */
	@Test
	public void test_FormAttachment_getAlignment_returnRealAlignmentForDefault() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private Button button_1;",
						"  private Button button_2;",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      button_1 = new Button(this, SWT.NONE);",
						"    }",
						"    {",
						"      button_2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data_2 = new FormData();",
						"        data_2.left = new FormAttachment(button_1, 5);",
						"        button_2.setLayoutData(data_2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button_1 = shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		//
		FormDataInfo formData = FormLayoutInfo.getFormData(button_2);
		FormAttachmentInfo attachment = formData.getAttachment(PositionConstants.LEFT);
		assertSame(button_1, attachment.getControl());
		assertEquals(5, attachment.getOffset());
		assertEquals(SWT.RIGHT, attachment.getAlignment());
	}

	/**
	 * Test for {@link FormLayoutInfo#setExplicitSize(AbstractComponentInfo, int, int, int)}.
	 */
	@Test
	public void test_setExplicitSize() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private Button button_1;",
						"  private Button button_2;",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      button_1 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data_1 = new FormData();",
						"        data_1.left = new FormAttachment(0, 50);",
						"        button_1.setLayoutData(data_1);",
						"      }",
						"    }",
						"    {",
						"      button_2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data_2 = new FormData();",
						"        data_2.left = new FormAttachment(button_1, 5);",
						"        button_2.setLayoutData(data_2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		//
		((FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl()).setExplicitSize(
				button_2,
				PositionConstants.LEFT,
				PositionConstants.RIGHT,
				10);
		assertEditor(
				"public class Test extends Shell {",
				"  private Button button_1;",
				"  private Button button_2;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button_1 = new Button(this, SWT.NONE);",
				"      {",
				"        FormData data_1 = new FormData();",
				"        data_1.left = new FormAttachment(0, 50);",
				"        button_1.setLayoutData(data_1);",
				"      }",
				"    }",
				"    {",
				"      button_2 = new Button(this, SWT.NONE);",
				"      {",
				"        FormData data_2 = new FormData();",
				"        data_2.right = new FormAttachment(button_1, " + (5 + 75 + 10) + ", SWT.RIGHT);",
				"        data_2.left = new FormAttachment(button_1, 5);",
				"        button_2.setLayoutData(data_2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test for copy/paste.
	//
	////////////////////////////////////////////////////////////////////////////
	@Ignore
	@Test
	public void test_clipboard() throws Exception {
		CompositeInfo composite =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Composite inner = new Composite(this, SWT.NONE);",
						"    inner.setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(inner, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.left = new FormAttachment(0, 50);",
						"      data.top = new FormAttachment(20);",
						"      button.setLayoutData(data);",
						"    }",
						"    {",
						"      Button button2 = new Button(inner, SWT.NONE);",
						"      FormData data = new FormData();",
						"      data.right = new FormAttachment(100, -10);",
						"      button2.setLayoutData(data);",
						"    }",
						"  }",
						"}");
		composite.refresh();
		// prepare memento
		JavaInfoMemento memento;
		{
			ControlInfo inner = composite.getChildrenControls().get(0);
			memento = JavaInfoMemento.createMemento(inner);
		}
		// add copy
		ControlInfo copy = (ControlInfo) memento.create(composite);
		composite.getLayout().command_CREATE(copy, null);
		memento.apply();
		String[] lines =
			{
					"public class Test extends Shell {",
					"  public Test() {",
					"    setLayout(new RowLayout());",
					"    Composite inner = new Composite(this, SWT.NONE);",
					"    inner.setLayout(new FormLayout());",
					"    {",
					"      Button button = new Button(inner, SWT.NONE);",
					"      FormData data = new FormData();",
					"      data.left = new FormAttachment(0, 50);",
					"      data.top = new FormAttachment(20);",
					"      button.setLayoutData(data);",
					"    }",
					"    {",
					"      Button button2 = new Button(inner, SWT.NONE);",
					"      FormData data = new FormData();",
					"      data.right = new FormAttachment(100, -10);",
					"      button2.setLayoutData(data);",
					"    }",
					"    {",
					"      Composite composite = new Composite(this, SWT.NONE);",
					"      composite.setLayout(new FormLayout());",
					"      {",
					"        Button button = new Button(composite, SWT.NONE);",
					"        {",
					"          FormData formData = new FormData();",
					"          formData.top = new FormAttachment(20);",
					"          formData.left = new FormAttachment(0, 50);",
					"          button.setLayoutData(formData);",
					"        }",
					"      }",
					"      {",
					"        Button button = new Button(composite, SWT.NONE);",
					"        {",
					"          FormData formData = new FormData();",
					"          formData.right = new FormAttachment(100, -10);",
					"          button.setLayoutData(formData);",
					"        }",
					"      }",
					"    }",
					"  }",
			"}"};
		assertEditor(lines);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link FormLayout} for given {@link CompositeInfo}.
	 */
	private void setFormLayout(CompositeInfo composite, String[] expectedLines) throws Exception {
		composite.getRoot().refresh();
		// set FormLayout
		FormLayoutInfo formLayout =
				(FormLayoutInfo) BTestUtils.createLayout("org.eclipse.swt.layout.FormLayout");
		composite.setLayout(formLayout);
		// check source
		assertEditor(expectedLines);
	}

	/**
	 * Sets the {@link GridLayout} for given {@link CompositeInfo}.
	 */
	private void setGridLayout(CompositeInfo composite, String[] expectedLines) throws Exception {
		composite.getRoot().refresh();
		// set GridLayout
		GridLayoutInfo gridLayout =
				(GridLayoutInfo) BTestUtils.createLayout("org.eclipse.swt.layout.GridLayout");
		composite.setLayout(gridLayout);
		// check source
		assertEditor(expectedLines);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// custom Button
	//
	////////////////////////////////////////////////////////////////////////////
	private void prepareComponent() throws Exception {
		prepareComponent(75, 25);
	}

	private void prepareComponent(int width, int height) throws Exception {
		setFileContentSrc(
				"test/Button.java",
				getTestSource(
						"public class Button extends org.eclipse.swt.widgets.Button {",
						"  public Button(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  protected void checkSubclass () {",
						"  }",
						"  public Point computeSize (int wHint, int hHint, boolean changed) {",
						"    return new Point(" + width + ", " + height + ");",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
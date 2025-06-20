/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.rcp.model.jface.ControlDecorationInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.fieldassist.ControlDecoration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ControlDecorationInfo}.
 *
 * @author scheglov_ke
 */
public class ControlDecorationTest extends RcpModelTest {
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
	@Test
	public void test_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.jface.fieldassist.ControlDecoration;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    Text text = new Text(this, SWT.BORDER);",
						"    {",
						"      ControlDecoration controlDecoration = new ControlDecoration(text, SWT.LEFT | SWT.TOP);",
						"      controlDecoration.setDescriptionText(\"My description\");",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /new Text(this, SWT.BORDER)/}",
				"  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
				"  {new: org.eclipse.swt.widgets.Text} {local-unique: text} {/new Text(this, SWT.BORDER)/ /new ControlDecoration(text, SWT.LEFT | SWT.TOP)/}",
				"    {new: org.eclipse.jface.fieldassist.ControlDecoration} {local-unique: controlDecoration} {/new ControlDecoration(text, SWT.LEFT | SWT.TOP)/ /controlDecoration.setDescriptionText('My description')/}",
				"    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
		ControlInfo text = shell.getChildrenControls().get(0);
		ControlDecorationInfo decoration = text.getChildren(ControlDecorationInfo.class).get(0);
		assertSame(text, decoration.getControl());
	}

	/**
	 * Set real image for {@link ControlDecoration}, so it is used in UI and in presentation.
	 */
	@Disabled
	@Test
	public void test_existingImage() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.jface.fieldassist.ControlDecoration;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    Text text = new Text(this, SWT.BORDER);",
						"    {",
						"      ControlDecoration controlDecoration = new ControlDecoration(text, SWT.LEFT | SWT.TOP);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo text = shell.getChildrenControls().get(0);
		ControlDecorationInfo decoration = text.getChildren(ControlDecorationInfo.class).get(0);
		// add SWTResourceManager and set Image
		{
			ManagerUtils.ensure_SWTResourceManager(shell);
			GenericProperty imageProperty = (GenericProperty) decoration.getPropertyByTitle("image");
			imageProperty.setExpression(
					"org.eclipse.wb.swt.SWTResourceManager.getImage(Test.class, \"/org/eclipse/jface/fieldassist/images/contassist_ovr.gif\")",
					Property.UNKNOWN_VALUE);
		}
		// check bounds
		assertEquals(new Rectangle(-9, -2, 7, 8), decoration.getModelBounds());
		// check presentation
		{
			IObjectPresentation presentation = decoration.getPresentation();
			assertFalse(UiUtils.equals(presentation.getIcon(), decoration.getDescription().getIcon()));
		}
	}

	/**
	 * When no "real" image for {@link ControlDecoration}, set "virtual", just to show something as
	 * {@link ControlDecoration} in UI. But presentation should use default icon of
	 * {@link ControlDecoration}.
	 */
	@Test
	public void test_virtualImage() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.jface.fieldassist.ControlDecoration;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    Text text = new Text(this, SWT.BORDER);",
						"    {",
						"      ControlDecoration controlDecoration = new ControlDecoration(text, SWT.LEFT | SWT.TOP);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo text = shell.getChildrenControls().get(0);
		ControlDecorationInfo decoration = text.getChildren(ControlDecorationInfo.class).get(0);
		// check bounds
		if (EnvironmentUtils.IS_WINDOWS) {
			assertEquals(new Rectangle(-9, -2, 7, 8), decoration.getModelBounds());
		} else {
			assertEquals(new Rectangle(-16, -1, 7, 8), decoration.getModelBounds());
		}
		// check presentation
		{
			IObjectPresentation presentation = decoration.getPresentation();
			assertSame(decoration.getDescription().getIcon(), presentation.getIcon());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ControlDecorationInfo#command_CREATE(ControlInfo)}.
	 */
	@Test
	public void test_CREATE() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.jface.fieldassist.ControlDecoration;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    Text text = new Text(this, SWT.BORDER);",
						"  }",
						"}");
		shell.refresh();
		ControlInfo text = shell.getChildrenControls().get(0);
		//
		ControlDecorationInfo decoration =
				createJavaInfo("org.eclipse.jface.fieldassist.ControlDecoration");
		decoration.command_CREATE(text);
		assertEditor(
				"import org.eclipse.jface.fieldassist.ControlDecoration;",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout());",
				"    Text text = new Text(this, SWT.BORDER);",
				"    {",
				"      ControlDecoration controlDecoration = new ControlDecoration(text, SWT.LEFT | SWT.TOP);",
				"      controlDecoration.setDescriptionText('Some description');",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /new Text(this, SWT.BORDER)/}",
				"  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
				"  {new: org.eclipse.swt.widgets.Text} {local-unique: text} {/new Text(this, SWT.BORDER)/ /new ControlDecoration(text, SWT.LEFT | SWT.TOP)/}",
				"    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
				"    {new: org.eclipse.jface.fieldassist.ControlDecoration} {local-unique: controlDecoration} {/new ControlDecoration(text, SWT.LEFT | SWT.TOP)/ /controlDecoration.setDescriptionText('Some description')/}");
	}

	/**
	 * Test for {@link ControlDecorationInfo#command_ADD(ControlInfo)}.
	 */
	@Test
	public void test_ADD() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.jface.fieldassist.ControlDecoration;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout());",
						"    {",
						"      Text text_1 = new Text(this, SWT.BORDER);",
						"      {",
						"        ControlDecoration controlDecoration = new ControlDecoration(text_1, SWT.LEFT | SWT.TOP);",
						"      }",
						"    }",
						"    {",
						"      Text text_2 = new Text(this, SWT.BORDER);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo text_1 = shell.getChildrenControls().get(0);
		ControlInfo text_2 = shell.getChildrenControls().get(1);
		ControlDecorationInfo decoration = text_1.getChildren(ControlDecorationInfo.class).get(0);
		//
		decoration.command_ADD(text_2);
		assertEditor(
				"import org.eclipse.jface.fieldassist.ControlDecoration;",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout());",
				"    {",
				"      Text text_1 = new Text(this, SWT.BORDER);",
				"    }",
				"    {",
				"      Text text_2 = new Text(this, SWT.BORDER);",
				"      {",
				"        ControlDecoration controlDecoration = new ControlDecoration(text_2, SWT.LEFT | SWT.TOP);",
				"      }",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /new Text(this, SWT.BORDER)/ /new Text(this, SWT.BORDER)/}",
				"  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
				"  {new: org.eclipse.swt.widgets.Text} {local-unique: text_1} {/new Text(this, SWT.BORDER)/}",
				"    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
				"  {new: org.eclipse.swt.widgets.Text} {local-unique: text_2} {/new Text(this, SWT.BORDER)/}",
				"    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
				"    {new: org.eclipse.jface.fieldassist.ControlDecoration} {local-unique: controlDecoration} {/new ControlDecoration(text_2, SWT.LEFT | SWT.TOP)/}");
	}
}
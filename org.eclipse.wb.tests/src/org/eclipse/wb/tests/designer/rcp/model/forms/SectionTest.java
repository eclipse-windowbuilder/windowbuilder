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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.rcp.model.forms.SectionInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Section;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SectionInfo}.
 *
 * @author scheglov_ke
 */
public class SectionTest extends AbstractFormsTest {
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
	public void test_properties() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Section composite = new Section(this, Section.TWISTIE);",
						"  }",
						"}");
		shell.refresh();
		SectionInfo composite = (SectionInfo) shell.getChildrenControls().get(0);
		assertNotNull(composite.getPropertyByTitle("SectionStyle"));
	}

	/**
	 * Section has method "getDescriptionControl()", so when <code>DESCRIPTION</code> style is used,
	 * it returns some {@link Control}. But we don't want it, because this is implementation details.
	 */
	@Test
	public void test_getDescriptionControl() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    Section composite = new Section(this, Section.DESCRIPTION);",
						"  }",
						"}");
		shell.refresh();
		SectionInfo composite = (SectionInfo) shell.getChildrenControls().get(0);
		// no any children expected - only possible child is exposed using getDescriptionControl()
		assertEquals(0, composite.getChildrenControls().size());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Zero size
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sometimes {@link Section} size is zero, for example when it does not fit into form, or absolute
	 * layout and no size set. When size is zero, {@link Section} fails to paint itself, because it
	 * creates internally {@link Image} with its own size.
	 */
	@Test
	public void test_zeroSize_absoluteLayout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Section section = new Section(this, Section.TITLE_BAR);",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
	}

	/**
	 * Sometimes {@link Section} size is zero, for example when it does not fit into form, or absolute
	 * layout and no size set. When size is zero, {@link Section} fails to paint itself, because it
	 * creates internally {@link Image} with its own size.
	 */
	@Test
	public void test_zeroSize_GridLayout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new GridLayout(1, false));",
						"    setSize(450, 300);",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      button.setLayoutData(new GridData(100, 500));",
						"    }",
						"    {",
						"      Section section = new Section(this, Section.TITLE_BAR);",
						"      section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));",
						"    }",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
	}
}
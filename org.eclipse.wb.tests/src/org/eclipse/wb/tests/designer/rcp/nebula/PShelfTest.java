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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.nebula.pshelf.PShelfInfo;
import org.eclipse.wb.internal.rcp.nebula.pshelf.PShelfItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.pshelf.PShelf;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PShelfInfo}.
 *
 * @author sablin_aa
 */
public class PShelfTest extends AbstractNebulaTest {
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
	 * General test {@link PShelfInfo}.
	 */
	@Test
	public void test_General() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.pshelf.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    PShelf shelf = new PShelf(this, SWT.NONE);",
						"    {",
						"        PShelfItem item = new PShelfItem(shelf, SWT.NONE);",
						"        item.getBody().setLayout(new GridLayout());",
						"        {",
						"            Button button = new Button(item.getBody(), SWT.NONE);",
						"            button.setText('Button');",
						"        }",
						"    }",
						"    {",
						"        PShelfItem item = new PShelfItem(shelf, SWT.NONE);",
						"    }",
						"  }",
						"}");
		// refresh() also should be successful
		shell.refresh();
		// info
		PShelfInfo pshelf = shell.getChildren(PShelfInfo.class).get(0);
		assertEquals(2, pshelf.getChildren().size());
		// widget
		PShelf pshelfObj = pshelf.getWidget();
		Rectangle pshelfBounds = new Rectangle(pshelfObj.getBounds());
		int itemHeight = ReflectionUtils.getFieldInt(pshelfObj, "itemHeight");
		// check first item (opened)
		{
			PShelfItemInfo item = pshelf.getChildren(PShelfItemInfo.class).get(0);
			// "item" should have some not empty bounds (test for PShelfItem_Info.fixBodyBounds())
			Rectangle bounds = item.getBounds();
			assertEquals(bounds.width, pshelfBounds.width);
			Assertions.assertThat(bounds.height).isGreaterThan(itemHeight);
			{
				// check inner composite
				CompositeInfo composite = item.getChildren(CompositeInfo.class).get(0);
				assertTrue(composite.hasLayout());
				Rectangle compositeBounds = composite.getBounds();
				assertEquals(compositeBounds.width, bounds.width);
				assertEquals(compositeBounds.height, bounds.height - itemHeight);
			}
		}
		// check second item (closed)
		{
			PShelfItemInfo item = pshelf.getChildren(PShelfItemInfo.class).get(1);
			// "item" should have some not empty bounds (test for PShelfItem_Info.fixBodyBounds())
			Rectangle bounds = item.getBounds();
			assertEquals(bounds.width, pshelfBounds.width);
			assertEquals(bounds.height, itemHeight);
		}
	}
}
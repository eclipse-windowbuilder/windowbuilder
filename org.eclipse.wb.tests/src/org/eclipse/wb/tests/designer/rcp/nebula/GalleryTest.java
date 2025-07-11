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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.eclipse.wb.internal.rcp.nebula.gallery.GalleryInfo;
import org.eclipse.wb.internal.rcp.nebula.gallery.GalleryItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.draw2d.geometry.Rectangle;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link GalleryInfo}.
 *
 * @author sablin_aa
 */
public class GalleryTest extends AbstractNebulaTest {
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
	 * General test {@link GalleryInfo}.
	 */
	@Test
	public void test_General() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.gallery.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Gallery gallery = new Gallery(this, SWT.NONE);",
						"    gallery.setGroupRenderer(new DefaultGalleryGroupRenderer());",
						"    gallery.setItemRenderer(new DefaultGalleryItemRenderer());",
						"    {",
						"        GalleryItem galleryGroup = new GalleryItem(gallery, SWT.NONE);",
						"        galleryGroup.setExpanded(true);",
						"        {",
						"            GalleryItem galleryItem = new GalleryItem(galleryGroup, SWT.NONE);",
						"        }",
						"    }",
						"  }",
						"}");
		// refresh() also should be successful
		shell.refresh();
		// info
		GalleryInfo gallery = shell.getChildren(GalleryInfo.class).get(0);
		assertEquals(1, gallery.getChildren().size());
		// check orientation
		assertTrue(gallery.isHorizontal());
		// check items
		GalleryItemInfo galleryGroup = gallery.getChildren(GalleryItemInfo.class).get(0);
		assertTrue(galleryGroup.isGroupItem());
		assertEquals(1, galleryGroup.getChildren().size());
		// "item" should have some not empty bounds (test for GalleryItem_Info.refresh_fetch())
		{
			Rectangle bounds = galleryGroup.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(15);
			Assertions.assertThat(bounds.height).isGreaterThan(50);
		}
		// check orientation group item
		assertFalse(galleryGroup.isHorizontal());
		// check picture item
		GalleryItemInfo galleryItem = galleryGroup.getChildren(GalleryItemInfo.class).get(0);
		assertFalse(galleryItem.isGroupItem());
		// "item" should have some not empty bounds (test for GalleryItem_Info.refresh_fetch())
		{
			Rectangle bounds = galleryItem.getBounds();
			Assertions.assertThat(bounds.width).isGreaterThan(20);
			Assertions.assertThat(bounds.height).isGreaterThan(20);
		}
		// check orientation picture item
		assertFalse(galleryItem.isHorizontal());
	}

	/**
	 * Test isHorizontal() {@link GalleryInfo}.
	 */
	@Test
	public void test_isHorizontal() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.nebula.widgets.gallery.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Gallery gallery = new Gallery(this, SWT.NONE);",
						"    gallery.setGroupRenderer(new DefaultGalleryGroupRenderer());",
						"    gallery.setItemRenderer(new DefaultGalleryItemRenderer());",
						"    gallery.setVertical(true);",
						"    {",
						"        GalleryItem galleryGroup = new GalleryItem(gallery, SWT.NONE);",
						"        galleryGroup.setExpanded(true);",
						"        {",
						"            GalleryItem subGalleryItem = new GalleryItem(galleryGroup, SWT.NONE);",
						"        }",
						"    }",
						"  }",
						"}");
		// info
		GalleryInfo gallery = shell.getChildren(GalleryInfo.class).get(0);
		// check orientation
		assertFalse(gallery.isHorizontal());
		// check items orientations
		GalleryItemInfo galleryGroup = gallery.getChildren(GalleryItemInfo.class).get(0);
		// check orientation group item
		assertTrue(galleryGroup.isHorizontal());
		// check orientation picture item
		GalleryItemInfo galleryItem = galleryGroup.getChildren(GalleryItemInfo.class).get(0);
		assertTrue(galleryItem.isHorizontal());
	}
}
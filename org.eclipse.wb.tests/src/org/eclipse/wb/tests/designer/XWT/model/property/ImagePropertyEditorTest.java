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
package org.eclipse.wb.tests.designer.XWT.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.property.editor.ImagePropertyEditor;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test for {@link ImagePropertyEditor}.
 *
 * @author scheglov_ke
 */
public class ImagePropertyEditorTest extends XwtModelTest {
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
	// getText()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getText_noValue() throws Exception {
		XmlObjectInfo shell = parse("<Shell/>");
		refresh();
		Property property = shell.getPropertyByTitle("image");
		// check state
		assertFalse(property.isModified());
		assertEquals(null, getPropertyText(property));
		assertEquals(null, getPropertyClipboardSource(property));
	}

	@Test
	public void test_getText_hasValue() throws Exception {
		XmlObjectInfo shell = parse("<Shell image='my.png'/>");
		refresh();
		Property property = shell.getPropertyByTitle("image");
		// check state
		assertTrue(property.isModified());
		assertEquals("my.png", getPropertyText(property));
		assertEquals("my.png", getPropertyClipboardSource(property));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dialog
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_dialog_inClasspath() throws Exception {
		XmlObjectInfo shell = parse("<Shell/>");
		refresh();
		final Property property = shell.getPropertyByTitle("image");
		// change value
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				openPropertyDialog(property);
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				animateClasspathImageSelection(context, "stop.gif");
			}
		});
		assertXML("<Shell image='/org/eclipse/jface/action/images/stop.gif'/>");
	}

	@DisposeProjectAfter
	@Test
	public void test_dialog_inSubPackage() throws Exception {
		TestUtils.createImagePNG(m_testProject, "src/test/images/testImage.png", 16, 16);
		// parse
		XmlObjectInfo shell = parse("<Shell/>");
		refresh();
		final Property property = shell.getPropertyByTitle("image");
		// change value
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				openPropertyDialog(property);
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				animateClasspathImageSelection(context, "testImage.png");
			}
		});
		assertXML("<Shell image='images/testImage.png'/>");
	}

	@DisposeProjectAfter
	@Test
	public void test_dialog_openWith_inSubPackage() throws Exception {
		TestUtils.createImagePNG(m_testProject, "src/test/images/testImage.png", 16, 16);
		// parse
		XmlObjectInfo shell = parse("<Shell image='images/testImage.png'/>");
		refresh();
		final Property property = shell.getPropertyByTitle("image");
		// change value
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				openPropertyDialog(property);
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.useShell("Image chooser");
				// testImage.png is selected
				{
					TreeItem treeItem = context.getTreeItem("testImage.png");
					assertNotNull(treeItem);
					Assertions.assertThat(treeItem.getParent().getSelection()).containsOnly(treeItem);
				}
				// select "Default image"
				{
					Button button = context.getButtonByTextPrefix("Default image");
					context.selectButton(button);
				}
				// done
				context.clickButton("OK");
			}
		});
		assertXML("<Shell/>");
	}

	@DisposeProjectAfter
	@Test
	public void test_dialog_inSourceFolder() throws Exception {
		TestUtils.createImagePNG(m_testProject, "src/images/testImage.png", 16, 16);
		// parse
		XmlObjectInfo shell =
				parse0(
						"src",
						getTestSource(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"<Shell/>"));
		refresh();
		final Property property = shell.getPropertyByTitle("image");
		// change value
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				openPropertyDialog(property);
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				animateClasspathImageSelection(context, "testImage.png");
			}
		});
		assertXML("<Shell image='images/testImage.png'/>");
	}

	private static void animateClasspathImageSelection(UiContext context, String imageName) {
		context.useShell("Image chooser");
		// select image from classpath
		{
			Button button = context.getButtonByTextPrefix("Classpath resource");
			context.selectButton(button);
		}
		// expand all, so allow to find and select image by name
		{
			Tree tree = context.findFirstWidget(Tree.class);
			((TreeViewer) tree.getData("org.eclipse.jface.viewers.TreeViewer")).expandAll();
		}
		// select image
		{
			TreeItem imageItem = context.getTreeItem(imageName);
			UiContext.setSelection(imageItem);
		}
		// done
		context.clickButton("OK");
	}
}
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
package org.eclipse.wb.tests.designer.rcp.model.util;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import org.junit.Test;

/**
 * Test for {@link Activator_getImages_ByteCodeProcessor}.
 *
 * @author lobas_av
 */
public class ActivatorImagesTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		// create activator
		setFileContentSrc(
				"testplugin/Activator.java",
				getSourceDQ(
						"package testplugin;",
						"import org.eclipse.jface.resource.ImageDescriptor;",
						"import org.eclipse.swt.graphics.Image;",
						"import org.eclipse.ui.plugin.AbstractUIPlugin;",
						"public class Activator extends AbstractUIPlugin {",
						"  public Activator() {",
						"  }",
						"  public static Activator getDefault() {",
						"    return null;",
						"  }",
						"  public static ImageDescriptor getImageDescriptor(String path) {",
						"    return null;",
						"  }",
						"  public static Image getImage(String path) {",
						"    return null;",
						"  }",
						"}"));
		//
		IOUtils2.ensureFolderExists(m_testProject.getProject(), "icons");
		IOUtils2.ensureFolderExists(m_testProject.getProject(), "images");
		waitForAutoBuild();
		// copy some image to test
		{
			IFile file = getFile("icons", "1.png");
			setFileContent(file, org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
		}
		{
			IFile file = getFile("images", "2.png");
			setFileContent(file, org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
		}
		// keep resources
		forgetCreatedResources();
	}

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
	// Tests: Image
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getImage_icons() throws Exception {
		test_getImage("icons/1.png");
	}

	@Test
	public void test_getImage_win_icons() throws Exception {
		test_getImage("icons\\\\1.png");
	}

	@Test
	public void test_getImage_icons_slash() throws Exception {
		test_getImage("/icons/1.png");
	}

	@Test
	public void test_getImage_win_icons_slash() throws Exception {
		test_getImage("\\\\icons\\\\1.png");
	}

	@Test
	public void test_getImage_images() throws Exception {
		test_getImage("images/2.png");
	}

	@Test
	public void test_getImage_win_images() throws Exception {
		test_getImage("images\\\\2.png");
	}

	@Test
	public void test_getImage_images_slash() throws Exception {
		test_getImage("/images/2.png");
	}

	@Test
	public void test_getImage_win_images_slash() throws Exception {
		test_getImage("\\\\images\\\\2.png");
	}

	private void test_getImage(String path) throws Exception {
		ShellInfo shell = (ShellInfo) parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setImage(testplugin.Activator.getImage('" + path + "'));",
						"  }",
						"}");
		shell.refresh();
		Image image = shell.getImage();
		assertNotNull(image);
		assertFalse(image.isDisposed());
	}

	@Test
	public void test_getImage_wrongPath() throws Exception {
		ShellInfo shell = (ShellInfo) parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setImage(testplugin.Activator.getImage('icons/3.png'));",
						"  }",
						"}");
		shell.refresh();
		assertNull(shell.getWidget().getImage());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test: ImageDescriptor
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getImageDescriptor_icons() throws Exception {
		test_getImageDescriptor("icons/1.png", true);
	}

	@Test
	public void test_getImageDescriptor_win_icons() throws Exception {
		test_getImageDescriptor("icons\\\\1.png", true);
	}

	@Test
	public void test_getImageDescriptor_icons_slash() throws Exception {
		test_getImageDescriptor("/icons/1.png", true);
	}

	@Test
	public void test_getImageDescriptor_win_icons_slash() throws Exception {
		test_getImageDescriptor("\\\\icons\\\\1.png", true);
	}

	@Test
	public void test_getImageDescriptor_images() throws Exception {
		test_getImageDescriptor("images/2.png", true);
	}

	@Test
	public void test_getImageDescriptor_win_images() throws Exception {
		test_getImageDescriptor("images\\\\2.png", true);
	}

	@Test
	public void test_getImageDescriptor_images_slash() throws Exception {
		test_getImageDescriptor("/images/2.png", true);
	}

	@Test
	public void test_getImageDescriptor_win_images_slash() throws Exception {
		test_getImageDescriptor("\\\\images\\\\2.png", true);
	}

	@Test
	public void test_getImageDescriptor_wrongPath() throws Exception {
		test_getImageDescriptor("icons/3.png", false);
	}

	private void test_getImageDescriptor(String path, boolean checkNotNull) throws Exception {
		setFileContentSrc(
				"test/MyShell.java",
				getTestSource(
						"public class MyShell extends Shell {",
						"  private ImageDescriptor m_imageDescriptor;",
						"  public void setID(ImageDescriptor id) {",
						"    m_imageDescriptor = id;",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		ShellInfo shell = (ShellInfo) parseComposite(
						"public class Test extends MyShell {",
						"  public Test() {",
						"    setID(testplugin.Activator.getImageDescriptor('" + path + "'));",
						"  }",
						"}");
		shell.refresh();
		//
		Shell shellObject = shell.getWidget();
		Object imageDescriptor = ReflectionUtils.getFieldObject(shellObject, "m_imageDescriptor");
		if (checkNotNull) {
			assertNotNull(imageDescriptor);
		} else {
			assertNull(imageDescriptor);
		}
	}
}
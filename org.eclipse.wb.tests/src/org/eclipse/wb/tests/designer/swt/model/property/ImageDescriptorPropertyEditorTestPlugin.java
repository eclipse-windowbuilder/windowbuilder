/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImageDescriptorPropertyEditor;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImageEvaluator;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;

import org.eclipse.core.resources.IFile;

import org.junit.Test;

/**
 * Tests for {@link ImageDescriptorPropertyEditor} with plugin images.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public class ImageDescriptorPropertyEditorTestPlugin extends ImageDescriptorPropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		{
			IFile file = getFile("icons", "1.png");
			setFileContent(file, org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
		}
		setFileContentSrc(
				"testplugin/Activator.java",
				getSourceDQ(
						"package testplugin;",
						"import org.eclipse.ui.plugin.AbstractUIPlugin;",
						"public class Activator extends AbstractUIPlugin {",
						"  public Activator() {",
						"  }",
						"  public static Activator getDefault() {",
						"    return null;",
						"  }",
						"}"));
		forgetCreatedResources();
		waitForAutoBuild();
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////

	@Test
	public void test_ThisPluginImage_Eclipse_workspace() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"TestProject\", \"icons/1.png\")",
				"Plugin: TestProject icons/1.png",
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"TestProject\", \"icons/1.png\")");
	}

	@Test
	public void test_PluginImage_Eclipse() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")",
				"Plugin: org.eclipse.jdt.ui /icons/full/elcl16/ch_cancel.png",
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")");
	}

	@Test
	public void test_ThisPluginImage_OLD() throws Exception {
		ensureManagers();
		// now we have ResourceManager, so use it for image
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.ResourceManager.getPluginImageDescriptor(testplugin.Activator.getDefault(), \"icons/1.png\")",
				"Plugin: TestProject icons/1.png",
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"TestProject\", \"icons/1.png\")");
	}

	@Test
	public void test_ThisPluginImage_NEW_workspace() throws Exception {
		ensureManagers();
		// now we have ResourceManager, so use it for image
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.ResourceManager.getPluginImageDescriptor(\"TestProject\", \"icons/1.png\")",
				"Plugin: TestProject icons/1.png",
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"TestProject\", \"icons/1.png\")");
	}

	@Test
	public void test_PluginImage_NEW() throws Exception {
		ensureManagers();
		// now we have ResourceManager, so use it for image
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.ResourceManager.getPluginImageDescriptor(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")",
				"Plugin: org.eclipse.jdt.ui /icons/full/elcl16/ch_cancel.png",
				"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")");
	}

	@Test
	public void test_ThisPlugin_Value() throws Exception {
		ensureManagers();
		GenericProperty property =
				createImageDescriptorPropertyForSource(
						"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"TestProject\", \"icons/1.png\")");
		assertNotNull(property);
		assertNotNull(property.getValue());
		//
		String[] values = ImageEvaluator.getPluginImageValue(property);
		assertNotNull(values);
		assertEquals(2, values.length);
		assertEquals("TestProject", values[0]);
		assertEquals("icons/1.png", values[1]);
	}

	@Test
	public void test_Plugin_Value() throws Exception {
		ensureManagers();
		GenericProperty property =
				createImageDescriptorPropertyForSource(
						"org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")");
		assertNotNull(property);
		assertNotNull(property.getValue());
		//
		String[] values = ImageEvaluator.getPluginImageValue(property);
		assertNotNull(values);
		assertEquals(2, values.length);
		assertEquals("org.eclipse.jdt.ui", values[0]);
		assertEquals("/icons/full/elcl16/ch_cancel.png", values[1]);
	}

	private void ensureManagers() throws Exception {
		ProjectUtils.ensureResourceType(
				m_testProject.getJavaProject(),
				Activator.getDefault().getBundle(),
				"org.eclipse.wb.swt.SWTResourceManager");
		ProjectUtils.ensureResourceType(
				m_testProject.getJavaProject(),
				Activator.getDefault().getBundle(),
				"org.eclipse.wb.swt.ResourceManager");
	}
}
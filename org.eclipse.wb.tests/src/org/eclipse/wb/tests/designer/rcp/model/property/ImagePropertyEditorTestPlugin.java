/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.property;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImageEvaluator;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImagePropertyEditor;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.BundleImageContainer;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.BundleImageResource;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.FileImageContainer;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.FileImageResource;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.FilterConfigurer;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginBundleContainer;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginImagesRoot;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.ProjectImageContainer;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.core.TestProject;
import org.eclipse.wb.tests.designer.rcp.model.rcp.AbstractPdeTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for {@link ImagePropertyEditor} with plugin images.
 *
 * @author lobas_av
 */
public class ImagePropertyEditorTestPlugin extends ImagePropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject != null) {
			waitForAutoBuild();
		}
	}

	@Override
	protected void configureNewProject() throws Exception {
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		// create activator
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
		// copy some image to test
		{
			IFile file = getFile("icons", "1.png");
			setFileContent(file, org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
		}
		waitForAutoBuild();
		// keep these resources
		forgetCreatedResources();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ThisPluginImage_OLD() throws Exception {
		ensureResourceManagers();
		// now we have ResourceManager, so use it for image
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.ResourceManager.getPluginImage(testplugin.Activator.getDefault(), \"icons/1.png\")",
				"Plugin: TestProject icons/1.png",
				"org.eclipse.wb.swt.ResourceManager.getPluginImage(\"TestProject\", \"icons/1.png\")");
	}

	@Test
	public void test_ThisPluginImage_NEW_workspace() throws Exception {
		ensureResourceManagers();
		// now we have ResourceManager, so use it for image
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.ResourceManager.getPluginImage(\"TestProject\", \"icons/1.png\")",
				"Plugin: TestProject icons/1.png",
				"org.eclipse.wb.swt.ResourceManager.getPluginImage(\"TestProject\", \"icons/1.png\")");
	}

	@Test
	public void test_PluginImage_NEW() throws Exception {
		ensureResourceManagers();
		// now we have ResourceManager, so use it for image
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.ResourceManager.getPluginImage(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")",
				"Plugin: org.eclipse.jdt.ui /icons/full/elcl16/ch_cancel.png",
				"org.eclipse.wb.swt.ResourceManager.getPluginImage(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")");
	}

	@Test
	public void test_ThisPlugin_Value() throws Exception {
		ensureResourceManagers();
		GenericProperty property =
				createImagePropertyForSource("org.eclipse.wb.swt.ResourceManager.getPluginImage(\"TestProject\", \"icons/1.png\")");
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
		ensureResourceManagers();
		GenericProperty property =
				createImagePropertyForSource("org.eclipse.wb.swt.ResourceManager.getPluginImage(\"org.eclipse.jdt.ui\", \"/icons/full/elcl16/ch_cancel.png\")");
		assertNotNull(property);
		assertNotNull(property.getValue());
		//
		String[] values = ImageEvaluator.getPluginImageValue(property);
		assertNotNull(values);
		assertEquals(2, values.length);
		assertEquals("org.eclipse.jdt.ui", values[0]);
		assertEquals("/icons/full/elcl16/ch_cancel.png", values[1]);
	}

	private void ensureResourceManagers() throws Exception {
		ProjectUtils.ensureResourceType(
				m_testProject.getJavaProject(),
				Activator.getDefault().getBundle(),
				"org.eclipse.wb.swt.SWTResourceManager");
		ProjectUtils.ensureResourceType(
				m_testProject.getJavaProject(),
				Activator.getDefault().getBundle(),
				"org.eclipse.wb.swt.ResourceManager");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PluginImagesRoot
	//
	////////////////////////////////////////////////////////////////////////////
	@Ignore
	@Test
	public void test_PluginImagesRoot() throws Exception {
		FilterConfigurer filterConfigurer = new FilterConfigurer(false, false, false, false);
		PluginImagesRoot pluginImagesRoot =
				new PluginImagesRoot(m_testProject.getProject(), filterConfigurer);
		//
		IImageElement[] elements = pluginImagesRoot.elements();
		assertNotNull(elements);
		assertEquals(1, elements.length);
		assertPluginElement(elements[0], ProjectImageContainer.class, "TestProject");
		assertPluginElement(
				elements[0],
				"icons/1.png",
				FileImageContainer.class,
				FileImageResource.class);
		//
		ProjectImageContainer fileContainer = (ProjectImageContainer) elements[0];
		//
		assertNull(fileContainer.findResource("Test-Project", "/icons/1.png"));
		assertNull(fileContainer.findResource("TestProject", "/icons/1.png"));
		//
		Object[] fileData = fileContainer.findResource("TestProject", "icons/1.png");
		assertNotNull(fileData);
		assertEquals(3, fileData.length);
		assertPluginElement((IImageElement) fileData[0], ProjectImageContainer.class, "TestProject");
		assertPluginElement((IImageElement) fileData[1], FileImageContainer.class, "icons");
		assertPluginElement((IImageElement) fileData[2], FileImageResource.class, "1.png");
		//
		assertSame(elements, pluginImagesRoot.elements());
		//
		filterConfigurer.showRequiredPlugins(true);
		assertNotSame(elements, pluginImagesRoot.elements());
		//
		elements = pluginImagesRoot.elements();
		assertNotNull(elements);
		assertEquals(3, elements.length);
		assertPluginElement(elements[0], ProjectImageContainer.class, "TestProject");
		assertPluginElement(elements[1], PluginBundleContainer.class, "org.eclipse.ui");
		assertPluginElement(elements[2], PluginBundleContainer.class, "org.eclipse.ui.forms");
		//
		assertPluginElement(
				elements[1],
				"icons/full/elcl16/close_view.gif",
				BundleImageContainer.class,
				BundleImageContainer.class,
				BundleImageContainer.class,
				BundleImageResource.class);
		//
		PluginBundleContainer bundleContainer = (PluginBundleContainer) elements[1];
		//
		assertNull(bundleContainer.findResource("org.eclipse.UI", "/icons/full/elcl16/close_view.gif"));
		assertNull(bundleContainer.findResource("org.eclipse.ui", "icons/full/elcl16/close_view.gif"));
		//
		Object[] bundleData =
				bundleContainer.findResource("org.eclipse.ui", "/icons/full/elcl16/close_view.gif");
		assertNotNull(bundleData);
		assertEquals(5, bundleData.length);
		assertPluginElement(
				(IImageElement) bundleData[0],
				PluginBundleContainer.class,
				"org.eclipse.ui");
		assertPluginElement((IImageElement) bundleData[1], BundleImageContainer.class, "icons");
		assertPluginElement((IImageElement) bundleData[2], BundleImageContainer.class, "full");
		assertPluginElement((IImageElement) bundleData[3], BundleImageContainer.class, "elcl16");
		assertPluginElement((IImageElement) bundleData[4], BundleImageResource.class, "close_view.gif");
	}

	@Test
	public void test_PluginImagesRoot_RequiredWithNullPluginId() throws Exception {
		TestProject testProject = null;
		try {
			testProject = new TestProject("RequiredProject");
			PdeProjectConversionUtils.convertToPDE(testProject.getProject(), null, null);
			// copy some image to test
			{
				IFile file = getFile(testProject.getProject(), "icons", "2.png");
				setFileContent(
						file,
						org.eclipse.wb.tests.designer.tests.Activator.getFile("icons/test.png"));
			}
			waitForAutoBuild();
			PdeUtils utils = PdeUtils.get(m_testProject.getProject());
			utils.addPluginImport("RequiredProject");
			waitForAutoBuild();
			//
			FilterConfigurer filterConfigurer = new FilterConfigurer(false, true, false, false);
			PluginImagesRoot pluginImagesRoot =
					new PluginImagesRoot(m_testProject.getProject(), filterConfigurer);
			//
			IImageElement[] elements = pluginImagesRoot.elements();
			assertNotNull(elements);
			assertEquals(4, elements.length);
			assertPluginElement(elements[0], ProjectImageContainer.class, "RequiredProject");
			assertPluginElement(elements[1], ProjectImageContainer.class, "TestProject");
			assertPluginElement(elements[2], PluginBundleContainer.class, "org.eclipse.ui");
			assertPluginElement(elements[3], PluginBundleContainer.class, "org.eclipse.ui.forms");
			//
			IFile pluginXmlFile = getFile(testProject.getProject(), "plugin.xml");
			setFileContent(
					pluginXmlFile,
					"<?xml version='1.0' encoding='UTF-8'?>\r\n<?eclipse version='3.0'?>\r\n<plugin/>");
			getFile(testProject.getProject(), "META-INF/MANIFEST.MF").delete(true, null);
			waitForAutoBuild();
			// we can get IPluginModelBase using IProject, but it has no ID
			IPluginModelBase plugin = PluginRegistry.findModel(testProject.getProject());
			assertNotNull(plugin);
			assertNull(PdeUtils.getId(plugin));
			//
			filterConfigurer.showRequiredPlugins(false);
			filterConfigurer.showRequiredPlugins(true);
			//
			elements = pluginImagesRoot.elements();
			assertNotNull(elements);
			assertEquals(3, elements.length);
			assertPluginElement(elements[0], ProjectImageContainer.class, "TestProject");
			assertPluginElement(elements[1], PluginBundleContainer.class, "org.eclipse.ui");
			assertPluginElement(elements[2], PluginBundleContainer.class, "org.eclipse.ui.forms");
		} finally {
			if (testProject != null) {
				do_projectDispose();
				testProject.dispose();
			}
		}
	}

	@Test
	public void test_PluginImagesRoot_NullPluginId() throws Exception {
		do_projectDispose();
		do_projectCreate();
		try {
			ProjectUtils.addNature(m_testProject.getProject(), "org.eclipse.pde.PluginNature");
			AbstractPdeTest.createPluginXML(new String[]{
					"<?xml version='1.0' encoding='UTF-8'?>",
					"<?eclipse version='3.0'?>",
			"<plugin/>"});
			waitForAutoBuild();
			// we can get IPluginModelBase using IProject, but it has no ID
			IPluginModelBase plugin = PluginRegistry.findModel(m_testProject.getProject());
			assertNotNull(plugin);
			assertNull(PdeUtils.getId(plugin));
			//
			PluginImagesRoot pluginImagesRoot =
					new PluginImagesRoot(m_testProject.getProject(), new FilterConfigurer(false,
							false,
							false,
							false));
			//
			IImageElement[] elements = pluginImagesRoot.elements();
			assertNotNull(elements);
			assertEquals(0, elements.length);
		} finally {
			do_projectDispose();
		}
	}

	private static void assertPluginElement(IImageElement element,
			String path,
			Class<?>... elementsClasses) throws Exception {
		int index = 0;
		label : for (String name : path.split("/")) {
			IImageContainer container = (IImageContainer) element;
			for (IImageElement children : container.elements()) {
				if (name.equals(children.getName())) {
					assertPluginElement(children, elementsClasses[index++], name);
					element = children;
					continue label;
				}
			}
			fail("part '" + name + "' of " + path + " not found");
		}
	}

	private static void assertPluginElement(IImageElement element, Class<?> elementClass, String name)
			throws Exception {
		assertNotNull(element);
		assertInstanceOf(elementClass, element);
		assertEquals(name, element.getName());
		assertNotNull(element.getImage());
	}
}
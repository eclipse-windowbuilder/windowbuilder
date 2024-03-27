/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.internal.core.editor.palette.model.entry.BundleLibraryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.LibraryInfo;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaProjectTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

public class LibraryInfoTest extends AbstractJavaProjectTest {
	private static final String SYMBOLIC_NAME = "com.miglayout.swing";
	private IConfigurationElement libraryElement;
	private LibraryInfo libraryInfo;
	private IFile libraryFile;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
		libraryElement = findBundleLibrary(SYMBOLIC_NAME);
		libraryInfo = new BundleLibraryInfo(libraryElement);
		libraryFile = getBundleFile(SYMBOLIC_NAME);
	}

	/**
	 * Add bundle to the java project if it doesn't exist already.
	 */
	@Test
	public void test_addBundleLibrary() throws Exception {
		assertFalse(libraryFile.exists());
		libraryInfo.ensure(m_javaProject);
		assertTrue(libraryFile.exists());
	}

	/**
	 * Update bundle if the java project contains an outdated version.
	 */
	@Test
	public void test_updateBundle() throws Exception {
		ProjectUtils.addPluginLibraries(m_javaProject, SYMBOLIC_NAME);
		assertTrue(libraryFile.exists());

		libraryFile.setPersistentProperty(BundleLibraryInfo.VERSION, "0.0.1");
		assertEquals(libraryFile.getPersistentProperty(BundleLibraryInfo.VERSION), "0.0.1");

		libraryInfo.ensure(m_javaProject);
		assertNotEquals(libraryFile.getPersistentProperty(BundleLibraryInfo.VERSION), "0.0.1");
	}

	private IFile getBundleFile(String symbolicName) {
		Bundle bundle = Platform.getBundle(symbolicName);
		assertNotNull("Bundle not found: " + symbolicName, bundle);

		File bundleFile = FileLocator.getBundleFileLocation(bundle).orElse(null);
		assertNotNull("Bundle file not found: " + symbolicName);

		return m_javaProject.getAdapter(IProject.class).getFile(bundleFile.getName());
	}

	private static IConfigurationElement findBundleLibrary(String symbolicName) {
		List<IConfigurationElement> toolkitElements = DescriptionHelper.getToolkitElements();
		IConfigurationElement libraryElement = toolkitElements.stream() //
				.flatMap(element -> Stream.of(element.getChildren("palette"))) //
				.flatMap(element -> Stream.of(element.getChildren("component"))) //
				.flatMap(element -> Stream.of(element.getChildren("bundle-library"))) //
				.filter(element -> symbolicName.equals(element.getAttribute("symbolicName"))) //
				.findFirst() //
				.orElse(null);
		assertNotNull("bundle-library extension point not found: " + SYMBOLIC_NAME, libraryElement);
		return libraryElement;
	}
}

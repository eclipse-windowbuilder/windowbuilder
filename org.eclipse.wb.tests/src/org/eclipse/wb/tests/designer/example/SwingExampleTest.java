/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.example;

import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;

import org.eclipse.jdt.core.ICompilationUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Contains simple test cases for our examples, verifying that the components
 * can be opened in the design editor without errors.
 */
public class SwingExampleTest extends DesignerEditorTestCase {
	private static final String PATH_PREFIX = "../examples/swing";

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		do_projectCreate();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		do_projectDispose();
		super.tearDown();
	}

	@Test
	public void test_swing_kmeans() throws Exception {
		copySwingExampleFile("kMeans", "kmeans", "KMeansCluster.java");
		copySwingExampleFile("kMeans", "kmeans", "KMeansFrame.java");
		copySwingExampleFile("kMeans", "kmeans", "KMeansPanel.java");
		waitForAutoBuild();
		//
		ICompilationUnit cu = m_testProject.getCompilationUnit("kmeans.KMeansFrame");
		openDesign(cu);
	}

	private void copySwingExampleFile(String projectName, String packageName, String className) throws Exception {
		Path sourcePath = Paths.get(PATH_PREFIX, projectName, "src", packageName, className);
		List<String> sourceLines = Files.readAllLines(sourcePath);
		setFileContentSrc(packageName + '/' + className, StringUtils.join(sourceLines, System.lineSeparator()));
	}
}

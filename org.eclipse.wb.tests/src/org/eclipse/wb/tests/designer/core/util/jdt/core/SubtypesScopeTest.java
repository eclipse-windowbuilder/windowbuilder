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
package org.eclipse.wb.tests.designer.core.util.jdt.core;

import org.eclipse.wb.internal.core.utils.jdt.core.SubtypesScope;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link SubtypesScope}.
 *
 * @author scheglov_ke
 */
public class SubtypesScopeTest extends AbstractJavaTest {
	private IJavaProject javaProject;
	private IType aType;
	private IType bType;
	private IType cType;
	private SubtypesScope scope;

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
			javaProject = m_testProject.getJavaProject();
			ICompilationUnit aUnit =
					createModelCompilationUnit(
							"test",
							"A.java",
							getSourceDQ("package test;", "public class A {", "}"));
			ICompilationUnit bUnit =
					createModelCompilationUnit(
							"test",
							"B.java",
							getSourceDQ("package test;", "public class B extends A {", "}"));
			ICompilationUnit cUnit =
					createModelCompilationUnit(
							"test",
							"C.java",
							getSourceDQ("package test;", "public class C extends B {", "}"));
			aType = aUnit.findPrimaryType();
			bType = bUnit.findPrimaryType();
			cType = cUnit.findPrimaryType();
			// prepare scope for "B"
			scope = new SubtypesScope(bType);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeClass
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_enclosesType() throws Exception {
		// try not a IType
		assertFalse(scope.encloses(cType.getCompilationUnit()));
		// "C" extends "B"
		assertTrue(scope.encloses(cType));
		// "A" is not subclass, it is super-class
		assertFalse(scope.encloses(aType));
		// "B" is not subclass of itself
		assertFalse(scope.encloses(bType));
		// "List" does not belong to this hierarchy at all
		assertFalse(scope.encloses(javaProject.findType("java.util.List")));
		// "Object" is not subclass, it is super-class
		assertFalse(scope.encloses(javaProject.findType("java.lang.Object")));
	}

	@Test
	public void test_enclosesResource() throws Exception {
		assertTrue(scope.encloses("/TestProject/src/test/C.java"));
		assertFalse(scope.encloses("/TestProject/src/test/A.java"));
		assertFalse(scope.encloses("/TestProject/src/test/B.java"));
	}

	@Test
	public void test_enclosingProjectsAndJars() throws Exception {
		IPath[] paths = scope.enclosingProjectsAndJars();
		// at least JRE and project expected
		assertTrue(paths.length >= 2);
	}

	@Test
	public void test_deprecated() throws Exception {
		scope.setIncludesBinaries(true);
		scope.setIncludesClasspaths(true);
		assertTrue(scope.includesBinaries());
		assertTrue(scope.includesClasspaths());
	}

	@Test
	public void test_otherScope() throws Exception {
		SubtypesScope scope2 = new SubtypesScope(javaProject.findType("java.util.List"));
		assertTrue(scope2.encloses(javaProject.findType("java.util.ArrayList")));
		assertTrue(scope2.encloses("C:/some/path/rt.jar|java/util/ArrayList.class"));
		assertFalse(scope2.encloses("C:/some/path/rt.jar|java/util/HashMap.class"));
	}
}

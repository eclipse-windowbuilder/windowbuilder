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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.tests.designer.core.util.ast.AstTests;
import org.eclipse.wb.tests.designer.core.util.base64.Base64UtilsTest;
import org.eclipse.wb.tests.designer.core.util.check.AssertTest;
import org.eclipse.wb.tests.designer.core.util.execution.ExecutionUtilsTest;
import org.eclipse.wb.tests.designer.core.util.execution.NoOpProgressMonitorTest;
import org.eclipse.wb.tests.designer.core.util.jdt.core.JdtCoreTests;
import org.eclipse.wb.tests.designer.core.util.refactoring.RefactoringTests;
import org.eclipse.wb.tests.designer.core.util.reflect.IntrospectionHelperTest;
import org.eclipse.wb.tests.designer.core.util.reflect.ReflectionUtilsTest;
import org.eclipse.wb.tests.designer.core.util.ui.ImageUtilsTest;
import org.eclipse.wb.tests.designer.core.util.ui.MenuIntersectorTest;
import org.eclipse.wb.tests.designer.core.util.xml.XmlTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		SystemUtilTest.class,
		AssertTest.class,
		Base64UtilsTest.class,
		XmlWriterTest.class,
		BrandingUtilsTest.class,
		DesignerExceptionTest.class,
		DesignerExceptionUtilsTest.class,
		CoreExceptionRewriterTest.class,
		GenericsUtilsTest.class,
		PairTest.class,
		ExecutionUtilsTest.class,
		NoOpProgressMonitorTest.class,
		XmlTests.class,
		ExternalFactoriesHelperTest.class,
		EditorWarningTest.class,
		ReflectionUtilsTest.class,
		IntrospectionHelperTest.class,
		MenuIntersectorTest.class,
		ImageUtilsTest.class,
		AstTests.class,
		JdtCoreTests.class,
		RefactoringTests.class
})
public class UtilTests {
}

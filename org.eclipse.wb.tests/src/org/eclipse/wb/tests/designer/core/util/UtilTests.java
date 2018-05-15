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

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scheglov_ke
 */
public class UtilTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.utils");
    suite.addTestSuite(SystemUtilTest.class);
    suite.addTestSuite(AssertTest.class);
    suite.addTestSuite(Base64UtilsTest.class);
    suite.addTestSuite(XmlWriterTest.class);
    suite.addTestSuite(BrandingUtilsTest.class);
    suite.addTestSuite(DesignerExceptionTest.class);
    suite.addTestSuite(DesignerExceptionUtilsTest.class);
    suite.addTestSuite(CoreExceptionRewriterTest.class);
    suite.addTestSuite(GenericsUtilsTest.class);
    suite.addTestSuite(PairTest.class);
    suite.addTest(createSingleSuite(ExecutionUtilsTest.class));
    suite.addTest(createSingleSuite(NoOpProgressMonitorTest.class));
    suite.addTest(XmlTests.suite());
    suite.addTestSuite(ExternalFactoriesHelperTest.class);
    suite.addTestSuite(EditorWarningTest.class);
    suite.addTestSuite(ReflectionUtilsTest.class);
    suite.addTestSuite(IntrospectionHelperTest.class);
    suite.addTestSuite(MenuIntersectorTest.class);
    suite.addTestSuite(ImageUtilsTest.class);
    suite.addTest(AstTests.suite());
    suite.addTest(JdtCoreTests.suite());
    suite.addTest(RefactoringTests.suite());
    return suite;
  }
}

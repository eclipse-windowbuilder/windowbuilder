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
package org.eclipse.wb.tests.designer.core.util.refactoring;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import org.eclipse.ltk.core.refactoring.Change;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link ReflectionUtils} and its {@link Change}s.
 *
 * @author scheglov_ke
 */
public class RefactoringTests extends DesignerSuiteTests {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Suite
  //
  ////////////////////////////////////////////////////////////////////////////
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.core.utils.refactoring");
    suite.addTest(createSingleSuite(RefactoringUtilsTest.class));
    return suite;
  }
}

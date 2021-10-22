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
package org.eclipse.wb.tests.designer.XWT.refactoring;

import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;
import org.eclipse.wb.tests.designer.core.RefactoringTestUtils;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Test for XWT refactoring participants.
 *
 * @author scheglov_ke
 */
public class RefactoringTest extends XwtModelTest {
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
  public void test_renameEventListenerMethod() throws Exception {
    setFileContentSrc(
        "test/Test.java",
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void onKeyDown(Event event) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/Test.xwt",
        getSource(
            "// filler filler filler filler filler",
            "<Shell x:Class='test.Test' KeyDownEvent='onKeyDown'/>"));
    // do rename
    {
      IType type = m_javaProject.findType("test.Test");
      IMethod method = type.getMethods()[0];
      RefactoringTestUtils.renameMethod(method, "myMethod");
    }
    assertEquals(
        getJavaSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public void myMethod(Event event) {",
            "  }",
            "}"),
        getFileContentSrc("test/Test.java"));
    assertEquals(
        getSource(
            "// filler filler filler filler filler",
            "<Shell x:Class='test.Test' KeyDownEvent='myMethod'/>"),
        getFileContentSrc("test/Test.xwt"));
  }
}
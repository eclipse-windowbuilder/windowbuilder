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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link EditorWarning}.
 * 
 * @author scheglov_ke
 */
public class EditorWarningTest extends AbstractCoreTest {
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
  public void test_onlyMessage() throws Exception {
    String message = "my message";
    EditorWarning warning = new EditorWarning(message);
    assertSame(message, warning.getMessage());
    assertSame(null, warning.getException());
  }

  public void test_messageAndException() throws Exception {
    String message = "my message";
    Throwable exception = new Exception();
    EditorWarning warning = new EditorWarning(message, exception);
    assertSame(message, warning.getMessage());
    assertSame(exception, warning.getException());
  }
}
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

import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Test for {@link EditorWarning}.
 *
 * @author scheglov_ke
 */
public class EditorWarningTest extends DesignerTestCase {
  public void test() throws Exception {
    String message = "message";
    Exception exception = new Exception();
    EditorWarning warning = new EditorWarning(message, exception);
    assertSame(message, warning.getMessage());
    assertSame(exception, warning.getException());
  }
}

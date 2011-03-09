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
package org.eclipse.wb.tests.gef;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author lobas_av
 */
public class CursorTests extends TestCase {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.gef.cursors");
    suite.addTestSuite(CreationToolCursorTest.class);
    suite.addTestSuite(SelectionToolCursorTest.class);
    suite.addTestSuite(MarqueeSelectionToolCursorTest.class);
    return suite;
  }
}
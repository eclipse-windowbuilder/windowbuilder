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
package org.eclipse.wb.tests.designer.tests.mock;

import org.easymock.IMocksControl;

/**
 * Understands a template for usage of <a href="http://www.easymock.org/"
 * target="_blank">EasyMock</a> mocks.
 */
public final class EasyMockTemplate {
  public static void run(IMocksControl mocksControl, MockRunnable2 runnable) throws Exception {
    // additional configuration
    runnable.configure();
    // standard EasyMock template
    {
      mocksControl.reset();
      runnable.expectations();
      mocksControl.replay();
      runnable.codeToTest();
      mocksControl.verify();
    }
    // additional verify
    runnable.verify();
  }

  public static void run(IMocksControl mocksControl, MockRunnable runnable) throws Exception {
    if (runnable instanceof MockRunnable2) {
      ((MockRunnable2) runnable).configure();
    }
    //
    mocksControl.reset();
    runnable.expectations();
    mocksControl.replay();
    runnable.codeToTest();
    // verify
    mocksControl.verify();
    if (runnable instanceof MockRunnable2) {
      ((MockRunnable2) runnable).verify();
    }
  }
}

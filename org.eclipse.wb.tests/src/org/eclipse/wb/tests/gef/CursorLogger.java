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

import org.eclipse.swt.graphics.Cursor;

/**
 * @author lobas_av
 *
 */
public class CursorLogger extends TestLogger {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Logging
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCursor(Cursor cursor) {
    log("setCursor( " + cursor + " )");
  }
}
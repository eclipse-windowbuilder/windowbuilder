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
package org.eclipse.wb.internal.os.linux;

/**
 * The callback interface for screen shot support. See details in
 * {@link OSSupportLinux#makeShots(Object)}.
 *
 * @author mitin_aa
 * @coverage os.linux
 */
public interface IScreenshotCallback<H extends Number> {
  /**
   * Called from native code when the <code>pixmap</code> available for <code>handle</code>.
   *
   * @param handle
   *          the handle of widget (GtkWidget*).
   * @param pixmap
   *          the pixmap (GdkPixmap*).
   */
  void storeImage(H handle, H pixmap);
}

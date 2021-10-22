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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.io.InputStream;
import java.lang.reflect.Constructor;

/**
 * Stub class for using SWT {@link org.eclipse.swt.graphics.Image} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class ImageSupport extends AbstractSupport {
  /**
   * Disposes given {@link org.eclipse.swt.graphics.Image}.
   */
  public static void dispose(Object image) {
    if (image != null) {
      ReflectionUtils.invokeMethodEx(image, "dispose()");
    }
  }

  /**
   * Create new {@link org.eclipse.swt.graphics.Image}.
   */
  public static Object createImage(InputStream stream) throws Exception {
    Constructor<?> constructor =
        ReflectionUtils.getConstructorBySignature(
            getImageClass(),
            "<init>(org.eclipse.swt.graphics.Device,java.io.InputStream)");
    return constructor.newInstance(null, stream);
  }

  /**
   * @return {@link org.eclipse.swt.graphics.Image} {@link Class} loaded from active editor
   *         {@link ClassLoader}.
   */
  public static Class<?> getImageClass() {
    return loadClass("org.eclipse.swt.graphics.Image");
  }
}
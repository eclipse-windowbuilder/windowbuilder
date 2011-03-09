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

import org.eclipse.swt.layout.FormAttachment;

import java.lang.reflect.Constructor;

/**
 * Stub class for using SWT {@link org.eclipse.swt.layout.FormLayout}'s with another
 * {@link ClassLoader}.
 * 
 * @author mitin_aa
 * @coverage swt.support
 */
public class FormLayoutSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // FormLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create new {@link org.eclipse.swt.layout.FormData}.
   */
  public static Object createFormData() throws Exception {
    return loadClass("org.eclipse.swt.layout.FormData").newInstance();
  }

  /**
   * Create new {@link org.eclipse.swt.layout.FormAttachment}.
   */
  public static Object createFormAttachment() throws Exception {
    Class<?> clazz = getFormAttachmentClass();
    // use ctor with two parameters
    Constructor<?> twoParamsConstructor = clazz.getConstructor(int.class, int.class);
    return twoParamsConstructor.newInstance(0, 0);
  }

  /**
   * @return a {@link Class} of {@link FormAttachment}.
   */
  public static Class<?> getFormAttachmentClass() throws Exception {
    return loadClass("org.eclipse.swt.layout.FormAttachment");
  }
}
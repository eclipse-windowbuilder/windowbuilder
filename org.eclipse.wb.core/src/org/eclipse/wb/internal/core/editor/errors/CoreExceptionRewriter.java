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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.core.editor.errors.IExceptionRewriter;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.os.OSSupportError;

/**
 * {@link IExceptionRewriter} for core exceptions.
 *
 * @author scheglov_ke
 * @coverage core.editor.errors
 */
public class CoreExceptionRewriter implements IExceptionRewriter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IExceptionRewriter INSTANCE = new CoreExceptionRewriter();

  private CoreExceptionRewriter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExceptionRewriter
  //
  ////////////////////////////////////////////////////////////////////////////
  public Throwable rewrite(Throwable e) {
    Throwable rootException = DesignerExceptionUtils.getRootCause(e);
    if (rootException instanceof OSSupportError) {
      return new DesignerException(OSSupportError.ERROR_CODE, e);
    }
    if (isIncompleteProductException(rootException)) {
      return new DesignerException(ICoreExceptionConstants.INCOMPLETE_PRODUCT, e);
    }
    return e;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link Throwable} means that not all parts of WindowBuilder
   *         are installed or updated. Usually this happens when we add new classes or methods.
   */
  private static boolean isIncompleteProductException(Throwable e) {
    if (e instanceof NoSuchMethodError) {
      return e.getMessage().startsWith("org.eclipse.wb.");
    }
    if (e instanceof NoClassDefFoundError) {
      String message = e.getMessage();
      return message.startsWith("org/eclipse/wb/") || message.startsWith("org/eclipse/jdt/");
    }
    return false;
  }
}

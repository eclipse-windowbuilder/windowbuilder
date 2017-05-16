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
package org.eclipse.wb.internal.core.model.util.generic;

import com.google.common.base.Predicate;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * This helper allows to create top-level {@link Property} as copy of other {@link Property}
 * (usually part of complex property).
 * <p>
 * For example we may want to create top level copy of some property from <code>"Constructor"</code>.
 * <p>
 * Format:
 *
 * <code><pre>
 *   &lt;parameter name="copyPropertyTop from=Constructor/columnWidth to=width category=system(7)"/&gt;
 * </pre></code>
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class CopyPropertyTopSupport extends CopyPropertyTopAbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given {@link JavaInfo} to copy properties according parameters in description.
   */
  public static void install(JavaInfo javaInfo) {
    new CopyPropertyTopSupport().install(javaInfo, "copyPropertyTop ");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Predicate<JavaInfo> createTargetPredicate(final JavaInfo javaInfo) {
    return new Predicate<JavaInfo>() {
      public boolean apply(JavaInfo t) {
        return t == javaInfo;
      }
    };
  }
}

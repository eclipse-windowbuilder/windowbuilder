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
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.property.Property;

/**
 * This helper allows to create top-level {@link Property} as copy of other {@link Property}
 * (usually part of complex property). Note, that properties of children of given {@link JavaInfo}
 * are analyzed.
 * <p>
 * For example for <code>StackPanel</code> we create top-level <code>"StackText"</code> property
 * that is copy of <code>"stackText"</code> argument of {@link InvocationChildAssociation}.
 * <p>
 * Format:
 *
 * <code><pre>
 *   &lt;parameter name="copyChildPropertyTop from=Association/stackText to=StackText category=system(7)"/&gt;
 * </pre></code>
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class CopyPropertyTopChildSupport extends CopyPropertyTopAbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Installation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configures given parent to copy properties of children according parameters in description.
   */
  public static void install(JavaInfo parent) {
    new CopyPropertyTopChildSupport().install(parent, "copyChildPropertyTop ");
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
        return t.getParent() == javaInfo;
      }
    };
  }
}

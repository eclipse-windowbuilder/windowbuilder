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
package org.eclipse.wb.internal.core.model.property.accessor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;

/**
 * Optional interface that can be implemented by {@link ExpressionAccessor} to check that its can be
 * used with given {@link JavaInfo}.
 * <p>
 * Problem is that in {@link SetterAccessor} and {@link FieldAccessor} method/field may be declared
 * as <code>public</code> (then it is always accessible), or <code>protected</code> (then it is
 * accessible only for {@link ThisCreationSupport}).
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public interface IAccessibleExpressionAccessor {
  /**
   * @param javaInfo
   *          the {@link JavaInfo} that has property with this {@link ExpressionAccessor}.
   *
   * @return <code>true</code> if this {@link ExpressionAccessor} can be used.
   */
  boolean isAccessible(JavaInfo javaInfo);
}

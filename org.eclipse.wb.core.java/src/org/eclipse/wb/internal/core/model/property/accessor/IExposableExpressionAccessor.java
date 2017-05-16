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

/**
 * Optional interface that can be implemented by {@link ExpressionAccessor} that can be exposed as
 * top-level property of container.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public interface IExposableExpressionAccessor {
  /**
   * @param javaInfo
   *          the {@link JavaInfo} that has property with this {@link ExpressionAccessor}.
   *
   * @return the {@link Class} of property value.
   */
  Class<?> getValueClass(JavaInfo javaInfo);

  /**
   * @param javaInfo
   *          the {@link JavaInfo} that has property with this {@link ExpressionAccessor}.
   *
   * @return the code that should be used to access this property. For example for property "text"
   *         it will return "${componentAccessExpression}getText()". Note that it does not prepends
   *         this code with component accessing code, this is up to caller.
   */
  String getGetterCode(JavaInfo javaInfo) throws Exception;

  /**
   * @param javaInfo
   *          the {@link JavaInfo} that has property with this {@link ExpressionAccessor}.
   * @param source
   *          the source of value to assign.
   *
   * @return the code that should be used to change this property to given value. For example for
   *         property "text" and parameter "text" in will return "setText(text)". Note that it does
   *         not prepends this code with component accessing code, this is up to caller.
   */
  String getSetterCode(JavaInfo javaInfo, String source) throws Exception;
}

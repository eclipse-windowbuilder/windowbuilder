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
import org.eclipse.wb.internal.core.model.description.AbstractDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;

import org.eclipse.jdt.core.dom.Expression;

/**
 * {@link ExpressionAccessor} defines how to access {@link Expression} related with
 * {@link GenericProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public abstract class ExpressionAccessor extends AbstractDescription {
  ////////////////////////////////////////////////////////////////////////////
  //
  // GenericPropertyDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  protected GenericPropertyDescription m_propertyDescription;

  /**
   * Sets the {@link GenericPropertyDescription} in which this {@link ExpressionAccessor} is used.
   */
  public final void setPropertyDescription(GenericPropertyDescription property) {
    m_propertyDescription = property;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Expression} for given {@link JavaInfo}.
   */
  public abstract Expression getExpression(JavaInfo javaInfo) throws Exception;

  /**
   * Sets new expression with given source to the given {@link JavaInfo}.
   *
   * @param source
   *          the source of new value, <code>null</code> means that value should be removed (if
   *          possible for this accessor)
   *
   * @return <code>true</code> if expression was successfully set.
   */
  public abstract boolean setExpression(JavaInfo javaInfo, String source) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default value
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property can be marked with this tag (with value "true") to specify, that
   * {@link ExpressionAccessor} should not try to fetch default value. For example in eSWT
   * <code>Menu.getVisible()</code> contains bug - infinite recursion, so we should prevent its
   * invocation.
   */
  public static final String NO_DEFAULT_VALUE_TAG = "noDefaultValue";
  /**
   * Component can be marked with this tag (with value "true") to specify, that
   * {@link ExpressionAccessor} should not try to fetch default value, if {@link JavaInfo} uses
   * {@link ThisCreationSupport}. For example in GWT <code>Composite</code> requires invocation of
   * <code>initWidget()</code> before accessing default values, however we can not do this because
   * we can do this only once - for real widget.
   */
  protected static final String NO_DEFAULT_VALUES_THIS_TAG = "noDefaultValuesForThis";

  /**
   * @return optional default value for this property or {@link Property#UNKNOWN_VALUE} if default
   *         value is unknown.
   */
  public Object getDefaultValue(JavaInfo javaInfo) throws Exception {
    return Property.UNKNOWN_VALUE;
  }
}

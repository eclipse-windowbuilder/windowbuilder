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
package org.eclipse.wb.internal.core.model.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * {@link GenericProperty} provides universal access to the {@link Expression}'s and values using
 * {@link ExpressionAccessor} and {@link ExpressionConverter}.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public abstract class GenericProperty extends JavaProperty implements ITypedProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericProperty(JavaInfo javaInfo, String title, PropertyEditor propertyEditor) {
    super(javaInfo, title, propertyEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the value from {@link Expression} AST property. This value is set during latest AST
   *         evaluation, so it does not exist for nodes that are not evaluated - for example for new
   *         {@link Expression} 's. So, most probably this value can be used only for displaying to
   *         user.
   */
  @Override
  public abstract Object getValue() throws Exception;

  /**
   * @return the default value of this {@link GenericProperty}. This value is got from component
   *         directly after its creation, or set externally in component description.
   */
  public abstract Object getDefaultValue() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Expression} that was used for calculating value of this
   *         {@link GenericProperty}.
   */
  public abstract Expression getExpression() throws Exception;

  /**
   * Changes value using Java source and (optional) value.
   */
  public abstract void setExpression(String source, Object value) throws Exception;
}

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
package org.eclipse.wb.internal.core.xml.model.property.editor;

/**
 * Broadcast for preparing attribute {@link String} for {@link StaticFieldPropertyEditor}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property.editor
 */
public interface StaticFieldPropertyEditorGetExpression {
  /**
   * @param clazz
   *          the {@link Class} with fields.
   * @param field
   *          the name of field.
   * @param expression
   *          the array with single element, initially <code>null</code>.
   */
  void invoke(Class<?> clazz, String field, String[] expression) throws Exception;
}
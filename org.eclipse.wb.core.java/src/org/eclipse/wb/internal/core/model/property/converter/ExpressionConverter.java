/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * {@link ExpressionConverter} converts property value into Java source of {@link Expression}. It is
 * used as proxy between {@link PropertyEditor} and {@link GenericProperty}, if we want to use same
 * {@link PropertyEditor} with different presentations of value.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public abstract class ExpressionConverter {
	/**
	 * @return the Java source for {@link Expression} with given value. This Java source will be used
	 *         for adding to the AST of given {@link JavaInfo}, so {@link ExpressionConverter} should
	 *         do also any additional operation, for example import required classes.
	 */
	public abstract String toJavaSource(JavaInfo javaInfo, Object value) throws Exception;
}

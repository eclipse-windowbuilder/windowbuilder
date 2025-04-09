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
package org.eclipse.wb.internal.core.model.property.editor;

/**
 * Extension for {@link PropertyEditor} that can be used to convert {@link Object} value into Java
 * source.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public interface IValueSourcePropertyEditor {
	/**
	 * @return the Java source for given value.
	 */
	String getValueSource(Object value) throws Exception;
}

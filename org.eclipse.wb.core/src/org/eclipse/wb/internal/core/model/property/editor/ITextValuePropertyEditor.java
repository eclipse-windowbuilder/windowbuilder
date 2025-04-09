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

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Extension of {@link PropertyEditor} that can set value using its text presentation.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public interface ITextValuePropertyEditor {
	/**
	 * Sets value that corresponds given text.
	 */
	void setText(Property property, String text) throws Exception;
}

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
package org.eclipse.wb.internal.core.model.property.editor.complex;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Extension for {@link PropertyEditor} that specifies that it has sub-properties.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public interface IComplexPropertyEditor {
	/**
	 * @return sub-properties of given complex property.
	 */
	Property[] getProperties(Property property) throws Exception;
}
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
package org.eclipse.wb.internal.core.utils.state;

import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

/**
 * Helper for accessing <code>ComponentDescription</code> and its parts.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface IDescriptionHelper {
	/**
	 * @return the {@link PropertyEditor} for given type.
	 */
	PropertyEditor getEditorForType(Class<?> type) throws Exception;

	/**
	 * @return the {@link IComponentDescription} of given model instance, may be <code>null</code> if
	 *         object is not model instance.
	 */
	IComponentDescription getDescription(Object object);
}

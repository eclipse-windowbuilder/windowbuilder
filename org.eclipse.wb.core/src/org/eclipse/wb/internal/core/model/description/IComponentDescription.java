/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Interface for component description.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public interface IComponentDescription {
	/**
	 * @return the {@link ToolkitDescription} for this component.
	 */
	ToolkitDescription getToolkit();

	/**
	 * @return the {@link Class} of component for which this description is.
	 */
	Class<?> getComponentClass();

	/**
	 * @return the {@link Class} of model that should be used for this component.
	 */
	Class<?> getModelClass();

	/**
	 * @return the icon for this component.
	 */
	ImageDescriptor getIcon();
}

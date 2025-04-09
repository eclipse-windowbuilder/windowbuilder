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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Interface for visual presentation of {@link IObserveInfo} - title and image.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IObservePresentation {
	/**
	 * @return the text to display for user.
	 */
	String getText() throws Exception;

	/**
	 * @return the text to display for user.
	 */
	String getTextForBinding() throws Exception;

	/**
	 * @return the image descriptor to display for user.
	 */
	ImageDescriptor getImageDescriptor() throws Exception;
}
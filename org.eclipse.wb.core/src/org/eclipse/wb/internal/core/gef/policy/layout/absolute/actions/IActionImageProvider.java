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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Helper interface to provide the image for action.
 *
 * @author mitin_aa
 */
public interface IActionImageProvider {
	ImageDescriptor getActionImageDescriptor(String imagePath);
}

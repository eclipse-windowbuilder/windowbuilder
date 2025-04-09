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
package org.eclipse.wb.core.controls.flyout;

import org.eclipse.jface.action.IMenuManager;

/**
 * Contributes items into {@link IMenuManager} or {@link FlyoutControlComposite}.
 *
 * @author scheglov_ke
 * @coverage core.control
 */
public interface IFlyoutMenuContributor {
	void contribute(IMenuManager manager);
}

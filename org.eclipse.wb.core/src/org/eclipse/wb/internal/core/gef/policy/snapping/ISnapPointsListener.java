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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * Fired every time when bounds adjusted.
 *
 * @author mitin_aa
 */
public interface ISnapPointsListener {
	void boundsChanged(Rectangle bounds,
			List<? extends IAbstractComponentInfo> components,
			SnapPoint[] snapPoints,
			int[] directions);
}

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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * An interface to size providers for preferred-size-oriented figures.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IPreferredSizeProvider {
	/**
	 * Calculate new preferred size use original preferred size.
	 */
	Dimension getPreferredSize(Dimension originalPreferredSize);
}
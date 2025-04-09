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
package org.eclipse.wb.internal.swt.model.layout.absolute;

import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Interface for absolute (null) layout model.
 *
 * @author mitin_aa
 * @coverage swt.model.layout
 */
public interface IAbsoluteLayoutInfo<C extends IControlInfo> extends ILayoutInfo<C> {
	/**
	 * Adds a Control into host composite.
	 */
	void commandCreate(C control, C nextControl) throws Exception;

	/**
	 * Moves a Control into host composite.
	 */
	void commandMove(C control, C nextControl) throws Exception;

	/**
	 * Perform "move" or "resize" operation. Modifies location/size values by modifying appropriate
	 * "setLocation", "setSize", "setBounds" arguments.
	 *
	 * @param widget
	 *          a Control which modifications applies to
	 * @param location
	 *          a {@link Point} of new location of component. May be null.
	 * @param size
	 *          a {@link Dimension} of new size of component. May be null.
	 */
	void commandChangeBounds(C widget, Point location, Dimension size) throws Exception;
}

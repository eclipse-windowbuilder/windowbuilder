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
package org.eclipse.wb.core.branding;

import org.eclipse.wb.internal.core.editor.DesignComposite;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * The interface for branding information.
 *
 * @author Jaime Wren
 * @coverage core.util
 */
public interface IBrandingDescription {
	/**
	 * @return the product name.
	 */
	String getProductName();

	/**
	 * @return the url for bug-tracking system and discussion forum.
	 */
	IBrandingSupportInfo getSupportInfo();

	/**
	 * Called by {@link DesignComposite} to paint all product branding onto the canvas.
	 */
	void paintBrandingOnCanvas(Rectangle clientArea, Graphics graphics);
}

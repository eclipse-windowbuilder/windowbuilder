/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.model.IObjectInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.layout.GridData;

/**
 * Interface model for SWT {@link GridData}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public interface IGridDataInfo extends IObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return x/column.
	 */
	int getX();

	/**
	 * Sets x/column.
	 */
	void setX(int x);

	/**
	 * @return y/row.
	 */
	int getY();

	/**
	 * Sets y/row.
	 */
	void setY(int y);

	////////////////////////////////////////////////////////////////////////////
	//
	// Span
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the horizontal span.
	 */
	void setHorizontalSpan(int width) throws Exception;

	/**
	 * Sets the vertical span.
	 */
	void setVerticalSpan(int height) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Grab
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return value of <code>grabExcessHorizontalSpace</code> property.
	 */
	boolean getHorizontalGrab();

	/**
	 * Sets the value of <code>grabExcessHorizontalSpace</code> property.
	 */
	void setHorizontalGrab(boolean grab) throws Exception;

	/**
	 * @return value of <code>grabExcessVerticalSpace</code> property.
	 */
	boolean getVerticalGrab();

	/**
	 * Sets the value of <code>grabExcessVerticalSpace</code> property.
	 */
	void setVerticalGrab(boolean grab) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return value of <code>horizontalAlignment</code> property.
	 */
	int getHorizontalAlignment();

	/**
	 * Sets value of <code>horizontalAlignment</code> property.
	 */
	void setHorizontalAlignment(int alignment) throws Exception;

	/**
	 * @return value of <code>verticalAlignment</code> property.
	 */
	int getVerticalAlignment();

	/**
	 * Sets of <code>verticalAlignment</code> property.
	 */
	void setVerticalAlignment(int alignment) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Hint
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the value of <code>widthHint</code> property.
	 */
	void setWidthHint(int widthHint) throws Exception;

	/**
	 * Sets the value of <code>heightHint</code> property.
	 */
	void setHeightHint(int heightHint) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the small {@link ImageDescriptor} that represents horizontal/vertical
	 *         alignment.
	 */
	ImageDescriptor getSmallAlignmentImage(boolean horizontal);

	/**
	 * Adds the horizontal alignment {@link Action}'s.
	 */
	void fillHorizontalAlignmentMenu(IMenuManager manager);

	/**
	 * Adds the vertical alignment {@link Action}'s.
	 */
	void fillVerticalAlignmentMenu(IMenuManager manager);
}
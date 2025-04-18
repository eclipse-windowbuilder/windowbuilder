/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.internal.swt.model.layout.ILayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Interface model for {@link TableWrapData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public interface ITableWrapDataInfo extends ILayoutDataInfo<ControlInfo> {
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
	 * @return the horizontal span.
	 */
	int getHorizontalSpan();

	/**
	 * Sets the horizontal span.
	 */
	void setHorizontalSpan(int width) throws Exception;

	/**
	 * @return the vertical span.
	 */
	int getVerticalSpan();

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
	 * @return value of {@link TableWrapData#grabHorizontal} property.
	 */
	boolean getHorizontalGrab();

	/**
	 * Sets the value of {@link TableWrapData#grabHorizontal} property.
	 */
	void setHorizontalGrab(boolean grab) throws Exception;

	/**
	 * @return value of {@link TableWrapData#grabVertical} property.
	 */
	boolean getVerticalGrab();

	/**
	 * Sets the value of {@link TableWrapData#grabVertical} property.
	 */
	void setVerticalGrab(boolean grab) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return value of {@link TableWrapData#align} property.
	 */
	int getHorizontalAlignment();

	/**
	 * Sets value of {@link TableWrapData#align} property.
	 */
	void setHorizontalAlignment(int alignment) throws Exception;

	/**
	 * @return value of {@link TableWrapData#valign} property.
	 */
	int getVerticalAlignment();

	/**
	 * Sets value of {@link TableWrapData#valign} property.
	 */
	void setVerticalAlignment(int alignment) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the small {@link ImageDescriptor} that represents horizontal/vertical
	 *         alignment.
	 */
	ImageDescriptor getSmallAlignmentImageDescriptor(boolean horizontal);

	/**
	 * Adds the horizontal alignment {@link Action}'s.
	 */
	void fillHorizontalAlignmentMenu(IMenuManager manager);

	/**
	 * Adds the vertical alignment {@link Action}'s.
	 */
	void fillVerticalAlignmentMenu(IMenuManager manager);
}
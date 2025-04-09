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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.custom.SashForm;

/**
 * Interface model of {@link SashForm}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public interface ISashFormInfo<C extends IControlInfo> extends ICompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link SashForm} has horizontal layout.
	 */
	public boolean isHorizontal();

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link IControlInfo}.
	 */
	void command_CREATE(C control, C nextControl) throws Exception;

	/**
	 * Moves existing {@link IControlInfo}.
	 */
	void command_MOVE(C control, C nextControl) throws Exception;

	/**
	 * Sets requested size for {@link IControlInfo}.
	 *
	 * @param control
	 *          the {@link IControlInfo}, not last.
	 * @param size
	 *          the size that should be set for given {@link IControlInfo}.
	 */
	void command_RESIZE(C control, int size) throws Exception;
}

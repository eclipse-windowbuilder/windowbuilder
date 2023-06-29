/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.swt.model.widgets.IItemInfo;

import org.eclipse.swt.widgets.TreeColumn;

/**
 * Interface model of {@link TreeColumn}.
 *
 * @author scheglov_ke
 * @coverage swt.model.widgets
 */
public interface ITreeColumnInfo extends IItemInfo {
	/**
	 * Sets width in pixels.
	 */
	public void setWidth(int width) throws Exception;
}
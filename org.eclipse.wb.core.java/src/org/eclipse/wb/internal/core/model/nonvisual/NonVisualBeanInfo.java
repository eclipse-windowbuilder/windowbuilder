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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.draw2d.geometry.Point;

/**
 * Abstract <i>non-visual bean</i> support model.
 *
 * @author lobas_av
 * @coverage core.model.nonvisual
 */
public abstract class NonVisualBeanInfo {
	private static final String KEY_NON_VISUAL_BEAN = "NON_VISUAL_BEAN";
	//
	protected Point m_location = null;
	protected JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Location
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Point} current bean visual location.
	 */
	public final Point getLocation() {
		return m_location;
	}

	/**
	 * Changes bean visual location.
	 *
	 * @param moveDelta
	 *          is move delta for new location.
	 */
	public abstract void moveLocation(Point moveDelta) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Remove
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes information that wrapped {@link JavaInfo} is NVO. For example, removes JavaDoc comment.
	 */
	public void remove() throws Exception {
		m_javaInfo.putArbitraryValue(KEY_NON_VISUAL_BEAN, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JavaInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return association <i>non-visual bean</i> {@link JavaInfo}.
	 */
	public final JavaInfo getJavaInfo() {
		return m_javaInfo;
	}

	/**
	 * Sets association <i>non-visual bean</i> {@link JavaInfo}.
	 */
	public final void setJavaInfo(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
		m_javaInfo.putArbitraryValue(KEY_NON_VISUAL_BEAN, this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link JavaInfo} in non-visual bean.
	 */
	public static boolean isNVO(JavaInfo javaInfo) {
		return getNonVisualInfo(javaInfo) != null;
	}

	/**
	 * @return {@link NonVisualBeanInfo} for given <i>non-visual bean</i> or <code>null</code>.
	 */
	public static NonVisualBeanInfo getNonVisualInfo(JavaInfo javaInfo) {
		return (NonVisualBeanInfo) javaInfo.getArbitraryValue(KEY_NON_VISUAL_BEAN);
	}
}
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
package org.eclipse.wb.internal.core.laf;

/**
 * Interface providing baseline offset in visual component.
 *
 * @author mitin_aa
 * @coverage core.laf
 */
public interface IBaselineSupport {
	/**
	 * Constant used for widgets which have no baseline or their baseline can't be determined.
	 */
	int NO_BASELINE = -1;

	/**
	 * Returns the baseline offset from top of component.
	 *
	 * @param component
	 *          the visual element of some GUI toolkit.
	 * @return the baseline offset from top of component.
	 */
	int getBaseline(Object component);
}

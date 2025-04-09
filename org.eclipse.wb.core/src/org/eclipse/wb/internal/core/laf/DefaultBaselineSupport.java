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
 * Default implementation of {@link IBaselineSupport}.
 *
 * @author mitin_aa
 * @coverage core.laf
 */
public class DefaultBaselineSupport implements IBaselineSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// IBaselineSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getBaseline(Object component) {
		return NO_BASELINE;
	}
}

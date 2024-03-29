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

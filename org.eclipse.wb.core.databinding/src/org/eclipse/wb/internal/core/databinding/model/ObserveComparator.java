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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import java.util.Comparator;

/**
 * A comparison function for {@link IObserveInfo} objects.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public final class ObserveComparator implements Comparator<IObserveInfo> {
	public static final Comparator<IObserveInfo> INSTANCE = new ObserveComparator();

	////////////////////////////////////////////////////////////////////////////
	//
	// Comparator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int compare(final IObserveInfo observe1, final IObserveInfo observe2) {
		return ExecutionUtils.runObjectLog(() -> {
			String text1 = observe1.getPresentation().getText();
			String text2 = observe2.getPresentation().getText();
			return text1.compareTo(text2);
		}, 0);
	}
}
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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * {@link IObservableFactory} for input for <code>JFace</code> viewers.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ViewerInputObservableFactory implements IObservableFactory {
	public static final IObservableFactory INSTANCE = new ViewerInputObservableFactory();

	////////////////////////////////////////////////////////////////////////////
	//
	// IObservableFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Type getType() throws Exception {
		return Type.Input;
	}

	@Override
	public ObservableInfo createObservable(BindableInfo object,
			BindableInfo property,
			Type type,
			boolean version_1_3) throws Exception {
		return null;
	}
}
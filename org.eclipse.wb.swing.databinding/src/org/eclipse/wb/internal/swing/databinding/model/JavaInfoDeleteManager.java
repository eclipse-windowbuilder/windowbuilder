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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.components.JavaInfoReferenceProvider;

import java.util.List;

/**
 * Observe {@link JavaInfo} events for delete bindings that have reference to deleted
 * {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class JavaInfoDeleteManager
extends
org.eclipse.wb.internal.core.databinding.model.JavaInfoDeleteManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoDeleteManager(DatabindingsProvider provider) {
		super(provider, provider.getJavaInfoRoot());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void deleteBinding(IBindingInfo ibinding, List<IBindingInfo> bindings) throws Exception {
		BindingInfo binding = (BindingInfo) ibinding;
		binding.delete(CoreUtils.<BindingInfo>cast(bindings));
	}

	@Override
	protected boolean accept(ObjectInfo javaInfo) throws Exception {
		return javaInfo instanceof AbstractComponentInfo
				|| javaInfo.getParent() instanceof NonVisualBeanContainerInfo;
	}

	@Override
	protected String getReference(ObjectInfo javaInfo) throws Exception {
		return JavaInfoReferenceProvider.getReference((JavaInfo) javaInfo);
	}

	@Override
	protected boolean equals(ObjectInfo javaInfo, String javaInfoReference, IObserveInfo iobserve)
			throws Exception {
		ObserveInfo observe = (ObserveInfo) iobserve;
		return javaInfoReference.equals(observe.getReference());
	}
}
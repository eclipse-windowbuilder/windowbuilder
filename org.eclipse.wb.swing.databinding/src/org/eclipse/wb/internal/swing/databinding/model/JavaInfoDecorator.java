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
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.components.JavaInfoReferenceProvider;

/**
 * Decorator for bindings {@link JavaInfo} models.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class JavaInfoDecorator
extends
org.eclipse.wb.internal.core.databinding.model.presentation.JavaInfoDecorator {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoDecorator(DatabindingsProvider provider) {
		super(provider, provider.getJavaInfoRoot());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean accept(ObjectInfo object) throws Exception {
		return object instanceof AbstractComponentInfo
				|| object.getParent() instanceof NonVisualBeanContainerInfo;
	}

	@Override
	protected String getReference(ObjectInfo object) throws Exception {
		return JavaInfoReferenceProvider.getReference((JavaInfo) object);
	}

	@Override
	protected boolean equals(ObjectInfo object, String objectReference, IObserveInfo iobserve)
			throws Exception {
		ObserveInfo observe = (ObserveInfo) iobserve;
		return objectReference.equals(observe.getReference());
	}
}
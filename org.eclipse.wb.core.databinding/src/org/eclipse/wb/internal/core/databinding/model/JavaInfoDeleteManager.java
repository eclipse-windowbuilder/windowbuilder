/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;

import java.util.ArrayList;
import java.util.List;

/**
 * Observe {@link JavaInfo} events for delete bindings that have reference to deleted
 * {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public abstract class JavaInfoDeleteManager {
	protected final IDatabindingsProvider m_provider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoDeleteManager(IDatabindingsProvider provider, ObjectInfo objectInfoRoot) {
		m_provider = provider;
		objectInfoRoot.addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
				deleteJavaInfo(child);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	public final void deleteJavaInfo(ObjectInfo javaInfo) throws Exception {
		List<IBindingInfo> deleteList = new ArrayList<>();
		List<IBindingInfo> bindings = m_provider.getBindings();
		//
		if (!m_provider.getBindings().isEmpty() && accept(javaInfo)) {
			String reference = getReference(javaInfo);
			//
			if (reference != null) {
				for (IBindingInfo binding : new ArrayList<>(bindings)) {
					if (equals(javaInfo, reference, binding.getTarget())
							|| equals(javaInfo, reference, binding.getModel())) {
						deleteList.add(binding);
						deleteBinding(binding, bindings);
					}
				}
			}
		}
		// commit
		if (!deleteList.isEmpty()) {
			bindings.removeAll(deleteList);
			m_provider.saveEdit();
		}
	}

	protected abstract void deleteBinding(IBindingInfo binding, List<IBindingInfo> bindings)
			throws Exception;

	/**
	 * @return <code>true</code> if given {@link JavaInfo} can work with bindings.
	 */
	protected abstract boolean accept(ObjectInfo javaInfo) throws Exception;

	/**
	 * @return {@link String} reference that represented given {@link JavaInfo}.
	 */
	protected abstract String getReference(ObjectInfo javaInfo) throws Exception;

	/**
	 * @return <code>true</code> if given {@link JavaInfo} equal with given {@link IObserveInfo}.
	 */
	protected abstract boolean equals(ObjectInfo javaInfo,
			String javaInfoReference,
			IObserveInfo iobserve) throws Exception;
}
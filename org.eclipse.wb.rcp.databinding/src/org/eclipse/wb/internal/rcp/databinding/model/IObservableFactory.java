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
package org.eclipse.wb.internal.rcp.databinding.model;

/**
 * Factory for creating {@link ObservableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public interface IObservableFactory {
	/**
	 * @return {@link Type} of this factory.
	 */
	Type getType() throws Exception;

	/**
	 * Create {@link ObservableInfo} for given <code>object</code> and <code>property</code> with
	 * required <code>type</code>.
	 */
	ObservableInfo createObservable(BindableInfo object,
			BindableInfo property,
			Type type,
			boolean version_1_3) throws Exception;

	/**
	 * Observable type which work this factory.
	 */
	enum Type {
		/**
		 * Work only with <code>observeValue()</code>.
		 */
		OnlyValue,
		/**
		 * Work only with <code>observeList()</code>.
		 */
		OnlyList,
		/**
		 * Work only with <code>observeSet()</code>.
		 */
		OnlySet,
		/**
		 * Undefined.
		 */
		Any,
		/**
		 * Object type is <code>List</code>, but may be work with any <code>bindXXX()</code>.
		 */
		List,
		/**
		 * Object type is <code>Set</code>, but may be work with any <code>bindXXX()</code>.
		 */
		Set,
		/**
		 * Binding input for JFace viewers.
		 */
		Input,
		/**
		 * Object type is collection (maybe List, maybe Set) and use only for binding input for JFace
		 * viewers.
		 */
		InputCollection,
		/**
		 * Work only with <code>observeDetailXXX()</code>.
		 */
		Detail
	}
}
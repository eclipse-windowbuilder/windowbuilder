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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;

/**
 * Decorator provider.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IObserveDecoration {
	/**
	 * @return {@link IObserveDecorator} for visual decorate this object.
	 */
	IObserveDecorator getDecorator();
}
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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * Interface for "leaf" event {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
public interface IListenerMethodProperty {
	/**
	 * Opens stub method (or listener method if there are no stub method).
	 */
	void openStubMethod() throws Exception;
}

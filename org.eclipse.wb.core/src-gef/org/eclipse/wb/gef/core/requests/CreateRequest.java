/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.gef.requests.CreationFactory;

/**
 * A {@link Request} to create a new object.
 *
 * @author lobas_av
 * @coverage gef.core
 * @deprecated Use {@link org.eclipse.gef.requests.CreateRequest CreateRequest}
 *             directly.
 */
@Deprecated(since = "2026-12", forRemoval = true)
public class CreateRequest extends org.eclipse.gef.requests.CreateRequest {

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a {@link CreateRequest} with the specified <i>type</i> and <i>factory</i>.
	 */
	@Deprecated(since="2026-12", forRemoval = true)
	public CreateRequest(CreationFactory factory) {
		setFactory(factory);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Deprecated(since="2026-12", forRemoval = true)
	public String toString() {
		StringBuffer buffer = new StringBuffer("CreateRequest(type=");
		buffer.append(getType());
		buffer.append(", location=");
		buffer.append(getLocation());
		buffer.append(", size=");
		buffer.append(getSize());
		buffer.append(", factory=");
		buffer.append(getFactory());
		if (getFactory() != null) {
			buffer.append("[object=");
			buffer.append(safeToString(getFactory().getNewObject()));
			buffer.append("]");
		}
		buffer.append(")");
		return buffer.toString();
	}

	/**
	 * @return the string presentation of given {@link Object} or "<exception>" if
	 *         any exception happened.
	 */
	@Deprecated(since="2026-12", forRemoval = true)
	protected static String safeToString(Object o) {
		try {
			return o == null ? null : o.toString();
		} catch (Throwable e) {
			return "<exception>";
		}
	}
}
/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.gef.core.EditPart;

/**
 * An Object used to communicate with {@link EditPart}s. {@link Request} encapsulates the
 * information {@link EditPart}s need to perform various functions. {@link Request}s are used for
 * obtaining commands, showing feedback, and performing generic operations.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class Request extends org.eclipse.gef.Request {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs an empty {@link Request}.
	 */
	public Request() {
	}

	/**
	 * Constructs a {@link Request} with the specified <i>type</i>.
	 */
	public Request(Object type) {
		super(type);
	}
}
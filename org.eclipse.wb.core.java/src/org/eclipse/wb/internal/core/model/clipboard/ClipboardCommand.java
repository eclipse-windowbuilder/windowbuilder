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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.core.model.JavaInfo;

import java.io.Serializable;

/**
 * Abstract command for copy/paste operation.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public abstract class ClipboardCommand implements Serializable {
	private static final long serialVersionUID = 0L;

	////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Executes this {@link ClipboardCommand} in context of its {@link JavaInfo}.
	 */
	public abstract void execute(JavaInfo javaInfo) throws Exception;
}

/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.gef.core;

import org.eclipse.wb.gef.core.tools.Tool;

import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;

/**
 * Performs pre-processing {@link Request} in {@link EditPart}'s, before sending {@link Request} to
 * {@link EditPolicy}'s.
 * <p>
 * Sometimes {@link Request} should be handled differently in conjunction with some special
 * {@link EditPart}'s, but nor {@link Tool} nor {@link EditPart} know about this. So, we use
 * external {@link RequestProcessor}'s to modify {@link Request}.For example, in Swing we can drop
 * {@link javax.swing.Action} on {@link javax.swing.JMenu}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public abstract class RequestProcessor {
	/**
	 * Processes given {@link Request}.
	 *
	 * @param editPart
	 *          the {@link EditPart} which initiates {@link Request} processing.
	 * @param request
	 *          the {@link Request} to process.
	 *
	 * @return a new {@link Request}, or same {@link Request}.
	 */
	public abstract Request process(EditPart editPart, Request request) throws Exception;
}

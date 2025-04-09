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
package org.eclipse.wb.internal.core.eval;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.awt.EventQueue;

/**
 * This interface may be contributed by toolkit plugins to provide tweaks for execution flow.
 * <p>
 * For example in Swing we should visit {@link EventQueue#invokeAndWait(Runnable)}, even if
 * {@link Runnable} is {@link AnonymousClassDeclaration}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public class ExecutionFlowProvider {
	/**
	 * @return the constructor to start execution flow from, may be <code>null</code> if no any
	 *         constructor or just none of constructors can be chosen as default.
	 */
	public MethodDeclaration getDefaultConstructor(TypeDeclaration typeDeclaration) {
		return null;
	}

	public boolean shouldVisit(AnonymousClassDeclaration anonymous) throws Exception {
		return false;
	}
}

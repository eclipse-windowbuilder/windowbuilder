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
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;

import java.io.Serializable;

/**
 * {@link IImplicitCreationSupport} returns implementation of this class to access corresponding
 * implicit/exposed child from pasting host {@link JavaInfo}.
 * <p>
 * For example when we copy/paste container that exposes <code>JButton</code> using method
 * <code>getButton()</code>, we create new instance of container and then, during paste
 * <code>JButton</code> child we don't create new instance of <code>JButton</code>, because it is
 * already created during initialization of container. We just ask clipboard-sensitive
 * {@link IImplicitCreationSupport} find <code>JButton</code> in container for us, it can use method
 * name/signature to do this.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public abstract interface IClipboardImplicitCreationSupport extends Serializable {
	/**
	 * Returns the child of given host, that corresponds to this {@link IImplicitCreationSupport}.
	 *
	 * @param host
	 *          the host {@link JavaInfo} to find children in.
	 */
	JavaInfo find(JavaInfo host) throws Exception;
}

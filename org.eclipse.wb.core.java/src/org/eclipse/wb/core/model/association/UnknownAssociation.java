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
package org.eclipse.wb.core.model.association;

/**
 * Implementation of {@link Association} that is unknown for Designer, for example established in
 * some custom binary code. This means that we can not remove it, or reparent. Practically we can
 * not do anything with it. But we still need some {@link Association} implementation.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class UnknownAssociation extends Association {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canDelete() {
		return false;
	}
}

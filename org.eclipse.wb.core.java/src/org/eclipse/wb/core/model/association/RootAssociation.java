/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.model.association;

/**
 * Implementation of {@link Association} for root component.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class RootAssociation extends Association {
	@Override
	public boolean remove() throws Exception {
		return false;
	}
}

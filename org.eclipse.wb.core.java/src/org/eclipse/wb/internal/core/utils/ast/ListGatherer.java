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
package org.eclipse.wb.internal.core.utils.ast;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Instances of the class <code>ListGatherer</code> implement a gatherer that maintains the order in
 * which the values were found.
 * <p>
 *
 * @author Brian Wilkerson
 * @version $Revision: 1.3 $
 * @coverage core.util.ast
 */
public abstract class ListGatherer<T> extends Gatherer<T> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Accessing -- internal
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected final Collection<T> createCollection() {
		return new ArrayList<>();
	}
}
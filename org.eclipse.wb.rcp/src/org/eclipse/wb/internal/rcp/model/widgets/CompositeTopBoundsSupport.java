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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

/**
 * Implementation of {@link org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport} for
 * RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class CompositeTopBoundsSupport
extends
org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CompositeTopBoundsSupport(CompositeInfo composite) {
		super(composite);
	}
}

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
package org.eclipse.wb.internal.rcp;

import org.eclipse.wb.internal.core.model.description.IToolkitProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;

/**
 * Implementation of {@link IToolkitProvider} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp
 */
public final class ToolkitProvider implements IToolkitProvider {
	public static final ToolkitDescriptionJava DESCRIPTION = RcpToolkitDescription.INSTANCE;
	static {
		((RcpToolkitDescription) DESCRIPTION).initialize();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IToolkitProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ToolkitDescription getDescription() {
		return DESCRIPTION;
	}
}

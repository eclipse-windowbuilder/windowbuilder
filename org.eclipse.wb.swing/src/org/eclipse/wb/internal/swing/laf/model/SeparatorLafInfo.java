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
package org.eclipse.wb.internal.swing.laf.model;

import org.eclipse.wb.internal.swing.model.ModelMessages;

import javax.swing.LookAndFeel;

/**
 * Used just to indicate separator in list of LAFs.
 *
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public class SeparatorLafInfo extends LafInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public SeparatorLafInfo(String name) {
		super(name, name, null);
	}

	public SeparatorLafInfo() {
		this("");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public LookAndFeel getLookAndFeelInstance() throws Exception {
		throw new RuntimeException(ModelMessages.SeparatorLafInfo_canNotInstantiate);
	}
}

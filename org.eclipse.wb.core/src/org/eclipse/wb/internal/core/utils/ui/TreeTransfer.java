/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Implementation of {@link ByteArrayTransfer} for tree elements.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class TreeTransfer extends ByteArrayTransfer {
	public static final TreeTransfer INSTANCE = new TreeTransfer();
	private static final String TYPE_NAME = "Tree content provider transfer";
	private static final int TYPE_ID = registerType(TYPE_NAME);

	private TreeTransfer() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Transfer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected int[] getTypeIds() {
		return new int[]{TYPE_ID};
	}

	@Override
	protected String[] getTypeNames() {
		return new String[]{TYPE_NAME};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ByteArrayTransfer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		return ArrayUtils.EMPTY_BYTE_ARRAY;
	}
}
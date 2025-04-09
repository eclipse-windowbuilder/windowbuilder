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
package org.eclipse.wb.internal.core.utils.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * {@link ClassVisitor} for visitors that transforms to byte array.
 *
 * @author scheglov_ke
 */
public class ToBytesClassAdapter extends ClassVisitor implements Opcodes {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ToBytesClassAdapter() {
		this(0);
	}

	public ToBytesClassAdapter(int flags) {
		super(Opcodes.ASM9, new ClassWriter(flags));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public byte[] toByteArray() {
		return ((ClassWriter) cv).toByteArray();
	}
}

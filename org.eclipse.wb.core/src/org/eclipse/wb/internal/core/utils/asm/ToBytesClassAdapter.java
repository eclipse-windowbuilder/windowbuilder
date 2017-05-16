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
package org.eclipse.wb.internal.core.utils.asm;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * {@link ClassAdapter} for visitors that transforms to byte array.
 *
 * @author scheglov_ke
 */
public class ToBytesClassAdapter extends ClassAdapter implements Opcodes {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToBytesClassAdapter() {
    this(0);
  }

  public ToBytesClassAdapter(int flags) {
    super(new ClassWriter(flags));
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

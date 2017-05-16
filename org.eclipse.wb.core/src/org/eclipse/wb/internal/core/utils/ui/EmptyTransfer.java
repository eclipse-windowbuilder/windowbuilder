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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

import org.apache.commons.lang.ArrayUtils;

/**
 * {@link Transfer} that can be used when we don't need to transfer real data, because, for example,
 * we track transferred object ourselves.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class EmptyTransfer extends ByteArrayTransfer {
  public static EmptyTransfer INSTANCE = new EmptyTransfer();
  private static final String TYPE_NAME = "WindowBuilder empty transfer";
  private static final int TYPE_ID = registerType(TYPE_NAME);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private EmptyTransfer() {
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

  @Override
  protected void javaToNative(Object object, TransferData transferData) {
  }

  @Override
  protected Object nativeToJava(TransferData transferData) {
    return ArrayUtils.EMPTY_BYTE_ARRAY;
  }
}
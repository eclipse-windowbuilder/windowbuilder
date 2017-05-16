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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import org.apache.commons.lang.ArrayUtils;

/**
 * Implementation of {@link ByteArrayTransfer} for tree elements.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
final class TreeTransfer extends ByteArrayTransfer {
  public static final TreeTransfer INSTANCE = new TreeTransfer();
  private static final String TYPE_NAME = "Tree content provider bindings tranfser";
  private static final int TYPE_ID = registerType(TYPE_NAME);

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
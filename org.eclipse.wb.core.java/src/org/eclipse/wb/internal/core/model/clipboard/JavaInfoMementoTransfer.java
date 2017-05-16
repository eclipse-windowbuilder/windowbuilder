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
package org.eclipse.wb.internal.core.model.clipboard;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation of {@link ByteArrayTransfer} for {@link JavaInfoMemento}.
 *
 * @author scheglov_ke
 * @coverage core.model.clipboard
 */
public class JavaInfoMementoTransfer extends ByteArrayTransfer {
  private static final String TYPE_NAME = "DESIGNER_TYPE_NAME";
  private static final int TYPE_ID = registerType(TYPE_NAME);
  private static JavaInfoMementoTransfer _instance = new JavaInfoMementoTransfer();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfoMementoTransfer() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static JavaInfoMementoTransfer getInstance() {
    return _instance;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Transfer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String[] getTypeNames() {
    return new String[]{TYPE_NAME};
  }

  @Override
  protected int[] getTypeIds() {
    return new int[]{TYPE_ID};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ByteArrayTransfer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void javaToNative(final Object object, final TransferData transferData) {
    if (isSupportedType(transferData)) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          byte[] bytes = convertObjectToBytes(object);
          JavaInfoMementoTransfer.super.javaToNative(bytes, transferData);
        }
      });
    }
  }

  @Override
  public Object nativeToJava(final TransferData transferData) {
    if (isSupportedType(transferData)) {
      return ExecutionUtils.runObject(new RunnableObjectEx<Object>() {
        public Object runObject() throws Exception {
          byte[] bytes = (byte[]) JavaInfoMementoTransfer.super.nativeToJava(transferData);
          return convertBytesToObject(bytes);
        }
      });
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bytes that correspond to the serialized object.
   */
  public static byte[] convertObjectToBytes(Object object) throws IOException {
    // write object to stream
    ByteArrayOutputStream baos;
    {
      baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos));
      oos.writeObject(object);
      oos.close();
    }
    // return as bytes
    return baos.toByteArray();
  }

  public static Object convertBytesToObject(byte[] bytes) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = new BundleObjectInputStream(new GZIPInputStream(bais));
    try {
      return ois.readObject();
    } finally {
      ois.close();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // BundleObjectInputStream
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ObjectInputStream} that resolves classes using {@link Bundle}'s.
   *
   * @author scheglov_ke
   */
  private static final class BundleObjectInputStream extends ObjectInputStream {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public BundleObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Resolve
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
        ClassNotFoundException {
      String className = desc.getName();
      return ExternalFactoriesHelper.loadBundleClass(className);
    }
  }
}

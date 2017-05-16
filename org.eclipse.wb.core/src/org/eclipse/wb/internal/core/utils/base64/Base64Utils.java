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
package org.eclipse.wb.internal.core.utils.base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Utils to provide encode/decode base64.
 *
 * @author mitin_aa
 * @coverage core.utils.base64
 */
public final class Base64Utils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Encoding utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the encoded form of the given unencoded string. The encoder uses the ISO-8859-1
   * (Latin-1) encoding to convert the string to bytes. For greater control over the encoding,
   * encode the string to bytes yourself and use encode(byte[]).
   *
   * @param unencoded
   *          the string to encode.
   * @return the encoded form of the unencoded string.
   */
  public static String encode(String unencoded) {
    byte[] bytes = null;
    try {
      bytes = unencoded.getBytes("8859_1");
    } catch (UnsupportedEncodingException ignored) {
    }
    return encode(bytes);
  }

  /**
   * Returns the encoded form of the given unencoded string.
   *
   * @param bytes
   *          the byte array to encode.
   * @return the encoded form of the unencoded string.
   */
  public static String encode(byte[] bytes) {
    try {
      byte[] encoded = Base64.encodeBytesToBytes(bytes);
      return new String(encoded, "8859_1");
    } catch (UnsupportedEncodingException ignored) {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoding utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the decoded form of the given encoded string, as a String. Note that not all binary
   * data can be represented as a String, so this method should only be used for encoded String
   * data. Use decodeToBytes() otherwise.
   *
   * @param encoded
   *          the string to decode.
   * @return the decoded form of the encoded string.
   */
  public static String decode(String encoded) {
    return new String(decodeToBytes(encoded));
  }

  /**
   * Returns the decoded form of the given encoded string as bytes.
   *
   * @param encoded
   *          the string to decode.
   * @return the decoded form of the encoded string.
   */
  public static byte[] decodeToBytes(String encoded) {
    byte[] bytes = null;
    try {
      bytes = encoded.getBytes("8859_1");
    } catch (UnsupportedEncodingException ignored) {
      return null;
    }
    return decodeToBytes(bytes);
  }

  /**
   * Returns the decoded form of the given encoded byte array.
   *
   * @param bytes
   *          the byte array to decode.
   * @return the decoded form of the encoded byte array.
   */
  public static byte[] decodeToBytes(byte[] bytes) {
    try {
      return Base64.decode(bytes);
    } catch (IOException ignored) {
      return null;
    }
  }
}

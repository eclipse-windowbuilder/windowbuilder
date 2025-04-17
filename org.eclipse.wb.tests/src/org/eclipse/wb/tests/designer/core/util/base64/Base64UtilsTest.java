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
package org.eclipse.wb.tests.designer.core.util.base64;

import org.eclipse.wb.internal.core.utils.base64.Base64Utils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;

/**
 * Base64 utils simple testing.
 *
 * @author mitin_aa
 */
public class Base64UtilsTest extends DesignerTestCase {
	private static final String UNENCODED_STRING = "some string to encode";
	private static final String ENCODED_STRING = "c29tZSBzdHJpbmcgdG8gZW5jb2Rl";
	private static final byte[] UNENCODED_BYTES = new byte[]{
			0x10,
			0x50,
			0x34,
			(byte) 0xAA,
			(byte) 0xDC,
			0x00,
			0x00,
			(byte) 0xCF,
			0x2B,
			(byte) 0xF1};
	private static final String ENCODED_BYTES = "EFA0qtwAAM8r8Q==";

	////////////////////////////////////////////////////////////////////////////
	//
	// Encoding
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_encodeString() throws Exception {
		String encodedString = Base64Utils.encode(UNENCODED_STRING);
		assertEquals(ENCODED_STRING, encodedString);
		assertTrue(StringUtils.isAsciiPrintable(encodedString));
	}

	@Test
	public void test_encodeBytes() throws Exception {
		String encodedString = Base64Utils.encode(UNENCODED_BYTES);
		assertEquals(ENCODED_BYTES, encodedString);
		assertTrue(StringUtils.isAsciiPrintable(encodedString));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Decoding
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_decodeString() throws Exception {
		String decodedString = Base64Utils.decode(ENCODED_STRING);
		assertEquals(UNENCODED_STRING, decodedString);
	}

	@Test
	public void test_decodeBytes() throws Exception {
		byte[] decodedBytes = Base64Utils.decodeToBytes(ENCODED_BYTES);
		assertTrue(Arrays.equals(UNENCODED_BYTES, decodedBytes));
	}
}

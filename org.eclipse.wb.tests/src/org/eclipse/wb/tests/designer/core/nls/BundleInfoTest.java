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
package org.eclipse.wb.tests.designer.core.nls;

import org.eclipse.wb.internal.core.nls.bundle.BundleInfo;
import org.eclipse.wb.internal.core.nls.bundle.StandardPropertiesAccessor;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.resources.IFile;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Test for {@link BundleInfo}.
 *
 * @author scheglov_ke
 */
public class BundleInfoTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		setFileContentSrc("test/messages.properties", getSourceDQ("frame.title=My JFrame"));
		setFileContentSrc("test/messages_it.properties", getSourceDQ("frame.title=My JFrame IT"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_createBundle() throws Exception {
		LocaleInfo localeInfo = new LocaleInfo(new Locale("it"));
		IFile[] files =
				new IFile[]{
						getFileSrc("test", "messages.properties"),
						getFileSrc("test", "messages_it.properties"),};
		BundleInfo bundle =
				BundleInfo.createBundle(
						StandardPropertiesAccessor.INSTANCE,
						"test.messages",
						localeInfo,
						files);
		assertNotNull(bundle);
		assertEquals("test.messages", bundle.getBundleName());
		assertSame(files[1], bundle.getFile());
		assertSame(localeInfo, bundle.getLocale());
		assertFalse(bundle.isExternallyChanged());
	}

	@Test
	public void test_createBundle_no() throws Exception {
		LocaleInfo localeInfo = new LocaleInfo(new Locale("fr"));
		IFile[] files =
				new IFile[]{
						getFileSrc("test", "messages.properties"),
						getFileSrc("test", "messages_it.properties"),};
		BundleInfo bundle =
				BundleInfo.createBundle(
						StandardPropertiesAccessor.INSTANCE,
						"test.messages",
						localeInfo,
						files);
		assertNull(bundle);
	}

	@Test
	public void test_getLocale_bad() throws Exception {
		IFile badFile = setFileContentSrc("test/messagesbad.properties", "");
		try {
			BundleInfo.getLocale("test.messages", badFile);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void test_getLocale_new() throws Exception {
		IFile badFile = setFileContentSrc("test/messages_new.properties", "");
		LocaleInfo locale = BundleInfo.getLocale("test.messages", badFile);
		assertEquals("new", locale.toString());
	}

	@Test
	public void test_getLocale_languageCountry() throws Exception {
		IFile badFile = setFileContentSrc("test/messages_bo_AM.properties", "");
		LocaleInfo locale = BundleInfo.getLocale("test.messages", badFile);
		assertEquals("bo_AM", locale.toString());
	}

	@Test
	public void test_getMap_setMap_save() throws Exception {
		IFile[] files = new IFile[]{getFileSrc("test", "messages.properties")};
		BundleInfo bundle =
				BundleInfo.createBundle(
						StandardPropertiesAccessor.INSTANCE,
						"test.messages",
						LocaleInfo.DEFAULT,
						files);
		assertEquals("My JFrame", bundle.getValue("frame.title"));
		//
		Map<String, String> map = bundle.getMap();
		assertNotSame(map, bundle.getMap()); // each time new map created
		assertEquals(1, map.size());
		assertEquals("My JFrame", map.get("frame.title"));
		// update map, should not change bundle
		map.put("frame.title", "New title");
		assertEquals("My JFrame", bundle.getValue("frame.title"));
		// set map, should see new value using bundle
		bundle.setMap(map);
		assertEquals("New title", bundle.getValue("frame.title"));
		// check save
		{
			// before save content of file is still same
			assertEquals(
					getSourceDQ("frame.title=My JFrame"),
					getFileContentSrc("test/messages.properties"));
			// do save and check content again
			bundle.save("Some comment");
			String newContent = getFileContentSrc("test/messages.properties");
			assertTrue(newContent.contains("Some comment"));
			assertTrue(newContent.contains("frame.title=New title"));
		}
	}

	@Test
	public void test_values_keys() throws Exception {
		IFile[] files = new IFile[]{getFileSrc("test", "messages.properties")};
		BundleInfo bundle =
				BundleInfo.createBundle(
						StandardPropertiesAccessor.INSTANCE,
						"test.messages",
						LocaleInfo.DEFAULT,
						files);
		assertEquals("My JFrame", bundle.getValue("frame.title"));
		// check keys
		{
			Set<String> keys = bundle.getKeys();
			assertEquals(1, keys.size());
			assertTrue(keys.contains("frame.title"));
			//
			assertTrue(bundle.containsKey("frame.title"));
			assertFalse(bundle.containsKey("no-such-key"));
		}
		// add new key/value
		{
			bundle.setValue("1", "aaa");
			assertTrue(bundle.containsKey("1"));
			assertEquals("aaa", bundle.getValue("1"));
		}
		// remove key
		{
			bundle.removeKey("1");
			assertFalse(bundle.containsKey("1"));
			assertNull(bundle.getValue("1"));
		}
		// check for empty value
		{
			bundle.setValue("1", "aaa");
			assertTrue(bundle.containsKey("1"));
			bundle.setValue("1", "");
			assertFalse(bundle.containsKey("1"));
		}
		// replace key
		{
			// no conflict
			{
				bundle.setValue("1", "aaa");
				bundle.setValue("2", "bbb");
				bundle.replaceKey("1", "1_", false);
				assertEquals("aaa", bundle.getValue("1_"));
				assertNull(bundle.getValue("1"));
			}
			// don't keep old value
			{
				bundle.setValue("1", "aaa");
				bundle.setValue("2", "bbb");
				bundle.replaceKey("1", "2", false);
				assertEquals("aaa", bundle.getValue("2"));
				assertNull(bundle.getValue("1"));
			}
			// keep old value
			{
				bundle.setValue("1", "aaa");
				bundle.setValue("2", "bbb");
				bundle.replaceKey("1", "2", true);
				assertEquals("bbb", bundle.getValue("2"));
				assertNull(bundle.getValue("1"));
			}
		}
	}

	/**
	 * Special support for "UTF-8".
	 */
	@Test
	public void test_UTF8() throws Exception {
		IFile file = getFileSrc("test/messages.properties");
		// prepare file in UTF-8 with Russian characters
		String key = "key";
		String value = "" + (char) 0x410 + (char) 0x411 + (char) 0x412;
		String newValue = "" + (char) 0x430 + (char) 0x431 + (char) 0x432;
		{
			String content = key + "=" + value;
			setFileContent(file, content.getBytes("UTF-8"));
			file.setCharset("UTF-8", null);
		}
		// use BundleInfo, session #1
		// check for initial value
		{
			BundleInfo bundle =
					BundleInfo.createBundle(
							StandardPropertiesAccessor.INSTANCE,
							"test.messages",
							LocaleInfo.DEFAULT,
							new IFile[]{file});
			assertNotNull(bundle);
			assertEquals(value, bundle.getValue(key));
			// set new value
			bundle.setValue(key, newValue);
			bundle.save("");
			assertEquals("UTF-8", file.getCharset());
		}
		// use BundleInfo, session #2
		// check for new value
		{
			BundleInfo bundle =
					BundleInfo.createBundle(
							StandardPropertiesAccessor.INSTANCE,
							"test.messages",
							LocaleInfo.DEFAULT,
							new IFile[]{file});
			assertNotNull(bundle);
			assertEquals(newValue, bundle.getValue(key));
		}
	}
}

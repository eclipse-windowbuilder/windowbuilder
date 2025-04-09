/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.jface.resource.ColorRegistryInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.ResourceRegistryInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Display;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Tests for {@link ResourceRegistryInfo}.
 *
 * @author lobas_av
 */
public class ResourceRegistryTest extends RcpModelTest {
	/**
	 * Initial count of dispose {@link Runnable}'s in {@link Display}.
	 */
	private int m_initialDisplayRunnables;

	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		m_initialDisplayRunnables = getDisplayRunnablesCount();
	}

	@Override
	protected void tearDown_afterLastModelDispose() throws Exception {
		super.tearDown_afterLastModelDispose();
		// no any new display runnables expected
		assertEquals(m_initialDisplayRunnables, getDisplayRunnablesCount());
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
	public void test_info() throws Exception {
		setFileContentSrc(
				"test/IRegistry.java",
				getTestSource(
						"public interface IRegistry {",
						"  String X_KEY = '_x_key_';",
						"  StringBuffer SB = new StringBuffer();",
						"  Object object = new Object();",
						"}"));
		setFileContentSrc(
				"test/AbstractRegistry.java",
				getTestSource(
						"public class AbstractRegistry extends ColorRegistry {",
						"  public static final String R_KEY = '_r_key_';",
						"  int XXX = 0;",
						"  public AbstractRegistry() {",
						"    put(R_KEY, new RGB(10, 10, 10));",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyRegistry.java",
				getTestSource(
						"public class MyRegistry extends AbstractRegistry implements IRegistry {",
						"  public static final String R2_KEY = '_r2_key_';",
						"  public static final String R1_KEY = '_r1_key_';",
						"  static String m_zzz;",
						"  private static final String AAA = 'BBB';",
						"  public MyRegistry() {",
						"    put(R2_KEY, new RGB(10, 10, 10));",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private MyRegistry registry = new MyRegistry();",
						"  public Test() {",
						"    setBackground(registry.get(MyRegistry.R1_KEY));",
						"  }",
						"}");
		shell.refresh();
		//
		List<RegistryContainerInfo> children = shell.getChildren(RegistryContainerInfo.class);
		assertEquals(1, children.size());
		RegistryContainerInfo containerInfo = children.get(0);
		//
		assertSame(containerInfo, RegistryContainerInfo.get(shell));
		//
		List<ColorRegistryInfo> colors = containerInfo.getChildren(ColorRegistryInfo.class);
		assertEquals(1, colors.size());
		//
		ColorRegistryInfo colorRegistryInfo = colors.get(0);
		List<KeyFieldInfo> keyFields = colorRegistryInfo.getKeyFields();
		assertEquals(4, keyFields.size());
		//
		assertRegistryKey("R1_KEY", "test.MyRegistry.R1_KEY", "_r1_key_", keyFields.get(0));
		assertRegistryKey("R2_KEY", "test.MyRegistry.R2_KEY", "_r2_key_", keyFields.get(1));
		assertRegistryKey("R_KEY", "test.AbstractRegistry.R_KEY", "_r_key_", keyFields.get(2));
		assertRegistryKey("X_KEY", "test.IRegistry.X_KEY", "_x_key_", keyFields.get(3));
	}

	private static void assertRegistryKey(String keyName,
			String keyAccessSource,
			String keyValue,
			KeyFieldInfo info) throws Exception {
		assertEquals(keyName, info.keyName);
		assertEquals(keyAccessSource, info.keySource);
		assertEquals(keyValue, info.keyValue);
		assertNull(info.value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return count of {@link Runnable}'s in {@link Display#disposeExec(Runnable)}.
	 */
	private int getDisplayRunnablesCount() throws Exception {
		int runnableCount = 0;
		Object disposeList = ReflectionUtils.getFieldObject(Display.getDefault(), "disposeList");
		if (disposeList != null) {
			int length = Array.getLength(disposeList);
			for (int i = 0; i < length; i++) {
				Object runnable = Array.get(disposeList, i);
				if (runnable != null) {
					runnableCount++;
				}
			}
		}
		return runnableCount;
	}
}
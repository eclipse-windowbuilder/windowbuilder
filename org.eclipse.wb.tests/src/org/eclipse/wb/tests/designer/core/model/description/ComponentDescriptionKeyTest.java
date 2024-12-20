/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescriptionKey;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

/**
 * Test for {@link ComponentDescriptionKey}.
 *
 * @author scheglov_ke
 */
public class ComponentDescriptionKeyTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_constructor_nullClass() throws Exception {
		try {
			new ComponentDescriptionKey(null, null, null);
			fail();
		} catch (Throwable e) {
		}
	}

	@Test
	public void test_constructor_nullHost_notNullSuffix() throws Exception {
		try {
			new ComponentDescriptionKey(Object.class, null, "theSuffix");
			fail();
		} catch (Throwable e) {
		}
	}

	@Test
	public void test_constructor_notNullHost_nullSuffix() throws Exception {
		ComponentDescriptionKey host = new ComponentDescriptionKey(Object.class);
		try {
			new ComponentDescriptionKey(Object.class, host, null);
			fail();
		} catch (Throwable e) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// toString()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toString_noHost() throws Exception {
		ComponentDescriptionKey key = new ComponentDescriptionKey(Component.class);
		assertEquals("CDKey(java.awt.Component)", key.toString());
		assertTrue(key.isPureComponent());
	}

	@Test
	public void test_toString_withHost() throws Exception {
		ComponentDescriptionKey host = new ComponentDescriptionKey(Container.class);
		ComponentDescriptionKey key = new ComponentDescriptionKey(Component.class, host, "theSuffix");
		assertEquals("CDKey(java.awt.Component,CDKey(java.awt.Container),theSuffix)", key.toString());
		assertFalse(key.isPureComponent());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// hashCode()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Don't check result of {@link ComponentDescriptionKey#hashCode()}, just call it.
	 */
	@Test
	public void test_hashCode() throws Exception {
		ComponentDescriptionKey key_1 = new ComponentDescriptionKey(Component.class);
		ComponentDescriptionKey key_2 = new ComponentDescriptionKey(Component.class);
		ComponentDescriptionKey key_3 = new ComponentDescriptionKey(Container.class);
		Assertions.assertThat(key_1.hashCode()).isEqualTo(key_2.hashCode());
		Assertions.assertThat(key_1.hashCode()).isNotEqualTo(key_3.hashCode());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// equals()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_equals_noHost() throws Exception {
		ComponentDescriptionKey key_1 = new ComponentDescriptionKey(Component.class);
		ComponentDescriptionKey key_2 = new ComponentDescriptionKey(Component.class);
		ComponentDescriptionKey key_3 = new ComponentDescriptionKey(Container.class);
		Assertions.assertThat(key_1).isNotEqualTo(this);
		Assertions.assertThat(key_1).isEqualTo(key_1);
		Assertions.assertThat(key_1).isEqualTo(key_2);
		Assertions.assertThat(key_1).isNotEqualTo(key_3);
	}

	@Test
	public void test_equals_withHost() throws Exception {
		ComponentDescriptionKey host_1 = new ComponentDescriptionKey(List.class);
		ComponentDescriptionKey host_2 = new ComponentDescriptionKey(Map.class);
		ComponentDescriptionKey key_1 =
				new ComponentDescriptionKey(Component.class, host_1, "suffix_1");
		ComponentDescriptionKey key_2 =
				new ComponentDescriptionKey(Component.class, host_1, "suffix_2");
		ComponentDescriptionKey key_3 =
				new ComponentDescriptionKey(Component.class, host_2, "suffix_1");
		ComponentDescriptionKey key_4 =
				new ComponentDescriptionKey(Component.class, host_2, "suffix_2");
		ComponentDescriptionKey key_5 =
				new ComponentDescriptionKey(Component.class, host_1, "suffix_1");
		Assertions.assertThat(key_1).isEqualTo(key_1);
		Assertions.assertThat(key_1).isNotEqualTo(key_2);
		Assertions.assertThat(key_1).isNotEqualTo(key_3);
		Assertions.assertThat(key_1).isNotEqualTo(key_4);
		Assertions.assertThat(key_1).isEqualTo(key_5);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getComponentClass()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getComponentClass() throws Exception {
		ComponentDescriptionKey key = new ComponentDescriptionKey(Component.class);
		assertSame(key.getComponentClass(), Component.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getName()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getName_noHost() throws Exception {
		ComponentDescriptionKey key = new ComponentDescriptionKey(Component.class);
		assertEquals("java/awt/Component", key.getName());
	}

	@Test
	public void test_getName_withHost() throws Exception {
		ComponentDescriptionKey host = new ComponentDescriptionKey(Container.class);
		ComponentDescriptionKey key = new ComponentDescriptionKey(Component.class, host, "theSuffix");
		assertEquals("java/awt/Container.theSuffix", key.getName());
	}

	@Test
	public void test_getName_withHost2() throws Exception {
		ComponentDescriptionKey key_1 = new ComponentDescriptionKey(Container.class);
		ComponentDescriptionKey key_2 =
				new ComponentDescriptionKey(Component.class, key_1, "firstSuffix");
		ComponentDescriptionKey key_3 =
				new ComponentDescriptionKey(JButton.class, key_2, "secondSuffix");
		assertEquals("java/awt/Container.firstSuffix.secondSuffix", key_3.getName());
	}
}

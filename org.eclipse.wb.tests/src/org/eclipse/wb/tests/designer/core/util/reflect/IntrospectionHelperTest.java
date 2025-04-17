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
package org.eclipse.wb.tests.designer.core.util.reflect;

import org.eclipse.wb.internal.core.utils.reflect.IntrospectionHelper;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.Test;

import java.beans.BeanDescriptor;
import java.beans.Customizer;
import java.beans.SimpleBeanInfo;

import javax.swing.JPanel;

/**
 * Test for {@link IntrospectionHelper}.
 *
 * @author scheglov_ke
 */
public class IntrospectionHelperTest extends DesignerTestCase {
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
	/**
	 * Test for {@link IntrospectionHelper#getBeanDescriptor()}.
	 */
	@Test
	public void test_getBeanDescriptor_BeanA() throws Exception {
		IntrospectionHelper helper = new IntrospectionHelper(BeanA.class);
		BeanDescriptor beanDescriptor = helper.getBeanDescriptor();
		assertSame(BeanCustomizer.class, beanDescriptor.getCustomizerClass());
		assertSame(Boolean.TRUE, beanDescriptor.getValue("attr_1"));
	}

	/**
	 * Test for {@link IntrospectionHelper#getBeanDescriptor()}.
	 * <p>
	 * {@link BeanB} inherits customizer and attributes from {@link BeanA}.
	 */
	@Test
	public void test_getBeanDescriptor_BeanB() throws Exception {
		IntrospectionHelper helper = new IntrospectionHelper(BeanB.class);
		BeanDescriptor beanDescriptor = helper.getBeanDescriptor();
		assertSame(BeanCustomizer.class, beanDescriptor.getCustomizerClass());
		assertSame(Boolean.TRUE, beanDescriptor.getValue("attr_1"));
	}

	/**
	 * Test for {@link IntrospectionHelper#getBeanDescriptor()}.
	 * <p>
	 * {@link BeanC} inherits customizer, but overrides attributes.
	 */
	@Test
	public void test_getBeanDescriptor_BeanC() throws Exception {
		IntrospectionHelper helper = new IntrospectionHelper(BeanC.class);
		BeanDescriptor beanDescriptor = helper.getBeanDescriptor();
		assertSame(BeanCustomizer.class, beanDescriptor.getCustomizerClass());
		assertSame(Boolean.FALSE, beanDescriptor.getValue("attr_1"));
		assertSame(5, beanDescriptor.getValue("attr_2"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// BeanA
	//
	////////////////////////////////////////////////////////////////////////////
	public static class BeanA {
	}
	public static class BeanABeanInfo extends SimpleBeanInfo {
		private final static Class<?> beanClass = BeanA.class;
		private final static Class<?> customizerClass = BeanCustomizer.class;

		@Override
		public BeanDescriptor getBeanDescriptor() {
			final BeanDescriptor result = new BeanDescriptor(beanClass, customizerClass);
			result.setValue("attr_1", Boolean.TRUE);
			return result;
		}
	}
	public static class BeanCustomizer extends JPanel implements Customizer {
		private static final long serialVersionUID = 0L;

		@Override
		public void setObject(Object bean) {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// BeanB
	//
	////////////////////////////////////////////////////////////////////////////
	public static class BeanB extends BeanA {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// BeanC
	//
	////////////////////////////////////////////////////////////////////////////
	public static class BeanC extends BeanA {
	}
	public static class BeanCBeanInfo extends SimpleBeanInfo {
		private final static Class<?> beanClass = BeanC.class;

		@Override
		public BeanDescriptor getBeanDescriptor() {
			final BeanDescriptor result = new BeanDescriptor(beanClass, null);
			result.setValue("attr_1", Boolean.FALSE);
			result.setValue("attr_2", 5);
			return result;
		}
	}
}

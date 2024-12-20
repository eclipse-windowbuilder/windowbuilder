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
package org.eclipse.wb.tests.designer.databinding.swing;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.model.beans.FieldBeanObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericTypeContainer;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * @author sablin_aa
 *
 */
@Ignore
public class GenericUtilsTest extends AbstractBindingTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getObjectType() throws Exception {
		JPanelInfo shell =
				DatabindingTestUtils.parseTestSource(this, new String[]{
						"import java.util.List;",
						"import java.util.Map;",
						"public class Test extends JPanel {",
						"  private List<Map<String,Number>> datas;",
						"  public Test() {",
						"  }",
				"}"});
		assertNotNull(shell);
		//
		DatabindingsProvider provider = getDatabindingsProvider();
		List<IObserveInfo> beanObserves = provider.getObserves(ObserveType.BEANS);
		List<FieldBeanObserveInfo> fieldObserves =
				GenericsUtils.select(beanObserves, FieldBeanObserveInfo.class);
		FieldBeanObserveInfo datas = fieldObserves.get(0);
		IGenericType datasObjectType = datas.getObjectType();
		//
		assertInstanceOf(GenericTypeContainer.class, datasObjectType);
		GenericTypeContainer datasType = (GenericTypeContainer) datasObjectType;
		assertEquals(datasType.getFullTypeName(), "java.util.List<java.util.Map<java.lang.String, java.lang.Number>>");
		//
		IGenericType datasItemObjectType = datasType.getSubType(0);
		//
		assertInstanceOf(GenericTypeContainer.class, datasItemObjectType);
		GenericTypeContainer datasItemType = (GenericTypeContainer) datasItemObjectType;
		assertEquals(datasItemType.getFullTypeName(), "java.util.Map<java.lang.String, java.lang.Number>");
		//
		List<IGenericType> subTypes = datasItemType.getSubTypes();
		assertEquals(subTypes.size(), 2);
	}
}
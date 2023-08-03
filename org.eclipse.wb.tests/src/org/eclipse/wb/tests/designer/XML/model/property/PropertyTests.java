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
package org.eclipse.wb.tests.designer.XML.model.property;

import org.eclipse.wb.internal.core.model.property.Property;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * XML {@link Property} tests.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		XmlPropertyTest.class,
		EmptyXmlPropertyTest.class,
		XmlAttributePropertyTest.class,
		PropertyTest.class,
		StaticFieldPropertyEditorTest.class,
		EnumPropertyEditorTest.class,
		StringArrayPropertyEditorTest.class,
		EventsPropertyTest.class,
})
public class PropertyTests {
}
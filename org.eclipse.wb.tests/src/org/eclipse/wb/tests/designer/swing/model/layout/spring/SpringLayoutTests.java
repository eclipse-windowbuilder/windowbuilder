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
package org.eclipse.wb.tests.designer.swing.model.layout.spring;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import javax.swing.SpringLayout;

/**
 * Tests for {@link SpringLayout}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		SpringAttachmentTest.class,
		SpringLayoutTest.class,
		SpringLayoutGefTest.class
})
public class SpringLayoutTests {
}

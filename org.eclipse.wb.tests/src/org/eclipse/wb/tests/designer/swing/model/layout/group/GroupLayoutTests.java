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
package org.eclipse.wb.tests.designer.swing.model.layout.group;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import javax.swing.GroupLayout;

/**
 * Tests for {@link GroupLayout}.
 *
 * @author mitin_aa
 */
@RunWith(Suite.class)
@SuiteClasses({
		GroupLayoutTest.class,
		GroupLayoutGefTest.class
})
public class GroupLayoutTests {
}

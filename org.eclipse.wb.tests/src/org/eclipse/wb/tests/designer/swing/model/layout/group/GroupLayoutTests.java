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

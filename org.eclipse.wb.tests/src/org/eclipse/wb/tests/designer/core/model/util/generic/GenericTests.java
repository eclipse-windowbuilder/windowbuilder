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
package org.eclipse.wb.tests.designer.core.model.util.generic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test for generic, description driven features.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		CopyPropertyTopChildTest.class,
		CopyPropertyTopTest.class,
		ModelMethodPropertyTest.class,
		ModelMethodPropertyChildTest.class
})
public class GenericTests {
}

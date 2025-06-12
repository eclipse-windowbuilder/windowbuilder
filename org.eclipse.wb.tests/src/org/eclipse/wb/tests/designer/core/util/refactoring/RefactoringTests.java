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
package org.eclipse.wb.tests.designer.core.util.refactoring;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.ltk.core.refactoring.Change;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link ReflectionUtils} and its {@link Change}s.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({ RefactoringUtilsTest.class })
public class RefactoringTests {
}

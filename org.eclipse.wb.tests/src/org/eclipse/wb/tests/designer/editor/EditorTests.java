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
package org.eclipse.wb.tests.designer.editor;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
    //basic policy
    TopSelectionEditPolicyTest.class,
    // basic features
    UndoManagerTest.class,
    ContentDescriberTest.class,
    ReparseOnModificationTest.class,
    SelectSupportTest.class,
    ComponentsPropertiesPageTest.class,
    JavaPropertiesToolBarContributorTest.class
})

public class EditorTests {
}

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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath.ClasspathImageRoot;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link AbstractImagePage} that selects image as class path resource.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ClasspathImagePage extends AbstractBrowseImagePage {
	public static final String ID = "CLASSPATH";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ClasspathImagePage(Composite parent,
			int style,
			AbstractImageDialog imageDialog,
			IJavaProject javaProject) {
		super(parent, style, imageDialog, new ClasspathImageRoot(ID, javaProject));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getTitle() {
		return Messages.ClasspathImagePage_title;
	}
}

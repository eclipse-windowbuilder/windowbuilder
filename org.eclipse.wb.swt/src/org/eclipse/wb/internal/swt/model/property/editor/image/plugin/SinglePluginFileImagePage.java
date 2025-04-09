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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;
import org.eclipse.wb.internal.swt.model.ModelMessages;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link AbstractImagePage} that selects image as single plugin resource.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public class SinglePluginFileImagePage extends AbstractBrowseImagePage {
	public static final String ID = "PLUGIN";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SinglePluginFileImagePage(Composite parent,
			int style,
			AbstractImageDialog imageDialog,
			IProject project) {
		super(parent, style, imageDialog, new SinglePluginImagesRoot(project));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractImagePage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getTitle() {
		return ModelMessages.SinglePluginFileImagePage_title;
	}
}
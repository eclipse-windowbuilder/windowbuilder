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
package org.eclipse.wb.internal.swing.model.property.editor.icon;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.icon.AbstractImagePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.ClasspathImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.DefaultImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.FileImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.NullImagePage;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javax.swing.Icon;

/**
 * Implementation of {@link PropertyEditor} for {@link Icon}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class IconPropertyEditor extends AbstractImagePropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new IconPropertyEditor();

	private IconPropertyEditor() {
		super(Icon.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ImageDialog
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractImageDialog createImageDialog(IJavaProject javaProject) {
		return new ImageDialog(javaProject);
	}

	private static final class ImageDialog extends AbstractImageDialog {
		private final IJavaProject m_javaProject;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		protected ImageDialog(IJavaProject javaProject) {
			super(DesignerPlugin.getShell(), Activator.getDefault());
			m_javaProject = javaProject;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Pages
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void addPages(Composite parent) {
			addPage(new ClasspathImagePage(parent, SWT.NONE, this, m_javaProject));
			addPage(new FileImagePage(parent, SWT.NONE, this));
			addPage(new NullImagePage(parent, SWT.NONE, this));
			addPage(new DefaultImagePage(parent, SWT.NONE, this));
		}
	}
}

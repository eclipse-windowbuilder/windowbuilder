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
package org.eclipse.wb.internal.swt.model.property.editor.image;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.icon.AbstractImagePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.ClasspathImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.DefaultImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.FileImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.NullImagePage;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginFileImagePage;
import org.eclipse.wb.internal.swt.model.property.editor.image.plugin.PluginImagesRoot;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link PropertyEditor} for {link org.eclipse.jface.resource.ImageDescriptor}.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @coverage swt.property.editor
 */
public final class ImageDescriptorPropertyEditor extends AbstractImagePropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new ImageDescriptorPropertyEditor();

	private ImageDescriptorPropertyEditor() {
		super(ImageDescriptor.class);
	}

	/**
	 * Returns the Java code required for creating a new {@link ImageDescriptor}
	 * instance.
	 *
	 * @param location the class whose resource directory contain the file
	 * @param filename the file name
	 * @return {@code ImageDescriptor.createFromFile(<clazz>, <path>)}
	 * @see ImageDescriptor#createFromFile(Class, String)
	 */
	public static String getInvocationSource(String location, String filename) {
		return String.format("org.eclipse.jface.resource.ImageDescriptor.createFromFile(%s, %s)", //
				location, filename);
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
			if (PluginImagesRoot.testPluginProject(m_javaProject.getProject())) {
				addPage(PluginFileImagePage.createPage(parent, SWT.NONE, this, m_javaProject.getProject()));
			}
		}
	}
}
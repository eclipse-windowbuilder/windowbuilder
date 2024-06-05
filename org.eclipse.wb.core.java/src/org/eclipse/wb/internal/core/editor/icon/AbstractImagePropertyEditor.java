/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH, Aachen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    DSA GmbH, Aachen - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.icon;

import org.eclipse.wb.core.editor.icon.AbstractClasspathImageProcessor;
import org.eclipse.wb.core.model.IImageInfo;
import org.eclipse.wb.core.model.IImageProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;

/**
 * Common base class used for all image editors of the {@link PropertyTable}.
 */
public abstract class AbstractImagePropertyEditor extends TextDialogPropertyEditor implements IClipboardSourceProvider {
	private final Class<?> imageType;

	/**
	 * @param imageType The type of image that is modified by this editor.
	 */
	protected AbstractImagePropertyEditor(Class<?> imageType) {
		this.imageType = imageType;
	}

	/**
	 * @param javaProject The java project of the currently open editor.
	 * @return A new image dialog for the selected {@link GenericProperty}.
	 */
	protected abstract AbstractImageDialog createImageDialog(IJavaProject javaProject);

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		if (property.getValue() != Property.UNKNOWN_VALUE) {
			String[] value = new String[] { null };
			ImageProcessorHelper.process(imageType, p -> p.process((GenericProperty) property, value));
			return value[0];
		}
		// unknown value
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		if (property.getValue() != Property.UNKNOWN_VALUE) {
			String[] text = new String[] { null };
			IImageProcessor imageProcessor = ImageProcessorHelper.process(imageType, p -> p.process(property, text));
			if (imageProcessor != null) {
				Object[] path = new Object[] { null };
				imageProcessor.preOpen(property, text[0], path);
				String[] source = new String[] { null };
				if (imageProcessor instanceof AbstractClasspathImageProcessor classpathProcessor) {
					classpathProcessor.postOpen(property, (String) path[0], source);
				} else {
					IImageInfo imageInfo = new ImageInfo(imageProcessor.getPageId(), path[0], null, 0);
					imageProcessor.postOpen(property, imageInfo, source);
				}
				return source[0];
			}
		}
		// unknown image pattern
		return null;
	}

	@Override
	protected void openDialog(Property property) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		JavaInfo javaInfo = genericProperty.getJavaInfo();
		AstEditor editor = javaInfo.getEditor();
		IJavaProject javaProject = editor.getJavaProject();
		// create dialog
		AbstractImageDialog imageDialog = createImageDialog(javaProject);
		// set input for dialog
		String text = getText(property);
		Object[] input = new Object[] { null };
		IImageProcessor imageProcessor = ImageProcessorHelper.process(imageType,
				p -> p.preOpen(genericProperty, text, input));
		if (imageProcessor != null) {
			imageDialog.setInput(imageProcessor.getPageId(), input[0]);
		}
		// open dialog
		if (imageDialog.open() == Window.OK) {
			ImageInfo imageInfo = imageDialog.getImageInfo();
			// prepare source
			String[] source = new String[] { null };
			ImageProcessorHelper.process(imageType, p -> p.postOpen(genericProperty, imageInfo, source));
			// set expression
			genericProperty.setExpression(source[0], Property.UNKNOWN_VALUE);
		}
	}
}

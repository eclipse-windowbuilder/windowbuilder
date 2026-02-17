/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.os.linux;

import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Creates a screenshot of a given widget.
 */
public abstract class ScreenshotMaker {
	// constants
	private static final Color TITLE_BORDER_COLOR_DARKEST = DrawUtils.getShiftedColor(ColorConstants.titleBackground,
			-24);
	private static final Color TITLE_BORDER_COLOR_DARKER = DrawUtils.getShiftedColor(ColorConstants.titleBackground,
			-16);

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Map<GtkWidget, Control> m_controlsRegistry;

	/**
	 * Prepares to screen shot: register controls. See
	 * {@link #registerControl(Control)} for details.
	 */
	private void prepareScreenshot(Shell shell) {
		m_controlsRegistry = new HashMap<>();
		registerControl(shell);
		registerByHandle(shell);
	}

	/**
	 * Registers the control to be checked in screen shot callback. Every control
	 * can be registered multiple times. The first image handle received for this
	 * control in callback is "root" for this control and should be bound as
	 * {@link Image}.
	 */
	private void registerControl(Control control) {
		// check size
		Point size = control.getSize();
		if (size.x == 0 || size.y == 0) {
			return;
		}
		{
			registerByHandle(control);
			registerByHandle(control);
		}
		control.setData(OSSupport.WBP_IMAGE, null);
		// traverse children
		if (control instanceof Composite composite) {
			for (Control child : composite.getChildren()) {
				registerControl(child);
			}
		}
	}

	/**
	 * Tries to get the {@code handle} from {@code control}. If the handle exists,
	 * fills {@code m_needsImage}.
	 */
	private void registerByHandle(Control control) {
		GtkWidget widget = new GtkWidget(control);
		if (widget.segment() != MemorySegment.NULL) {
			m_controlsRegistry.put(widget, control);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Screen shot
	//
	////////////////////////////////////////////////////////////////////////////

	public void makeShots(Control control) {
		Shell shell = control.getShell();
		makeShots0(shell);
		// check for decorations and draw if needed
		drawDecorations(shell, shell.getDisplay());
	}

	/**
	 * Screen shot algorithm is the following:
	 *
	 * <pre>
	 * 1. Register controls which requires the image. See {@link #registerControl(Control)}.
	 * 2. Create the callback, binding the screenshot to the model. See {@link #makeShot(Shell, BiConsumer)
	 * 3. While traversing the gtk widgets/gdk windows, the callback returns native widget handle and the
	 *    image. If the control corresponding to the widget handle has been found in the registry the
	 *    received image is bound to the control (see {@link #bindImage(Display, Control, int)}).
	 *    Otherwise, the image is disposed later (because it may be used in drawing).
	 * </pre>
	 */
	private void makeShots0(final Shell shell) {
		prepareScreenshot(shell);
		final Set<Image> disposeImages = new HashSet<>();
		// apply shot magic
		makeShot(shell, (handle, image) -> {
			// get the registered control by handle
			Control imageForControl = m_controlsRegistry.get(handle);
			if (imageForControl == null || !bindImage(imageForControl, image)) {
				// this means given image handle used to draw the gtk widget internally
				disposeImages.add(image);
			}
		});
		// done, dispose image handles needed to draw internally.
		for (Image image : disposeImages) {
			image.dispose();
		}
	}

	/**
	 * Causes taking the screen shot.
	 *
	 * @param shell    the root {@link Shell} to capture.
	 * @param callback the callback instance for binding the snapshot to the data
	 *                 model. Can be <code>null</code>.
	 * @return the GdkPixmap* or cairo_surface_t* of {@link Shell}.
	 */
	protected abstract Image makeShot(Shell shell, BiConsumer<GtkWidget, Image> callback);

	private boolean bindImage(final Control control, final Image image) {
		if (control.getData(OSSupport.WBP_NEED_IMAGE) != null && control.getData(OSSupport.WBP_IMAGE) == null) {
			control.setData(OSSupport.WBP_IMAGE, image);
			return true;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Draws decorations if available/applicable.
	 */
	private void drawDecorations(Shell shell, final Display display) {
		Image shellImage = (Image) shell.getData(OSSupport.WBP_IMAGE);
		// draw title if any
		if (shellImage != null && (shell.getStyle() & SWT.TITLE) != 0) {
			Rectangle shellBounds = shell.getBounds();
			Rectangle imageBounds = shellImage.getBounds();
			Point offset = shell.toControl(shell.getLocation());
			offset.x = -offset.x;
			offset.y = -offset.y;
			// adjust by menu bar size
			if (shell.getMenuBar() != null) {
				offset.y -= OSSupportLinux.getWidgetBounds(shell.getMenuBar()).height;
			}
			// draw
			Image decoratedShellImage = new Image(display, shellBounds.width, shellBounds.height);
			GC gc = new GC(decoratedShellImage);
			// draw background
			gc.setBackground(ColorConstants.titleBackground);
			gc.fillRectangle(0, 0, shellBounds.width, shellBounds.height);
			// title area gradient
			gc.setForeground(ColorConstants.titleGradient);
			gc.fillGradientRectangle(0, 0, shellBounds.width, offset.y, true);
			int buttonGapX = offset.x - 1;
			int nextPositionX;
			// buttons and title
			{
				// menu button
				Image buttonImage = Activator.getImage("decorations/button-menu-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, buttonGapX, buttonOffsetY);
				nextPositionX = buttonGapX + buttonImageBounds.width + buttonGapX;
			}
			{
				// close button
				Image buttonImage = Activator.getImage("decorations/button-close-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				nextPositionX = shellBounds.width - buttonImageBounds.width - buttonGapX;
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, nextPositionX, buttonOffsetY);
				nextPositionX -= buttonGapX + buttonImageBounds.width;
			}
			{
				// maximize button
				Image buttonImage = Activator.getImage("decorations/button-max-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, nextPositionX, buttonOffsetY);
				nextPositionX -= buttonGapX + buttonImageBounds.width;
			}
			{
				// minimize button
				Image buttonImage = Activator.getImage("decorations/button-min-icon.png");
				Rectangle buttonImageBounds = buttonImage.getBounds();
				int buttonOffsetY = offset.y / 2 - buttonImageBounds.height / 2;
				gc.drawImage(buttonImage, nextPositionX, buttonOffsetY);
			}
			// outline
			gc.setForeground(TITLE_BORDER_COLOR_DARKEST);
			gc.drawRectangle(offset.x - 1, offset.y - 1, imageBounds.width + 1, imageBounds.height + 1);
			gc.setForeground(TITLE_BORDER_COLOR_DARKER);
			gc.drawRectangle(offset.x - 2, offset.y - 2, imageBounds.width + 3, imageBounds.height + 3);
			// shell screen shot
			gc.drawImage(shellImage, offset.x, offset.y);
			// done
			gc.dispose();
			shellImage.dispose();
			shell.setData(OSSupport.WBP_IMAGE, decoratedShellImage);
		}
	}
}

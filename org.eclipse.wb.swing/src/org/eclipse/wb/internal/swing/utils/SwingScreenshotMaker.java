/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.utils;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang3.StringUtils;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Creates screenshots for given Swing top component.
 *
 * @author mitin_aa
 */
@SuppressWarnings("removal")
public final class SwingScreenshotMaker {
	private final AbstractComponentInfo m_root;
	private final Map<Component, java.awt.Image> m_componentImages = new HashMap<>();
	private final Component m_component;
	private Window m_window;
	private Point m_oldComponentLocation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwingScreenshotMaker(AbstractComponentInfo rootModel, Component rootComponent) {
		m_root = rootModel;
		SwingUtils.ensureQueueEmpty();
		// fill images map with key
		m_componentImages.put(rootComponent, null);
		rootModel.accept(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				if (objectInfo instanceof ComponentInfo) {
					m_componentImages.put(((ComponentInfo) objectInfo).getComponent(), null);
				}
			}
		});
		// prepare component
		m_component = rootComponent;
		if (m_component instanceof Window) {
			m_window = (Window) m_component;
		} else {
			JFrame frame = new JFrame();
			// The JFrame decorations are causing issues when taking the screenshot on
			// Windows and are not relevant for the captured image.
			frame.setUndecorated(true);
			frame.setPreferredSize(m_component.getSize());
			m_window = frame;
			// configure panel to have same size as given component
			JPanel panel;
			{
				panel = new JPanel(new BorderLayout());
				panel.setPreferredSize(m_component.getSize());
				frame.getContentPane().add(panel, BorderLayout.CENTER);
			}
			// add component
			if (m_component instanceof JInternalFrame) {
				JDesktopPane desktop = new JDesktopPane();
				desktop.setLayout(new BorderLayout());
				panel.add(desktop);
				desktop.add(m_component);
				m_component.setVisible(true);
			} else {
				panel.add(m_component);
			}
			frame.pack();
		}
		// Just clearing the "resizable" flag doesn't seem to be sufficient on Windows
		// and the edges are still cut off when they are larger than the screen. I've
		// traced the problem back to a native call to
		// sun.awt.windows.WWindowPeer.reshapeFrame, so this is something beyond our
		// control. The only way to resolve this issue is by "forcing" the frame to be
		// at least as large as the component we try to capture.
		if (EnvironmentUtils.IS_WINDOWS) {
			m_window.setMinimumSize(m_window.getPreferredSize());
		}
	}

	/**
	 * Traverses through components hierarchy and prepares screen shot for every component needed.
	 *
	 * Note: it doesn't make top component not visible, use {@link #dispose()} for that.
	 *
	 * Important note: must be called from AWT dispatch thread.
	 */
	public void makeShots() throws Exception {
		SwingImageUtils.checkForDialog(m_component);
		final int componentWidth = Math.max(1, m_component.getWidth());
		final int componentHeight = Math.max(1, m_component.getHeight());
		m_oldComponentLocation = m_component.getLocation();
		boolean isResizable = false;
		// When the size of the frame exceeds the size of the screen, then the frame
		// will be maximized instead. As a result, the edges of the frame outside the
		// visible area won't be captured. This behavior can be explicitly disabled by
		// simply clearing the "resizable" flag.
		if (m_window instanceof Frame frame) {
			isResizable = frame.isResizable();
			frame.setResizable(false);
		}
		SwingImageUtils.prepareForPrinting(m_window);
		fixJLabelWithHTML(m_component);
		// prepare empty image
		BufferedImage image;
		// prepare window image
		final BufferedImage windowImage;
		// print component and its children
		{
			int windowWidth = Math.max(1, m_window.getWidth());
			int windowHeight = Math.max(1, m_window.getHeight());
			windowImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
			m_window.printAll(windowImage.getGraphics());
		}
		// prepare component image
		if (m_component == m_window) {
			image = windowImage;
		} else {
			// prepare location of component image on window image
			Point componentLocation;
			{
				Point p_component;
				if (m_component instanceof Applet) {
					// Applet reports "screen location" as (0,0), but we want location on "window"
					p_component = SwingUtils.getScreenLocation(m_component.getParent());
				} else {
					p_component = SwingUtils.getScreenLocation(m_component);
				}
				// continue
				Point p_window = SwingUtils.getScreenLocation(m_window);
				componentLocation = new Point(p_component.x - p_window.x, p_component.y - p_window.y);
			}
			// copy part of window image
			BufferedImage componentImage =
					new BufferedImage(componentWidth, componentHeight, BufferedImage.TYPE_INT_RGB);
			componentImage.getGraphics().drawImage(
					windowImage,
					0,
					0,
					componentWidth,
					componentHeight,
					componentLocation.x,
					componentLocation.y,
					componentLocation.x + componentWidth,
					componentLocation.y + componentHeight,
					m_window);
			image = componentImage;
		}
		// store image for top-level first
		m_componentImages.put(m_component, image);
		// do traverse
		SwingImageUtils.makeShotsHierarchy(m_component, m_componentImages, m_component);
		// convert images
		final Map<Component, Image> convertedImages = new HashMap<>();
		for (Component keyComponent : Collections.unmodifiableMap(m_componentImages).keySet()) {
			java.awt.Image image2 = m_componentImages.get(keyComponent);
			if (image2 != null) {
				convertedImages.put(keyComponent, SwingImageUtils.convertImage_AWT_to_SWT(image2).createImage());
			}
		}
		// draw decorations on OS X
		if (EnvironmentUtils.IS_MAC && m_window == m_component) {
			Image oldImage = convertedImages.get(m_component);
			convertedImages.put(m_component, SwingImageUtils.createOSXImage(m_window, oldImage));
			if (oldImage != null) {
				oldImage.dispose();
			}
		}
		if (m_window instanceof Frame frame) {
			frame.setResizable(isResizable);
		}
		// set images
		m_root.accept(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				if (objectInfo instanceof AbstractComponentInfo componentInfo) {
					Object componentObject = componentInfo.getComponentObject();
					Image image = convertedImages.get(componentObject);
					componentInfo.setImage(image);
				}
			}
		});
	}

	/**
	 * Fix for {@link JLabel} with "html" as text.
	 */
	private static void fixJLabelWithHTML(Component component) throws Exception {
		if (component instanceof JLabel label) {
			String text = label.getText();
			if (StringUtils.containsIgnoreCase(text, "<html>")) {
				SwingImageUtils.createComponentShotAWT(component);
			}
		}
		// process children
		if (component instanceof Container container) {
			for (Component childComponent : container.getComponents()) {
				fixJLabelWithHTML(childComponent);
			}
		}
	}

	/**
	 * Restores the top component state: restores location and makes it invisible.
	 */
	public void dispose() throws Exception {
		if (m_window == null || m_component == null) {
			return;
		}
		// hide only if it is not part of bigger hierarchy (for example embedded into SWT)
		if (m_root.isRoot()) {
			SwingImageUtils.setVisible(m_window, false);
		}
		// restore location
		if (m_oldComponentLocation != null) {
			int deltaX = m_component.getLocation().x - m_oldComponentLocation.x;
			int deltaY = m_component.getLocation().y - m_oldComponentLocation.y;
			moveRectangleSafe(m_root.getBounds(), deltaX, deltaY);
			moveRectangleSafe(m_root.getModelBounds(), deltaX, deltaY);
		}
		// dispose temporary window
		if (m_window != m_component) {
			if (m_window instanceof JFrame) {
				((JFrame) m_window).setContentPane(new JPanel());
			}
			m_window.dispose();
		}
	}

	/**
	 * Moves given {@link Rectangle}, if it is not <code>null</code>.
	 */
	private static void moveRectangleSafe(Rectangle bounds, int deltaX, int deltaY) {
		if (bounds != null) {
			bounds.x -= deltaX;
			bounds.y -= deltaY;
		}
	}
}

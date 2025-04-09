/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package kmeans;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * This class is used to draw the image that is created as part of the k-means
 * color reduction. Its purpose is to not only show that WindowBuilder is able
 * to handle sub-classes of Swing components, but to also avoid having to edit
 * the generated code that creates this object..
 */
public class KMeansPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage content;

	@Override
	protected void paintComponent(Graphics gc) {
		gc.setColor(getBackground());
		gc.fillRect(0, 0, getWidth(), getHeight());
		//
		if (content != null) {
			// if the image is smaller than the panel, draw it in the center
			int x = 0;
			if (content.getWidth() < getWidth()) {
				x = ((getWidth() - content.getWidth()) / 2);
			}
			//
			int y = 0;
			if (content.getHeight() < getHeight()) {
				y = ((getHeight() - content.getHeight()) / 2);
			}
			//
			gc.drawImage(content, x, y, null);
		}
	}

	/**
	 * Updates the image that is drawn on the {@link JPanel}. If the image is
	 * {@code null}, an empty image is drawn.
	 * 
	 * @param image The image to draw. May be {@code null}.
	 */
	public void setContent(BufferedImage image) {
		this.content = image;
	}
}

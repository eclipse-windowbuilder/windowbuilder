/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH, Aachen and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    DSA GmbH, Aachen - initial API and implementation
 *******************************************************************************/
package imageProcessor;

import javax.swing.ImageIcon;

/**
 * "Custom" image icon to be used instead of the default Swing class.
 */
public class SwingImageIcon extends ImageIcon {
	private static final long serialVersionUID = -6087490305406355920L;

	public SwingImageIcon (String filename) {
		super(filename);
	}
}

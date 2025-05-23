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
package org.eclipse.wb.core.editor.palette.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.palette.command.Command;

import org.eclipse.swt.widgets.Shell;

/**
 * External interface for palette access.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public interface IPaletteSite {
	/**
	 * @return the {@link Shell} that contains palette widget.
	 */
	Shell getShell();

	/**
	 * @return the {@link PaletteInfo} displayed currently in this {@link IPaletteSite}.
	 */
	PaletteInfo getPalette();

	/**
	 * Adds some external palette {@link Command}.
	 */
	void addCommand(Command command);

	/**
	 * Opens palette editing dialog.<br>
	 * Note, that after this method {@link #getPalette()} may return different instance of
	 * {@link PaletteInfo} .
	 */
	void editPalette();

	////////////////////////////////////////////////////////////////////////////
	//
	// Helper
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Helper for accessing {@link IPaletteSite}.
	 *
	 * @author scheglov_ke
	 */
	public static class Helper {
		private static final String KEY = "key_IPaletteContainer";

		/**
		 * @return {@link IPaletteSite} for given {@link ObjectInfo}.
		 */
		public static IPaletteSite getSite(ObjectInfo objectInfo) {
			return (IPaletteSite) objectInfo.getRoot().getArbitraryValue(KEY);
		}

		/**
		 * Sets the {@link IPaletteSite} for given {@link ObjectInfo}.
		 */
		public static void setSite(ObjectInfo objectInfo, IPaletteSite site) {
			objectInfo.getRoot().putArbitraryValue(KEY, site);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Empty implementation
	//
	////////////////////////////////////////////////////////////////////////////
	public static class Empty implements IPaletteSite {
		@Override
		public void addCommand(Command command) {
		}

		@Override
		public void editPalette() {
		}

		@Override
		public PaletteInfo getPalette() {
			return null;
		}

		@Override
		public Shell getShell() {
			return null;
		}
	}
}

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
package org.eclipse.wb.internal.swing.model.property.editor.border.pages;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderValue;
import org.eclipse.wb.internal.swing.model.property.editor.font.FontInfo;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Implementation of {@link AbstractBorderComposite} that sets {@link Border} from {@link UIManager}
 * .
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class SwingBorderComposite extends AbstractBorderComposite {
	private final List m_bordersList;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwingBorderComposite(Composite parent) {
		super(parent, "Swing");
		GridLayoutFactory.create(this);
		m_bordersList = new List(this, SWT.BORDER | SWT.V_SCROLL);
		GridDataFactory.create(m_bordersList).hintVC(10).grab().fill();
		// fill Border's
		prepareBorders();
		for (String key : m_borderKeys) {
			m_bordersList.add(key);
		}
		// selection listener
		m_bordersList.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				m_borderDialog.borderUpdated();
			}
		});
		// set defaults values
		m_bordersList.deselectAll();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public CompletableFuture<Void> setBorderValue(BorderValue borderValue) {
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be called from the AWT event dispatcher thread");
		// note, that this algorithm is not ideal, because we can not identify "key" by Border,
		// and we don't have AST Expression, so we try to do our best, but can fail...
		Border border = borderValue.getValue();
		if (border != null) {
			String borderClassName = border.getClass().getName();
			if (borderClassName.indexOf('$') != -1) {
				for (int i = 0; i < m_borders.size(); i++) {
					if (m_borders.get(i).getClass() == border.getClass()) {
						// OK, this is our Border
						int index = i;
						return ExecutionUtils.runLogLater(() -> {
							m_bordersList.select(index);
							m_bordersList.showSelection();
						});
					}
				}
			}
		}
		// no, we don't know this Border
		return null;
	}

	@Override
	public String getSource() {
		int index = m_bordersList.getSelectionIndex();
		if (index != -1) {
			String key = m_borderKeys.get(index);
			return "javax.swing.UIManager.getBorder(\"" + key + "\")";
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	private static java.util.List<String> m_borderKeys;
	private static java.util.List<Border> m_borders;

	/**
	 * @return {@code true}, if this composite can manage the given border.
	 */
	/* package */ static boolean contains(Class<?> border) {
		return m_borders.stream().map(Border::getClass).anyMatch(border::equals);
	}

	/**
	 * Prepares {@link FontInfo}'s for {@link Font}'s from {@link UIManager}.
	 */
	private static synchronized void prepareBorders() {
		if (m_borders == null) {
			m_borderKeys = new ArrayList<>();
			m_borders = new ArrayList<>();
			UIDefaults defaults = UIManager.getLookAndFeelDefaults();
			// prepare set of all String keys in UIManager
			Set<String> allKeys = new TreeSet<>();
			for (Iterator<?> I = defaults.keySet().iterator(); I.hasNext();) {
				Object key = I.next();
				if (key instanceof String) {
					allKeys.add((String) key);
				}
			}
			// add Border for each key
			for (String key : allKeys) {
				Border border = defaults.getBorder(key);
				if (border != null) {
					m_borderKeys.add(key);
					m_borders.add(border);
				}
			}
		}
	}
}

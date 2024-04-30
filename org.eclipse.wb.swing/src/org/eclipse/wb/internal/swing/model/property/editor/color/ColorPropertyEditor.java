/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - Color preferences integration and System color warning
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.color;

import org.eclipse.wb.core.editor.color.CustomColorPickerComposite;
import org.eclipse.wb.core.editor.constants.IColorChooserPreferenceConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages.NamedColorsComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages.WebSafeColorsComposite;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.osgi.service.prefs.Preferences;

import javax.swing.UIManager;

/**
 * Implementation of {@link PropertyEditor} for {@link Color}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ColorPropertyEditor extends PropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new ColorPropertyEditor();
	private static String preferencePrefix = "SWING";
	//Preferences for the color property editor
	static Preferences preferences =
			InstanceScope.INSTANCE.getNode(IColorChooserPreferenceConstants.PREFERENCE_NODE);

	private ColorPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final int SAMPLE_SIZE = 10;
	private static final int SAMPLE_MARGIN = 3;
	private final PropertyEditorPresentation m_presentation = new ButtonPropertyEditorPresentation() {
		@Override
		protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
			openDialog(property);
		}
	};

	@Override
	public PropertyEditorPresentation getPresentation() {
		return m_presentation;
	}

	@Override
	public void paint(Property property, Graphics graphics, int x, int y, int width, int height)
			throws Exception {
		Object value = property.getValue();
		if (value instanceof java.awt.Color) {
			// draw color sample
			{
				java.awt.Color awtColor = (java.awt.Color) value;
				Color swtColor =
						new Color(null, awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
				//
				Color oldBackground = graphics.getBackgroundColor();
				Color oldForeground = graphics.getForegroundColor();
				try {
					int width_c = SAMPLE_SIZE;
					int height_c = SAMPLE_SIZE;
					int x_c = x;
					int y_c = y + (height - height_c) / 2;
					// update rest bounds
					{
						int delta = SAMPLE_SIZE + SAMPLE_MARGIN;
						x += delta;
						width -= delta;
					}
					// fill
					{
						graphics.setBackgroundColor(swtColor);
						graphics.fillRectangle(x_c, y_c, width_c, height_c);
					}
					// draw line
					graphics.setForegroundColor(ColorConstants.gray);
					graphics.drawRectangle(x_c, y_c, width_c, height_c);
				} finally {
					graphics.setBackgroundColor(oldBackground);
					graphics.setForegroundColor(oldForeground);
					swtColor.dispose();
				}
			}
			// draw color text
			{
				String text = getText(property);
				DrawUtils.drawStringCV(graphics, text, x, y, width, height);
			}
		}
	}

	/**
	 * @return the text for current color value.
	 */
	private String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value instanceof java.awt.Color color) {
			Expression expression = ((GenericProperty) property).getExpression();
			// UIManager.getColor(key)
			{
				String colorKey = getKey_UIManager_getColor(expression);
				if (colorKey != null) {
					return colorKey;
				}
			}
			// Color.xxx or SystemColor.xxx
			if (expression instanceof QualifiedName qualifiedName) {
				String qualifier = AstNodeUtils.getFullyQualifiedName(qualifiedName.getQualifier(), false);
				String fieldName = qualifiedName.getName().getIdentifier();
				// Color.xxx
				if ("java.awt.Color".equals(qualifier)) {
					return fieldName;
				}
				// system.Color.xxx
				if ("java.awt.SystemColor".equals(qualifier)) {
					return fieldName;
				}
			}
			// use RGB
			return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		}
		// not a java.awt.Color
		return null;
	}

	/**
	 * @return the key for {@link UIManager#getColor(Object)} invocation.
	 */
	private static String getKey_UIManager_getColor(Expression expression) {
		if (expression instanceof MethodInvocation invocation) {
			if (AstNodeUtils.getMethodSignature(invocation).equals("getColor(java.lang.Object)")
					&& AstNodeUtils.getFullyQualifiedName(invocation.getExpression(), false).equals(
							"javax.swing.UIManager")
					&& invocation.arguments().get(0) instanceof StringLiteral) {
				return ((StringLiteral) invocation.arguments().get(0)).getLiteralValue();
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	private static ColorDialog m_colorDialog = new ColorDialog();

	public static void reloadColorDialog() {
		m_colorDialog = new ColorDialog();
	}

	@Override
	public boolean activate(PropertyTable propertyTable, Property property, Point location)
			throws Exception {
		// activate using keyboard
		if (location == null) {
			openDialog(property);
		}
		// don't activate
		return false;
	}

	/**
	 * Opens editing dialog.
	 */
	private void openDialog(Property property) throws Exception {
		// set initial color
		{
			Object value = property.getValue();
			if (value instanceof java.awt.Color awtColor) {
				m_colorDialog.setColorInfo(
						new ColorInfo(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
			}
		}
		// open dialog
		if (m_colorDialog.open() == Window.OK) {
			ColorInfo colorInfo = m_colorDialog.getColorInfo();
			String source = external_getSource(colorInfo);
			// set expression
			GenericProperty genericProperty = (GenericProperty) property;
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// External editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Edits given {@link ColorInfo}.
	 *
	 * @return the modified {@link ColorInfo}, or <code>null</code> if editing was canceled.
	 */
	public static ColorInfo external_editColor(ColorInfo colorInfo) {
		// set initial color
		if (colorInfo != null) {
			m_colorDialog.setColorInfo(colorInfo);
		}
		// open dialog
		if (m_colorDialog.open() == Window.OK) {
			return m_colorDialog.getColorInfo();
		}
		// cancel
		return null;
	}

	/**
	 * @return the source for given {@link ColorInfo}.
	 */
	public static String external_getSource(ColorInfo colorInfo) {
		if (colorInfo.getData() != null) {
			return (String) colorInfo.getData();
		} else {
			return "new java.awt.Color(" + colorInfo.getCommaRGB() + ")";
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ColorDialog
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class ColorDialog extends AbstractColorDialog {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ColorDialog() {
			super(DesignerPlugin.getShell());
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Pages
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void addPages(Composite parent) {
			Preferences prefs = preferences.node(IColorChooserPreferenceConstants.PREFERENCE_NODE_1);
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_pageCustomColors,
						new CustomColorPickerComposite(parent, SWT.NONE, this));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_AWT_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_pageAwtColors,
						new AwtColorsPage(parent, SWT.NONE, this));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_pageSystemColors,
						new SystemColorsPage(parent, SWT.NONE, this));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_SWING_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_pageSwingColors,
						new SwingColorsPage(parent, SWT.NONE, this));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_pageNamedColors,
						new NamedColorsComposite(parent, SWT.NONE, this));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_pageWebColors,
						new WebSafeColorsComposite(parent, SWT.NONE, this));
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// AWT Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class AwtColorsPage extends AbstractColorsGridComposite {
		public AwtColorsPage(Composite parent, int style, AbstractColorDialog colorDialog) {
			super(parent, style, colorDialog);
			{
				ColorsGridComposite colorsGrid = createColorsGroup(this, null, AwtColors.getColors_AWT());
				colorsGrid.showNames(50);
				colorsGrid.setCellHeight(25);
				colorsGrid.setColumns(2);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// System Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class SystemColorsPage extends AbstractColorsGridComposite {
		public SystemColorsPage(Composite parent, int style, AbstractColorDialog colorDialog) {
			super(parent, style, colorDialog);
			{
				ColorsGridComposite colorsGrid =
						createColorsGroup(this, null, AwtColors.getColors_System());
				colorsGrid.showNames(50);
				colorsGrid.setCellHeight(25);
				colorsGrid.setColumns(2);
				Image warningIcon = Display.getCurrent().getSystemImage(SWT.ICON_WARNING);
				CLabel label = new CLabel(this, SWT.NONE);
				label.setImage(warningIcon);
				label.setText(ModelMessages.SystemColorWarning);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Swing Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class SwingColorsPage extends AbstractColorsGridComposite {
		public SwingColorsPage(Composite parent, int style, AbstractColorDialog colorDialog) {
			super(parent, style, colorDialog);
			{
				ScrolledComposite scrolledComposite =
						new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL);
				GridDataFactory.create(scrolledComposite).grab().fill().hintV(400);
				// create grid
				ColorsGridComposite colorsGrid =
						createColorsGrid(scrolledComposite, AwtColors.getColors_Swing());
				colorsGrid.showNames(50);
				colorsGrid.setCellHeight(25);
				colorsGrid.setColumns(1);
				// configure scrolling
				scrolledComposite.setExpandHorizontal(true);
				scrolledComposite.setContent(colorsGrid);
				colorsGrid.setSize(colorsGrid.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}
	}
}

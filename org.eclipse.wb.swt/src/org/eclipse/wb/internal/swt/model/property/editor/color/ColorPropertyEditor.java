/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.color;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.color.CustomColorPickerComposite;
import org.eclipse.wb.core.editor.constants.IColorChooserPreferenceConstants;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages.NamedColorsComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages.WebSafeColorsComposite;
import org.eclipse.wb.internal.swt.model.jface.resource.ColorRegistryInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.ResourceRegistryInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.support.ColorSupport;
import org.eclipse.wb.internal.swt.support.SwtSupport;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import org.osgi.service.prefs.Preferences;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;

/**
 * Implementation of {@link PropertyEditor} for {@link org.eclipse.swt.graphics.Color}.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class ColorPropertyEditor extends PropertyEditor implements IClipboardSourceProvider {
	private static final String preferencePrefix = "SWT";
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new ColorPropertyEditor();
	static Preferences preferences = InstanceScope.INSTANCE.getNode(IColorChooserPreferenceConstants.PREFERENCE_NODE);

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
	public void paint(Property property, GC gc, int x, int y, int width, int height)
			throws Exception {
		Object value = property.getValue();
		if (value != Property.UNKNOWN_VALUE && value != null) {
			Color color = ColorSupport.getColor(value);
			// draw color sample
			{
				Color oldBackground = gc.getBackground();
				Color oldForeground = gc.getForeground();
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
						gc.setBackground(color);
						gc.fillRectangle(x_c, y_c, width_c, height_c);
					}
					// draw line
					gc.setForeground(IColorConstants.gray);
					gc.drawRectangle(x_c, y_c, width_c, height_c);
				} finally {
					gc.setBackground(oldBackground);
					gc.setForeground(oldForeground);
					color.dispose();
				}
			}
			// draw color text
			{
				String text = getText(property);
				DrawUtils.drawStringCV(gc, text, x, y, width, height);
			}
		}
	}

	/**
	 * @return the text for current color value.
	 */
	private String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value != Property.UNKNOWN_VALUE) {
			Expression expression = ((GenericProperty) property).getExpression();
			// Display.getSystemColor(int key)
			if (AstNodeUtils.isMethodInvocation(
					expression,
					"org.eclipse.swt.widgets.Display",
					"getSystemColor(int)")) {
				MethodInvocation invocation = (MethodInvocation) expression;
				Expression idExpression = DomGenerics.arguments(invocation).get(0);
				String fieldName = getColorFieldName(idExpression);
				if (fieldName != null) {
					return fieldName;
				}
			}
			// SWTResourceManager.getColor(String key)
			if (AstNodeUtils.isMethodInvocation(
					expression,
					"org.eclipse.wb.swt.SWTResourceManager",
					"getColor(int)")) {
				MethodInvocation invocation = (MethodInvocation) expression;
				Expression idExpression = DomGenerics.arguments(invocation).get(0);
				return getColorFieldName(idExpression);
			}
			// ColorRegistry.get(String key)
			if (expression instanceof MethodInvocation invocation) {
				if (AstNodeUtils.getMethodSignature(invocation).equals("get(java.lang.String)")
						&& AstNodeUtils.isSuccessorOf(
								AstNodeUtils.getTypeBinding(invocation.getExpression()),
								"org.eclipse.jface.resource.ColorRegistry")) {
					Object keyArgument = invocation.arguments().get(0);
					if (keyArgument instanceof QualifiedName) {
						GenericProperty genericProperty = (GenericProperty) property;
						ResourceRegistryInfo registry = RegistryContainerInfo.getRegistry(
								genericProperty.getJavaInfo().getRootJava(),
								invocation.getExpression());
						//
						QualifiedName keyQualifiedName = (QualifiedName) keyArgument;
						//
						return registry.getVariableSupport().getTitle()
								+ " - "
								+ keyQualifiedName.getName().getIdentifier();
					}
					return null;
				}
			}
			// use RGB
			return ColorSupport.toString(value);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// System colors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @param idExpression
	 *          the {@link Expression} with "id" of system color.
	 *
	 * @return the name of "COLOR_XXX" field from <code>SWT</code>.
	 */
	private static String getColorFieldName(Expression idExpression) throws Exception {
		int id = (Integer) JavaInfoEvaluationHelper.getValue(idExpression);
		// find SWT.COLOR_XXX field
		for (Field field : SwtSupport.getSwtClass().getFields()) {
			String fieldName = field.getName();
			if (fieldName.startsWith("COLOR_") && field.getInt(null) == id) {
				return fieldName;
			}
		}
		// no field, unexpected
		throw new IllegalArgumentException(
				MessageFormat.format(org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_wrongSwtColor,
						idExpression, id));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		// prepare RGB based ColorInfo
		ColorInfo colorInfo;
		{
			Object value = property.getValue();
			if (value == Property.UNKNOWN_VALUE) {
				return null;
			}
			colorInfo = ColorSupport.createInfo(value);
		}
		// special cases
		{
			Expression expression = property.getExpression();
			// Display.getSystemColor(int key)
			if (AstNodeUtils.isMethodInvocation(
					expression,
					"org.eclipse.swt.widgets.Display",
					"getSystemColor(int)")) {
				MethodInvocation invocation = (MethodInvocation) expression;
				Expression idExpression = DomGenerics.arguments(invocation).get(0);
				colorInfo.setData("org.eclipse.swt.SWT." + getColorFieldName(idExpression));
			}
			// SWTResourceManager.getColor(String key)
			if (AstNodeUtils.isMethodInvocation(
					expression,
					"org.eclipse.wb.swt.SWTResourceManager",
					"getColor(int)")) {
				MethodInvocation invocation = (MethodInvocation) expression;
				Expression idExpression = DomGenerics.arguments(invocation).get(0);
				colorInfo.setData("org.eclipse.swt.SWT." + getColorFieldName(idExpression));
			}
		}
		// convert ColorInfo into source
		return getSource(property, colorInfo);
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
		GenericProperty genericProperty = (GenericProperty) property;
		ColorDialog colorDialog = new ColorDialog(genericProperty.getJavaInfo());
		// set initial color
		{
			Object value = property.getValue();
			if (value != Property.UNKNOWN_VALUE && value != null) {
				colorDialog.setColorInfo(ColorSupport.createInfo(value));
			}
		}
		// open dialog
		if (colorDialog.open() == Window.OK) {
			ColorInfo colorInfo = colorDialog.getColorInfo();
			// prepare source
			String source = getSource(genericProperty, colorInfo);
			// set expression
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
	}

	/**
	 * @return the Java source for given {@link ColorInfo}.
	 */
	private static String getSource(GenericProperty property, ColorInfo colorInfo) throws Exception {
		if (colorInfo.getData() instanceof Object[]) {
			Object[] data = (Object[]) colorInfo.getData();
			ColorRegistryInfo registryInfo = (ColorRegistryInfo) data[0];
			KeyFieldInfo keyInfo = (KeyFieldInfo) data[1];
			return TemplateUtils.format("{0}.get({1})", registryInfo, keyInfo.keySource);
		}
		IPreferenceStore preferences =
				property.getJavaInfo().getDescription().getToolkit().getPreferences();

		if (preferences.getBoolean(IPreferenceConstants.P_USE_RESOURCE_MANAGER)) {
			ManagerUtils.ensure_SWTResourceManager(property.getJavaInfo());
			if (colorInfo.getData() != null) {
				return "org.eclipse.wb.swt.SWTResourceManager.getColor(" + colorInfo.getData() + ")";
			}
			return "org.eclipse.wb.swt.SWTResourceManager.getColor(" + colorInfo.getCommaRGB() + ")";
		} else {
			if (colorInfo.getData() != null) {
				return "org.eclipse.swt.widgets.Display.getCurrent().getSystemColor("
						+ colorInfo.getData()
						+ ")";
			} else {
				return "new org.eclipse.swt.graphics.Color(null, " + colorInfo.getCommaRGB() + ")";
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ColorDialog
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class ColorDialog extends AbstractColorDialog {
		private static String m_lastPageTitle;
		private final JavaInfo m_javaInfo;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ColorDialog() {
			super(DesignerPlugin.getShell());
			m_javaInfo = null;

		}

		public ColorDialog(JavaInfo javaInfo) {
			super(DesignerPlugin.getShell());
			m_javaInfo = javaInfo;
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
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_systemColorsPage,
						new SystemColorsPage(parent, SWT.NONE, this, m_javaInfo));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_namedColorsPage,
						new NamedColorsComposite(parent, SWT.NONE, this));
			}
			if (prefs.getBoolean(preferencePrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS, true)) {
				addPage(
						org.eclipse.wb.internal.core.model.ModelMessages.ColorPropertyEditor_webSafePage,
						new WebSafeColorsComposite(parent, SWT.NONE, this));
			}
			try {
				List<ColorRegistryInfo> registries =
						RegistryContainerInfo.getRegistries(m_javaInfo.getRootJava(), ColorRegistryInfo.class);
				if (!registries.isEmpty()) {
					addPage("ColorRegistry", new RegistryColorsPage(parent, SWT.NONE, this, registries));
				}
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
			selectPageByTitle(m_lastPageTitle);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Dialog
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void buttonPressed(int buttonId) {
			// save page selection
			if (buttonId == IDialogConstants.OK_ID) {
				m_lastPageTitle = getSelectedPageTitle();
			}
			// super handle
			super.buttonPressed(buttonId);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// System Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class SystemColorsPage extends AbstractColorsGridComposite {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public SystemColorsPage(Composite parent,
				int style,
				AbstractColorDialog colorDialog,
				JavaInfo javaInfo) {
			super(parent, style, colorDialog);
			{
				ColorsGridComposite colorsGrid =
						createColorsGroup(this, null, SwtColors.getSystemColors(javaInfo));
				colorsGrid.showNames(50);
				colorsGrid.setCellHeight(25);
				colorsGrid.setColumns(2);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// JFace Registry Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final class RegistryColorsPage extends AbstractColorsGridComposite {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public RegistryColorsPage(Composite parent,
				int style,
				AbstractColorDialog colorDialog,
				List<ColorRegistryInfo> registries) throws Exception {
			super(parent, style, colorDialog);
			//
			for (ColorRegistryInfo registryInfo : registries) {
				// prepare color info's
				Object registry = registryInfo.getObject();
				List<ColorInfo> infos = Lists.newArrayList();
				for (KeyFieldInfo keyInfo : registryInfo.getKeyFields()) {
					if (keyInfo.value == null) {
						keyInfo.value =
								ReflectionUtils.invokeMethod(registry, "get(java.lang.String)", keyInfo.keyValue);
					}
					if (keyInfo.value != null) {
						ColorInfo colorInfo = ColorSupport.createInfo(keyInfo.keyName, keyInfo.value);
						colorInfo.setData(new Object[]{registryInfo, keyInfo});
						infos.add(colorInfo);
					}
				}
				// create grid composite
				ColorsGridComposite colorsGrid = createColorsGroup(
						this,
						registryInfo.getVariableSupport().getTitle(),
						infos.toArray(new ColorInfo[infos.size()]));
				colorsGrid.showNames(50);
				colorsGrid.setCellHeight(25);
				colorsGrid.setColumns(2);
			}
		}
	}
}
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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.ManagerContainerInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.ResourceRegistryInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.support.FontSupport;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.FontData;

import java.util.List;

/**
 * {@link PropertyEditor} for {@link org.eclipse.swt.graphic.Font}.
 *
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class FontPropertyEditor extends TextDialogPropertyEditor
implements
IClipboardSourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new FontPropertyEditor();

	private FontPropertyEditor() {
	}

	/**
	 * Returns the Java code required for creating a new {@link Font} using a
	 * {@link ResourceManager}.
	 *
	 * @param javaInfo Java info the resource manager belongs to.
	 * @param name     os-specific font name
	 * @param height   height (pixels)
	 * @param style    a bitwise combination of NORMAL, BOLD, ITALIC
	 * @return {@code LocalResourceManager.create(FontDescriptor.createFrom(<clazz>, <path>))}
	 * @see FontDescriptor#createFromFile(Class, String)
	 */
	public static String getInvocationSource(JavaInfo javaInfo, String name, int height, String style) throws Exception {
		String resourceManager = ManagerContainerInfo //
				.getResourceManagerInfo(javaInfo.getRootJava()) //
				.getVariableSupport() //
				.getName();
		return String.format(
				"%s.create(org.eclipse.jface.resource.FontDescriptor.createFrom(\"%s\", %d, %s))", //
				resourceManager, name, height, style);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		Object value = property.getValue();
		if (value != Property.UNKNOWN_VALUE) {
			Expression expression = ((GenericProperty) property).getExpression();
			if (expression instanceof MethodInvocation invocation) {
				// JFaceResource.getXXXFont()
				if (isJFaceValue(invocation)) {
					return invocation.getName().getIdentifier() + "()";
				}
				// FontRegistry.get(String key)
				if (isFontRegistryInvocation(invocation)) {
					return getTextForRegistry(property, invocation);
				}
				// ResourceManager.createFont()
				if (isResourceManagerInvocation(invocation)) {
					return getTextForResourceManager(invocation);
				}
			}
			// default font
			if (value == null) {
				value = DesignerPlugin.getStandardDisplay().getSystemFont();
			}
			// use font.toString()
			return getText(value);
		}
		return null;
	}

	private static String getText(Object font) throws Exception {
		return getText((FontData) FontSupport.getFontData(font));
	}

	private static String getText(FontData fontData) throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append(FontSupport.getFontName(fontData));
		buffer.append(" ");
		buffer.append(FontSupport.getFontSize(fontData));
		{
			String styleText = FontSupport.getFontStyleText(fontData);
			if (styleText.length() != 0) {
				buffer.append(" ");
				buffer.append(styleText);
			}
		}
		return buffer.toString();
	}

	/**
	 * @return <code>true</code> if given {@link MethodInvocation} represented method
	 *         <code>JFaceResources.getXXXFont()</code>.
	 */
	private static boolean isJFaceValue(MethodInvocation invocation) {
		String identifier = invocation.getName().getIdentifier();
		return identifier.startsWith("get")
				&& identifier.endsWith("Font")
				&& AstNodeUtils.isSuccessorOf(
						invocation.getExpression(),
						"org.eclipse.jface.resource.JFaceResources") && invocation.arguments().isEmpty();
	}

	/**
	 * @return <code>true</code> if given {@link Expression} represented method
	 *         <code>JFaceResources.getXXXFont()</code>.
	 */
	private static boolean isJFaceValue(Expression expression) {
		if (expression instanceof MethodInvocation) {
			return isJFaceValue((MethodInvocation) expression);
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IClipboardSourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getClipboardSource(GenericProperty property) throws Exception {
		// FondData based FontInfo
		FontInfo fontInfo;
		{
			Object value = property.getValue();
			if (value == Property.UNKNOWN_VALUE) {
				return null;
			}
			fontInfo = new FontInfo(null, value, null, false);
		}
		// JFaceResource.getXXXFont()
		{
			Expression expression = property.getExpression();
			// JFaceResource.getXXXFont()
			if (expression instanceof MethodInvocation invocation) {
				String identifier = invocation.getName().getIdentifier();
				if (identifier.startsWith("get")
						&& identifier.endsWith("Font")
						&& AstNodeUtils.getFullyQualifiedName(invocation.getExpression(), false).equals(
								"org.eclipse.jface.resource.JFaceResources")
						&& invocation.arguments().isEmpty()) {
					fontInfo =
							new FontInfo(null, null, "org.eclipse.jface.resource.JFaceResources."
									+ identifier
									+ "()", false);
				}
			}
		}
		// convert FontInfo into source
		return getSource(property, fontInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		FontDialog fontDialog =
				new FontDialog(DesignerPlugin.getShell(), genericProperty.getJavaInfo());
		// set initial value
		{
			Object value = property.getValue();
			if (value == null || value == Property.UNKNOWN_VALUE) {
				value = DesignerPlugin.getStandardDisplay().getSystemFont();
			}
			// prepare font value
			Object[] registryValue = getRegistryValue(property);
			FontInfo fontInfo;
			if (registryValue == null) {
				fontInfo = new FontInfo(null, value, null, false);
				if (isJFaceValue(genericProperty.getExpression())) {
					fontInfo.setPageId(JFaceFontPage.NAME);
				}
			} else {
				fontInfo = new FontInfo(null, value, (String) registryValue[3], false);
				fontInfo.setData(registryValue);
				fontInfo.setPageId(RegistryFontPage.NAME);
			}
			// set value
			fontDialog.setFontInfo(fontInfo);
		}
		// open dialog
		if (fontDialog.open() == Window.OK) {
			FontInfo fontInfo = fontDialog.getFontInfo();
			// prepare source
			String source = getSource(genericProperty, fontInfo);
			// set expression
			genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
		}
		fontDialog.disposeFont();
	}

	/**
	 * @return the source for given {@link FontInfo}.
	 */
	private static String getSource(GenericProperty property, FontInfo fontInfo) throws Exception {
		String source;
		if (fontInfo.getSourceCode() != null) {
			source = fontInfo.getSourceCode();
		} else {
			Object fontData = FontSupport.getFontData(fontInfo.getFont());
			// prepare prefix
			String prefix;
			String suffix = "";
			{
				IPreferenceStore preferences =
						property.getJavaInfo().getDescription().getToolkit().getPreferences();
				if (preferences.getBoolean(IPreferenceConstants.P_USE_RESOURCE_MANAGER)) {
					String resourceManager = ManagerContainerInfo.getResourceManagerInfo(property.getJavaInfo().getRootJava()) //
							.getVariableSupport() //
							.getName();
					prefix = resourceManager + ".create(org.eclipse.jface.resource.FontDescriptor.createFrom(";
					suffix = ")";
				} else {
					prefix = "new org.eclipse.swt.graphics.Font(null, ";
				}
			}
			// prepare source
			source =
					prefix
					+ "\""
					+ FontSupport.getFontName(fontData)
					+ "\", "
					+ FontSupport.getFontSize(fontData)
					+ ", "
					+ FontSupport.getFontStyleSource(fontData)
					+ ")"
					+ suffix;
		}
		return source;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FontRegistry
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean isFontRegistryInvocation(MethodInvocation invocation) {
		return AstNodeUtils.isMethodInvocation(
				invocation,
				"org.eclipse.jface.resource.FontRegistry",
				new String[]{
						"get(java.lang.String)",
						"getBold(java.lang.String)",
				"getItalic(java.lang.String)"});
	}

	private static String getTextForRegistry(Property property, MethodInvocation invocation)
			throws Exception {
		Object keyArgument = invocation.arguments().get(0);
		if (keyArgument instanceof QualifiedName) {
			// prepare title for registry
			String registryTitle;
			{
				GenericProperty genericProperty = (GenericProperty) property;
				ResourceRegistryInfo registry =
						RegistryContainerInfo.getRegistry(
								genericProperty.getJavaInfo().getRootJava(),
								invocation.getExpression());
				registryTitle = registry.getVariableSupport().getTitle();
			}
			// prepare key
			String key;
			{
				QualifiedName keyQualifiedName = (QualifiedName) keyArgument;
				key = keyQualifiedName.getName().getIdentifier();
			}
			// prepare style prefix
			String prefix = "";
			{
				String signature = AstNodeUtils.getMethodSignature(invocation);
				if (signature.startsWith("getBold")) {
					prefix = "(b)";
				} else if (signature.startsWith("getItalic")) {
					prefix = "(i)";
				}
			}
			//
			return registryTitle + " - " + key + prefix;
		}
		return null;
	}

	/**
	 * @return <code>[{@link ResourceRegistryInfo}, {@link KeyFieldInfo}, {none, bold, italic}, source]</code>
	 *         if property value sets as "JFace Font Registry" otherwise <code>null</code>.
	 */
	private static Object[] getRegistryValue(Property property) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		Expression expression = genericProperty.getExpression();
		//
		if (expression instanceof MethodInvocation invocation) {
			String signature = AstNodeUtils.getMethodSignature(invocation);
			if (isFontRegistryInvocation(invocation)) {
				Object keyArgument = invocation.arguments().get(0);
				if (keyArgument instanceof QualifiedName) {
					ResourceRegistryInfo registryInfo =
							RegistryContainerInfo.getRegistry(
									genericProperty.getJavaInfo().getRootJava(),
									invocation.getExpression());
					// prepare key
					QualifiedName keyQualifiedName = (QualifiedName) keyArgument;
					String keyName = keyQualifiedName.getName().getIdentifier();
					KeyFieldInfo keyFieldInfo = null;
					List<KeyFieldInfo> keyFields = registryInfo.getKeyFields();
					for (KeyFieldInfo info : keyFields) {
						if (keyName.equals(info.keyName)) {
							keyFieldInfo = info;
							break;
						}
					}
					if (keyFieldInfo == null) {
						return null;
					}
					// prepare method type
					int selectionIndex = RegistryFontPage.FONT_STYLE_NONE;
					String methodName = "get";
					if (signature.startsWith("getBold")) {
						selectionIndex = RegistryFontPage.FONT_STYLE_BOLD;
						methodName += "Bold";
					} else if (signature.startsWith("getItalic")) {
						selectionIndex = RegistryFontPage.FONT_STYLE_ITALIC;
						methodName += "Italic";
					}
					// prepare source
					String source =
							TemplateUtils.format(
									"{0}.{1}({2}.{3})",
									registryInfo,
									methodName,
									registryInfo.getDescription().getComponentClass().getName(),
									keyQualifiedName.getName().getIdentifier());
					//
					return new Object[]{registryInfo, keyFieldInfo, selectionIndex, source};
				}
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ResourceManager
	//
	////////////////////////////////////////////////////////////////////////////

	private static boolean isResourceManagerInvocation(Expression expression) {
		return AstNodeUtils.isMethodInvocation(expression, //
				"org.eclipse.jface.resource.ResourceManager", //
				"create(org.eclipse.jface.resource.DeviceResourceDescriptor)");
	}

	private static String getTextForResourceManager(MethodInvocation methodInvocation) throws Exception {
		Expression managerExpression = DomGenerics.arguments(methodInvocation).get(0);
		if (AstNodeUtils.isMethodInvocation(managerExpression, //
				"org.eclipse.jface.resource.FontDescriptor", //
				"createFrom(java.lang.String,int,int)")) {
			MethodInvocation invocation = (MethodInvocation) managerExpression;
			String name = (String) JavaInfoEvaluationHelper.getValue(DomGenerics.arguments(invocation).get(0));
			int height = (int) JavaInfoEvaluationHelper.getValue(DomGenerics.arguments(invocation).get(1));
			int style = (int) JavaInfoEvaluationHelper.getValue(DomGenerics.arguments(invocation).get(2));
			return getText(new FontData(name, height, style));
		}
		return null;
	}
}
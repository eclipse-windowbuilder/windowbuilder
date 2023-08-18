/*******************************************************************************
 * Copyright (c) 2019-2020
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    bergert - modified to implement Activator.getPluginBundle().getString()
 *******************************************************************************/
package org.eclipse.wb.internal.core.nls.bundle.pure.activator;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.nls.bundle.pure.AbstractPureBundleSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Source for direct {@link ResourceBundle} usage, for example:
 *
 * Activator.getPluginBundle().getString("key.for.property")
 *
 * @author bergert
 * @coverage core.nls
 */
public final class ActivatorSource extends AbstractPureBundleSource {
	////////////////////////////////////////////////////////////////////////////
	//
	// Possible sources
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String BUNDLE_COMMENT = "Activator plugin ResourceBundle";

	@Override
	protected String getBundleComment() {
		return BUNDLE_COMMENT;
	}

	/**
	 * Return "possible" sources that exist in given package.
	 *
	 * "Possible" source is source that exists in current package, but is not used in current unit. We
	 * show "possible" sources only if there are no "real" sources.
	 */
	public static List<AbstractSource> getPossibleSources(JavaInfo root, IPackageFragment pkg)
			throws Exception {
		List<AbstractSource> sources = Lists.newArrayList();
		IJavaElement[] packageElements = pkg.getChildren();
		for (int i = 0; i < packageElements.length; i++) {
			ICompilationUnit unit = (ICompilationUnit) packageElements[i];
			if (unit.getElementName().endsWith("Activator.java")) {
				IType type = unit.findPrimaryType();
				if (type != null) {
					// check for field pluginBundle
					{
						IField field_bundleName = type.getField("pluginBundle");
						if (!field_bundleName.exists()) {
							continue;
						}
					}
					// check for method getPluginBundle()
					{
						IMethod method_getString_0 = CodeUtils.findMethodSingleType(type, "getPluginBundle()");
						if (method_getString_0 == null) {
							continue;
						}
						if (!method_getString_0.getReturnType().equals("QResourceBundle;")) {
							continue;
						}
					}
					// OK, this is probably correct source
					try {
						//AbstractSource source = new DirectSource(root, pkg.getElementName()+".plugin");
						AbstractSource source = new ActivatorSource(root, "plugin");
						sources.add(source);
					} catch (Exception e) {
						DesignerPlugin.log(e);
					}
				}
			}
		}
		return sources;
	}

	@Override
	public void attachPossible() throws Exception {
		addField(m_root, m_bundleName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse given expression and return NLSSource for it (new or existing from list).
	 */
	public static AbstractSource get(JavaInfo component,
			GenericProperty property,
			Expression expression,
			List<AbstractSource> sources) throws Exception {
		ExpressionInfo expressionInfo = getExpressionInfo(expression);
		if (expressionInfo != null) {
			String bundleName = expressionInfo.m_bundleName;
			ActivatorSource source = getNewOrExistingSource(component, bundleName, sources);
			if (source == null) {
				return null;
			}
			source.onKeyAdd(component, expressionInfo.m_key);
			return source;
		}
		return null;
	}

	/**
	 * Find existing source with same bundle name or create new one.
	 */
	private static ActivatorSource getNewOrExistingSource(JavaInfo component,
			String bundleName,
			List<AbstractSource> sources) throws Exception {
		for (AbstractSource abstractSource : sources) {
			if (abstractSource instanceof ActivatorSource source) {
				if (source.m_bundleName.equals(bundleName)) {
					return source;
				}
			}
		}
		ActivatorSource result = null;
		try {
			result = new ActivatorSource(component.getRootJava(), bundleName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Parse given expression and if it is valid Eclipse style messages class access, extract resource
	 * bundle name, key and optional default value.
	 *
	 * @param abstractSource
	 */
	private static ExpressionInfo getExpressionInfo(Expression expression) {
		if (expression instanceof MethodInvocation) {
			// the invocation is Activator.getPluginBundle().getString("a.b.c")
			// the default plugin name is 'plugin'
			String pluginName = "plugin";
			MethodInvocation getLocalString_invocation = (MethodInvocation) expression;
			MethodInvocation possible_Activator = null;
			String activator_bundleName = null;
			// check for Activator
			{
				possible_Activator = (MethodInvocation) getLocalString_invocation.getExpression();
				Expression possible_Activator_invokation = possible_Activator.getExpression();
				if (!(possible_Activator_invokation instanceof Expression)) {
					return null;
				}
				String activator_Name =
						((SimpleName) possible_Activator_invokation).getFullyQualifiedName();
				boolean is_Activator = activator_Name.endsWith("Activator");
				if (!is_Activator) {
					return null;
				}
				if (activator_Name.length() > "Activator".length()) {
					int pos = activator_Name.length() - "Activator".length();
					pluginName = activator_Name.substring(0, pos).toLowerCase();
				}
				ITypeBinding type = possible_Activator_invokation.resolveTypeBinding();
				IPackageBinding pkg = type.getPackage();
				activator_bundleName = pkg.getName();
			}
			// check for method getPluginBundle
			{
				boolean is_getPluginBundle =
						possible_Activator.getName().getIdentifier().equals("getPluginBundle");
				if (!is_getPluginBundle) {
					return null;
				}
			}
			// check for getString(key)
			{
				boolean is_getString =
						getLocalString_invocation.getName().getIdentifier().equals("getString")
						&& getLocalString_invocation.arguments().size() == 1
						&& getLocalString_invocation.arguments().get(0) instanceof StringLiteral;
				if (!is_getString) {
					return null;
				}
			}
			// check that getPluginBundle() returns a ResourceBundle
			{
				String varexp = AstNodeUtils.getFullyQualifiedName(possible_Activator, false);
				if (varexp == null || !varexp.equals("java.util.ResourceBundle")) {
					return null;
				}
			}
			// all was checked, we can create expression information
			try {
				// prepare key
				StringLiteral nlsStringLiteral =
						(StringLiteral) getLocalString_invocation.arguments().get(0);
				String nlsString = nlsStringLiteral.getLiteralValue();
				// return information about expression
				ExpressionInfo expressionInfo = new ExpressionInfo(expression,
						activator_bundleName,
						pluginName,
						nlsStringLiteral,
						nlsString);
				expression.setProperty(NLS_EXPRESSION_INFO, expressionInfo);
				return expressionInfo;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expression information
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Information about expression. We store it in expression property to avoid parsing every time.
	 */
	protected static class ExpressionInfo extends BasicExpressionInfo {
		private final String m_bundleName;

		public ExpressionInfo(Expression expression,
				String packageName,
				String pluginName,
				StringLiteral keyExpression,
				String key) {
			super(expression, keyExpression, key);
			m_bundleName = pluginName;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActivatorSource(JavaInfo root, String bundleName) throws Exception {
		super(root, bundleName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTypeTitle() throws Exception {
		return "Activator Plugin usage";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Edit support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IKeyGeneratorStrategy getKeyGeneratorStrategy() {
		return KEY_GENERATOR;
	}

	@Override
	protected Expression apply_renameKey_replaceKeyExpression(AstEditor editor,
			Expression keyExpression,
			String newKey) throws Exception {
		String newSource = StringConverter.INSTANCE.toJavaSource(m_root, newKey);
		return editor.replaceExpression(keyExpression, newSource);
	}

	@Override
	protected BasicExpressionInfo apply_externalize_replaceExpression(GenericProperty property,
			String key) throws Exception {
		// prepare code
		String code = "Activator.getPluginBundle().getString("
				+ StringConverter.INSTANCE.toJavaSource(m_root, key)
				+ ")";
		// replace expression
		Expression expression = property.getExpression();
		ExpressionInfo expressionInfo = (ExpressionInfo) replaceExpression_getInfo(expression, code);
		// add //$NON-NLS-xxx$ comments
		{
			addNonNLSComment(expressionInfo.m_keyExpression);
		}
		// return expression information
		return expressionInfo;
	}

	@Override
	protected void apply_removeNonNLSComments(BasicExpressionInfo basicExpressionInfo)
			throws Exception {
		ExpressionInfo expressionInfo = (ExpressionInfo) basicExpressionInfo;
		removeNonNLSComment((StringLiteral) expressionInfo.m_keyExpression);
	}

	public static ActivatorSource apply_create(IEditableSource editable, JavaInfo root, Object o)
			throws Exception {
		// prepare parameters
		SourceParameters parameters = (SourceParameters) o;
		// create property bundle
		createPropertyBundleFile(parameters.m_propertyPackage, parameters.m_propertyFileName, null);
		// create source
		return new ActivatorSource(root, parameters.m_propertyBundleName);
	}

	/**
	 * Add field with ResourceBundle definition.
	 */
	private static void addField(JavaInfo root, String bundleName) throws Exception {
		// prepare code
		String code = "import " + StringConverter.INSTANCE.toJavaSource(root, bundleName);
		// add field
		TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(root);
		root.getEditor().addInterfaceMethodDeclaration(
				code,
				new BodyDeclarationTarget(typeDeclaration, true));
	}
}

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
package org.eclipse.wb.internal.xwt.parser;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.xml.model.description.IDescriptionRulesProvider;
import org.eclipse.wb.internal.core.xml.model.description.internal.PropertyEditorDescription;
import org.eclipse.wb.internal.xwt.model.property.editor.style.StylePropertyEditor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Widget;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.xml.sax.Attributes;

/**
 * {@link IDescriptionRulesProvider} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public class XwtDescriptionRulesProvider implements IDescriptionRulesProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IDescriptionRulesProvider INSTANCE = new XwtDescriptionRulesProvider();

	private XwtDescriptionRulesProvider() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionRulesProvider
	//
	////////////////////////////////////////////////////////////////////////////
	public void addRules(Digester digester, EditorContext context, Class<?> componentClass) {
		if (Widget.class.isAssignableFrom(componentClass)
				|| Viewer.class.isAssignableFrom(componentClass)) {
			addRule_forStylePropertyEditor(digester, context);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parses {@link StylePropertyEditor} specification for constructor parameter and remember it as
	 * arbitrary value in {@link ComponentDescription}.
	 */
	private void addRule_forStylePropertyEditor(final Digester digester, final EditorContext context) {
		String pattern = "component/constructors/constructor/parameter/editor";
		digester.addRule(pattern, new Rule() {
			@Override
			public void begin(String namespace, String name, Attributes attributes) throws Exception {
				String editorId = attributes.getValue("id");
				PropertyEditor editor = DescriptionPropertiesHelper.getConfigurableEditor(editorId);
				digester.push(new PropertyEditorDescription(context, editor));
			}

			@Override
			public void end(String namespace, String name) throws Exception {
				// prepare editor
				PropertyEditor configuredEditor;
				{
					PropertyEditorDescription description = (PropertyEditorDescription) digester.pop();
					configuredEditor = description.getConfiguredEditor();
				}
				// remember editor
				if (configuredEditor instanceof StylePropertyEditor) {
					ComponentDescription componentDescription = (ComponentDescription) digester.peek();
					Class<StylePropertyEditor> key = StylePropertyEditor.class;
					// XXX
					/*if (componentDescription.getArbitraryValue(key) == null)*/{
						componentDescription.putArbitraryValue(key, configuredEditor);
					}
				}
			}
		});
		ComponentDescriptionHelper.addConfigurableObjectParametersRules(digester, pattern);
	}
}

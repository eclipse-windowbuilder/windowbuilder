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
package org.eclipse.wb.internal.swing.databinding.model.bindings;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.components.ComponentsObserveTypeContainer;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ObjectPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;

import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import org.apache.commons.lang.math.NumberUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author lobas_av
 * @coverage bindings.swing.model.bindings
 */
public final class VirtualParser {
	public static Object[] getBindings(AstEditor editor,
			DatabindingsProvider provider,
			MethodDeclaration initDataBindings) throws Exception {
		// prepare virtual observes
		ObserveInfo virtualObserve =
				(ObserveInfo) provider.getContainer(ObserveType.BEANS).getObservables().get(0);
		ObserveInfo virtualProperty =
				(ObserveInfo) virtualObserve.getChildren(ChildrenContext.ChildrenForPropertiesTable).get(0);
		PropertyInfo virtualAstProperty = virtualProperty.createProperty(virtualObserve);
		// prepare components container
		ComponentsObserveTypeContainer componentsContainer =
				(ComponentsObserveTypeContainer) provider.getContainer(ObserveType.WIDGETS);
		// prepare class loader
		ClassLoader classLoader = EditorState.get(editor).getEditorLoader();
		//
		List<Integer> indexes = new ArrayList<>();
		List<BindingInfo> virtualBindings = new ArrayList<>();
		// scan comments
		for (Comment comment : editor.getCommentList()) {
			if (comment instanceof LineComment) {
				if (initDataBindings == editor.getEnclosingMethod(comment.getStartPosition())) {
					// filter virtual comment line
					String line = editor.getSource(comment);
					if (line.startsWith("// [Virtual]")) {
						StringTokenizer tokenizer = new StringTokenizer(line);
						tokenizer.nextToken(); // skip '//'
						tokenizer.nextToken(); // skip '[Virtual]'
						// prepare binding location
						String indexValue = tokenizer.nextToken();
						int index = NumberUtils.isNumber(indexValue) ? Integer.parseInt(indexValue) : 0;
						// prepare target/model orientation
						boolean isSwingTarget = "target".equals(tokenizer.nextToken());
						// prepare reference to swing component
						String reference = tokenizer.nextToken();
						ObserveInfo swingObserve = componentsContainer.resolve(reference);
						if (swingObserve == null) {
							AbstractParser.addError(
									editor,
									MessageFormat.format(Messages.VirtualParser_errUndefinedSwingObject, reference),
									new Throwable());
							continue;
						}
						// prepare component observes
						PropertyInfo swingAstProperty = new ObjectPropertyInfo(swingObserve.getObjectType());
						ObserveInfo swingProperty = swingAstProperty.getObserveProperty(swingObserve);
						// prepare element type
						String elementClassName = tokenizer.nextToken();
						IGenericType elementType = null;
						try {
							elementType =
									new ClassGenericType(CoreUtils.load(classLoader, elementClassName), null, null);
						} catch (ClassNotFoundException e) {
							AbstractParser.addError(editor, MessageFormat.format(
									Messages.VirtualParser_errUnknownElementClass,
									elementClassName), new Throwable());
							continue;
						}
						// create binding
						VirtualBindingInfo binding = null;
						//
						if (isSwingTarget) {
							binding =
									new VirtualBindingInfo(swingObserve,
											swingProperty,
											swingAstProperty,
											virtualObserve,
											virtualProperty,
											virtualAstProperty);
						} else {
							binding =
									new VirtualBindingInfo(virtualObserve,
											virtualProperty,
											virtualAstProperty,
											swingObserve,
											swingProperty,
											swingAstProperty);
						}
						// configure
						binding.setElementType(elementType);
						// add to result
						indexes.add(index);
						virtualBindings.add(binding);
						// complete invoke
						binding.create(provider.getRootInfo().getBindings());
					}
				}
			}
		}
		// result
		return new Object[]{indexes, virtualBindings};
	}
}
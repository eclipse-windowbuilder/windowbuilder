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
package org.eclipse.wb.internal.swt.parser;

import org.eclipse.wb.internal.core.parser.AbstractParseFactory;
import org.eclipse.wb.internal.core.parser.IParseFactory;

/**
 * {@link IParseFactory} for SWT.
 *
 * @author sablin_aa
 * @coverage swt.parser
 */
public abstract class ParseFactory extends AbstractParseFactory {
  /*@Override
  public JavaInfo create(ASTEditor editor,
  		ClassInstanceCreation creation,
  		IMethodBinding methodBinding,
  		ITypeBinding typeBinding,
  		Expression[] arguments,
  		JavaInfo[] argumentInfos) throws Exception {
  	// create JavaInfo
  	if (isToolkitObject(editor, typeBinding)) {
  		// prepare class of widget
  		Class<?> componentClass = getClass(editor, typeBinding);
  		if (componentClass != null) {
  			return JavaInfoUtils.createJavaInfo(
  				editor,
  				componentClass,
  				new ConstructorCreationSupport(creation));
  		}
  		// return created JavaInfo
  	}
  	// unknown class
  	return super.create(editor, creation, methodBinding, typeBinding, arguments, argumentInfos);
  }*/
  /*@Override
  protected void createStaticFactory_Association(JavaInfo javaInfo,
  		MethodInvocation invocation,
  		JavaInfo[] argumentInfos,
  		FactoryMethodDescription description) throws Exception {
  	if (javaInfo instanceof ViewerInfo) {
  		((ViewerInfo) javaInfo).configureWrapper(description, argumentInfos);
  		javaInfo.setAssociation(new FactoryParentAssociation(invocation));
  	} else {
  		super.createStaticFactory_Association(javaInfo, invocation, argumentInfos, description);
  	}
  }*/
}
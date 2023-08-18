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
package org.eclipse.wb.internal.xwt.editor;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.xml.AbstractDocumentHandler;
import org.eclipse.wb.internal.core.utils.xml.DocumentEditContext;
import org.eclipse.wb.internal.core.xml.editor.actions.IPairResourceProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;

/**
 * {@link IPairResourceProvider} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.editor
 */
public final class XwtPairResourceProvider implements IPairResourceProvider {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IPairResourceProvider INSTANCE = new XwtPairResourceProvider();

	private XwtPairResourceProvider() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPairResourceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IFile getPair(final IFile file) {
		return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<IFile>() {
			@Override
			public IFile runObject() throws Exception {
				if (file.getFileExtension().equalsIgnoreCase("XWT")) {
					return getJavaFile(file);
				}
				if (file.getFileExtension().equalsIgnoreCase("JAVA")) {
					return getXWTFile(file);
				}
				return null;
			}
		}, null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XWT -> Java
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the Java {@link IFile} for given XWT one.
	 */
	private IFile getJavaFile(IFile xwtFile) throws Exception {
		IProject project = xwtFile.getProject();
		IJavaProject javaProject = JavaCore.create(project);
		// try to use x:Class
		{
			String content = IOUtils2.readString(xwtFile);
			if (content.contains("Class")) {
				String className = getClassAttribute(content);
				if (className != null) {
					IType type = javaProject.findType(className);
					if (type != null) {
						return (IFile) type.getCompilationUnit().getUnderlyingResource();
					}
				}
			}
		}
		// try to find Java file in same package
		{
			IFolder folder = (IFolder) xwtFile.getParent();
			IPackageFragment packageFragment = (IPackageFragment) JavaCore.create(folder);
			// find IType
			String formName = xwtFile.getFullPath().removeFileExtension().lastSegment();
			String typeName = packageFragment.getElementName() + "." + formName;
			IType type = javaProject.findType(typeName);
			if (type != null) {
				return (IFile) type.getCompilationUnit().getUnderlyingResource();
			}
		}
		// no Java file
		return null;
	}

	/**
	 * @return the value of "x:Class" attribute for root element.
	 */
	private static String getClassAttribute(String content) throws Exception {
		Document document = new Document(content);
		DocumentEditContext context = new DocumentEditContext(document) {
			@Override
			protected AbstractDocumentHandler createDocumentHandler() {
				return new AbstractDocumentHandler();
			}
		};
		try {
			return context.getRoot().getAttribute("x:Class");
		} finally {
			context.disconnect();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Java -> XWT
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the XWT {@link IFile} for given Java one.
	 */
	private IFile getXWTFile(IFile javaFile) throws Exception {
		// try to find XWT file in same package
		{
			IFolder folder = (IFolder) javaFile.getParent();
			IPackageFragment packageFragment = (IPackageFragment) JavaCore.create(folder);
			for (Object object : packageFragment.getNonJavaResources()) {
				if (object instanceof IFile xwtFile) {
					IFile sameJavaFile = getJavaFile(xwtFile);
					if (javaFile.equals(sameJavaFile)) {
						return xwtFile;
					}
				}
			}
		}
		// no XWT file
		return null;
	}
}

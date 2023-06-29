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
package org.eclipse.wb.internal.dev.builder;

import org.eclipse.wb.internal.dev.Activator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Validator for <code>*.wbp-component.xml</code> descriptions against schema.
 *
 * @author scheglov_ke
 */
public final class ComponentDescriptionValidatorHandler implements BuilderHandler {
	private static final String MARKER_TYPE = "org.eclipse.wb.dev.wbProblem";
	private static final String WBP_COMPONENT_SCHEMA = "org.eclipse.wb.core/schema/wbp-component.xsd";

	////////////////////////////////////////////////////////////////////////////
	//
	// Builder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException {
		project.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		project.accept(new ValidatorResourceVisitor());
	}

	@Override
	public void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new ValidatorDeltaVisitor());
	}

	private void workspaceBuild(IProject project, IProgressMonitor monitor) throws CoreException {
		project.getWorkspace().getRoot().accept(new ValidatorResourceVisitor());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Builder visitors
	//
	////////////////////////////////////////////////////////////////////////////
	private class ValidatorDeltaVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				checkXML(resource);
				break;
			case IResourceDelta.REMOVED :
				break;
			case IResourceDelta.CHANGED :
				checkXML(resource);
				break;
			}
			// return true to continue visiting children.
			return true;
		}
	}
	private class ValidatorResourceVisitor implements IResourceVisitor {
		@Override
		public boolean visit(IResource resource) {
			checkXML(resource);
			return true;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XML validation
	//
	////////////////////////////////////////////////////////////////////////////
	private static boolean m_considerXSD = true;

	private void checkXML(IResource resource) {
		String resourcePathString = resource.getFullPath().toPortableString();
		if (resource instanceof IFile && resourcePathString.endsWith(".xsd") && m_considerXSD) {
			try {
				m_considerXSD = false;
				workspaceBuild(resource.getProject(), null);
			} catch (Throwable e) {
				Activator.log(e);
			} finally {
				m_considerXSD = true;
			}
		}
		if (resource instanceof IFile && resourcePathString.endsWith(".wbp-component.xml")) {
			IFile file = (IFile) resource;
			if (resourcePathString.contains("/bin/")) {
				return;
			}
			deleteMarkers(file);
			try {
				validateComponentDescription(file);
			} catch (Throwable e) {
				Activator.log(e);
			}
		}
	}

	private void validateComponentDescription(IFile file) throws SAXException, CoreException,
	IOException {
		// prepare Schema
		Schema schema;
		{
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			IFile schemaFile =
					ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(WBP_COMPONENT_SCHEMA));
			if (!schemaFile.exists()) {
				return;
			}
			schema = factory.newSchema(new StreamSource(schemaFile.getContents(true)));
		}
		// prepare Validator
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new XmlErrorHandler(file));
		// do validate
		InputStream contents = file.getContents(true);
		try {
			validator.validate(new StreamSource(contents));
		} finally {
			contents.close();
		}
	}

	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XMLErrorHandler
	//
	////////////////////////////////////////////////////////////////////////////
	private class XmlErrorHandler implements ErrorHandler {
		private final IFile file;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public XmlErrorHandler(IFile file) {
			this.file = file;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// ErrorHandler
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void error(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_ERROR);
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			addMarker(exception, IMarker.SEVERITY_WARNING);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Utils
		//
		////////////////////////////////////////////////////////////////////////////
		private void addMarker(SAXParseException e, int severity) {
			ComponentDescriptionValidatorHandler.this.addMarker(
					file,
					e.getMessage(),
					e.getLineNumber(),
					severity);
		}
	}
}

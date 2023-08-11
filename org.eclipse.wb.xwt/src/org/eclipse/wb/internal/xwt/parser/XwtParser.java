/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.parser;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.util.NamePropertySupport;
import org.eclipse.wb.internal.xwt.model.util.NameSupport;
import org.eclipse.wb.internal.xwt.model.util.XwtListenerProperties;
import org.eclipse.wb.internal.xwt.model.util.XwtStaticFieldSupport;
import org.eclipse.wb.internal.xwt.model.util.XwtStringArraySupport;
import org.eclipse.wb.internal.xwt.model.util.XwtTagResolver;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Button;
import org.eclipse.xwt.DefaultLoadingContext;
import org.eclipse.xwt.ILoadingContext;
import org.eclipse.xwt.IXWTLoader;
import org.eclipse.xwt.XWT;
import org.eclipse.xwt.XWTLoaderManager;
import org.eclipse.xwt.core.IElementLoaderFactory;
import org.eclipse.xwt.core.IRenderingContext;
import org.eclipse.xwt.core.IVisualElementLoader;
import org.eclipse.xwt.forms.XWTForms;
import org.eclipse.xwt.internal.core.Core;
import org.eclipse.xwt.internal.xml.Element;
import org.eclipse.xwt.javabean.ResourceLoader;
import org.eclipse.xwt.javabean.metadata.Metaclass;
import org.eclipse.xwt.metadata.IMetaclass;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Parser for XWT UI.
 *
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public final class XwtParser {
	private final EditorContext m_context;
	private final IFile m_file;
	private final IDocument m_document;
	private final Map<String, DocumentElement> m_pathToElementMap = Maps.newHashMap();
	private final Map<String, XmlObjectInfo> m_pathToModelMap = Maps.newHashMap();
	private XmlObjectInfo m_rootModel;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtParser(IFile file, IDocument document) throws Exception {
		m_file = file;
		m_document = document;
		m_context = new XwtEditorContext(file, m_document);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parse
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectInfo parse() throws Exception {
		return ExecutionUtils.runDesignTime(new RunnableObjectEx<XmlObjectInfo>() {
			public XmlObjectInfo runObject() throws Exception {
				return parse0();
			}
		});
	}

	private XmlObjectInfo parse0() throws Exception {
		m_context.initialize();
		m_context.setParsing(true);
		fillMap_pathToElement();
		// notifications from model
		m_context.getBroadcastSupport().addListener(null, new XwtParserBindToElement() {
			public void invoke(XmlObjectInfo object, DocumentElement element) {
				String path = getPath(element);
				m_pathToModelMap.put(path, object);
			}
		});
		// handler for creation events
		Core profile = new Core(new IElementLoaderFactory() {
			private int m_level;

			public IVisualElementLoader createElementLoader(IRenderingContext context, IXWTLoader loader) {
				return new ResourceLoader(context, loader) {
					@Override
					protected void postCreation0(final Element element, final Object targetObject) {
						if (m_level > 1) {
							return;
						}
						ExecutionUtils.runRethrow(new RunnableEx() {
							public void run() throws Exception {
								postCreationEx(element, targetObject);
							}
						});
					}

					private void postCreationEx(Element element, Object targetObject) throws Exception {
						String path = getPath(element);
						DocumentElement xmlElement = m_pathToElementMap.get(path);
						if (xmlElement == null) {
							return;
						}
						// create model
						XmlObjectInfo objectInfo = createObjectInfo(targetObject, xmlElement);
						if (objectInfo == null) {
							return;
						}
						m_pathToModelMap.put(path, objectInfo);
						// add to hierarchy
						if ("0".equals(path)) {
							m_rootModel = objectInfo;
						} else {
							XmlObjectInfo parentObjectInfo = getParentObjectInfo(element);
							if (parentObjectInfo != null) {
								parentObjectInfo.addChild(objectInfo);
							}
						}
					}

					////////////////////////////////////////////////////////////////////////////
					//
					// Tweaks for handling tested XWT files
					//
					////////////////////////////////////////////////////////////////////////////
					private final Set<Element> m_processedElements = Sets.newHashSet();

					private boolean isRoot(Element element) {
						if (!m_processedElements.contains(element)) {
							m_processedElements.add(element);
							return "0".equals(element.getPath());
						}
						return false;
					}

					@Override
					protected Object doCreate(Object parent,
							Element element,
							Class<?> constraintType,
							Map<String, Object> options) throws Exception {
						boolean isRoot = isRoot(element);
						try {
							if (isRoot) {
								m_level++;
							}
							return super.doCreate(parent, element, constraintType, options);
						} finally {
							if (isRoot) {
								m_level--;
							}
						}
					}
				};
			}
		}, XWTLoaderManager.getActive());
		// render, with parsing
		XWT.applyProfile(profile);
		ILoadingContext _loadingContext = XWT.getLoadingContext();
		XWT.setLoadingContext(new DefaultLoadingContext(m_context.getClassLoader()));
		try {
			URI uri = m_file.getLocationURI();
			IPath localPath = URIUtil.toPath(uri);
			String host = uri.getHost();
			URL url = (host != null && localPath == null ? uri : URIUtil.toURI(localPath)).toURL();
			String content = m_document.get();
			Map<String, Object> options = Maps.newHashMap();
			options.put(IXWTLoader.DESIGN_MODE_PROPERTY, Boolean.TRUE);
			configureForForms(m_context, options);
			XWT.loadWithOptions(IOUtils.toInputStream(content), url, options);
		} finally {
			XWT.setLoadingContext(_loadingContext);
			XWT.restoreProfile();
		}
		// done
		m_context.setParsing(false);
		NameSupport.decoratePresentationWithName(m_rootModel);
		XmlObjectUtils.callRootProcessors(m_rootModel);
		new XwtTagResolver(m_rootModel);
		new XwtStringArraySupport(m_rootModel);
		new XwtStaticFieldSupport(m_rootModel);
		new XwtListenerProperties(m_rootModel);
		new NamePropertySupport(m_rootModel);
		GlobalStateXml.activate(m_rootModel);
		m_rootModel.getBroadcast(ObjectInfoTreeComplete.class).invoke();
		m_rootModel.refresh_dispose();
		return m_rootModel;
	}

	/**
	 * Visits all {@link DocumentElement}s and remembers all of them with path.
	 */
	private void fillMap_pathToElement() {
		m_context.getRootElement().accept(new DocumentModelVisitor() {
			@Override
			public void endVisit(DocumentElement element) {
				m_pathToElementMap.put(getPath(element), element);
			}
		});
	}

	private XmlObjectInfo createObjectInfo(Object targetObject, DocumentElement element)
			throws Exception {
		XmlObjectInfo objectInfo;
		{
			Class<?> componentClass = targetObject.getClass();
			CreationSupport creationSupport = new ElementCreationSupport(element);
			objectInfo = XmlObjectUtils.createObject(m_context, componentClass, creationSupport);
			GlobalStateXml.activate(objectInfo);
		}
		// check if model should be created
		if (!XmlObjectUtils.hasTrueParameter(objectInfo, "XWT.hasModel")) {
			return null;
		}
		// done
		objectInfo.setObject(targetObject);
		return objectInfo;
	}

	private XmlObjectInfo getParentObjectInfo(Element element) {
		XmlObjectInfo parent = null;
		String path = element.getPath();
		do {
			path = StringUtils.substringBeforeLast(path, "/");
			parent = m_pathToModelMap.get(path);
		} while (parent == null && path.contains("/"));
		return parent;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Forms API
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String[] FORMS_CLASSES = {
			"org.eclipse.xwt.forms.metaclass.FormMetaclass",
			"org.eclipse.xwt.forms.metaclass.ButtonMetaclass",
			"org.eclipse.xwt.forms.metaclass.CompositeMetaclass",
			"org.eclipse.xwt.forms.metaclass.ExpandableCompositeMetaclass",
			"org.eclipse.xwt.forms.metaclass.FormMetaclass",
			"org.eclipse.xwt.forms.metaclass.FormTextMetaclass",
			"org.eclipse.xwt.forms.metaclass.HyperlinkMetaclass",
			"org.eclipse.xwt.forms.metaclass.ImageHyperlinkMetaclass",
			"org.eclipse.xwt.forms.metaclass.LabelMetaclass",
			"org.eclipse.xwt.forms.metaclass.ScrolledFormMetaclass",
			"org.eclipse.xwt.forms.metaclass.ScrolledPageBookMetaclass",
			"org.eclipse.xwt.forms.metaclass.SectionMetaclass",
			"org.eclipse.xwt.forms.metaclass.TableMetaclass",
	"org.eclipse.xwt.forms.metaclass.TextMetaclass"};

	/**
	 * Configures XWT and options for Forms API support.
	 */
	static void configureForForms(EditorContext context, Map<String, Object> options)
			throws Exception {
		if (!hasForms(context)) {
			XWT.registerMetaclass(new Metaclass(Button.class, null));
			return;
		}
		// install Forms decoration action
		{
			Object createdAction = ReflectionUtils.getFieldObject(XWTForms.class, "CreatedAction");
			options.put(IXWTLoader.CREATED_CALLBACK, createdAction);
		}
		// register IMetaclass-s
		for (String className : FORMS_CLASSES) {
			Class<?> clazz = XWTForms.class.getClassLoader().loadClass(className);
			IMetaclass metaclass = (IMetaclass) clazz.newInstance();
			XWT.registerMetaclass(metaclass);
		}
	}

	public static boolean hasForms(EditorContext context) {
		return context.getDocument().get().contains("Forms API");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Path
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the path of our {@link DocumentElement}. It has same format as
	 *         {@link #getPath(Element)}.
	 */
	static String getPath(DocumentElement element) {
		DocumentElement parent = element.getParent();
		if (parent == null) {
			return "0";
		} else {
			int index = parent.indexOf(element);
			return getPath(parent) + "/" + index;
		}
	}

	/**
	 * @return the path of XWT element. It has same format as {@link #getPath(DocumentElement)}.
	 */
	static String getPath(Element element) {
		return element.getPath();
	}
}

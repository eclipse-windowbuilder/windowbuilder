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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectSetObjectAfter;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.xwt.DefaultLoadingContext;
import org.eclipse.xwt.ILoadingContext;
import org.eclipse.xwt.IXWTLoader;
import org.eclipse.xwt.XWT;
import org.eclipse.xwt.XWTLoaderManager;
import org.eclipse.xwt.core.IElementLoaderFactory;
import org.eclipse.xwt.core.IRenderingContext;
import org.eclipse.xwt.core.IVisualElementLoader;
import org.eclipse.xwt.internal.core.Core;
import org.eclipse.xwt.internal.xml.Element;
import org.eclipse.xwt.javabean.ResourceLoader;
import org.eclipse.xwt.metadata.IMetaclass;
import org.eclipse.xwt.metadata.IProperty;
import org.eclipse.xwt.metadata.ISetPostAction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for XWT UI.
 *
 * @author scheglov_ke
 * @coverage XWT.parser
 */
public final class XwtRenderer {
	private final Map<String, XmlObjectInfo> m_pathToModelMap = Maps.newHashMap();
	private final Map<Object, XmlObjectInfo> m_objectToModelMap = Maps.newHashMap();
	private final XmlObjectInfo m_rootModel;
	private final EditorContext m_context;
	private final ISetPostAction m_setPostAction;
	private final IXWTLoader m_loaderXWT;
	private final Set<IMetaclass> m_knownMetaclassXWT = Sets.newHashSet();
	private final XmlObjectSetObjectAfter m_broadcast_setObjectAfter;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtRenderer(ControlInfo rootControl) throws Exception {
		m_loaderXWT = XWTLoaderManager.getActive();
		//
		m_rootModel = rootControl;
		m_context = m_rootModel.getContext();
		// object -> model
		m_broadcast_setObjectAfter = new XmlObjectSetObjectAfter() {
			public void invoke(XmlObjectInfo target, Object o) throws Exception {
				m_objectToModelMap.put(o, target);
			}
		};
		// hook for properties
		m_setPostAction = new ISetPostAction() {
			public void action(Object target, IProperty property, Object value) {
				XmlObjectInfo xmlObject = m_objectToModelMap.get(target);
				if (xmlObject != null) {
					xmlObject.registerAttributeValue(property.getName(), value);
				}
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Render
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Renders current content of {@link EditorContext} and fill objects for {@link XmlObjectInfo}s.
	 */
	public void render() throws Exception {
		GlobalStateXml.activate(m_rootModel);
		// path -> model
		m_rootModel.accept(new ObjectInfoVisitor() {
			@Override
			public void endVisit(ObjectInfo objectInfo) throws Exception {
				if (objectInfo instanceof XmlObjectInfo) {
					XmlObjectInfo xmlObjectInfo = (XmlObjectInfo) objectInfo;
					CreationSupport creationSupport = xmlObjectInfo.getCreationSupport();
					if (!XmlObjectUtils.isImplicit(xmlObjectInfo)) {
						DocumentElement element = creationSupport.getElement();
						String path = XwtParser.getPath(element);
						m_pathToModelMap.put(path, xmlObjectInfo);
					}
				}
			}
		});
		m_context.getBroadcastSupport().addListener(null, m_broadcast_setObjectAfter);
		// prepare IXWTLoader with intercepting IMetaclass loading
		IXWTLoader loader;
		{
			final IXWTLoader loader0 = XWTLoaderManager.getActive();
			loader =
					(IXWTLoader) Proxy.newProxyInstance(
							getClass().getClassLoader(),
							new Class<?>[]{IXWTLoader.class},
							new InvocationHandler() {
								public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
									Object result = method.invoke(loader0, args);
									String methodSignature = ReflectionUtils.getMethodSignature(method);
									if (methodSignature.equals("registerMetaclass(java.lang.Class)")
											|| methodSignature.equals("getMetaclass(java.lang.String,java.lang.String)")) {
										IMetaclass metaclass = (IMetaclass) result;
										hookProperties(metaclass);
										hookProperties_ofExposedWidgets(metaclass);
									}
									return result;
								}

								private void hookProperties_ofExposedWidgets(IMetaclass metaclass) throws Exception {
									for (Method typeMethod : metaclass.getType().getMethods()) {
										Class<?> returnType = typeMethod.getReturnType();
										if (typeMethod.getParameterTypes().length == 0
												&& Widget.class.isAssignableFrom(returnType)) {
											metaclass =
													(IMetaclass) ReflectionUtils.invokeMethod(
															loader0,
															"registerMetaclass(java.lang.Class)",
															returnType);
											hookProperties(metaclass);
										}
									}
								}
							});
		}
		// provide ResourceLoader with "postCreation"
		Core profile = new Core(new IElementLoaderFactory() {
			private int m_level;

			public IVisualElementLoader createElementLoader(IRenderingContext context, IXWTLoader loader) {
				return new ResourceLoader(context, loader) {
					@Override
					protected Integer getStyleValue(Element element, int styles) {
						Integer styleValue = super.getStyleValue(element, styles);
						if (styleValue != null) {
							String path = XwtParser.getPath(element);
							XmlObjectInfo xmlObject = m_pathToModelMap.get(path);
							if (xmlObject != null) {
								xmlObject.registerAttributeValue("x:Style", styleValue);
							}
						}
						return styleValue;
					}

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
						String path = XwtParser.getPath(element);
						XmlObjectInfo xmlObjectInfo = m_pathToModelMap.get(path);
						if (xmlObjectInfo == null) {
							return;
						}
						// wrapper Shell, ignore it
						{
							Class<? extends Object> targetClass = targetObject.getClass();
							if (targetClass != xmlObjectInfo.getDescription().getComponentClass()) {
								if (targetClass == Shell.class) {
									return;
								}
							}
						}
						// set Object
						xmlObjectInfo.setObject(targetObject);
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
							String path = element.getPath();
							return "0".equals(path);
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
		}, loader);
		// render
		XWT.applyProfile(profile);
		ILoadingContext _loadingContext = XWT.getLoadingContext();
		XWT.setLoadingContext(new DefaultLoadingContext(m_context.getClassLoader()));
		try {
			URI uri = m_context.getFile().getLocationURI();
			IPath localPath = URIUtil.toPath(uri);
			String host = uri.getHost();
			URL url = (host != null && localPath == null ? uri : URIUtil.toURI(localPath)).toURL();
			String content = m_context.getContent();
			Map<String, Object> options = Maps.newHashMap();
			options.put(IXWTLoader.DESIGN_MODE_PROPERTY, Boolean.TRUE);
			content = removeCompositeClassAttribute(content);
			XwtParser.configureForForms(m_context, options);
			XWT.loadWithOptions(IOUtils.toInputStream(content), url, options);
		} finally {
			XWT.setLoadingContext(_loadingContext);
			XWT.restoreProfile();
		}
	}

	/**
	 * XWT behaves weird when "Composite" is rendered using XWT and used also in "x:Class" attribute.
	 * So, we remove "x:Class" at design time.
	 */
	private String removeCompositeClassAttribute(String content) {
		DocumentElement rootElement = m_context.getRootElement();
		DocumentAttribute classAttribute = rootElement.getDocumentAttribute("x:Class");
		if (rootElement.getTagLocal().equals("Composite") && classAttribute != null) {
			int begin = classAttribute.getNameOffset();
			int end = classAttribute.getValueOffset() + classAttribute.getValueLength() + 1;
			content =
					content.substring(0, begin)
					+ StringUtils.repeat(" ", end - begin)
					+ content.substring(end);
		}
		return content;
	}

	/**
	 * Adds {@link #m_setPostAction} for all {@link IProperty}s for given {@link IMetaclass}.
	 */
	private void hookProperties(IMetaclass metaclass) {
		if (m_knownMetaclassXWT.contains(metaclass)) {
			return;
		}
		m_knownMetaclassXWT.add(metaclass);
		for (IProperty property : metaclass.getProperties()) {
			property.addSetPostAction(m_setPostAction);
		}
	}

	/**
	 * Disposes allocated resources, such as remove listeners.
	 */
	public void dispose() {
		m_context.getBroadcastSupport().removeListener(null, m_broadcast_setObjectAfter);
		for (IMetaclass metaclass : m_loaderXWT.getAllMetaclasses()) {
			for (IProperty property : metaclass.getProperties()) {
				property.removeSetPostAction(m_setPostAction);
			}
		}
	}
}

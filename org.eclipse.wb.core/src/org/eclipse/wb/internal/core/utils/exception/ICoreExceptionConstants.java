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
package org.eclipse.wb.internal.core.utils.exception;

/**
 * Constants for "core" {@link DesignerException}'s.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public interface ICoreExceptionConstants {
  int UNEXPECTED = 1;
  int INCOMPLETE_PRODUCT = 2;
  int FUTURE = 99;
  //
  int PARSER_NO_ROOT_METHODS = 100;
  int PARSER_NO_ROOT_WHEN_COMPILATION_ERRORS = 101;
  int PARSER_WARN_IGNORE_METHOD_MULTIPLE_INOCATIONS = 102;
  int PARSER_NO_TOOLKIT = 103;
  int PARSER_WRONG_NON_VISUAL_COMMENT = 104;
  int PARSER_FACTORY_NOT_SUPPORTED = 105;
  int PARSER_NO_PRIMARY_TYPE = 106;
  int PARSER_DOUBLE_ASSOCIATION = 107;
  int PARSER_JAVA_VERSION = 108;
  int PARSER_NO_SUPER_CLASS = 109;
  int PARSER_NO_TYPE_DECLARATION = 110;
  int PARSER_NOT_GUI = 111;
  //
  int AST_EDITOR_REPLACE = 201;
  int AST_EDITOR_ASSIGNMENT = 202;
  int AST_NOT_VARIABLE = 203;
  int AST_NO_TYPE_BINDING = 204;
  int AST_PARSE_ERROR = 205;
  int NO_PLUGIN = 206;
  int GEN_NO_TYPE_BINDING = 207;
  int GEN_NO_CONSTRUCTOR_BINDING = 208;
  //
  int EVAL_UNKNOWN_EXPRESSION_TYPE = 300;
  //int EVAL_UNKNOWN_OPERATOR = 301;
  //int EVAL_NO_METHOD_FOUND = 302;
  int EVAL_NO_SIMPLE_NAME_FOUND = 303;
  int EVAL_NO_METHOD_INVOCATION = 304;
  int EVAL_SINGLE_METHOD_INVOCATION_EXPECTED = 305;
  int EVAL_LOCAL_METHOD_INVOCATION = 306;
  int EVAL_CONSTRUCTOR = 307;
  int EVAL_METHOD = 308;
  int EVAL_SUPER_METHOD = 309;
  int EVAL_NON_PUBLIC_CONSTRUCTOR = 311;
  int EVAL_NULL_INVOCATION_EXPRESSION = 312;
  int EVAL_CGLIB = 313;
  int EVAL_NO_CONSTRUCTOR = 314;
  int EVAL_ANONYMOUS = 315;
  //int EVAL_UNSUPPORTED_NULL = 306;
  //int EVAL_THIS_ARGUMENT_CAN_NOT_PARSE = 307;
  //
  int EXECUTION_FLOW_TOO_MANY_CONSTRUCTORS = 400;
  //
  int DESCRIPTION_NO_DESCRIPTIONS = 500;
  int DESCRIPTION_EDITOR_STATIC_FIELD = 501;
  int DESCRIPTION_LOAD_ERROR = 502;
  int DESCRIPTION_NO_TOOLKIT = 503;
}

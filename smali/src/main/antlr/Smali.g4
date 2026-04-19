grammar Smali;

@header {
   package com.v7878.dex.smali;

   import static com.v7878.dex.Format.*;
}

options {
    superClass=SmaliParserBase;
}

// Whitespace and comments
WS
    : [ \t\r\n]+   -> channel(HIDDEN)
    ;

LINE_COMMENT
    : '#' ~[\r\n]* -> channel(HIDDEN)
    ;

// Directives
CLASS_DIRECTIVE: '.class';
SUPER_DIRECTIVE: '.super';
IMPLEMENTS_DIRECTIVE: '.implements';
SOURCE_DIRECTIVE: '.source';
FIELD_DIRECTIVE: '.field';
END_FIELD_DIRECTIVE: '.end field';
SUBANNOTATION_DIRECTIVE: '.subannotation';
END_SUBANNOTATION_DIRECTIVE: '.end subannotation';
ANNOTATION_DIRECTIVE: '.annotation';
END_ANNOTATION_DIRECTIVE: '.end annotation';
ENUM_DIRECTIVE: '.enum';
METHOD_DIRECTIVE: '.method';
END_METHOD_DIRECTIVE: '.end method';
REGISTERS_DIRECTIVE: '.registers';
LOCALS_DIRECTIVE: '.locals';
ARRAY_DATA_DIRECTIVE: '.array-data';
END_ARRAY_DATA_DIRECTIVE: '.end array-data';
PACKED_SWITCH_DIRECTIVE: '.packed-switch';
END_PACKED_SWITCH_DIRECTIVE: '.end packed-switch';
SPARSE_SWITCH_DIRECTIVE: '.sparse-switch';
END_SPARSE_SWITCH_DIRECTIVE: '.end sparse-switch';
CATCH_DIRECTIVE: '.catch';
CATCHALL_DIRECTIVE: '.catchall';
LINE_DIRECTIVE: '.line';
PARAMETER_DIRECTIVE: '.param';
END_PARAMETER_DIRECTIVE: '.end param';
LOCAL_DIRECTIVE: '.local';
END_LOCAL_DIRECTIVE: '.end local';
RESTART_LOCAL_DIRECTIVE: '.restart local';
PROLOGUE_DIRECTIVE: '.prologue';
EPILOGUE_DIRECTIVE: '.epilogue';

// Separators
LPAREN: '(';
RPAREN: ')';

LBRACE: '{';
RBRACE: '}';

ASSIGN: '=';
COLON: ':';
COMMA: ',';
ARROW: '->';
DOTDOT: '..';

// Fragments
fragment HexDigit: [0-9a-fA-F];

fragment EscapeSequence
    : '\\' [bstnfr"'\\]
    | '\\u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment Char
    : (~["\\\r\n] | EscapeSequence)
    ;

CHAR_LITERAL
    : '\'' Char '\''
    ;

STRING_LITERAL
    : '"' Char* '"'
    ;

fragment DecExponent
    : [eE] '-'? [0-9]+
    ;

fragment BinExponent
    : [pP] '-'? [0-9]+
    ;

// This can only be a float and not an identifier, due to the decimal point
fragment Float1
    : '-'? [0-9]+ '.' [0-9]* DecExponent?
    ;

fragment Float2
    : '-'? '.' [0-9]+ DecExponent?
    ;

fragment Float3
    : '-'? '0' [xX] HexDigit+ '.' HexDigit* BinExponent
    ;

fragment Float4
    : '-'? '0' [xX] '.' HexDigit+ BinExponent
    ;

fragment Float
    : Float1
    | Float2
    | Float3
    | Float4
    ;

FLOAT_LITERAL
    : Float [fF]
    ;

DOUBLE_LITERAL
    : Float [dD]?
    ;

// https://source.android.com/docs/core/runtime/dex-format#simplename
fragment SimpleChar
    : [A-Z]
    | [a-z]
    | [0-9]
// TODO? | ' ' // since DEX version 040
    | '$'
    | '-'
    | '_'
    | '\u{a0}' // since DEX version 040
    | [\u{a1}-\u{1fff}]
    | [\u{2000}-\u{200a}] // since DEX version 040
    | [\u{2010}-\u{2027}]
    | '\u{202f}' // since DEX version 040
    | [\u{2030}-\u{d7ff}]
    | [\u{e000}-\u{ffef}]
    | [\u{10000}-\u{10ffff}]
    ;

fragment SimpleName
    : SimpleChar+
    ;

fragment InitName
    : '<' SimpleName '>'
    ;

fragment ClassName
    : (SimpleName '/')* SimpleName
    ;

fragment ClassDescriptor
    : 'L' ClassName ';'
    ;

fragment ArrayDescriptor
    : '['+ ([ZBSCIFJD] | ClassDescriptor)
    ;

fragment Identifier
    : SimpleName
    | InitName
    | ClassDescriptor
    | ArrayDescriptor
    ;

IDENTIFIER: Identifier;

simple_name: /* TODO: {isSimpleName()}? */ IDENTIFIER;

member_name: /* TODO: {isMemberName()}? */ IDENTIFIER;

register
    : {isRegister()}? IDENTIFIER
    ;

class_descriptor: /* TODO: {is...()}? */ IDENTIFIER;

type_descriptor: /* TODO: {is...()}? */ IDENTIFIER;

reference_type_descriptor: /* TODO: {is...()}? */ IDENTIFIER;

nonvoid_type_descriptor: /* TODO: {is...()}? */ IDENTIFIER;

literal
    : integer_literal
    | long_literal
    | short_literal
    | byte_literal
    | float_literal
    | double_literal
    | CHAR_LITERAL
    | STRING_LITERAL
    | bool_literal
    | null_literal
    | array_literal
    | subannotation
    | type_field_method_literal
    | enum_literal
//  | method_handle_literal
    | method_prototype
    ;

integral_literal
    : integer_literal
    | long_literal
    | short_literal
    | byte_literal
    | CHAR_LITERAL
    ;

fixed_32bit_literal
    : integer_literal
    | long_literal
    | short_literal
    | byte_literal
    | float_literal
    | CHAR_LITERAL
    | bool_literal
    ;

fixed_literal
    : integer_literal
    | long_literal
    | short_literal
    | byte_literal
    | float_literal
    | double_literal
    | CHAR_LITERAL
    | bool_literal
    ;

long_literal
    : {isLong()}? IDENTIFIER
    ;

integer_literal
    : {isInteger()}? IDENTIFIER
    ;

short_literal
    : {isShort()}? IDENTIFIER
    ;

byte_literal
    : {isByte()}? IDENTIFIER
    ;

float_literal
    : {isFloat()}? IDENTIFIER
    | FLOAT_LITERAL // checked
    ;

double_literal
    : {isDouble()}? IDENTIFIER
    | DOUBLE_LITERAL // checked
    ;

null_literal
    : {isNull()}? IDENTIFIER
    ;

bool_literal
    : {isBool()}? IDENTIFIER
    ;

array_literal
    : LBRACE (literal (COMMA literal)* | ) RBRACE
    ;

annotation
    : ANNOTATION_DIRECTIVE annotation_visibility class_descriptor
    annotation_element*
    END_ANNOTATION_DIRECTIVE
    ;

subannotation
    : SUBANNOTATION_DIRECTIVE class_descriptor
    annotation_element*
    END_SUBANNOTATION_DIRECTIVE
    ;

annotation_element
    : simple_name ASSIGN literal
    ;

annotation_visibility
    : {isAnnotationVisibility()}? IDENTIFIER
    ;

enum_literal
    : ENUM_DIRECTIVE field_reference
    ;

type_field_method_literal
    : type_descriptor
    | field_reference
    | method_reference
    ;

// TODO: Multiple primitive types can be parsed as a single ID token
param_list
    : nonvoid_type_descriptor*
    ;

method_prototype
    : LPAREN param_list RPAREN type_descriptor
    ;

method_reference
    : (reference_type_descriptor ARROW)? member_name method_prototype
    ;

field_reference
    : (reference_type_descriptor ARROW)? member_name COLON nonvoid_type_descriptor
    ;

class_spec
    : CLASS_DIRECTIVE access_list class_descriptor
    ;

super_spec
    : SUPER_DIRECTIVE class_descriptor
    ;

implements_spec
    : IMPLEMENTS_DIRECTIVE class_descriptor
    ;

source_spec
    : SOURCE_DIRECTIVE STRING_LITERAL
    ;

access_spec
    : {isAccessFlag()}? IDENTIFIER
    ;

access_list
    : access_spec*
    ;

restriction_spec
    : {isRestrictionFlag()}? IDENTIFIER
    ;

access_or_restriction_spec
    : access_spec
    | restriction_spec
    ;

access_or_restriction_list
    : access_or_restriction_spec*
    ;

field
    : FIELD_DIRECTIVE access_or_restriction_list member_name COLON nonvoid_type_descriptor
    (ASSIGN literal)?
    (annotation END_FIELD_DIRECTIVE)?
    ;

method
    : METHOD_DIRECTIVE access_or_restriction_list member_name method_prototype
    statements_and_directives
    END_METHOD_DIRECTIVE
    ;

debug_directive
    : line_directive
    | local_directive
    | end_local_directive
    | restart_local_directive
    | prologue_directive
    | epilogue_directive
    | source_directive
    ;

line_directive
    : LINE_DIRECTIVE integral_literal
    ;

local_directive
    : LOCAL_DIRECTIVE register
    (COMMA (null_literal | name=STRING_LITERAL) 
    // void as 'no type'
    COLON (type_descriptor)
    (COMMA signature=STRING_LITERAL)? )?
    ;

end_local_directive
    : END_LOCAL_DIRECTIVE register
    ;

restart_local_directive
    : RESTART_LOCAL_DIRECTIVE register
    ;

prologue_directive: PROLOGUE_DIRECTIVE;

epilogue_directive: EPILOGUE_DIRECTIVE;

source_directive
    : SOURCE_DIRECTIVE STRING_LITERAL?
    ;

label
    : COLON simple_name
    ;

// TODO: remove
label_ref: label;

catch_directive
    : CATCH_DIRECTIVE nonvoid_type_descriptor LBRACE from=label_ref DOTDOT to=label_ref RBRACE using=label_ref
    ;

catchall_directive
    : CATCHALL_DIRECTIVE LBRACE from=label_ref DOTDOT to=label_ref RBRACE using=label_ref
    ;

parameter_directive
    : PARAMETER_DIRECTIVE register 
    (COMMA STRING_LITERAL)?
    (annotation END_PARAMETER_DIRECTIVE)?
    ;

statements_and_directives
    : 
    ( ordered_method_item
    | registers_directive
    | catch_directive
    | catchall_directive
    | parameter_directive
    | annotation 
    )*
    ;

ordered_method_item
    : label
//    | instruction
    | debug_directive
    ;

registers_directive
    : (
      directive=REGISTERS_DIRECTIVE count=integral_literal 
    | directive=LOCALS_DIRECTIVE count=integral_literal 
    )
    ;

smali
    : 
  ( class_spec
  | super_spec
  | implements_spec
  | source_spec
  | method
  | field
  | annotation
  )+
  EOF
    ;

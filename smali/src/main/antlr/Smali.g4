grammar Smali;

@header {
   package com.v7878.dex.smali;
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
    : (Float1
    | Float2
    | Float3
    | Float4)
    ([fF] | [dD]?)
    ;

FLOAT_LITERAL: Float;

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

literal
    : IDENTIFIER
    | FLOAT_LITERAL
    | CHAR_LITERAL
    | STRING_LITERAL
    | array_literal
    | subannotation
    | type_field_method_literal
    | enum_literal
// TODO: | method_handle_literal
    | method_prototype
    ;

array_literal
    : LBRACE (literal (COMMA literal)* | ) RBRACE
    ;

annotation_element
    : IDENTIFIER ASSIGN literal
    ;

annotation
    : ANNOTATION_DIRECTIVE visibility=IDENTIFIER IDENTIFIER
    annotation_element* END_ANNOTATION_DIRECTIVE
    ;

subannotation
    : SUBANNOTATION_DIRECTIVE IDENTIFIER annotation_element* END_SUBANNOTATION_DIRECTIVE
    ;

enum_literal
    : ENUM_DIRECTIVE field_reference
    ;

type_field_method_literal
    : IDENTIFIER
    | field_reference
    | method_reference
    ;

param_list
    : IDENTIFIER*
    ;

method_prototype
    : LPAREN param_list RPAREN IDENTIFIER
    ;

method_reference
    : (IDENTIFIER ARROW)? IDENTIFIER method_prototype
    ;

field_reference
    : (IDENTIFIER ARROW)? IDENTIFIER COLON IDENTIFIER
    ;

access_list
    : IDENTIFIER*
    ;

class_spec
    : CLASS_DIRECTIVE access_list type=IDENTIFIER
    ;

super_spec
    : SUPER_DIRECTIVE type=IDENTIFIER
    ;

implements_spec
    : IMPLEMENTS_DIRECTIVE type=IDENTIFIER
    ;

source_spec
    : SOURCE_DIRECTIVE file=STRING_LITERAL
    ;

field
    : FIELD_DIRECTIVE access_list IDENTIFIER COLON
    IDENTIFIER (ASSIGN literal)? 
    (annotation END_FIELD_DIRECTIVE)?
    ;

method
    : METHOD_DIRECTIVE access_list IDENTIFIER method_prototype 
    // TODO: statements_and_directives
    END_METHOD_DIRECTIVE
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

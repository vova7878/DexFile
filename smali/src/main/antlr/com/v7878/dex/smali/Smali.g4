grammar Smali;

@header {
   package com.v7878.dex.smali;
}

// Whitespace and comments

WS           : [ \t\r\n]+   -> channel(HIDDEN);
LINE_COMMENT : '#' ~[\r\n]* -> channel(HIDDEN);

// Directives

CLASS_DIRECTIVE             : '.class';
SUPER_DIRECTIVE             : '.super';
IMPLEMENTS_DIRECTIVE        : '.implements';
SOURCE_DIRECTIVE            : '.source';
FIELD_DIRECTIVE             : '.field';
END_FIELD_DIRECTIVE         : '.end field';
SUBANNOTATION_DIRECTIVE     : '.subannotation';
END_SUBANNOTATION_DIRECTIVE : '.end subannotation';
ANNOTATION_DIRECTIVE        : '.annotation';
END_ANNOTATION_DIRECTIVE    : '.end annotation';
ENUM_DIRECTIVE              : '.enum';
METHOD_DIRECTIVE            : '.method';
END_METHOD_DIRECTIVE        : '.end method';
REGISTERS_DIRECTIVE         : '.registers';
LOCALS_DIRECTIVE            : '.locals';
ARRAY_DATA_DIRECTIVE        : '.array-data';
END_ARRAY_DATA_DIRECTIVE    : '.end array-data';
PACKED_SWITCH_DIRECTIVE     : '.packed-switch';
END_PACKED_SWITCH_DIRECTIVE : '.end packed-switch';
SPARSE_SWITCH_DIRECTIVE     : '.sparse-switch';
END_SPARSE_SWITCH_DIRECTIVE : '.end sparse-switch';
CATCH_DIRECTIVE             : '.catch' ;
CATCHALL_DIRECTIVE          : '.catchall';
LINE_DIRECTIVE              : '.line';
PARAMETER_DIRECTIVE         : '.param';
END_PARAMETER_DIRECTIVE     : '.end param';
LOCAL_DIRECTIVE             : '.local';
END_LOCAL_DIRECTIVE         : '.end local';
RESTART_LOCAL_DIRECTIVE     : '.restart local';
PROLOGUE_DIRECTIVE          : '.prologue';
EPILOGUE_DIRECTIVE          : '.epilogue';

// Separators

LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
SEMI   : ';';
COMMA  : ',';
DOT    : '.';

// Operators

ASSIGN   : '=';
COLON    : ':';
ARROW    : '->';

// Literals
DEC_LITERAL : ('0' | [1-9] (Digits? | '_'+ Digits));
HEX_LITERAL : '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])?;

DEC_LONG_LITERAL : DEC_LITERAL [lL];
HEX_LONG_LITERAL : HEX_LITERAL [lL];

DEC_SHORT_LITERAL : DEC_LITERAL [sS];
HEX_SHORT_LITERAL : HEX_LITERAL [sS];

DEC_BYTE_LITERAL : DEC_LITERAL [tT];
HEX_BYTE_LITERAL : HEX_LITERAL [tT];

// For floats, suffix 'f' is required
DEC_FLOAT_LITERAL:
    (Digits '.' Digits? | '.' Digits) ExponentPart? [fF]
    | Digits ExponentPart? [fF]
;
HEX_FLOAT_LITERAL: '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [fF];

DEC_DOUBLE_LITERAL:
    (Digits '.' Digits? | '.' Digits) ExponentPart? [dD]?
    | Digits (ExponentPart [dD]? | [dD])
;
HEX_DOUBLE_LITERAL: '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [dD]?;

BOOL_LITERAL: 'true' | 'false';
CHAR_LITERAL: '\'' Char '\'';
STRING_LITERAL: '"' Char* '"';

NULL_LITERAL: 'null';

integer_literal
    : DEC_LITERAL
    | HEX_LITERAL
    ;

long_literal
    : DEC_LONG_LITERAL
    | HEX_LONG_LITERAL
    ;

short_literal
    : DEC_SHORT_LITERAL
    | HEX_SHORT_LITERAL
    ;

byte_literal
    : DEC_BYTE_LITERAL
    | HEX_BYTE_LITERAL
    ;

float_literal
    : DEC_FLOAT_LITERAL
    | HEX_FLOAT_LITERAL
    ;

double_literal
    : DEC_DOUBLE_LITERAL
    | HEX_DOUBLE_LITERAL
    ;

literal
  : integer_literal
  | long_literal
  | short_literal
  | byte_literal
  | float_literal
  | double_literal
  | CHAR_LITERAL
  | STRING_LITERAL
  | BOOL_LITERAL
  | NULL_LITERAL
  | array_literal
  | subannotation
  | type_field_method_literal
  | enum_literal
//  | method_handle_literal
  | method_prototype
;

array_literal
  : LBRACE (literal (COMMA literal)* | ) RBRACE;

type_field_method_literal
  : TYPE_DESCRIPTOR
  | method_reference
  | field_reference;

param_list
  : TYPE_DESCRIPTOR*;

method_prototype
  : LPAREN param_list RPAREN TYPE_DESCRIPTOR;

method_reference
  : (TYPE_DESCRIPTOR ARROW)? MEMBER_NAME method_prototype;

field_reference
  : (TYPE_DESCRIPTOR ARROW)? MEMBER_NAME COLON TYPE_DESCRIPTOR;

enum_literal
    : ENUM_DIRECTIVE field_reference;

annotation_element
  : MEMBER_NAME ASSIGN literal;

annotation
  : ANNOTATION_DIRECTIVE ANNOTATION_VISIBILITY TYPE_DESCRIPTOR
    annotation_element* END_ANNOTATION_DIRECTIVE;

subannotation
  : SUBANNOTATION_DIRECTIVE TYPE_DESCRIPTOR annotation_element* END_SUBANNOTATION_DIRECTIVE;

fragment EscapeSequence: '\\' [bstnfr"'\\]
    | '\\u'+ HexDigit HexDigit HexDigit HexDigit;
fragment Char : (~["\\\r\n] | EscapeSequence);

fragment HexDigit: [0-9a-fA-F];
fragment HexDigits: HexDigit ((HexDigit | '_')* HexDigit)?;
fragment Digits: [0-9] ([0-9_]* [0-9])?;

fragment ExponentPart: [eE] [+-]? Digits;

// Misc

ANNOTATION_VISIBILITY : 'build' | 'runtime' | 'system';

ACCESS_SPEC
: 'public' | 'private' | 'protected' | 'static' | 'final' | 'synchronized' | 'super' | 'volatile'
| 'bridge' | 'transient' | 'varargs' | 'native' | 'interface' | 'abstract' | 'strictfp' | 'synthetic'
| 'annotation' | 'enum' | 'mandated' | 'constructor' | 'declared-synchronized' | 'verified' | 'optimized';

MEMBER_NAME : MemberName;
TYPE_DESCRIPTOR : TypeDescriptor;

// https://source.android.com/docs/core/runtime/dex-format#simplename
fragment SimpleNameChar
: [A-Z]
| [a-z]
| [0-9]
| '\\ ' // TODO? | ' ' // since DEX version 040
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

fragment SimpleName : SimpleNameChar+;
fragment MemberName : SimpleName | '<' SimpleName '>';
fragment FullClassName : (SimpleName '/')* SimpleName;
fragment NonArrayFieldTypeDescriptor : [ZBSCIFJD] | 'L' FullClassName ';';
// TODO: '['{0,255}
fragment FieldTypeDescriptor : '['* NonArrayFieldTypeDescriptor;
fragment TypeDescriptor  : 'V' | FieldTypeDescriptor;

smali
  :
  ( class_spec
  | super_spec
  | implements_spec
  | source_spec
//TODO:  | method
  | field
  | annotation
  )+
  EOF
  ;

class_spec
  : CLASS_DIRECTIVE access_list TYPE_DESCRIPTOR;

super_spec
  : SUPER_DIRECTIVE TYPE_DESCRIPTOR;

implements_spec
  : IMPLEMENTS_DIRECTIVE TYPE_DESCRIPTOR;

source_spec
  : SOURCE_DIRECTIVE STRING_LITERAL;

access_list
  : ACCESS_SPEC*;

//TODO: access_or_restriction
//  : ACCESS_SPEC | HIDDENAPI_RESTRICTION;
access_or_restriction : ACCESS_SPEC;

access_or_restriction_list
  : access_or_restriction*;

field : FIELD_DIRECTIVE access_or_restriction_list MEMBER_NAME COLON TYPE_DESCRIPTOR (ASSIGN literal)?
(annotation END_FIELD_DIRECTIVE)?;

// TODO
//method
//  : METHOD_DIRECTIVE access_or_restriction_list member_name method_prototype statements_and_directives
//    END_METHOD_DIRECTIVE;

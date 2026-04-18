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
CATCH_DIRECTIVE             : '.catch';
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
DOTDOT   : '..';

// Literals

fragment HexPrefix : '0' [xX];
fragment HexDigit : [0-9a-fA-F];

fragment Integer1 : '0';
fragment Integer2 : [1-9] [0-9]*;
fragment Integer3 : '0' [0-7]+;
fragment Integer4 : HexPrefix HexDigit+;
fragment Integer : Integer1 | Integer2 | Integer3 | Integer4;

fragment DecimalExponent : [eE] '-'? [0-9]+;
fragment BinaryExponent : [pP] '-'? [0-9]+;

/*This can either be a floating point number or an identifier*/
fragment FloatOrID1 : '-'? [0-9]+ DecimalExponent;
fragment FloatOrID2 : '-'? HexPrefix HexDigit+ BinaryExponent;
fragment FloatOrID3 : '-'? [iI][nN][fF][iI][nN][iI][tT][yY];
fragment FloatOrID4 : [nN][aA][nN];
fragment FloatOrID :  FloatOrID1 | FloatOrID2 | FloatOrID3 | FloatOrID4;

/*This can only be a float and not an identifier, due to the decimal point*/
fragment Float1 : '-'? [0-9]+ '.' [0-9]* DecimalExponent?;
fragment Float2 : '-'? '.' [0-9]+ DecimalExponent?;
fragment Float3 : '-'? HexPrefix HexDigit+ '.' HexDigit* BinaryExponent;
fragment Float4 : '-'? HexPrefix '.' HexDigit+ BinaryExponent;
fragment Float :  Float1 | Float2 | Float3 | Float4;

POSITIVE_INTEGER_LITERAL : Integer;
NEGATIVE_INTEGER_LITERAL : '-' Integer;
LONG_LITERAL  : '-'? Integer [lL];
SHORT_LITERAL : '-'? Integer [sS];
BYTE_LITERAL  : '-'? Integer [tT];

FLOAT_LITERAL_OR_ID  : FloatOrID [fF] | '-'? [0-9]+ [fF];
DOUBLE_LITERAL_OR_ID : FloatOrID [dD]? | '-'? [0-9]+ [dD];

FLOAT_LITERAL  : Float [fF];
DOUBLE_LITERAL : Float [dD]?;

BOOL_LITERAL: 'true' | 'false';
CHAR_LITERAL: '\'' Char '\'';
STRING_LITERAL: '"' Char* '"';

NULL_LITERAL: 'null';

integer_literal
  : POSITIVE_INTEGER_LITERAL | NEGATIVE_INTEGER_LITERAL;

float_literal
  : FLOAT_LITERAL_OR_ID | FLOAT_LITERAL;

double_literal
  : DOUBLE_LITERAL_OR_ID | DOUBLE_LITERAL;

literal
  : LONG_LITERAL
  | integer_literal
  | SHORT_LITERAL
  | BYTE_LITERAL
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
// TODO: | method_handle_literal
  | method_prototype
  ;

array_literal
  : LBRACE (literal (COMMA literal)* | ) RBRACE;

annotation_element
  : simple_name ASSIGN literal;

annotation
  : ANNOTATION_DIRECTIVE ANNOTATION_VISIBILITY CLASS_DESCRIPTOR
    annotation_element* END_ANNOTATION_DIRECTIVE;

subannotation
  : SUBANNOTATION_DIRECTIVE CLASS_DESCRIPTOR annotation_element* END_SUBANNOTATION_DIRECTIVE;

enum_literal
  : ENUM_DIRECTIVE field_reference;

type_field_method_literal
  : type_descriptor
  | field_reference
  | method_reference;

method_prototype
  : LPAREN param_list RPAREN type_descriptor;

method_reference
  : (reference_type_descriptor ARROW)? member_name method_prototype;

field_reference
  : (reference_type_descriptor ARROW)? member_name COLON nonvoid_type_descriptor;

//integral_literal
//  : LONG_LITERAL
//  | integer_literal
//  | SHORT_LITERAL
//  | CHAR_LITERAL
//  | BYTE_LITERAL;
//
//fixed_32bit_literal
//  : LONG_LITERAL
//  | integer_literal
//  | SHORT_LITERAL
//  | BYTE_LITERAL
//  | float_literal
//  | CHAR_LITERAL
//  | BOOL_LITERAL;
//
//fixed_literal
//  : integer_literal
//  | LONG_LITERAL
//  | SHORT_LITERAL
//  | BYTE_LITERAL
//  | float_literal
//  | double_literal
//  | CHAR_LITERAL
//  | BOOL_LITERAL;

// Fragments

fragment EscapeSequence: '\\' [bstnfr"'\\]
    | '\\u'+ HexDigit HexDigit HexDigit HexDigit;
fragment Char : (~["\\\r\n] | EscapeSequence);

fragment SimpleNameChar
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

fragment SimpleName : SimpleNameChar+;
fragment MemberName : '<' SimpleName '>';
fragment ClassName : (SimpleName '/')* SimpleName;
fragment ClassDescriptor : 'L' ClassName ';';

REGISTER : [vp] [0-9]+;
PRIMITIVE_TYPE : [ZBSCIFJD];
VOID_TYPE : 'V';
ARRAY_TYPE_PREFIX : '['+;
CLASS_DESCRIPTOR : ClassDescriptor;

ANNOTATION_VISIBILITY : 'build' | 'runtime' | 'system';

ACCESS_SPEC
: 'public' | 'private' | 'protected' | 'static' | 'final' | 'synchronized' | 'super' | 'volatile'
| 'bridge' | 'transient' | 'varargs' | 'native' | 'interface' | 'abstract' | 'strictfp' | 'synthetic'
| 'annotation' | 'enum' | 'mandated' | 'constructor' | 'declared-synchronized' | 'verified' | 'optimized';

HIDDENAPI_RESTRICTION
: 'whitelist' | 'greylist' | 'blacklist' | 'greylist-max-o' | 'greylist-max-p'
| 'greylist-max-q' | 'greylist-max-r' | 'greylist-max-s' | 'core-platform-api' | 'test-api';

MEMBER_NAME : MemberName;
SIMPLE_NAME : SimpleName;

simple_name
  : SIMPLE_NAME
  | ACCESS_SPEC
  | HIDDENAPI_RESTRICTION
// TODO: | VERIFICATION_ERROR_TYPE
  | POSITIVE_INTEGER_LITERAL
  | NEGATIVE_INTEGER_LITERAL
  | FLOAT_LITERAL_OR_ID
  | DOUBLE_LITERAL_OR_ID
  | BOOL_LITERAL
  | NULL_LITERAL
  | REGISTER
  | param_list_or_id
  | PRIMITIVE_TYPE
  | VOID_TYPE
  | ANNOTATION_VISIBILITY
// TODO: | METHOD_HANDLE_TYPE_FIELD
// TODO: | METHOD_HANDLE_TYPE_METHOD
  ;

array_descriptor
  : ARRAY_TYPE_PREFIX (PRIMITIVE_TYPE | CLASS_DESCRIPTOR);

type_descriptor
  : VOID_TYPE
  | PRIMITIVE_TYPE
  | CLASS_DESCRIPTOR
  | array_descriptor;

nonvoid_type_descriptor
  : PRIMITIVE_TYPE
  | CLASS_DESCRIPTOR
  | array_descriptor;

reference_type_descriptor
  : CLASS_DESCRIPTOR
  | array_descriptor;

member_name
  : simple_name | MEMBER_NAME;

param_list_or_id
  : PRIMITIVE_TYPE+;

param_list
  : param_list_or_id+
  | nonvoid_type_descriptor*;

smali
  :
  (class_spec
  | super_spec
  | implements_spec
  | source_spec
// TODO: | method
  | field
  | annotation
  )+
  EOF
  ;

class_spec : CLASS_DIRECTIVE access_list CLASS_DESCRIPTOR;

super_spec
  : SUPER_DIRECTIVE CLASS_DESCRIPTOR;

implements_spec
  : IMPLEMENTS_DIRECTIVE CLASS_DESCRIPTOR;

source_spec
  : SOURCE_DIRECTIVE STRING_LITERAL;

access_list
  : ACCESS_SPEC*;

access_or_restriction
  : ACCESS_SPEC | HIDDENAPI_RESTRICTION;

access_or_restriction_list
  : access_or_restriction*;

field : FIELD_DIRECTIVE access_or_restriction_list member_name
COLON nonvoid_type_descriptor (ASSIGN literal)?
(annotation END_FIELD_DIRECTIVE)?;

// TODO:
//method
//  : METHOD_DIRECTIVE access_or_restriction_list member_name method_prototype statements_and_directives
//    END_METHOD_DIRECTIVE
//    -> ^(I_METHOD[$start, "I_METHOD"] member_name method_prototype access_or_restriction_list statements_and_directives);
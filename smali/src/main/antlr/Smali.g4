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

fragment At: '@';

INLINE_INDEX
    : 'inline' At '0' [xX] HexDigit+
    ;

VTABLE_INDEX
    : 'vtable' At '0' [xX] HexDigit+
    ;

FIELD_OFFSET
    : 'field' At '0' [xX] HexDigit+
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
    : (SimpleName '/')* SimpleName ';'?
    ;

fragment ArrayDescriptor
    : '['+ ([ZBSCIFJD] | ClassName)
    ;

fragment Identifier
    : SimpleName
    | InitName
    | ClassName
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
//  | method_handle_reference
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

catch_directive
    : CATCH_DIRECTIVE nonvoid_type_descriptor LBRACE from=label DOTDOT to=label RBRACE using=label
    ;

catchall_directive
    : CATCHALL_DIRECTIVE LBRACE from=label DOTDOT to=label RBRACE using=label
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
    | instruction
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

instruction
    : op=IDENTIFIER
    ( {format($op) == Format10t}? args_format10t
    | {format($op) == Format10x}? args_format10x
    | {format($op) == Format11n}? args_format11n
    | {format($op) == Format11x}? args_format11x
    | {format($op) == Format12x}? args_format12x
// TODO:    | {format($op) == Format20bc}? args_format20bc
    | {format($op) == Format20t}? args_format20t
    | {format($op) == Format21c}? args_format21c
    | {format($op) == Format21ih}? args_format21ih
    | {format($op) == Format21lh}? args_format21lh
    | {format($op) == Format21s}? args_format21s
    | {format($op) == Format21t}? args_format21t
    | {format($op) == Format22b}? args_format22b
    | {format($op) == Format22c}? args_format22c
    | {format($op) == Format22s}? args_format22s
    | {format($op) == Format22t}? args_format22t
    | {format($op) == Format22x}? args_format22x
    | {format($op) == Format23x}? args_format23x
    | {format($op) == Format30t}? args_format30t
    | {format($op) == Format31c}? args_format31c
    | {format($op) == Format31i}? args_format31i
    | {format($op) == Format31t}? args_format31t
    | {format($op) == Format32x}? args_format32x
    | {format($op) == Format35c}? args_format35c
    | {format($op) == Format3rc}? args_format3rc
    | {format($op) == Format45cc}? args_format45cc
    | {format($op) == Format4rcc}? args_format4rcc
    | {format($op) == Format51l}? args_format51l
    )
    | insn_array_data_directive
    | insn_packed_switch_directive
    | insn_sparse_switch_directive
    ;

reference
    : STRING_LITERAL
    | type_field_method_literal
    | method_prototype
    //TODO: | callsite_reference
    //TODO: | method_handle_reference
    | INLINE_INDEX
    | VTABLE_INDEX
    | FIELD_OFFSET
    ;

register_list
    : (register (COMMA register)*)?
    ;

register_range
    : (startreg=register (DOTDOT endreg=register)?)?
    ;

args_format10t: label;

args_format10x
    : // nothing
    ;

args_format11n
    : register COMMA integral_literal
    ;

args_format11x: register;

args_format12x
    : register COMMA register
    ;

args_format20t: label;

args_format21c
    : register COMMA reference
    ;

args_format21ih
    : register COMMA fixed_32bit_literal
    ;

args_format21lh
    : register COMMA fixed_32bit_literal
    ;

args_format21s
    : register COMMA integral_literal
    ;

args_format21t
    : register COMMA label
    ;

args_format22b
    : register COMMA register COMMA integral_literal
    ;

args_format22c
    : register COMMA register COMMA reference
    ;

args_format22s
    : register COMMA register COMMA integral_literal
    ;

args_format22t
    : register COMMA register COMMA label
    ;

args_format22x
    : register COMMA register
    ;

args_format23x
    : register COMMA register COMMA register
    ;

args_format30t: label;

args_format31c
    : register COMMA reference
    ;

args_format31i
    : register COMMA fixed_32bit_literal
    ;

args_format31t
    : register COMMA label
    ;

args_format32x
    : register COMMA register
    ;

args_format35c
    : LBRACE register_list RBRACE COMMA reference
    ;

args_format3rc
    : LBRACE register_range RBRACE COMMA reference
    ;

args_format45cc
    : LBRACE register_list RBRACE COMMA reference COMMA reference
    ;

args_format4rcc
    : LBRACE register_range RBRACE COMMA reference COMMA reference
    ;

args_format51l
    : register COMMA fixed_literal
    ;

insn_array_data_directive
    : ARRAY_DATA_DIRECTIVE
    // TODO: check width (must be 1, 2, 4 or 8)
    width=integer_literal
    fixed_literal* END_ARRAY_DATA_DIRECTIVE
    ;

insn_packed_switch_directive
    : PACKED_SWITCH_DIRECTIVE
    fixed_32bit_literal label*
    END_PACKED_SWITCH_DIRECTIVE
    ;

insn_sparse_switch_directive
    : SPARSE_SWITCH_DIRECTIVE
    (fixed_32bit_literal ARROW label)*
    END_SPARSE_SWITCH_DIRECTIVE
    ;

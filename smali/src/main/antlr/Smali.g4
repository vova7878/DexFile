grammar Smali;

@header {
    package com.v7878.dex.smali;

    import static com.v7878.dex.Format.*;
    import com.v7878.dex.immutable.value.*;
    import com.v7878.dex.immutable.*;
    import com.v7878.dex.*;

    import java.util.*;
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
M_PACKED_SWITCH_DIRECTIVE: '.m-packed-switch';
END_M_PACKED_SWITCH_DIRECTIVE: '.end m-packed-switch';
M_SPARSE_SWITCH_DIRECTIVE: '.m-sparse-switch';
END_M_SPARSE_SWITCH_DIRECTIVE: '.end m-sparse-switch';
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
AT: '@';

// Fragments
fragment HexDigit: [0-9a-fA-F];

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment Char
    : (~["\\\r\n] | EscapeSequence)
    ;

CHAR_LITERAL
    : '\'' ('"' | Char) '\''
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

// TODO: move to parser rules
INLINE_INDEX
    : 'inline' AT '0' [xX] HexDigit+
    ;

VTABLE_INDEX
    : 'vtable' AT '0' [xX] HexDigit+
    ;

FIELD_OFFSET
    : 'field' AT '0' [xX] HexDigit+
    ;

// https://source.android.com/docs/core/runtime/dex-format#simplename
fragment SimpleChar
    : [A-Z]
    | [a-z]
    | [0-9]
    // The only character that requires post-processing '\ ' -> ' '
    | '\\ ' // since DEX version 040
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

register
    : {isRegister()}? IDENTIFIER
    ;

simple_name returns[String value]
    : /* TODO: {isSimpleName()}? */ val=IDENTIFIER
    { $value = LiteralUtils.parseSimpleName($val.text); }
    ;

member_name returns[String value]
    : /* TODO: {isMemberName()}? */ val=IDENTIFIER
    { $value = LiteralUtils.parseMemberName($val.text); }
    ;

class_descriptor returns[TypeId value]
    : /* TODO: {is...()}? */ val=IDENTIFIER
    { $value = TypeId.of($val.text); }
    ;

type_descriptor returns[TypeId value]
    : /* TODO: {is...()}? */ val=IDENTIFIER
    { $value = TypeId.of($val.text); }
    ;

reference_type_descriptor returns[TypeId value]
    : /* TODO: {is...()}? */ val=IDENTIFIER
    { $value = TypeId.of($val.text); }
    ;

nonvoid_type_descriptor returns[TypeId value]
    : /* TODO: {is...()}? */ val=IDENTIFIER
    { $value = TypeId.of($val.text); }
    ;

literal returns[EncodedValue value]
    : int_literal { $value = EncodedInt.of($int_literal.value); }
    | long_literal { $value = EncodedLong.of($long_literal.value); }
    | short_literal { $value = EncodedShort.of($short_literal.value); }
    | byte_literal { $value = EncodedByte.of($byte_literal.value); }
    | float_literal { $value = EncodedFloat.of($float_literal.value); }
    | double_literal { $value = EncodedDouble.of($double_literal.value); }
    | char_literal { $value = EncodedChar.of($char_literal.value); }
    | string_literal { $value = EncodedString.of($string_literal.value); }
    | bool_literal { $value = EncodedBoolean.of($bool_literal.value); }
    | null_literal { $value = EncodedNull.INSTANCE; }
    | array_literal { $value = EncodedArray.raw($array_literal.value); }
    | subannotation { $value = $subannotation.value; }
    | type_descriptor { $value = EncodedType.of($type_descriptor.value); }
    | field_reference { $value = EncodedField.of($field_reference.value); }
    | enum_literal { $value = EncodedEnum.of($enum_literal.value); }
    | method_reference { $value = EncodedMethod.of($method_reference.value); }
    | method_handle_reference { $value = EncodedMethodHandle.of($method_handle_reference.value); }
    | method_prototype { $value = EncodedMethodType.of($method_prototype.value); }
    ;

integral_literal returns[int value]
    : int_literal { $value = $int_literal.value; }
    | long_literal { $value = Math.toIntExact($long_literal.value); }
    | short_literal { $value = $short_literal.value; }
    | byte_literal { $value = $byte_literal.value; }
    | char_literal { $value = $char_literal.value; }
    ;

fixed_32bit_literal returns[int value]
    : integral_literal { $value = $integral_literal.value; }
    | float_literal { $value = Float.floatToRawIntBits($float_literal.value); }
    | bool_literal { $value = $bool_literal.value ? 1 : 0; }
    ;

fixed_64bit_literal returns[long value]
    : long_literal { $value = $long_literal.value; }
    | double_literal { $value = Double.doubleToRawLongBits($double_literal.value); }
    // long_literal case was above
    | fixed_32bit_literal { $value = $fixed_32bit_literal.value; }
    ;

char_literal returns[char value]
    : val=CHAR_LITERAL
    { $value = LiteralUtils.parseChar($val.text); }
    ;

string_literal returns[String value]
    : val=STRING_LITERAL
    { $value = LiteralUtils.parseString($val.text); }
    ;

int_literal returns[int value]
    : {isInteger()}? val=IDENTIFIER
    { $value = LiteralUtils.parseInt($val.text); }
    ;

long_literal returns[long value]
    : {isLong()}? val=IDENTIFIER
    { $value = LiteralUtils.parseLong($val.text); }
    ;

short_literal returns[short value]
    : {isShort()}? val=IDENTIFIER
    { $value = LiteralUtils.parseShort($val.text); }
    ;

byte_literal returns[byte value]
    : {isByte()}? val=IDENTIFIER
    { $value = LiteralUtils.parseByte($val.text); }
    ;

float_literal returns[float value]
    : ({isFloat()}? val=IDENTIFIER
    | /* checked */ val=FLOAT_LITERAL)
    { $value = LiteralUtils.parseFloat($val.text); }
    ;

double_literal returns[double value]
    : ({isDouble()}? val=IDENTIFIER
    | /* checked */ val=DOUBLE_LITERAL)
    { $value = LiteralUtils.parseDouble($val.text); }
    ;

null_literal
    : {isNull()}? IDENTIFIER
    ;

bool_literal returns[boolean value]
    : {isBool()}? val=IDENTIFIER
    { $value = LiteralUtils.parseBool($val.text); }
    ;

array_literal returns[List<EncodedValue> value]
    : { $value = new ArrayList<>(); }
    LBRACE (literal { $value.add($literal.value); }
    (COMMA literal { $value.add($literal.value); })* | )
    RBRACE
    { $value = Collections.unmodifiableList($value); }
    ;

annotation_data
    returns [TypeId type, NavigableSet<AnnotationElement> elements]
    : { $elements = new TreeSet<>(); }
    class_descriptor { $type = $class_descriptor.value; }
    (annotation_element { $elements.add($annotation_element.value); })*
    { $elements = Collections.unmodifiableNavigableSet($elements); }
    ;

annotation returns[Annotation value]
    : ANNOTATION_DIRECTIVE annotation_visibility annotation_data
    { $value = Annotation.raw($annotation_visibility.value,
    $annotation_data.type, $annotation_data.elements); }
    END_ANNOTATION_DIRECTIVE
    ;

subannotation returns[EncodedAnnotation value]
    : SUBANNOTATION_DIRECTIVE annotation_data
    { $value = EncodedAnnotation.raw($annotation_data.type, $annotation_data.elements); }
    END_SUBANNOTATION_DIRECTIVE
    ;

annotation_element returns[AnnotationElement value]
    : simple_name ASSIGN literal
    { $value = AnnotationElement.of($simple_name.value, $literal.value); }
    ;

annotation_visibility returns[AnnotationVisibility value]
    : {isAnnotationVisibility()}? val=IDENTIFIER
    { $value = AnnotationVisibility.of($val.text); }
    ;

unchecked_param_list
    // Note: multiple primitive types can be parsed as IDENTIFIER token.
    // A full analysis of the parameter list will occur later
    : IDENTIFIER*
    ;

unchecked_method_prototype
    : LPAREN unchecked_param_list RPAREN type_descriptor
    ;

method_prototype returns[ProtoId value]
    : whole=unchecked_method_prototype
    { $value = ProtoId.of($unchecked_method_prototype.text); }
    ;

method_reference returns[MethodId value]
    // TODO: : (reference_type_descriptor ARROW)? member_name method_prototype
    : dclass=reference_type_descriptor ARROW name=member_name proto=method_prototype
    { $value = MethodId.of($dclass.value, $name.value, $proto.value); }
    ;

field_reference returns[FieldId value]
    // TODO: (reference_type_descriptor ARROW)? member_name COLON nonvoid_type_descriptor
    : dclass=reference_type_descriptor ARROW name=member_name COLON type=nonvoid_type_descriptor
    { $value = FieldId.of($dclass.value, $name.value, $type.value); }
    ;

enum_literal returns[FieldId value]
    : ENUM_DIRECTIVE field_reference
    { $value = $field_reference.value; }
    ;

method_handle_type_field returns[MethodHandleType value]
    : {isMethodHandleTypeField()}? val=IDENTIFIER
    { $value = MethodHandleType.of($val.text); }
    ;

method_handle_type_method returns[MethodHandleType value]
    : {isMethodHandleTypeMethod()}? val=IDENTIFIER
    { $value = MethodHandleType.of($val.text); }
    ;

method_handle_reference returns[MethodHandleId value]
    : ftype=method_handle_type_field AT fid=field_reference
    { $value = MethodHandleId.of($ftype.value, $fid.value); }
    | mtype=method_handle_type_method AT mid=method_reference
    { $value = MethodHandleId.of($mtype.value, $mid.value); }
    ;

call_site_reference
    : simple_name LPAREN string_literal COMMA method_prototype (COMMA literal)* RPAREN AT method_reference
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
    : SOURCE_DIRECTIVE string_literal
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
    (COMMA (null_literal | name=string_literal) 
    // V as 'no type'
    COLON (type_descriptor)
    (COMMA signature=string_literal)? )?
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
    : SOURCE_DIRECTIVE string_literal?
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
    (COMMA string_literal)?
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
    | {format($op) == Format11p}? args_format11p
    | {format($op) == Format11x}? args_format11x
    | {format($op) == Format12x}? args_format12x
// TODO:    | {format($op) == Format20bc}? args_format20bc
    | {format($op) == Format20t}? args_format20t
    | {format($op) == Format20t_24}? args_format20t_24
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
    | {format($op) == Format34c}? args_format34c
    | {format($op) == Format35c}? args_format35c
    | {format($op) == Format3rc}? args_format3rc
    | {format($op) == Format41c}? args_format41c
    | {format($op) == Format45cc}? args_format45cc
    | {format($op) == Format4rcc}? args_format4rcc
    | {format($op) == Format51l}? args_format51l
    | {format($op) == Format52c}? args_format52c
    | {format($op) == Format5rc}? args_format5rc
    )
    | insn_array_data_directive
    | insn_packed_switch_directive
    | insn_sparse_switch_directive
    | insn_m_packed_switch_directive
    | insn_m_sparse_switch_directive
    ;

reference
    : string_literal
    | type_descriptor
    | field_reference
    | method_reference
    | method_prototype
    | call_site_reference
    | method_handle_reference
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

args_format11p
    // TODO: check index (must be [0, 15])
    : register LBRACE index=int_literal RBRACE
    ;

args_format11x: register;

args_format12x
    : register COMMA register
    ;

args_format20t: label;
args_format20t_24: label;

args_format21c
    : register COMMA reference
    ;

args_format21ih
    : register COMMA fixed_32bit_literal
    ;

args_format21lh
    : register COMMA fixed_64bit_literal
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

args_format34c
    : LBRACE register_list RBRACE COMMA reference
    ;

args_format35c
    : LBRACE register_list RBRACE COMMA reference
    ;

args_format3rc
    : LBRACE register_range RBRACE COMMA reference
    ;

args_format41c
    : register COMMA reference
    ;

args_format45cc
    : LBRACE register_list RBRACE COMMA reference COMMA reference
    ;

args_format4rcc
    : LBRACE register_range RBRACE COMMA reference COMMA reference
    ;

args_format51l
    : register COMMA fixed_64bit_literal
    ;

args_format52c
    : register COMMA register COMMA reference
    ;

args_format5rc
    : LBRACE register_range RBRACE COMMA reference
    ;

insn_array_data_directive
    : ARRAY_DATA_DIRECTIVE
    // TODO: check width (must be 1, 2, 4 or 8)
    width=int_literal
    fixed_64bit_literal* END_ARRAY_DATA_DIRECTIVE
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

insn_m_packed_switch_directive
    : M_PACKED_SWITCH_DIRECTIVE
    fixed_32bit_literal label*
    END_M_PACKED_SWITCH_DIRECTIVE
    ;

insn_m_sparse_switch_directive
    : M_SPARSE_SWITCH_DIRECTIVE
    (fixed_32bit_literal ARROW label)*
    END_M_SPARSE_SWITCH_DIRECTIVE
    ;

grammar Smali;

@header {
    package com.v7878.dex.smali.parser;

    import static com.v7878.dex.Format.*;
    import static com.v7878.dex.ReferenceType.*;
    import static com.v7878.dex.MethodHandleType.*;
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
    : val=IDENTIFIER { $value = LiteralUtils.parseSimpleName($val.text); }
    ;

member_name returns[String value]
    : val=IDENTIFIER { $value = LiteralUtils.parseMemberName($val.text); }
    ;

class_descriptor returns[TypeId value]
    : {($value = getClassId()) != null}? val=IDENTIFIER
    ;

type_descriptor returns[TypeId value]
    : {($value = getTypeId()) != null}? val=IDENTIFIER
    ;

reference_type_descriptor returns[TypeId value]
    : {($value = getRefTypeId()) != null}? val=IDENTIFIER
    ;

nonvoid_type_descriptor returns[TypeId value]
    : {($value = getNonVoidTypeId()) != null}? val=IDENTIFIER
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
    @init { $value = new ArrayList<>(); }
    @after { $value = Collections.unmodifiableList($value); }
    : LBRACE (literal { $value.add($literal.value); }
    (COMMA literal { $value.add($literal.value); })* | )
    RBRACE
    ;

annotation_data
    returns [TypeId type, NavigableSet<AnnotationElement> elements]
    @init { $elements = new TreeSet<>(); }
    @after { $elements = Collections.unmodifiableNavigableSet($elements); }
    : class_descriptor { $type = $class_descriptor.value; }
    (annotation_element { $elements.add($annotation_element.value); })*
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

unchecked_method_prototype
    // Note: multiple primitive types can be parsed as
    // IDENTIFIER token. A full analysis will occur later
    : LPAREN IDENTIFIER* RPAREN IDENTIFIER
    ;

method_prototype returns[ProtoId value]
    : whole=unchecked_method_prototype
    { $value = ProtoId.of($whole.text); }
    ;

method_reference returns[MethodId value]
    @init { TypeId declaring_class = null; }
    : (
        (ref=reference_type_descriptor ARROW) { declaring_class = $ref.value; }
        | { declaring_class = $class_def::type; }
    )
    name=member_name proto=method_prototype
    { $value = MethodId.of(declaring_class, $name.value, $proto.value); }
    ;

field_reference returns[FieldId value]
    @init { TypeId declaring_class = null; }
    : (
        (ref=reference_type_descriptor ARROW) { declaring_class = $ref.value; }
        | { declaring_class = $class_def::type; }
    )
    name=member_name COLON type=nonvoid_type_descriptor
    { $value = FieldId.of(declaring_class, $name.value, $type.value); }
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

call_site_reference returns[CallSiteId value]
    @init{
        String name;
        MethodHandleId method_handle;
        String method_name;
        ProtoId method_proto;
        var arguments = new ArrayList<EncodedValue>();
    }
    @after{
        $value = CallSiteId.raw(
            name, method_handle, method_name, method_proto,
            Collections.unmodifiableList(arguments)
        );
    }
    : simple_name { name = $simple_name.value; }
    LPAREN string_literal { method_name = $string_literal.value; }
    COMMA method_prototype  { method_proto = $method_prototype.value; }
    (COMMA literal { arguments.add($literal.value); })*
    RPAREN AT method_reference {
        method_handle = MethodHandleId.of(INVOKE_STATIC, $method_reference.value);
    }
    ;

class_spec returns[TypeId type, int access_flags]
    : CLASS_DIRECTIVE
    access_list { $access_flags = $access_list.access_flags; }
    class_descriptor { $type = $class_descriptor.value; }
    ;

super_spec returns[TypeId value]
    : SUPER_DIRECTIVE class_descriptor
    { $value = $class_descriptor.value; }
    ;

implements_spec returns[TypeId value]
    : IMPLEMENTS_DIRECTIVE class_descriptor
    { $value = $class_descriptor.value; }
    ;

source_spec returns[String value]
    : SOURCE_DIRECTIVE string_literal
    { $value = $string_literal.value; }
    ;

access_spec returns[int value]
    : {isAccessFlag()}? IDENTIFIER
    // TODO: parse
    { $value = 0; }
    ;

access_list returns[int access_flags]
    : (access_spec { $access_flags |= $access_spec.value; })*
    ;

restriction_spec returns[int value]
    : {isRestrictionFlag()}? IDENTIFIER
    // TODO: parse
    { $value = 0; }
    ;

access_or_restriction_list returns[int access_flags, int restriction_flags]
    : (access_spec { $access_flags |= $access_spec.value; }
    | restriction_spec { $restriction_flags |= $restriction_spec.value; }
    )*
    ;

field returns[FieldDef value]
    // TODO: parse
    : FIELD_DIRECTIVE flags=access_or_restriction_list
    name=member_name COLON type=nonvoid_type_descriptor
    (ASSIGN literal)?
    (annotation* END_FIELD_DIRECTIVE)?
    ;

method returns[MethodDef value]
    // TODO: parse
    : METHOD_DIRECTIVE access_or_restriction_list
    member_name method_prototype
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
    (annotation* END_PARAMETER_DIRECTIVE)?
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

class_def
    returns[ClassDef value]
    locals[TypeId type, boolean has_super, boolean has_source]
    @init {
        int access_flags;
        TypeId superclass = null;
        var interfaces = new ArrayList<TypeId>();
        String source = null;
        var fields = new TreeSet<FieldDef>();
        var methods = new TreeSet<MethodDef>();
        var annotations = new TreeSet<Annotation>();
    }
    @after {
        $value = ClassDef.raw(
            $type,
            access_flags,
            superclass,
            Collections.unmodifiableList(interfaces),
            source,
            Collections.unmodifiableNavigableSet(fields),
            Collections.unmodifiableNavigableSet(methods),
            Collections.unmodifiableNavigableSet(annotations)
        );
    }
    : class_spec {
        $type = $class_spec.type;
        access_flags = $class_spec.access_flags;
    }
    (
    { !$has_super }? super_spec {
        superclass = $super_spec.value;
        $has_super = true;
    }
    | { !$has_source }? source_spec {
          source = $source_spec.value;
          $has_source = true;
      }
    | implements_spec { interfaces.add($implements_spec.value); }
    | annotation { annotations.add($annotation.value); }
    | method // TODO: { methods.add($method.value); }
    | field // TODO: { fields.add($field.value); }
    )+
    ;

smali returns[Dex dex]
    @init { var classes = new ArrayList<ClassDef>(); }
    @after { $dex = Dex.raw(Collections.unmodifiableList(classes)); }
    : (class_def { classes.add($class_def.value); })+ EOF
    ;

instruction locals[Opcode op]
    : opname=IDENTIFIER { $op = opcode($opname.text); }
    ( {$op.format() == Format10t}? args_format10t
    | {$op.format() == Format10x}? args_format10x
    | {$op.format() == Format11n}? args_format11n
    | {$op.format() == Format11p}? args_format11p
    | {$op.format() == Format11x}? args_format11x
    | {$op.format() == Format12x}? args_format12x
// TODO: | {$op.format() == Format20bc}? args_format20bc
    | {$op.format() == Format20t}? args_format20t
    | {$op.format() == Format20t_24}? args_format20t_24
    | {$op.format() == Format21c}? args_format21c
    | {$op.format() == Format21ih}? args_format21ih
    | {$op.format() == Format21lh}? args_format21lh
    | {$op.format() == Format21s}? args_format21s
    | {$op.format() == Format21t}? args_format21t
    | {$op.format() == Format22b}? args_format22b
    | {$op.format() == Format22c}? args_format22c
    | {$op.format() == Format22s}? args_format22s
    | {$op.format() == Format22t}? args_format22t
    | {$op.format() == Format22x}? args_format22x
    | {$op.format() == Format23x}? args_format23x
    | {$op.format() == Format30t}? args_format30t
    | {$op.format() == Format31c}? args_format31c
    | {$op.format() == Format31i}? args_format31i
    | {$op.format() == Format31t}? args_format31t
    | {$op.format() == Format32x}? args_format32x
    | {$op.format() == Format34c}? args_format34c
    | {$op.format() == Format35c}? args_format35c
    | {$op.format() == Format3rc}? args_format3rc
    | {$op.format() == Format41c}? args_format41c
    | {$op.format() == Format45cc}? args_format45cc
    | {$op.format() == Format4rcc}? args_format4rcc
    | {$op.format() == Format51l}? args_format51l
    | {$op.format() == Format52c}? args_format52c
    | {$op.format() == Format5rc}? args_format5rc
    )
    | insn_array_data
    | insn_packed_switch
    | insn_sparse_switch
    | insn_m_packed_switch
    | insn_m_sparse_switch
    ;

reference[int index] returns[Object ref] locals[ReferenceType type]
    @init{ $type = $instruction::op.getReferenceType($index); }
    : {$type == STRING}? string_literal { $ref = $string_literal.value; }
    | {$type == TYPE}? type_descriptor { $ref = $type_descriptor.value; }
    | {$type == FIELD}? field_reference { $ref = $field_reference.value; }
    | {$type == METHOD}? method_reference { $ref = $method_reference.value; }
    | {$type == PROTO}? method_prototype { $ref = $method_prototype.value; }
    | {$type == CALLSITE}? call_site_reference { $ref = $call_site_reference.value; }
    | {$type == METHOD_HANDLE}? method_handle_reference { $ref = $method_handle_reference.value; }
    | {$type == RAW_INDEX}?
    ( inline_index // TODO
    | vtable_index // TODO
    | field_offset // TODO
    )
    ;
inline_index: INLINE_INDEX;
vtable_index: VTABLE_INDEX;
field_offset: FIELD_OFFSET;

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
    : register COMMA reference[0]
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
    : register COMMA register COMMA reference[0]
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
    : register COMMA reference[0]
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
    : LBRACE register_list RBRACE COMMA reference[0]
    ;

args_format35c
    : LBRACE register_list RBRACE COMMA reference[0]
    ;

args_format3rc
    : LBRACE register_range RBRACE COMMA reference[0]
    ;

args_format41c
    : register COMMA reference[0]
    ;

args_format45cc
    : LBRACE register_list RBRACE COMMA reference[0] COMMA reference[1]
    ;

args_format4rcc
    : LBRACE register_range RBRACE COMMA reference[0] COMMA reference[1]
    ;

args_format51l
    : register COMMA fixed_64bit_literal
    ;

args_format52c
    : register COMMA register COMMA reference[0]
    ;

args_format5rc
    : LBRACE register_range RBRACE COMMA reference[0]
    ;

// TODO: check width (must be 1, 2, 4 or 8)
args_array_data
    : width=int_literal fixed_64bit_literal*
    ;

args_packed_switch
    : fixed_32bit_literal label*
    ;

args_sparse_switch
    : (fixed_32bit_literal ARROW label)*
    ;

insn_array_data
    : ARRAY_DATA_DIRECTIVE
    args_array_data
    END_ARRAY_DATA_DIRECTIVE
    ;

insn_packed_switch
    : PACKED_SWITCH_DIRECTIVE
    args_packed_switch
    END_PACKED_SWITCH_DIRECTIVE
    ;

insn_sparse_switch
    : SPARSE_SWITCH_DIRECTIVE
    args_sparse_switch
    END_SPARSE_SWITCH_DIRECTIVE
    ;

insn_m_packed_switch
    : M_PACKED_SWITCH_DIRECTIVE
    args_packed_switch
    END_M_PACKED_SWITCH_DIRECTIVE
    ;

insn_m_sparse_switch
    : M_SPARSE_SWITCH_DIRECTIVE
    args_sparse_switch
    END_M_SPARSE_SWITCH_DIRECTIVE
    ;

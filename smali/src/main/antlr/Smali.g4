grammar Smali;

@header {
    package com.v7878.dex.smali.parser;

    import static com.v7878.dex.Format.*;
    import static com.v7878.dex.ReferenceType.*;
    import static com.v7878.dex.MethodHandleType.*;
    import static com.v7878.dex.DexConstants.*;

    import com.v7878.dex.immutable.value.*;
    import com.v7878.dex.immutable.debug.*;
    import com.v7878.dex.immutable.*;
    import com.v7878.dex.builder.*;
    import com.v7878.dex.*;
    import com.v7878.collections.*;

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

IDENTIFIER
    : (Identifier)
    ;

// For some reason, antlr thinks the first parser rule is the entry point,
// and that this rule can't have a returns block. This is ugly
file: smali;

register returns[int value]
    : {isRegister()}? val=IDENTIFIER
    { $value = CodeUtils.parseRegister($method_body::ib, $val.text); }
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
    : {isAccessFlag()}? val=IDENTIFIER
    { $value = AccessFlag.of($val.text).value(); }
    ;

access_list returns[int access_flags]
    : (access_spec { $access_flags |= $access_spec.value; })*
    ;

restriction_spec returns[int value]
    : {isRestrictionFlag()}? IDENTIFIER
    // TODO: parse
    { $value = 0; }
    ;

access_or_restriction_list returns[int access_flags, int restrictions]
    : (access_spec { $access_flags |= $access_spec.value; }
    | restriction_spec { $restrictions |= $restriction_spec.value; }
    )*
    ;

field returns[FieldDef value]
    @init{
        String name;
        TypeId type;
        int access_flags;
        int restrictions;
        EncodedValue initial_value = null;
        var annotations = new TreeSet<Annotation>();
    }
    @after {
        $value = FieldDef.raw(
            name, type, access_flags,
            restrictions, initial_value,
            Collections.unmodifiableNavigableSet(annotations)
        );
    }
    : FIELD_DIRECTIVE
    flags=access_or_restriction_list {
        access_flags = $flags.access_flags;
        restrictions = $flags.restrictions;
    }
    member_name { name = $member_name.value; }
    COLON type=nonvoid_type_descriptor { type = $type.value; }
    (ASSIGN literal { initial_value = $literal.value; })?
    ((annotation { add(annotations, $annotation.value); })*
    END_FIELD_DIRECTIVE)?
    ;

method returns[MethodDef value]
    locals[
        int args, ProtoId proto,
        NavigableSet<Annotation> annotations
    ]
    @init{
        String name;
        int access_flags;
        int restrictions;
        MethodImplementation impl;
        List<Parameter> parameters;
        $annotations = new TreeSet<>();
    }
    @after{
        $annotations = Collections.unmodifiableNavigableSet($annotations);
        $value = MethodDef.raw(
            name, $proto.getReturnType(), parameters,
            access_flags, restrictions, impl, $annotations
        );
    }
    : METHOD_DIRECTIVE
    flags=access_or_restriction_list {
        access_flags = $flags.access_flags;
        restrictions = $flags.restrictions;
    }
    member_name { name = $member_name.value; }
    method_prototype {
        $proto = $method_prototype.value;
        $args = $proto.countInputRegisters();
        // Add 'this' reg
        $args += (access_flags & ACC_STATIC) == 0 ? 1 : 0;
    }
    method_body {
        impl = $method_body.value;
        parameters = $method_body.parameters;
    }
    END_METHOD_DIRECTIVE
    ;

method_body
    returns[
        MethodImplementation value,
        List<Parameter> parameters
    ]
    locals[
        CodeBuilder ib, IntMap<String> pnames,
        IntMap<NavigableSet<Annotation>> pannos,
        List<Runnable> actions
    ]
    @after{
        if ($ib == null) {
            $parameters = Parameter.listOf($method::proto.getParameterTypes());
        } else {
            $actions.forEach(Runnable::run);
            $value = $ib.finish();
            $parameters = mergeParameters($ib.registers(),
                $method::proto, $pnames.freeze(), $pannos.freeze());
        }
    }
    : (
    ({$ib == null}? regs=registers_directive {
        $ib = CodeBuilder.newInstance($regs.value, $method::args);
        $pnames = new IntMap<>(); $pannos = new IntMap<>();
        $actions = new ArrayList<>();
    })
    | ({$ib != null}?
        ( instruction
        | label_directive
        | debug_directive
        | catch_directive
        | catchall_directive
        | parameter_directive
        )
    )
    | annotation { add($method::annotations, $annotation.value); }
    )*
    ;

registers_directive returns[int value]
    : ( REGISTERS_DIRECTIVE count=integral_literal
    { $value = $count.value; }
    | LOCALS_DIRECTIVE count=integral_literal
    { $value = $count.value + $method::args; }
    )
    ;

parameter_directive
    @init{ int p; var annotations = new TreeSet<Annotation>(); }
    : PARAMETER_DIRECTIVE register { p = $register.value; }
    (COMMA name=string_literal { $method_body::pnames.append(p, $name.value); })?
    ((annotation { add(annotations, $annotation.value); })* END_PARAMETER_DIRECTIVE)?
    { $method_body::pannos.append(p, Collections.unmodifiableNavigableSet(annotations)); }
    ;

label returns[String value]
    : COLON simple_name { $value = $simple_name.value; }
    ;

label_directive
    : label { $method_body::ib.label($label.value); }
    ;

catch_directive
    : CATCH_DIRECTIVE ex=nonvoid_type_descriptor
    LBRACE from=label DOTDOT to=label RBRACE handler=label
    { $method_body::ib.try_catch($from.value, $to.value, $ex.value, $handler.value); }
    ;

catchall_directive
    : CATCHALL_DIRECTIVE
    LBRACE from=label DOTDOT to=label RBRACE handler=label
    { $method_body::ib.try_catch_all($from.value, $to.value, $handler.value); }
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
    : LINE_DIRECTIVE line=integral_literal
    { $method_body::ib.line($line.value); }
    ;

local_directive
    @init{
        int reg; String name = null;
        TypeId type = null; String signature = null;
    }
    : LOCAL_DIRECTIVE register { reg = $register.value; }
    (COMMA (null_literal | (name=string_literal { name=$name.value; }))
    // V as 'no type'
    COLON (type=type_descriptor { type = $type.value; })
    (COMMA signature=string_literal { signature = $signature.value; })?)?
    { $method_body::ib.local(reg, name, type, signature); }
    ;
end_local_directive
    : END_LOCAL_DIRECTIVE register
    { $method_body::ib.end_local($register.value); }
    ;

restart_local_directive
    : RESTART_LOCAL_DIRECTIVE register
    { $method_body::ib.restart_local($register.value); }
    ;

prologue_directive
    : PROLOGUE_DIRECTIVE
    { $method_body::ib.prologue(); }
    ;

epilogue_directive
    : EPILOGUE_DIRECTIVE
    { $method_body::ib.epilogue(); }
    ;

source_directive
    @init{ String name = null; }
    : SOURCE_DIRECTIVE (name=string_literal { name = $name.value; })?
    { $method_body::ib.source(name); }
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
            $type, access_flags, superclass,
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
    | annotation { add(annotations, $annotation.value); }
    | method { methods.add($method.value); }
    | field { fields.add($field.value); }
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
    // TODO: | {$op.format() == Format40sc}? args_format40sc
    | {$op.format() == Format41c}? args_format41c
    | {$op.format() == Format45cc}? args_format45cc
    | {$op.format() == Format4rcc}? args_format4rcc
    | {$op.format() == Format51l}? args_format51l
    | {$op.format() == Format52c}? args_format52c
    | {$op.format() == Format5rc}? args_format5rc
    // TODO: | {$op.format() == FormatRaw}? args_format_raw
    )
    | insn_array_data
    | insn_packed_switch
    | insn_sparse_switch
    ;

reference[int index] returns[Object value] locals[ReferenceType type]
    @init{ $type = $instruction::op.referenceType($index); }
    : {$type == STRING}? string_literal { $value = $string_literal.value; }
    | {$type == TYPE}? type_descriptor { $value = $type_descriptor.value; }
    | {$type == FIELD}? field_reference { $value = $field_reference.value; }
    | {$type == METHOD}? method_reference { $value = $method_reference.value; }
    | {$type == PROTO}? method_prototype { $value = $method_prototype.value; }
    | {$type == CALLSITE}? call_site_reference { $value = $call_site_reference.value; }
    | {$type == METHOD_HANDLE}? method_handle_reference { $value = $method_handle_reference.value; }
    | {$type == RAW_INDEX}?
    ( inline_index { $value = $inline_index.value; }
    | vtable_index { $value = $vtable_index.value; }
    | field_offset { $value = $field_offset.value; }
    )
    ;

inline_index returns[int value]
    : INLINE_INDEX
    // TODO
    { throw new UnsupportedOperationException("Unimplemented yet!"); }
    ;

vtable_index returns[int value]
    : VTABLE_INDEX
    // TODO
    { throw new UnsupportedOperationException("Unimplemented yet!"); }
    ;

field_offset returns[int value]
    : FIELD_OFFSET
    // TODO
    { throw new UnsupportedOperationException("Unimplemented yet!"); }
    ;

register_list returns[int[] value]
    : (regs+=register (COMMA regs+=register)*)?
    { $value = $regs.stream().mapToInt(r -> r.value).toArray(); }
    ;

register_range returns[int start, int count]
    : (register { $start = $register.value; }
         ((DOTDOT register { $count = $register.value - $start + 1; })
         | { $count = 1; }
         )
    )
    | { $start = $count = 0; }
    ;

args_format10t returns[String target]
    : label { $target = $label.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f10t($instruction::op, $target);
    }
    ;

args_format10x
    : // nothing
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f10x($instruction::op);
    }
    ;

args_format11n returns[int reg, int lit]
    : register { $reg = $register.value; }
    COMMA integral_literal { $lit = $integral_literal.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f11n($instruction::op, $reg, $lit);
    }
    ;

args_format11p
    // TODO: check index (must be [0, 15])
    : register LBRACE index=int_literal RBRACE
    // TODO
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            throw new UnsupportedOperationException("Unimplemented yet!");
    }
    ;

args_format11x returns[int reg]
    : register { $reg = $register.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f11x($instruction::op, $reg);
    }
    ;

args_format12x returns[int reg1, int reg2]
    : r1=register { $reg1 = $r1.value; }
    COMMA r2=register { $reg2 = $r2.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f12x($instruction::op, $reg1, $reg2);
    }
    ;

args_format20t returns[String target]
    : label { $target = $label.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f20t($instruction::op, $target);
    }
    ;

args_format20t_24 returns[String target]
    : label { $target = $label.value; }
    // TODO
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            throw new UnsupportedOperationException("Unimplemented yet!");
    }
    ;

args_format21c returns[int reg, Object ref]
    : register { $reg = $register.value; }
    COMMA reference[0] { $ref = $reference.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f21c($instruction::op, $reg, $ref);
    }
    ;

args_format21ih returns[int reg, int lit]
    : register { $reg = $register.value; }
    COMMA l32=fixed_32bit_literal { $lit = $l32.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f21ih($instruction::op, $reg, $lit);
    }
    ;

args_format21lh returns[int reg, long lit]
    : register { $reg = $register.value; }
    COMMA l64=fixed_64bit_literal { $lit = $l64.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f21lh($instruction::op, $reg, $lit);
    }
    ;

args_format21s returns[int reg, int lit]
    : register { $reg = $register.value; }
    COMMA integral_literal { $lit = $integral_literal.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f21s($instruction::op, $reg, $lit);
    }
    ;

args_format21t returns[int reg, String target]
    : register { $reg = $register.value; }
    COMMA label { $target = $label.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f21t($instruction::op, $reg, $target);
    }
    ;

args_format22b returns[int reg1, int reg2, int lit]
    : r1=register { $reg1 = $r1.value; } COMMA r2=register { $reg2 = $r2.value; }
    COMMA integral_literal { $lit = $integral_literal.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f22b($instruction::op, $reg1, $reg2, $lit);
    }
    ;

args_format22c returns[int reg1, int reg2, Object ref]
    : r1=register { $reg1 = $r1.value; } COMMA r2=register { $reg2 = $r2.value; }
    COMMA reference[0] { $ref = $reference.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f22c($instruction::op, $reg1, $reg2, $ref);
    }
    ;

args_format22s returns[int reg1, int reg2, int lit]
    : r1=register { $reg1 = $r1.value; } COMMA r2=register { $reg2 = $r2.value; }
    COMMA integral_literal { $lit = $integral_literal.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f22s($instruction::op, $reg1, $reg2, $lit);
    }
    ;

args_format22t returns[int reg1, int reg2, String target]
    : r1=register { $reg1 = $r1.value; } COMMA r2=register { $reg2 = $r2.value; }
    COMMA label { $target = $label.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f22t($instruction::op, $reg1, $reg2, $target);
    }
    ;

args_format22x returns[int reg1, int reg2]
    : r1=register { $reg1 = $r1.value; }
    COMMA r2=register { $reg2 = $r2.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f22x($instruction::op, $reg1, $reg2);
    }
    ;

args_format23x returns[int reg1, int reg2, int reg3]
    : r1=register { $reg1 = $r1.value; }
    COMMA r2=register { $reg2 = $r2.value; }
    COMMA r3=register { $reg3 = $r3.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f23x($instruction::op, $reg1, $reg2, $reg3);
    }
    ;

args_format30t returns[String target]
    : label { $target = $label.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f30t($instruction::op, $target);
    }
    ;

args_format31c returns[int reg, Object ref]
    : register { $reg = $register.value; }
    COMMA reference[0] { $ref = $reference.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f31c($instruction::op, $reg, $ref);
    }
    ;

args_format31i returns[int reg, int lit]
    : register { $reg = $register.value; }
    COMMA l32=fixed_32bit_literal { $lit = $l32.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f31i($instruction::op, $reg, $lit);
    }
    ;

args_format31t returns[int reg, String target]
    : register { $reg = $register.value; }
    COMMA label { $target = $label.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f31t($instruction::op, $reg, $target);
    }
    ;

args_format32x returns[int reg1, int reg2]
    : r1=register { $reg1 = $r1.value; }
    COMMA r2=register { $reg2 = $r2.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f32x($instruction::op, $reg1, $reg2);
    }
    ;

args_format34c returns[int[] args, Object ref]
    : LBRACE register_list { $args = $register_list.value; }
    RBRACE COMMA reference[0] { $ref = $reference.value; }
    // TODO
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            throw new UnsupportedOperationException("Unimplemented yet!");
    }
    ;

args_format35c returns[int[] args, Object ref]
    : LBRACE register_list { $args = $register_list.value; }
    RBRACE COMMA reference[0] { $ref = $reference.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f35c($instruction::op, $ref, $args);
    }
    ;

args_format3rc returns[int start, int count, Object ref]
    : LBRACE rr=register_range { $start = $rr.start; $count = $rr.count; }
    RBRACE COMMA reference[0] { $ref = $reference.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f3rc($instruction::op, $ref, $count, $start);
    }
    ;

args_format41c returns[int reg, Object ref]
    : register { $reg = $register.value; }
    COMMA reference[0] { $ref = $reference.value; }
    // TODO
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            throw new UnsupportedOperationException("Unimplemented yet!");
    }
    ;

args_format45cc returns[int[] args, Object ref1, Object ref2]
    : LBRACE register_list { $args = $register_list.value; }
    RBRACE COMMA r1=reference[0] { $ref1 = $r1.value; }
    COMMA r2=reference[1] { $ref2 = $r2.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f45cc($instruction::op, $ref1, $ref2, $args);
    }
    ;

args_format4rcc returns[int start, int count, Object ref1, Object ref2]
    : LBRACE rr=register_range { $start = $rr.start; $count = $rr.count; }
    RBRACE COMMA r1=reference[0] { $ref1 = $r1.value; }
    COMMA r2=reference[1] { $ref2 = $r2.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f4rcc($instruction::op, $ref1, $ref2, $count, $start);
    }
    ;

args_format51l returns[int reg, long lit]
    : register { $reg = $register.value; }
    COMMA l64=fixed_64bit_literal { $lit = $l64.value; }
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            $method_body::ib.f51l($instruction::op, $reg, $lit);
    }
    ;

args_format52c returns[int reg1, int reg2, Object ref]
    : r1=register { $reg1 = $r1.value; } COMMA r2=register { $reg2 = $r2.value; }
    COMMA reference[0] { $ref = $reference.value; }
    // TODO
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            throw new UnsupportedOperationException("Unimplemented yet!");
    }
    ;

args_format5rc returns[int start, int count, Object ref]
    : LBRACE rr=register_range { $start = $rr.start; $count = $rr.count; }
    RBRACE COMMA reference[0] { $ref = $reference.value; }
    // TODO
    {
        if(!CodeUtils.instruction($method_body::ib, $instruction::op, $ctx, $method_body::actions))
            throw new UnsupportedOperationException("Unimplemented yet!");
    }
    ;

args_array_data returns[int width, List<Number> table]
    : int_literal v+=fixed_64bit_literal*
    {
        $width = $int_literal.value;
        var data = $v.stream().mapToLong(v -> v.value).toArray();
        $table = CodeUtils.parseArrayData($width, data);
    }
    ;

args_packed_switch returns[IntMap<Object> table]
    : key=fixed_32bit_literal l+=label*
    {
        var first_key = $key.value;
        var labels = $l.stream().map(v -> v.value).toArray();
        $table = CodeUtils.parsePackedSwitch(first_key, labels);
    }
    ;

args_sparse_switch returns[IntMap<Object> table]
    : (v+=fixed_32bit_literal ARROW l+=label)*
    {
        var keys = $v.stream().mapToInt(v -> v.value).toArray();
        var labels = $l.stream().map(v -> v.value).toArray();
        $table = CodeUtils.parseSparseSwitch(keys, labels);
    }
    ;

insn_array_data
    : ARRAY_DATA_DIRECTIVE
    payload=args_array_data
    { $method_body::ib.put_metadata($payload.ctx); }
    END_ARRAY_DATA_DIRECTIVE
    ;

insn_packed_switch
    : PACKED_SWITCH_DIRECTIVE
    payload=args_packed_switch
    { $method_body::ib.put_metadata($payload.ctx); }
    END_PACKED_SWITCH_DIRECTIVE
    ;

insn_sparse_switch
    : SPARSE_SWITCH_DIRECTIVE
    payload=args_sparse_switch
    { $method_body::ib.put_metadata($payload.ctx); }
    END_SPARSE_SWITCH_DIRECTIVE
    ;

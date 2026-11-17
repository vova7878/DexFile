# Dalvik executable instruction formats

This page lists the instruction formats used by the Dalvik executable (DEX) format and Dalvik
bytecode. It is meant to be used in conjunction with the
[bytecode reference document](./dalvik-bytecode.md).

## Bitwise descriptions

The first column in the format table lists the bitwise layout of the format. It consists of one or
more space-separated "words" each of which describes a 16-bit code unit. Each character in a word
represents four bits, read from high bits to low, with vertical bars ("`|`") interspersed to aid in
reading. Uppercase letters in sequence from "`A`" are used to indicate fields within the format
(which then get defined further by the syntax column). The term "`op`" is used to indicate the
position of an eight-bit opcode within the format. A slashed zero ("`Ø`") is used to indicate that
all bits must be zero in the indicated position. The first code unit of an expanded-opcode format
carries the field *`exop`*, spelled "`NN|ff`": the selector "`NN`" in the high byte and the marker
"`ff`" in the low byte.

For the most part, lettering proceeds from earlier code units to later code units, and low-order to
high-order within a code unit. However, there are a few exceptions to this general rule, which are
done in order to make the naming of similar-meaning parts be the same across different instruction
formats. These cases are noted explicitly in the format descriptions.

For example, the format "`B|A|`*`op`*`CCCC`" indicates that the format consists of two 16-bit code
units. The first word consists of the opcode in the low eight bits and a pair of four-bit values in
the high eight bits; and the second word consists of a single 16-bit value.

## Format IDs

The second column in the format table indicates the short identifier for the format, which is used
in other documents and in code to identify the format.

Most format IDs consist of three characters, two digits followed by a letter. The first digit
indicates the number of 16-bit code units in the format. The second digit indicates the maximum
number of registers that the format contains (maximum, since some formats can accommodate a variable
number of registers), with the special designation "`r`" indicating that a range of registers is
encoded. The final letter semi-mnemonically indicates the type of any extra data encoded by the
format. For example, format "`21t`" is of length two, contains one register reference, and
additionally contains a branch target.

Suggested static linking formats have an additional "`s`" suffix, making them four characters total.
Similarly, suggested "inline" linking formats have an additional "`i`" suffix. (In this context,
inline linking is like static linking, except with more direct ties into a machine's
implementation.) Finally, a couple oddball suggested formats (e.g., "`20bc`") include two pieces of
data which are both represented in its format ID.

The full list of typecode letters are as follows. Note that some forms have different sizes,
depending on the format:

| Mnemonic | Bit Sizes     | Meaning                                                                                         |
|----------|---------------|-------------------------------------------------------------------------------------------------|
| b        | 8             | immediate signed **b**yte                                                                       |
| c        | 16, 32        | **c**onstant pool index                                                                         |
| f        | 16            | inter**f**ace constants (only used in statically linked formats)                                |
| h        | 16            | immediate signed **h**at (high-order bits of a 32- or 64-bit value; low-order bits are all `0`) |
| i        | 32            | immediate signed **i**nt, or 32-bit float                                                       |
| l        | 64            | immediate signed **l**ong, or 64-bit double                                                     |
| m        | 16            | **m**ethod constants (only used in statically linked formats)                                   |
| n        | 4             | immediate signed **n**ibble                                                                     |
| p        | 4             | index into the fixed table of 16 **p**redefined constants (format `11p`)                        |
| s        | 16            | immediate signed **s**hort                                                                      |
| t        | 8, 16, 24, 32 | branch **t**arget                                                                               |
| x        | 0             | no additional data                                                                              |

The statically linked ("`s`"), inline-linked ("`i`"), odex, quickened and expanded-opcode formats
are defined by one runtime or one era only; they are listed in the same table and described in their
own sections below.

## Syntax

The third column of the format table indicates the human-oriented syntax for instructions which use
the indicated format. Each instruction starts with the named opcode and is optionally followed by
one or more arguments, themselves separated with commas.

Wherever an argument refers to a field from the first column, the letter for that field is indicated
in the syntax, repeated once for each four bits of the field. For example, an eight-bit field
labeled "`BB`" in the first column would also be labeled "`BB`" in the syntax column.

Arguments which name a register have the form "`v`*`X`*". The prefix "`v`" was chosen instead of the
more common "`r`" exactly to avoid conflicting with (non-virtual) architectures on which the Dalvik
Executable format might be implemented which themselves use the prefix "`r`" for their registers.
(That is, this decision makes it possible to talk about both virtual and real registers together
without the need for circumlocution.)

Arguments which indicate a literal value have the form "`#+`*`X`*". Some formats indicate literals
that only have non-zero bits in their high-order bits; for these, the zeroes are represented
explicitly in the syntax, even though they do not appear in the bitwise representation. An argument
of the form "`#`*`X`*" (without the plus) is an index into the fixed table of sixteen predefined
constants used by format `11p`, not a literal.

Arguments which indicate a relative instruction address offset have the form "`+`*`X`*".

Arguments which indicate a literal constant pool index have the form "*`kind`*`@`*`X`*", where
"*`kind`*" indicates which constant pool is being referred to. Each opcode that uses such a format
explicitly allows only one kind of constant; see the opcode reference to figure out the
correspondence. The kinds of constant pool are "`string`" (string pool index), "`type`" (type pool
index), "`field`" (field pool index), "`meth`" (method pool index), "`proto`" (prototype pool
index), "`method_handle`" (method handle pool index), and "`site`" (call site pool index).

Similar to the representation of constant pool indices, there are also suggested (optional) forms
that indicate prelinked offsets or indices. There are four types of suggested prelinked value:
vtable offsets (indicated as "`vtaboff`"), field offsets (indicated as "`fieldoff`"), interface
constants (indicated as "`iface`"), and inline-method-table indices (indicated as "`inline`").

In the cases where a format value isn't explicitly part of the syntax but instead picks a variant,
each variant is listed with the prefix "`[`*`X`*`=`*`N`*`]`" (e.g., "`[A=2]`") to indicate the
correspondence.

## Formats

| Format                                                 | ID   | Syntax                                                                                                                                                                                                                                                                                                                                                                 | Notable Opcodes Covered                                                                         |
|--------------------------------------------------------|------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| *N/A*                                                  | 00x  | *`N/A`*                                                                                                                                                                                                                                                                                                                                                                | *suggested for use as the nominal format for a breakpoint opcode*                               |
| ØØ\|*op*                                               | 10x  | *`op`*                                                                                                                                                                                                                                                                                                                                                                 |                                                                                                 |
| B\|A\|*op*                                             | 12x  | *`op`* vA, vB                                                                                                                                                                                                                                                                                                                                                          |                                                                                                 |
|                                                        | 11n  | *`op`* vA, #+B                                                                                                                                                                                                                                                                                                                                                         |                                                                                                 |
|                                                        | 11p  | *`op`* vA, #B                                                                                                                                                                                                                                                                                                                                                          | `const/special`, `const-wide/special`                                                           |
| AA\|*op*                                               | 11x  | *`op`* vAA                                                                                                                                                                                                                                                                                                                                                             |                                                                                                 |
|                                                        | 10t  | *`op`* +AA                                                                                                                                                                                                                                                                                                                                                             | `goto`                                                                                          |
| ØØ\|*op* AAAA                                          | 20t  | *`op`* +AAAA                                                                                                                                                                                                                                                                                                                                                           | `goto/16`                                                                                       |
| AA<sub>hi</sub>\|*op* AAAA<sub>lo</sub>                | 20t  | *`op`* +AAAAAA                                                                                                                                                                                                                                                                                                                                                         | `goto/24` (dex `009` / `013`)                                                                   |
| AA\|*op* BBBB                                          | 20bc | *`op`* AA, kind@BBBB                                                                                                                                                                                                                                                                                                                                                   | `throw-verification-error`                                                                      |
| AA\|*op* BBBB                                          | 22x  | *`op`* vAA, vBBBB                                                                                                                                                                                                                                                                                                                                                      |                                                                                                 |
|                                                        | 21t  | *`op`* vAA, +BBBB                                                                                                                                                                                                                                                                                                                                                      |                                                                                                 |
|                                                        | 21s  | *`op`* vAA, #+BBBB                                                                                                                                                                                                                                                                                                                                                     |                                                                                                 |
|                                                        | 21h  | *`op`* vAA, #+BBBB0000<br>*`op`* vAA, #+BBBB000000000000                                                                                                                                                                                                                                                                                                               |                                                                                                 |
|                                                        | 21c  | *`op`* vAA, type@BBBB<br>*`op`* vAA, field@BBBB<br>*`op`* vAA, method_handle@BBBB<br>*`op`* vAA, proto@BBBB<br>*`op`* vAA, string@BBBB                                                                                                                                                                                                                                 | `check-cast`<br>`sget-object`<br>`const-method-handle`<br>`const-method-type`<br>`const-string` |
| AA\|*op* CC\|BB                                        | 23x  | *`op`* vAA, vBB, vCC                                                                                                                                                                                                                                                                                                                                                   |                                                                                                 |
|                                                        | 22b  | *`op`* vAA, vBB, #+CC                                                                                                                                                                                                                                                                                                                                                  |                                                                                                 |
| B\|A\|*op* CCCC                                        | 22t  | *`op`* vA, vB, +CCCC                                                                                                                                                                                                                                                                                                                                                   |                                                                                                 |
|                                                        | 22s  | *`op`* vA, vB, #+CCCC                                                                                                                                                                                                                                                                                                                                                  |                                                                                                 |
|                                                        | 22c  | *`op`* vA, vB, type@CCCC<br>*`op`* vA, vB, field@CCCC                                                                                                                                                                                                                                                                                                                  | `instance-of`<br>`iget-object`                                                                  |
|                                                        | 22cs | *`op`* vA, vB, fieldoff@CCCC                                                                                                                                                                                                                                                                                                                                           | `iget-quick`, `iput-quick`                                                                      |
| G\|B\|*op* F\|E\|D\|C                                  | 25x  | *[`B=4`] `op`* vC, {vD, vE, vF, vG}<br>*[`B=3`] `op`* vC, {vD, vE, vF}<br>*[`B=2`] `op`* vC, {vD, vE}<br>*[`B=1`] `op`* vC, {vD}<br>*[`B=0`] `op`* vC, {}                                                                                                                                                                                                              | `invoke-lambda`                                                                                 |
| ØØ\|*op* AAAA<sub>lo</sub> AAAA<sub>hi</sub>           | 30t  | *`op`* +AAAAAAAA                                                                                                                                                                                                                                                                                                                                                       | `goto/32`                                                                                       |
| ØØ\|*op* AAAA BBBB                                     | 32x  | *`op`* vAAAA, vBBBB                                                                                                                                                                                                                                                                                                                                                    |                                                                                                 |
| AA\|*op* BBBB<sub>lo</sub> BBBB<sub>hi</sub>           | 31i  | *`op`* vAA, #+BBBBBBBB                                                                                                                                                                                                                                                                                                                                                 |                                                                                                 |
|                                                        | 31t  | *`op`* vAA, +BBBBBBBB                                                                                                                                                                                                                                                                                                                                                  |                                                                                                 |
|                                                        | 31c  | *`op`* vAA, string@BBBBBBBB                                                                                                                                                                                                                                                                                                                                            | `const-string/jumbo`                                                                            |
| NN\|ff BB\|AA CCCC                                     | 32s  | *`exop`* vAA, vBB, #+CCCC                                                                                                                                                                                                                                                                                                                                              | *(unused)*                                                                                      |
|                                                        | 33x  | *`exop`* vAA, vBB, vCCCC                                                                                                                                                                                                                                                                                                                                               | *(unused)*                                                                                      |
| AA\|*op* BBBB F\|E\|D\|C                               | 34c  | *[`A=4`] `op`* {vC, vD, vE, vF}, meth@BBBB<br>*[`A=4`] `op`* {vC, vD, vE, vF}, type@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, *`kind`*@BBBB<br>*[`A=2`] `op`* {vC, vD}, *`kind`*@BBBB<br>*[`A=1`] `op`* {vC}, *`kind`*@BBBB<br>*[`A=0`] `op`* {}, *`kind`*@BBBB                                                                                                             | `invoke-virtual`<br>`filled-new-array`                                                          |
|                                                        | 34mi | *[`A=4`] `op`* {vC, vD, vE, vF}, inline@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, inline@BBBB<br>*[`A=2`] `op`* {vC, vD}, inline@BBBB<br>*[`A=1`] `op`* {vC}, inline@BBBB<br>*[`A=0`] `op`* {}, inline@BBBB                                                                                                                                                                 | `execute-inline`                                                                                |
|                                                        | 34ms | *[`A=4`] `op`* {vC, vD, vE, vF}, vtaboff@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, vtaboff@BBBB<br>*[`A=2`] `op`* {vC, vD}, vtaboff@BBBB<br>*[`A=1`] `op`* {vC}, vtaboff@BBBB                                                                                                                                                                                               | `invoke-virtual-quick`, `invoke-super-quick`                                                    |
| A\|G\|*op* BBBB F\|E\|D\|C                             | 35c  | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, meth@BBBB<br>*[`A=5`] `op`* {vC, vD, vE, vF, vG}, site@BBBB<br>*[`A=5`] `op`* {vC, vD, vE, vF, vG}, type@BBBB<br>*[`A=4`] `op`* {vC, vD, vE, vF}, *`kind`*@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, *`kind`*@BBBB<br>*[`A=2`] `op`* {vC, vD}, *`kind`*@BBBB<br>*[`A=1`] `op`* {vC}, *`kind`*@BBBB<br>*[`A=0`] `op`* {}, *`kind`*@BBBB | `invoke-virtual`<br>`invoke-custom`<br>`filled-new-array`                                       |
|                                                        | 35ms | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, vtaboff@BBBB<br>*[`A=4`] `op`* {vC, vD, vE, vF}, vtaboff@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, vtaboff@BBBB<br>*[`A=2`] `op`* {vC, vD}, vtaboff@BBBB<br>*[`A=1`] `op`* {vC}, vtaboff@BBBB                                                                                                                                          | `invoke-virtual-quick`, `invoke-super-quick`                                                    |
|                                                        | 35mi | *[`A=4`] `op`* {vC, vD, vE, vF}, inline@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, inline@BBBB<br>*[`A=2`] `op`* {vC, vD}, inline@BBBB<br>*[`A=1`] `op`* {vC}, inline@BBBB<br>*[`A=0`] `op`* {}, inline@BBBB                                                                                                                                                                 | `execute-inline`                                                                                |
|                                                        | 35fs | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, iface@BBBB<br>*[`A=4`] `op`* {vC, vD, vE, vF}, iface@BBBB<br>*[`A=3`] `op`* {vC, vD, vE}, iface@BBBB<br>*[`A=2`] `op`* {vC, vD}, iface@BBBB<br>*[`A=1`] `op`* {vC}, iface@BBBB                                                                                                                                                    | *(unused)*                                                                                      |
| AA\|*op* BBBB CCCC                                     | 3rc  | *`op`* {vCCCC .. vNNNN}, meth@BBBB<br>*`op`* {vCCCC .. vNNNN}, site@BBBB<br>*`op`* {vCCCC .. vNNNN}, type@BBBB<br>*where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                         | `invoke-virtual/range`<br>`invoke-custom/range`<br>`filled-new-array/range`                     |
|                                                        | 3rms | *`op`* {vCCCC .. vNNNN}, vtaboff@BBBB<br>*where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                  | `invoke-virtual-quick/range`, `invoke-super-quick/range`                                        |
|                                                        | 3rmi | *`op`* {vCCCC .. vNNNN}, inline@BBBB<br>*where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                   | `execute-inline/range`                                                                          |
|                                                        | 3rfs | *`op`* {vCCCC .. vNNNN}, iface@BBBB<br>*where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                    | *(unused)*                                                                                      |
| NN\|ff BBBB<sub>lo</sub> BBBB<sub>hi</sub> AAAA        | 40sc | *`exop`* AAAA, kind@BBBBBBBB                                                                                                                                                                                                                                                                                                                                           | `throw-verification-error/jumbo`                                                                |
|                                                        | 41c  | *`exop`* vAAAA, type@BBBBBBBB<br>*`exop`* vAAAA, field@BBBBBBBB                                                                                                                                                                                                                                                                                                        | `const-class/jumbo`<br>`sget-object/jumbo`                                                      |
| NN\|ff CCCC<sub>lo</sub> CCCC<sub>hi</sub> AAAA BBBB   | 52c  | *`exop`* vAAAA, vBBBB, type@CCCCCCCC<br>*`exop`* vAAAA, vBBBB, field@CCCCCCCC                                                                                                                                                                                                                                                                                          | `instance-of/jumbo`<br>`iget-object/jumbo`                                                      |
| NN\|ff BBBB<sub>lo</sub> BBBB<sub>hi</sub> AAAA CCCC   | 5rc  | *`exop`* {vCCCC .. vNNNN}, meth@BBBBBBBB<br>*`exop`* {vCCCC .. vNNNN}, type@BBBBBBBB<br>*where `NNNN = CCCC+AAAA-1`, that is `A` determines the count `0..65535`, and `C` determines the first register*                                                                                                                                                               | `invoke-virtual/jumbo`<br>`filled-new-array/jumbo`                                              |
| A\|G\|*op* BBBB F\|E\|D\|C HHHH                        | 45cc | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, meth@BBBB, proto@HHHH<br>*[`A=4`] `op`* {vC, vD, vE, vF}, meth@BBBB, proto@HHHH<br>*[`A=3`] `op`* {vC, vD, vE}, meth@BBBB, proto@HHHH<br>*[`A=2`] `op`* {vC, vD}, meth@BBBB, proto@HHHH<br>*[`A=1`] `op`* {vC}, meth@BBBB, proto@HHHH                                                                                             | `invoke-polymorphic`                                                                            |
| AA\|*op* BBBB CCCC HHHH                                | 4rcc | *`op`* {vCCCC .. vNNNN}, meth@BBBB, proto@HHHH<br>*where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                         | `invoke-polymorphic/range`                                                                      |
| AA\|*op* BBBB<sub>lo</sub> BBBB BBBB BBBB<sub>hi</sub> | 51l  | *`op`* vAA, #+BBBBBBBBBBBBBBBB                                                                                                                                                                                                                                                                                                                                         | `const-wide`                                                                                    |

## Format 10x (All API)

`ØØ|op` – one code unit: the opcode alone, with every other bit zero.

## Format 12x (All API)

`B|A|op` – one code unit: the opcode plus two 4-bit register fields, `A` and `B`, each in the range
`v0`–`v15`.

## Format 11n (All API)

`B|A|op` – one code unit: a 4-bit register field `A` (`v0`–`v15`) and a 4-bit signed literal `B`,
sign-extended to 32 bits.

## Format 11p (API m3, m5)

`B|A|op` – one code unit: a 4-bit register field `A` (`v0`–`v15`) and a 4-bit index `B` into a fixed
table of sixteen predefined constants. The table holds each value in 32-bit (IEEE 754 single)
and 64-bit (IEEE 754 double) form:

| Index | 32-bit bits  | 64-bit bits          | Value  |
|-------|--------------|----------------------|--------|
| 0     | `0xc0000000` | `0xc000000000000000` | -2.0   |
| 1     | `0xbf800000` | `0xbff0000000000000` | -1.0   |
| 2     | `0xbf000000` | `0xbfe0000000000000` | -0.5   |
| 3     | `0xbe800000` | `0xbfd0000000000000` | -0.25  |
| 4     | `0xbdcccccd` | `0xbfb999999999999a` | -0.1   |
| 5     | `0x3dcccccd` | `0x3fb999999999999a` | 0.1    |
| 6     | `0x3e800000` | `0x3fc0000000000000` | 0.25   |
| 7     | `0x3f000000` | `0x3fd0000000000000` | 0.5    |
| 8     | `0x3f800000` | `0x3fe0000000000000` | 1.0    |
| 9     | `0x40000000` | `0x4000000000000000` | 2.0    |
| 10    | `0x40400000` | `0x4008000000000000` | 3.0    |
| 11    | `0x40800000` | `0x4010000000000000` | 4.0    |
| 12    | `0x40a00000` | `0x4014000000000000` | 5.0    |
| 13    | `0x41200000` | `0x4024000000000000` | 10.0   |
| 14    | `0x42c80000` | `0x4059000000000000` | 100.0  |
| 15    | `0x447a0000` | `0x408f400000000000` | 1000.0 |

## Format 11x (All API)

`AA|op` – one code unit: the opcode plus one 8-bit register field `A` (`v0`–`v255`).

## Format 10t (All API)

`AA|op` – one code unit: the opcode plus an 8-bit signed branch offset in code units, relative to
the address of the instruction. The offset must not be `0`.

## Format 20t (API 1+)

`ØØ|op` `AAAA` – two code units: the opcode and a 16-bit signed branch offset in code units,
relative to the address of the instruction. The offset must not be `0`.

## Format 20t (API m3, m5)

`AA`<sub>hi</sub>`|op` `AAAA`<sub>lo</sub> – two code units: the high eight bits of a 24-bit signed
branch offset in the high byte of the first unit alongside the opcode, and the offset's low 16 bits
in the second unit. The offset is in code units, relative to the address of the instruction, and
must fit in 24 signed bits.

Dex `009` / `013` only form of 20t.

## Format 20bc (Dalvik-only, API 5–20)

`AA|op` `BBBB` – two code units: a 6-bit verification-failure code and a 2-bit reference kind in
`AA` (failure code in bits 0–5, kind in bits 6–7: `0` class, `1` field, `2` method), then the 16-bit
index of the reference whose handling failed.

## Format 22x (All API)

`AA|op` `BBBB` – two code units: an 8-bit register field `A` (`v0`–`v255`) and a 16-bit register
field `B` (`v0`–`v65535`).

## Format 21t (All API)

`AA|op` `BBBB` – two code units: an 8-bit register field (`v0`–`v255`) and a 16-bit signed branch
offset. The offset must not be `0`.

## Format 21s (All API)

`AA|op` `BBBB` – two code units: an 8-bit register field and a 16-bit signed literal, sign-extended
to 32 bits.

## Format 21h (API 1+)

`AA|op` `BBBB` – two code units: an 8-bit register field and the high-order 16 signed bits of a
literal whose low-order 16 bits (32-bit value) or 48 bits (64-bit value) are all `0`.

## Format 21c (All API)

`AA|op` `BBBB` – two code units: an 8-bit register field and a 16-bit constant pool index `B`.

## Format 23x (All API)

`AA|op` `CC|BB` – two code units: an 8-bit register field, then two more 8-bit register fields in
the second unit (`B` in its low byte, `C` in its high byte). All three are in `v0`–`v255`.

## Format 22b (All API)

`AA|op` `CC|BB` – two code units: an 8-bit destination register, then an 8-bit source register in
the low byte of the second unit and an 8-bit signed literal in its high byte.

## Format 22t (All API)

`B|A|op` `CCCC` – two code units: two 4-bit register fields (`v0`–`v15`) and a 16-bit signed branch
offset. The offset must not be `0`.

## Format 22s (All API)

`B|A|op` `CCCC` – two code units: two 4-bit register fields and a 16-bit signed literal.

## Format 22c (All API)

`B|A|op` `CCCC` – two code units: two 4-bit register fields and a 16-bit constant pool index `C`.

## Format 22cs (API m3, m5, 1–30)

`B|A|op` `CCCC` – the `22c` layout with `CCCC` holding the byte offset of an instance field from the
start of the object instead of a field index. The offset must fit 16 bits.

## Format 25x (API 24–25)

`G|B|op` `F|E|D|C` – two code units: a 4-bit argument-word count `B` (`0`–`4`), the closure register
`C` and up to four argument registers `D` .. `G`, all in `v0`–`v15`. The closure occupies a register
pair and is not counted by `B`.

## Format 30t (API 1+)

`ØØ|op` `AAAA`<sub>lo</sub> `AAAA`<sub>hi</sub> – three code units: the opcode and a 32-bit signed
branch offset, low-order unit first.

## Format 32x (All API)

`ØØ|op` `AAAA` `BBBB` – three code units: the opcode and two 16-bit register fields (`v0`–`v65535`).

## Format 31i (All API)

`AA|op` `BBBB`<sub>lo</sub> `BBBB`<sub>hi</sub> – three code units: an 8-bit register field and a
32-bit literal, low-order unit first.

## Format 31t (API 1+)

`AA|op` `BBBB`<sub>lo</sub> `BBBB`<sub>hi</sub> – three code units: an 8-bit register field and a
32-bit signed branch offset, low-order unit first.

## Format 31c (API 1+)

`AA|op` `BBBB`<sub>lo</sub> `BBBB`<sub>hi</sub> – three code units: an 8-bit register field and a
32-bit constant pool index `B`, low-order unit first.

## Format 32s (API 11–15)

`NN|ff` `BB|AA` `CCCC` – three code units: the `exop` field, then two 8-bit registers in the second
unit (`A` in its low byte, `B` in its high byte, both `v0`–`v255`) and a signed 16-bit literal `C`.

No instruction uses this format.

## Format 33x (API 11–15)

`NN|ff` `BB|AA` `CCCC` – three code units: the `exop` field, then two 8-bit registers in the second
unit (`A` in its low byte, `B` in its high byte, both `v0`–`v255`) and a 16-bit register `C`
(`v0`–`v65535`).

No instruction uses this format.

## Format 34c (API m3, m5)

`AA|op` `BBBB` `F|E|D|C` – three code units: an 8-bit argument-word count `A` (`0`–`4`), 16-bit
constant pool index `B`, and four 4-bit argument registers `C` .. `F` (`v0`–`v15`, `C` in the lowest
nibble). A wide argument takes two consecutive registers, so the count is the number of argument
words; no more than four argument words fit.

## Format 34mi (API m3, m5)

The `34c` layout with `BBBB` indexing the inline-method table; `A` counts at most four argument
words.

## Format 34ms (API m3, m5)

The `34c` layout with `BBBB` holding a vtable index.

## Format 35c (API 1+)

`A|G|op` `BBBB` `F|E|D|C` – three code units: a 4-bit argument-word count `A` (`0`–`5`), a 16-bit
constant pool index `B`, and the argument registers in `C` .. `G`
(`v0`–`v15`, `C` in the lowest nibble). A wide argument takes two consecutive registers, so the
count is the number of argument words.

## Format 35ms (API 1–30)

The `35c` layout with `BBBB` holding a vtable index.

## Format 35mi (Dalvik-only, API 1–20)

The `35c` layout with `BBBB` indexing the inline-method table; `A` counts at most four argument
words.

## Format 35fs (API 1–10)

The `35c` layout with `BBBB` indexing statically linked interface constants.

No instruction uses this format.

## Format 3rc (All API)

`AA|op` `BBBB` `CCCC` – three code units: an 8-bit argument-word count `A` (`0`–`255`), a 16-bit
constant pool index `B` and the 16-bit first argument register `C` (`v0`–`v65535`); the arguments
occupy the consecutive registers `C` .. `C+A-1`.

## Format 3rms (API m3, m5, 1–30)

The `3rc` layout with `BBBB` holding a vtable index.

## Format 3rmi (Dalvik-only, API 8–20)

The `3rc` layout with `BBBB` indexing the inline-method table; `A` counts at most four argument
words.

## Format 3rfs (API 1–10)

The `3rc` layout with `BBBB` indexing statically linked interface constants.

No instruction uses this format.

## Format 40sc (API 14–15)

`NN|ff` `BBBB`<sub>lo</sub> `BBBB`<sub>hi</sub> `AAAA` – four code units: the `exop` field, a 32-bit
reference index in code units 1–2 (low-order unit first), then a 16-bit field holding the 6-bit
failure code and 2-bit reference kind of format `20bc` in its low byte, with its high byte zero.

## Format 41c (API 11–15)

`NN|ff` `BBBB`<sub>lo</sub> `BBBB`<sub>hi</sub> `AAAA` – four code units: the `exop` field, a 32-bit
constant pool index `B`, low-order unit first and a 16-bit register field `AAAA`
(`v0`–`v65535`).

Used only by the expanded instructions of dalvik API 14–15.

## Format 52c (API 11–15)

`NN|ff` `CCCC`<sub>lo</sub> `CCCC`<sub>hi</sub> `AAAA` `BBBB` – five code units: the `exop` field, a
32-bit constant pool index `C`, low-order unit first, then the register fields `AAAA` (`v0`–
`v65535`) and
`BBBB` (`v0`–`v65535`).

Used only by the expanded instructions of dalvik API 14–15.

## Format 5rc (API 11–15)

`NN|ff` `BBBB`<sub>lo</sub> `BBBB`<sub>hi</sub> `AAAA` `CCCC` – five code units: the `exop` field, a
32-bit constant pool index `B`, low-order unit first, a 16-bit argument-word count `AAAA` and the
16-bit first argument register `CCCC` (`v0`–`v65535`); the arguments occupy `CCCC` .. `CCCC+AAAA-1`.

Used only by the expanded instructions of dalvik API 14–15.

## Format 45cc (API 26+)

`A|G|op` `BBBB` `F|E|D|C` `HHHH` – four code units: a 4-bit argument-word count, a 16-bit method
index, the argument registers `C` .. `G` and a 16-bit prototype index.

## Format 4rcc (API 26+)

`AA|op` `BBBB` `CCCC` `HHHH` – four code units: an 8-bit argument-word count, a 16-bit method index,
the 16-bit first argument register and a 16-bit prototype index; the arguments occupy
`CCCC` .. `CCCC+AA-1`.

## Format 51l (All API)

`AA|op` `BBBB`<sub>lo</sub> `BBBB` `BBBB` `BBBB`<sub>hi</sub> – five code units: an 8-bit register
field and a 64-bit literal, low-order unit first.

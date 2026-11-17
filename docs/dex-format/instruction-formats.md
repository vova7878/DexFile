# Dalvik executable instruction formats

This page lists the instruction formats used by the Dalvik executable (DEX) format and Dalvik
bytecode. It is meant to be used in conjunction with the
[bytecode reference document](https://source.android.com/docs/core/runtime/dalvik-bytecode).

## Bitwise descriptions

The first column in the format table lists the bitwise layout of the format. It consists of one or
more space-separated "words" each of which describes a 16-bit code unit. Each character in a word
represents four bits, read from high bits to low, with vertical bars ("`|`") interspersed to aid in
reading. Uppercase letters in sequence from "`A`" are used to indicate fields within the format (which then get defined further by the syntax column). The term "`op`" is used to indicate the
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
Executable format might be implemented which themselves use the prefix "`r`" for their registers. (That is, this decision makes it possible to talk about both virtual and real registers together
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
index), "`method_handle`" (method handles list index), "`iface`" (interface constants in a
statically linked file), and "`site`" (call site index).

Similar to the representation of constant pool indices, there are also suggested (optional) forms
that indicate prelinked offsets or indices. There are three types of suggested prelinked value:
vtable offsets (indicated as "`vtaboff`"), field offsets (indicated as "`fieldoff`"), and
inline-method-table indices (indicated as "`inline`").

In the cases where a format value isn't explicitly part of the syntax but instead picks a variant,
each variant is listed with the prefix "`[`*`X`*`=`*`N`*`]`" (e.g., "`[A=2]`") to indicate the
correspondence.

## Formats

| Format                               | ID   | Syntax                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Notable Opcodes Covered                                                                                                     |
|--------------------------------------|------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| ØØ\|*op*                             | 10x  | *`op`*                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |                                                                                                                             |
| B\|A\|*op*                           | 12x  | *`op`* vA, vB                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |                                                                                                                             |
|                                      | 11n  | *`op`* vA, #+B                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |                                                                                                                             |
|                                      | 11p  | *`op`* vA, #B                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | `const/special` `const-wide/special`                                                                                        |
| AA\|*op*                             | 11x  | *`op`* vAA                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |                                                                                                                             |
|                                      | 10t  | *`op`* +AA                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | goto                                                                                                                        |
| ØØ\|*op* AAAA                        | 20t  | *`op`* +AAAA                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | goto/16                                                                                                                     |
| AA~hi~\|*op* AAAA~lo~                | 20t  | *`op`* +AAAAAAAA                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | `goto/24` (dex `009` / `013`)                                                                                               |
| AA\|*op* BBBB                        | 20bc | *`op`* AA, kind@BBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | throw-verification-error                                                                                                    |
| AA\|*op* BBBB                        | 22x  | *`op`* vAA, vBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |                                                                                                                             |
|                                      | 21t  | *`op`* vAA, +BBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |                                                                                                                             |
|                                      | 21s  | *`op`* vAA, #+BBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |                                                                                                                             |
|                                      | 21h  | *`op`* vAA, #+BBBB0000 *`op`* vAA, #+BBBB000000000000                                                                                                                                                                                                                                                                                                                                                                                                                                      |                                                                                                                             |
|                                      | 21c  | *`op`* vAA, type@BBBB *`op`* vAA, field@BBBB *`op`* vAA, meth@BBBB *`op`* vAA, method_handle@BBBB *`op`* vAA, proto@BBBB *`op`* vAA, string@BBBB                                                                                                                                                                                                                                                                                                                                           | check-cast const-class const-method-handle const-method-type const-string                                                   |
| AA\|*op* CC\|BB                      | 23x  | *`op`* vAA, vBB, vCC                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                             |
|                                      | 22b  | *`op`* vAA, vBB, #+CC                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |                                                                                                                             |
| B\|A\|*op* CCCC                      | 22t  | *`op`* vA, vB, +CCCC                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |                                                                                                                             |
|                                      | 22s  | *`op`* vA, vB, #+CCCC                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |                                                                                                                             |
|                                      | 22c  | *`op`* vA, vB, type@CCCC *`op`* vA, vB, field@CCCC                                                                                                                                                                                                                                                                                                                                                                                                                                         | instance-of                                                                                                                 |
|                                      | 22cs | *`op`* vA, vB, fieldoff@CCCC                                                                                                                                                                                                                                                                                                                                                                                                                                                               | `iget-quick` `iput-quick`                                                                                                   |
| G\|B\|*op* F\|E\|D\|C                | 25x  | *`op`* vC, {vD, vE, vF, vG}                                                                                                                                                                                                                                                                                                                                                                                                                                                                | invoke-lambda                                                                                                               |
| ØØ\|*op* AAAA~lo~ AAAA~hi~           | 30t  | *`op`* +AAAAAAAA                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | goto/32                                                                                                                     |
| ØØ\|*op* AAAA BBBB                   | 32x  | *`op`* vAAAA, vBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |                                                                                                                             |
| AA\|*op* BBBB~lo~ BBBB~hi~           | 31i  | *`op`* vAA, #+BBBBBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |                                                                                                                             |
|                                      | 31t  | *`op`* vAA, +BBBBBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |                                                                                                                             |
|                                      | 31c  | *`op`* vAA, string@BBBBBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                | const-string/jumbo                                                                                                          |
| NN\|ff BB\|AA CCCC                   | 32s  | *`op`* vAA, vBB, #+CCCC                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | *(unused)*                                                                                                                  |
|                                      | 33x  | *`op`* vAA, vBB, vCCCC                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | *(unused)*                                                                                                                  |
| AA\|*op* BBBB F\|E\|D\|C             | 34c  | *[`A=4`] `op`* {vC, vD, vE, vF}, meth@BBBB *[`A=4`] `op`* {vC, vD, vE, vF}, type@BBBB *[`A=3`] `op`* {vC, vD, vE}, *`kind`*@BBBB *[`A=2`] `op`* {vC, vD}, *`kind`*@BBBB *[`A=1`] `op`* {vC}, *`kind`*@BBBB *[`A=0`] `op`* {}, *`kind`*@BBBB                                                                                                                                                                                                                                                | `invoke-virtual` `invoke-super` `invoke-direct` `invoke-static` `invoke-interface` `invoke-direct-empty` `filled-new-array` |
|                                      | 34mi | *[`A=4`] `op`* {vC, vD, vE, vF}, inline@BBBB *[`A=3`] `op`* {vC, vD, vE}, inline@BBBB *[`A=2`] `op`* {vC, vD}, inline@BBBB *[`A=1`] `op`* {vC}, inline@BBBB *[`A=0`] `op`* {}, inline@BBBB                                                                                                                                                                                                                                                                                                 | `execute-inline`                                                                                                            |
|                                      | 34ms | *[`A=4`] `op`* {vC, vD, vE, vF}, vtaboff@BBBB *[`A=3`] `op`* {vC, vD, vE}, vtaboff@BBBB *[`A=2`] `op`* {vC, vD}, vtaboff@BBBB *[`A=1`] `op`* {vC}, vtaboff@BBBB *[`A=0`] `op`* {}, vtaboff@BBBB                                                                                                                                                                                                                                                                                            | `invoke-virtual-quick` `invoke-super-quick`                                                                                 |
| A\|G\|*op* BBBB F\|E\|D\|C           | 35c  | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, meth@BBBB *[`A=5`] `op`* {vC, vD, vE, vF, vG}, site@BBBB *[`A=5`] `op`* {vC, vD, vE, vF, vG}, type@BBBB *[`A=4`] `op`* {vC, vD, vE, vF}, *`kind`*@BBBB *[`A=3`] `op`* {vC, vD, vE}, *`kind`*@BBBB *[`A=2`] `op`* {vC, vD}, *`kind`*@BBBB *[`A=1`] `op`* {vC}, *`kind`*@BBBB *[`A=0`] `op`* {}, *`kind`*@BBBB *The unusual choice in lettering here reflects a desire to make the count and the reference index have the same label as in format 3rc.* |                                                                                                                             |
|                                      | 35ms | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, vtaboff@BBBB *[`A=4`] `op`* {vC, vD, vE, vF}, vtaboff@BBBB *[`A=3`] `op`* {vC, vD, vE}, vtaboff@BBBB *[`A=2`] `op`* {vC, vD}, vtaboff@BBBB *[`A=1`] `op`* {vC}, vtaboff@BBBB *The unusual choice in lettering here reflects a desire to make the count and the reference index have the same label as in format 3rms.*                                                                                                                                | `invoke-virtual-quick` `invoke-super-quick`                                                                                 |
|                                      | 35mi | *[`A=4`] `op`* {vC, vD, vE, vF}, inline@BBBB *[`A=3`] `op`* {vC, vD, vE}, inline@BBBB *[`A=2`] `op`* {vC, vD}, inline@BBBB *[`A=1`] `op`* {vC}, inline@BBBB *The unusual choice in lettering here reflects a desire to make the count and the reference index have the same label as in format 3rmi.*                                                                                                                                                                                      | `execute-inline`                                                                                                            |
|                                      | 35fs | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, iface@BBBB *[`A=4`] `op`* {vC, vD, vE, vF}, iface@BBBB *[`A=3`] `op`* {vC, vD, vE}, iface@BBBB *[`A=2`] `op`* {vC, vD}, iface@BBBB *[`A=1`] `op`* {vC}, iface@BBBB                                                                                                                                                                                                                                                                                    | *(unused)*                                                                                                                  |
| AA\|*op* BBBB CCCC                   | 3rc  | *`op`* {vCCCC .. vNNNN}, meth@BBBB *`op`* {vCCCC .. vNNNN}, site@BBBB *`op`* {vCCCC .. vNNNN}, type@BBBB *where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                                                                      |                                                                                                                             |
|                                      | 3rms | *`op`* {vCCCC .. vNNNN}, vtaboff@BBBB *where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                                                                                                                                         | `invoke-virtual-quick/range` `invoke-super-quick/range`                                                                     |
|                                      | 3rmi | *`op`* {vCCCC .. vNNNN}, inline@BBBB *where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                                                                                                                                          | `execute-inline/range`                                                                                                      |
|                                      | 3rfs | *`op`* {vCCCC .. vNNNN}, iface@BBBB *where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                                                                                                                                           | *(unused)*                                                                                                                  |
| NN\|ff BBBB~lo~ BBBB~hi~ AAAA        | 40sc | *`op`* AAAA, kind@BBBBBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | `throw-verification-error/jumbo`                                                                                            |
|                                      | 41c  | *`op`* vAAAA, *`kind`*@BBBBBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                            | `const-class/jumbo` `check-cast/jumbo` `new-instance/jumbo` `sget`/`sput` `/jumbo`                                          |
|                                      | 52c  | *`op`* vAAAA, vBBBB, *`kind`*@CCCCCCCC                                                                                                                                                                                                                                                                                                                                                                                                                                                     | `instance-of/jumbo` `new-array/jumbo` `iget`/`iput` `/jumbo`                                                                |
|                                      | 5rc  | *`op`* {vCCCC .. vNNNN}, *`kind`*@BBBBBBBB *where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                                                                                                                                    | `filled-new-array/jumbo` `invoke-*` `/jumbo` `invoke-object-init/jumbo`                                                     |
| A\|G\|*op* BBBB F\|E\|D\|C HHHH      | 45cc | *[`A=5`] `op`* {vC, vD, vE, vF, vG}, meth@BBBB, proto@HHHH *[`A=4`] `op`* {vC, vD, vE, vF}, meth@BBBB, proto@HHHH *[`A=3`] `op`* {vC, vD, vE}, meth@BBBB, proto@HHHH *[`A=2`] `op`* {vC, vD}, meth@BBBB, proto@HHHH *[`A=1`] `op`* {vC}, meth@BBBB, proto@HHHH                                                                                                                                                                                                                             | invoke-polymorphic                                                                                                          |
| AA\|*op* BBBB CCCC HHHH              | 4rcc | *`op`* {vCCCC .. vNNNN}, meth@BBBB, proto@HHHH *where `NNNN = CCCC+AA-1`, that is `A` determines the count `0..255`, and `C` determines the first register*                                                                                                                                                                                                                                                                                                                                | invoke-polymorphic/range                                                                                                    |
| AA\|*op* BBBB~lo~ BBBB BBBB BBBB~hi~ | 51l  | *`op`* vAA, #+BBBBBBBBBBBBBBBB                                                                                                                                                                                                                                                                                                                                                                                                                                                             | const-wide                                                                                                                  |

## Format 10x

`ØØ|op` — one code unit: the opcode alone, with every other bit zero.

Instructions: `nop`, `return-void`, the dalvik `return-void-barrier` and the ART
`return-void-barrier` / `return-void-no-barrier`.

## Format 12x

`B|A|op` — one code unit: the opcode plus two 4-bit register fields, `A` and `B`, each in the range
`v0`--`v15`.

Instructions: `move`, `move-wide`, `move-object`, `array-length`, the 21 unary operations (`neg-int` .. `int-to-short`) and the 32 `/2addr` binary operations.

Dex `009` / `013`: `new-array-boolean`, `new-array-byte`, `new-array-char`, `new-array-short`,
`new-array-int`, `new-array-long`, `new-array-float`, `new-array-double`.

## Format 11n

`B|A|op` — one code unit: a 4-bit register field `A` (`v0`--`v15`) and a 4-bit signed literal `B`,
sign-extended to 32 bits.

Instructions: `const/4`.

## Format 11p

`B|A|op` — one code unit: a 4-bit register field `A` (`v0`--`v15`) and a 4-bit index `B` into a
fixed table of sixteen predefined constants. The table holds each value in 32-bit (IEEE 754 single)
and 64-bit (IEEE 754 double) form:

| Index | 32-bit bits  | Value  | 64-bit bits          | Value  |
|-------|--------------|--------|----------------------|--------|
| 0     | `0xc0000000` | -2.0   | `0xc000000000000000` | -2.0   |
| 1     | `0xbf800000` | -1.0   | `0xbff0000000000000` | -1.0   |
| 2     | `0xbf000000` | -0.5   | `0xbfe0000000000000` | -0.5   |
| 3     | `0xbe800000` | -0.25  | `0xbfd0000000000000` | -0.25  |
| 4     | `0xbdcccccd` | -0.1   | `0xbfb999999999999a` | -0.1   |
| 5     | `0x3dcccccd` | 0.1    | `0x3fb999999999999a` | 0.1    |
| 6     | `0x3e800000` | 0.25   | `0x3fc0000000000000` | 0.25   |
| 7     | `0x3f000000` | 0.5    | `0x3fd0000000000000` | 0.5    |
| 8     | `0x3f800000` | 1.0    | `0x3fe0000000000000` | 1.0    |
| 9     | `0x40000000` | 2.0    | `0x4000000000000000` | 2.0    |
| 10    | `0x40400000` | 3.0    | `0x4008000000000000` | 3.0    |
| 11    | `0x40800000` | 4.0    | `0x4010000000000000` | 4.0    |
| 12    | `0x40a00000` | 5.0    | `0x4014000000000000` | 5.0    |
| 13    | `0x41200000` | 10.0   | `0x4024000000000000` | 10.0   |
| 14    | `0x42c80000` | 100.0  | `0x4059000000000000` | 100.0  |
| 15    | `0x447a0000` | 1000.0 | `0x408f400000000000` | 1000.0 |

Dex `009` / `013` only.

Instructions: `const/special` (loads the 32-bit entry into one register) and `const-wide/special`
(loads the 64-bit entry into a register pair).

## Format 11x

`AA|op` — one code unit: the opcode plus one 8-bit register field `A` (`v0`--`v255`).

Instructions: `move-result`, `move-result-wide`, `move-result-object`, `move-exception`, `return`,
`return-wide`, `return-object`, `monitor-enter`, `monitor-exit`, `throw`.

## Format 10t

`AA|op` — one code unit: the opcode plus an 8-bit signed branch offset in code units, relative to
the address of the instruction. The offset must not be `0`.

Instructions: `goto`.

## Format 20t

`ØØ|op` `AAAA` — two code units: the opcode and a 16-bit signed branch offset in code units,
relative to the address of the instruction. The offset must not be `0`.

Instructions: `goto/16`.

## Format 20t (dex `009` / `013`)

`AA~hi~|op` `AAAA~lo~` — two code units: the high eight bits of a 24-bit signed branch offset in the
high byte of the first unit alongside the opcode, and the offset's low 16 bits in the second unit.
The offset is in code units, relative to the address of the instruction, and must fit in 24 signed
bits.

Dex `009` / `013` only.

Instructions: `goto/24` — the only wide unconditional branch of the set.

## Format 20bc

`AA|op` `BBBB` — two code units: a 6-bit verification-failure code and a 2-bit reference kind in
`AA` (failure code in bits 0--5, kind in bits 6--7: `0` class, `1` field, `2` method), then the
16-bit index of the reference whose handling failed.

Dalvik only, from API 5.

Instructions: `throw-verification-error`.

## Format 22x

`AA|op` `BBBB` — two code units: an 8-bit register field `A` (`v0`--`v255`) and a 16-bit register
field `B` (`v0`--`v65535`).

Instructions: `move/from16`, `move-wide/from16`, `move-object/from16`, and the ART lambda
`box-lambda`.

## Format 21t

`AA|op` `BBBB` — two code units: an 8-bit register field (`v0`--`v255`) and a 16-bit signed branch
offset. The offset must not be `0`.

Instructions: `if-eqz`, `if-nez`, `if-ltz`, `if-gez`, `if-gtz`, `if-lez`.

Dex `009` / `013`: `packed-switch` and `sparse-switch` carry the 16-bit offset of their payload
table in this format.

## Format 21s

`AA|op` `BBBB` — two code units: an 8-bit register field and a 16-bit signed literal, sign-extended
to 32 bits.

Instructions: `const/16`, `const-wide/16`.

## Format 21h

`AA|op` `BBBB` — two code units: an 8-bit register field and the high-order 16 signed bits of a
literal whose low-order 16 bits (32-bit value) or 48 bits (64-bit value) are all `0`.

Instructions: `const/high16`, `const-wide/high16`.

## Format 21c

`AA|op` `BBBB` — two code units: an 8-bit register field and a 16-bit constant pool index of kind
`type`, `field`, `meth`, `method_handle`, `proto` or `string`.

Instructions: `check-cast`, `const-class`, `const-method-handle`, `const-method-type`,
`const-string`, `new-instance`, the fourteen `sget` / `sput` variants, the six dalvik volatile
`sget` / `sput` variants (`sget-volatile`, `sput-volatile`, `sget-wide-volatile`,
`sput-wide-volatile`, `sget-object-volatile`, `sput-object-volatile`) and the ART lambda
`capture-variable` / `create-lambda`.

## Format 23x

`AA|op` `CC|BB` — two code units: an 8-bit register field, then two more 8-bit register fields in
the second unit (`B` in its low byte, `C` in its high byte). All three are in `v0`--`v255`.

Instructions: the five `cmp`*`kind`* operations, the fourteen *`arrayop`* operations and the 32
three-operand binary operations.

## Format 22b

`AA|op` `CC|BB` — two code units: an 8-bit destination register, then an 8-bit source register in
the low byte of the second unit and an 8-bit signed literal in its high byte.

Instructions: the eleven `binop/lit8` variants (`add-int/lit8` .. `ushr-int/lit8`).

## Format 22t

`B|A|op` `CCCC` — two code units: two 4-bit register fields (`v0`--`v15`) and a 16-bit signed branch
offset. The offset must not be `0`.

Instructions: `if-eq`, `if-ne`, `if-lt`, `if-ge`, `if-gt`, `if-le`.

## Format 22s

`B|A|op` `CCCC` — two code units: two 4-bit register fields and a 16-bit signed literal.

Instructions: the eight `binop/lit16` variants (`add-int/lit16` .. `xor-int/lit16`, including
`rsub-int`).

## Format 22c

`B|A|op` `CCCC` — two code units: two 4-bit register fields and a 16-bit constant pool index of kind
`type` or `field`.

Instructions: `instance-of`, `new-array`, the fourteen `iget` / `iput` variants, the six dalvik
volatile instance-field variants (`iget-volatile`, `iput-volatile`, `iget-object-volatile`,
`iget-wide-volatile`, `iput-wide-volatile`, `iput-object-volatile`), the fourteen ART quickened
field accesses (`iget-quick`, `iput-quick` and their typed variants) and the ART lambda
`liberate-variable` / `unbox-lambda`.

Dex `009` / `013`: the `new-array` index refers to the array's component type, and the six quick
field accesses (`iget-quick`, `iget-wide-quick`, `iget-object-quick`, `iput-quick`,
`iput-wide-quick`, `iput-object-quick`) carry the field's byte offset in `CCCC`.

## Format 22cs

`B|A|op` `CCCC` — the `22c` layout with `CCCC` holding the byte offset of an instance field from the
start of the object instead of a field index. The offset must fit 16 bits.

Dalvik only.

Instructions: `iget-quick`, `iget-wide-quick`, `iget-object-quick`, `iput-quick`,
`iput-wide-quick`, `iput-object-quick`.

## Format 25x

`G|B|op` `F|E|D|C` — two code units: a 4-bit argument-word count `B` (`0`--`4`), the closure
register `C` and up to four argument registers `D` .. `G`, all in `v0`--`v15`. The closure occupies
a register pair and is not counted by `B`.

ART only, API 24--25.

Instructions: `invoke-lambda`.

## Format 30t

`ØØ|op` `AAAA~lo~` `AAAA~hi~` — three code units: the opcode and a 32-bit signed branch offset,
low-order unit first.

Instructions: `goto/32`.

## Format 32x

`ØØ|op` `AAAA` `BBBB` — three code units: the opcode and two 16-bit register fields (`v0`--
`v65535`).

Instructions: `move/16`, `move-wide/16`, `move-object/16`.

## Format 31i

`AA|op` `BBBB~lo~` `BBBB~hi~` — three code units: an 8-bit register field and a 32-bit literal,
low-order unit first.

Instructions: `const`, `const-wide/32`.

## Format 31t

`AA|op` `BBBB~lo~` `BBBB~hi~` — three code units: an 8-bit register field and a 32-bit signed branch
offset, low-order unit first.

Instructions: `fill-array-data`, `packed-switch`, `sparse-switch`.

## Format 31c

`AA|op` `BBBB~lo~` `BBBB~hi~` — three code units: an 8-bit register field and a 32-bit string index,
low-order unit first.

Instructions: `const-string/jumbo`.

## Format 32s

`NN|ff` `BB|AA` `CCCC` — three code units: the `exop` field, then two 8-bit registers in the second
unit (`A` in its low byte, `B` in its high byte, both `v0`--`v255`) and a signed 16-bit literal `C`.

Dalvik only, API 11--15. No instruction uses this format.

## Format 33x

`NN|ff` `BB|AA` `CCCC` — three code units: the `exop` field, then two 8-bit registers in the second
unit (`A` in its low byte, `B` in its high byte, both `v0`--`v255`) and a 16-bit register `C`
(`v0`--`v65535`).

Dalvik only, API 11--15. No instruction uses this format.

## Format 34c

`AA|op` `BBBB` `F|E|D|C` — three code units: an 8-bit argument-word count `A` (`0`--`4`), a 16-bit
method or type index `B`, and four 4-bit argument registers `C` .. `F` (`v0`--`v15`, `C` in the
lowest nibble). A wide argument takes two consecutive registers, so the count is the number of
argument words; no more than four argument words fit.

Dex `009` / `013` only.

Instructions: `invoke-virtual`, `invoke-super`, `invoke-direct`, `invoke-static`,
`invoke-interface` and `invoke-direct-empty` (method index), `filled-new-array` (type index).

## Format 34mi

The `34c` layout with `BBBB` indexing the inline-method table; `A` counts at most four argument
words.

Dex `009` / `013` only.

Instructions: `execute-inline`.

## Format 34ms

The `34c` layout with `BBBB` holding a vtable index.

Dex `009` / `013` only.

Instructions: `invoke-virtual-quick`, `invoke-super-quick`.

## Format 35c

`A|G|op` `BBBB` `F|E|D|C` — three code units: a 4-bit argument-word count `A` (`0`--`5`), a 16-bit
constant pool index `B` of kind `meth`, `site` or `type`, and the argument registers in `C` .. `G`
(`v0`--`v15`, `C` in the lowest nibble). A wide argument takes two consecutive registers, so the
count is the number of argument words.

Instructions: `invoke-virtual`, `invoke-super`, `invoke-direct`, `invoke-static`,
`invoke-interface`, `filled-new-array`, `invoke-custom`, the dalvik `invoke-direct-empty` and the
ART `invoke-virtual-quick`.

## Format 35ms

The `35c` layout with `BBBB` holding a vtable index.

Dalvik only.

Instructions: `invoke-virtual-quick`, `invoke-super-quick`.

## Format 35mi

The `35c` layout with `BBBB` indexing the inline-method table; `A` counts at most four argument
words.

Dalvik only. In API 1--10 the pair is named `3inline` / `3rinline`.

Instructions: `execute-inline`.

## Format 35fs

The `35c` layout with `BBBB` indexing statically linked interface constants.

Dalvik only, API 1--10. No instruction uses this format.

## Format 3rc

`AA|op` `BBBB` `CCCC` — three code units: an 8-bit argument-word count `A` (`0`--`255`), a 16-bit
constant pool index `B` and the 16-bit first argument register `C` (`v0`--`v65535`); the arguments
occupy the consecutive registers `C` .. `C+A-1`.

Instructions: the five `invoke-*` `/range` variants, `filled-new-array/range`,
`invoke-custom/range`, the dalvik `invoke-object-init/range` and the ART
`invoke-virtual/range-quick`.

Dex `009` / `013`: `invoke-virtual-quick/range` and `invoke-super-quick/range` carry a vtable index
in `BBBB`.

## Format 3rms

The `3rc` layout with `BBBB` holding a vtable index.

Dalvik only.

Instructions: `invoke-virtual-quick/range`, `invoke-super-quick/range`.

## Format 3rmi

The `3rc` layout with `BBBB` indexing the inline-method table.

Dalvik only, from API 8. Its API 1--10 name is `3rinline`.

Instructions: `execute-inline/range`.

## Format 3rfs

The `3rc` layout with `BBBB` indexing statically linked interface constants.

Dalvik only, API 1--10. No instruction uses this format.

## Format 40sc

`NN|ff` `BBBB~lo~` `BBBB~hi~` `AAAA` — four code units: the `exop` field, a 32-bit reference index
in code units 1--2 (low-order unit first), then a 16-bit field holding the 6-bit failure code and
2-bit reference kind of format `20bc` in its low byte, with its high byte zero.

Used only by the expanded instructions of dex version `036` (dalvik API 14--15).

Instructions: `throw-verification-error/jumbo`.

## Format 41c

`NN|ff` `BBBB~lo~` `BBBB~hi~` `AAAA` — four code units: the `exop` field, a 32-bit constant pool
index in code units 1--2 (low-order unit first) and a 16-bit register field `AAAA`
(`v0`--`v65535`).

Used only by the expanded instructions of dex version `036` (dalvik API 14--15).

Instructions: `const-class/jumbo`, `check-cast/jumbo`, `new-instance/jumbo`, the fourteen
`sget` / `sput` `/jumbo` variants and the six volatile `sget` / `sput` `/jumbo` variants.

## Format 52c

`NN|ff` `CCCC~lo~` `CCCC~hi~` `AAAA` `BBBB` — five code units: the `exop` field, a 32-bit constant
pool index in code units 1--2, then the register fields `AAAA` (`v0`--`v65535`) and `BBBB`
(`v0`--`v65535`).

Used only by the expanded instructions of dex version `036` (dalvik API 14--15).

Instructions: `instance-of/jumbo`, `new-array/jumbo`, the fourteen `iget` / `iput` `/jumbo`
variants and the six volatile `iget` / `iput` `/jumbo` variants.

## Format 5rc

`NN|ff` `BBBB~lo~` `BBBB~hi~` `AAAA` `CCCC` — five code units: the `exop` field, a 32-bit constant
pool index in code units 1--2, a 16-bit argument-word count `AAAA` and the 16-bit first argument
register `CCCC` (`v0`--`v65535`); the arguments occupy `CCCC` .. `CCCC+AAAA-1`.

Used only by the expanded instructions of dex version `036` (dalvik API 14--15).

Instructions: `filled-new-array/jumbo`, the five `invoke-*` `/jumbo` variants and the dalvik
`invoke-object-init/jumbo`.

## Format 45cc

`A|G|op` `BBBB` `F|E|D|C` `HHHH` — four code units: a 4-bit argument-word count, a 16-bit method
index, the argument registers `C` .. `G` and a 16-bit prototype index.

Instructions: `invoke-polymorphic`.

## Format 4rcc

`AA|op` `BBBB` `CCCC` `HHHH` — four code units: an 8-bit argument-word count, a 16-bit method index,
the 16-bit first argument register and a 16-bit prototype index; the arguments occupy
`CCCC` .. `CCCC+AA-1`.

Instructions: `invoke-polymorphic/range`.

## Format 51l

`AA|op` `BBBB~lo~` `BBBB` `BBBB` `BBBB~hi~` — five code units: an 8-bit register field and a 64-bit
literal, low-order unit first.

Instructions: `const-wide`.


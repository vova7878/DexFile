# Dalvik executable format

This document specifies the layout and contents of `.dex` files, which hold a set of class
definitions and their associated adjunct data.

Two container variants are specified. The standard container, defined by format versions `035` and
later, is one flat file. The compact container — compact dex, `cdex` — keeps the wire format of
every item and the sorting and uniqueness rules of the id and definition tables, and changes only
the container: it splits the file into a main section and a separately placed data section with a
different offset base for the items that live there, enlarges the header, packs the code item header
into four bytes plus an optional preheader, moves the debug info offset of a method out of its code
item into a per-method lookup table, deduplicates items and lets several files share one data
section, and redefines the checksum.

The legacy format, in versions `009` and `013`, is one flat file without a `map_list` or
`endian_tag`: the header locates the sections, and the members of a class are carried by four
uniform lists referenced from its definition.

The instruction set and its encodings are specified by the companion documents
[Dalvik bytecode](dalvik-bytecode.md) and
[Dalvik executable instruction formats](instruction-formats.md).

## Guide to types

| Name      | Description                                           |
|-----------|-------------------------------------------------------|
| byte      | 8-bit signed int                                      |
| ubyte     | 8-bit unsigned int                                    |
| short     | 16-bit signed int, little-endian                      |
| ushort    | 16-bit unsigned int, little-endian                    |
| int       | 32-bit signed int, little-endian                      |
| uint      | 32-bit unsigned int, little-endian                    |
| long      | 64-bit signed int, little-endian                      |
| ulong     | 64-bit unsigned int, little-endian                    |
| sleb128   | signed LEB128, variable-length (see below)            |
| uleb128   | unsigned LEB128, variable-length (see below)          |
| uleb128p1 | unsigned LEB128 plus `1`, variable-length (see below) |

### LEB128

LEB128 ("**L**ittle- **E**ndian **B**ase **128**") is a variable-length encoding for arbitrary
signed or unsigned integer quantities. The format was borrowed from the
[DWARF3](http://dwarfstd.org/Dwarf3Std.php) specification. In a `.dex` file, LEB128 is only ever
used to encode 32-bit quantities.

Each LEB128 encoded value consists of one to five bytes, which together represent a single 32-bit
value. Each byte has its most significant bit set except for the final byte in the sequence, which
has its most significant bit clear. The remaining seven bits of each byte are payload, with the
least significant seven bits of the quantity in the first byte, the next seven in the second byte
and so on. In the case of a signed LEB128 (`sleb128`), the most significant payload bit of the final
byte in the sequence is sign-extended to produce the final value. In the unsigned case (`uleb128`),
any bits not explicitly represented are interpreted as `0`.

| Bitwise diagram of a two-byte LEB128 value |        |        |        |        |        |        |        |             |         |         |         |         |        |        |        |
|--------------------------------------------|--------|--------|--------|--------|--------|--------|--------|-------------|---------|---------|---------|---------|--------|--------|--------|
| First byte                                 |        |        |        |        |        |        |        | Second byte |         |         |         |         |        |        |        |
| `1`                                        | bit~6~ | bit~5~ | bit~4~ | bit~3~ | bit~2~ | bit~1~ | bit~0~ | `0`         | bit~13~ | bit~12~ | bit~11~ | bit~10~ | bit~9~ | bit~8~ | bit~7~ |

The variant `uleb128p1` is used to represent a signed value, where the representation is of the
value *plus one* encoded as a `uleb128`. This makes the encoding of `-1` (alternatively thought of
as the unsigned value `0xffffffff`) --- but no other negative number --- a single byte, and is
useful in exactly those cases where the represented number must either be non-negative or `-1` (or
`0xffffffff`), and where no other negative values are allowed (or where large unsigned values are
unlikely to be needed).

Here are some examples of the formats:

| Encoded Sequence | As `sleb128` | As `uleb128` | As `uleb128p1` |
|------------------|--------------|--------------|----------------|
| 00               | 0            | 0            | -1             |
| 01               | 1            | 1            | 0              |
| 7f               | -1           | 127          | 126            |
| 80 7f            | -128         | 16256        | 16255          |

## File layout

A standard file is one flat sequence of sections. A compact file is a main section followed by a
separately placed data section; the two are specified below.

### Standard file layout

| Name           | Format               | Description                                                                                                                                                                                                                                                                                                                                                                                           |
|----------------|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| header         | header_item          | the header                                                                                                                                                                                                                                                                                                                                                                                            |
| string_ids     | string_id_item[]     | string identifiers list. These are identifiers for all the strings used by this file, either for internal naming (e.g., type descriptors) or as constant objects referred to by code. This list must be sorted by string contents, using UTF-16 code point values (not in a locale-sensitive manner), and it must not contain any duplicate entries.                                                  |
| type_ids       | type_id_item[]       | type identifiers list. These are identifiers for all types (classes, arrays, or primitive types) referred to by this file, whether defined in the file or not. This list must be sorted by `string_id` index, and it must not contain any duplicate entries.                                                                                                                                          |
| proto_ids      | proto_id_item[]      | method prototype identifiers list. These are identifiers for all prototypes referred to by this file. This list must be sorted in return-type (by `type_id` index) major order, and then by argument list (lexicographic ordering, individual arguments ordered by `type_id` index). The list must not contain any duplicate entries.                                                                 |
| field_ids      | field_id_item[]      | field identifiers list. These are identifiers for all fields referred to by this file, whether defined in the file or not. This list must be sorted, where the defining type (by `type_id` index) is the major order, field name (by `string_id` index) is the intermediate order, and type (by `type_id` index) is the minor order. The list must not contain any duplicate entries.                 |
| method_ids     | method_id_item[]     | method identifiers list. These are identifiers for all methods referred to by this file, whether defined in the file or not. This list must be sorted, where the defining type (by `type_id` index) is the major order, method name (by `string_id` index) is the intermediate order, and method prototype (by `proto_id` index) is the minor order. The list must not contain any duplicate entries. |
| class_defs     | class_def_item[]     | class definitions list. The classes must be ordered such that a given class's superclass and implemented interfaces appear in the list earlier than the referring class. Furthermore, it is invalid for a definition for the same-named class to appear more than once in the list.                                                                                                                   |
| call_site_ids  | call_site_id_item[]  | call site identifiers list. These are identifiers for all call sites referred to by this file, whether defined in the file or not. This list must be sorted in ascending order of `call_site_off`.                                                                                                                                                                                                    |
| method_handles | method_handle_item[] | method handles list. A list of all method handles referred to by this file, whether defined in the file or not. This list is not sorted and may contain duplicates which will logically correspond to different method handle instances.                                                                                                                                                              |
| data           | ubyte[]              | data area, containing all the support data for the tables listed above. Different items have different alignment requirements, and padding bytes are inserted before each item if necessary to achieve proper alignment.                                                                                                                                                                              |
| link_data      | ubyte[]              | data used in statically linked files. The format of the data in this section is left unspecified by this document. This section is empty in unlinked files, and runtime implementations may use it as they see fit.                                                                                                                                                                                   |

### Container format

Version 41, which appeared in Android 16, introduces a new container format for DEX data with the
goal to save space. This container format allows several logical DEX files to be combined into a
single physical file. The new format is mostly just naive concatenation of files in the previous
format, with some differences:

- The `file_size` is the size of the logical file, not the physical file. It can be used to iterate
  over all the logical files in the container.
- Logical dex files may reference any later data in the container (but not earlier). This allows dex
  files to share data, such as strings, between them.
- All offsets are relative to the physical file. No offset is relative to the header. This ensures
  that sections with offsets can be shared between logical files.
- The header adds two new fields to describe the bounds of the container. This is an additional
  consistency check and makes porting code to the new format easier.
- The `data_size` and `data_off` are now unused. Data can be spread across multiple logical files
  and does not have to be contiguous.

### Compact file layout

| Name           | Format               | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|----------------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| header         | compact header_item  | the compact header                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| string_ids     | string_id_item[]     | string identifiers list, sorted and unique as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                               |
| type_ids       | type_id_item[]       | type identifiers list, sorted and unique as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                                 |
| proto_ids      | proto_id_item[]      | method prototype identifiers list, sorted and unique as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                     |
| field_ids      | field_id_item[]      | field identifiers list, sorted and unique as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                                |
| method_ids     | method_id_item[]     | method identifiers list, sorted and unique as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                               |
| class_defs     | class_def_item[]     | class definitions list, ordered and unique as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                               |
| call_site_ids  | call_site_id_item[]  | call site identifiers list, sorted as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                                                       |
| method_handles | method_handle_item[] | method handles list, unsorted and possibly with duplicates, as in the standard layout                                                                                                                                                                                                                                                                                                                                                                                              |
| data           | ubyte[]              | data area, containing all the support data for the tables listed above: code items, debug info items, encoded arrays, annotation items, annotation sets, annotation set ref lists, annotation directories, type lists, class data, string data, hidden API class data, the map list, link data, and the debug info offsets table. Different items have different alignment requirements, and padding bytes are inserted before each item if necessary to achieve proper alignment. |

A compact file is exactly these two parts:

- The *main section* is the header followed by the id and definition tables, in the order above.
  `file_size` counts the main section only, never the whole file.
- The *data section* begins at `data_off` and is `data_size` bytes long. A standalone file places it
  immediately after the main section. A container may hold several compact files that share one data
  section: each `data_off` points from that file's own start into the shared region, and
  `data_size` is the part of the region the file may use, which may be smaller than the whole
  section.
- `owned_data_begin` and `owned_data_end` delimit the range of the data section that holds data
  which was not deduplicated to another file. The owned ranges of files sharing a data section are
  disjoint.
- The first 8 bytes of the data section are reserved and no item ever occupies them, so offset `0`
  denotes an absent reference. The data section ends on an 8-byte boundary.

### Legacy file layout

A version `009` or `013` file is always little-endian, and the header carries no `endian_tag`. There
is no `map_list` and no `proto_ids`, `call_site_ids`, or `method_handles` section: version `009`
locates the five identifier and definition tables through header fields and everything else through
the offsets stored in items, while version `013` additionally gives the size and the offset of each
of its four data areas. An offset of `0` means that the target is absent; an empty section has size
`0` and offset `0`. Version `009` uses no LEB128 encodings; version `013` uses LEB128 only inside
`debug_info_item` and the annotation structures. Version `009` carries no annotation structures and
no debug info item.

| Name        | Format                                                                        | Description                                                                                                                                                                                                                                                                |
|-------------|-------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| header      | legacy header_item                                                            | the header: `0x5c` (92) bytes in version `009`, `0x7c` (124) bytes in version `013`                                                                                                                                                                                        |
| string_ids  | legacy string_id_item[]                                                       | string identifiers list, 8 bytes per entry                                                                                                                                                                                                                                 |
| type_ids    | legacy class_id_item[] (version `009`); legacy type_id_item[] (version `013`) | type identifiers list, 4 bytes per entry, in arbitrary order                                                                                                                                                                                                               |
| field_ids   | legacy field_id_item[]                                                        | field identifiers list, 12 bytes per entry, sorted by defining type, name, and descriptor                                                                                                                                                                                  |
| method_ids  | legacy method_id_item[]                                                       | method identifiers list, 12 bytes per entry, sorted by defining type, name, and descriptor                                                                                                                                                                                 |
| class_defs  | legacy class_def_item[]                                                       | class definitions list, 32 bytes per entry in version `009` and 36 in version `013`; a class's superclass and implemented interfaces appear earlier than the class itself, and a class is defined at most once                                                             |
| word_data   | items                                                                         | data area, aligned to 8 bytes: type lists, the per-class member lists, exception handler lists, and code items; in version `009` also position lists and local variable lists; in version `013` also annotation directories, annotation sets, and annotation set ref lists |
| codes       | items                                                                         | data area, aligned to 4 bytes: instruction arrays                                                                                                                                                                                                                          |
| string_data | items                                                                         | data area, byte-aligned: string data, sorted by string content                                                                                                                                                                                                             |
| byte_data   | items                                                                         | data area, byte-aligned, version `013` only: debug info items and annotation items                                                                                                                                                                                         |

The sections follow in the order above, each aligned to its requirement with zero padding. The items
of `word_data`, `codes`, and `byte_data` have no prescribed order.

### Offset bases

In a standard file every offset is measured from the start of the file.

In a compact file every offset is measured from the start of the data section. The main section is
addressed by index, never by an offset: its tables are selected through the `*_size`/`*_off` pairs
of the header and their elements through indexes, so everything that an offset can reach lies in the
data section.

The only exceptions are the entries that locate the main section itself:

| Field                                                                                                                                            | Base              |
|--------------------------------------------------------------------------------------------------------------------------------------------------|-------------------|
| `header.string_ids_off`, `header.type_ids_off`, `header.proto_ids_off`, `header.field_ids_off`, `header.method_ids_off`, `header.class_defs_off` | start of the file |
| `map_item.offset` of an entry that describes the header or one of those tables                                                                   | start of the file |

`header.debug_info_offsets_table_offset` is relative to the start of the table data, not to a
section; it is defined with the table.

### Checksum and signature

In a standard file, `checksum` is the adler32 of the whole file except `magic` and `checksum`, and
`signature` is the SHA-1 of the whole file except `magic`, `checksum`, and `signature`.

In a compact file the data section placement depends on the container, so `data_off` and
`data_size` are excluded from the sum and the three ranges are summed separately:

```
sum1 = adler32(compact header with checksum, data_off, and data_size zeroed)
sum2 = adler32(rest of the main section, after the header)
sum3 = adler32(data section region)
checksum = (sum1 * 31) ^ sum2 ^ sum3
```

A compact `signature` is the SHA-1 signature of the standard file the compact file was produced
from; it is not recomputed from the compact contents.

## Bitfield, string, and constant definitions

### DEX_FILE_MAGIC

#### Embedded in header_item

The constant array/string `DEX_FILE_MAGIC` is the list of bytes that must appear at the beginning of
a `.dex` file in order for it to be recognized as such. The value intentionally contains a newline (`"\n"` or `0x0a`) and a null byte (`"\0"` or `0x00`) in order to help in the detection of certain
forms of corruption. The value also encodes a format version number as three decimal digits, which
is expected to increase monotonically over time as the format evolves.

```
ubyte[8] DEX_FILE_MAGIC = { 0x64 0x65 0x78 0x0a 0x30 0x33 0x39 0x00 }
                        = "dex\n039\0"
```

**Note:** Version `041` adds the [container format](#container-format) described under File layout.
It appeared in Android 16 and is *experimental*, so it should not be used for production code.

**Note:** Version `040` extends the set of allowed characters in *SimpleName* with space characters;
it appeared in Android 10.0.

**Note:** Version `039` adds the bytecodes `const-method-handle` and `const-method-type`; it
appeared in Android 9.0. In Android 10, version `039` extends the format with the hidden API data
that is only applicable to dex files on the boot class path.

**Note:** Version `038` adds the bytecodes `invoke-polymorphic` and `invoke-custom`, together with
the `call_site_ids` and `method_handles` sections; it appeared in Android 8.0.

**Note:** Version `037` adds default methods and adjusts the `invoke` bytecode; it appeared in
Android 7.0. Prior to version `037`, most versions of Android used version `035` of the format. The
only difference between versions `035` and `037` is the addition of default methods and the
adjustment of the `invoke` bytecode.

**Note:** Version `036` existed in Android 4.0--4.4W (API 14--20) and is specific to the dalvik
runtime; ART, shipped from Android 4.4 (API 19), rejects it. The version was planned as a public
format whose expanded opcodes allowed more than 65535 elements in some sections. In Android 4.1 (API

16) all expanded opcodes were removed, and in API 16--20 the format is completely identical to
    `035`.

**Note:** At least a couple earlier versions of the format have been used in widely available public
software releases. For example, version `009` was used for the M3 releases of the Android platform (November--December 2007), and version `013` was used for the M5 releases of the Android platform (February--March 2008). In several respects, these earlier versions of the format differ
significantly from the version described in this document; they are specified here, under "Legacy
file layout" and the legacy items below.

**Note:** Compact dex is an internal ART format that exists from Android 9 through Android 15 (API
28--35).

#### Embedded in legacy header_item

A legacy file carries one of two magic values.

```
ubyte[8] DEX_FILE_MAGIC_009 = { 0x64 0x65 0x78 0x0a 0x30 0x30 0x39 0x00 }
                          = "dex\n009\0"
ubyte[8] DEX_FILE_MAGIC_013 = { 0x64 0x65 0x78 0x0a 0x30 0x31 0x33 0x00 }
                          = "dex\n013\0"
```

### CDEX_FILE_MAGIC

#### Embedded in compact header_item

The constant array/string `CDEX_FILE_MAGIC` is the list of bytes that must appear at the beginning
of a compact dex file in order for it to be recognized as such. The value spells `cdex`, followed by
the version string `001` and a null byte. Unlike `DEX_FILE_MAGIC` it contains no newline byte, and
its version digits are independent of the version digits of a standard file.

```
ubyte[8] CDEX_FILE_MAGIC = { 0x63 0x64 0x65 0x78 0x30 0x30 0x31 0x00 }
                         = "cdex001\0"
```

### Feature flags

#### Embedded in compact header_item

Bitfields of this word describe properties that a standard file expresses through its version
number.

| Name            | Value | Meaning                                                                                                           |
|-----------------|-------|-------------------------------------------------------------------------------------------------------------------|
| kDefaultMethods | 0x1   | the file uses default (interface) methods, which a standard file expresses through format version `037` and later |

No other bits are defined.

### ENDIAN_CONSTANT and REVERSE_ENDIAN_CONSTANT

#### Embedded in header_item

The constant `ENDIAN_CONSTANT` is used to indicate the endianness of the file in which it is found.
Although the standard `.dex` format is little-endian, implementations may choose to perform
byte-swapping. Should an implementation come across a header whose `endian_tag` is
`REVERSE_ENDIAN_CONSTANT` instead of `ENDIAN_CONSTANT`, it would know that the file has been
byte-swapped from the expected form.

```
uint ENDIAN_CONSTANT = 0x12345678;
uint REVERSE_ENDIAN_CONSTANT = 0x78563412;
```

### NO_INDEX

#### Embedded in class_def_item and debug_info_item

The constant `NO_INDEX` is used to indicate that an index value is absent.

**Note:** This value isn't defined to be `0`, because that is in fact typically a valid index.

The chosen value for `NO_INDEX` is representable as a single byte in the `uleb128p1` encoding.

```
uint NO_INDEX = 0xffffffff;    // == -1 if treated as a signed int
```

### access_flags definitions

#### Embedded in class_def_item, encoded_field, encoded_method, and InnerClass

Bitfields of these flags are used to indicate the accessibility and overall properties of classes
and class members.

| Name                      | Value   | For Classes (and `InnerClass` annotations)                   | For Fields                                                  | For Methods                                                                                                                                           |
|---------------------------|---------|--------------------------------------------------------------|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| ACC_PUBLIC                | 0x1     | `public`: visible everywhere                                 | `public`: visible everywhere                                | `public`: visible everywhere                                                                                                                          |
| ACC_PRIVATE               | 0x2     | *`private`: only visible to defining class                   | `private`: only visible to defining class                   | `private`: only visible to defining class                                                                                                             |
| ACC_PROTECTED             | 0x4     | *`protected`: visible to package and subclasses              | `protected`: visible to package and subclasses              | `protected`: visible to package and subclasses                                                                                                        |
| ACC_STATIC                | 0x8     | *`static`: is not constructed with an outer `this` reference | `static`: global to defining class                          | `static`: does not take a `this` argument                                                                                                             |
| ACC_FINAL                 | 0x10    | `final`: not subclassable                                    | `final`: immutable after construction                       | `final`: not overridable                                                                                                                              |
| ACC_SYNCHRONIZED          | 0x20    |                                                              |                                                             | `synchronized`: associated lock automatically acquired around call to this method. **Note:** This is only valid to set when `ACC_NATIVE` is also set. |
| ACC_VOLATILE              | 0x40    |                                                              | `volatile`: special access rules to help with thread safety |                                                                                                                                                       |
| ACC_BRIDGE                | 0x40    |                                                              |                                                             | bridge method, added automatically by compiler as a type-safe bridge                                                                                  |
| ACC_TRANSIENT             | 0x80    |                                                              | `transient`: not to be saved by default serialization       |                                                                                                                                                       |
| ACC_VARARGS               | 0x80    |                                                              |                                                             | last argument should be treated as a "rest" argument by compiler                                                                                      |
| ACC_NATIVE                | 0x100   |                                                              |                                                             | `native`: implemented in native code                                                                                                                  |
| ACC_INTERFACE             | 0x200   | `interface`: multiply-implementable abstract class           |                                                             |                                                                                                                                                       |
| ACC_ABSTRACT              | 0x400   | `abstract`: not directly instantiable                        |                                                             | `abstract`: unimplemented by this class                                                                                                               |
| ACC_STRICT                | 0x800   |                                                              |                                                             | `strictfp`: strict rules for floating-point arithmetic                                                                                                |
| ACC_SYNTHETIC             | 0x1000  | not directly defined in source code                          | not directly defined in source code                         | not directly defined in source code                                                                                                                   |
| ACC_ANNOTATION            | 0x2000  | declared as an annotation class                              |                                                             |                                                                                                                                                       |
| ACC_ENUM                  | 0x4000  | declared as an enumerated type                               | declared as an enumerated value                             |                                                                                                                                                       |
| *(unused)*                | 0x8000  |                                                              |                                                             |                                                                                                                                                       |
| ACC_CONSTRUCTOR           | 0x10000 |                                                              |                                                             | constructor method (class or instance initializer)                                                                                                    |
| ACC_DECLARED_SYNCHRONIZED | 0x20000 |                                                              |                                                             | declared `synchronized`. **Note:** This has no effect on execution (other than in reflection of this flag, per se).                                   |

\* Only allowed on for `InnerClass` annotations, and must not ever be on in a `class_def_item`.

**Note:** In versions `009` and `013` no flag above `0x4000` is defined, and `0x20` is
`ACC_SUPER` in class definitions and `ACC_SYNCHRONIZED` in method entries.

### Modified UTF-8 encoding

As a concession to easier legacy support, the `.dex` format encodes its string data in a de facto
standard modified UTF-8 form, hereafter referred to as MUTF-8. This form is identical to standard
UTF-8, except:

- Only the one-, two-, and three-byte encodings are used.
- Code points in the range `U+10000` ... `U+10ffff` are encoded as a surrogate pair, each of which
  is represented as a three-byte encoded value.
- The code point `U+0000` is encoded in two-byte form.
- A plain null byte (value `0`) indicates the end of a string, as is the standard C language
  interpretation.

The first two items above can be summarized as: MUTF-8 is an encoding format for UTF-16, instead of
being a more direct encoding format for Unicode characters.

The final two items above make it simultaneously possible to include the code point `U+0000` in a
string *and* still manipulate it as a C-style null-terminated string.

However, the special encoding of `U+0000` means that, unlike normal UTF-8, the result of calling the
standard C function `strcmp()` on a pair of MUTF-8 strings does not always indicate the properly
signed result of comparison of *unequal* strings. When ordering (not just equality) is a concern,
the most straightforward way to compare MUTF-8 strings is to decode them character by character, and
compare the decoded values. (However, more clever implementations are also possible.)

Please refer to [The Unicode Standard](http://unicode.org/) for further information about character
encoding. MUTF-8 is actually closer to the (relatively less well-known) encoding
[CESU-8](http://www.unicode.org/reports/tr26/) than to UTF-8 per se.

### encoded_value encoding

#### Embedded in annotation_element and encoded_array_item

An `encoded_value` is an encoded piece of (nearly) arbitrary hierarchically structured data. The
encoding is meant to be both compact and straightforward to parse.

| Name                           | Format  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|--------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| (value_arg << 5) \| value_type | ubyte   | byte indicating the type of the immediately subsequent `value` along with an optional clarifying argument in the high-order three bits. See below for the various `value` definitions. In most cases, `value_arg` encodes the length of the immediately-subsequent `value` in bytes, as `(size - 1)`, e.g., `0` means that the value requires one byte, and `7` means it requires eight bytes; however, there are exceptions as noted below. |
| value                          | ubyte[] | bytes representing the value, variable in length and interpreted differently for different `value_type` bytes, though always little-endian. See the various value definitions below for details.                                                                                                                                                                                                                                             |

### Value formats

| Type Name           | `value_type` | `value_arg` Format    | `value` Format     | Description                                                                                                                                                      |
|---------------------|--------------|-----------------------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| VALUE_BYTE          | 0x00         | *(none; must be `0`)* | ubyte[1]           | signed one-byte integer value                                                                                                                                    |
| VALUE_SHORT         | 0x02         | size - 1 (0...1)      | ubyte[size]        | signed two-byte integer value, sign-extended                                                                                                                     |
| VALUE_CHAR          | 0x03         | size - 1 (0...1)      | ubyte[size]        | unsigned two-byte integer value, zero-extended                                                                                                                   |
| VALUE_INT           | 0x04         | size - 1 (0...3)      | ubyte[size]        | signed four-byte integer value, sign-extended                                                                                                                    |
| VALUE_LONG          | 0x06         | size - 1 (0...7)      | ubyte[size]        | signed eight-byte integer value, sign-extended                                                                                                                   |
| VALUE_FLOAT         | 0x10         | size - 1 (0...3)      | ubyte[size]        | four-byte bit pattern, zero-extended *to the right*, and interpreted as an IEEE754 32-bit floating point value                                                   |
| VALUE_DOUBLE        | 0x11         | size - 1 (0...7)      | ubyte[size]        | eight-byte bit pattern, zero-extended *to the right*, and interpreted as an IEEE754 64-bit floating point value                                                  |
| VALUE_METHOD_TYPE   | 0x15         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `proto_ids` section and representing a method type value                      |
| VALUE_METHOD_HANDLE | 0x16         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `method_handles` section and representing a method handle value               |
| VALUE_STRING        | 0x17         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `string_ids` section and representing a string value                          |
| VALUE_TYPE          | 0x18         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `type_ids` section and representing a reflective type/class value             |
| VALUE_FIELD         | 0x19         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `field_ids` section and representing a reflective field value                 |
| VALUE_METHOD        | 0x1a         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `method_ids` section and representing a reflective method value               |
| VALUE_ENUM          | 0x1b         | size - 1 (0...3)      | ubyte[size]        | unsigned (zero-extended) four-byte integer value, interpreted as an index into the `field_ids` section and representing the value of an enumerated type constant |
| VALUE_ARRAY         | 0x1c         | *(none; must be `0`)* | encoded_array      | an array of values, in the format specified by "`encoded_array` format" below. The size of the `value` is implicit in the encoding.                              |
| VALUE_ANNOTATION    | 0x1d         | *(none; must be `0`)* | encoded_annotation | a sub-annotation, in the format specified by "`encoded_annotation` format" below. The size of the `value` is implicit in the encoding.                           |
| VALUE_NULL          | 0x1e         | *(none; must be `0`)* | *(none)*           | `null` reference value                                                                                                                                           |
| VALUE_BOOLEAN       | 0x1f         | boolean (0...1)       | *(none)*           | one-bit value; `0` for `false` and `1` for `true`. The bit is represented in the `value_arg`.                                                                    |

### encoded_array format

| Name   | Format              | Description                                                                                                           |
|--------|---------------------|-----------------------------------------------------------------------------------------------------------------------|
| size   | uleb128             | number of elements in the array                                                                                       |
| values | encoded_value[size] | a series of `size` `encoded_value` byte sequences in the format specified by this section, concatenated sequentially. |

### encoded_annotation format

| Name     | Format                   | Description                                                                                                                                  |
|----------|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| type_idx | uleb128                  | type of the annotation. This must be a class (not array or primitive) type.                                                                  |
| size     | uleb128                  | number of name-value mappings in this annotation                                                                                             |
| elements | annotation_element[size] | elements of the annotation, represented directly in-line (not as offsets). Elements must be sorted in increasing order by `string_id` index. |

### annotation_element format

| Name     | Format        | Description                                                                                                                                 |
|----------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| name_idx | uleb128       | element name, represented as an index into the `string_ids` section. The string must conform to the syntax for *MemberName*, defined below. |
| value    | encoded_value | element value                                                                                                                               |

## String syntax

There are several kinds of item in a `.dex` file which ultimately refer to a string. The following
BNF-style definitions indicate the acceptable syntax for these strings.

### SimpleName

A *SimpleName* is the basis for the syntax of the names of other things. The `.dex` format allows a
fair amount of latitude here (much more than most common source languages). In brief, a simple name
consists of any low-ASCII alphabetic character or digit, a few specific low-ASCII symbols, and most
non-ASCII code points that are not control, space, or special characters. Starting from version
`040` the format additionally allows space characters (Unicode `Zs` category). Note that surrogate
code points (in the range `U+d800` ... `U+dfff`) are not considered valid name characters, per se,
but Unicode supplemental characters *are* valid (which are represented by the final alternative of
the rule for *SimpleNameChar*), and they should be represented in a file as pairs of surrogate code
points in the MUTF-8 encoding.

|                    |                                      |                       |
|--------------------|--------------------------------------|-----------------------|
| *SimpleName* →     |                                      |                       |
|                    | *SimpleNameChar* (*SimpleNameChar*)* |                       |
| *SimpleNameChar* → |                                      |                       |
|                    | `'A'` ... `'Z'`                      |                       |
| \|                 | `'a'` ... `'z'`                      |                       |
| \|                 | `'0'` ... `'9'`                      |                       |
| \|                 | `' '`                                | since DEX version 040 |
| \|                 | `'$'`                                |                       |
| \|                 | `'-'`                                |                       |
| \|                 | `'_'`                                |                       |
| \|                 | `U+00a0`                             | since DEX version 040 |
| \|                 | `U+00a1` ... `U+1fff`                |                       |
| \|                 | `U+2000` ... `U+200a`                | since DEX version 040 |
| \|                 | `U+2010` ... `U+2027`                |                       |
| \|                 | `U+202f`                             | since DEX version 040 |
| \|                 | `U+2030` ... `U+d7ff`                |                       |
| \|                 | `U+e000` ... `U+ffef`                |                       |
| \|                 | `U+10000` ... `U+10ffff`             |                       |

### MemberName

#### Used by field_id_item and method_id_item

A *MemberName* is the name of a member of a class, members being fields, methods, and inner classes.

|                |                          |
|----------------|--------------------------|
| *MemberName* → |                          |
|                | *SimpleName*             |
| \|             | `'<'` *SimpleName* `'>'` |

### FullClassName

A *FullClassName* is a fully qualified class name, including an optional package specifier followed
by a required name.

|                           |                                      |
|---------------------------|--------------------------------------|
| *FullClassName* →         |                                      |
|                           | *OptionalPackagePrefix* *SimpleName* |
| *OptionalPackagePrefix* → |                                      |
|                           | (*SimpleName* `'/'`)*                |

### TypeDescriptor

#### Used by type_id_item

A *TypeDescriptor* is the representation of any type, including primitives, classes, arrays, and
`void`. See below for the meaning of the various versions.

|                                |                                                 |
|--------------------------------|-------------------------------------------------|
| *TypeDescriptor* →             |                                                 |
|                                | `'V'`                                           |
| \|                             | *FieldTypeDescriptor*                           |
| *FieldTypeDescriptor* →        |                                                 |
|                                | *NonArrayFieldTypeDescriptor*                   |
| \|                             | (`'['` * 1...255) *NonArrayFieldTypeDescriptor* |
| *NonArrayFieldTypeDescriptor*→ |                                                 |
|                                | `'Z'`                                           |
| \|                             | `'B'`                                           |
| \|                             | `'S'`                                           |
| \|                             | `'C'`                                           |
| \|                             | `'I'`                                           |
| \|                             | `'J'`                                           |
| \|                             | `'F'`                                           |
| \|                             | `'D'`                                           |
| \|                             | `'L'` *FullClassName* `';'`                     |

### ShortyDescriptor

#### Used by proto_id_item

A *ShortyDescriptor* is the short form representation of a method prototype, including return and
parameter types, except that there is no distinction between various reference (class or array)
types. Instead, all reference types are represented by a single `'L'` character.

|                      |                                         |
|----------------------|-----------------------------------------|
| *ShortyDescriptor* → |                                         |
|                      | *ShortyReturnType* (*ShortyFieldType*)* |
| *ShortyReturnType* → |                                         |
|                      | `'V'`                                   |
| \|                   | *ShortyFieldType*                       |
| *ShortyFieldType* →  |                                         |
|                      | `'Z'`                                   |
| \|                   | `'B'`                                   |
| \|                   | `'S'`                                   |
| \|                   | `'C'`                                   |
| \|                   | `'I'`                                   |
| \|                   | `'J'`                                   |
| \|                   | `'F'`                                   |
| \|                   | `'D'`                                   |
| \|                   | `'L'`                                   |

### TypeDescriptor semantics

This is the meaning of each of the variants of *TypeDescriptor*.

| Syntax                   | Meaning                                                                                                                  |
|--------------------------|--------------------------------------------------------------------------------------------------------------------------|
| V                        | `void`; only valid for return types                                                                                      |
| Z                        | `boolean`                                                                                                                |
| B                        | `byte`                                                                                                                   |
| S                        | `short`                                                                                                                  |
| C                        | `char`                                                                                                                   |
| I                        | `int`                                                                                                                    |
| J                        | `long`                                                                                                                   |
| F                        | `float`                                                                                                                  |
| D                        | `double`                                                                                                                 |
| L*fully/qualified/Name*; | the class *`fully.qualified.Name`*                                                                                       |
| [*descriptor*            | array of *`descriptor`*, usable recursively for arrays-of-arrays, though it is invalid to have more than 255 dimensions. |

## Items and related structures

This section defines each top-level item that may appear in a `.dex` file, in file order. Where the
compact container changes an item's layout, the compact definition follows the standard one.

### header_item

#### Appears in the header section

#### Alignment: 4 bytes

#### Size: 0x70 (112) bytes (version `040` and earlier); 0x78 (120) bytes (version `041` and later)

| Name            | Format                    | Description                                                                                                                                                                                                                                                                                                               |
|-----------------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| magic           | ubyte[8] = DEX_FILE_MAGIC | magic value. See "`DEX_FILE_MAGIC`" above.                                                                                                                                                                                                                                                                                |
| checksum        | uint                      | adler32 checksum of the rest of the file; see "Checksum and signature" under File layout                                                                                                                                                                                                                                  |
| signature       | ubyte[20]                 | SHA-1 signature of the rest of the file; see "Checksum and signature" under File layout                                                                                                                                                                                                                                   |
| file_size       | uint                      | size of the entire file (including the header), in bytes (version `040` and earlier); distance in bytes from the start of this header to the next header or to the end of the whole file (the container) (version `041` and later)                                                                                        |
| header_size     | uint                      | size of the header (this entire section), in bytes. This allows for at least a limited amount of backwards/forwards compatibility without invalidating the format. Must be 0x70 (112) bytes (version `040` and earlier) or 0x78 (120) bytes (version `041` and later).                                                    |
| endian_tag      | uint = ENDIAN_CONSTANT    | endianness tag. See "`ENDIAN_CONSTANT` and `REVERSE_ENDIAN_CONSTANT`" above.                                                                                                                                                                                                                                              |
| link_size       | uint                      | size of the link section, or `0` if this file isn't statically linked                                                                                                                                                                                                                                                     |
| link_off        | uint                      | offset from the start of the file to the link section, or `0` if `link_size == 0`. The offset, if non-zero, should point into the `link_data` section. The format of the data pointed at is left unspecified by this document; this header field (and the previous) are left as hooks for use by runtime implementations. |
| map_off         | uint                      | offset from the start of the file to the map item. The offset, which must be non-zero, should point into the `data` section, and the data should be in the format specified by "`map_list`" below.                                                                                                                        |
| string_ids_size | uint                      | count of strings in the string identifiers list                                                                                                                                                                                                                                                                           |
| string_ids_off  | uint                      | offset from the start of the file to the string identifiers list, or `0` if `string_ids_size == 0` (admittedly a strange edge case). The offset, if non-zero, should be to the start of the `string_ids` section.                                                                                                         |
| type_ids_size   | uint                      | count of elements in the type identifiers list, at most 65535                                                                                                                                                                                                                                                             |
| type_ids_off    | uint                      | offset from the start of the file to the type identifiers list, or `0` if `type_ids_size == 0` (admittedly a strange edge case). The offset, if non-zero, should be to the start of the `type_ids` section.                                                                                                               |
| proto_ids_size  | uint                      | count of elements in the prototype identifiers list, at most 65535                                                                                                                                                                                                                                                        |
| proto_ids_off   | uint                      | offset from the start of the file to the prototype identifiers list, or `0` if `proto_ids_size == 0` (admittedly a strange edge case). The offset, if non-zero, should be to the start of the `proto_ids` section.                                                                                                        |
| field_ids_size  | uint                      | count of elements in the field identifiers list                                                                                                                                                                                                                                                                           |
| field_ids_off   | uint                      | offset from the start of the file to the field identifiers list, or `0` if `field_ids_size == 0`. The offset, if non-zero, should be to the start of the `field_ids` section.                                                                                                                                             |
| method_ids_size | uint                      | count of elements in the method identifiers list                                                                                                                                                                                                                                                                          |
| method_ids_off  | uint                      | offset from the start of the file to the method identifiers list, or `0` if `method_ids_size == 0`. The offset, if non-zero, should be to the start of the `method_ids` section.                                                                                                                                          |
| class_defs_size | uint                      | count of elements in the class definitions list                                                                                                                                                                                                                                                                           |
| class_defs_off  | uint                      | offset from the start of the file to the class definitions list, or `0` if `class_defs_size == 0` (admittedly a strange edge case). The offset, if non-zero, should be to the start of the `class_defs` section.                                                                                                          |
| data_size       | uint                      | size of the `data` section in bytes. Must be an even multiple of `sizeof(uint)` (version `040` and earlier); unused (version `041` and later)                                                                                                                                                                             |
| data_off        | uint                      | offset from the start of the file to the start of the `data` section (version `040` and earlier); unused (version `041` and later)                                                                                                                                                                                        |
| container_size  | uint                      | size of the entire container, including other dex headers and their data (version `041` and later). In earlier versions this field does not exist; there it is equivalent to `file_size`.                                                                                                                                 |
| header_offset   | uint                      | offset from the start of the file to the start of this header (version `041` and later). In earlier versions this field does not exist; there it is equivalent to `0`.                                                                                                                                                    |

### compact header_item

#### Appears in the header section

#### Alignment: 4 bytes

#### Size: 0x88 (136) bytes

The header of a compact file. Its first 0x70 bytes follow the standard `header_item` layout; the
last six `uint`s are compact-specific.

| Name                            | Format                     | Description                                                                                                                                         |
|---------------------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| magic                           | ubyte[8] = CDEX_FILE_MAGIC | magic value. See "`CDEX_FILE_MAGIC`" above.                                                                                                         |
| checksum                        | uint                       | cdex checksum of the main and data sections; see "Checksum and signature" under File layout                                                         |
| signature                       | ubyte[20]                  | SHA-1 signature of the standard file this compact file was produced from                                                                            |
| file_size                       | uint                       | size of the main section only: the header plus the id and definition tables                                                                         |
| header_size                     | uint                       | size of the header (this entire section), in bytes; must be 0x88 (136)                                                                              |
| endian_tag                      | uint = ENDIAN_CONSTANT     | endianness tag. See "`ENDIAN_CONSTANT` and `REVERSE_ENDIAN_CONSTANT`" above.                                                                        |
| link_size                       | uint                       | size of the link data, or `0` if this file isn't statically linked                                                                                  |
| link_off                        | uint                       | offset to the link data, or `0` if `link_size == 0`                                                                                                 |
| map_off                         | uint                       | offset to the map item. The offset must be non-zero, and the data should be in the format specified by "`map_list`" below.                          |
| string_ids_size                 | uint                       | count of strings in the string identifiers list                                                                                                     |
| string_ids_off                  | uint                       | offset from the start of the file to the string identifiers list, or `0` if `string_ids_size == 0`                                                  |
| type_ids_size                   | uint                       | count of elements in the type identifiers list, at most 65535                                                                                       |
| type_ids_off                    | uint                       | offset from the start of the file to the type identifiers list, or `0` if `type_ids_size == 0`                                                      |
| proto_ids_size                  | uint                       | count of elements in the prototype identifiers list, at most 65535                                                                                  |
| proto_ids_off                   | uint                       | offset from the start of the file to the prototype identifiers list, or `0` if `proto_ids_size == 0`                                                |
| field_ids_size                  | uint                       | count of elements in the field identifiers list                                                                                                     |
| field_ids_off                   | uint                       | offset from the start of the file to the field identifiers list, or `0` if `field_ids_size == 0`                                                    |
| method_ids_size                 | uint                       | count of elements in the method identifiers list                                                                                                    |
| method_ids_off                  | uint                       | offset from the start of the file to the method identifiers list, or `0` if `method_ids_size == 0`                                                  |
| class_defs_size                 | uint                       | count of elements in the class definitions list                                                                                                     |
| class_defs_off                  | uint                       | offset from the start of the file to the class definitions list, or `0` if `class_defs_size == 0`                                                   |
| data_size                       | uint                       | size of the part of the data section this file may use                                                                                              |
| data_off                        | uint                       | offset from the start of this file to the data section                                                                                              |
| feature_flags                   | uint                       | compact feature flags. See "`Feature flags`" above.                                                                                                 |
| debug_info_offsets_pos          | uint                       | offset to the debug info offsets table data                                                                                                         |
| debug_info_offsets_table_offset | uint                       | offset of the `uint32` lookup table within that table data. See "`debug_info_offsets_table`" below.                                                 |
| debug_info_base                 | uint                       | the smallest non-zero debug info offset; the base of the table's deltas                                                                             |
| owned_data_begin                | uint                       | start of the range of the data section that this file owns, relative to the start of the data section. See "Compact file layout" under File layout. |
| owned_data_end                  | uint                       | end of that range                                                                                                                                   |

### legacy header_item (version 009)

#### Appears in the header section

#### Alignment: 4 bytes

#### Size: 0x5c (92) bytes

| Name                | Format                        | Description                                                                                        |
|---------------------|-------------------------------|----------------------------------------------------------------------------------------------------|
| magic               | ubyte[8] = DEX_FILE_MAGIC_009 | magic value. See "`DEX_FILE_MAGIC`" above.                                                         |
| checksum            | uint                          | adler32 checksum of everything from offset `12` to the end of the file                             |
| signature           | ubyte[20]                     | SHA-1 signature of everything from offset `32` to the end of the file                              |
| file_size           | uint                          | size of the entire file (including the header), in bytes                                           |
| header_size         | uint                          | size of the header (this entire section), in bytes; must be 0x5c (92)                              |
| link_size           | uint                          | size of the link section; `0`, because no link data exists in this version                         |
| link_off            | uint                          | offset from the start of the file to the link section; `0`                                         |
| string_ids_size     | uint                          | count of strings in the string identifiers list                                                    |
| string_ids_off      | uint                          | offset from the start of the file to the string identifiers list, or `0` if `string_ids_size == 0` |
| string_objects_size | uint                          | count of strings that code refers to as constant objects                                           |
| class_ids_size      | uint                          | count of elements in the type identifiers list                                                     |
| class_ids_off       | uint                          | offset from the start of the file to the type identifiers list, or `0` if `class_ids_size == 0`    |
| field_ids_size      | uint                          | count of elements in the field identifiers list                                                    |
| field_ids_off       | uint                          | offset from the start of the file to the field identifiers list, or `0` if `field_ids_size == 0`   |
| method_ids_size     | uint                          | count of elements in the method identifiers list                                                   |
| method_ids_off      | uint                          | offset from the start of the file to the method identifiers list, or `0` if `method_ids_size == 0` |
| class_defs_size     | uint                          | count of elements in the class definitions list                                                    |
| class_defs_off      | uint                          | offset from the start of the file to the class definitions list, or `0` if `class_defs_size == 0`  |

### legacy header_item (version 013)

#### Appears in the header section

#### Alignment: 4 bytes

#### Size: 0x7c (124) bytes

| Name                | Format                        | Description                                                                                        |
|---------------------|-------------------------------|----------------------------------------------------------------------------------------------------|
| magic               | ubyte[8] = DEX_FILE_MAGIC_013 | magic value. See "`DEX_FILE_MAGIC`" above.                                                         |
| checksum            | uint                          | adler32 checksum of everything from offset `12` to the end of the file                             |
| signature           | ubyte[20]                     | SHA-1 signature of everything from offset `32` to the end of the file                              |
| file_size           | uint                          | size of the entire file (including the header), in bytes                                           |
| header_size         | uint                          | size of the header (this entire section), in bytes; must be 0x7c (124)                             |
| link_size           | uint                          | size of the link section; `0`, because no link data exists in this version                         |
| link_off            | uint                          | offset from the start of the file to the link section; `0`                                         |
| string_ids_size     | uint                          | count of strings in the string identifiers list                                                    |
| string_ids_off      | uint                          | offset from the start of the file to the string identifiers list, or `0` if `string_ids_size == 0` |
| string_objects_size | uint                          | count of strings that code refers to as constant objects                                           |
| type_ids_size       | uint                          | count of elements in the type identifiers list                                                     |
| type_ids_off        | uint                          | offset from the start of the file to the type identifiers list, or `0` if `type_ids_size == 0`     |
| field_ids_size      | uint                          | count of elements in the field identifiers list                                                    |
| field_ids_off       | uint                          | offset from the start of the file to the field identifiers list, or `0` if `field_ids_size == 0`   |
| method_ids_size     | uint                          | count of elements in the method identifiers list                                                   |
| method_ids_off      | uint                          | offset from the start of the file to the method identifiers list, or `0` if `method_ids_size == 0` |
| class_defs_size     | uint                          | count of elements in the class definitions list                                                    |
| class_defs_off      | uint                          | offset from the start of the file to the class definitions list, or `0` if `class_defs_size == 0`  |
| word_data_size      | uint                          | size of the `word_data` area, in bytes, or `0` if the area is empty                                |
| word_data_off       | uint                          | offset from the start of the file to the `word_data` area, or `0` if the area is empty             |
| codes_size          | uint                          | size of the `codes` area, in bytes, or `0` if the area is empty                                    |
| codes_off           | uint                          | offset from the start of the file to the `codes` area, or `0` if the area is empty                 |
| string_data_size    | uint                          | size of the `string_data` area, in bytes, or `0` if the area is empty                              |
| string_data_off     | uint                          | offset from the start of the file to the `string_data` area, or `0` if the area is empty           |
| byte_data_size      | uint                          | size of the `byte_data` area, in bytes, or `0` if the area is empty                                |
| byte_data_off       | uint                          | offset from the start of the file to the `byte_data` area, or `0` if the area is empty             |

### map_list

#### Appears in the data section

#### Referenced from header_item and compact header_item

#### Alignment: 4 bytes

#### Size: 4 + (size × 12) bytes

This is a list of the entire contents of a file, in order. It contains some redundancy with respect
to the `header_item` but is intended to be an easy form to use to iterate over an entire file. A
given type must appear at most once in a map, but there is no restriction on what order types may
appear in, other than the restrictions implied by the rest of the format (e.g., a `header` section
must appear first, followed by a `string_ids` section, etc.). Additionally, the map entries must be
ordered by initial offset and must not overlap.

| Name | Format         | Description                  |
|------|----------------|------------------------------|
| size | uint           | size of the list, in entries |
| list | map_item[size] | elements of the list         |

A `map_item` that describes the header or one of the id and definition tables carries a
file-relative offset; every other offset in the file is data-section relative. See "Offset bases"
under File layout.

The map list is also the only way to locate the `method_handles` and `call_site_ids` tables: the
header has no fields for them.

### map_item format

#### Size: 12 bytes

| Name   | Format | Description                                                                                                   |
|--------|--------|---------------------------------------------------------------------------------------------------------------|
| type   | ushort | type of the items; see table below                                                                            |
| unused | ushort | *(unused)*                                                                                                    |
| size   | uint   | count of the number of items to be found at the indicated offset                                              |
| offset | uint   | offset to the items in question. See "Offset bases" under File layout for the entries that are file-relative. |

### Type codes

| Item Type                  | Constant                        | Value  | Item Size In Bytes     |
|----------------------------|---------------------------------|--------|------------------------|
| header_item                | TYPE_HEADER_ITEM                | 0x0000 | 0x70                   |
| string_id_item             | TYPE_STRING_ID_ITEM             | 0x0001 | 0x04                   |
| type_id_item               | TYPE_TYPE_ID_ITEM               | 0x0002 | 0x04                   |
| proto_id_item              | TYPE_PROTO_ID_ITEM              | 0x0003 | 0x0c                   |
| field_id_item              | TYPE_FIELD_ID_ITEM              | 0x0004 | 0x08                   |
| method_id_item             | TYPE_METHOD_ID_ITEM             | 0x0005 | 0x08                   |
| class_def_item             | TYPE_CLASS_DEF_ITEM             | 0x0006 | 0x20                   |
| call_site_id_item          | TYPE_CALL_SITE_ID_ITEM          | 0x0007 | 0x04                   |
| method_handle_item         | TYPE_METHOD_HANDLE_ITEM         | 0x0008 | 0x08                   |
| map_list                   | TYPE_MAP_LIST                   | 0x1000 | 4 + (item.size * 12)   |
| type_list                  | TYPE_TYPE_LIST                  | 0x1001 | 4 + (item.size * 2)    |
| annotation_set_ref_list    | TYPE_ANNOTATION_SET_REF_LIST    | 0x1002 | 4 + (item.size * 4)    |
| annotation_set_item        | TYPE_ANNOTATION_SET_ITEM        | 0x1003 | 4 + (item.size * 4)    |
| class_data_item            | TYPE_CLASS_DATA_ITEM            | 0x2000 | *implicit; must parse* |
| code_item                  | TYPE_CODE_ITEM                  | 0x2001 | *implicit; must parse* |
| string_data_item           | TYPE_STRING_DATA_ITEM           | 0x2002 | *implicit; must parse* |
| debug_info_item            | TYPE_DEBUG_INFO_ITEM            | 0x2003 | *implicit; must parse* |
| annotation_item            | TYPE_ANNOTATION_ITEM            | 0x2004 | *implicit; must parse* |
| encoded_array_item         | TYPE_ENCODED_ARRAY_ITEM         | 0x2005 | *implicit; must parse* |
| annotations_directory_item | TYPE_ANNOTATIONS_DIRECTORY_ITEM | 0x2006 | *implicit; must parse* |
| hiddenapi_class_data_item  | TYPE_HIDDENAPI_CLASS_DATA_ITEM  | 0xF000 | *implicit; must parse* |

In a compact file a `TYPE_HEADER_ITEM` item is 0x88 bytes long. The debug info offsets table is not
a map entry type: it is located through the compact header fields only.

### string_id_item

#### Appears in the string_ids section

#### Alignment: 4 bytes

#### Size: 4 bytes

| Name            | Format | Description                                                                                                                                                                                                               |
|-----------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| string_data_off | uint   | offset to the string data for this item. The offset should point into the `data` section, and the data should be in the format specified by "`string_data_item`" below. There is no alignment requirement for the offset. |

### legacy string_id_item

#### Appears in the string_ids section

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name       | Format | Description                                                                  |
|------------|--------|------------------------------------------------------------------------------|
| utf8_off   | uint   | offset from the start of the file to this string's `legacy string_data_item` |
| utf16_size | uint   | size of this string in UTF-16 code units (the decoded length)                |

The list is not sorted by content as a whole: the strings that code refers to as constant objects
come first, then the remaining strings, and each group is ordered by content. The
`string_objects_size` field of the header gives the size of the first group, so the indices below it
form the constant string index space.

### string_data_item

#### Appears in the data section

#### Alignment: none (byte-aligned)

#### Size: variable

| Name       | Format  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| utf16_size | uleb128 | size of this string, in UTF-16 code units (which is the "string length" in many systems). That is, this is the decoded length of the string. (The encoded length is implied by the position of the `0` byte.)                                                                                                                                                                                                                                                                                                                       |
| data       | ubyte[] | a series of MUTF-8 code units (a.k.a. octets, a.k.a. bytes) followed by a byte of value `0`. See "MUTF-8 (Modified UTF-8) Encoding" above for details and discussion about the data format. **Note:** It is acceptable to have a string which includes (the encoded form of) UTF-16 surrogate code units (that is, `U+d800` ... `U+dfff`) either in isolation or out-of-order with respect to the usual encoding of Unicode into UTF-16. It is up to higher-level uses of strings to reject such invalid encodings, if appropriate. |

### legacy string_data_item

#### Appears in the string_data section

#### Alignment: none (byte-aligned)

#### Size: variable

| Name | Format  | Description                                                                                 |
|------|---------|---------------------------------------------------------------------------------------------|
| data | ubyte[] | a series of MUTF-8 code units (a.k.a. octets, a.k.a. bytes) followed by a byte of value `0` |

There is no length prefix: the decoded length is carried by the `utf16_size` field of the owning
`legacy string_id_item`.

### type_id_item

#### Appears in the type_ids section

#### Alignment: 4 bytes

#### Size: 4 bytes

| Name           | Format | Description                                                                                                                                         |
|----------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| descriptor_idx | uint   | index into the `string_ids` list for the descriptor string of this type. The string must conform to the syntax for *TypeDescriptor*, defined above. |

### legacy class_id_item (version 009)

#### Appears in the class_ids section

#### Alignment: 4 bytes

#### Size: 4 bytes

| Name     | Format | Description                                                                                  |
|----------|--------|----------------------------------------------------------------------------------------------|
| name_idx | uint   | index into the `string_ids` list for the binary name of this class, e.g. `java/lang/String`; |

The list carries no sorting requirement: the order of the entries is arbitrary.

### legacy type_id_item (version 013)

#### Appears in the type_ids section

#### Alignment: 4 bytes

#### Size: 4 bytes

| Name           | Format | Description                                                             |
|----------------|--------|-------------------------------------------------------------------------|
| descriptor_idx | uint   | index into the `string_ids` list for the descriptor string of this type |

The list carries no sorting requirement: the order of the entries is arbitrary.

### proto_id_item

#### Appears in the proto_ids section

#### Alignment: 4 bytes

#### Size: 12 bytes

| Name            | Format | Description                                                                                                                                                                                                                                                                                                              |
|-----------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| shorty_idx      | uint   | index into the `string_ids` list for the short-form descriptor string of this prototype. The string must conform to the syntax for *ShortyDescriptor*, defined above, and must correspond to the return type and parameters of this item.                                                                                |
| return_type_idx | uint   | index into the `type_ids` list for the return type of this prototype                                                                                                                                                                                                                                                     |
| parameters_off  | uint   | offset to the list of parameter types for this prototype, or `0` if this prototype has no parameters. This offset, if non-zero, should be in the `data` section, and the data there should be in the format specified by "`type_list`" below. Additionally, there should be no reference to the type `void` in the list. |

### field_id_item

#### Appears in the field_ids section

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name      | Format | Description                                                                                                                         |
|-----------|--------|-------------------------------------------------------------------------------------------------------------------------------------|
| class_idx | ushort | index into the `type_ids` list for the definer of this field. This must be a class type, and not an array or primitive type.        |
| type_idx  | ushort | index into the `type_ids` list for the type of this field                                                                           |
| name_idx  | uint   | index into the `string_ids` list for the name of this field. The string must conform to the syntax for *MemberName*, defined above. |

### legacy field_id_item

#### Appears in the field_ids section

#### Alignment: 4 bytes

#### Size: 12 bytes

| Name           | Format | Description                                                                                                                 |
|----------------|--------|-----------------------------------------------------------------------------------------------------------------------------|
| class_idx      | uint   | index into the `type_ids` list (version `013`) or the `class_ids` list (version `009`) for the defining class of this field |
| name_idx       | uint   | index into the `string_ids` list for the name of this field                                                                 |
| descriptor_idx | uint   | index into the `string_ids` list for the type descriptor of this field                                                      |

The list is sorted by defining class, then name, then descriptor.

### method_id_item

#### Appears in the method_ids section

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name      | Format | Description                                                                                                                          |
|-----------|--------|--------------------------------------------------------------------------------------------------------------------------------------|
| class_idx | ushort | index into the `type_ids` list for the definer of this method. This must be a class or array type, and not a primitive type.         |
| proto_idx | ushort | index into the `proto_ids` list for the prototype of this method                                                                     |
| name_idx  | uint   | index into the `string_ids` list for the name of this method. The string must conform to the syntax for *MemberName*, defined above. |

### legacy method_id_item

#### Appears in the method_ids section

#### Alignment: 4 bytes

#### Size: 12 bytes

| Name           | Format | Description                                                                                                                  |
|----------------|--------|------------------------------------------------------------------------------------------------------------------------------|
| class_idx      | uint   | index into the `type_ids` list (version `013`) or the `class_ids` list (version `009`) for the defining class of this method |
| name_idx       | uint   | index into the `string_ids` list for the name of this method                                                                 |
| descriptor_idx | uint   | index into the `string_ids` list for the prototype descriptor of this method                                                 |

The list is sorted by defining class, then name, then descriptor. A method's prototype is its
descriptor string; there is no `proto_ids` section.

### class_def_item

#### Appears in the class_defs section

#### Alignment: 4 bytes

#### Size: 32 bytes

| Name              | Format | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|-------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| class_idx         | uint   | index into the `type_ids` list for this class. This must be a class type, and not an array or primitive type.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| access_flags      | uint   | access flags for the class (`public`, `final`, etc.). See "`access_flags` Definitions" for details.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| superclass_idx    | uint   | index into the `type_ids` list for the superclass, or the constant value `NO_INDEX` if this class has no superclass (i.e., it is a root class such as `Object`). If present, this must be a class type, and not an array or primitive type.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| interfaces_off    | uint   | offset to the list of interfaces, or `0` if there are none. This offset should point into the `data` section, and the data there should be in the format specified by "`type_list`" below. Each of the elements of the list must be a class type (not an array or primitive type), and there must not be any duplicates.                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| source_file_idx   | uint   | index into the `string_ids` list for the name of the file containing the original source for (at least most of) this class, or the special value `NO_INDEX` to represent a lack of this information. The `debug_info_item` of any given method may override this source file, but the expectation is that most classes will only come from one source file.                                                                                                                                                                                                                                                                                                                                                                                                            |
| annotations_off   | uint   | offset to the annotations structure for this class, or `0` if there are no annotations on this class. This offset, if non-zero, should point into the `data` section, and the data there should be in the format specified by "`annotations_directory_item`" below, with all items referring to this class as the definer.                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| class_data_off    | uint   | offset to the associated class data for this item, or `0` if there is no class data for this class. (This may be the case, for example, if this class is a marker interface.) The offset, if non-zero, should point into the `data` section, and the data there should be in the format specified by "`class_data_item`" below, with all items referring to this class as the definer.                                                                                                                                                                                                                                                                                                                                                                                 |
| static_values_off | uint   | offset to the list of initial values for `static` fields, or `0` if there are none (and all `static` fields are to be initialized with `0` or `null`). This offset should point into the `data` section, and the data there should be in the format specified by "`encoded_array_item`" below. The size of the array must be no larger than the number of `static` fields declared by this class, and the elements correspond to the `static` fields in the same order as declared in the corresponding `field_list`. The type of each array element must match the declared type of its corresponding field. If there are fewer elements in the array than there are `static` fields, then the leftover fields are initialized with a type-appropriate `0` or `null`. |

### legacy class_def_item (version 009)

#### Appears in the class_defs section

#### Alignment: 4 bytes

#### Size: 32 bytes

| Name                | Format | Description                                                                                                                                                                                                                              |
|---------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| class_idx           | uint   | index into the `class_ids` list for this class                                                                                                                                                                                           |
| access_flags        | uint   | access flags for the class (`public`, `final`, etc.). See "`access_flags` definitions" above.                                                                                                                                            |
| superclass_idx      | uint   | index into the `class_ids` list for the superclass, or `NO_INDEX` if this class has no superclass                                                                                                                                        |
| interfaces_off      | uint   | offset to a `legacy type_list` of the implemented interfaces, or `0` if there are none                                                                                                                                                   |
| static_fields_off   | uint   | offset to the uniform list of static fields, or `0` if there are none. See "`legacy uniform list format`" and "`legacy static_field_item format`" below.                                                                                 |
| instance_fields_off | uint   | offset to the uniform list of instance fields, or `0` if there are none. See "`legacy uniform list format`" and "`legacy instance_field_item format`" below.                                                                             |
| direct_methods_off  | uint   | offset to the uniform list of direct methods, which hold the methods that are `static`, `private`, or a constructor, or `0` if there are none. See "`legacy uniform list format`" and "`legacy method_list_item format`" below.          |
| virtual_methods_off | uint   | offset to the uniform list of virtual methods, which hold the methods that are none of `static`, `private`, or a constructor, or `0` if there are none. See "`legacy uniform list format`" and "`legacy method_list_item format`" below. |

The list is ordered so that a class's superclass and implemented interfaces appear earlier than the
class itself, and the same class is not defined twice.

### legacy class_def_item (version 013)

#### Appears in the class_defs section

#### Alignment: 4 bytes

#### Size: 36 bytes

| Name                | Format | Description                                                                                                                                                                                                                              |
|---------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| class_idx           | uint   | index into the `type_ids` list for this class                                                                                                                                                                                            |
| access_flags        | uint   | access flags for the class (`public`, `final`, etc.). See "`access_flags` definitions" above.                                                                                                                                            |
| superclass_idx      | uint   | index into the `type_ids` list for the superclass, or `NO_INDEX` if this class has no superclass                                                                                                                                         |
| interfaces_off      | uint   | offset to a `legacy type_list` of the implemented interfaces, or `0` if there are none                                                                                                                                                   |
| static_fields_off   | uint   | offset to the uniform list of static fields, or `0` if there are none. See "`legacy uniform list format`" and "`legacy static_field_item format`" below.                                                                                 |
| instance_fields_off | uint   | offset to the uniform list of instance fields, or `0` if there are none. See "`legacy uniform list format`" and "`legacy instance_field_item format`" below.                                                                             |
| direct_methods_off  | uint   | offset to the uniform list of direct methods, which hold the methods that are `static`, `private`, or a constructor, or `0` if there are none. See "`legacy uniform list format`" and "`legacy method_list_item format`" below.          |
| virtual_methods_off | uint   | offset to the uniform list of virtual methods, which hold the methods that are none of `static`, `private`, or a constructor, or `0` if there are none. See "`legacy uniform list format`" and "`legacy method_list_item format`" below. |
| annotations_off     | uint   | offset to the `legacy annotations_directory_item` for this class; `0` in every item                                                                                                                                                      |

The list is ordered so that a class's superclass and implemented interfaces appear earlier than the
class itself, and the same class is not defined twice.

### call_site_id_item

#### Appears in the call_site_ids section

#### Alignment: 4 bytes

#### Size: 4 bytes

| Name          | Format | Description                                                                                                                                                            |
|---------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| call_site_off | uint   | offset to the call site definition. The offset should point into the `data` section, and the data there should be in the format specified by "`call_site_item`" below. |

### call_site_item

#### Referenced from call_site_id_item

#### Appears in the data section

#### Alignment: none (byte-aligned)

#### Size: variable

The call_site_item is an encoded_array_item whose elements correspond to the arguments provided to a
bootstrap linker method. The first three arguments are:

1. A method handle representing the bootstrap linker method (VALUE_METHOD_HANDLE).
2. A method name that the bootstrap linker should resolve (VALUE_STRING).
3. A method type corresponding to the type of the method name to be resolved (VALUE_METHOD_TYPE).

Any additional arguments are constant values passed to the bootstrap linker method. These arguments
are passed in order and without any type conversions.

The method handle representing the bootstrap linker method must have return type
`java.lang.invoke.CallSite`. The first three parameter types are:

1. `java.lang.invoke.Lookup`
2. `java.lang.String`
3. `java.lang.invoke.MethodType`

The parameter types of any additional arguments are determined from their constant values.

### method_handle_item

#### Appears in the method_handles section

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name               | Format | Description                                                                                        |
|--------------------|--------|----------------------------------------------------------------------------------------------------|
| method_handle_type | ushort | type of the method handle; see table below                                                         |
| unused             | ushort | *(unused)*                                                                                         |
| field_or_method_id | ushort | field or method id, depending on whether the method handle type is an accessor or a method invoker |
| unused             | ushort | *(unused)*                                                                                         |

### Method handle type codes

| Constant                              | Value | Description                                          |
|---------------------------------------|-------|------------------------------------------------------|
| METHOD_HANDLE_TYPE_STATIC_PUT         | 0x00  | Method handle is a static field setter (accessor)    |
| METHOD_HANDLE_TYPE_STATIC_GET         | 0x01  | Method handle is a static field getter (accessor)    |
| METHOD_HANDLE_TYPE_INSTANCE_PUT       | 0x02  | Method handle is an instance field setter (accessor) |
| METHOD_HANDLE_TYPE_INSTANCE_GET       | 0x03  | Method handle is an instance field getter (accessor) |
| METHOD_HANDLE_TYPE_INVOKE_STATIC      | 0x04  | Method handle is a static method invoker             |
| METHOD_HANDLE_TYPE_INVOKE_INSTANCE    | 0x05  | Method handle is an instance method invoker          |
| METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR | 0x06  | Method handle is a constructor method invoker        |
| METHOD_HANDLE_TYPE_INVOKE_DIRECT      | 0x07  | Method handle is a direct method invoker             |
| METHOD_HANDLE_TYPE_INVOKE_INTERFACE   | 0x08  | Method handle is an interface method invoker         |

### class_data_item

#### Referenced from class_def_item

#### Appears in the data section

#### Alignment: none (byte-aligned)

#### Size: variable

| Name                 | Format                               | Description                                                                                                                                                                                                                                                                                                                                                                            |
|----------------------|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| static_fields_size   | uleb128                              | the number of static fields defined in this item                                                                                                                                                                                                                                                                                                                                       |
| instance_fields_size | uleb128                              | the number of instance fields defined in this item                                                                                                                                                                                                                                                                                                                                     |
| direct_methods_size  | uleb128                              | the number of direct methods defined in this item                                                                                                                                                                                                                                                                                                                                      |
| virtual_methods_size | uleb128                              | the number of virtual methods defined in this item                                                                                                                                                                                                                                                                                                                                     |
| static_fields        | encoded_field[static_fields_size]    | the defined static fields, represented as a sequence of encoded elements. The fields must be sorted by `field_idx` in increasing order.                                                                                                                                                                                                                                                |
| instance_fields      | encoded_field[instance_fields_size]  | the defined instance fields, represented as a sequence of encoded elements. The fields must be sorted by `field_idx` in increasing order.                                                                                                                                                                                                                                              |
| direct_methods       | encoded_method[direct_methods_size]  | the defined direct (any of `static`, `private`, or constructor) methods, represented as a sequence of encoded elements. The methods must be sorted by `method_idx` in increasing order.                                                                                                                                                                                                |
| virtual_methods      | encoded_method[virtual_methods_size] | the defined virtual (none of `static`, `private`, or constructor) methods, represented as a sequence of encoded elements. This list should *not* include inherited methods unless overridden by the class that this item represents. The methods must be sorted by `method_idx` in increasing order. The `method_idx` of a virtual method must *not* be the same as any direct method. |

**Note:** All elements' `field_id` and `method_id` instances must refer to the same defining class.

### encoded_field format

#### Size: variable

| Name           | Format  | Description                                                                                                                                                                                                                                      |
|----------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| field_idx_diff | uleb128 | index into the `field_ids` list for the identity of this field (includes the name and descriptor), represented as a difference from the index of previous element in the list. The index of the first element in a list is represented directly. |
| access_flags   | uleb128 | access flags for the field (`public`, `final`, etc.). See "`access_flags` Definitions" for details.                                                                                                                                              |

### encoded_method format

#### Size: variable

| Name            | Format  | Description                                                                                                                                                                                                                                                       |
|-----------------|---------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| method_idx_diff | uleb128 | index into the `method_ids` list for the identity of this method (includes the name and descriptor), represented as a difference from the index of previous element in the list. The index of the first element in a list is represented directly.                |
| access_flags    | uleb128 | access flags for the method (`public`, `final`, etc.). See "`access_flags` Definitions" for details.                                                                                                                                                              |
| code_off        | uleb128 | offset to the code structure for this method, or `0` if this method is either `abstract` or `native`. The offset should point into the `data` section. The format of the data is specified by "`code_item`" below, or by "`compact code_item`" in a compact file. |

### legacy uniform list format

#### Appears in the word_data section

#### Alignment: max (4, element alignment)

#### Size: 4 + (count × element size) bytes, plus optional padding

| Name     | Format                    | Description                                                                                        |
|----------|---------------------------|----------------------------------------------------------------------------------------------------|
| count    | uint                      | number of elements in the list                                                                     |
| padding  | ubyte[4] *(optional)* = 0 | four zero bytes, present when the elements are 8-byte aligned, so that the header occupies 8 bytes |
| elements | element[count]            | the elements, each of the same fixed size and alignment                                            |

All per-class member lists and the version `013` annotation lists are uniform lists. A list of
4-byte-aligned elements begins directly with the count; a list of static fields, whose elements are
8-byte aligned, begins with the count followed by four zero bytes.

### legacy instance_field_item format

#### Appears in a uniform list referenced from `legacy class_def_item`

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name         | Format | Description                                                                                   |
|--------------|--------|-----------------------------------------------------------------------------------------------|
| field_idx    | uint   | index into the `field_ids` list for the identity of this field                                |
| access_flags | uint   | access flags for the field (`public`, `final`, etc.). See "`access_flags` definitions" above. |

### legacy static_field_item format

#### Appears in a uniform list referenced from `legacy class_def_item`

#### Alignment: 8 bytes

#### Size: 16 bytes

| Name           | Format | Description                                                                                   |
|----------------|--------|-----------------------------------------------------------------------------------------------|
| field_idx      | uint   | index into the `field_ids` list for the identity of this field                                |
| access_flags   | uint   | access flags for the field (`public`, `final`, etc.). See "`access_flags` definitions" above. |
| constant_value | ulong  | the initial value of the field, interpreted according to its declared type as follows         |

| Declared type of the field                                     | Meaning of `constant_value`                                                                                   |
|----------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| primitive                                                      | the bits of the constant, interpreted according to the declared type; `0` if the field has no value           |
| `String`                                                       | index into the `string_ids` list for the constant string                                                      |
| `Class`                                                        | index into the `type_ids` list (version `013`) or the `class_ids` list (version `009`) for the constant class |
| any other reference type, or a reference field without a value | `0xffffffffffffffff` (`null`)                                                                                 |

### legacy method_list_item format

#### Appears in a uniform list referenced from `legacy class_def_item`

#### Alignment: 4 bytes

#### Size: 16 bytes

| Name            | Format | Description                                                                                      |
|-----------------|--------|--------------------------------------------------------------------------------------------------|
| method_idx      | uint   | index into the `method_ids` list for the identity of this method                                 |
| access_flags    | uint   | access flags for the method (`public`, `final`, etc.). See "`access_flags` definitions" above.   |
| throws_list_off | uint   | offset to a `legacy type_list` of the declared thrown classes, or `0` if there are none          |
| code_off        | uint   | offset to the `legacy code_item` for this method, or `0` if the method is `abstract` or `native` |

### type_list

#### Referenced from class_def_item and proto_id_item

#### Appears in the data section

#### Alignment: 4 bytes

#### Size: 4 + (size × 2) bytes

| Name | Format          | Description                  |
|------|-----------------|------------------------------|
| size | uint            | size of the list, in entries |
| list | type_item[size] | elements of the list         |

### type_item format

#### Size: 2 bytes

| Name     | Format | Description                    |
|----------|--------|--------------------------------|
| type_idx | ushort | index into the `type_ids` list |

### legacy type_list

#### Referenced from `legacy class_def_item` and `legacy method_list_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 4 + (size × 4) bytes

| Name | Format     | Description                                                                                                |
|------|------------|------------------------------------------------------------------------------------------------------------|
| size | uint       | size of the list, in entries                                                                               |
| list | uint[size] | one type index per entry, into the `type_ids` list (version `013`) or the `class_ids` list (version `009`) |

The item carries the interface lists of class definitions and the throws lists of methods.

### code_item

#### Referenced from encoded_method

#### Appears in the data section

#### Alignment: 4 bytes

#### Size: variable

| Name           | Format                                  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|----------------|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| registers_size | ushort                                  | the number of registers used by this code                                                                                                                                                                                                                                                                                                                                                                                                        |
| ins_size       | ushort                                  | the number of words of incoming arguments to the method that this code is for                                                                                                                                                                                                                                                                                                                                                                    |
| outs_size      | ushort                                  | the number of words of outgoing argument space required by this code for method invocation                                                                                                                                                                                                                                                                                                                                                       |
| tries_size     | ushort                                  | the number of `try_item`s for this instance. If non-zero, then these appear as the `tries` array just after the `insns` in this instance.                                                                                                                                                                                                                                                                                                        |
| debug_info_off | uint                                    | offset to the debug info (line numbers + local variable info) sequence for this code, or `0` if there simply is no information. The offset, if non-zero, should point into the `data` section. The format of the data is specified by "`debug_info_item`" below.                                                                                                                                                                                 |
| insns_size     | uint                                    | size of the instructions list, in 16-bit code units                                                                                                                                                                                                                                                                                                                                                                                              |
| insns          | ushort[insns_size]                      | actual array of bytecode. The format of code in an `insns` array is specified by the companion document [Dalvik bytecode](dalvik-bytecode.md). Note that though this is defined as an array of `ushort`, there are some internal structures that prefer four-byte alignment. Also, if this happens to be in an endian-swapped file, then the swapping is *only* done on individual `ushort` instances and not on the larger internal structures. |
| padding        | ushort *(optional)* = 0                 | two bytes of padding to make `tries` four-byte aligned. This element is only present if `tries_size` is non-zero and `insns_size` is odd.                                                                                                                                                                                                                                                                                                        |
| tries          | try_item[tries_size] *(optional)*       | array indicating where in the code exceptions are caught and how to handle them. Elements of the array must be non-overlapping in range and in order from low to high address. This element is only present if `tries_size` is non-zero.                                                                                                                                                                                                         |
| handlers       | encoded_catch_handler_list *(optional)* | bytes representing a list of lists of catch types and associated handler addresses. Each `try_item` has a byte-wise offset into this structure. This element is only present if `tries_size` is non-zero.                                                                                                                                                                                                                                        |

### compact code_item

#### Referenced from encoded_method

#### Appears in the data section

#### Alignment: 2 bytes

#### Size: variable

A compact code item carries a four-byte header in place of the standard sixteen-byte one: two packed
`ushort`s, preceded by a preheader of up to six `ushort`s that holds the values which do not fit.
The registered `code_off` points at `fields_`, after the preheader. There is no `debug_info_off`
field: the debug info offset of a method is looked up by method index in the
"`debug_info_offsets_table`".

| Name                   | Format                                  | Description                                                                                                                                                                                                                                            |
|------------------------|-----------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| fields_                | ushort                                  | four 4-bit fields: `registers_size - ins_size` in the high nibble, then `ins_size`, `outs_size`, `tries_size`. See the bit layout below.                                                                                                               |
| insns_count_and_flags_ | ushort                                  | the low 11 bits of `insns_size` in the high bits and the five preheader flags in the low 5 bits. See the bit layout below.                                                                                                                             |
| insns                  | ushort[insns_size]                      | the bytecode, which begins four bytes after `code_off`, directly after the two packed `ushort`s                                                                                                                                                        |
| padding                | ushort *(optional)* = 0                 | two bytes of padding to make `tries` four-byte aligned. It is present only when `tries_size` is non-zero and the end of `insns` is not four-byte aligned, which depends on the parity of the `insns` offset rather than on the parity of `insns_size`. |
| tries                  | try_item[tries_size] *(optional)*       | array of `try_item`, present only if `tries_size` is non-zero                                                                                                                                                                                          |
| handlers               | encoded_catch_handler_list *(optional)* | list of catch handlers, present only if `tries_size` is non-zero                                                                                                                                                                                       |

The item has no stored size: its end follows from `insns_size` and from the length of the catch
handler data after the instructions.

Bit layout of `fields_`:

| bit~15~                 | bit~14~          | bit~13~          | bit~12~          | bit~11~           | bit~10~    | bit~9~     | bit~8~     | bit~7~             | bit~6~      | bit~5~      | bit~4~      | bit~3~              | bit~2~       | bit~1~       | bit~0~       |
|-------------------------|------------------|------------------|------------------|-------------------|------------|------------|------------|--------------------|-------------|-------------|-------------|---------------------|--------------|--------------|--------------|
| `registers_size` (high) | `registers_size` | `registers_size` | `registers_size` | `ins_size` (high) | `ins_size` | `ins_size` | `ins_size` | `outs_size` (high) | `outs_size` | `outs_size` | `outs_size` | `tries_size` (high) | `tries_size` | `tries_size` | `tries_size` |

Bit layout of `insns_count_and_flags_`:

| bit~15~                          | ... | bit~5~             | bit~4~                        | bit~3~                  | bit~2~                 | bit~1~                | bit~0~                      |
|----------------------------------|-----|--------------------|-------------------------------|-------------------------|------------------------|-----------------------|-----------------------------|
| `insns_size` (low 11 bits, high) | ... | `insns_size` (low) | preheader: `insns_size`, high | preheader: `tries_size` | preheader: `outs_size` | preheader: `ins_size` | preheader: `registers_size` |

Flag bits `0x1 << 0` to `0x1 << 4` mark, in the order registers, ins, outs, tries, insns, which
values did not fit their packed field and are therefore extended by the preheader. Each nibble of
`fields_` stores its value modulo 16, and the preheader word of a field stores `value >> 4`.
`registers_size` is stored as `registers_size - ins_size`, which always fits because every code item
has `registers_size >= ins_size`, and decoding adds `ins_size` back.

The preheader words sit immediately before `fields_` in this increasing address order:

| Name                 | Format              | Description                                                             |
|----------------------|---------------------|-------------------------------------------------------------------------|
| `tries_size_ext`     | ushort *(optional)* | `tries_size >> 4`; present when flag `0x1 << 3` is set                  |
| `outs_size_ext`      | ushort *(optional)* | `outs_size >> 4`; present when flag `0x1 << 2` is set                   |
| `ins_size_ext`       | ushort *(optional)* | `ins_size >> 4`; present when flag `0x1 << 1` is set                    |
| `registers_size_ext` | ushort *(optional)* | `(registers_size - ins_size) >> 4`; present when flag `0x1 << 0` is set |
| `insns_size_hi`      | ushort *(optional)* | high 16 bits of `insns_size >> 11`; present when flag `0x1 << 4` is set |
| `insns_size_lo`      | ushort *(optional)* | low 16 bits of `insns_size >> 11`; present when flag `0x1 << 4` is set  |

The preheader is decoded by walking backward from `fields_` and testing the flags in the order
insns, registers, ins, outs, tries; both `insns` words are present whenever their single flag is
set. The complete values are:

```
registers_size = (fields_ >> 12) + ins_size + 16 * registers_size_ext
ins_size       = ((fields_ >> 8) & 0xf) + 16 * ins_size_ext
outs_size      = ((fields_ >> 4) & 0xf) + 16 * outs_size_ext
tries_size     = (fields_ & 0xf) + 16 * tries_size_ext
insns_size     = (insns_count_and_flags_ >> 5)
               | ((insns_size_lo | (insns_size_hi << 16)) << 11)
```

Only the try table and the payload data of `fill-array-data`, `packed-switch`, and `sparse-switch`
instructions are four-byte aligned inside the item, and payload data must be four-byte aligned
absolutely. The item itself starts 2-byte aligned, and the preheader lies before the registered
offset, so when a method references a payload, two bytes of padding precede the preheader exactly
when that makes `fields_`, and with it `insns`, four-byte aligned. The try table is four-byte
aligned absolutely, and its padding depends on the parity of the `insns` offset.

### legacy code_item (version 009)

#### Referenced from `legacy method_list_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 28 bytes

| Name            | Format | Description                                                                                        |
|-----------------|--------|----------------------------------------------------------------------------------------------------|
| registers_size  | ushort | the number of registers used by this code                                                          |
| ins_size        | ushort | the number of words of incoming arguments, including `this` for instance methods                   |
| outs_size       | ushort | the number of words of outgoing argument space required by this code                               |
| unused          | ushort | `0`                                                                                                |
| source_file_idx | int    | index into the `string_ids` list for the source file name, or `NO_INDEX` if unknown                |
| insns_off       | uint   | offset to the `legacy insns_item` for this code                                                    |
| exceptions_off  | uint   | offset to the `legacy exception_handler_list` for this code, or `0` if there are none              |
| positions_off   | uint   | offset to the `legacy positions_list` for this code, or `0` if there is no position information    |
| locals_off      | uint   | offset to the `legacy locals_list` for this code, or `0` if there is no local variable information |

The instruction array, the exception handler list, the position list, and the local variable list
are separate items.

### legacy code_item (version 013)

#### Referenced from `legacy method_list_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 24 bytes

| Name            | Format | Description                                                                           |
|-----------------|--------|---------------------------------------------------------------------------------------|
| registers_size  | ushort | the number of registers used by this code                                             |
| ins_size        | ushort | the number of words of incoming arguments, including `this` for instance methods      |
| outs_size       | ushort | the number of words of outgoing argument space required by this code                  |
| unused          | ushort | `0`                                                                                   |
| source_file_idx | int    | index into the `string_ids` list for the source file name, or `NO_INDEX` if unknown   |
| insns_off       | uint   | offset to the `legacy insns_item` for this code                                       |
| exceptions_off  | uint   | offset to the `legacy exception_handler_list` for this code, or `0` if there are none |
| debug_info_off  | uint   | offset to the `legacy debug_info_item` for this code, or `0` if there is none         |

The instruction array, the exception handler list, and the debug info item are separate items.

### legacy insns_item

#### Referenced from `legacy code_item`

#### Appears in the codes section

#### Alignment: 4 bytes

#### Size: 4 + (insns_size × 2) bytes

| Name       | Format             | Description                                         |
|------------|--------------------|-----------------------------------------------------|
| insns_size | uint               | size of the instruction array, in 16-bit code units |
| insns      | ushort[insns_size] | the instruction array of this code                  |

### legacy exception_handler_list

#### Referenced from `legacy code_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 4 + (count × 16) bytes

| Name    | Format       | Description               |
|---------|--------------|---------------------------|
| count   | uint         | number of entries         |
| entries | entry[count] | the entries, concatenated |

The list holds one entry per covered range and caught type pair: a range covered for several types
produces several entries.

### legacy exception_handler_entry format

#### Size: 16 bytes

| Name         | Format | Description                                                                                                                                                   |
|--------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| start_addr   | uint   | address of the first covered code unit, in 16-bit code units                                                                                                  |
| end_addr     | uint   | address one past the last covered code unit, in 16-bit code units                                                                                             |
| handler_addr | uint   | address of the first instruction of the exception handler, in 16-bit code units                                                                               |
| type_idx     | int    | index into the `type_ids` list (version `013`) or the `class_ids` list (version `009`) for the exception type to catch, or `NO_INDEX` for a catch-all handler |

### legacy positions_list (version 009)

#### Referenced from `legacy code_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 4 + (count × 6) bytes

| Name    | Format       | Description               |
|---------|--------------|---------------------------|
| count   | uint         | number of entries         |
| entries | entry[count] | the entries, concatenated |

### legacy position_entry format

#### Size: 6 bytes

| Name    | Format | Description                               |
|---------|--------|-------------------------------------------|
| address | uint   | instruction address, in 16-bit code units |
| line    | ushort | source line number                        |

### legacy locals_list (version 009)

#### Referenced from `legacy code_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 4 + (count × 20) bytes

| Name    | Format       | Description               |
|---------|--------------|---------------------------|
| count   | uint         | number of entries         |
| entries | entry[count] | the entries, concatenated |

### legacy local_entry format

#### Size: 20 bytes

| Name           | Format | Description                                                                                                                                                                   |
|----------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| start          | uint   | instruction address where the local goes live, in 16-bit code units                                                                                                           |
| end            | uint   | instruction address where the local goes out of scope, in 16-bit code units                                                                                                   |
| name_idx       | uint   | index into the `string_ids` list for the name of the local, or `NO_INDEX` if unknown                                                                                          |
| descriptor_idx | uint   | index into the `string_ids` list for a string holding the type descriptor of the local, or `NO_INDEX` if unknown; a local of the null type has the descriptor string `<null>` |
| register       | uint   | the register that contains the local                                                                                                                                          |

### try_item format

#### Size: 8 bytes

| Name        | Format | Description                                                                                                                                                                                     |
|-------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| start_addr  | uint   | start address of the block of code covered by this entry. The address is a count of 16-bit code units to the start of the first covered instruction.                                            |
| insn_count  | ushort | number of 16-bit code units covered by this entry. The last code unit covered (inclusive) is `start_addr + insn_count - 1`.                                                                     |
| handler_off | ushort | offset in bytes from the start of the associated `encoded_catch_handler_list` to the `encoded_catch_handler` for this entry. This must be an offset to the start of an `encoded_catch_handler`. |

### encoded_catch_handler_list format

#### Size: variable

| Name | Format                               | Description                                                                                        |
|------|--------------------------------------|----------------------------------------------------------------------------------------------------|
| size | uleb128                              | size of this list, in entries                                                                      |
| list | encoded_catch_handler[handlers_size] | actual list of handler lists, represented directly (not as offsets), and concatenated sequentially |

### encoded_catch_handler format

#### Size: variable

| Name           | Format                            | Description                                                                                                                                                                                                                                                                                                                                                                                                                          |
|----------------|-----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| size           | sleb128                           | number of catch types in this list. If non-positive, then this is the negative of the number of catch types, and the catches are followed by a catch-all handler. For example: A `size` of `0` means that there is a catch-all but no explicitly typed catches. A `size` of `2` means that there are two explicitly typed catches and no catch-all. And a `size` of `-1` means that there is one typed catch along with a catch-all. |
| handlers       | encoded_type_addr_pair[abs(size)] | stream of `abs(size)` encoded items, one for each caught type, in the order that the types should be tested.                                                                                                                                                                                                                                                                                                                         |
| catch_all_addr | uleb128 *(optional)*              | bytecode address of the catch-all handler. This element is only present if `size` is non-positive.                                                                                                                                                                                                                                                                                                                                   |

### encoded_type_addr_pair format

#### Size: variable

| Name     | Format  | Description                                                           |
|----------|---------|-----------------------------------------------------------------------|
| type_idx | uleb128 | index into the `type_ids` list for the type of the exception to catch |
| addr     | uleb128 | bytecode address of the associated exception handler                  |

### debug_info_item

#### Referenced from code_item

#### Appears in the data section

#### Alignment: none (byte-aligned)

#### Size: variable

Each `debug_info_item` defines a DWARF3-inspired byte-coded state machine that, when interpreted,
emits the positions table and (potentially) the local variable information for a `code_item`. The
sequence begins with a variable-length header (the length of which depends on the number of method
parameters), is followed by the state machine bytecodes, and ends with an `DBG_END_SEQUENCE` byte.

The state machine consists of five registers. The `address` register represents the instruction
offset in the associated `insns` array in 16-bit code units. The `address` register starts at `0` at
the beginning of each `debug_info` sequence and must only monotonically increase. The `line`
register represents what source line number should be associated with the next positions table entry
emitted by the state machine. It is initialized in the sequence header, and may change in positive
or negative directions but must never be less than `1`. The `source_file` register represents the
source file that the line number entries refer to. It is initialized to the value of
`source_file_idx` in `class_def_item`. The other two variables, `prologue_end` and `epilogue_begin`,
are boolean flags (initialized to `false`) that indicate whether the next position emitted should be
considered a method prologue or epilogue. The state machine must also track the name and type of the
last local variable live in each register for the `DBG_RESTART_LOCAL` code.

The header is as follows:

| Name            | Format                     | Description                                                                                                                                                                                                                         |
|-----------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| line_start      | uleb128                    | the initial value for the state machine's `line` register. Does not represent an actual positions entry.                                                                                                                            |
| parameters_size | uleb128                    | the number of parameter names that are encoded. There should be one per method parameter, excluding an instance method's `this`, if any.                                                                                            |
| parameter_names | uleb128p1[parameters_size] | string index of the method parameter name. An encoded value of `NO_INDEX` indicates that no name is available for the associated parameter. The type descriptor and signature are implied from the method descriptor and signature. |

The byte code values are as follows:

| Name                     | Value       | Format                                                                       | Arguments                                                                                                                                                              | Description                                                                                                                                                                                                                                                                                                                                                                                                    |
|--------------------------|-------------|------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DBG_END_SEQUENCE         | 0x00        |                                                                              | *(none)*                                                                                                                                                               | terminates a debug info sequence for a `code_item`                                                                                                                                                                                                                                                                                                                                                             |
| DBG_ADVANCE_PC           | 0x01        | uleb128 addr_diff                                                            | `addr_diff`: amount to add to address register                                                                                                                         | advances the address register without emitting a positions entry                                                                                                                                                                                                                                                                                                                                               |
| DBG_ADVANCE_LINE         | 0x02        | sleb128 line_diff                                                            | `line_diff`: amount to change line register by                                                                                                                         | advances the line register without emitting a positions entry                                                                                                                                                                                                                                                                                                                                                  |
| DBG_START_LOCAL          | 0x03        | uleb128 register_num uleb128p1 name_idx uleb128p1 type_idx                   | `register_num`: register that will contain local `name_idx`: string index of the name `type_idx`: type index of the type                                               | introduces a local variable at the current address. Either `name_idx` or `type_idx` may be `NO_INDEX` to indicate that that value is unknown.                                                                                                                                                                                                                                                                  |
| DBG_START_LOCAL_EXTENDED | 0x04        | uleb128 register_num uleb128p1 name_idx uleb128p1 type_idx uleb128p1 sig_idx | `register_num`: register that will contain local `name_idx`: string index of the name `type_idx`: type index of the type `sig_idx`: string index of the type signature | introduces a local with a type signature at the current address. Any of `name_idx`, `type_idx`, or `sig_idx` may be `NO_INDEX` to indicate that that value is unknown. (If `sig_idx` is `-1`, though, the same data could be represented more efficiently using the opcode `DBG_START_LOCAL`.) **Note:** See the discussion under "`dalvik.annotation.Signature`" below for caveats about handling signatures. |
| DBG_END_LOCAL            | 0x05        | uleb128 register_num                                                         | `register_num`: register that contained local                                                                                                                          | marks a currently-live local variable as out of scope at the current address                                                                                                                                                                                                                                                                                                                                   |
| DBG_RESTART_LOCAL        | 0x06        | uleb128 register_num                                                         | `register_num`: register to restart                                                                                                                                    | re-introduces a local variable at the current address. The name and type are the same as the last local that was live in the specified register.                                                                                                                                                                                                                                                               |
| DBG_SET_PROLOGUE_END     | 0x07        |                                                                              | *(none)*                                                                                                                                                               | sets the `prologue_end` state machine register, indicating that the next position entry that is added should be considered the end of a method prologue (an appropriate place for a method breakpoint). The `prologue_end` register is cleared by any special (`>= 0x0a`) opcode.                                                                                                                              |
| DBG_SET_EPILOGUE_BEGIN   | 0x08        |                                                                              | *(none)*                                                                                                                                                               | sets the `epilogue_begin` state machine register, indicating that the next position entry that is added should be considered the beginning of a method epilogue (an appropriate place to suspend execution before method exit). The `epilogue_begin` register is cleared by any special (`>= 0x0a`) opcode.                                                                                                    |
| DBG_SET_FILE             | 0x09        | uleb128p1 name_idx                                                           | `name_idx`: string index of source file name; `NO_INDEX` if unknown                                                                                                    | indicates that all subsequent line number entries make reference to this source file name, instead of the default name specified in `code_item`                                                                                                                                                                                                                                                                |
| *Special Opcodes*        | 0x0a...0xff |                                                                              | *(none)*                                                                                                                                                               | advances the `line` and `address` registers, emits a position entry, and clears `prologue_end` and `epilogue_begin`. See below for description.                                                                                                                                                                                                                                                                |

In a compact file there is no per-code-item `debug_info_off`; the offset of a method's sequence
comes from the "`debug_info_offsets_table`".

### Special opcodes

Opcodes with values between `0x0a` and `0xff` (inclusive) move both the `line` and `address`
registers by a small amount and then emit a new position table entry. The formula for the increments
are as follows:

```
DBG_FIRST_SPECIAL = 0x0a  // the smallest special opcode
DBG_LINE_BASE   = -4      // the smallest line number increment
DBG_LINE_RANGE  = 15      // the number of line increments represented

adjusted_opcode = opcode - DBG_FIRST_SPECIAL

line += DBG_LINE_BASE + (adjusted_opcode % DBG_LINE_RANGE)
address += (adjusted_opcode / DBG_LINE_RANGE)
```

### debug_info_offsets_table

#### Referenced from compact header_item

#### Appears in the data section

#### Alignment: 4 bytes

#### Size: variable

The table maps a method index to the offset of that method's `debug_info_item`, replacing the
per-code-item `debug_info_off` of a standard file. It holds one entry per element of `method_ids`,
in method index order; an entry value of `0` means the method has no debug info.

The table data starts at `header.debug_info_offsets_pos` and consists of two parts:

| Name   | Format                     | Description                                                                                                                                                             |
|--------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| blocks | debug_info_offsets_block[] | one block per group of 16 consecutive method indexes                                                                                                                    |
| lookup | uint32[groups]             | offset of each block, relative to the start of the table data. `header.debug_info_offsets_table_offset` is the distance from the start of the table data to this array. |

A block for its group of 16 method indexes has the following layout:

| Name    | Format             | Description                                                                                                                                                                                  |
|---------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| bitmask | ushort, big-endian | bit *i* is set when method index `group * 16 + i` has a non-zero debug info offset                                                                                                           |
| offsets | uleb128[popcount]  | one value per set bit, in increasing method index order: the difference between this offset and the offset of the previous set bit, the first difference being from `header.debug_info_base` |

Padding aligns the lookup array to four bytes. Because the offsets are deltas, they are non-negative
and `header.debug_info_base` is the smallest non-zero offset in the table. The positions of the
blocks and of the lookup array are not fixed relative to each other:
`debug_info_offsets_table_offset` locates the lookup array wherever the table writes it.

To look up method `m`, read `lookup[m / 16]`, seek to that block, test bit `m % 16` of its bitmask (bit clear yields offset `0`), decode the number of `uleb128` values equal to the count of set bits
at or below that bit, and accumulate their deltas onto `header.debug_info_base`.

Because the table has exactly one entry per method index, the format cannot carry different debug
info for two methods that share a method index.

### legacy debug_info_item (version 013)

#### Referenced from `legacy code_item`

#### Appears in the byte_data section

#### Alignment: none (byte-aligned)

#### Size: variable

The item is a byte-coded state machine over the `address`, `line`, `source_file`, `prologue_end`,
and `epilogue_begin` registers and the last local name and type of each register; the
`source_file` register starts at the value of `source_file_idx` of the owning `legacy code_item`.
The sequence begins with the header below and ends with a `DBG_END_SEQUENCE` byte.

| Name            | Format      | Description                                                                                                                                                                                     |
|-----------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| line_start      | uleb128     | the initial value for the state machine's `line` register. Does not represent an actual positions entry.                                                                                        |
| parameter_names | uleb128p1[] | one string index per method parameter, in order; an encoded `NO_INDEX` means that the parameter has no name. The count is not stored: it is the parameter count of the method's own descriptor. |

The constant opcodes are:

| Name                     | Value       | Format                                                                       | Arguments                                                                                                                                                                               | Description                                                                                                                                                                    |
|--------------------------|-------------|------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DBG_END_SEQUENCE         | 0x00        |                                                                              | *(none)*                                                                                                                                                                                | terminates the debug info sequence                                                                                                                                             |
| DBG_ADVANCE_PC           | 0x01        | uleb128 addr_diff                                                            | `addr_diff`: amount to add to the address register                                                                                                                                      | advances the address register without emitting a positions entry                                                                                                               |
| DBG_ADVANCE_LINE         | 0x02        | sleb128 line_diff                                                            | `line_diff`: amount to change the line register by                                                                                                                                      | advances the line register without emitting a positions entry                                                                                                                  |
| DBG_START_LOCAL          | 0x03        | uleb128 register_num uleb128p1 name_idx uleb128p1 type_idx                   | `register_num`: register that will contain the local `name_idx`: string index of the name `type_idx`: string index of the type descriptor                                               | introduces a local variable at the current address. Either `name_idx` or `type_idx` may be `NO_INDEX` to indicate that the value is unknown.                                   |
| DBG_END_LOCAL            | 0x04        | uleb128 register_num                                                         | `register_num`: register that contained the local                                                                                                                                       | marks a live local variable as out of scope at the current address                                                                                                             |
| DBG_RESTART_LOCAL        | 0x05        | uleb128 register_num                                                         | `register_num`: register to restart                                                                                                                                                     | re-introduces a local variable at the current address, with the name and type of the last local that was live in the register                                                  |
| DBG_SET_PROLOGUE_END     | 0x06        |                                                                              | *(none)*                                                                                                                                                                                | sets the `prologue_end` register, indicating that the next position entry marks the end of a method prologue                                                                   |
| DBG_SET_EPILOGUE_BEGIN   | 0x07        |                                                                              | *(none)*                                                                                                                                                                                | sets the `epilogue_begin` register, indicating that the next position entry marks the beginning of a method epilogue                                                           |
| DBG_START_LOCAL_EXTENDED | 0x08        | uleb128 register_num uleb128p1 name_idx uleb128p1 type_idx uleb128p1 sig_idx | `register_num`: register that will contain the local `name_idx`: string index of the name `type_idx`: string index of the type descriptor `sig_idx`: string index of the type signature | introduces a local variable with a type signature at the current address. Any of `name_idx`, `type_idx`, or `sig_idx` may be `NO_INDEX` to indicate that the value is unknown. |
| DBG_SET_FILE             | 0x09        | uleb128p1 name_idx                                                           | `name_idx`: string index of the source file name; `NO_INDEX` if unknown                                                                                                                 | makes all subsequent line number entries refer to this source file name                                                                                                        |
| *Special opcodes*        | 0x0a...0xff |                                                                              | *(none)*                                                                                                                                                                                | advances the `line` and `address` registers, emits a position entry, and clears `prologue_end` and `epilogue_begin`. See "Special opcodes" above.                              |

A local of the null type carries the descriptor string `<null>`.

### annotations_directory_item

#### Referenced from class_def_item

#### Appears in the data section

#### Alignment: 4 bytes

#### Size: 16 + 8 × (fields_size + annotated_methods_size + annotated_parameters_size) bytes

| Name                      | Format                                                       | Description                                                                                                                                                                                                                               |
|---------------------------|--------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| class_annotations_off     | uint                                                         | offset to the annotations made directly on the class, or `0` if the class has no direct annotations. The offset, if non-zero, should point into the `data` section. The format of the data is specified by "`annotation_set_item`" below. |
| fields_size               | uint                                                         | count of fields annotated by this item                                                                                                                                                                                                    |
| annotated_methods_size    | uint                                                         | count of methods annotated by this item                                                                                                                                                                                                   |
| annotated_parameters_size | uint                                                         | count of method parameter lists annotated by this item                                                                                                                                                                                    |
| field_annotations         | field_annotation[fields_size] *(optional)*                   | list of associated field annotations. The elements of the list must be sorted in increasing order, by `field_idx`.                                                                                                                        |
| method_annotations        | method_annotation[annotated_methods_size] *(optional)*       | list of associated method annotations. The elements of the list must be sorted in increasing order, by `method_idx`.                                                                                                                      |
| parameter_annotations     | parameter_annotation[annotated_parameters_size] *(optional)* | list of associated method parameter annotations. The elements of the list must be sorted in increasing order, by `method_idx`.                                                                                                            |

**Note:** All elements' `field_id` and `method_id` instances must refer to the same defining class.

### legacy annotations_directory_item

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 16 bytes

| Name                     | Format | Description                                                                                                  |
|--------------------------|--------|--------------------------------------------------------------------------------------------------------------|
| class_annotations_off    | uint   | offset to the `legacy annotation_set_item` for the annotations on the class itself, or `0` if there are none |
| annotated_fields_off     | uint   | offset to the uniform list of `legacy field_annotations_item`, or `0` if there are none                      |
| annotated_methods_off    | uint   | offset to the uniform list of `legacy method_annotations_item`, or `0` if there are none                     |
| annotated_parameters_off | uint   | offset to the uniform list of `legacy parameter_annotations_item`, or `0` if there are none                  |

No offset in the file points to this item: `annotations_off` of every `legacy class_def_item` is
`0`, so annotation data is reachable only by walking the `word_data` and `byte_data` areas.

### field_annotation format

#### Referenced from annotations_directory_item

#### Size: 8 bytes

| Name            | Format | Description                                                                                                                                                             |
|-----------------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| field_idx       | uint   | index into the `field_ids` list for the identity of the field being annotated                                                                                           |
| annotations_off | uint   | offset to the list of annotations for the field. The offset should point into the `data` section. The format of the data is specified by "`annotation_set_item`" below. |

### legacy field_annotations_item

#### Appears in a uniform list referenced from `legacy annotations_directory_item`

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name            | Format | Description                                                                  |
|-----------------|--------|------------------------------------------------------------------------------|
| field_idx       | uint   | index into the `field_ids` list for the identity of the annotated field      |
| annotations_off | uint   | offset to the `legacy annotation_set_item` for the annotations of this field |

### method_annotation format

#### Referenced from annotations_directory_item

#### Size: 8 bytes

| Name            | Format | Description                                                                                                                                                              |
|-----------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| method_idx      | uint   | index into the `method_ids` list for the identity of the method being annotated                                                                                          |
| annotations_off | uint   | offset to the list of annotations for the method. The offset should point into the `data` section. The format of the data is specified by "`annotation_set_item`" below. |

### legacy method_annotations_item

#### Appears in a uniform list referenced from `legacy annotations_directory_item`

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name            | Format | Description                                                                   |
|-----------------|--------|-------------------------------------------------------------------------------|
| method_idx      | uint   | index into the `method_ids` list for the identity of the annotated method     |
| annotations_off | uint   | offset to the `legacy annotation_set_item` for the annotations of this method |

### parameter_annotation format

#### Referenced from annotations_directory_item

#### Size: 8 bytes

| Name            | Format | Description                                                                                                                                                                             |
|-----------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| method_idx      | uint   | index into the `method_ids` list for the identity of the method whose parameters are being annotated                                                                                    |
| annotations_off | uint   | offset to the list of annotations for the method parameters. The offset should point into the `data` section. The format of the data is specified by "`annotation_set_ref_list`" below. |

### legacy parameter_annotations_item

#### Appears in a uniform list referenced from `legacy annotations_directory_item`

#### Alignment: 4 bytes

#### Size: 8 bytes

| Name            | Format | Description                                                                                    |
|-----------------|--------|------------------------------------------------------------------------------------------------|
| method_idx      | uint   | index into the `method_ids` list for the identity of the method whose parameters are annotated |
| annotations_off | uint   | offset to a `legacy annotation_set_ref_list` for the parameters of this method                 |

### annotation_set_ref_list

#### Referenced from parameter_annotations_item

#### Appears in the data section

#### Alignment: 4 bytes

#### Size: 4 + (size × 4) bytes

| Name | Format                        | Description                  |
|------|-------------------------------|------------------------------|
| size | uint                          | size of the list, in entries |
| list | annotation_set_ref_item[size] | elements of the list         |

### annotation_set_ref_item format

#### Size: 4 bytes

| Name            | Format | Description                                                                                                                                                                                                                       |
|-----------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| annotations_off | uint   | offset to the referenced annotation set or `0` if there are no annotations for this element. The offset, if non-zero, should point into the `data` section. The format of the data is specified by "`annotation_set_item`" below. |

### legacy annotation_set_ref_list

#### Referenced from `legacy parameter_annotations_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 4 + (count × 4) bytes

| Name  | Format                                | Description                                 |
|-------|---------------------------------------|---------------------------------------------|
| count | uint                                  | number of entries, one per method parameter |
| list  | legacy annotation_set_ref_item[count] | the elements of the list                    |

### legacy annotation_set_ref_item format

#### Size: 4 bytes

| Name            | Format | Description                                                                        |
|-----------------|--------|------------------------------------------------------------------------------------|
| annotations_off | uint   | offset to the `legacy annotation_set_item` with the annotations for that parameter |

### annotation_set_item

#### Referenced from annotations_directory_item, field_annotations_item, method_annotations_item, and annotation_set_ref_item

#### Appears in the data section

#### Alignment: 4 bytes

#### Size: 4 + (size × 4) bytes

| Name    | Format                    | Description                                                                          |
|---------|---------------------------|--------------------------------------------------------------------------------------|
| size    | uint                      | size of the set, in entries                                                          |
| entries | annotation_off_item[size] | elements of the set. The elements must be sorted in increasing order, by `type_idx`. |

### annotation_off_item format

#### Size: 4 bytes

| Name           | Format | Description                                                                                                                                                      |
|----------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| annotation_off | uint   | offset to an annotation. The offset should point into the `data` section, and the format of the data at that location is specified by "`annotation_item`" below. |

### legacy annotation_set_item

#### Referenced from `legacy annotations_directory_item`, `legacy field_annotations_item`,

`legacy method_annotations_item`, and `legacy annotation_set_ref_item`

#### Appears in the word_data section

#### Alignment: 4 bytes

#### Size: 4 + (size × 4) bytes

| Name    | Format     | Description                                                                                            |
|---------|------------|--------------------------------------------------------------------------------------------------------|
| size    | uint       | size of the set, in entries                                                                            |
| entries | uint[size] | offsets to `legacy annotation_item`, sorted by the index of the annotation type in the `type_ids` list |

### annotation_item

#### Referenced from annotation_set_item

#### Appears in the data section

#### Alignment: none (byte-aligned)

#### Size: variable

| Name       | Format             | Description                                                                                                                   |
|------------|--------------------|-------------------------------------------------------------------------------------------------------------------------------|
| visibility | ubyte              | intended visibility of this annotation (see below)                                                                            |
| annotation | encoded_annotation | encoded annotation contents, in the format described by "`encoded_annotation` format" under "`encoded_value` encoding" above. |

### Visibility values

These are the options for the `visibility` field in an `annotation_item`:

| Name               | Value | Description                                                                                         |
|--------------------|-------|-----------------------------------------------------------------------------------------------------|
| VISIBILITY_BUILD   | 0x00  | intended only to be visible at build time (e.g., during compilation of other code)                  |
| VISIBILITY_RUNTIME | 0x01  | intended to be visible at runtime                                                                   |
| VISIBILITY_SYSTEM  | 0x02  | intended to be visible at runtime, but only to the underlying system (and not to regular user code) |

### legacy annotation_item

#### Referenced from `legacy annotation_set_item`

#### Appears in the byte_data section

#### Alignment: none (byte-aligned)

#### Size: variable

| Name       | Format                             | Description                                                                 |
|------------|------------------------------------|-----------------------------------------------------------------------------|
| visibility | ubyte                              | `1` (runtime) in every item                                                 |
| type_idx   | uleb128                            | index into the `type_ids` list for the type of the annotation               |
| size       | uleb128                            | number of name-value pairs                                                  |
| elements   | (uleb128 `name_idx` + value)[size] | the elements of the annotation: one string index and one encoded value each |

### legacy annotation value encoding

#### Embedded in `legacy annotation_item`

Each value starts with a tag byte:

| Tag byte             | Followed by                                                 | Meaning                                                 |
|----------------------|-------------------------------------------------------------|---------------------------------------------------------|
| `0x80 \| (v & 0x7f)` | *(nothing)*                                                 | signed literal that fits in 7 bits (range `-64`...`63`) |
| `0x30 + n`           | `n` bytes, little-endian                                    | signed literal of `n` bytes, with `n` in `1`...`8`      |
| `'s'` (`0x73`)       | uleb128 string index                                        | a string value                                          |
| `'t'` (`0x74`)       | uleb128 type index                                          | a type value                                            |
| `'e'` (`0x65`)       | uleb128 field index                                         | an enumerated type constant                             |
| `'['` (`0x5b`)       | uleb128 size, then `size` values                            | an array value                                          |
| `'@'` (`0x40`)       | an encoded annotation: `type_idx`, `size`, and the elements | a sub-annotation                                        |

There are no tags for method references, `null`, or booleans; a boolean is encoded as an integer
literal.

### encoded_array_item

#### Referenced from class_def_item

#### Appears in the data section

#### Alignment: none (byte-aligned)

#### Size: variable

| Name  | Format        | Description                                                                                                                             |
|-------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| value | encoded_array | bytes representing the encoded array value, in the format specified by "`encoded_array` Format" under "`encoded_value` Encoding" above. |

### hiddenapi_class_data_item

#### Appears in the data section

#### Referenced from map_list

#### Alignment: 4 bytes

#### Size: variable

This item holds the hidden API flags of the classes on the boot class path: one value per field and
method, at most one item per file, located only through the map entry
`TYPE_HIDDENAPI_CLASS_DATA_ITEM` (`0xF000`, count 1); without that entry every member of the file is
in state `sdk`.

| Name    | Format                | Description                                                                                                                                                                                                          |
|---------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| size    | uint                  | total size of the item in bytes, including the `offsets` table                                                                                                                                                       |
| offsets | uint[class_defs_size] | one entry per class definition, in `class_defs` order: the offset from the start of this item to the class's first value, or `0` when the class has no values, which leaves every member of the class in state `sdk` |
| values  | uleb128[]             | concatenated per-class streams, one value per field and method of the class, in class data order                                                                                                                     |

The table is indexed by the position of a class in `class_defs`, not by `class_idx`:

```
item_start + 0                size
item_start + 4                offsets[0]
...                           ...
item_start + 4 * n            offsets[n - 1]      (n = class_defs_size)
item_start + 4 * (n + 1)      first value of the first class with a non-zero offset
...                           further streams, in class definition order
item_start + size             end of the item
```

A non-zero stream holds `static_fields_size` + `instance_fields_size` + `direct_methods_size` +
`virtual_methods_size` values, the k-th value belonging to the k-th member of the class.

- `size` is at least `4 * (class_defs_size + 1)`.
- The first non-zero offset is `4 * (class_defs_size + 1)`, and each later stream starts immediately
  after the previous one: no gap, no overlap.
- An entry is zero exactly when the class has no values, and a class without class data has a zero
  entry.

The item appeared in Android 10 (API 29) as a section of version `039` files without a dex version
bump. The restriction data itself is older: Android 9 (API 28) carries it in a simplified form
inside the class data, also without a version bump — two bits of the `access_flags` of a member, bit
0 as an inversion of the visibility flags and bit 1 at `0x20` (at `0x200` for a `native`
method), which select `whitelist` (`0`), light greylist (`1`), dark greylist (`2`), and `blacklist`
(`3`), the predecessors of `sdk`, `unsupported`, `max-target-o`, and `blocked`.

### Hidden API flag values

A value is the unsigned LEB128 encoding of a 32-bit word:

| Bits | Field               | Mask   | Meaning                                             |
|------|---------------------|--------|-----------------------------------------------------|
| 0–3  | restriction list    | `0x0f` | one of the eight values below                       |
| 4    | `core-platform-api` | `0x10` | the member belongs to the core platform API surface |
| 5    | `test-api`          | `0x20` | the member belongs to the test API surface          |
| 6–31 | —                   | —      | reserved                                            |

| Value | Former name    | Meaning                                                                 |
|-------|----------------|-------------------------------------------------------------------------|
| 0     | `sdk`          | documented in the SDK package index and supported                       |
| 1     | `unsupported`  | non-SDK; usable regardless of the target API level                      |
| 2     | `blocked`      | non-SDK; not usable at any target API level                             |
| 3     | `max-target-o` | non-SDK; usable while an application targets up to Android 8.1 (API 27) |
| 4     | `max-target-p` | non-SDK; usable while an application targets up to Android 9 (API 28)   |
| 5     | `max-target-q` | non-SDK; usable while an application targets up to Android 10 (API 29)  |
| 6     | `max-target-r` | non-SDK; usable while an application targets up to Android 11 (API 30)  |
| 7     | `max-target-s` | non-SDK; usable while an application targets up to Android 12 (API 31)  |

list fields `0x8`–`0xE` are unassigned and `0xF` is invalid.

v1 (up to Android 11 / API 30): three-bit list, `core-platform-api` = `0x8`, `test-api` = `0x10`; v2 (from Android 12 / API 31): four-bit list, `core-platform-api` = `0x10`, `test-api` = `0x20`.

## System annotations

System annotations are used to represent various pieces of reflective information about classes (and
methods and fields). This information is generally only accessed indirectly by client (non-system)
code.

System annotations are represented in `.dex` files as annotations with visibility set to
`VISIBILITY_SYSTEM`.

### dalvik.annotation.AnnotationDefault

#### Appears on methods in annotation interfaces

An `AnnotationDefault` annotation is attached to each annotation interface which wishes to indicate
default bindings.

| Name  | Format     | Description                                                                                                                                                                                          |
|-------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| value | Annotation | the default bindings for this annotation, represented as an annotation of this type. The annotation need not include all names defined by the annotation; missing names simply do not have defaults. |

### dalvik.annotation.EnclosingClass

#### Appears on classes

An `EnclosingClass` annotation is attached to each class which is either defined as a member of
another class, per se, or is anonymous but not defined within a method body (e.g., a synthetic inner
class). Every class that has this annotation must also have an `InnerClass` annotation.
Additionally, a class must not have both an `EnclosingClass` and an `EnclosingMethod` annotation.

| Name  | Format | Description                                              |
|-------|--------|----------------------------------------------------------|
| value | Class  | the class which most closely lexically scopes this class |

### dalvik.annotation.EnclosingMethod

#### Appears on classes

An `EnclosingMethod` annotation is attached to each class which is defined inside a method body.
Every class that has this annotation must also have an `InnerClass` annotation. Additionally, a
class must not have both an `EnclosingClass` and an `EnclosingMethod` annotation.

| Name  | Format | Description                                               |
|-------|--------|-----------------------------------------------------------|
| value | Method | the method which most closely lexically scopes this class |

### dalvik.annotation.InnerClass

#### Appears on classes

An `InnerClass` annotation is attached to each class which is defined in the lexical scope of
another class's definition. Any class which has this annotation must also have *either* an
`EnclosingClass` annotation *or* an `EnclosingMethod` annotation.

| Name        | Format | Description                                                                                                                                                                                        |
|-------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name        | String | the originally declared simple name of this class (not including any package prefix). If this class is anonymous, then the name is `null`.                                                         |
| accessFlags | int    | the originally declared access flags of the class (which may differ from the effective flags because of a mismatch between the execution models of the source language and target virtual machine) |

### dalvik.annotation.MemberClasses

#### Appears on classes

A `MemberClasses` annotation is attached to each class which declares member classes. (A member
class is a direct inner class that has a name.)

| Name  | Format  | Description                 |
|-------|---------|-----------------------------|
| value | Class[] | array of the member classes |

### dalvik.annotation.MethodParameters

#### Appears on methods

A `MethodParameters` annotation is optional and provides parameter metadata such as parameter names
and modifiers. It may be omitted when that metadata is not needed, and it describes only the
individual parameters, so a constructor or method without parameters needs none.

A `MethodParameters` annotation describes all formal method parameters, including those not
explicitly or implicitly declared in source code, so its arrays may be longer than the parameter
list of a signature and it carries no type annotation receiver parameters.

The arrays must be the same size as the parameter list of the associated method, that is
`method_id_item.proto_idx` → `proto_id_item.parameters_off` → `type_list.size` must equal
`names().length` and `accessFlags().length`.

| Name        | Format   | Description                                                                                                                                                                                                                                                                                                                                                                                                                           |
|-------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| names       | String[] | the names of formal parameters for the associated method. The array must not be null but must be empty if there are no formal parameters. A value in the array must be null if the formal parameter with that index has no name. A name must not be empty and must not contain `'.'`, `';'`, `'['`, or `'/'`.                                                                                                                         |
| accessFlags | int[]    | the access flags of the formal parameters for the associated method. The array must not be null but must be empty if there are no formal parameters. The value is a bit mask of `0x0010` (final: the parameter was declared final), `0x1000` (synthetic: the parameter was introduced by the compiler), and `0x8000` (mandated: the parameter is synthetic but also implied by the language specification). No other bits may be set. |

### dalvik.annotation.Signature

#### Appears on classes, fields, and methods

A `Signature` annotation is attached to each class, field, or method which is defined in terms of a
more complicated type than is representable by a `type_id_item`. The `.dex` format does not define
the format of signatures; it is merely meant to be able to represent whatever signatures a source
language requires for successful implementation of that language's semantics, so a signature is
passed on to higher-level APIs and tools as opaque text.

Because signature strings tend to have a lot of duplicated content, a `Signature` annotation is
defined as an *array* of strings, where duplicated elements naturally refer to the same underlying
data, and the signature is taken to be the concatenation of all the strings in the array. There are
no rules about how to pull apart a signature into separate strings; that is entirely up to the tools
that generate `.dex` files.

| Name  | Format   | Description                                                                                       |
|-------|----------|---------------------------------------------------------------------------------------------------|
| value | String[] | the signature of this class or member, as an array of strings that is to be concatenated together |

### dalvik.annotation.Throws

#### Appears on methods

A `Throws` annotation is attached to each method which is declared to throw one or more exception
types.

| Name  | Format  | Description                         |
|-------|---------|-------------------------------------|
| value | Class[] | the array of exception types thrown |

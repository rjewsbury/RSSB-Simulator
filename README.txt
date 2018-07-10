==================
~~RSSB Simulator~~
==================

RSSB is a URISC instruction that functions with only one argument
https://esolangs.org/wiki/RSSB

The compiler converts assembly-like .src files into RSSB instructions

The assembler converts RSSB into machine code, and handles the conversion between
labels and physical addresses.

The simulator models the process of an RSSB computer on physical hardware

This simulator does not handle I/O, instead using pre-loaded values in RAM
as input, and displaying the contents of RAM as output. In the future,
negative address space could potentially be used as memory-mapped I/O

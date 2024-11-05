asect 0 
main: ext               # Declare labels 
default_handler: ext    # as external 
 
# Interrupt vector table (IVT) 
# Place a vector to program start and 
# map all internal exceptions to default_handler 
dc main, 0              # Startup/Reset vector 
dc default_handler, 0   # Unaligned SP 
dc default_handler, 0   # Unaligned PC 
dc default_handler, 0   # Invalid instruction 
dc default_handler, 0   # Double fault 
align 0x16              # Reserve space for the rest  
                        # of IVT 
 
# Exception handlers section 
rsect exc_handlers 
 
# This handler halts processor 
default_handler> 
    halt 
 
# Main program section 
rsect main

main>
	ldi r6, 0
	push 0
	push 1
	ldi r0, 0xFFFE
	ldi r1, 0xFFFC
	ldi r4, 22

	while
		tst r4
	stays nz
		dec r4
		ldw r0, r2
		ldw r1, r3
		add r2, r3
		push r3
		dec r0
		dec r0
		dec r1
		dec r1
	wend

	ldi r1, fib
	ld r1, r2
	pop r0

	while
		cmp r2, r0
	stays eq
		pop r0
		inc r1
		inc r1
		ld r1, r2
	wend

	if
		tst r2
	is z
		ldi r6, 1
	fi
	
INPUTS>
	fib: dc 0x6ff1, 0x452f, 0x2ac2, 0x1a6d, 0x1055, 0x0a18, 0x063d, 0x03db, 0x0262, 0x0179, 0x00e9, 0x0090, 0x0059, 0x0037, 0x0022, 0x0015, 0x000d, 0x0008, 0x0005, 0x0003, 0x0002, 0x0001, 0x0001, 0x0000, 0x0000

end

end.
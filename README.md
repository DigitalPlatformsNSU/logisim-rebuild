# Logism
Logisim is an educational tool for designing and simulating digital logic circuits. With its simple toolbar interface and simulation of circuits as you build them, it is simple enough to facilitate learning the most basic concepts related to logic circuits. With the capacity to build larger circuits from smaller subcircuits, and to draw bundles of wires with a single mouse drag, Logisim can be used (and is used) to design and simulate entire CPUs for educational purposes.
Logisim is used by students at colleges and universities around the world in many types of classes, ranging from a brief unit on logic in general-education computer science surveys, to computer organization courses, to full-semester courses on computer architecture.
# Feachers
1. It runs on any machine supporting Java 21 or later; special versions are released for MacOS X and Windows. The cross-platform nature is important for students who have a variety of home/dorm computer systems.
2. The drawing interface is based on an intuitive toolbar. Color-coded wires aid in simulating and debugging a circuit.
3. The wiring tool draws horizontal and vertical wires, automatically connecting to components and to other wires. It's very easy to draw circuits!
4. Completed circuits can be saved into a file, exported to a GIF file, or printed on a printer.
5. Circuit layouts can be used as "subcircuits" of other circuits, allowing for hierarchical circuit design.6
6. Included circuit components include inputs and outputs, gates, multiplexers, arithmetic circuits, flip-flops, and RAM memory.
7. The included "combinational analysis" module allows for conversion between circuits, truth tables, and Boolean expressions.
# Deployment
1. Install Gradle 8.8 or later (https://gradle.org/install/)
2. Install Java 21 or later (https://www.oracle.com/de/java/technologies/downloads/)
3. Run:

       gradle assemble
       gradle run

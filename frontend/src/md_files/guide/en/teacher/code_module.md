## Code module tab

### Code module types

1) **Showcase**: This option is useful if you wish to allow the students to run uneditable code with various inputs
and to view the respective outputs.
2) **Standard I/O test**: In this type, the student must write code that reads from the standard input, and outputs to
standard output *(similar to a progtest "data generator" assignment)*.
3) **Assert test**: Here, the student usually implements a function that takes some input parameters and must return or output
specific values *(similar to a progtest "testbed" assignment)*.
4) **Write asserts**: Here, the student usually writes a function that uses asserts to test a provided function's implementation.
The student's asserts must pass for a correct implementation and catch an incorrect one.

### Libraries

#### C Library
The C library is available with `#include "lib/c/trainer.h"` and has the following interface:
```c++
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <time.h>

namespace CTeacher {
    CTesterString ( const char * str, bool free = false );
    CTesterString ( char * str, bool free = true );
    #define swrap(str) (CTesterString(str, false))
    #define STR_EMPTY ((const char *) NULL)
    
    class CTester {
    public:
    static FILE * test_stdin;
    static FILE * test_stdout;
    static int assertBool ( int condition, CTesterString input, CTesterString output = STR_EMPTY, CTesterString ref = STR_EMPTY );
    static int assertEqual ( CTesterString input, CTesterString output = STR_EMPTY, CTesterString ref = STR_EMPTY );
    static int assertEqual ( CTesterString input, int output, int ref );
    static void outputPrint ( char * input, char * output );
    static int compareLines ( const char * l, const char * r );
    };
    
    template<typename T, typename R, typename... Args>
    char * generateOutput ( const char * in, size_t szOut, T function, R * retPtr, Args... args);
    
    template<typename T, typename... Args>
    char * generateOutput ( const char * in, size_t szOut, T function, Args... args);
}
```
`CTesterString` is a wrapper class for a C string. If its constructor is provided a `char*`, its destructor will free this pointer.
If its constructor is instead called with `const char*`, it will not be freed. The macro `swrap(str)` can be used to pass a `char*`
without having it freed.

`assertBool` asserts that `condition` is not equal to 0.\
`assertEqual` asserts that `output` and `ref` are equal.\
It is recommended to use these functions for asserts in your tester code. The number of calls of these functions
will be displayed as the tests passed number to students. Should a test fail, the input, output and correct output (ref)
will be displayed to the student.

`compareLines` compares lines of `l` and `r` irrespective of the order in which they come. Should the test fail,
the student will see either that their solution had the wrong number of lines, or the first line that differed.

`outputPrint` is best used in showcases, it shows a single passed test with the input and output as provided.

`generateOutput` initializes `test_stdin`'s contents with `in`, and links `test_stdout` to a dynamically allocated buffer of size `szOut`.
Then it calls `function(args...)`, placing its return value into retPtr if provided.\
If used in conjunction with the `C code, redirected I/O` code envelope, upon reading from standard input, `function` will instead
read from `in`. Upon writing to standard output, the return buffer will be written to instead. Please inspect this code envelope
before attempting to use a custom one in conjunction with `generateOutput`.

#### C++ Library
The C++ library, available with `#include "lib/cpp/trainer.hpp"` has a very similar interface, improved with C++ features and standard classes.

```c++
#include <cassert>
#include <string>
#include <set>
#include <iostream>
#include <sstream>

namespace CTeacher {
    class CTester {
    public:
        static std::istringstream test_stdin;
        static std::ostringstream test_stdout;
        static bool assertBool ( bool condition, const std::string & input, const std::string & output = "", const std::string & ref = "" );
        static bool assertEqual ( const std::string & input, const std::string & output = "", const std::string & ref = "" );
        template<typename T>
        static bool assertEqual ( const std::string & input, const T & out, const T & ref );
        static void outputPrint ( const std::string & input, const std::string & output );
        static bool compareLines ( const std::string & l, const std::string & r );
    };
    
    template<typename T, typename R, typename... Args>
    std::string generateOutput ( const std::string & in, T function, R & retRef, Args... args);
    template<typename T, typename... Args>
    std::string generateOutput ( const std::string & in, T function, Args... args);
}
```
C++ standard library streams replace `FILE*`. `std::string` replaces CTesterString.\
`assertEqual` now makes use of templates. `out` and `ref` are compared using `std::ostream` and the `<<` operator.

`generateOutput` now doesn't have any envelope requirements. It will however only work correctly if the student uses
`std::cout` and `std::cin` (for example `printf()` will not be redirected properly).

### Automatic generation
Upon selecting a different code module type, you will be asked if you wish to have default settings generated for you.
The default settings will be based on the selected code module type, and on the choice of library. The default tester codes
include some example usages of the library functions.

## Tests
The test's name and description will always be displayed to students. The parameter is how the tester code and individual tests
are linked. A single test is equivalent to one execution of the tester code, with the parameter as a command line argument.
The automatically generated tester codes demonstrate a simple way to map individual tests to functions in the tester code.

Beware of setting the Check memory access option to true, as especially if using the C library, the library functions may allocate
buffers of their own.

The Test should fail setting is only available for the Write asserts code module type. Use this setting if the test corresponds to an
incorrect implementation of the tested function.

## Code files
These will be the default files that students will see when they first open the module. You may fill in a reference solution to what the
file should look like if you wish.

## Code envelope
The student's code will always be run inside of this envelope. `__STUDENT_NAMESPACE__` corresponds to `CStudent` or `CRef`,
`__STUDENT_FILE__` is substituted for the contents of the file. You may use a custom envelope to enable or disable includes as necessary,
or define types or function signatures that you want students to use.
Refer to the default options for examples.

## Tester code
This code is run to test the student's solution, once per each test with the test's parameter as a command line argument.
Refer to the automatically generated tester codes for examples.

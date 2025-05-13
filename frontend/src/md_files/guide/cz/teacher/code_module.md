## Záložka kódový modul

### Druhy kódového modulu


 1. **Ukázka kódu**: Varianta slouží, pokud chcete studentům umožnit spuštění předdefinovaného neupravitelného kódu s různými vstupy a možností si zobrazit výstupy, například pro ukázku, co udělá kód `scanf("%d")`, pokud do něj pošlete vstup `ahoj`, `10` nebo prázdný vstup.
 2. **Standardní I/O test**: v této variantě je cílem studenta napsat kód, který typicky něco čte ze vstupu a vypisuje na výstup _(jedná se tedy o běžný test s "generátorem dat" z progtestu)_.
 3. **Assertový test**: v této variantě student typicky píše funkci, která dostane nějaké vstupní parametry a musí něco vypsat nebo vrátit _(jedná se tedy o běžný test s "testbedem" z progtestu)_.
 4. **Napiš asserty**: v této variantě má student za úkol napsat funkci `testXXX`, která s pomocí assertů otestuje zadanou funkci `xxx`. Studentovu programu jsou pak přikládány různé implementace této funkce a je kontrolováno, jestli asserty vhodně odhalí chybu.

### Knihovny

#### C Knihovna
C knihovna si můžete přilinkovat pomocí `#include "lib/c/trainer.h"`. Její rozhraní je následující:
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
`CTesterString` je pomocná třída obalující tradiční C řetězec. Její konstrukor přijímá jak `char*`, `const char*`. V prvním případě je v destruktoru pointer uvolněn, v druhém není. Makro `swrap(str)` můžete použít, pokud potřebujete využít konstruktor `char*`, ale nechcete, aby byl pointer uvolněn automaticky.

`assertBool` assertuje, že `condition` se nerovná 0.\
`assertEqual` assertuje, že se `output` a `ref` rovnají.\
Je doporučeno používat tyto funkce pro asserty ve vašem kódu. Počet volání těchto funkcí bude zobrazen studentům jako počet úspěšných testů. Pokud test selže, `input`, `output` a `ref` budou zobrazeny studentovi.

`compareLines` srovnává řádky `l` a `r` bez ohledu na jejich pořadí. Pokud test selže, student uvidí buď že měl jeho výstup jiný počet řádků, nebo první řádek odlišný od správného výstupu.

Funkce `outputPrint` je vhodná pro ukázky, zobrazí jeden úspěšný test, se vstupem a výstupem podle argumentů.

`generateOutput` inicializuje `test_stdin` s obsahem `in` a naváže `test_stdout` na dynamicky alokovaný buffer o velikosti `szOut`.
Poté zavolá `function(args...)`, a ve verzi s `retPtr` vloží její návratovou hodnotu do tohoto pointeru (`*retPtr = function(args...);`).
Nakonec resetuje `test_stdin` a `test_stdout`, a návratovou hodnotou je výstupový buffer.\
V kombinací s obálkou `C kód, přesměrované I/O` zajišťuje tato funkce, že studentův program dostane na standardním vstupu obsah `in` a to, co jeho program vypíše na standardní výstup, bude funkcí vráceno. Pokud chcete tuto funkci využívat spolu s vlastní obálkou, prohlédněte si nejprve, jak přesně funguje obálka `C kód, přesměrované I/O`.

#### C++ Knihovna
C++ knihovna je dostupná s použitím `#include "lib/cpp/trainer.hpp"` a má velice podobné rozhraní, vylepšené o použití featur C++ a standardních tříd.

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
Standardní C++ streamy nahrazují `FILE*`. `std::string` nahrazuje CTesterString.\
`assertEqual` nyní používá templaty. `out` a `ref` jsou porovnávány pomocí `std::ostream` a operátoru `<<`.

`generateOutput` nyní nepožaduje žádný specifický kód v obálce. Bude však pracovat správně pouze v případě použití
`std::cout` a `std::cin` (například `printf()` nebude správně přesměrováno).

### Automatické generování
Po výběru jiného typu kódového modulu dostanete možnost si nechat vygenerovat výchozí nastavení.
Výchozí nastavení bude vycházet z vybraného typu kódového modulu a z výběru knihovny. Výchozí kódy testeru
obsahují několik příkladů použití funkcí knihovny.

## Testy
Studentům se vždy zobrazí název a popis testu. Parametr je způsob, kterým jsou propojeny jednotlivé testy a kód testeru. Jeden test odpovídá jednomu spuštění kódu testeru, přičemž toto spuštění dostane parametr jako argument z příkazové řádky.
Automaticky generované kódy testeru ukazují jednoduchý způsob, jak jednotlivé testy přiřadit k funkcím v kódu testeru.

Nastavení kontroly práce s pamětí používejte s rozvahou, protože zejména při použití knihovny C mohou funkce knihovny alokovat
buffery, kvůli kterým mohou testy neprocházet, přestože student nikde chybu neudělal.

Nastavení program má selhat je k dispozici pouze pro typ Napiš asserty. Toto nastavení použijte v případě, že test odpovídá
nesprávné implementaci testované funkce.

## Kódové soubory
Jedná se o výchozí soubory, které studenti dostanou při prvním otevření modulu. Můžete vyplnit referenční řešení, jak by měl soubor vypadat.

## Obálka kódu
Kód studenta bude vždy spuštěn uvnitř této obálky. `__STUDENT_NAMESPACE__` odpovídá `CStudent` nebo `CRef`,
`__STUDENT_FILE__` je nahrazen obsahem souboru. Vlastní obálku můžete použít k povolení nebo zakázání direktiv `#include` podle potřeby,
nebo pro definici typů či signatury funkcí, které chcete, aby studenti používali.
Při tvorbě vlastní obálky se můžete nechat inspirovat předvytvořenými možnostmi.

## Kód testeru
Studentovo řešení je testováno spouštěním tohoto kódu, jednou pro každý test, s parametrem daného testu jako argumentem z příkazové řádky.
Příklady naleznete v automaticky generovaných kódech testeru.

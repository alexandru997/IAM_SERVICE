# Branch: 5-23-Exceptions-Handling

## Metadata
- **Branch Name:** 5-23-Exceptions-Handling
- **Created From:** 5-22-DTO-Servoce-Mapping
- **Type:** Feature Implementation
- **Status:** Merged
- **Commit Hash:** 85d11beaf54891d7423217a0e3cba69bf60c3570

## Descriere Generala

Acest branch introduce mecanismul de **Global Exception Handling** in aplicatie prin intermediul pattern-ului **@ControllerAdvice** din Spring. Implementarea adauga un layer centralizat pentru gestionarea exceptiilor care asigura ca toate erorile din aplicatie sunt tratate uniform si returneaza raspunsuri consistente catre client.

Pana in acest moment, exceptiile aruncate in service (precum `NotFoundException`) nu erau prinse nicaieri, rezultand in stack trace-uri complete expuse catre client si status code-uri generice de 500 Internal Server Error. Acest branch rezolva aceasta problema prin:

- Crearea clasei `CommonControllerAdvice` care intercepteaza toate exceptiile
- Adaugarea de constante pentru formatarea si colorarea log-urilor
- Implementarea unei metode custom de logging a stack trace-urilor
- Standardizarea raspunsurilor de eroare

Aceasta implementare asigura ca toate exceptiile neprinse din controller-e, service-uri si repository-uri sunt gestionate centralizat, oferind o experienta consistenta pentru client si log-uri detaliate pentru dezvoltatori.

## Probleme Rezolvate

### 1. Lipsa Gestionarii Centralizate a Exceptiilor

**Problema Initiala:**

In branch-ul anterior (5-22-DTO-Servoce-Mapping), s-a introdus exceptia `NotFoundException` care era aruncata atunci cand un post nu era gasit:

```java
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    @Override
    public IamResponse<PostDTO> getById(@NotNull Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(
                    ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(postId)));
        // ... mapping si return
    }
}
```

**Ce se intampla fara Exception Handler:**

Cand exceptia e aruncata, nu exista un mecanism de prindere, deci Spring o trateaza ca exceptie nehandlata:

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "timestamp": "2025-10-03T20:39:09.123+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Post cu ID 999 nu a fost gasit",
  "path": "/posts/999",
  "trace": "com.post_hub.iam_Service.model.exeption.NotFoundException: Post cu ID 999 nu a fost gasit
    at com.post_hub.iam_Service.service.impl.PostServiceImpl.getById(PostServiceImpl.java:21)
    at com.post_hub.iam_Service.controller.PostController.getPostById(PostController.java:25)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    ... 50+ linii de stack trace
}
```

**Probleme Multiple:**

1. **Status Code Incorect:**
   - Returneaza 500 (Internal Server Error)
   - Ar trebui sa fie 404 (Not Found) pentru cazul acesta
   - 500 sugereaza o problema a serverului, nu a clientului

2. **Expunerea Stack Trace-ului:**
   - Stack trace-ul complet e vizibil pentru client
   - Expune structura interna a aplicatiei
   - Risc de securitate - poate dezvalui informatii sensibile
   - Mesaj confuz pentru utilizatori non-tehnici

3. **Inconsistenta Raspunsurilor:**
   - Raspunsul de succes: `IamResponse<PostDTO>` (format custom)
   - Raspunsul de eroare: format Spring default (inconsistent)
   - Client-ul trebuie sa trateze doua formate diferite

4. **Logging Ineficient:**
   - Log-urile Spring default sunt verbose
   - Greu de filtrat ce e relevant pentru debugging
   - Lipsesc informatii contextuale

**Solutia Implementata:**

Introducerea unui `@ControllerAdvice` care intercepteaza toate exceptiile:

```java
@Slf4j
@ControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler
    @ResponseBody
    protected ResponseEntity<String> handleException(Exception ex) {
        logStackTrace(ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    private void logStackTrace(Exception ex) {
        // Logare customizata cu colorare si filtrare
    }
}
```

**Rezultat:**

Acum cand se arunca `NotFoundException`, raspunsul devine:

```
HTTP/1.1 404 Not Found
Content-Type: text/plain

Post cu ID 999 nu a fost gasit
```

**Avantaje:**

1. **Status Code Corect:** 404 Not Found
2. **Raspuns Clean:** Doar mesajul de eroare, fara stack trace
3. **Logging Customizat:** Log-uri colorate si filtrate in server
4. **Securitate Imbunatatita:** Nu se expune structura interna

### 2. Logging Neoptimizat pentru Debugging

**Problema:**

Log-urile Spring default pentru exceptii sunt extrem de verbose si contin multe informatii irelevante:

```
2025-10-03 20:39:09.123 ERROR 12345 --- [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is com.post_hub.iam_Service.model.exeption.NotFoundException: Post cu ID 999 nu a fost gasit] with root cause

com.post_hub.iam_Service.model.exeption.NotFoundException: Post cu ID 999 nu a fost gasit
    at com.post_hub.iam_Service.service.impl.PostServiceImpl.getById(PostServiceImpl.java:21)
    at com.post_hub.iam_Service.controller.PostController.getPostById(PostController.java:25)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190)
    at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138)
    ... 50+ linii de framework stack trace
```

**Probleme:**

1. **Prea Mult Zgomot:**
   - Stack trace-ul contine 50+ linii
   - Majoritatea sunt din framework-uri (Spring, Reflection, Tomcat)
   - Greu de gasit linia relevanta din codul aplicatiei

2. **Lipsa Vizibilitatii:**
   - Log-urile se pierd intre alte mesaje
   - Nu exista diferentiere vizuala (culori)
   - Dificil de identificat erorile rapid

3. **Informatii Redundante:**
   - Servlet path, context path - nu ajuta la debugging
   - Nested exception messages - duplicate
   - Metadata irelevante

**Solutia Implementata:**

O metoda custom de logging care:
- Filtreaza stack trace-ul la doar pachetele aplicatiei
- Adauga colorare (rosu pentru erori)
- Formateaza clar clasa, metoda si linia

```java
private void logStackTrace(Exception ex) {
    StringBuilder stackTrace = new StringBuilder();

    // 1. Incepe cu culoarea rosie
    stackTrace.append(ApiConstants.ANSI_RED);

    // 2. Adauga mesajul exceptiei
    stackTrace.append(ex.getMessage()).append(ApiConstants.BREAK_LINE);

    // 3. Adauga cauza (daca exista)
    if (Objects.nonNull(ex.getCause())) {
        stackTrace.append(ex.getCause().getMessage()).append(ApiConstants.BREAK_LINE);
    }

    // 4. Filtreaza si adauga doar stack trace-ul aplicatiei
    Arrays.stream(ex.getStackTrace())
            .filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
            .forEach(st -> stackTrace
                    .append(st.getClassName())
                    .append(".")
                    .append(st.getMethodName())
                    .append(" (")
                    .append(st.getLineNumber())
                    .append(") ")
            );

    // 5. Reseteaza culoarea si logeaza
    log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
}
```

**Rezultat:**

Log-ul devine concis si colorat:

```
[ROSU]Post cu ID 999 nu a fost gasit
com.post_hub.iam_Service.service.impl.PostServiceImpl.getById (21)
com.post_hub.iam_Service.controller.PostController.getPostById (25)[ALB]
```

**Avantaje:**

1. **Concis:** Doar 3 linii in loc de 50+
2. **Relevant:** Doar codul aplicatiei, fara framework
3. **Vizibil:** Colorat in rosu pentru atentie imediata
4. **Formatat:** Clasa.metoda (linia) - usor de navigat

**NOTA IMPORTANTA - BUG IN IMPLEMENTARE:**

Exista o problema in implementare la linia 41:

```java
.filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
```

Unde `TIME_ZONE_PACKAGE_NAME = "java.time.zone"`.

Aceasta constanta este **GRESITA** - ar trebui sa fie numele pachetului aplicatiei:

```java
public static final String APP_PACKAGE_NAME = "com.post_hub";
```

**Implicatii:**

Cu valoarea curenta, filtrul va cauta doar clase din pachetul `java.time.zone`, care:
- Nu exista in stack trace-ul exceptiilor aplicatiei
- Rezulta intr-un log aproape gol (doar mesajul, fara stack trace)

**Solutie:**

In implementari viitoare, trebuie corectat la:

```java
.filter(st -> st.getClassName().startsWith("com.post_hub"))
```

Sau mai bine, definit ca:

```java
public static final String APP_PACKAGE_NAME = "com.post_hub";
```

Aceasta problema demonstreaza importanta testarii riguroase a exception handling-ului.

### 3. Lipsa Standardizarii Raspunsurilor de Eroare

**Problema:**

In branch-ul anterior, am introdus `IamResponse<T>` pentru raspunsurile de succes:

```java
public ResponseEntity<IamResponse<PostDTO>> getPostById(@PathVariable Integer postId){
    IamResponse<PostDTO> response = postService.getById(postId);
    return ResponseEntity.ok(response);
}
```

Raspuns de succes:

```json
{
  "message": "",
  "payload": {
    "id": 1,
    "title": "Post Title"
  },
  "success": true
}
```

Dar pentru erori, formatul era complet diferit (Spring default):

```json
{
  "timestamp": "2025-10-03T20:39:09.123+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Post cu ID 999 nu a fost gasit",
  "path": "/posts/999"
}
```

**Probleme:**

1. **Inconsistenta:**
   - Client-ul trebuie sa parseze doua formate diferite
   - Logica diferita pentru succes vs. eroare
   - Campuri diferite: `success` vs. `status`, `payload` vs. `error`

2. **Lipsa Unificarii:**
   - Raspunsul de eroare nu foloseste `IamResponse`
   - Nu exista flag `success: false`
   - Nu se pastreaza structura wrapper-ului

3. **Complexitate pentru Client:**
   ```javascript
   // Client JavaScript trebuie sa faca:
   if (response.success !== undefined) {
       // Format IamResponse
       if (response.success) {
           handleData(response.payload);
       } else {
           handleError(response.message);
       }
   } else {
       // Format Spring default
       handleError(response.error || response.message);
   }
   ```

**Solutia Partiala din Acest Branch:**

Momentan, raspunsul de eroare e simplificat la doar mesajul de eroare:

```java
@ExceptionHandler
@ResponseBody
protected ResponseEntity<String> handleException(Exception ex) {
    logStackTrace(ex);
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());  // Doar String, nu IamResponse
}
```

Raspuns:

```
HTTP/1.1 404 Not Found
Content-Type: text/plain

Post cu ID 999 nu a fost gasit
```

**Problema Ramasa:**

Desi e mai curat decat stack trace-ul complet, tot nu e consistent cu `IamResponse`.

**Solutia Ideala (pentru viitor):**

```java
@ExceptionHandler
@ResponseBody
protected ResponseEntity<IamResponse<Void>> handleException(Exception ex) {
    logStackTrace(ex);
    IamResponse<Void> errorResponse = IamResponse.createError(ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
}
```

Raspuns consistent:

```json
{
  "message": "Post cu ID 999 nu a fost gasit",
  "payload": null,
  "success": false
}
```

**Avantaje:**

- Format identic pentru succes si eroare
- Client-ul are logica unificata
- Se pastreaza pattern-ul `IamResponse`

## Modificari Tehnice Detaliate

### 1. Clasa CommonControllerAdvice

**Locatie:** `/iam_Service/src/main/java/com/post_hub/iam_Service/advice/CommonControllerAdvice.java`

**Cod Complet:**

```java
package com.post_hub.iam_Service.advice;

import com.post_hub.iam_Service.model.constants.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler
    @ResponseBody
    protected ResponseEntity<String> handleException(Exception ex) {
        logStackTrace(ex);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    private void logStackTrace(Exception ex) {
        StringBuilder stackTrace = new StringBuilder();

        stackTrace.append(ApiConstants.ANSI_RED);

        stackTrace.append(ex.getMessage()).append(ApiConstants.BREAK_LINE);

        if (Objects.nonNull(ex.getCause())) {
            stackTrace.append(ex.getCause().getMessage()).append(ApiConstants.BREAK_LINE);
        }

        Arrays.stream(ex.getStackTrace())
                .filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
                .forEach(st -> stackTrace
                        .append(st.getClassName())
                        .append(".")
                        .append(st.getMethodName())
                        .append(" (")
                        .append(st.getLineNumber())
                        .append(") ")
                );

        log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
    }
}
```

**Analiza Detaliata:**

#### Adnotarile Clasei

```java
@Slf4j
@ControllerAdvice
public class CommonControllerAdvice {
```

**@ControllerAdvice:**

- Adnotare Spring care marcheaza clasa ca un component global pentru toti controller-ii
- Similar cu `@Component`, dar specializat pentru cross-cutting concerns ale controller-elor
- Se aplica automat la **toate** controller-ele din aplicatie (`@RestController`, `@Controller`)
- Permite definirea de:
  - Exception handlers (`@ExceptionHandler`)
  - Model attributes (`@ModelAttribute`)
  - Binder initialization (`@InitBinder`)

**Cum Functioneaza:**

```
1. Client face request → POST /posts/999
   │
   ▼
2. DispatcherServlet routeaza catre PostController
   │
   ▼
3. PostController.getPostById(999)
   │
   ▼
4. postService.getById(999)
   │
   ▼
5. repository.findById(999) → Optional.empty()
   │
   ▼
6. orElseThrow() → NotFoundException("Post cu ID 999 nu a fost gasit")
   │
   ▼
7. Exceptia se propaga inapoi prin stack:
   Service → Controller → DispatcherServlet
   │
   ▼
8. DispatcherServlet cauta @ExceptionHandler pentru NotFoundException
   │
   ▼
9. Gaseste CommonControllerAdvice.handleException(Exception ex)
   │
   ▼
10. Executa handler-ul:
    - logStackTrace(ex)
    - return ResponseEntity.status(404).body(ex.getMessage())
   │
   ▼
11. Client primeste: 404 Not Found, "Post cu ID 999 nu a fost gasit"
```

**Scopul @ControllerAdvice:**

- **Scope Global:** Se aplica la toate exceptiile din toate controller-ele
- **Centralizare:** Un singur loc pentru gestionarea erorilor
- **Separation of Concerns:** Controller-ele nu mai trebuie sa gestioneze exceptii

**@Slf4j:**

- Lombok annotation care genereaza:
  ```java
  private static final Logger log = LoggerFactory.getLogger(CommonControllerAdvice.class);
  ```
- Permite logging direct: `log.error(...)`, `log.info(...)`, etc.

#### Metoda handleException

```java
@ExceptionHandler
@ResponseBody
protected ResponseEntity<String> handleException(Exception ex) {
    logStackTrace(ex);
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ex.getMessage());
}
```

**@ExceptionHandler:**

- Adnotare care marcheaza metoda ca handler pentru exceptii
- Fara parametri (implicit) → prinde **toate** exceptiile de tip `Exception` si subtipurile sale
- Poate fi specificat tipul: `@ExceptionHandler(NotFoundException.class)` - doar pentru exceptii specifice

**Exemple de Specificitate:**

```java
// Prinde TOATE exceptiile
@ExceptionHandler
protected ResponseEntity<?> handleException(Exception ex) { }

// Prinde doar NotFoundException
@ExceptionHandler(NotFoundException.class)
protected ResponseEntity<?> handleNotFound(NotFoundException ex) { }

// Prinde mai multe tipuri
@ExceptionHandler({NotFoundException.class, IllegalArgumentException.class})
protected ResponseEntity<?> handleMultiple(Exception ex) { }
```

**Ordinea de Prioritate:**

Spring alege handler-ul cel mai specific:

```java
@ControllerAdvice
public class CommonControllerAdvice {

    // Prioritate 1: Cel mai specific
    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    // Prioritate 2: Mai generic
    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(500).body("Runtime error");
    }

    // Prioritate 3: Cel mai generic (catch-all)
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(500).body("Unknown error");
    }
}
```

Daca se arunca `NotFoundException`, Spring va apela `handleNotFound`, nu `handleRuntime` sau `handleAll`.

**@ResponseBody:**

- Indica ca valoarea returnata de metoda trebuie serializata direct in corpul raspunsului HTTP
- Fara aceasta adnotare, Spring ar incerca sa gaseasca o view (pentru MVC traditional)
- In `@RestController` e implicit, dar in `@ControllerAdvice` trebuie explicit

**Return Type: ResponseEntity<String>**

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)  // Status: 404
        .body(ex.getMessage());        // Body: mesajul exceptiei
```

- `ResponseEntity` - wrapper Spring pentru raspunsuri HTTP complete
- Permite setarea explicita a:
  - Status code (`status()`)
  - Headers (`header()`)
  - Body (`body()`)

**Exemplu Response:**

```
HTTP/1.1 404 Not Found
Content-Type: text/plain
Content-Length: 32

Post cu ID 999 nu a fost gasit
```

**Problema cu Implementarea Curenta:**

Toate exceptiile returneaza **404 Not Found**, chiar daca nu toate sunt "not found":

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)  // Hardcoded 404
        .body(ex.getMessage());
```

**Exemple de Exceptii cu Status Code Gresit:**

```java
// IllegalArgumentException ar trebui 400 Bad Request
throw new IllegalArgumentException("ID-ul trebuie sa fie pozitiv");
// Dar va returna: 404 Not Found

// NullPointerException ar trebui 500 Internal Server Error
throw new NullPointerException("Object-ul este null");
// Dar va returna: 404 Not Found

// AccessDeniedException ar trebui 403 Forbidden
throw new AccessDeniedException("Nu ai permisiuni");
// Dar va returna: 404 Not Found
```

**Solutia Corecta:**

Ar trebui sa existe handler-e separate pentru fiecare tip de exceptie:

```java
@ExceptionHandler(NotFoundException.class)
protected ResponseEntity<String> handleNotFound(NotFoundException ex) {
    logStackTrace(ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}

@ExceptionHandler(IllegalArgumentException.class)
protected ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
    logStackTrace(ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
}

@ExceptionHandler(Exception.class)  // Catch-all pentru exceptii neasteptate
protected ResponseEntity<String> handleGeneric(Exception ex) {
    logStackTrace(ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("A aparut o eroare neasteptata");
}
```

#### Metoda logStackTrace

```java
private void logStackTrace(Exception ex) {
    StringBuilder stackTrace = new StringBuilder();

    // 1. Adauga culoarea rosie
    stackTrace.append(ApiConstants.ANSI_RED);

    // 2. Adauga mesajul exceptiei
    stackTrace.append(ex.getMessage()).append(ApiConstants.BREAK_LINE);

    // 3. Adauga cauza daca exista
    if (Objects.nonNull(ex.getCause())) {
        stackTrace.append(ex.getCause().getMessage()).append(ApiConstants.BREAK_LINE);
    }

    // 4. Filtreaza si adauga stack trace-ul aplicatiei
    Arrays.stream(ex.getStackTrace())
            .filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
            .forEach(st -> stackTrace
                    .append(st.getClassName())
                    .append(".")
                    .append(st.getMethodName())
                    .append(" (")
                    .append(st.getLineNumber())
                    .append(") ")
            );

    // 5. Reseteaza culoarea si logeaza
    log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
}
```

**Analiza Pas cu Pas:**

**Pas 1: Colorare cu ANSI Codes**

```java
stackTrace.append(ApiConstants.ANSI_RED);
// ANSI_RED = "\u001B[31m"
```

**ANSI Escape Codes:**

- Coduri speciale pentru controlul terminalului (culori, formatare)
- `\u001B` - escape character (ESC)
- `[31m` - cod pentru culoarea rosie
- `[37m` - cod pentru culoarea alba (reset la default)

**Exemple de Coduri ANSI:**

```java
public static final String ANSI_RESET = "\u001B[0m";
public static final String ANSI_BLACK = "\u001B[30m";
public static final String ANSI_RED = "\u001B[31m";
public static final String ANSI_GREEN = "\u001B[32m";
public static final String ANSI_YELLOW = "\u001B[33m";
public static final String ANSI_BLUE = "\u001B[34m";
public static final String ANSI_PURPLE = "\u001B[35m";
public static final String ANSI_CYAN = "\u001B[36m";
public static final String ANSI_WHITE = "\u001B[37m";

// Bold
public static final String ANSI_BOLD = "\u001B[1m";

// Background colors
public static final String ANSI_BG_RED = "\u001B[41m";
```

**Utilizare:**

```java
System.out.println(ANSI_RED + "Text rosu" + ANSI_RESET);
System.out.println(ANSI_GREEN + "Text verde" + ANSI_RESET);
System.out.println(ANSI_BOLD + ANSI_YELLOW + "Text galben bold" + ANSI_RESET);
```

**Output in Terminal:**

```
Text rosu        [AFISAT IN ROSU]
Text verde       [AFISAT IN VERDE]
Text galben bold [AFISAT IN GALBEN BOLD]
```

**NOTA:** Codurile ANSI functioneaza doar in terminale compatibile (Linux, macOS, Windows 10+).

**Pas 2: Adaugarea Mesajului Exceptiei**

```java
stackTrace.append(ex.getMessage()).append(ApiConstants.BREAK_LINE);
// BREAK_LINE = "\n"
```

- `ex.getMessage()` - returneaza mesajul transmis la crearea exceptiei
- Pentru `new NotFoundException("Post cu ID 999 nu a fost gasit")`, returneaza: `"Post cu ID 999 nu a fost gasit"`

**Pas 3: Adaugarea Cauzei**

```java
if (Objects.nonNull(ex.getCause())) {
    stackTrace.append(ex.getCause().getMessage()).append(ApiConstants.BREAK_LINE);
}
```

**Ce este Cauza:**

Exceptiile pot fi "chained" - o exceptie poate avea o alta exceptie ca si cauza:

```java
try {
    // Cod care arunca SQLException
    connection.execute(query);
} catch (SQLException sqlEx) {
    // Wrap SQLException intr-o exceptie custom
    throw new DatabaseException("Eroare la executia query-ului", sqlEx);
    //                                                             ^
    //                                                             cauza
}
```

**Exemplu:**

```java
DatabaseException: Eroare la executia query-ului
Caused by: SQLException: Syntax error in SQL
```

- `ex.getMessage()` = "Eroare la executia query-ului"
- `ex.getCause().getMessage()` = "Syntax error in SQL"

**De ce e Util:**

- Pastreaza contextul original al erorii
- Ajuta la debugging - stii cauza root
- Permite handling diferentiat bazat pe cauza

**Pas 4: Filtrarea Stack Trace-ului**

```java
Arrays.stream(ex.getStackTrace())
        .filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
        .forEach(st -> stackTrace
                .append(st.getClassName())
                .append(".")
                .append(st.getMethodName())
                .append(" (")
                .append(st.getLineNumber())
                .append(") ")
        );
```

**Structura Stack Trace:**

```java
StackTraceElement[] stackTrace = ex.getStackTrace();
// Array de elemente, fiecare reprezinta un frame din call stack

// Fiecare StackTraceElement contine:
stackTraceElement.getClassName();   // "com.post_hub.iam_Service.service.impl.PostServiceImpl"
stackTraceElement.getMethodName();  // "getById"
stackTraceElement.getLineNumber();  // 21
stackTraceElement.getFileName();    // "PostServiceImpl.java"
```

**Stream Processing:**

1. **`Arrays.stream(ex.getStackTrace())`** - converteste array-ul intr-un Stream

2. **`.filter(st -> st.getClassName().startsWith(...)`** - pastreaza doar elementele care incep cu prefixul specificat

3. **`.forEach(st -> ...)`** - pentru fiecare element ramas, construieste string-ul formatat

**BUG-ul Mentionat Anterior:**

```java
.filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
// TIME_ZONE_PACKAGE_NAME = "java.time.zone"
```

Problema: `"java.time.zone"` nu e pachetul aplicatiei!

**Exemplu de Stack Trace Complet:**

```
com.post_hub.iam_Service.service.impl.PostServiceImpl.getById (21)          ← RELEVANT
com.post_hub.iam_Service.controller.PostController.getPostById (25)         ← RELEVANT
sun.reflect.NativeMethodAccessorImpl.invoke0 (Native Method)                ← Ignorat
sun.reflect.NativeMethodAccessorImpl.invoke (62)                            ← Ignorat
org.springframework.web.method.support.InvocableHandlerMethod.doInvoke (190) ← Ignorat
org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod (895) ← Ignorat
org.apache.catalina.core.ApplicationFilterChain.doFilter (166)              ← Ignorat
```

**Cu filtrul corect (`com.post_hub`):**

Ar pastra doar:
```
com.post_hub.iam_Service.service.impl.PostServiceImpl.getById (21)
com.post_hub.iam_Service.controller.PostController.getPostById (25)
```

**Cu filtrul gresit (`java.time.zone`):**

Nu pastreaza nimic (nici o clasa din stack trace nu e din pachetul `java.time.zone`).

**Pas 5: Reset Culoare si Logging**

```java
log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
// ANSI_WHITE = "\u001B[37m"
```

- Adauga codul pentru culoare alba (reset la culoarea default)
- Previne ca log-urile ulterioare sa ramana rosii
- Converteste `StringBuilder` la `String` cu `toString()`
- Logeaza la nivel ERROR (cel mai sever nivel dupa FATAL)

**Output Final in Console:**

```
[ROSU]Post cu ID 999 nu a fost gasit
[ALB]
```

(Datorita bug-ului cu filtrul, stack trace-ul e gol)

**Output Asteptat (cu filtrul corectat):**

```
[ROSU]Post cu ID 999 nu a fost gasit
com.post_hub.iam_Service.service.impl.PostServiceImpl.getById (21) com.post_hub.iam_Service.controller.PostController.getPostById (25) [ALB]
```

### 2. Constante Adaugate in ApiConstants

**Modificari:**

```java
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ApiConstants {

    public static final String UNDEFINED = "undefined";  // Existent anterior

    // NOU - pentru colorarea log-urilor
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_WHITE = "\u001B[37m";

    // NOU - pentru formatare
    public static final String BREAK_LINE = "\n";

    // NOU - pentru filtrarea stack trace-ului (BUG: nume gresit si valoare gresita)
    public static final String TIME_ZONE_PACKAGE_NAME = "java.time.zone";
}
```

**Analiza Constantelor:**

#### ANSI_RED si ANSI_WHITE

```java
public static final String ANSI_RED = "\u001B[31m";
public static final String ANSI_WHITE = "\u001B[37m";
```

**Scop:**
- Colorarea output-ului in terminal
- ANSI_RED - pentru erori (atrage atentia)
- ANSI_WHITE - pentru reset la culoarea default

**Alternativa Extinsa:**

```java
public class AnsiColors {
    // Text colors
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bold
    public static final String BOLD = "\u001B[1m";

    // Backgrounds
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
}

// Utilizare
log.error(AnsiColors.RED + "Eroare critica" + AnsiColors.RESET);
log.warn(AnsiColors.YELLOW + "Avertisment" + AnsiColors.RESET);
log.info(AnsiColors.GREEN + "Succes" + AnsiColors.RESET);
```

#### BREAK_LINE

```java
public static final String BREAK_LINE = "\n";
```

**Scop:**
- Adauga o linie noua in string-uri
- Mai citibil decat `\n` direct in cod

**Alternative:**

```java
System.lineSeparator()  // Platform-independent (\n pe Unix, \r\n pe Windows)
"\n"                    // Unix/Linux/macOS
"\r\n"                  // Windows
```

**De ce Constanta:**

```java
// Fara constanta
String message = "Linia 1\nLinia 2\nLinia 3";

// Cu constanta
String message = "Linia 1" + BREAK_LINE + "Linia 2" + BREAK_LINE + "Linia 3";

// Sau cu StringBuilder
StringBuilder sb = new StringBuilder();
sb.append("Linia 1").append(BREAK_LINE);
sb.append("Linia 2").append(BREAK_LINE);
sb.append("Linia 3").append(BREAK_LINE);
```

**Avantaje:**
- Claritate: `BREAK_LINE` e mai citibil decat `\n`
- Usor de schimbat: daca vrei `\r\n`, schimbi intr-un singur loc
- Intentie clara: comunica ca vrei o linie noua, nu doar un caracter special

#### TIME_ZONE_PACKAGE_NAME (BUG)

```java
public static final String TIME_ZONE_PACKAGE_NAME = "java.time.zone";
```

**Problema Majora:**

Numele si valoarea constantei sunt **ambele gresite**:

1. **Numele:** `TIME_ZONE_PACKAGE_NAME` sugereaza ca e legat de time zones
2. **Valoarea:** `"java.time.zone"` e un pachet Java standard, nu al aplicatiei
3. **Scopul Real:** Ar trebui sa fie pachetul ROOT al aplicatiei pentru filtrarea stack trace-ului

**Ce Ar Trebui Sa Fie:**

```java
public static final String APP_BASE_PACKAGE = "com.post_hub";
```

**Sau mai detaliat:**

```java
public static final String APP_BASE_PACKAGE = "com.post_hub.iam_Service";
```

**Impactul Bug-ului:**

```java
Arrays.stream(ex.getStackTrace())
        .filter(st -> st.getClassName().startsWith("java.time.zone"))  // Gresit!
        .forEach(st -> ...);
```

- Filtrul cauta clase din pachetul `java.time.zone`
- Nici o clasa din aplicatie nu e in acest pachet
- Rezultat: filtrul nu pastreaza nimic → stack trace gol in log

**Demonstratie:**

```java
// Stack trace real
NotFoundException:
  at com.post_hub.iam_Service.service.impl.PostServiceImpl.getById(...)
  at com.post_hub.iam_Service.controller.PostController.getPostById(...)
  at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(...)
  at org.apache.catalina.core.ApplicationFilterChain.doFilter(...)

// Cu filtrul "java.time.zone"
// → nici o clasa nu incepe cu "java.time.zone"
// → rezultat gol

// Cu filtrul "com.post_hub"
// → primele 2 clase trec filtrul
// → rezultat: doar stack trace-ul aplicatiei
```

**Corectie Necesara:**

```java
// In ApiConstants.java
public static final String APP_BASE_PACKAGE = "com.post_hub";

// In CommonControllerAdvice.java
Arrays.stream(ex.getStackTrace())
        .filter(st -> st.getClassName().startsWith(ApiConstants.APP_BASE_PACKAGE))
        .forEach(st -> ...);
```

## Principii si Concepte

### 1. @ControllerAdvice Pattern

**Definitie:**

`@ControllerAdvice` e un pattern Spring pentru implementarea cross-cutting concerns la nivelul controller-elor.

**Caracteristici:**

1. **Scope Global:**
   - Se aplica automat la **toate** controller-ele
   - Nu trebuie sa fie injectat sau apelat explicit
   - Spring il detecteaza si il inregistreaza automat

2. **Specializare:**
   ```java
   // Se aplica la toate controller-ele
   @ControllerAdvice
   public class GlobalAdvice { }

   // Se aplica doar la controller-ele din anumite pachete
   @ControllerAdvice("com.post_hub.iam_Service.controller")
   public class ControllerPackageAdvice { }

   // Se aplica doar la controller-e annotate cu @RestController
   @ControllerAdvice(annotations = RestController.class)
   public class RestControllerAdvice { }

   // Se aplica doar la controller-e specifice
   @ControllerAdvice(assignableTypes = {PostController.class, UserController.class})
   public class SpecificControllerAdvice { }
   ```

3. **Use Cases:**
   - Exception handling (`@ExceptionHandler`)
   - Model attributes globale (`@ModelAttribute`)
   - Binder initialization (`@InitBinder`)

**Avantaje:**

- **Centralizare:** Un singur loc pentru logica cross-cutting
- **DRY (Don't Repeat Yourself):** Nu mai duplici codul in fiecare controller
- **Separation of Concerns:** Controller-ele raman curate, fara logica de error handling

**Exemple de Utilizare:**

```java
@ControllerAdvice
public class GlobalControllerAdvice {

    // Exception handling
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    // Model attributes globale (adaugate la toate raspunsurile)
    @ModelAttribute("appVersion")
    public String appVersion() {
        return "1.0.0";
    }

    // Initializare custom pentru data binding
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }
}
```

### 2. Exception Handling Strategy

**Nivele de Exception Handling:**

```
┌─────────────────────────────────────┐
│ 1. Try-Catch in Metoda              │  Cel mai specific
│    try { ... } catch (Ex e) { ... } │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│ 2. Try-Catch in Controller          │  Specific la controller
│    @GetMapping                       │
│    public ResponseEntity<?> get() { │
│        try { ... } catch { ... }    │
│    }                                 │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│ 3. @ExceptionHandler in Controller  │  Specific la controller
│    @ExceptionHandler                 │
│    public ResponseEntity<?> handle() │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│ 4. @ControllerAdvice Global         │  Global pentru aplicatie
│    @ExceptionHandler                 │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│ 5. Spring Default Error Handler     │  Cel mai generic
│    (returneaza stack trace)          │
└─────────────────────────────────────┘
```

**Best Practice:**

Foloseste cel mai specific nivel necesar:

- **Try-catch local** pentru logica specifica metodei
- **@ExceptionHandler in controller** pentru exceptii specifice controller-ului
- **@ControllerAdvice** pentru exceptii comune tuturor controller-elor

**Exemplu Strategie Completa:**

```java
// Service - arunca exceptii de business
@Service
public class PostServiceImpl {
    public IamResponse<PostDTO> getById(Integer id) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        // Nu handle-uim aici, doar aruncam
        return mapToDTO(post);
    }
}

// Controller - nu handle-uim exceptii (le lasam sa se propage)
@RestController
public class PostController {
    @GetMapping("/{id}")
    public ResponseEntity<IamResponse<PostDTO>> getById(@PathVariable Integer id){
        // Daca service arunca exceptie, se propaga la ControllerAdvice
        return ResponseEntity.ok(postService.getById(id));
    }
}

// ControllerAdvice - handle-uim toate exceptiile global
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)  // Catch-all
    public ResponseEntity<?> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(500).body("Internal server error");
    }
}
```

### 3. ANSI Escape Codes pentru Logging

**Ce Sunt ANSI Codes:**

- Coduri speciale pentru controlul terminalului
- Permit colorare, formatare, mutarea cursorului, etc.
- Standard ANSI X3.64 (ISO/IEC 6429)

**Structura:**

```
ESC [ <parametri> <comanda>

ESC  = \u001B (sau \033 in octal, sau \x1B in hex)
[    = bracket de deschidere
<parametri> = numere separate prin ;
<comanda> = litera (m pentru culoare)
```

**Exemple:**

```java
// Culori de text
"\u001B[30m"  // Black
"\u001B[31m"  // Red
"\u001B[32m"  // Green
"\u001B[33m"  // Yellow
"\u001B[34m"  // Blue
"\u001B[35m"  // Magenta
"\u001B[36m"  // Cyan
"\u001B[37m"  // White

// Culori intense (bright)
"\u001B[90m"  // Bright Black (Gray)
"\u001B[91m"  // Bright Red
"\u001B[92m"  // Bright Green
"\u001B[93m"  // Bright Yellow
"\u001B[94m"  // Bright Blue
"\u001B[95m"  // Bright Magenta
"\u001B[96m"  // Bright Cyan
"\u001B[97m"  // Bright White

// Culori de fundal
"\u001B[40m"  // Black background
"\u001B[41m"  // Red background
"\u001B[42m"  // Green background
// ... etc

// Formatare
"\u001B[0m"   // Reset all
"\u001B[1m"   // Bold
"\u001B[2m"   // Dim
"\u001B[3m"   // Italic
"\u001B[4m"   // Underline
"\u001B[5m"   // Blinking
"\u001B[7m"   // Reverse (swap foreground/background)

// Combinatii
"\u001B[1;31m"  // Bold red
"\u001B[4;32m"  // Underlined green
"\u001B[1;33;41m"  // Bold yellow text on red background
```

**Utilizare in Logging:**

```java
public class ColoredLogger {
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RESET = "\u001B[0m";

    public static void logError(String message) {
        System.out.println(RED + "[ERROR] " + message + RESET);
    }

    public static void logSuccess(String message) {
        System.out.println(GREEN + "[SUCCESS] " + message + RESET);
    }

    public static void logWarning(String message) {
        System.out.println(YELLOW + "[WARNING] " + message + RESET);
    }
}

// Utilizare
ColoredLogger.logError("Database connection failed");     // Rosu
ColoredLogger.logSuccess("User created successfully");   // Verde
ColoredLogger.logWarning("Cache is full");               // Galben
```

**Output in Terminal:**

```
[ERROR] Database connection failed      [AFISAT IN ROSU]
[SUCCESS] User created successfully    [AFISAT IN VERDE]
[WARNING] Cache is full                [AFISAT IN GALBEN]
```

**Limitari:**

- Nu functioneaza in toate terminalele (mai vechi, IDE-uri)
- Windows Command Prompt nu suporta (pre-Windows 10)
- In fisiere de log, apar ca text normal (ex: `←[31mError←[0m`)

**Alternativa Moderna - Logback cu Color Converters:**

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} %highlight(%-5level) %cyan(%logger{36}) - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

Acest lucru adauga automat culori bazate pe nivel:
- ERROR → Rosu
- WARN → Galben
- INFO → Verde
- DEBUG → Cyan

## Flow-ul unei Exceptii

### Cazul: Post Nu Exista (ID = 999)

```
┌──────────────────────────────────────────┐
│ 1. Client Request                        │
│    GET /posts/999                        │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 2. DispatcherServlet                     │
│    - Primeste request-ul                 │
│    - Ruteza catre PostController         │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 3. PostController.getPostById(999)       │
│    log.trace("Current method: getPost...")│
│    response = postService.getById(999)   │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 4. PostServiceImpl.getById(999)          │
│    repository.findById(999)              │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 5. PostRepository.findById(999)          │
│    - Executa: SELECT * FROM posts        │
│      WHERE id = 999                      │
│    - Rezultat: 0 rows                    │
│    - Returneaza: Optional.empty()        │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 6. PostServiceImpl (continuare)          │
│    Optional.empty().orElseThrow(...)     │
│    → throw new NotFoundException(        │
│         "Post cu ID 999 nu a fost gasit")│
└────────────┬─────────────────────────────┘
             │ NotFoundException propagata
             ▼
┌──────────────────────────────────────────┐
│ 7. Exception Propagation                 │
│    Service → Controller → DispatcherServlet│
│    (exceptia nu e prinsa in controller)  │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 8. DispatcherServlet Exception Handling  │
│    - Cauta @ExceptionHandler pentru      │
│      NotFoundException                   │
│    - Gaseste CommonControllerAdvice      │
│      .handleException(Exception ex)      │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 9. CommonControllerAdvice.handleException│
│    logStackTrace(ex)                     │
│    → log.error("[RED]Post cu ID 999...")│
│    return ResponseEntity                 │
│      .status(404)                        │
│      .body(ex.getMessage())              │
└────────────┬─────────────────────────────┘
             │
             ▼
┌──────────────────────────────────────────┐
│ 10. Response la Client                   │
│     HTTP/1.1 404 Not Found               │
│     Content-Type: text/plain             │
│                                          │
│     Post cu ID 999 nu a fost gasit       │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│ 11. Server Logs                          │
│     [ROSU]Post cu ID 999 nu a fost gasit │
│     [ALB]                                │
│     (stack trace filtrat, dar gol        │
│      datorita bug-ului cu TIME_ZONE)     │
└──────────────────────────────────────────┘
```

## Comparatie Inainte/Dupa

### Inainte de 5-23-Exceptions-Handling

**Request:** `GET /posts/999` (post care nu exista)

**Response:**

```
HTTP/1.1 500 Internal Server Error
Content-Type: application/json
Content-Length: 1234

{
  "timestamp": "2025-10-03T20:39:09.123+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Post cu ID 999 nu a fost gasit",
  "path": "/posts/999",
  "trace": "com.post_hub.iam_Service.model.exeption.NotFoundException: Post cu ID 999 nu a fost gasit\n\tat com.post_hub.iam_Service.service.impl.PostServiceImpl.getById(PostServiceImpl.java:21)\n\tat com.post_hub.iam_Service.controller.PostController.getPostById(PostController.java:25)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n\t... 50+ linii"
}
```

**Server Logs:**

```
2025-10-03 20:39:09.123 ERROR 12345 --- [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is com.post_hub.iam_Service.model.exeption.NotFoundException: Post cu ID 999 nu a fost gasit] with root cause

com.post_hub.iam_Service.model.exeption.NotFoundException: Post cu ID 999 nu a fost gasit
    at com.post_hub.iam_Service.service.impl.PostServiceImpl.getById(PostServiceImpl.java:21)
    at com.post_hub.iam_Service.controller.PostController.getPostById(PostController.java:25)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    ... [50+ linii de stack trace]
```

**Probleme:**

- Status code 500 (incorect pentru "not found")
- Stack trace expus catre client (risc de securitate)
- Log-uri verbose, greu de citit
- Format inconsistent cu raspunsurile de succes

### Dupa 5-23-Exceptions-Handling

**Request:** `GET /posts/999` (acelasi post care nu exista)

**Response:**

```
HTTP/1.1 404 Not Found
Content-Type: text/plain
Content-Length: 32

Post cu ID 999 nu a fost gasit
```

**Server Logs:**

```
[ROSU]Post cu ID 999 nu a fost gasit
[ALB]
```

(Datorita bug-ului, stack trace-ul e gol, dar ar trebui sa contina liniile aplicatiei)

**Imbunatatiri:**

- Status code corect: 404 Not Found
- Raspuns clean: doar mesajul
- Log-uri colorate si concise
- Securitate imbunatatita: fara expunere interna

**NOTA:** Raspunsul tot nu e consistent cu `IamResponse` - asta ramane de imbunatatit.

## Limitari si Probleme Ramase

### 1. Status Code Hardcoded

**Problema:**

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)  // Mereu 404
        .body(ex.getMessage());
```

Toate exceptiile returneaza 404, indiferent de tip:

```java
// NotFoundException → 404 ✓ Corect
throw new NotFoundException("Post not found");

// IllegalArgumentException → 404 ✗ Ar trebui 400
throw new IllegalArgumentException("ID must be positive");

// AccessDeniedException → 404 ✗ Ar trebui 403
throw new AccessDeniedException("No permission");

// NullPointerException → 404 ✗ Ar trebui 500
throw new NullPointerException("Object is null");
```

**Solutie:**

Handler-e multiple pentru tipuri diferite:

```java
@ControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        logStackTrace(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        logStackTrace(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleForbidden(AccessDeniedException ex) {
        logStackTrace(ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)  // Catch-all pentru exceptii neasteptate
    public ResponseEntity<String> handleGeneric(Exception ex) {
        logStackTrace(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("A aparut o eroare neasteptata");
    }
}
```

### 2. Raspunsul Nu Foloseste IamResponse

**Problema:**

Raspunsul de eroare e `String`, nu `IamResponse<T>`:

```java
protected ResponseEntity<String> handleException(Exception ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
```

Rezulta inconsistenta:

```json
// Succes
{
  "message": "",
  "payload": { "id": 1, "title": "Post" },
  "success": true
}

// Eroare
"Post cu ID 999 nu a fost gasit"  // Just a string
```

**Solutie:**

Foloseste `IamResponse` si pentru erori:

```java
@ExceptionHandler(NotFoundException.class)
public ResponseEntity<IamResponse<Void>> handleNotFound(NotFoundException ex) {
    logStackTrace(ex);
    IamResponse<Void> errorResponse = new IamResponse<>(ex.getMessage(), null, false);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
}
```

Raspuns consistent:

```json
{
  "message": "Post cu ID 999 nu a fost gasit",
  "payload": null,
  "success": false
}
```

Client-ul are acum format unificat pentru succes si eroare.

### 3. Bug-ul cu TIME_ZONE_PACKAGE_NAME

**Problema:**

```java
public static final String TIME_ZONE_PACKAGE_NAME = "java.time.zone";

// Utilizare
.filter(st -> st.getClassName().startsWith(ApiConstants.TIME_ZONE_PACKAGE_NAME))
```

- Numele constantei e misleading
- Valoarea e gresita (nu e pachetul aplicatiei)
- Stack trace-ul filtrat e gol

**Solutie:**

```java
// In ApiConstants.java
public static final String APP_BASE_PACKAGE = "com.post_hub";

// In CommonControllerAdvice.java
.filter(st -> st.getClassName().startsWith(ApiConstants.APP_BASE_PACKAGE))
```

### 4. Lipsa Validarii Parametrilor

**Problema:**

Handler-ul nu valideaza nimic:

```java
protected ResponseEntity<String> handleException(Exception ex) {
    logStackTrace(ex);  // What if ex is null?
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
```

Daca `ex` e null (teoretic imposibil, dar...):
- `logStackTrace(ex)` → NullPointerException
- `ex.getMessage()` → NullPointerException

**Solutie:**

```java
protected ResponseEntity<String> handleException(Exception ex) {
    if (ex == null) {
        log.error("Received null exception in handler");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unknown error occurred");
    }
    logStackTrace(ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
```

Sau cu `Objects.requireNonNull()`:

```java
protected ResponseEntity<String> handleException(Exception ex) {
    Objects.requireNonNull(ex, "Exception cannot be null");
    logStackTrace(ex);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}
```

### 5. Logging Incomplet

**Problema:**

Metoda `logStackTrace` filtreaza stack trace-ul, dar:

- Nu logeaza timestamp-ul
- Nu logeaza severitatea (mereu ERROR)
- Nu logeaza contextul (request path, user, etc.)

**Solutie Imbunatatita:**

```java
private void logStackTrace(Exception ex, HttpServletRequest request) {
    StringBuilder stackTrace = new StringBuilder();

    stackTrace.append(ApiConstants.ANSI_RED);

    // Timestamp
    stackTrace.append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    stackTrace.append(" [ERROR] ");
    stackTrace.append(ApiConstants.BREAK_LINE);

    // Request context
    if (request != null) {
        stackTrace.append("Path: ").append(request.getRequestURI());
        stackTrace.append(", Method: ").append(request.getMethod());
        stackTrace.append(ApiConstants.BREAK_LINE);
    }

    // Exception message
    stackTrace.append(ex.getMessage()).append(ApiConstants.BREAK_LINE);

    // Cause
    if (Objects.nonNull(ex.getCause())) {
        stackTrace.append("Caused by: ").append(ex.getCause().getMessage());
        stackTrace.append(ApiConstants.BREAK_LINE);
    }

    // Filtered stack trace
    Arrays.stream(ex.getStackTrace())
            .filter(st -> st.getClassName().startsWith(ApiConstants.APP_BASE_PACKAGE))
            .forEach(st -> stackTrace
                    .append("  at ")
                    .append(st.getClassName())
                    .append(".")
                    .append(st.getMethodName())
                    .append(" (")
                    .append(st.getFileName())
                    .append(":")
                    .append(st.getLineNumber())
                    .append(")")
                    .append(ApiConstants.BREAK_LINE)
            );

    log.error(stackTrace.append(ApiConstants.ANSI_WHITE).toString());
}
```

**Output Imbunatatit:**

```
[ROSU]2025-10-03T20:39:09.123 [ERROR]
Path: /posts/999, Method: GET
Post cu ID 999 nu a fost gasit
  at com.post_hub.iam_Service.service.impl.PostServiceImpl.getById (PostServiceImpl.java:21)
  at com.post_hub.iam_Service.controller.PostController.getPostById (PostController.java:25)
[ALB]
```

## Best Practices Demonstrate

### 1. Global Exception Handling

**Pattern:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SpecificException.class)
    public ResponseEntity<?> handleSpecific(SpecificException ex) {
        // Handle specific exception
    }
}
```

**Avantaje:**

- Centralizare: un singur loc pentru toate exceptiile
- DRY: nu duplici logica in fiecare controller
- Consistenta: toate exceptiile sunt tratate uniform

### 2. Colored Logging pentru Vizibilitate

**Pattern:**

```java
log.error(ANSI_RED + message + ANSI_WHITE);
```

**Avantaje:**

- Atrage atentia asupra erorilor
- Usor de identificat in log-uri voluminoase
- Debugging mai rapid

### 3. Stack Trace Filtering

**Pattern:**

```java
Arrays.stream(ex.getStackTrace())
        .filter(st -> st.getClassName().startsWith(APP_PACKAGE))
        .forEach(st -> ...);
```

**Avantaje:**

- Elimina noise-ul (framework stack traces)
- Focuseaza pe codul relevant
- Log-uri mai concise si citibile

### 4. Separation of Concerns

**Pattern:**

```java
// Service - arunca exceptii
throw new NotFoundException("...");

// Controller - nu handle-uieste
return ResponseEntity.ok(service.getById(id));

// ControllerAdvice - handle-uieste global
@ExceptionHandler(NotFoundException.class)
public ResponseEntity<?> handle(...) { }
```

**Avantaje:**

- Fiecare layer are responsabilitatile sale
- Controller-e curate, fara try-catch
- Service-uri focusate pe business logic

## Evolutie si Branch-uri Viitoare

Acest branch stabileste mecanismul de exception handling care va fi:

### Imbunatatit in Branch-uri Viitoare

1. **Adaugarea de Exceptii Custom:**
   ```java
   public class BadRequestException extends RuntimeException { }
   public class UnauthorizedException extends RuntimeException { }
   public class ForbiddenException extends RuntimeException { }
   ```

2. **Handler-e Specifice pentru Fiecare Exceptie:**
   ```java
   @ExceptionHandler(BadRequestException.class)
   public ResponseEntity<?> handleBadRequest(...) {
       return ResponseEntity.status(400).body(...);
   }
   ```

3. **Integrare cu IamResponse:**
   ```java
   @ExceptionHandler(NotFoundException.class)
   public ResponseEntity<IamResponse<Void>> handleNotFound(...) {
       return ResponseEntity.status(404).body(IamResponse.createError(...));
   }
   ```

4. **Validation Exception Handling:**
   ```java
   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
       Map<String, String> errors = new HashMap<>();
       ex.getBindingResult().getFieldErrors().forEach(error ->
           errors.put(error.getField(), error.getDefaultMessage())
       );
       return ResponseEntity.status(400).body(errors);
   }
   ```

5. **Request Context in Logs:**
   ```java
   private void logStackTrace(Exception ex, HttpServletRequest request) {
       // Include request path, method, user, etc.
   }
   ```

## Concluzii

Branch-ul **5-23-Exceptions-Handling** introduce mecanismul fundamental de global exception handling in aplicatie, marcand o imbunatatire semnificativa in:

### Realizari Cheie

1. **Global Exception Handling:**
   - Introducerea `@ControllerAdvice` pentru gestionare centralizata
   - Interceptarea tuturor exceptiilor neprinse
   - Eliminarea stack trace-urilor expuse catre client

2. **Logging Imbunatatit:**
   - Colorare cu ANSI codes pentru vizibilitate
   - Filtrarea stack trace-ului (cu un bug de corectat)
   - Log-uri concise si focusate pe codul aplicatiei

3. **Securitate:**
   - Nu mai expune structura interna a aplicatiei
   - Raspunsuri curate, fara informatii sensibile
   - Stack trace-uri doar in log-uri server-side

4. **Consistenta Partiala:**
   - Status code corect (404) pentru not found
   - Raspuns simplu (doar mesaj)
   - Pregatire pentru unificare cu IamResponse

### Impactul pe Termen Lung

Aceasta implementare:
- **Stabileste pattern-ul** pentru exception handling viitor
- **Centralizeaza logica** de error handling
- **Imbunatateste debugging-ul** cu log-uri filtrate si colorate
- **Pregateste terenul** pentru exceptii custom si raspunsuri standardizate

### Probleme Identificate

1. **Bug cu TIME_ZONE_PACKAGE_NAME** - filtrul stack trace nu functioneaza corect
2. **Status code hardcoded** - toate exceptiile returneaza 404
3. **Lipsa IamResponse** - raspunsul nu e consistent cu format-ul de succes
4. **Handler generic** - nu distinge intre tipuri de exceptii

### Lectii Esentiale

1. **@ControllerAdvice e esential** pentru aplicatii profesionale
2. **Nu expune stack trace-uri** catre client niciodata
3. **Logging customizat** ajuta enorm la debugging
4. **Testarea e critica** - bug-ul cu TIME_ZONE demonstreaza asta
5. **Consistenta e cheie** - raspunsurile ar trebui sa aiba acelasi format

Acest branch, desi are cateva probleme de rezolvat, stabileste fundatia pentru un sistem robust de exception handling care va fi imbunatatit in iteratiile urmatoare.

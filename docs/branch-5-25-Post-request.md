# Branch 5-25-Post-request - Implementarea Funcționalității de Creare Postări

## Cuprins
1. [Prezentare Generală](#prezentare-generală)
2. [Contextul Dezvoltării](#contextul-dezvoltării)
3. [Analiza Commit-ului](#analiza-commit-ului)
4. [Arhitectura Implementării](#arhitectura-implementării)
5. [Componente Noi Adăugate](#componente-noi-adăugate)
6. [Modificări ale Componentelor Existente](#modificări-ale-componentelor-existente)
7. [Fluxul de Date](#fluxul-de-date)
8. [Integrarea cu MapStruct](#integrarea-cu-mapstruct)
9. [Validarea și Gestionarea Erorilor](#validarea-și-gestionarea-erorilor)
10. [Configurarea Endpoint-urilor](#configurarea-endpoint-urilor)
11. [Impactul asupra Arhitecturii](#impactul-asupra-arhitecturii)
12. [Comparație cu Funcționalitatea GET](#comparație-cu-funcționalitatea-get)
13. [Scenarii de Utilizare](#scenarii-de-utilizare)
14. [Teste și Validare](#teste-și-validare)
15. [Concluzii și Perspective](#concluzii-și-perspective)

---

## Prezentare Generală

Branch-ul **5-25-Post-request** reprezintă o etapă crucială în evoluția aplicației IAM_SERVICE, introducând capacitatea de **creare a postărilor** prin intermediul unui endpoint REST dedicat. Acest branch completează funcționalitatea de bază CRUD pentru entitatea `Post`, adăugând operația de CREATE după ce branch-ul anterior (5-24-MapStruct) a implementat operația READ.

### Obiective Principale

1. **Implementarea operației POST**: Adăugarea unui endpoint HTTP POST pentru crearea de noi postări
2. **Definirea modelului de request**: Crearea clasei `PostRequest` pentru primirea datelor de la client
3. **Extinderea serviciului**: Adăugarea metodei `createPost()` în interfața și implementarea serviciului
4. **Integrarea MapStruct**: Utilizarea mapper-ului pentru conversia din `PostRequest` în `Post`
5. **Completarea controller-ului**: Adăugarea metodei în `PostController` pentru gestionarea request-urilor POST

### Date Tehnice

- **Data commit-ului**: 3 Octombrie 2025, 21:48:33
- **Hash commit**: `ddc7f8de6472d1e0c671f369faa80070a81b539d`
- **Autor**: Alexandru (besliualexandru33@gmail.com)
- **Fișiere modificate**: 6 fișiere
- **Linii adăugate**: 45 linii
- **Linii șterse**: 4 linii

### Fișiere Afectate

1. **PostController.java** - Adăugarea endpoint-ului POST
2. **PostMapper.java** - Adăugarea metodei de mapare pentru crearea postărilor
3. **PostRequest.java** - Fișier nou creat pentru modelul de request
4. **PostService.java** - Adăugarea metodei în interfață
5. **PostServiceImpl.java** - Implementarea logicii de creare
6. **application.properties** - Configurarea noului endpoint

---

## Contextul Dezvoltării

### Evoluția Proiectului Până la Acest Branch

Pentru a înțelege importanța acestui branch, este esențial să analizăm evoluția proiectului:

#### Branch-uri Anterioare

1. **3-11 până la 3-13**: Fundamentele Dependency Injection
   - Implementarea DI prin constructor
   - Implementarea DI prin setter/getter
   - Utilizarea @Primary și @Qualifier

2. **4-15 până la 4-18**: Integrarea Bazei de Date
   - Trecerea de la H2 la PostgreSQL
   - Configurarea Flyway pentru migrații
   - Crearea entității `Post` cu adnotări JPA

3. **5-21 până la 5-24**: Funcționalitatea de Citire
   - Implementarea repository-ului JPA
   - Crearea DTO-urilor și serviciilor
   - Gestionarea excepțiilor
   - Integrarea MapStruct pentru mapare

### Necesitatea Acestui Branch

După implementarea operației READ în branch-ul anterior, era natural să se continue cu operația CREATE. Acest branch oferă:

1. **Completarea CRUD-ului**: Primul pas către un set complet de operații
2. **Pattern-ul Request/Response**: Introducerea unui model clar de comunicare
3. **Separarea preocupărilor**: Distincția între DTO-uri (response) și Request-uri (input)
4. **Persistența datelor**: Nu doar citire, ci și crearea de noi înregistrări

---

## Analiza Commit-ului

### Mesajul Commit-ului

```
add `createPost` functionality in `PostService`, `PostController`, and related components
```

Mesajul este clar și descriptiv, indicând exact ce funcționalitate a fost adăugată și în ce componente.

### Statistici Detaliate

```
6 files changed, 45 insertions(+), 4 deletions(-)
```

#### Distribuția Modificărilor

1. **PostController.java**: +11 linii, -4 linii
   - Adăugarea import-ului pentru `PostRequest`
   - Simplificarea import-urilor (wildcard)
   - Adăugarea metodei `createPost()`

2. **PostMapper.java**: +5 linii
   - Adăugarea metodei `createPost()` cu adnotări de mapare

3. **PostRequest.java**: +14 linii (fișier nou)
   - Crearea întregului model de request

4. **PostService.java**: +3 linii
   - Adăugarea semnăturii metodei în interfață

5. **PostServiceImpl.java**: +11 linii
   - Implementarea completă a logicii de creare

6. **application.properties**: +1 linie
   - Configurarea endpoint-ului `/create`

### Impactul Modificărilor

Modificările sunt **minime dar eficiente**, demonstrând:
- Arhitectura bine concepută permite adăugarea ușoară de funcționalități
- Pattern-urile existente facilitează extinderea
- Separarea responsabilităților face modificările izolate și clare

---

## Arhitectura Implementării

### Pattern-ul MVC Utilizat

Implementarea urmează strict pattern-ul **Model-View-Controller** adaptat pentru aplicații REST:

```
Client Request (JSON)
        ↓
    Controller (PostController)
        ↓
    Service Interface (PostService)
        ↓
    Service Implementation (PostServiceImpl)
        ↓
    Mapper (PostMapper)
        ↓
    Repository (PostRepository)
        ↓
    Database (PostgreSQL)
```

### Layered Architecture

#### 1. Presentation Layer (Controller)

**Responsabilități**:
- Primirea request-urilor HTTP
- Validarea formatului datelor
- Delegarea către service layer
- Returnarea response-urilor HTTP

**Fișier**: `PostController.java`

```java
@PostMapping("${end.points.create}")
public ResponseEntity<IamResponse<PostDTO>> createPost(
        @RequestBody PostRequest postRequest){
    log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());
    IamResponse<PostDTO> response = postService.createPost(postRequest);
    return ResponseEntity.ok(response);
}
```

#### 2. Service Layer

**Responsabilități**:
- Logica de business
- Orchestrarea operațiilor
- Validarea datelor
- Coordonarea între repository și mapper

**Fișiere**: `PostService.java` (interfață), `PostServiceImpl.java` (implementare)

```java
@Override
public IamResponse<PostDTO> createPost(@NotNull PostRequest postRequest) {
    Post post = postMapper.createPost(postRequest);
    Post savedPost = postRepository.save(post);
    PostDTO postDTO = postMapper.toPostDTO(savedPost);
    return IamResponse.createSuccessful(postDTO);
}
```

#### 3. Data Access Layer (Repository)

**Responsabilități**:
- Interacțiunea cu baza de date
- Operații CRUD de bază
- Gestionarea tranzacțiilor

**Utilizare**: `postRepository.save(post)`

#### 4. Mapping Layer

**Responsabilități**:
- Conversia între obiecte
- Transformarea datelor
- Maparea câmpurilor

**Fișier**: `PostMapper.java`

```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "created", ignore = true)
Post createPost(PostRequest postRequest);
```

### Separarea Preocupărilor

Arhitectura demonstrează o **separare clară a preocupărilor**:

1. **Request Models** (`PostRequest`): Datele primite de la client
2. **Domain Models** (`Post`): Reprezentarea în baza de date
3. **Response Models** (`PostDTO`): Datele returnate clientului
4. **Business Logic** (`PostServiceImpl`): Logica aplicației
5. **Data Access** (`PostRepository`): Accesul la date

---

## Componente Noi Adăugate

### 1. PostRequest - Modelul de Request

#### Descriere

`PostRequest` este o **clasă DTO (Data Transfer Object)** care încapsulează datele necesare pentru crearea unei postări noi. Această clasă servește ca **contract** între client și server pentru operația de creare.

#### Cod Complet

```java
package com.post_hub.iam_Service.model.request.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
    private String title;
    private String content;
    private Integer likes;
}
```

#### Analiza Componentelor

##### Adnotări Lombok

1. **@Data**
   - Generează automat getters pentru toate câmpurile
   - Generează automat setters pentru toate câmpurile non-final
   - Generează `toString()`, `equals()`, și `hashCode()`
   - Reduce codul boilerplate cu aproximativ 50-70 linii

2. **@AllArgsConstructor**
   - Generează un constructor cu toate câmpurile ca parametri
   - Util pentru instanțierea rapidă: `new PostRequest(title, content, likes)`
   - Facilitatea creării în teste

3. **@NoArgsConstructor**
   - Generează un constructor fără parametri
   - **Esențial pentru deserializarea JSON** de către Jackson
   - Spring Boot folosește acest constructor pentru a crea instanța, apoi setează câmpurile

##### Câmpuri

1. **title** (String)
   - Titlul postării
   - Tipul String permite texte de orice lungime (în limitele JVM)
   - Poate fi null (lipsă validare în acest branch)

2. **content** (String)
   - Conținutul postării
   - Tipul String pentru texte lungi
   - Fără restricții de lungime la nivel de model

3. **likes** (Integer)
   - Numărul de aprecieri
   - Utilizează wrapper-ul `Integer` în loc de `int` primitiv
   - Permite valori null
   - **Observație**: În practică, like-urile ar trebui să înceapă de la 0 și să fie gestionate de server

#### Design Decisions

##### De ce PostRequest și nu Post direct?

1. **Separarea preocupărilor**:
   - `Post` include câmpuri generate de sistem (id, created)
   - `PostRequest` conține doar datele de la client

2. **Securitate**:
   - Clientul nu poate seta id-ul
   - Clientul nu poate manipula timestamp-ul

3. **Flexibilitate**:
   - Permite validări diferite pentru create vs update
   - Poate avea câmpuri diferite de entitatea de bază

4. **Clarity**:
   - Intent clar: aceasta este o cerere, nu o entitate

##### De ce Integer pentru likes?

```java
private Integer likes;  // Wrapper - poate fi null
// vs
private int likes;      // Primitiv - default 0
```

**Avantaje Integer**:
- Permite distincția între "nu a fost setat" (null) și "zero"
- Compatibil cu JSON (null vs 0)
- Flexibilitate în validare

**Dezavantaje**:
- Overhead ușor de memorie (obiect vs primitiv)
- Necesită null-checking

#### Utilizare

##### Exemplu de Request JSON

```json
POST /posts/create
Content-Type: application/json

{
  "title": "Primul meu post",
  "content": "Acesta este conținutul postării mele",
  "likes": 0
}
```

##### Procesul de Deserializare

1. **Spring Boot** primește JSON-ul
2. **Jackson** (biblioteca de serializare) deserializează JSON-ul:
   ```java
   PostRequest request = new PostRequest();  // NoArgsConstructor
   request.setTitle("Primul meu post");       // setter generat de @Data
   request.setContent("Acesta este...");      // setter generat de @Data
   request.setLikes(0);                        // setter generat de @Data
   ```
3. **Spring MVC** injectează obiectul în parametrul metodei controller-ului

##### Crearea Programatică

```java
// În teste sau alte componente
PostRequest request = new PostRequest(
    "Titlu",
    "Conținut",
    0
);

// Sau cu setters
PostRequest request = new PostRequest();
request.setTitle("Titlu");
request.setContent("Conținut");
request.setLikes(0);
```

#### Structura Pachetului

```
com.post_hub.iam_Service.model.request.post
└── PostRequest.java
```

Această structură sugerează:
- Vor exista **alte clase de request** pentru Post (ex: `UpdatePostRequest`)
- Posibil pachet similar pentru alte entități (ex: `model.request.user`)
- Organizare logică și scalabilă

#### Comparație cu PostDTO

| Aspect | PostRequest | PostDTO |
|--------|-------------|---------|
| **Scop** | Date de intrare | Date de ieșire |
| **Câmpuri** | title, content, likes | id, title, content, likes, created |
| **Utilizare** | @RequestBody | Corp de response |
| **id** | Absent | Present |
| **created** | Absent | Present (formatat) |
| **Validare** | La intrare | Nu necesită |

---

## Modificări ale Componentelor Existente

### 1. PostController - Adăugarea Endpoint-ului POST

#### Modificări la Import-uri

**Înainte**:
```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
```

**După**:
```java
import org.springframework.web.bind.annotation.*;
```

**Motivație**:
- **Simplificare**: Wildcard import pentru toate adnotările Spring Web
- **Pregătire pentru extindere**: Anticiparea altor adnotări (@PutMapping, @DeleteMapping)
- **Curățenie**: Reduce numărul de linii

**Import nou adăugat**:
```java
import com.post_hub.iam_Service.model.request.post.PostRequest;
```

#### Noua Metodă createPost()

```java
@PostMapping("${end.points.create}")
public ResponseEntity<IamResponse<PostDTO>> createPost(
        @RequestBody PostRequest postRequest){
    log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());
    IamResponse<PostDTO> response = postService.createPost(postRequest);
    return ResponseEntity.ok(response);
}
```

##### Analiza Detaliată

###### 1. Adnotarea @PostMapping

```java
@PostMapping("${end.points.create}")
```

- **Metoda HTTP**: POST (pentru crearea de resurse)
- **Path**: Rezolvat din `application.properties` (`/create`)
- **URL complet**: `/posts/create` (combinat cu `@RequestMapping` de la nivel de clasă)

**De ce POST și nu GET?**
- **Semantic REST**: POST creează resurse noi
- **Idempotență**: POST nu este idempotent (multiple request-uri creează multiple resurse)
- **Best practices**: Urmează standardele HTTP

###### 2. Parametrul @RequestBody

```java
@RequestBody PostRequest postRequest
```

- **@RequestBody**: Indică Spring să deserializeze JSON-ul din corpul request-ului
- **Proces**:
  1. Client trimite JSON
  2. Spring folosește Jackson pentru deserializare
  3. Creează obiectul `PostRequest`
  4. Injectează în metodă

**Exemplu de Request**:
```http
POST /posts/create HTTP/1.1
Content-Type: application/json

{
  "title": "Titlu postare",
  "content": "Conținut postare",
  "likes": 0
}
```

###### 3. Logging-ul

```java
log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());
```

- **Nivel TRACE**: Cel mai detaliat nivel de logging
- **Mesaj**: Numele metodei curente
- **Utilitate**: Debugging și audit trail
- **Pattern consistent**: Aceeași abordare ca la `getPostById()`

###### 4. Delegarea către Service

```java
IamResponse<PostDTO> response = postService.createPost(postRequest);
```

- **Separarea responsabilităților**: Controller-ul nu conține logică de business
- **Testabilitate**: Service-ul poate fi mock-uit în teste
- **Tip de return**: `IamResponse<PostDTO>` - wrapper consistent

###### 5. Returnarea Response-ului

```java
return ResponseEntity.ok(response);
```

- **ResponseEntity**: Permite controlul complet al response-ului HTTP
- **.ok()**: Status code 200 (success)
- **Alternative** (nu utilizate aici):
  - `ResponseEntity.created(uri)` - 201 Created (mai semantic pentru POST)
  - `ResponseEntity.badRequest()` - 400 Bad Request

##### Comparație cu getPostById()

| Aspect | getPostById() | createPost() |
|--------|---------------|--------------|
| **HTTP Method** | GET | POST |
| **Parametru** | @PathVariable Integer | @RequestBody PostRequest |
| **URL** | /posts/{id} | /posts/create |
| **Operație** | Citire | Creare |
| **Status Code** | 200 OK | 200 OK (ar trebui 201) |
| **Idempotență** | Da | Nu |

##### Îmbunătățiri Posibile

```java
// Versiune îmbunătățită cu 201 Created și Location header
@PostMapping("${end.points.create}")
public ResponseEntity<IamResponse<PostDTO>> createPost(
        @RequestBody PostRequest postRequest){
    log.trace(ApiLogoMessage.NAME_OF_CURRENT_METHOD.getValue(), APIUtils.getMethodName());
    IamResponse<PostDTO> response = postService.createPost(postRequest);

    // Construiește URI-ul pentru resursa creată
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(response.getData().getId())
        .toUri();

    // Returnează 201 Created cu Location header
    return ResponseEntity.created(location).body(response);
}
```

### 2. PostService - Extinderea Interfeței

#### Modificare

```java
public interface PostService {
    IamResponse<PostDTO> getById(@NotNull Integer postId);

    IamResponse<PostDTO> createPost(@NotNull PostRequest postRequest);  // NOU
}
```

#### Analiza

##### Semnătura Metodei

```java
IamResponse<PostDTO> createPost(@NotNull PostRequest postRequest);
```

1. **Tip de return**: `IamResponse<PostDTO>`
   - Consistent cu `getById()`
   - Wrapper pentru response standardizat
   - Conține postarea creată (cu id și timestamp)

2. **Parametru**: `@NotNull PostRequest postRequest`
   - **@NotNull**: Validare la nivel de interfață
   - Previne apeluri cu null
   - Documentare implicită (parametrul este obligatoriu)

3. **Nume**: `createPost`
   - Verb clar și descriptiv
   - Urmează convenția de denumire
   - Intent evident

##### Design de Interfață

**Principii respectate**:

1. **Interface Segregation Principle (ISP)**
   - Metode specifice și focusate
   - Fiecare metodă are un singur scop

2. **Dependency Inversion Principle (DIP)**
   - Controller-ul depinde de interfață, nu de implementare
   - Permite înlocuirea implementării

3. **Open/Closed Principle (OCP)**
   - Interfața poate fi extinsă cu noi metode
   - Implementările existente nu sunt afectate

##### Consistența Interfața

| Metodă | Parametru | Validare | Return Type |
|--------|-----------|----------|-------------|
| getById | Integer postId | @NotNull | IamResponse<PostDTO> |
| createPost | PostRequest postRequest | @NotNull | IamResponse<PostDTO> |

**Observație**: Pattern consistent facilitează înțelegerea și utilizarea

### 3. PostServiceImpl - Implementarea Logicii

#### Codul Complet al Metodei

```java
@Override
public IamResponse<PostDTO> createPost(@NotNull PostRequest postRequest) {
    Post post = postMapper.createPost(postRequest);

    Post savedPost = postRepository.save(post);
    PostDTO postDTO = postMapper.toPostDTO(savedPost);
    return IamResponse.createSuccessful(postDTO);
}
```

#### Analiza Pas cu Pas

##### Pasul 1: Maparea Request → Entity

```java
Post post = postMapper.createPost(postRequest);
```

**Ce se întâmplă**:
1. MapStruct creează o instanță nouă de `Post`
2. Copiază câmpurile din `PostRequest`:
   - `title` → `post.title`
   - `content` → `post.content`
   - `likes` → `post.likes`
3. Ignoră câmpurile specificate în mapper:
   - `id` rămâne null (va fi generat de DB)
   - `created` rămâne null (va fi setat de DB)

**Starea obiectului `post`**:
```java
Post {
    id: null,
    title: "Titlul din request",
    content: "Conținutul din request",
    likes: 0,
    created: null
}
```

##### Pasul 2: Salvarea în Baza de Date

```java
Post savedPost = postRepository.save(post);
```

**Proces detaliat**:

1. **JPA analiza obiectul**:
   - `id` este null → consideră obiectul ca fiind NOU
   - Alege operația INSERT în loc de UPDATE

2. **Hibernate generează SQL**:
   ```sql
   INSERT INTO posts (title, content, likes, created)
   VALUES (?, ?, ?, CURRENT_TIMESTAMP);
   ```

3. **Baza de date execută**:
   - Inserează înregistrarea
   - Generează id-ul (SERIAL sau IDENTITY)
   - Setează timestamp-ul `created` (DEFAULT CURRENT_TIMESTAMP)

4. **JPA returnează obiectul sincronizat**:
   ```java
   Post {
       id: 1,  // Generat de DB
       title: "Titlul din request",
       content: "Conținutul din request",
       likes: 0,
       created: Timestamp("2025-10-03 21:48:33")  // Setat de DB
   }
   ```

**Importanța variabilei `savedPost`**:
- Conține id-ul generat
- Conține timestamp-ul setat de DB
- Reprezintă starea reală din baza de date

##### Pasul 3: Maparea Entity → DTO

```java
PostDTO postDTO = postMapper.toPostDTO(savedPost);
```

**Proces**:
1. MapStruct creează o instanță de `PostDTO`
2. Mapează toate câmpurile, inclusiv:
   - `id` (acum disponibil)
   - `created` (formatat conform pattern-ului din mapper)

**Rezultat**:
```java
PostDTO {
    id: 1,
    title: "Titlul din request",
    content: "Conținutul din request",
    likes: 0,
    created: "2025-10-03T21:48:33"  // Formatat ca string
}
```

##### Pasul 4: Învelierea în IamResponse

```java
return IamResponse.createSuccessful(postDTO);
```

**Structura finală**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Titlul din request",
    "content": "Conținutul din request",
    "likes": 0,
    "created": "2025-10-03T21:48:33"
  },
  "error": null
}
```

#### Flux Complet de Date

```
PostRequest (din client)
    ↓
Post (entitate nouă, id=null)
    ↓
Post (entitate salvată, id=1, created=timestamp)
    ↓
PostDTO (pentru client)
    ↓
IamResponse<PostDTO> (wrapper)
```

#### Comparație cu getById()

| Aspect | getById() | createPost() |
|--------|-----------|--------------|
| **Input** | Integer postId | PostRequest postRequest |
| **Mapări** | 1 (Post → PostDTO) | 2 (Request → Post, Post → DTO) |
| **Operație DB** | findById() | save() |
| **Excepții** | NotFoundException posibilă | Constraint violations posibile |
| **Complexitate** | Simplă (read) | Medie (create + 2 mappings) |

#### Gestionarea Erorilor (Implicită)

În acest branch, **nu există gestionare explicită a erorilor** pentru createPost, dar există mecanisme implicite:

1. **Validare null**: `@NotNull` pe parametru
2. **Constraint violations**: Gestionate de Spring/Hibernate
3. **Database errors**: Propagate către `CommonControllerAdvice`

**Erori posibile**:
- Parametru null → `ConstraintViolationException`
- Duplicate key → `DataIntegrityViolationException`
- Database down → `DataAccessException`

### 4. PostMapper - Adăugarea Metodei de Mapare

#### Noua Metodă

```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "created", ignore = true)
Post createPost(PostRequest postRequest);
```

#### Analiza Detaliată

##### Adnotările @Mapping

###### 1. @Mapping(target = "id", ignore = true)

**Explicație**:
- **target**: Câmpul din clasa destinație (`Post.id`)
- **ignore = true**: Nu mapează acest câmp, lasă-l null

**Motivație**:
- `PostRequest` nu conține câmpul `id`
- `id`-ul va fi generat de baza de date (SERIAL/IDENTITY)
- Clientul nu trebuie să specifice id-ul

**Fără această adnotare**:
- MapStruct ar căuta un câmp `id` în `PostRequest`
- Nu găsind, ar seta `Post.id` la null (comportament implicit)
- Adnotarea face **intențiunea explicită**

###### 2. @Mapping(target = "created", ignore = true)

**Explicație**:
- **target**: Câmpul `Post.created`
- **ignore = true**: Nu seta acest câmp

**Motivație**:
- Timestamp-ul este gestionat de baza de date
- Columnul are `DEFAULT CURRENT_TIMESTAMP`
- Aplicația nu trebuie să seteze valoarea

**Alternativa fără DB default**:
```java
@Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
```

##### Maparea Implicită

Pentru câmpurile **nespecificate**, MapStruct mapează automat **by name**:

```java
// Implicit generat de MapStruct
Post post = new Post();
post.setTitle(postRequest.getTitle());       // Automat
post.setContent(postRequest.getContent());   // Automat
post.setLikes(postRequest.getLikes());       // Automat
// post.setId(...) - IGNORAT
// post.setCreated(...) - IGNORAT
```

##### Configurația Mapper-ului

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    imports = {DateTimeUtils.class, Objects.class}
)
```

**Implicații pentru createPost()**:

1. **componentModel = "spring"**:
   - MapStruct generează un Spring Bean
   - Poate fi injectat cu `@Autowired` sau prin constructor

2. **nullValuePropertyMappingStrategy = IGNORE**:
   - Dacă un câmp din `PostRequest` este null, nu suprascrie valoarea din `Post`
   - Util pentru UPDATE (nu în CREATE unde totul e nou)

3. **imports**:
   - Clase disponibile în expresii
   - Nu utilizate direct în `createPost()`, dar disponibile

##### Codul Generat de MapStruct

MapStruct generează o implementare la compile-time:

```java
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public Post createPost(PostRequest postRequest) {
        if (postRequest == null) {
            return null;
        }

        Post post = new Post();

        // Mapare explicită conform adnotărilor
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setLikes(postRequest.getLikes());

        // id și created sunt IGNORATE (nu se setează)

        return post;
    }

    @Override
    public PostDTO toPostDTO(Post post) {
        // ... implementare existentă
    }
}
```

##### Comparație cu toPostDTO()

| Aspect | toPostDTO(Post) | createPost(PostRequest) |
|--------|-----------------|-------------------------|
| **Direcția** | Entity → DTO | Request → Entity |
| **Câmpuri mapate** | Toate (5) | 3 din 5 |
| **Câmpuri ignorate** | Niciunul | id, created |
| **Formatare** | created (date format) | Niciuna |
| **Scop** | Output pentru client | Pregătire pentru DB |

#### Design Decisions

##### De ce o metodă separată pentru create?

```java
// Alternativa: o singură metodă
Post toPost(PostRequest postRequest);  // Generic

// Alegerea: metodă specializată
Post createPost(PostRequest postRequest);  // Specific
```

**Avantaje metodă specializată**:
1. **Intent clar**: Numele indică operația (create)
2. **Flexibilitate**: Permite altă metodă pentru update
3. **Validări diferite**: Pot fi adăugate validări specifice

##### De ce ignoră câmpuri în loc de default?

```java
// Opțiunea 1: Ignoră (folosită)
@Mapping(target = "id", ignore = true)

// Opțiunea 2: Setează default
@Mapping(target = "id", constant = "0")

// Opțiunea 3: Expresie
@Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
```

**Motivația pentru IGNORE**:
- Lasă responsabilitatea la baza de date
- Evită inconsistențe (aplicație vs DB)
- Mai puțin cod de întreținut

---

## Fluxul de Date

### Fluxul Complet al unei Cereri POST

```
┌──────────────────────────────────────────────────────────────┐
│                         CLIENT                                │
│  POST /posts/create                                           │
│  Content-Type: application/json                               │
│  {                                                            │
│    "title": "Titlu",                                          │
│    "content": "Conținut",                                     │
│    "likes": 0                                                 │
│  }                                                            │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│              SPRING BOOT (DispatcherServlet)                  │
│  - Primește request-ul HTTP                                   │
│  - Identifică controller-ul și metoda potrivită               │
│  - Deserializează JSON → PostRequest                          │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│                    PostController                             │
│  @PostMapping("${end.points.create}")                         │
│  public ResponseEntity<IamResponse<PostDTO>> createPost(      │
│          @RequestBody PostRequest postRequest)                │
│                                                               │
│  1. Logging: log.trace(...)                                   │
│  2. Delegare: postService.createPost(postRequest)             │
│  3. Return: ResponseEntity.ok(response)                       │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│                  PostServiceImpl                              │
│  @Override                                                    │
│  public IamResponse<PostDTO> createPost(                      │
│          @NotNull PostRequest postRequest)                    │
│                                                               │
│  Pasul 1: Post post = postMapper.createPost(postRequest)     │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│                     PostMapper                                │
│  @Mapping(target = "id", ignore = true)                       │
│  @Mapping(target = "created", ignore = true)                  │
│  Post createPost(PostRequest postRequest)                     │
│                                                               │
│  Creează: Post {                                              │
│    id: null,                                                  │
│    title: "Titlu",                                            │
│    content: "Conținut",                                       │
│    likes: 0,                                                  │
│    created: null                                              │
│  }                                                            │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓ (înapoi la PostServiceImpl)
┌──────────────────────────────────────────────────────────────┐
│                  PostServiceImpl (cont.)                      │
│  Pasul 2: Post savedPost = postRepository.save(post)         │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│              JPA / Hibernate (PostRepository)                 │
│  1. Detectează entitate nouă (id = null)                     │
│  2. Generează SQL INSERT                                      │
│  3. Execută query                                             │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                        │
│  INSERT INTO posts (title, content, likes, created)           │
│  VALUES ('Titlu', 'Conținut', 0, CURRENT_TIMESTAMP);         │
│                                                               │
│  Generează: id = 1 (SERIAL)                                   │
│  Setează: created = '2025-10-03 21:48:33'                    │
│                                                               │
│  Returnează: Post complet cu id și created                    │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓ (înapoi la PostServiceImpl)
┌──────────────────────────────────────────────────────────────┐
│                  PostServiceImpl (cont.)                      │
│  Pasul 3: PostDTO postDTO = postMapper.toPostDTO(savedPost) │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│                     PostMapper                                │
│  PostDTO toPostDTO(Post post)                                 │
│                                                               │
│  Creează: PostDTO {                                           │
│    id: 1,                                                     │
│    title: "Titlu",                                            │
│    content: "Conținut",                                       │
│    likes: 0,                                                  │
│    created: "2025-10-03T21:48:33"                            │
│  }                                                            │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓ (înapoi la PostServiceImpl)
┌──────────────────────────────────────────────────────────────┐
│                  PostServiceImpl (cont.)                      │
│  Pasul 4: return IamResponse.createSuccessful(postDTO)       │
│                                                               │
│  Creează: IamResponse {                                       │
│    success: true,                                             │
│    data: PostDTO {...},                                       │
│    error: null                                                │
│  }                                                            │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓ (înapoi la PostController)
┌──────────────────────────────────────────────────────────────┐
│                    PostController (cont.)                     │
│  return ResponseEntity.ok(response)                           │
│                                                               │
│  Creează: ResponseEntity {                                    │
│    status: 200 OK,                                            │
│    headers: {...},                                            │
│    body: IamResponse {...}                                    │
│  }                                                            │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│              SPRING BOOT (DispatcherServlet)                  │
│  - Serializează IamResponse → JSON                            │
│  - Setează Content-Type: application/json                     │
│  - Trimite response-ul                                        │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────────┐
│                         CLIENT                                │
│  HTTP/1.1 200 OK                                              │
│  Content-Type: application/json                               │
│  {                                                            │
│    "success": true,                                           │
│    "data": {                                                  │
│      "id": 1,                                                 │
│      "title": "Titlu",                                        │
│      "content": "Conținut",                                   │
│      "likes": 0,                                              │
│      "created": "2025-10-03T21:48:33"                        │
│    },                                                         │
│    "error": null                                              │
│  }                                                            │
└──────────────────────────────────────────────────────────────┘
```

### Transformările Obiectelor

```
PostRequest              Post (nou)           Post (salvat)         PostDTO
-----------              ----------           -------------         -------
title: "Titlu"    →      title: "Titlu"  →    title: "Titlu"   →    title: "Titlu"
content: "..."    →      content: "..."  →    content: "..."   →    content: "..."
likes: 0          →      likes: 0        →    likes: 0         →    likes: 0
                         id: null        →    id: 1            →    id: 1
                         created: null   →    created: TS      →    created: "2025-..."

      │                       │                     │                    │
      │                       │                     │                    │
    CLIENT               MAPPER              DATABASE              MAPPER
    INPUT            (createPost)            (save)           (toPostDTO)
```

---

## Integrarea cu MapStruct

### Rolul MapStruct în Acest Branch

MapStruct joacă un rol **central** în implementarea funcționalității de creare, facilitând **două transformări cruciale**:

1. **PostRequest → Post**: Pregătirea datelor pentru persistență
2. **Post → PostDTO**: Pregătirea răspunsului pentru client

### Configurația MapStruct

#### pom.xml Dependencies

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>

<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>${mapstruct.version}</version>
    <scope>provided</scope>
</dependency>
```

#### Procesarea la Compile-Time

MapStruct generează implementări **la momentul compilării**:

```
Source Code              Compilation              Runtime
-----------              -----------              -------
PostMapper.java    →    PostMapperImpl.java  →   Spring Bean
(interfață)             (implementare gen.)       (injectat)
```

**Avantaje**:
- **Performanță**: Fără reflection la runtime
- **Type safety**: Erori detectate la compilare
- **Debugging**: Cod generat vizibil și debuggable

### Strategii de Mapare

#### 1. Mapare Explicită cu @Mapping

```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "created", ignore = true)
Post createPost(PostRequest postRequest);
```

**Când se folosește**:
- Câmpuri care trebuie ignorate
- Transformări custom
- Formatări speciale

#### 2. Mapare Implicită By-Name

```java
// Fără adnotări pentru title, content, likes
// MapStruct mapează automat dacă numele coincid
```

**Condiții**:
- Nume identice în sursă și destinație
- Tipuri compatibile
- Getters și setters accesibili

#### 3. Mapare cu Formatare

```java
// În toPostDTO()
@Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
PostDTO toPostDTO(Post post);
```

**Utilitate**:
- Conversii de tip (Timestamp → String)
- Formatări (date, numere)
- Transformări simple

### Null Handling Strategy

```java
@Mapper(
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    ...
)
```

**Comportament**:
- Dacă `postRequest.getTitle()` returnează `null`, `post.title` rămâne neatins
- Util pentru UPDATE (unde null înseamnă "nu modifica")
- Pentru CREATE, toate câmpurile sunt noi, deci impact minim

### Avantajele MapStruct față de Alte Soluții

#### Comparație cu Alternative

| Aspect | MapStruct | Mapare Manuală | ModelMapper | Dozer |
|--------|-----------|----------------|-------------|-------|
| **Performanță** | Excelentă | Excelentă | Medie | Medie |
| **Compile-time safety** | Da | Da | Nu | Nu |
| **Cod boilerplate** | Minim | Mult | Minim | Minim |
| **Debugging** | Ușor | Ușor | Dificil | Dificil |
| **Flexibilitate** | Mare | Maximă | Medie | Medie |
| **Learning curve** | Mediu | Minim | Mediu | Mediu |

#### Exemplu de Mapare Manuală (pentru comparație)

```java
// Fără MapStruct
public Post createPost(PostRequest postRequest) {
    Post post = new Post();
    post.setTitle(postRequest.getTitle());
    post.setContent(postRequest.getContent());
    post.setLikes(postRequest.getLikes());
    // id și created rămân null
    return post;
}

// Cu MapStruct
@Mapping(target = "id", ignore = true)
@Mapping(target = "created", ignore = true)
Post createPost(PostRequest postRequest);
```

**Observații**:
- Cod mai puțin pentru MapStruct
- Intențiile mai clare cu adnotări
- Mai puține șanse de erori (uitarea unui câmp)

---

## Validarea și Gestionarea Erorilor

### Validarea Implicită

În acest branch, validarea este **minimalistă**, bazându-se pe:

1. **@NotNull la nivel de service**:
   ```java
   IamResponse<PostDTO> createPost(@NotNull PostRequest postRequest);
   ```
   - Previne apeluri cu parametru null
   - Aruncă `ConstraintViolationException` dacă se încalcă

2. **Validări de bază de date**:
   - Constraint-uri (NOT NULL, UNIQUE, etc.)
   - Foreign keys
   - Check constraints

### Scenarii de Eroare

#### 1. Request Body Lipsa

**Request**:
```http
POST /posts/create HTTP/1.1
Content-Type: application/json

(body gol sau lipsă)
```

**Răspuns**:
- Status: 400 Bad Request
- Mesaj: "Required request body is missing"

#### 2. JSON Invalid

**Request**:
```http
POST /posts/create HTTP/1.1
Content-Type: application/json

{
  "title": "Titlu",
  "content": "Conținut"
  // virgulă lipsă sau sintaxă greșită
  "likes": 0
}
```

**Răspuns**:
- Status: 400 Bad Request
- Mesaj: JSON parse error

#### 3. Tipuri Incorecte

**Request**:
```json
{
  "title": "Titlu",
  "content": "Conținut",
  "likes": "zero"  // String în loc de Integer
}
```

**Răspuns**:
- Status: 400 Bad Request
- Mesaj: Type mismatch

#### 4. Câmpuri Null

**Request**:
```json
{
  "title": null,
  "content": "Conținut",
  "likes": 0
}
```

**Comportament**:
- Depinde de constraint-urile din DB
- Dacă `title` are `NOT NULL` în DB → `DataIntegrityViolationException`
- Interceptată de `CommonControllerAdvice`

### Gestionarea Erorilor prin CommonControllerAdvice

Deși nu e modificat în acest branch, `CommonControllerAdvice` (din branch-ul 5-23) gestionează erorile:

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<IamResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
    // Returnează răspuns structurat
}

@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<IamResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    // Gestionează erori de bază de date
}
```

### Îmbunătățiri Posibile pentru Validare

În branch-uri viitoare (probabil 5-26-Validation-NotNull), se pot adăuga:

```java
public class PostRequest {
    @NotNull(message = "Title cannot be null")
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotNull(message = "Content cannot be null")
    @NotBlank(message = "Content cannot be blank")
    private String content;

    @Min(value = 0, message = "Likes cannot be negative")
    private Integer likes;
}
```

Și în controller:
```java
public ResponseEntity<IamResponse<PostDTO>> createPost(
        @Valid @RequestBody PostRequest postRequest) {
    // @Valid activează validarea
}
```

---

## Configurarea Endpoint-urilor

### Modificarea application.properties

#### Linia Adăugată

```properties
end.points.create=/create
```

#### Structura Completă a Endpoint-urilor

```properties
# Endpoint de bază pentru posts
end.point.posts=/posts

# Endpoint pentru ID specific
end.point.id=/{id}

# Endpoint pentru creare
end.points.create=/create
```

### Utilizarea în Controller

```java
@RestController
@RequestMapping("${end.point.posts}")  // /posts
public class PostController {

    @GetMapping("${end.point.id}")  // /posts/{id}
    public ResponseEntity<IamResponse<PostDTO>> getPostById(...) { ... }

    @PostMapping("${end.points.create}")  // /posts/create
    public ResponseEntity<IamResponse<PostDTO>> createPost(...) { ... }
}
```

### URL-uri Finale

| Operație | Metodă HTTP | URL | Descriere |
|----------|-------------|-----|-----------|
| Get by ID | GET | /posts/{id} | Obține o postare specifică |
| Create | POST | /posts/create | Creează o postare nouă |

### Avantajele Externalizării Configurației

#### 1. Flexibilitate

Schimbarea URL-urilor fără modificarea codului:

```properties
# Development
end.points.create=/create

# Production (versioning API)
end.points.create=/v1/create
```

#### 2. Consistență

Definirea o singură dată, utilizare multiplă:

```java
@PostMapping("${end.points.create}")  // În PostController
// Același path poate fi referențiat în documentație, teste, etc.
```

#### 3. Profile-uri Multiple

```properties
# application-dev.properties
end.points.create=/create

# application-prod.properties
end.points.create=/api/v1/posts/create
```

#### 4. Mentenanță Ușoară

Schimbarea unei singure valori actualizează toate referințele.

### Design API RESTful

#### Alternativa REST Pur

În REST pur, crearea ar folosi:

```http
POST /posts HTTP/1.1
```

**Nu**:
```http
POST /posts/create HTTP/1.1
```

**Motivație REST pur**:
- Metoda HTTP (POST) indică acțiunea
- URL-ul indică resursa
- Nu e nevoie de verb în URL

**Motivația abordării actuale**:
- **Claritate**: URL-ul e explicit despre acțiune
- **Compatibilitate**: Ușor de extins cu alte acțiuni
- **Documentare**: Mai evident pentru developeri

#### Evoluția Posibilă

```properties
# Endpoint-uri actuale
end.point.posts=/posts
end.point.id=/{id}
end.points.create=/create

# Endpoint-uri viitoare (speculativ)
end.points.update=/update/{id}
end.points.delete=/delete/{id}
end.points.search=/search
end.points.list=/list
```

---

## Impactul asupra Arhitecturii

### Îmbunătățiri Arhitecturale

#### 1. Pattern Request/Response

Introducerea `PostRequest` stabilește un **pattern clar**:

```
REQUEST MODELS         DOMAIN MODELS        RESPONSE MODELS
(input)                (persistence)        (output)
──────────────         ──────────────       ───────────────
PostRequest            Post                 PostDTO
  ↓                      ↓                     ↓
Validare              Persistență          Serializare
```

**Beneficii**:
- **Separarea preocupărilor**: Fiecare model are un scop specific
- **Securitate**: Clientul nu poate manipula câmpuri sensibile (id, created)
- **Flexibilitate**: Modele diferite pentru create vs update

#### 2. Completarea CRUD

| Operație | Implementată în | Metodă HTTP | Endpoint |
|----------|-----------------|-------------|----------|
| **C**reate | Branch 5-25 | POST | /posts/create |
| **R**ead | Branch 5-22 | GET | /posts/{id} |
| **U**pdate | Viitor | PUT/PATCH | /posts/update/{id} |
| **D**elete | Viitor | DELETE | /posts/delete/{id} |

#### 3. Stratificarea Responsabilităților

```
┌─────────────────────────────────────┐
│   Presentation Layer                │
│   - PostController                  │
│   - HTTP endpoints                  │
│   - Request/Response handling       │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│   Service Layer                     │
│   - PostService (interface)         │
│   - PostServiceImpl                 │
│   - Business logic                  │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│   Mapping Layer                     │
│   - PostMapper (MapStruct)          │
│   - Object transformations          │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│   Data Access Layer                 │
│   - PostRepository (JPA)            │
│   - Database operations             │
└──────────────┬──────────────────────┘
               │
┌──────────────┴──────────────────────┐
│   Database Layer                    │
│   - PostgreSQL                      │
│   - Data persistence                │
└─────────────────────────────────────┘
```

### Principii SOLID Respectate

#### 1. Single Responsibility Principle (SRP)

Fiecare clasă are o singură responsabilitate:

- **PostRequest**: Transportă date de input
- **Post**: Reprezintă entitatea de bază de date
- **PostDTO**: Transportă date de output
- **PostController**: Gestionează HTTP
- **PostService**: Conține logică de business
- **PostMapper**: Transformă obiecte

#### 2. Open/Closed Principle (OCP)

- Sistemul e deschis pentru extindere (noi endpoint-uri, noi validări)
- Închis pentru modificare (funcționalitatea existentă nu e afectată)

#### 3. Liskov Substitution Principle (LSP)

- `PostServiceImpl` poate înlocui `PostService` fără probleme
- Implementări alternative sunt posibile

#### 4. Interface Segregation Principle (ISP)

- Interfața `PostService` conține doar metode relevante
- Fără metode inutile forțate pe implementări

#### 5. Dependency Inversion Principle (DIP)

- `PostController` depinde de abstracția `PostService`, nu de `PostServiceImpl`
- Facilitează testarea și schimbarea implementărilor

### Scalabilitate

#### Adăugarea de Noi Entități

Pattern-ul stabilit poate fi replicat:

```
PostRequest     →     UserRequest
PostDTO         →     UserDTO
PostController  →     UserController
PostService     →     UserService
PostMapper      →     UserMapper
```

#### Extinderea Funcționalității Post

Adăugarea de noi operații e simplă:

```java
// În PostService
IamResponse<PostDTO> updatePost(Integer id, UpdatePostRequest request);
IamResponse<Void> deletePost(Integer id);
IamResponse<List<PostDTO>> getAllPosts();
```

---

## Comparație cu Funcționalitatea GET

### Similarități

| Aspect | GET | POST |
|--------|-----|------|
| **Controller** | Metodă în PostController | Metodă în PostController |
| **Service** | Metodă în PostService | Metodă în PostService |
| **Mapper** | Utilizează PostMapper | Utilizează PostMapper |
| **Response** | IamResponse<PostDTO> | IamResponse<PostDTO> |
| **Logging** | log.trace(...) | log.trace(...) |

### Diferențe

| Aspect | GET | POST |
|--------|-----|------|
| **Metodă HTTP** | GET | POST |
| **Input** | @PathVariable Integer | @RequestBody PostRequest |
| **URL** | /posts/{id} | /posts/create |
| **Mapări** | 1 (Post → DTO) | 2 (Request → Post, Post → DTO) |
| **Operație DB** | findById() | save() |
| **Rezultat** | Întoarce existent | Creează nou |
| **Idempotență** | Da | Nu |

### Complementaritate

```
GET /posts/1               POST /posts/create
     ↓                            ↓
Verifică existența    →    Creează postare
     ↓                            ↓
200 OK + PostDTO      ←    200 OK + PostDTO (cu id nou)
```

**Flux tipic**:
1. Client creează postare: `POST /posts/create`
2. Server returnează: `{ "id": 1, ... }`
3. Client verifică postarea: `GET /posts/1`
4. Server returnează aceeași postare

---

## Scenarii de Utilizare

### Scenariul 1: Crearea unei Postări Simple

#### Request

```http
POST /posts/create HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "title": "Primul meu post",
  "content": "Acesta este conținutul primului meu post pe platformă.",
  "likes": 0
}
```

#### Procesare

1. Spring deserializează JSON în `PostRequest`
2. Controller-ul apelează `postService.createPost(request)`
3. Service-ul mapează `PostRequest` → `Post`
4. Repository-ul salvează în DB
5. DB generează `id = 1` și `created = current_timestamp`
6. Service-ul mapează `Post` → `PostDTO`
7. Response-ul e învelit în `IamResponse`

#### Response

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "data": {
    "id": 1,
    "title": "Primul meu post",
    "content": "Acesta este conținutul primului meu post pe platformă.",
    "likes": 0,
    "created": "2025-10-03T21:48:33"
  },
  "error": null
}
```

### Scenariul 2: Crearea Multiplelor Postări

#### Request 1

```json
{
  "title": "Post despre Java",
  "content": "Java este un limbaj versatil...",
  "likes": 5
}
```

**Response**: `{ "id": 1, ... }`

#### Request 2

```json
{
  "title": "Post despre Spring Boot",
  "content": "Spring Boot simplifică dezvoltarea...",
  "likes": 10
}
```

**Response**: `{ "id": 2, ... }`

#### Request 3

```json
{
  "title": "Post despre REST APIs",
  "content": "REST APIs sunt standardul pentru...",
  "likes": 8
}
```

**Response**: `{ "id": 3, ... }`

**Observație**: `id`-urile sunt generate secvențial de DB.

### Scenariul 3: Câmpuri Opționale

#### Request cu likes null

```json
{
  "title": "Post fără likes",
  "content": "Acest post nu are likes specificate."
}
```

**Comportament**:
- `likes` e null în `PostRequest`
- Maparea setează `post.likes = null`
- Dacă coloana DB permite NULL → salvează null
- Dacă coloana DB are DEFAULT → folosește valoarea default

### Scenariul 4: Integrare cu Frontend

#### Cod JavaScript (fetch API)

```javascript
async function createPost(title, content, likes) {
  try {
    const response = await fetch('http://localhost:8080/posts/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ title, content, likes })
    });

    const result = await response.json();

    if (result.success) {
      console.log('Post creat cu succes!', result.data);
      return result.data;  // PostDTO cu id generat
    } else {
      console.error('Eroare la crearea postării:', result.error);
      return null;
    }
  } catch (error) {
    console.error('Eroare de rețea:', error);
    return null;
  }
}

// Utilizare
const newPost = await createPost(
  'Titlul postării',
  'Conținutul postării',
  0
);

console.log('ID-ul postării create:', newPost.id);
```

#### Cod React (useState + useEffect)

```jsx
import React, { useState } from 'react';

function CreatePostForm() {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [likes, setLikes] = useState(0);
  const [createdPost, setCreatedPost] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const response = await fetch('http://localhost:8080/posts/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, content, likes })
    });

    const result = await response.json();

    if (result.success) {
      setCreatedPost(result.data);
      // Resetează formularul
      setTitle('');
      setContent('');
      setLikes(0);
    }
  };

  return (
    <div>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Titlu"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          placeholder="Conținut"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <input
          type="number"
          placeholder="Likes"
          value={likes}
          onChange={(e) => setLikes(Number(e.target.value))}
        />
        <button type="submit">Creează Postare</button>
      </form>

      {createdPost && (
        <div>
          <h3>Postare creată cu succes!</h3>
          <p>ID: {createdPost.id}</p>
          <p>Titlu: {createdPost.title}</p>
          <p>Creat la: {createdPost.created}</p>
        </div>
      )}
    </div>
  );
}
```

---

## Teste și Validare

### Teste Manuale cu cURL

#### Test 1: Creare Standard

```bash
curl -X POST http://localhost:8080/posts/create \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Post",
    "content": "This is a test post",
    "likes": 0
  }'
```

**Expected Output**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Test Post",
    "content": "This is a test post",
    "likes": 0,
    "created": "2025-10-03T21:48:33"
  },
  "error": null
}
```

#### Test 2: Verificarea Postării Create

```bash
# Obține ID-ul din răspunsul anterior (ex: 1)
curl -X GET http://localhost:8080/posts/1
```

**Expected Output**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Test Post",
    "content": "This is a test post",
    "likes": 0,
    "created": "2025-10-03T21:48:33"
  },
  "error": null
}
```

### Teste cu Postman

#### Test Collection

```json
{
  "info": {
    "name": "IAM Service - Post Creation",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Post - Success",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"title\": \"Postman Test Post\",\n  \"content\": \"Created via Postman\",\n  \"likes\": 5\n}"
        },
        "url": {
          "raw": "http://localhost:8080/posts/create",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["posts", "create"]
        }
      },
      "response": []
    },
    {
      "name": "Create Post - With Null Likes",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"title\": \"Post Without Likes\",\n  \"content\": \"No likes specified\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/posts/create",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["posts", "create"]
        }
      },
      "response": []
    }
  ]
}
```

### Teste Unitare (Speculativ)

Deși nu sunt implementate în acest branch, teste unitare ar arăta astfel:

#### Test pentru PostServiceImpl

```java
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void createPost_Success() {
        // Arrange
        PostRequest request = new PostRequest("Title", "Content", 0);
        Post post = new Post();
        post.setTitle("Title");
        post.setContent("Content");
        post.setLikes(0);

        Post savedPost = new Post();
        savedPost.setId(1);
        savedPost.setTitle("Title");
        savedPost.setContent("Content");
        savedPost.setLikes(0);
        savedPost.setCreated(LocalDateTime.now());

        PostDTO expectedDTO = new PostDTO(1, "Title", "Content", 0, "2025-10-03T21:48:33");

        when(postMapper.createPost(request)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(savedPost);
        when(postMapper.toPostDTO(savedPost)).thenReturn(expectedDTO);

        // Act
        IamResponse<PostDTO> response = postService.createPost(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(expectedDTO, response.getData());

        verify(postMapper).createPost(request);
        verify(postRepository).save(post);
        verify(postMapper).toPostDTO(savedPost);
    }

    @Test
    void createPost_NullRequest_ThrowsException() {
        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> {
            postService.createPost(null);
        });
    }
}
```

#### Test pentru PostController

```java
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void createPost_Success() throws Exception {
        // Arrange
        PostRequest request = new PostRequest("Title", "Content", 0);
        PostDTO postDTO = new PostDTO(1, "Title", "Content", 0, "2025-10-03T21:48:33");
        IamResponse<PostDTO> response = IamResponse.createSuccessful(postDTO);

        when(postService.createPost(any(PostRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/posts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Title\",\"content\":\"Content\",\"likes\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Title"));
    }

    @Test
    void createPost_InvalidJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/posts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}
```

### Teste de Integrare (Speculativ)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    void createPost_IntegrationTest() throws Exception {
        // Arrange
        String requestBody = """
            {
                "title": "Integration Test Post",
                "content": "This is an integration test",
                "likes": 10
            }
            """;

        // Act
        MvcResult result = mockMvc.perform(post("/posts/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // Assert - Verifică răspunsul
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("\"success\":true");
        assertThat(responseBody).contains("Integration Test Post");

        // Assert - Verifică baza de date
        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("Integration Test Post");
        assertThat(posts.get(0).getLikes()).isEqualTo(10);
    }
}
```

---

## Concluzii și Perspective

### Realizări Cheie

Branch-ul **5-25-Post-request** reprezintă o **etapă fundamentală** în dezvoltarea aplicației IAM_SERVICE:

1. **Funcționalitate Completă de Creare**
   - Endpoint POST funcțional
   - Persistența datelor în baza de date
   - Răspuns structurat cu postarea creată

2. **Arhitectură Bine Concepută**
   - Separarea clară între Request, Domain și Response models
   - Respectarea principiilor SOLID
   - Pattern-uri consistente și reutilizabile

3. **Integrare Eficientă**
   - MapStruct pentru mapări performante
   - JPA pentru persistență
   - Spring Boot pentru orchestrare

4. **Fundamentul pentru Extindere**
   - Pattern-ul stabilit poate fi replicat pentru UPDATE și DELETE
   - Structura permite adăugarea ușoară de validări
   - Arhitectura suportă scalabilitate

### Limitări Actuale

1. **Validare Minimă**
   - Lipsă validări la nivel de câmpuri
   - Fără restricții de lungime sau format
   - Posibilitatea de date invalide

2. **Status Code Neoptim**
   - Folosește 200 OK în loc de 201 Created
   - Lipsă Location header pentru resursa creată

3. **Gestionarea Erorilor**
   - Bazare pe mecanisme implicite
   - Mesaje de eroare generice
   - Fără erori custom pentru create

4. **Likes Controlat de Client**
   - Clientul poate seta orice număr de likes
   - În practică, ar trebui să înceapă de la 0 și să fie gestionat de sistem

### Direcții de Dezvoltare

#### 1. Branch Următor: Validare (5-26-Validation-NotNull)

Se așteaptă adăugarea de:
```java
@Valid @RequestBody PostRequest postRequest
```

Cu validări în `PostRequest`:
```java
@NotNull
@NotBlank
@Size(min = 3, max = 200)
private String title;
```

#### 2. Operații CRUD Rămase

- **UPDATE** (5-27): `PUT /posts/update/{id}`
- **DELETE** (5-28): `DELETE /posts/delete/{id}`
- **LIST** (5-29): `GET /posts/list` (cu paginare)

#### 3. Funcționalități Avansate

- **Căutare și Filtrare** (5-30): Endpoint-uri pentru search
- **Sorting**: Ordonarea rezultatelor
- **Paginare**: Gestionarea listelor mari de postări

#### 4. Entități Noi

- **User**: Autorul postărilor (branch-uri 6-33 onwards)
- **Comment**: Comentarii la postări
- **Role**: Sistem de roluri și permisiuni

### Lecții Învățate

1. **Pattern-urile Contează**
   - Un pattern bine stabilit facilitează dezvoltarea viitoare
   - Consistența reduce erori și îmbunătățește mentenabilitatea

2. **Separarea Modelelor**
   - Request, Domain și Response models au roluri distincte
   - Separarea oferă securitate și flexibilitate

3. **Tooling-ul Potrivit**
   - MapStruct reduce codul boilerplate
   - Spring Boot simplifică configurarea
   - JPA abstractizează accesul la date

4. **Arhitectura Stratificată**
   - Fiecare layer are responsabilități clare
   - Testarea și mentenanța sunt facilitate
   - Scalabilitatea este îmbunătățită

### Importanța în Contextul Proiectului

Acest branch marchează **trecerea de la read-only la operații de modificare**. Este un moment crucial în dezvoltarea oricărei aplicații:

- **Înainte**: Aplicația putea doar citi date
- **Acum**: Aplicația poate crea date
- **Viitor**: Aplicația va putea modifica și șterge date

Funcționalitatea de creare este **fundația** pentru toate operațiile ulterioare și stabilește **standardele de calitate** pentru implementările viitoare.

### Valoarea Adăugată

Branch-ul **5-25-Post-request** aduce valoare prin:

1. **Completarea ciclului de date**: Citire + Scriere
2. **Stabilirea pattern-urilor**: Modele de implementare clare
3. **Demonstrarea integrării**: MapStruct + JPA + Spring Boot
4. **Pregătirea terenului**: Pentru funcționalități mai complexe

---

## Rezumat Tehnic

### Componente Adăugate

1. **PostRequest.java**: Model pentru request-uri de creare
2. **PostMapper.createPost()**: Metodă de mapare Request → Entity
3. **PostService.createPost()**: Semnătură în interfața de service
4. **PostServiceImpl.createPost()**: Implementarea logicii de creare
5. **PostController.createPost()**: Endpoint HTTP POST
6. **application.properties**: Configurarea endpoint-ului `/create`

### Fluxul de Date

```
JSON Request → PostRequest → Post (new) → Post (saved) → PostDTO → IamResponse → JSON Response
```

### Operații de Bază de Date

```sql
-- Generat de Hibernate
INSERT INTO posts (title, content, likes, created)
VALUES (?, ?, ?, CURRENT_TIMESTAMP)
RETURNING id, created;
```

### Endpoint-uri

```
POST /posts/create
Request Body: { "title": "...", "content": "...", "likes": 0 }
Response: { "success": true, "data": { "id": 1, ... }, "error": null }
```

### Statistici Finale

- **Fișiere modificate**: 6
- **Linii de cod adăugate**: 45
- **Linii de cod șterse**: 4
- **Complexitate**: Medie
- **Impact**: Mare (funcționalitate nouă esențială)

---

**Concluzie**: Branch-ul 5-25-Post-request reprezintă o implementare solidă și bine gândită a funcționalității de creare postări, stabilind fundamentul pentru operațiile CRUD complete și demonstrând o arhitectură scalabilă și mentenabilă.

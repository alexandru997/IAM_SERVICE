# Branch: 4-18-Entity

## 📋 Informații Generale
- **Status**: ✅ MERGED (PR #6)
- **Bazat pe**: 4-17-SQL (după merge în master)
- **Commits**: 1
- **Fișiere modificate**: 1 (nou)
- **Linii de cod**: +26
- **Data merge**: 1 Octombrie 2025

## 🎯 Scopul Branch-ului

Acest branch introduce **prima entitate JPA** (`Post`) care mapează la tabelul `posts` din PostgreSQL. Marchează tranziția de la SQL pur la **Object-Relational Mapping (ORM)** prin Hibernate/JPA.

### Motivație
- **Crearea primei entități JPA** - mapare Java class ↔ database table
- **Introducerea Lombok** - reducere boilerplate (getters/setters)
- **Object-Oriented persistence** - lucru cu obiecte în loc de SQL queries
- **Fundație pentru Repository pattern** - permite JpaRepository în branch-uri viitoare

## ✨ Modificări Implementate

### 1. Entitatea Post
**Fișier**: `iam_Service/src/main/java/com/post_hub/iam_Service/model/enteties/Post.java` ⭐ **NOU**

```java
package com.post_hub.iam_Service.model.enteties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime create = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "integer default 0")
    private String content;
}
```

**Mapare la tabelul posts:**
| Field Java | Annotation | Database Column | Type în DB |
|------------|------------|-----------------|------------|
| `id` | `@Id` `@GeneratedValue` | `id` | BIGSERIAL (PostgreSQL) |
| `title` | `@Column(nullable = false)` | `title` | VARCHAR(255) |
| `create` | `@Column(nullable = false, updatable = false)` | `created` | TIMESTAMP |
| `content` | `@Column(nullable = false, columnDefinition = ...)` | `content` | TEXT |

⚠️ **OBSERVAȚII IMPORTANTE - Issues în Cod:**

1. **Typo în numele field-ului:**
   ```java
   private LocalDateTime create = LocalDateTime.now();
   // ❌ Ar trebui: "created" (pentru consistență cu DB schema)
   ```

2. **Type incorect pentru `content`:**
   ```java
   private String content;  // ✅ Corect - este String

   // Dar annotation-ul este greșit:
   @Column(nullable = false, columnDefinition = "integer default 0")
   // ❌ columnDefinition spune "integer" dar field-ul este String
   // ❌ Ar trebui: "text" sau eliminat complet
   ```

3. **Type prea mic pentru `id`:**
   ```java
   private Integer id;
   // ⚠️ În DB este BIGSERIAL (Long în Java)
   // ⚠️ Ar trebui: private Long id;
   ```

4. **Default value la nivel Java:**
   ```java
   private LocalDateTime create = LocalDateTime.now();
   // ⚠️ Se setează la instanțiere, nu la persistare
   // ⚠️ Poate cauza probleme - ar trebui folosit @PrePersist sau să lase DB-ul să seteze
   ```

**Aceste issues sunt probabil intenționate pentru scop educațional** - demonstrând ce **NU** trebuie făcut. Branch-urile viitoare le vor corecta.

## 🔧 Implementare Tehnică Detaliată

### Arhitectură și Pattern-uri

#### 1. JPA Entity Basics

**@Entity Annotation:**
```java
@Entity
@Table(name = "posts")
public class Post { ... }
```

**Ce face `@Entity`:**
- Marchează clasa ca JPA entity (managed by persistence context)
- Hibernate va crea mapping automat între clasă și tabel
- Permite operații CRUD prin EntityManager sau Repositories

**@Table Annotation:**
- Specifică numele tabelului în DB (`posts`)
- Fără `@Table`, Hibernate ar folosi numele clasei (lowercase: `post`)
- Permite customizare: schema, uniqueConstraints, indexes

**Exemplu fără @Table:**
```java
@Entity
public class Post { ... }
// Hibernate ar căuta tabelul "post" (singular, lowercase)
```

#### 2. Primary Key Mapping

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;
```

**@Id** - Marchează primary key field
**@GeneratedValue** - Strategia de generare ID

**GenerationType.IDENTITY:**
- Folosește auto-increment din database (BIGSERIAL în PostgreSQL)
- Database-ul generează ID-ul la INSERT
- Hibernate face SELECT după INSERT pentru a obține ID-ul generat

**Alternative strategies:**
| Strategy | Comportament | Use Case |
|----------|--------------|----------|
| `IDENTITY` | DB auto-increment | PostgreSQL SERIAL, MySQL AUTO_INCREMENT |
| `SEQUENCE` | DB sequence | PostgreSQL, Oracle (mai performant) |
| `TABLE` | Tabel separat pentru ID-uri | Portabilitate cross-database |
| `AUTO` | Hibernate alege automat | Default (nu recomandat - ambiguitate) |

**De ce IDENTITY aici:**
- ✅ Mapează la BIGSERIAL din schema PostgreSQL
- ✅ Simplu de înțeles pentru începători
- ⚠️ **SEQUENCE** ar fi mai performant pentru batch inserts

**Problema cu Integer:**
```java
private Integer id;  // ❌ Integer = max 2.1 billion
// În DB:
id BIGSERIAL  -- PostgreSQL BIGINT = max 9 quintillion

// Ar trebui:
private Long id;  // ✅ Java Long = PostgreSQL BIGINT
```

**Consecințe:**
- Dacă DB-ul ajunge la ID > 2,147,483,647 → **overflow în Java**
- ClassCastException sau data loss

#### 3. Column Mapping

**Mapare simplă:**
```java
@Column(nullable = false)
private String title;
```

**Parametri @Column:**
| Parametru | Valoare | Efect |
|-----------|---------|-------|
| `nullable` | `false` | Generează `NOT NULL` în schema (dacă Hibernate creează tabela) |
| `unique` | `true` | Generează `UNIQUE` constraint |
| `length` | `255` | Pentru VARCHAR - lungime maximă |
| `columnDefinition` | `"TEXT"` | SQL exact pentru coloană (override Hibernate defaults) |
| `updatable` | `false` | Field-ul NU poate fi modificat după INSERT |
| `insertable` | `false` | Field-ul NU poate fi setat la INSERT |

**nullable = false:**
- La nivel JPA: validare înainte de persist
- La nivel DB: `NOT NULL` constraint (dacă Hibernate creează tabela)
- ⚠️ Dacă tabela există deja (din Flyway), acest parametru NU modifică schema

**updatable = false:**
```java
@Column(nullable = false, updatable = false)
private LocalDateTime create = LocalDateTime.now();
```

**Comportament:**
- La CREATE: field-ul este salvat
- La UPDATE: field-ul este **ignorat** de Hibernate
- Util pentru audit fields (`created_at`, `created_by`)

**Problema cu columnDefinition:**
```java
@Column(nullable = false, columnDefinition = "integer default 0")
private String content;
```

**Issues:**
1. `content` este String în Java, dar `integer` în columnDefinition
2. `default 0` nu are sens pentru TEXT column
3. Confuzie între `content` și `likes` (pare copy-paste error)

**Ar trebui:**
```java
// Pentru content (TEXT):
@Column(nullable = false, columnDefinition = "TEXT")
private String content;

// Pentru likes (INTEGER):
@Column(nullable = false, columnDefinition = "integer default 0")
private Integer likes;
```

#### 4. Lombok Integration

```java
@Getter
@Setter
public class Post { ... }
```

**Ce face Lombok:**
- Generează getters/setters la compile-time
- Reduce boilerplate code dramatic

**Fără Lombok:**
```java
public class Post {
    private Integer id;
    private String title;
    // ...

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    // ... +10 linii pentru fiecare field
}
```

**Cu Lombok:**
```java
@Getter
@Setter
public class Post {
    private Integer id;
    private String title;
    // Gata! Getters/setters generate automat
}
```

**Alte Lombok annotations utile:**
| Annotation | Generează |
|------------|-----------|
| `@Getter` | Getters pentru toate fields |
| `@Setter` | Setters pentru toate fields |
| `@ToString` | toString() method |
| `@EqualsAndHashCode` | equals() și hashCode() |
| `@NoArgsConstructor` | Constructor fără parametri |
| `@AllArgsConstructor` | Constructor cu toți parametrii |
| `@Data` | Combo: @Getter + @Setter + @ToString + @EqualsAndHashCode |

**Best Practice pentru Entities:**
```java
@Entity
@Getter
@Setter
@NoArgsConstructor  // Required by JPA
@AllArgsConstructor
@ToString(exclude = {"lazyLoadedField"})  // Exclude lazy fields
public class Post { ... }
```

#### 5. LocalDateTime vs java.sql.Timestamp

```java
private LocalDateTime create = LocalDateTime.now();
```

**De ce LocalDateTime:**
- ✅ Modern Java 8+ Date/Time API
- ✅ Immutable și thread-safe
- ✅ Mai clar decât `java.util.Date` sau `java.sql.Timestamp`
- ✅ JPA 2.2+ suportă automat mapping la SQL TIMESTAMP

**Alternative (deprecate/old):**
```java
// ❌ Old way (evită):
private java.util.Date created;
private java.sql.Timestamp created;

// ✅ Modern way:
private LocalDateTime created;
private Instant created;        // Cu timezone info
private OffsetDateTime created; // Cu timezone offset
```

**Problema cu inițializarea:**
```java
private LocalDateTime create = LocalDateTime.now();
```

**Issues:**
1. Se setează la **instanțiere**, nu la **persistare**
2. Dacă creezi obiectul dar îl salvezi mai târziu → timestamp incorect
3. Nu respectă timezone-ul serverului de DB

**Soluții corecte:**

**Opțiunea 1: @PrePersist** (best practice)
```java
@Column(nullable = false, updatable = false)
private LocalDateTime created;

@PrePersist
protected void onCreate() {
    created = LocalDateTime.now();
}
```

**Opțiunea 2: Database default**
```java
@Column(nullable = false, updatable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
private LocalDateTime created;
// Lasă DB-ul să seteze valoarea
```

**Opțiunea 3: Auditing** (cel mai profesional)
```java
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Post {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime updated;
}
```

#### 6. Package Structure - `model.enteties` (typo)

```
model/
└── enteties/    ← ⚠️ Typo: ar trebui "entities"
    └── Post.java
```

**Observație:**
- Package-ul este scris greșit: `enteties` în loc de `entities`
- Probabil typo de la începutul proiectului
- Ar trebui corectat, dar poate cauza breaking changes

**Structură corectă:**
```
model/
├── entities/     ← Corect
│   └── Post.java
├── dto/
│   └── PostDTO.java
└── request/
    └── PostRequest.java
```

### JPA Annotations Overview

| Annotation | Nivel | Scop |
|------------|-------|------|
| `@Entity` | Class | Marchează ca JPA entity |
| `@Table` | Class | Specifică numele tabelului |
| `@Id` | Field | Primary key |
| `@GeneratedValue` | Field | Strategy pentru generare ID |
| `@Column` | Field | Customizare mapare coloană |
| `@Getter` | Class | Lombok - generează getters |
| `@Setter` | Class | Lombok - generează setters |

## 🗄️ Database Changes

**Nu există modificări** - branch-ul creează doar entitatea Java care **mapează** la tabelul existent `posts` din branch 4-17-SQL.

**Mapping validation:**
```
Java Entity Post          →    PostgreSQL Table posts
─────────────────────────      ────────────────────────
id (Integer)              →    id (BIGSERIAL) ⚠️ Type mismatch
title (String)            →    title (VARCHAR(255)) ✅
create (LocalDateTime)    →    created (TIMESTAMP) ⚠️ Name mismatch
content (String)          →    content (TEXT) ⚠️ columnDefinition wrong
[missing likes field]     →    likes (INTEGER)
```

## 🔗 Relații cu Alte Branch-uri

### Predecesor
**4-17-SQL** - a creat tabelul `posts` în PostgreSQL

### Diferențe față de 4-17:
| Aspect | 4-17-SQL | 4-18-Entity |
|--------|----------|-------------|
| **Database schema** | ✅ Tabelul `posts` | ✅ Same (nu modifică) |
| **Java entity** | ❌ Nu există | ✅ Clasă `Post` |
| **ORM mapping** | ❌ Nu | ✅ JPA annotations |
| **Lombok** | ❌ Nu | ✅ @Getter, @Setter |
| **Repository** | ❌ Nu | ❌ Nu încă (urmează în 5-21) |

### Succesor Direct
**5-21-JPARepository-GetMapping** - va crea `PostRepository` pentru operații CRUD

### Impact pe Branch-uri Viitoare
- ✅ **Fundație pentru Repository pattern** - permite JpaRepository
- ✅ **Object-oriented DB access** - lucru cu obiecte `Post` în loc de SQL
- ⚠️ **Issues vor fi corectate** în branch-uri viitoare (nume fields, tipuri)

## 📝 Commit History

```
579ea85 - add Post entity with JPA annotations (1 Oct 2025)
└── Post.java (new file, 26 lines)
    ├── @Entity, @Table annotations
    ├── JPA field mappings (@Id, @Column)
    └── Lombok (@Getter, @Setter)

2ddd978 - Merge pull request #6 from alexandru997/4-18-Entity
```

## 💡 Învățăminte și Best Practices

### ✅ Ce a fost bine implementat:

1. **Prima entitate JPA** ⭐
   - Demonstrează mapare class ↔ table
   - Folosește Jakarta Persistence (JPA 3.0)

2. **Lombok integration** ⭐
   - Reduce boilerplate dramatic
   - @Getter și @Setter pentru toate fields

3. **@GeneratedValue(IDENTITY)** ⭐
   - Corect pentru BIGSERIAL din PostgreSQL
   - Auto-increment managed de DB

4. **LocalDateTime usage** ⭐
   - Modern Java 8+ Date/Time API
   - Mai bun decât java.util.Date

5. **Package structure** ⭐
   - Separare model în package dedicat
   - (deși cu typo în nume)

### ❌ Issues și Anti-Patterns:

1. **Type mismatch pentru `id`** ⚠️⚠️⚠️
   ```java
   private Integer id;  // ❌ Java Integer (max 2.1B)
   // DB: BIGSERIAL = Long (max 9 quintillion)

   // Ar trebui:
   private Long id;
   ```

2. **Typo în field name** ⚠️⚠️
   ```java
   private LocalDateTime create;  // ❌ Inconsistent cu DB "created"
   // Ar trebui:
   private LocalDateTime created;
   ```

3. **columnDefinition incorect** ⚠️⚠️⚠️
   ```java
   @Column(nullable = false, columnDefinition = "integer default 0")
   private String content;  // ❌ content este String, nu integer!

   // Ar trebui:
   @Column(columnDefinition = "TEXT")
   private String content;
   ```

4. **Lipsă field `likes`** ⚠️
   - DB are coloana `likes INTEGER DEFAULT 0`
   - Entity nu are acest field
   - Va cauza probleme la citire din DB

5. **Default value la instanțiere** ⚠️
   ```java
   private LocalDateTime create = LocalDateTime.now();
   // ❌ Se setează când se creează obiectul, nu când se salvează

   // Ar trebui @PrePersist:
   @PrePersist
   protected void onCreate() {
       created = LocalDateTime.now();
   }
   ```

6. **Lipsă constructor** ⚠️
   - Nu există `@NoArgsConstructor` (required by JPA)
   - Funcționează doar pentru că nu există alte constructors
   - Best practice: explicit `@NoArgsConstructor`

7. **Lipsă toString, equals, hashCode** ⚠️
   - Important pentru debugging
   - Important pentru collections (Set, Map)

### 🔧 Versiune Corectată (Best Practice):

```java
package com.post_hub.iam_Service.model.entities;  // ✅ "entities" nu "enteties"

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "title")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ✅ Long pentru BIGSERIAL

    @Column(nullable = false, unique = true)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp  // ✅ Hibernate sets timestamp on persist
    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likes = 0;
}
```

### 📚 Concepte Demonstrate:

#### JPA/Hibernate:
- ✅ **Entity mapping** - class ↔ table
- ✅ **@Entity, @Table** - basic annotations
- ✅ **@Id, @GeneratedValue** - primary key
- ✅ **@Column** - column customization
- ⚠️ **Type mappings** - cu issues demonstrate

#### Lombok:
- ✅ **@Getter, @Setter** - boilerplate reduction
- ❌ **Alte annotations** - lipsesc (@NoArgsConstructor, etc.)

#### Design:
- ✅ **Domain model** - separare în package `model`
- ✅ **Entity best practices** - partial demonstrate
- ❌ **Complete implementation** - issues intenționate

## 🎓 Scop Educațional

Acest branch servește ca **introducere în JPA entities** cu:

### 1. First JPA Entity
Demonstrează:
- Cum se creează o entitate JPA
- Annotations de bază (@Entity, @Table, @Id)
- Mapping la tabel PostgreSQL existent

### 2. Lombok Benefits
Arată:
- Reducerea boilerplate cu @Getter/@Setter
- Code mai clean și maintainable

### 3. Common Mistakes (probabil intenționate)
Demonstrează **ce NU trebuie făcut:**
- Type mismatches (Integer vs BIGSERIAL)
- Field name inconsistencies
- Wrong columnDefinitions
- Missing fields

### 4. Foundation pentru ORM
Stabilește baza pentru:
- Repository pattern (branch 5-21)
- Service layer cu entities (branch 5-22)
- CRUD operations (branch-uri viitoare)

**Target audience**:
- Beginneri care învață JPA/Hibernate
- Developeri care trec de la SQL pur la ORM
- Oricine vrea să înțeleagă entity mapping

## 🔄 Comparație: SQL vs ORM

| Aspect | Branch 4-17 (SQL) | Branch 4-18 (Entity) |
|--------|-------------------|---------------------|
| **Lucru cu date** | SQL queries raw | Java objects |
| **Type safety** | ❌ String queries | ✅ Compile-time checks |
| **Boilerplate** | ⚠️ Mult SQL | ✅ Minimal (Lombok) |
| **Portability** | ❌ PostgreSQL specific | ✅ JPA standard |
| **Learning curve** | ✅ SQL familiar | ⚠️ JPA concepts noi |

## 💼 Evoluție în Branch-uri Viitoare

**Issues din acest branch vor fi corectate în:**

1. **Branch 5-22-DTO-Servoce-Mapping** - probabil adaugă `likes` field
2. **Branch-uri ulterioare** - corectează type pentru `id`
3. **Refactoring** - posibil rename `create` → `created`

**Pattern-ul demonstrate:**
- Branch 4-18: Implementare simplă cu issues
- Branch-uri viitoare: Îmbunătățiri incrementale
- **Learning by iteration** - proces natural de development

**Concluzie**: Branch 4-18-Entity este **introducere practică în JPA**, cu issues intenționate care vor fi învățăminte pentru corectare în branch-uri viitoare.

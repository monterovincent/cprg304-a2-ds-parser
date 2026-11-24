# CPRG 304 – Assignment 2: Implementing ADTs and a Bracket Parser

SAIT · CPRG 304 (Object-Oriented Programming III) · Group assignment

This project builds four linear data structures from scratch in Java (an array list, a doubly linked list, a stack and a queue) without using Java's built-in collections. It then uses the stack and the queue to power a command-line **parser** that reads a text file and reports every line with mismatched brackets.

---

## Table of contents

1. [Quick start](#quick-start)
2. [Requirements](#requirements)
3. [Project structure](#project-structure)
4. [The data structures](#the-data-structures)
5. [The parser](#the-parser)
6. [Rules and constraints we followed](#rules-and-constraints-we-followed)
7. [Testing](#testing)
8. [Building Parser.jar](#building-parserjar)
9. [Team and roles](#team-and-roles)
10. [Git workflow](#git-workflow)
11. [Submission checklist](#submission-checklist)
12. [Known notes and limitations](#known-notes-and-limitations)

---

## Quick start

From a terminal in the folder that contains `Parser.jar`:

```
java -jar Parser.jar testC1.txt
```

A file with no bracket errors prints:

```
File: testC1.txt has no errors.
```

A file with errors lists each problem with its line number:

```
Errors in file testC3.txt:
Line 4: Syntax error on "]".
Line 9: Syntax error on ")".
...
```

> The exact wording of the output comes from the assignment's sample screenshot. The spec allows our format to differ as long as the same information is shown.

---

## Requirements

| Tool | Version |
| --- | --- |
| Java | 21 |
| IDE | Eclipse |
| Test framework | JUnit 4 |

The assignment requires exactly this setup. A submission that doesn't meet it is graded zero.

---

## Project structure

```
assignment2StartingCode/
├── src/
│   ├── appDomain/           Parser application (entry point for Parser.jar)
│   ├── exceptions/
│   │   └── EmptyQueueException.java      provided, unchanged
│   ├── implementations/
│   │   ├── MyArrayList.java              array-based list
│   │   ├── MyDLL.java                    doubly linked list
│   │   ├── MyDLLNode.java                one node of the linked list
│   │   ├── MyStack.java                  stack, built on MyArrayList
│   │   └── MyQueue.java                  queue, built on MyDLL
│   └── utilities/                        provided interfaces, unchanged
│       ├── Iterator.java
│       ├── ListADT.java
│       ├── QueueADT.java
│       └── StackADT.java
├── test/
│   └── unitTests/                        provided JUnit 4 tests, unchanged
│       ├── MyArrayListTest.java
│       ├── MyDLLTest.java
│       ├── MyQueueTest.java
│       └── MyStackTest.java
├── res/                                  sample input files for the parser
│   ├── testC1.txt
│   ├── testC2.txt
│   └── testC3.txt
└── Parser.jar                            runnable build of the parser
```

**Files we must never edit:** everything in `utilities/`, `exceptions/` and `test/`. The instructor grades by dropping in the original copies of these files, so any change would break compilation or the tests.

---

## The data structures

All four classes store **Strings only**. The starter code deliberately leaves out generics (`MyList<E>`), so every element, parameter and return type is `String`.

### How they fit together

```
String[] array  →  MyArrayList  →  MyStack  ─┐
                                             ├→  Parser
MyDLLNode       →  MyDLL        →  MyQueue  ─┘
```

Each interface (ADT) defines **what** a structure does. Each class decides **how** it does it. The parser only depends on the stack and queue behaviour, not on how they're built inside.

### MyArrayList (implements `ListADT`)

A list stored in a plain `String[]` that grows when it's full.

- **Fields:** `String[] data`, `int size`, `double growthRate`.
- **Constructors:** `MyArrayList()` uses a starting capacity of 10 and a growth rate of 0.25. `MyArrayList(int capacity, double growthRate)` lets you set both and rejects invalid values with `IllegalArgumentException`.
- **Growth:** when the array is full, it grows by `growthRate` × its current length, and always by **at least 1 slot**, so a small or empty array still grows. Copying uses `Arrays.copyOf()`.
- **Insert and remove** shift the following items right or left. A removed slot is set to `null`.
- **Iterator:** a private inner class that takes a snapshot copy of the elements when it's created.

### MyDLLNode

One link in the doubly linked list.

- **Fields:** `String element`, `MyDLLNode pred` (the node before), `MyDLLNode succ` (the node after).
- **Constructors:** `MyDLLNode(String)` creates an unlinked node. `MyDLLNode(String, MyDLLNode, MyDLLNode)` creates a node already pointing at its neighbours.
- **Getters and setters** for all three fields. A `null` `pred` marks the head, and a `null` `succ` marks the tail.

### MyDLL (implements `ListADT`)

A chain of `MyDLLNode` objects, with references to both ends.

- **Fields:** `head`, `tail`, `size`.
- **Adding at either end** is instant, because `head` and `tail` are always known. Adding in the middle links a new node between two neighbours.
- **`getNode(index)`** walks from whichever end is closer, so it never walks more than half the list.
- **`unlink(node)`** is a private helper used by both `remove` methods. The same few lines handle removing the head, the tail, a middle node or the only node:
  - no node before → `head` moves forward; otherwise the previous node skips over it
  - no node after → `tail` moves back; otherwise the next node skips back over it
- **`toArray`** reuses the holder array if it's big enough, otherwise creates a correctly sized one with `Arrays.copyOf()`.
- **Iterator:** a private inner class that copies every element into its own array when it's created, then reads only that copy.

### MyStack (implements `StackADT`)

Last in, first out. Built on a `MyArrayList`.

- The **top of the stack is the end of the list**, so `push` and `pop` never shift items.
- `pop` and `peek` on an empty stack throw `java.util.EmptyStackException`. A private `topIndex()` helper checks for an empty stack first, so the list's `IndexOutOfBoundsException` never leaks out.
- `search` returns a **1-based position counted from the top** (the top item is 1). It loops backwards from the end, so the copy nearest the top wins. It returns -1 when the item isn't there.
- `iterator`, `toArray` and `equals` work **bottom to top**, which is the list's own order.

### MyQueue (implements `QueueADT`)

First in, first out. Built on a `MyDLL`.

- **Index 0 is the front, and the last index is the back.** `enqueue` adds at the tail and `dequeue` removes from the head. Both are instant with a doubly linked list.
- `dequeue` and `peek` on an empty queue throw `EmptyQueueException`. They check `isEmpty()` first, so MyDLL's `IndexOutOfBoundsException` never leaks out.
- `search` returns a **1-based position counted from the front**. It returns -1 when the item is missing (checked before adding 1, so a missing item never returns 0) and -1 for `null`.
- `equals(QueueADT)` is true only when both queues have the same size and equal elements in the same order.

### Performance summary

| Operation | MyArrayList | MyDLL | MyStack | MyQueue |
| --- | --- | --- | --- | --- |
| Add at end | O(1) on average (occasional resize) | O(1) | push: O(1) on average | enqueue: O(1) |
| Remove at end / front | end: O(1), front: O(n) | O(1) at either end | pop: O(1) | dequeue: O(1) |
| Get by index | O(1) | O(n) (walks from the closer end) | peek: O(1) | peek: O(1) |
| Insert / remove in the middle | O(n) (shifting) | O(n) (walking to the spot) | n/a | n/a |
| Search by value | O(n) | O(n) | O(n) | O(n) |

These numbers explain the pairings: a stack only touches one end, which suits an array's end; a queue touches both ends, which suits a doubly linked list.

### Shared behaviour (from the interfaces)

- **Null is never allowed as an element.** `add`, `set`, `push`, `enqueue`, `contains`, `indexOf`, `remove(String)`, `addAll` and `toArray` throw `NullPointerException` on `null`.
- **Index rules:** `add(index, …)` accepts 0 to `size`, while `get`, `set` and `remove(index)` accept 0 to `size - 1`. Anything else throws `IndexOutOfBoundsException`.
- **`remove(String)`** removes only the first match and returns `null` when nothing matches.
- **Strings are compared with `.equals()`**, never `==`.
- **Iterators work on a copy** made at creation (required by `Iterator.java`), so later changes to the structure don't affect an iterator already in use. Calling `next()` with nothing left throws `NoSuchElementException`.

---

## The parser

The parser lives in the `appDomain` package and is the entry point of `Parser.jar`.

### What it checks

Only these eight characters count. Everything else on a line is ignored.

| Opening | Closing |
| --- | --- |
| `(` | `)` |
| `{` | `}` |
| `[` | `]` |
| `<` | `>` |

A file is correct when every opening bracket has a matching closing bracket and the brackets nest in the right order:

- `( [ ] )` is correct
- `( [ ) ]` is incorrect
- `{ [ ( ) ] }` is correct
- `{ [ ( ] ) }` is incorrect

Comments aren't skipped. A bracket inside a `//` comment counts like any other.

### How it works

1. Read the file named on the command line, **one line at a time**, keeping a line counter.
2. Pick out the bracket characters on each line.
3. Process each bracket with **Kitty's Parser Algorithm** (provided by the instructor), using our `MyStack` and two `MyQueue`s:
   - **Opening bracket:** push it onto the stack.
   - **Closing bracket:**
     - matches the top of the stack → pop it; all is well
     - otherwise, matches the head of `errorQ` → dequeue it and ignore it
     - otherwise, the stack is empty → add it to `errorQ`
     - otherwise, search the stack for a matching opening bracket:
       - found → pop each item into `errorQ` until the match, and report an error
       - not found → add it to `extrasQ`
4. At the end of the file:
   - pop anything left on the stack into `errorQ`
   - if exactly one of the two queues is empty, report every item in both as an error
   - if both have items, compare their heads: no match → dequeue from `errorQ` and report it; match → dequeue from both. Repeat until both are empty.
5. Print "no errors", or every error with its line number, in the order the errors occur.

The spec notes that a "perfect" parser is very hard, so line numbers may differ slightly from the sample output. What must hold is that every missing or extra bracket is detected.

<!-- TODO (Person C): add the parser's class names and any design notes here, e.g. how each bracket and its line number are stored, since MyStack and MyQueue hold Strings only. -->

### Sample input files

| File | What it contains |
| --- | --- |
| `res/testC1.txt` – `res/testC3.txt` | C binary-search programs. One is the clean, correct version. The others have planted bracket errors: extra `]`, extra `)`, extra `[`, extra `(`, doubled `{`, and a missing `{` after a `while`. |

<!-- TODO: note which file is which once confirmed. -->

---

## Rules and constraints we followed

| Instructor rule | How we met it |
| --- | --- |
| **No built-in collections** (`java.util.ArrayList`, `LinkedList`, `Stack`, `ArrayDeque` and similar). Using them costs 50% of the mark | None are used. The only `java.util` imports are `Arrays` and exception classes |
| **Limited array utilities:** only `Arrays.copyOf()` and `System.arraycopy()`, only for resizing, `toArray` and iterators | MyArrayList: resize, `toArray`, iterator snapshot. MyDLL: `toArray` only |
| **Standard exceptions are allowed** | `NullPointerException`, `IndexOutOfBoundsException`, `NoSuchElementException`, `EmptyStackException`, `IllegalArgumentException`, plus the provided `EmptyQueueException` |
| **ADT interfaces must not be modified** | `utilities/` and `exceptions/` are untouched. All classes compile against the originals |
| **Provided tests must not be modified** | `test/unitTests/` is untouched |
| **MyStack must use MyArrayList; MyQueue must use MyDLL** | MyStack's only field is a `MyArrayList`; MyQueue's only field is a `MyDLL` |
| **MyDLL must store each element in a `MyDLLNode`** | Every element lives in a node linked with `pred` and `succ`; there's no array storage |
| **Comprehensive JUnit testing** | All four provided suites pass (see below) |

---

## Testing

### Provided JUnit 4 suites

Run each file in `test/unitTests/` with **right-click → Run As → JUnit Test**, or right-click the whole `unitTests` package to run all four at once.

| Test file | Tests | Status |
| --- | --- | --- |
| `MyArrayListTest` | 61 | ✅ passing |
| `MyDLLTest` | 61 | ✅ passing |
| `MyStackTest` | 63 | ✅ passing |
| `MyQueueTest` | 66 | ✅ passing |
| **Total** | **251** | ✅ |

### Extra checks beyond the provided tests

The provided tests never add more than 3 items and don't check a few contract details, so we also checked:

- **Resizing:** 100 items added to MyArrayList and read back correctly; a list starting at capacity 0 still grows; 200 pushes and 200 pops on MyStack
- **Iterator snapshot:** an iterator still returns the original first item after the list was changed
- **Self-append:** `list.addAll(list)` doubles the list instead of looping forever
- **Exhausted iterator:** `next()` with nothing left throws `NoSuchElementException`
- **`search(null)`:** returns -1 on both MyStack and MyQueue

---

## Building Parser.jar

1. In Eclipse: **File → Export → Java → Runnable JAR file**.
2. **Launch configuration:** the parser's main class in `appDomain`.
3. **Export destination:** `Parser.jar`.
4. **Library handling:** "Extract required libraries into generated JAR".
5. Test it from a terminal:
   ```
   java -jar Parser.jar res/testC3.txt
   ```

---

## Team and roles

| Role | Owns | Built on |
| --- | --- | --- |
| **Person A** – <!-- Adam --> | `MyArrayList`, `MyStack` | A plain `String[]` |
| **Person B** – Ebube | `MyDLLNode`, `MyDLL`, `MyQueue` | Linked nodes |
| **Person C** – <!-- David  --> | Parser application in `appDomain`, `Parser.jar` | `MyStack` and `MyQueue` |

A and B worked in parallel from day one. C started with file reading and the algorithm, then plugged in MyStack and MyQueue once their tests passed.

---

## Git workflow

There's one shared GitHub repository, and everyone pushes directly to `main`. Each person owns separate files, so conflicts are rare.

**Before starting work:**
```
git pull origin main
```
Then refresh Eclipse (select the project and press **F5**). Eclipse doesn't notice files changed on disk until you do.

**To push your work:**
```
git status                         # check what changed
git add <only your files>          # never bin/ or a teammate's file
git commit -m "Short description of the change"
git pull --rebase                  # replay your commit on top of anything new
git push
```

**Rules:**
- Make sure the project compiles before every push, because everyone pulls straight away.
- Compiled files (`bin/`, `*.class`) aren't tracked; `.gitignore` excludes them.
- After pushing, a teammate pulls and runs the tests as a review.

---

## Submission checklist

One submission per group, as a zip named `A2<GroupName>.zip`, containing:

- [ ] `Parser.jar`, which runs with `java -jar Parser.jar <file>`
- [ ] The zipped Eclipse project folder (File Explorer → right-click the project folder → Compress to ZIP file)
- [ ] `MarkingCriteria_Assignment2.docx`, completed by the whole group together as our sign-off
- [ ] Each member's peer assessment, submitted as the instructor directs

Before zipping:
- [ ] All four test suites green in Eclipse
- [ ] Search the project for `import java.util`: only `Arrays` and exception classes remain
- [ ] `Parser.jar` tested from a terminal, not just from Eclipse



## Known notes and limitations

- **`<` in C code.** The sample input files are C programs, where `<` and `<=` are comparison operators. Under the assignment's rules, `<` counts as a bracket, so an unmatched `<` is reported as an error. This means even the clean test file has two "errors", on the lines with `left <= right` and `arr[mid] < key`. <!-- TODO: record the group's decision here. -->
- **Strings only.** Following the starter's design note, none of the structures use generics.
- **Parser line numbers** for some errors may differ from the instructor's sample. The spec allows this as long as every missing or extra bracket is caught.

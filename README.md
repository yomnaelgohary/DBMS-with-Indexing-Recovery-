
# DBMS with Indexing and Recovery

## Overview

This project implements a simplified Database Management System (DBMS) focusing on core functionalities such as table creation, record insertion, selection with conditions, indexing via Bitmap indexes, and data recovery after page loss. It is based on the course project specifications provided by the German University in Cairo, Faculty of Media Engineering and Technology (CSEN604 – Databases II).

---

## Features

### Milestone 1 - Basic DBMS Operations

- **Create Table**: Initialize a new table with specified column names.
- **Insert Record**: Insert records into the table pages, appending at the end.
- **Select Records**:
  - Select all records from a table.
  - Select records based on a condition (e.g., column values).
  - Direct access to a record by page and record position using pointers.
- **Operation Trace**: Retrieve full or partial trace logs of operations performed on tables with timestamps.

### Milestone 2 - Indexing & Data Recovery

- **Bitmap Index Creation**: Create Bitmap indexes for specific columns to improve query performance.
- **Insert into Bitmap Index**: Automatically update indexes on every record insertion.
- **Select Using Index**: Efficient selection queries leveraging single or multiple Bitmap indexes with combined conditions.
- **Data Recovery**: Detect missing data pages and recover deleted records back to their original pages.

---

## Project Structure

- `DBApp.java`: Core implementation of DBMS functionalities, including table operations, record management, indexing, selection, and recovery methods.
- `FileManager.java`: Handles serialization and deserialization of tables, pages, and indexes on disk. Manages the physical storage structure inside a `Tables` directory.
- Optional classes (like `Table.java`, `Page.java`, `BitmapIndex.java`): Provide guidance for modular design and encapsulate lower-level abstractions.
- Test files (`DBAppTests.java`, `DBAppTestsMS2.java`): Contain unit tests used to validate milestone 1 and milestone 2 implementations.

---

## Usage Examples

Here's a quick example illustrating the usage of core functionalities:

```java
// Create a table with columns
String[] columns = {"id", "name", "major", "semester", "gpa"};
DBApp.createTable("student", columns);

// Insert records into the table
String[] record1 = {"1", "stud1", "CS", "5", "0.9"};
DBApp.insert("student", record1);
String[] record2 = {"2", "stud2", "BI", "7", "1.2"};
DBApp.insert("student", record2);

// Select all records
ArrayList<String[]> allData = DBApp.select("student");

// Select records by condition
String[] conditionCols = {"gpa"};
String[] conditionVals = {"1.2"};
ArrayList<String[]> filtered = DBApp.select("student", conditionCols, conditionVals);

// Create bitmap index on a column
DBApp.createBitMapIndex("student", "major");

// Select using index
ArrayList<String[]> indexedSelection = DBApp.selectIndex("student", new String[]{"major"}, new String[]{"CS"});

// Data recovery after page deletion
ArrayList<String[]> missing = DBApp.validateRecords("student");
DBApp.recoverRecords("student", missing);
```

## Implementation Highlights

- **Data Model**: All data is treated as strings, simplifying data types and storage.
- **Page Management**: Tables store records in pages with a fixed maximum number of tuples.
- **Trace Logs**: Every operation is timestamped and logged for auditing and debugging.
- **Bitmap Indexes**: Implemented as bitvectors representing presence of records per value efficiently.
- **Recovery**: Detects missing data pages and restores missing records to their original positions without appending.

---

## How to Run

1. Clone the repository.
2. Import the project into your preferred Java IDE.
3. Run the main method in `DBApp.java` for demonstration.
4. Use the provided test classes for comprehensive validation of functionalities.

---

## Notes

- The project strictly follows the method signatures and constraints provided in the course starter code.
- No primary keys are enforced; records are appended in insertion order.
- All SELECT queries return complete rows (SELECT * semantics).
- The physical storage is managed under a `Tables` folder created in the project directory.
- Tests validate correctness, performance aspects, and trace accuracy.

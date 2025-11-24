## Main Entities
- **Book**: main entity with the following attributes:
  - `title` — book title
  - `author` — book author
  - `year_published` — publication year
  - `genre` — book genre (may contain multiple values separated by commas)
- **Author**: secondary entity related to books (one author can have multiple books)

---

## Example Input (JSON)

```json
[
  {
    "title": "1984",
    "author": "George Orwell",
    "year_published": 1949,
    "genre": "Dystopian, Political Fiction"
  },
  {
    "title": "Pride and Prejudice",
    "author": "Jane Austen",
    "year_published": 1813,
    "genre": "Romance, Satire"
  },
  {
    "title": "Romeo and Juliet",
    "author": "William Shakespeare",
    "year_published": 1597,
    "genre": "Romance, Tragedy"
  },
  {
    "title": "Moby Dick",
    "author": "Herman Melville",
    "year_published": 1851,
    "genre": "Adventure, Epic"
  }
]
```
---

## Example Output (XML)

### `statistics_by_author.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<statistics>
  <item>
    <value>Jane Austen</value>
    <count>1</count>
  </item>
  <item>
    <value>Herman Melville</value>
    <count>1</count>
  </item>
  <item>
    <value>George Orwell</value>
    <count>1</count>
  </item>
  <item>
    <value>William Shakespeare</value>
    <count>1</count>
  </item>
</statistics>
```
### `statistics_by_genre.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<statistics>
  <item>
    <value>Romance</value>
    <count>2</count>
  </item>
  <item>
    <value>Adventure</value>
    <count>1</count>
  </item>
  <item>
    <value>Dystopian</value>
    <count>1</count>
  </item>
  <item>
    <value>Epic</value>
    <count>1</count>
  </item>
  <item>
    <value>Satire</value>
    <count>1</count>
  </item>
  <item>
    <value>Political Fiction</value>
    <count>1</count>
  </item>
  <item>
    <value>Tragedy</value>
    <count>1</count>
  </item>
</statistics>
```
---

## Multithreading Experiments

Tested different thread counts for parallel JSON file processing to evaluate performance:

| Number of Threads | Execution Time (ms) |
|------------------|-------------------|
| 2                | 89                |
| 4                | 82 ✅ Best result |
| 8                | 87                |

**Observation:** Using **4 threads** gave the fastest processing time for this dataset.  
This shows that moderate parallelism improves performance, while too few or too many threads can lead to slower processing.




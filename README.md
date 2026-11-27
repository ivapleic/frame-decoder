# frame-decoder

A small client/server app that sends **length-prefixed messages** over TCP and converts their
text from **ISO-8859-2 to UTF-8**.

```
 input file            TCP             server
┌───────────┐     ┌─────────┐     ┌──────────────────────────┐
│ .bin      │ ──► │ client  │ ──► │ decode ISO-8859-2        │
│ (frames)  │     └─────────┘     │   ├─► output.txt (UTF-8) │
└───────────┘                     │   └─► transactions.db    │
                                  └──────────────────────────┘
```

Each message is a frame: **2-byte length (big-endian) + payload**.

## Requirements

Java 21. Nothing else to install (no database server, no Maven).

## How to run

Start the **server first**, then the **client**. No parameters are needed.

| | Server | Client |
|---|---|---|
| **IntelliJ IDEA** | run `ServerMain` | run `ClientMain` |
| **Windows** (double-click) | `run-server.bat` | `run-client.bat` |

Wait for `Listening on port 5000` on the server before starting the client.
The client prints `Done, sent 3 frames.`

The first start through the `.bat` files downloads Maven once, so it needs internet.
On Mac/Linux: `sh mvnw compile exec:java "-Dexec.mainClass=hr.task.server.ServerMain"`
(and `hr.task.client.ClientMain` for the client).

## Result

Two files appear in the project folder:

| file | content |
|---|---|
| `output.txt` | decoded messages, one per line |
| `transactions.db` | SQLite database, table `transactions` (type, ID, amount, currency) |

Output for the default sample (`sample-data/sample-input-hr.bin`):

```
UPLATA|ČĆŽŠĐ|100.50|HRK
STORNO|žćčšđ|42.00|EUR
KRAJ
```

`KRAJ` (and `END` in the other sample) is not a transaction, so it is only written to
`output.txt` and not stored in the database.

## Parameters (optional)

| program | parameters | defaults |
|---|---|---|
| `ServerMain` | `[port] [outputFile] [databaseFile]` | `5000`, `output.txt`, `transactions.db` |
| `ClientMain` | `[inputFile] [host] [port]` | `sample-data/sample-input-hr.bin`, `localhost`, `5000` |

The second sample file is `sample-data/sample-input-ascii.bin`.

## Tests

Right click `src/test/java` in IntelliJ and choose **Run**, or run `mvnw.cmd test`.

See [DESIGN.md](DESIGN.md) for assumptions and trade-offs.

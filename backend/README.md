# smart-words / backend

[ FRONTEND DOCS ](../frontend/README.md) |
[ MAIN DOCS ](../README.md)

This folder contains all backend services used in smart-words application:

* **service-quiz**: backend service for handling word quiz type of game ([DOCS](./service-quiz/README.md)).<br>
It's responsible for creating and starting a quiz based on the user input parameters like: number of questions and language. 
Upon creation it provides a specified question number with four possible answers, receives and processes responsens and sends a response with correct or incorrect answer status. 
Finally after finishing a quiz it calculates the end result.

* **service-word**: backend service for handling dictionary files of different games and/or modes ([DOCS](./service-word/README.md)).<br>
It's responsible for handling CRUD operations on dictionaries: adding new word to a specified dictionary JSON file, reading dictionary JSON files, editing and removing an existing word in a dictionary.
This service has built-in dictionary files which user can edit, read or delete, but any new file is saved in a separate JSON file to separate the built-in words from user created ones.<br>
More detailed information about dictionary file naming convention is available [HERE](service-word/src/main/resources/README.md).

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>
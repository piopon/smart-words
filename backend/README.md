# smart-words / backend

This folder contains all backend services used in smart-words application:
* **service-quiz**: backend service for handling word quiz type of game. 
It's responsible for creating and starting a quiz based on the user input parameters like: number of questions and language. 
Upon creation it provides a specified question number with four possible answers, receives and processes responsens and sends a response with correct or incorrect answer status. 
Finally after finishing a quiz it calculates the end result.
* **service-word**: backend service for handling word and dictionary files for different games and/or modes.
It's responsible for handling CRUD operations on dictionaries: adding new word to a specified dictionary JSON file, reading dictionary JSON files, editing and removing an existing word in a dictionary.
This service has built-in dictionary files which user can edit, read or delete, but any new file is saved in a separate JSON file to separate the built-in words from user created ones.
More detailed information about dictionary file naming convention is available [HERE](service-word/src/main/resources/README.md).



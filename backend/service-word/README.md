# smart-words / backend / service-word

Service for handling word and dictionary files for different games and/or modes.

It's responsible for handling CRUD operations on dictionaries: adding new word to a specified dictionary JSON file, reading dictionary JSON files, editing and removing an existing word in a dictionary.<br>
This service has built-in dictionary files which user can edit, read or delete, but any new file is saved in a separate JSON file to separate the built-in words from user created ones.

### endpoints

* `GET /words/{mode}/{lang}` -> `OK 200+JSON` | `ERR 500`<br>Receive all language-specific words for selected mode
* `GET /words/{mode}/{lang}?size=no` -> `OK 200+JSON` | `ERR 500`<br>Receive specified number of words for selected mode and language
* `GET /words/{mode}/{lang}?cat=adj` -> `OK 200+JSON` | `ERR 500`<br>Receive category-specific words for selected mode and language
* `GET /words/{mode}/{lang}?random=bool` -> `OK 200+JSON` | `ERR 500`<br>Receive words in random order for selected mode and language
* `POST /words/{mode}/{lang} + JSON` -> `OK 200` | `ERR 500`<br>Add a new word to dictionary with specified mode and language
* `PUT /words/{mode}/{lang}/{name} + JSON` -> `OK 200+JSON` | `ERR 404`<br>Update word in dictionary with specified mode and language
* `DELETE /words/{mode}/{lang}/{name}` -> `OK 200` | `ERR 404`<br>Delete word from dictionary with specified mode and language
* `GET /dictionaries` -> `OK 200+JSON` | `ERR 500`<br>Receive all used dictionary information

### technology

<img src="../../resources/logo/scala.png" alt="scala logo" width="300"/>

This service is written entirely in **Scala** language.<br>
No database framework is used. All files are saved directly into JSON files containing all information about dictionaries.
For more info about dictionaries checkout [THIS](src/main/resources/README.md) markdown documentation file.

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>
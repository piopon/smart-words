# smart-words

![frontend status](https://github.com/piopon/smart-words/actions/workflows/frontend-schedule.yml/badge.svg)
![backend status](https://github.com/piopon/smart-words/actions/workflows/backend-schedule.yml/badge.svg)
![license-mit](https://img.shields.io/github/license/piopon/smart-words)
[![codecov](https://codecov.io/gh/piopon/smart-words/branch/main/graph/badge.svg?token=2R3LUSOGW6)](https://codecov.io/gh/piopon/smart-words)

**Smart Words** is a simple web application written in Scala (backend) and HTML/CSS/JS (frontend) which focuses on different games with words.

Currently only a quiz type of game is available which supports two types of modes:
* guessing the definition of the specified word,
* guessing the word based on a provided definition.

User can extend the current capabilities by creating a new word dictionaries for different languages, as well as edit the currently exisiting ones. The modes mentioned above can be edited via the UI by adjusting their input parameters, options, and labels.

## contents
 * [why?](#why)
 * [running application](#running-application)
 * [starting a quiz game](#starting-a-quiz-game)
 * [editing dictionaries](#editing-dictionaries)
 * [editing quiz modes](#editing-quiz-modes)
 * [want to contribute?](#want-to-contribute)

## why?

This application started as a hobby side-project for learning web technologies and consolidating new words in different languages in the form of various fun word games.
The first and most obvious choice was to create a quiz game where the user has four options to select the correct answer.
Currently this is the only type of game fully implemented but there is a plan to add more modes like: hangman or wordly-like game.

> **Warning**<br>
> I don't have an exact plan for when new game modes will be added to the app 😅.

Initially, this application was to be written entirely in HTML/CSS/JS and run entirely on the client side.
Over time, this concept has evolved and the logic has been applied to the microservices architecture.

Currently the application is structured as follows:
* the frontend logic is done in HTML, CSS, and JavaScript. No framework or bootstrap is currently used since I wanted to focus on learning the fundamentals before understanding different wrappers.
* the backend logic is divided into services written in Scala:
  * quiz service - responsible for controlling quiz game type which includes: starting and stopping quizzes, receiving questions and sending answers, and calculating the final result
  * word service - responsible for handling different words and dicrionarires for specific languages. This includes: reading, adding, deleting, and updating words in appropriate dictionary files, and handle those files as well.

No database engine is currently used. All data is saved directly in the JSON files. New words are saved in a entirely new files which all are read while starting word service. Quiz service has only in-memory database for now (quizzes are stored in a map).

## running application

> **Note**<br>
> I realize that the current way of launching the application is not very useful and cumbersome (at least 😉). I'm going to improve it sooner than later.
> This section of the documentation will be updated as it happens.<br>
> Stay tuned 📢.

Currently there is no release package or Docker image distribution.

One can manually invoke the [interactive `sbt` tool](https://www.scala-sbt.org/1.x/docs/index.html) (initially reffered to as `Simple Build Tool`, then redefined to `Scala Build Tool`, but really the name doesn’t stand for anything, it’s just `sbt` 😎) to create JAR packages for both backend services and after that run them on the same machine, and then open the `index.html` page from the `frontend` directory.

Alternatively, one can use the IntelliJ editor and run both services in it, and then run `index.html` from the `frontend` folder.

## starting a `quiz` game

After running the application a big button with `start quiz` label is visible at the center of the screen:
![smart-words: home screen](/resources/docs/000_home-screen.png)
One can also see a top menu buttons which are responsible for dictionary edition and quiz mode settings.

After pressing the `start quiz` button user will be provided with the mode selection where languages and other settings can be adjusted:
![quiz: select mode view](/resources/docs/001_quiz-mode-selection.png)
As stated in the first section of this documentation currently two modes are supported: guessing the definition of a specified word, and selecting the word by the provided definition.

Pressing `start` on a particular quiz mode will start a new game and a first question will appear:
![quiz: question view](/resources/docs/002_quiz-question.png)
This screen consists of four answer buttons (labeled `A`, `B`, `C`, and `D`) with a question above those buttons (currently depending on the selected mode it can be a word or a definition). On the bottom one can navigate between questions via the `PREVIOUS` and `NEXT` buttons, as well as instantly end the game using the `STOP` button.

After answering questions (or by explicitly quitting) the quiz will finish and a summary will be displayed:
![quiz: summary view](/resources/docs/003_quiz-summary.png)
Acknowledging this screen will return the user to the quiz mode selection screen.

## editing dictionaries

User can also edit existing dicrionaries (new ones can be currently added only manually to the word service resource - for more information checkout the appropriate [README.md](backend/service-word/src/main/resources/README.md)).

To do so one must select the dictionary icon (second right icon from the upper menu in the main `index.html` site). After that the frontend will receive and display all words for a specified dictionary:
![dictionary: show all words](/resources/docs/004_dictionary-show-words.png)

Selecting combo boxes on the top of the words table will change the dictionary to be displayed. For example in the screenshot above all words for the `quiz` mode `0` and `en` (English) language dictionary is displayed.

After that the user can edit or delete a word by selecting the blue or red button of each word:
![dictionary: edit word](/resources/docs/006_dictionary-edit-word.png)

Also a new word can be added by clicking the `+` button in the top-right corner of the window. This will result a similar dialog to appear:
![dictionary: add word](/resources/docs/005_dictionary-add-word.png)

## editing quiz modes

Besides editing words dictionaries, user can also edit quiz modes. To do so a right-most icon should be selected from the main `index.html` screen and a new view will appear:
![settings: edit quiz modes](/resources/docs/007_settings-mode-edit.png)
From this screen the user can add new mode, select the input settings and modify them with custom values. Currently only two settings are supported:
* `questions` - which allows to specify the minum, maximum, and initial value of quiz questions
* `languages` - which allows to select supported languages which correspond to the dictionary JSON files

Both of those settings can be dragged and dropped to the selected mode, but only one instance can be present at once in a single mode. Every change will make the edited mode as "dirty" which will show a disc icon with an exclamation mark on the left side of edited mode.

Also the user can remove manually added modes. The ones defined in the main `modes.json` file in the backend quiz service resources directory cannot be removed from the UI. Only manual JSON file modification can delete them permanently.

## want to contribute?

Follow the steps from the [`CONTRIBUTING.md`](CONTRIBUTING.md) file contents.

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>
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
 * [starting a game](#starting-a-game)
 * [editing dictionaries](#editing-dictionaries)
 * [editing quiz modes](#editing-quiz-modes)
 * [want to contribute?](#want-to-contribute)

## why?

This application started as a hobby side-project useful for learning and consolidating new words in different languages in the form of various fun games.
The first and most obvious choice was to create a quiz game where the user has four options to select the correct answer.
Currently this is the only type of game fully implemented but there is a plan to add more modes like: hangman or wordly-like game.

> **Warning**<br>
> I don't have an exact plan for when new game modes will be added to the app 😅.

Initially, this application was to be written entirely in HTML/CSS/JS and run entirely on the client side.
Over time, this concept has evolved and the logic has been applied to the microservices architecture.
Having that said now it all ended that:
* the frontend logic is done in HTML, CSS, and JavaScript,
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

## starting a game

After running the application a big button is visible at the center of the screen:
![smart-words home screen](/resources/docs/000_home-screen.png)

After pressing it user will be provided with the mode selection where languages and other settings can be adjusted:
![smart-words home screen](/resources/docs/001_quiz-mode-selection.png)

Pressing "start" on a particular quiz mode will start a new game and a first question will appear:
![smart-words home screen](/resources/docs/002_quiz-question.png)

After answering questions (or by explicitly quitting) the quiz will finish and a summary will be displayed:
![smart-words home screen](/resources/docs/003_quiz-summary.png)

Acknowledging this screen will return the user to the quiz mode selection screen.

## editing dictionaries

User can also edit existing dicrionaries (new ones can be currently added only manually to the word service resource - for more information checkout the appropriate [README.md](backend/service-word/src/main/resources/README.md)).

To do so one must select the dictionary icon (second right icon from the upper menu in the main `index.html` site). After that the frontend will receive and display all words for a specified dictionary:
![dictionary show all words](/resources/docs/004_dictionary-show-words.png)

Selecting combo boxes on the top of the words table will change the dictionary to be displayed. For example in the screenshot above all words for the `quiz` mode `0` and `en` (English) language dictionary is displayed.

After that the user can edit or delete a word by selecting the blue or red button of each word:
![dictionary edit word](/resources/docs/006_dictionary-edit-word.png)

Also a new word can be added by clicking the `+` button in the top-right corner of the window. This will result a similar dialog to appear:
![dictionary add word](/resources/docs/005_dictionary-add-word.png)

## editing quiz modes


## want to contribute?

Follow the steps from the [`CONTRIBUTING.md`](CONTRIBUTING.md) file contents.

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>
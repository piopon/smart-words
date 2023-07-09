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

This application started as a hobby side-project useful for learning and consolidating new words in different languages in the form of different fun games.
The first and most obvious choice was to create a quiz game where the user has four options to select the correct answer. Currently this is the only type of game fully implemented but there is a plan to add more modes like: hangman or wordly-like game.

Initially, this application was to be written entirely in HTML/CSS/JS and run entirely on the client side.
Over time, this concept has evolved and the logic has been applied to the microservices architecture.
Having that said now all the frontend logic is done in HTML, CSS, and JS while the backend logic is divided into services:
* quiz service - responsible for controlling quiz game type with starting, stopping quiz, receiving questions and sending answers, and finally for calculating the final result
* word service - responsible for adding, deleting, and updating words in appropriate dictionary files.

## running application


## starting a game


## editing dictionaries


## editing quiz modes


## want to contribute?

Follow the steps from the [`CONTRIBUTING.md`](CONTRIBUTING.md) file contents.

---
<p align="center">Created by PNK with 💚 @ 2022-2023</p>
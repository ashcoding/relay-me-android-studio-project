# 1. Introduction #

This repo contains all the code needed to for the Android app [Relay ME](https://play.google.com/store/apps/details?id=com.tinywebgears.relayme).
There is a wiki page, which introduces this app and answers some common questions. You can find it [here](https://github.com/codolutions/relay-me-android-studio-project/wiki).

# 2. Quickstart #

In order to get started, follow these steps:
* Clone this repo.
* Create a project in Android Studio, or build using Gradle: `./gradlew build`.

# 3. Development #

The rest of this document is about the development process.

## 3.1. Contribution ##

In order to contribute, fork and raise pull-requests against this repo. Once you have started contributing, you will be added as a collaborator.

## 3.2. Collaboration ##

There is a Slack group used for discussions and discussions around development of this app. Please send an email to [relayme+slack@codolutions.com](mailto:relayme+slack@codolutions.com) to be added to this group.

## 3.3. Tasks ##

There is a [Trello board](https://trello.com/b/gZQKuDBB/relay-me-for-android), where all the current tasks are kept in. This board is public, so that everyone can see what is currently being done for this project.

## 3.4. Releasing to Google Play ##

This is currently done manually, but this will be automated in the future.

# 4. Server-side component #

This app requires a server-side component, which is used for the OAuth flow with Google.
Its code lives [here](https://github.com/codolutions/relay-me-server-side-component), and you can access it live on [on Heroku](https://relay-me-test-server-side.herokuapp.com/) for testing.
In order to modify the Android app, you don't need to touch the server-side component.

## 4.1. Whey does Relay ME use browser OAuth, instead of a native one? ##
At the time this application was written there was limitations and concerns about native OAuth. For example, [this](http://thehackernews.com/2014/08/hacking-gmail-account-mobile-app.html) was a real concern.

## 4.2. Running your own server-side component ##

* Follow the instructions in [here](https://github.com/codolutions/relay-me-server-side-component).
* Clone [the server-side settings](https://github.com/codolutions/relay-me-android-server-side), update the settings accordingly, build, and replace the jar file `relayme-serverside.jar` with your own artifact.
* Now Relay ME will talk to your own server-side component.
* Re-install the app, so its server-side settings is reset.


# Introduction #

This repo contains all the code needed to for the Android app [Relay ME](https://play.google.com/store/apps/details?id=com.tinywebgears.relayme).
There is a wiki page, which introduces this app and answers some common questions. You can find it [here](https://github.com/codolutions/relay-me-android-studio-project/wiki).

# Quickstart #

Create a project in Android Studio, or build using Gradle: `./gradlew build`

# How to contribute #

Please fork and raise pull-requests against this repo. Once you have started contributing, you will be added as a collaborator.

# Agile board #

There is a [Trello board](https://trello.com/b/gZQKuDBB/relay-me-for-android), where all the current tasks are kept in. This board is public, so that everyone can see what is currently being done for this project.

# Server-side component #

This app requires a server-side component, which is used for the OAuth flow with Google.
Its code lives [here](https://github.com/codolutions/relay-me-server-side-component), and you can access it live on [on Heroku](https://relay-me-test-server-side.herokuapp.com/) for testing.
In order to modify the Android app, you don't need to touch the server-side component.

### Whey does Relay ME use browser OAuth, instead of a native one? ###
At the time this application was written there was limitations and concerns about native OAuth. For example, [this](http://thehackernews.com/2014/08/hacking-gmail-account-mobile-app.html) was a real concern.

### Running your own server-side component ###

* Follow the instructions in [here](https://github.com/codolutions/relay-me-server-side-component).
* Clone [the server-side settings](https://github.com/codolutions/relay-me-android-server-side), update the settings accordingly, build, and replace the jar file `relayme-serverside.jar` with your own artifact.
* Now Relay ME will talk to your own server-side component.
* Re-install the app, so its server-side settings is reset.


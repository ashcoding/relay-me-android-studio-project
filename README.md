# README #

This repo contains all the code needed to for the Android app [Relay ME](https://play.google.com/store/apps/details?id=com.tinywebgears.relayme).

# Quickstart #

Create a project in Android Studio, or build using Gradle.

# How to contribute #

Please raise pull-requests to this repo.

# Server-side component #

This app requires a server-side component, which is used for the OAuth flow with Google.
Its code lives [here](https://github.com/codolutions/relay-me-server-side-component), and you can access it live on [on Heroku](https://relay-me-test-server-side.herokuapp.com/) for testing.

### Whey does Relay ME use browser OAuth, instead of a native one? ###
http://thehackernews.com/2014/08/hacking-gmail-account-mobile-app.html

### Running your own server-side component ###

* Follow the instructions in [here](https://github.com/codolutions/relay-me-server-side-component).
* Clone [the server-side settings](https://github.com/codolutions/relay-me-android-server-side), update the settings accordingly, build, and replace the jar file `relayme-serverside.jar` with your own artifact.
* Now Relay ME will talk to your own server-side component.
* Re-install the app, so its server-side settings is reset.


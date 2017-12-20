![](banner.jpg)

CSCI3310 Course Project -- bookeeper: Day-to-day book keeping app

There are 3 ways to build / run this app:

- Hosted - run the app on top of the Expo client
  - Requires Expo client on mobile device
  - https://play.google.com/store/apps/details?id=host.exp.exponent
  - https://itunes.apple.com/us/app/expo-client/id982107779
- iOS Standalone - build an iOS IPA
  - Requires an Apple provisioning profile
- Android Standalone - build an android APK

## Prerequisites

- git
- Java 8
  - [leiningen](leiningen.org) (Clojure build tool)
- Node.JS
  - [yarn](yarnpkg.com) (JS package manager)
  - [expo](expo.io) CLI or (React Native Toolchain)

Plus requirements mentioned above for each type of build.

## Building

Common:

```
$ git clone ...
$ cd bookeeper
$ lein build-prod
$ yarn install
$ exp start
```

Hosted:

- Using the Expo client, scan the QR Code on the terminal.

Android:

- On another terminal, run `exp build:android`.
- Let Expo handle KeyPair generation.
- Download the APK file from the URL provided.

iOS:

- On another terminal, run `exp build:ios`.
- Enter your Apple ID, Password and Provisioning Profile.
- Download the IPA file from the URL provided.

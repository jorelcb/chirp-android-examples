# Chirp Android Examples

A selection of example Android apps using Chirp.

## Setup

For all of the example apps you will need to

- Sign up at [developers.chirp.io](https://developers.chirp.io)
- Copy/paste your Chirp credentials into the `CHIRP_APP_KEY`, `CHIRP_APP_SECRET` and `CHIRP_APP_CONFIG` variables in the app's `MainActivity` file.

----

## Java

### Demo

Demonstrates how to use the Chirp Android SDK in a sample app.

- The app will send and receive random payloads.
- The app will display payloads as hexadecimal strings

![DemoGIF](/Assets/ChirpDemo.gif)

----

## Kotlin

### Messenger

Chirp Messenger uses your device's speaker and microphone to send and receive messages via audio.

To be compatible with Chirp Messenger on other platforms, e.g., [messenger.chirp.io](https://messenger.chirp.io),
the `16khz-mono` protocol should be used.

![DemoGIF](/Assets/Messenger.gif)

### Demo

Demonstrates how to use the Chirp Android SDK in a sample app.

- The app will send and receive random payloads.
- The app will display payloads as hexadecimal strings

![DemoGIF](/Assets/ChirpDemo.gif)

### Secure Messenger

Chirp Secure Messenger is an extension of the Chirp Messenger app, adding in AES encryption
to illustrate the simplicity of encrypting data using Chirp.

Compatible with the iOS SecureMessenger at [chirp-ios-examples](https://github.com/chirp/chirp-ios-examples)

![DemoGIF](/Assets/Messenger.gif)

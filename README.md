# Dash It

DashIt is a Client side Android application designed to enable user to avail cab booking service, With 3 Different categories of rides Dash X, Dash XL, Dash LUX, Along with appropriate fare &
real time Distance calculation between Pickup & Destination location.


## Features

- **Payment Gateway Integration**: Seamlessly process transactions within the app using Stripe payment gateways, ensuring secure and reliable payment processing.
  
- **Backend Deployment**: Utilize **AWS Lambda** & **AWS API** Gateways for deploying the PHP backend, ensuring scalable and efficient serverless architecture.

- **CI/CD Pipeline**: Implement Github Actions for continuous integration and continuous deployment, automating the process of generating **release builds** & .apk files, ensuring fast and reliable delivery of updates.

- **Jetpack Compose Interoperability**: Enhance user interface development by leveraging **Jetpack Compose** interoperability APIs, allowing seamless integration with traditional Android Views and simplifying UI development.

- **Location Search**: Implement a robust location search feature utilizing Mapbox Search APIs, providing users with accurate and efficient location search functionality.

- **Distance Calculation**: Utilize Distance Matrix APIs (Mapbox) to calculate short distances between pickup & destination locations, providing users with accurate distance information for better decision-making.

- **Asynchronous Task Handling**: Employ **Kotlin Coroutines**, **LiveData** & **Kotlin Flows** to manage asynchronous tasks efficiently, ensuring smooth and responsive user experience while handling background operations.

- **Dependency Injection**: Streamline development and improve code maintainability by implementing Dependency Injection using **Dagger Hilt**, facilitating modular and testable code architecture.


## Screen Shots
|          |             |                |          |
| :---:    |    :----:   |          :---: |    :---: |
| Home Screen <br> <br>![1](https://github.com/YatinGarg07/dash-it/assets/73949161/ccd2d6b2-170a-41c7-8e96-2cbe189f7802)| Login Screen <br> <br> ![2](https://github.com/YatinGarg07/dash-it/assets/73949161/9926ee1b-5676-49af-a463-2fa4acdcda9e)| Select Destination <br> <br> ![3](https://github.com/YatinGarg07/dash-it/assets/73949161/f9ae89b2-8107-46fe-8a5a-552e4899bab2)| Select Ride Screen <br> <br>![4](https://github.com/YatinGarg07/dash-it/assets/73949161/371f7af7-2fcc-4cf3-875f-c82b29d83b9d) |
Confirm Ride & Checkout Screen <br> <br> ![5](https://github.com/YatinGarg07/dash-it/assets/73949161/06422466-cce4-48d1-9479-ca96b02d0739)

## Usage
<div align="center">
  <video src="https://github.com/YatinGarg07/dash-it/assets/73949161/e15167df-55f2-453f-8815-1fb0aba8f8c4"/>
</div>

## Tech Stack

### Core

- 100% [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material3 design](https://m3.material.io/) (UI components)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (structured concurrency)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Hilt](https://dagger.dev/hilt/) (DI)

### Build & CI
- [Github Actions](https://github.com/Ivy-Apps/ivy-wallet/actions) (CI/CD)

## Installation

1. Clone the repository:

    ```
    git clone https://github.com/yourusername/myapp.git
    ```

2. Open the project in Android Studio.

3. Build and run the project on an emulator or physical device.




## Contributing

Contributions are welcome! If you'd like to contribute to this project, feel free to fork this repository and submit a pull request. Here are a few areas where we would appreciate help:

- [Feature requests](https://github.com/yourusername/myapp/issues): If you have any ideas for new features or improvements, please open an issue to discuss.
- Bug fixes: If you find any bugs, please report them by opening an issue or submitting a pull request with the fix.
- Documentation: Help improve the README or inline documentation to make it easier for others to understand and contribute to the project.

## License

This project is licensed under the [MIT License](LICENSE). You are free to use, modify, and distribute this software as you see fit. See the [LICENSE](LICENSE) file for more details.

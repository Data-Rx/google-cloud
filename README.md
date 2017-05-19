# Google Cloud Functions

A library of functions for working with Google Cloud.

# Installation

To install the library:

```bash
$ git clone https://github.com/Data-Rx/google-cloud
$ cd google-cloud
$ lein install
```

# Usage

In your project's `project.clj` file add a dependency for:

    [net.data-rx/google-cloud "0.1.0"]

In your code's `:require` use something like:

    [google-cloud.oauth :as oauth]

## Environment Variables

The library requires that [Google Application Credentials](https://github.com/GoogleCloudPlatform/google-cloud-java#authentication) be provided. During development of your application the easiest way is to create a file called `profiles.clj` in the same directory as your application's `project.clj`. Place the following into `profiles.clj`:

```clojure
{:profile-env
  {:env
    {:google-application-credentials "/path/to/google-credentials.json"}}}
```

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

    [net.data-rx/google-cloud "0.2.0"]

In your code's `:require` use something like:

    [google-cloud.oauth :as oauth]

## Environment Variables

The library requires that [Google Application Credentials](https://github.com/GoogleCloudPlatform/google-cloud-java#authentication) be provided. During development of your application the easiest way is to create a file called `profiles.clj` in the same directory as your application's `project.clj`. Place the following into `profiles.clj`:

```clojure
{:profile-env
  {:env
    {:google-application-credentials "/path/to/google-credentials.json"}}}
```

# PubSub

The `google-cloud.pubsub` namespace contains functions for interacting with [Google's Pub/Sub](https://cloud.google.com/pubsub/) service.

## Allowed Google Naming Conventions

The following applies to both topics and subscriptions.

Must start with a letter, and contain only letters ([A-Za-z]), numbers ([0-9]), dashes (-), underscores (_), periods (.), tildes (~), plus (+) or percent signs (%). It must be between 3 and 255 characters in length, and it must not start with goog.

### Creating/Deleting Topics and Subscriptions

``` clojure
(ns your-ns.core
  (:require [google-cloud.pubsub :as gps]))

; First create a topic
(gps/create-topic "some-new-topic")
; Then we are able to create a subscription
(gps/create-subscription "some-new-topic" "some-new-subscription")

; Deleting topics and subscriptions
(gps/delete-topic "an-existing-topic")
(gps/delete-subscription "an-existing-subscription")
```

The management of topics and subscriptions can also be accomplished via the web [web UI](https://console.cloud.google.com/cloudpubsub/topicList?project=avid-willow-120318&authuser=0)

### Publishing Messages

``` clojure
(ns your-ns.core
  (:require [google-cloud.pubsub :as gps]))

(gps/publish "some-topic" ["Text Of Message 1" "Text Of Message 2"] "crypto-key")
```

### Processing Messages

The function `process-subscription-messages` processes messages from the subscription using the given callback function. This function is an infinite loop and will never return so you may want to run it in its own thread.

The callback function should accept a single argument which is a map representing a message from the queue the map will be in the form of:

``` clojure
{:ackId            ack-id
 :messageId        message-id
 :message-contents decrypted-message-content}
```

####  Example Usage

``` clojure
(ns your-ns.core
  (:require [google-cloud.pubsub :as gps]))

(def crypto-key "go-go-gadget-arm")

(defn process-message-callback [message]
  (println (str (:message-contents message) " is being processed and removed from queue")))

(gps/process-subscription-messages "switch-topic-demo-sub-5" process-message-callback crypto-key)
```

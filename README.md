# media-sample-scala

This is a sample project for developers interested in Scala and Play Framework(2.6.x).

This project is featuring some basic and general topics like below:

* Authentication
* Pagination
* DB relationships
* DB transactions
* Dockerizing
* Unit testing
* Integration testing
* Database testing
* Service layer
* Secure session management with Akka
* Upload image files to S3

## Preface

This project is intended to be used on Mac OS X or Linux.

And following instructions are only for Mac OS X.

## Usage

*As with all Play projects, you must have JDK 1.8 and sbt installed.*

First of all, you need to install MySQL.

```bash
brew install mysql
mysql.server start
```

And you need to create db user `sampleuser` with password `changeme` like below.

```bash
CREATE USER 'sampleuser'@'localhost' IDENTIFIED by 'changeme';
GRANT ALL PRIVILEGES ON *.* TO 'sampleuser'@'localhost';
```

And you also need to create db `playdb` like below.

```bash
CREATE DATABASE playdb;
```

Next, you need to install libsodium.

```bash
brew install libsodium
```

If you want to store image files to Amazon S3, you should set `conf/application.conf`.

```bash
# Change isEnabled true & set configuration if you store images to S3.
s3{
    isEnabled = true
    accessKey = ""          # Please set your access key here.
    secretKey = ""          # Please set the secret key here.
    bucketName = ""         # Please set your bucket name here.
    serviceEndpoint = ""    # For example "s3-ap-northeast-1.amazonaws.com"
    regionName = ""         # For example "ap-northeast-1"
}
```

After that, you can start this app like below:

```bash
# Run within media-sample-scala directory.
sbt run
```

Now you can visit [`localhost:9000`](http://localhost:9000) from your browser.

## Dockerizing

Here is the repository of the app that dockerizing this app.

[`gifmura/docker-sample`](https://github.com/gifmura/docker-sample)

If you want, you can build a new docker image like below.

```bash
# Run within media-sample-scala directory.
sbt docker:publishLocal
```

## TODO

- [ ] Correspond the uploading and displaying of images with multiple extensions. (Now it only supports ".png", sorry.)

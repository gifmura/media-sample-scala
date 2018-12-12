# media-sample-scala

This is a sample project for developers intersted in Scala and Play Framework(2.6.x).

This project is featuring some basic and general topics like below:

* User registration
* Login & Logout
* List articles
* Show article details
* Post articles
* Relationships between database tables
* DB transaction
* Uploading and Displaying image files (only .png)
* Dockerize this application and run
* Secure sission (TODO)
* Unit testing (TODO)

## Preface

This project is intended to be used on Mac OS X or Linux.

And following instructions are only for Mac OS X.

## Usage

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

And restart MySQL

```bash
mysql.server restart
```

Next, you need to create a directory to save image files like below.

```bash
mkdir /tmp/mediasample
```

After that, you can start this app like below:

```bash
# Run within media-sample-scala directory.
sbt run
```

Now you can visit [`localhost:9000`](http://localhost:9000) from your browser.

## Dockerizing

Here is the repository of the application that dockerizing this.

[`gifmura/docker-sample`](https://github.com/gifmura/docker-sample)

If you want, you can build a new docker image like below.

```bash
# Run within media-sample-scala directory.
sbt docker:publishLocal
```

## TODO

- [ ] Correspond the uploading and displaying of images with multiple extensions.
- [ ] Secure sission.
- [ ] Unit testing.

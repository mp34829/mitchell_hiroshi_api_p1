# SignMeUp

SignMeUp is a course registration application that allows for users to easily view and register for 
courses that have been added to the application. Users are able to create courses and upload them to
application so that they can be visible to other users, additional configurations such as course
descriptions, start and end times, and max attendee limits can be specified as well. Users are 
able to register for open courses and view them in a convenient dashboard view that allows for
them to explore the details of the registered course.

## Technologies Used

* Java - version 1.8
* JavaxServlet - version 4.0.1
* JsonWebToken - version 0.9.0
* Jackson - version 2.12.4
* MongoDB - version 4.3.0
* Logback - version 1.2.5
* SLF4J - version 1.7.32
* JUnit - version 4.13.12
* Mockito - version 3.11.2

## Features

* Users can login or register student and faculty accounts
* Students can view all courses or available courses
* Students can enroll in a course
* Students can drop a course they're registered for
* Faculty members can view all courses
* Faculty members can create courses
* Faculty members can update courses
* Faculty members can delete a course
* Updated a course will also update schedules
* Deleting a course will unregister students in the course

To-do list:
* Users can update their profiles
* Students will not see courses they're registered for in the available list
* Faculty members can view students in a course
* Faculty members can drop students from a course
* Add more error messages throughout the application

## Getting Started
   
Clone the remote repository of the API to your machine by running this command in your terminal:
```
git clone https://github.com/mp34829/mitchell_hiroshi_api_p1/git
```

## Usage

After cloning and opening the project, you can deploy the application to a tomcat server as a WAR file.
In order to interact with the API, please use the UI found [here](https://github.com/roshmadosh/mitchell_hiroshi_ui_p1)

## Contributors

* Mitchell Panenko (mp34829)
* Hiroshi Nobuoka (roshmadosh)

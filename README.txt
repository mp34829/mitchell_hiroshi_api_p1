-----ENDPOINTS-----
- asterisks may or may not be needed, but our API has it set up like so

UserServlet:   "/user/*"
AuthServlet:	"/auth"
StudentServlet:	"/student/*"
BatchServlet:	"/batch/*"


-----POJOs-------

AppUser
 	String id;
   	String firstName;
   	String lastName;
    	String email;
    	String username;
    	String password;
    	List<String> batchRegistrations
    	String userPrivileges;

Batch
	String id;
   	String shortName;
  	String name;
    	String status;
    	String description;
   	Instant registrationStart;
    	Instant registrationEnd;
    	List<String> usersRegistered;
    
-----LOGGING IN-----

Login Page will allow
	- User to log in (AuthServlet POST)
			- Request body must look something like this:
			{
				"username":"admin",
				"password":"revature"
			}
	- Navigation to a registration page
	
Successful login => HTTP Response will include a token under the "Authorization" header.
		 => Navigates to either a student or faculty dashboard, depending on if their privilege field is 
					set to "0" (student) or "1" (faculty).

-----REGITERING A USER---------------

Register Page will allow
	- User to register
			- Request body must look something like this:
			{
			  "firstName": "Alice",
			  "lastName": "Anderson",
			  "email": "alice.anderson@gmail.com",
			  "username": "aanderson",
			  "password": "password",
			  "userPrivileges": "0"
			}

Registration successful => HTTP Response will include a token under the "Authorization" header.
			=> Navigates to either a student or faculty dashboard, depending on if their privilege field is 
						   set to "0" (student) or "1" (faculty).


-----DASHBOARDS--------------------------

Student Dashboard will allow

	- Viewing all USEABLE batches (BatchServlet's GET)
			- see Faculty dashboard for meaning of "useable"
			- Doesn't require a request body
			
	- Viewing all courses the student registered to (StudentServlet's GET)
			- Doesn't require a request body
			
	- Registering for an EXISTING, USABLE course (StudentServlet's POST)
			- Request body must look something like this. Shortname key MUST BE IN CAMEL CASE:
			{
				"shortName": "shortName"
			}
			
	- Updating personal info (UserServlet's PUT)
			- CANNOT change username. CAN change firstName, lastName, email, and password.
			- Request body must look something like this. To update first name and email:
			{
				"firstName": "Caleb",
				"email": "caleb.cameron@gmail.com"
			}
			
	- Withdrawing from a course they've actually registered for (StudentServlet's DELETE)
			- Request body must look something like this:
			{
				"shortName": "shortName"
			}

Faculty Dashboard will allow

	- Viewing all USEABLE batches
			- Useable means the batch must have the following fields, 
					status: "Enabled",
					RegistrationStart: "<before Instant.now()>",
					RegistrationEnd: "<after Instant.now()>"
			- Request body can be empty
				
	- Creating a batch
			- Request body must look something like this:
			{
			   "shortName":"shortName",
			   "name":"name",
			   "status":"Enabled",
			   "description":"description",
			   "registrationStart": "2016-05-28T17:39:44.937Z",
			   "registrationEnd": "2022-05-28T17:39:44.937Z"
			}
			
	- Updating batch details
			- User CANNOT update batch shortname. CAN update name, status, description, registrationStart, registrationEnd.
			- Request MUST include a shortname key-value pair, along with the fields you want to update.
			- Example:
			{
				"shortName":"shortName",
				"registrationEnd":"2023-06-15T17:39:44.937Z",
				"status":"Not Enabled"
			}
			- The fields in the request body don't have to be in order.
			
	- Delete an existing batch
			- Request body just needs the shortname key-value pair. Key MUST be in camel case:
			{
				"shortName": "shortName"
			}


------SIDE NOTES--------

	- Batches are not specific to a faculty member
	- There is no "admin" functionality. Only "student" and "faculty"
	- Working on displaying changes to the Batch field usersRegistered

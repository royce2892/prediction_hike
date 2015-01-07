===============
Informatics App
===============

========
Features
========
* Name 
* Gender
* Birthday
* Email
* Books read
* Movies seen
* Best three Friends
* Top three Apps used

The name , gender , birthday , email , books read and movies seen by the user are fetched by Facebook Login .
Simple Facebook open source library has been used for the same .

The algorithm used to determine the best three friends is as follows :
	Entire call log history of the user is analyzed 
	Friend quotient =Sum of outgoingcall duration * 2 + Sum of incoming call duraction * 1
	Numbers starting with 0 or +91 are stripped to 10 digits to prevent duplication
	Contact names of the top 3 numbers having maximum friend quotient is displayed

The algorithm used to determine most uses apps is as follows :
	A background service is started when the application is first started
	The service runs at an interval of three minutes and queries for the app running in foreground at that time
	App quotient = Number of times an application was running as a foreground process
	The interval of three minutes was chosen to minimize memory usage without trading off accuracy of the results

Please note - Top 3 apps will be shown as null when the app is run initially .The results gain accuracy as more information is collected by the App Tracking Service .
